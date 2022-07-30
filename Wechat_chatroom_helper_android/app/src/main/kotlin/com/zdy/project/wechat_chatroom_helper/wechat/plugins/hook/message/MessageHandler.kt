package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.interfaces.MessageEventNotifyListener
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHandler {

    private const val MessageDBName = "EnMicroMsg.db"

    //查询当前第一个服务号的会话信息
    private const val SqlForGetFirstOfficial = "select rconversation.username, flag, rconversation.conversationTime from rconversation,rcontact " +
            "where ( rcontact.username = rconversation.username and rcontact.verifyFlag != 0) and ( parentRef is null  or parentRef = '' or parentRef = 'message_fold' )  " +
            "and ( 1 !=1 or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  " +
            "and rconversation.username != 'qmessage' order by flag desc limit 1"

    //查询当前第一个群聊的会话信息
    private const val SqlForGetFirstChatroom = "select username, flag, conversationTime from rconversation " +
            "where  username like '%@chatroom' " +
            "and ( parentRef is null  or parentRef = '' or parentRef = 'message_fold' ) " +
            "order by flag desc limit 1"

    //查询除去服务号和群聊的sql语句，可以通过拼接添加自定义名单
    private var SqlForNewAllContactConversation =
            arrayOf("select rconversation.unReadCount, rconversation.status, rconversation.isSend, " +
                    "rconversation.conversationTime, rconversation.username, rconversation.content, " +
                    "rconversation.msgType, rconversation.flag, rconversation.digest, " +
                    "rconversation.digestUser, rconversation.attrflag, rconversation.editingMsg, " +
                    "rconversation.atCount, rconversation.unReadMuteCount, " +
                    "rconversation.UnReadInvite, rconversation.hasTodo, rconversation.hbMarkRed, " +
                    "rconversation.remitMarkRed, rconversation.parentRef " +
                    "from rconversation, rcontact where " +
                    "( rconversation.username = rcontact.username and rcontact.verifyFlag = 0 and rcontact.username not like '%@chatroom') ",
                    "and  ( parentRef is null  or parentRef = '' or parentRef = 'message_fold' )",
                    "and rconversation.username != 'qmessage' and rconversation.username != 'appbrand_notify_message' and rconversation.username != 'message_fold' ",
                    "order by flag desc")

    //微信原始的查询所有消息回话的语句，通过分段来筛选出相关逻辑
    private val FilterListForOriginAllConversation =
            arrayOf("select unReadCount, status, isSend, conversationTime, username, content, msgType,",
                    "parentRef is null",
                    "or parentRef = ''",
                    "and rconversation.username != 'qmessage'",
                    "order by flag desc")


    @Deprecated("微信老版本废弃")
    private val FilterListForOriginAllUnread1 =
            arrayOf("select sum(unReadCount) from rconversation, rcontact",
                    "(rconversation.parentRef is null or parentRef = '' )",
                    "1 != 1  or rconversation.username like",
                    "rconversation.username like '%@chatroom'",
                    "( type & 512 ) == 0",
                    "rcontact.username != 'officialaccounts'")

    @Deprecated("微信老版本废弃， verifyFlag为0时一定不是订阅号和服务号")
    private const val FilterListForOriginAllUnread2 = "rcontact.verifyFlag == 0"

    @Deprecated("微信8.0.2废弃")
    private val FilterListForOriginAllUnread3 = arrayOf(
            "select unReadCount from rconversation",
            "AND (parentRef is null or parentRef = '' )  ",
            "and ( 1 != 1  or rconversation.username like '%@im.chatroom' ",
            "or rconversation.username like '%@chatroom' ",
            "or rconversation.username like '%@openim' ",
            "or rconversation.username not like '%@%' ) ")

    //微信原始的查询消息未读数的语句，通过分段来筛选出相关逻辑
    private val FilterListForOriginAllUnread4 = arrayOf("SELECT rconversation.username, rconversation.unReadCount, rconversation.conversationTime",
            "from rconversation inner join rcontact", "" +
            "WHERE rconversation.username = rcontact.username AND unReadCount > 0",
            "AND  ( parentRef is null or parentRef = '' )",
            "and ( rcontact.usernameFlag in ( 4 , 2 , 65536 , 0 )  )")


    //判断当前sql语句是否为微信原始的未读数逻辑
    private fun isQueryOriginAllUnReadCount(sql: String) = FilterListForOriginAllUnread4.all { sql.contains(it) }

    //判断当前sql语句是否为微信原始的会话列表逻辑
    private fun isQueryOriginAllConversation(sql: String) = FilterListForOriginAllConversation.all { sql.contains(it) }

    //判断当前sql语句是否为我们自定义的会话列表【已经去除了群聊和服务号】逻辑
    private fun isQueryNewAllConversation(sql: String) = SqlForNewAllContactConversation.all { sql.contains(it) }

    /**
     * 连接数据库的对象
     */
    var MessageDatabaseObject: Any? = null

    private var iMainAdapterRefreshes = ArrayList<MessageEventNotifyListener>()

    var totalUnReadCount: Int = 0

    fun addMessageEventNotifyListener(messageEventNotifyListener: MessageEventNotifyListener) {
        iMainAdapterRefreshes.add(messageEventNotifyListener)
    }

    /**
     * 查询并保存服务号和群聊的入口
     */
    private fun refreshEntryUsername(thisObject: Any): Pair<String, String> {

        val cursorForOfficial = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY,
                MessageFactory.getDataBaseFactory(thisObject), SqlForGetFirstOfficial, null, null) as Cursor
        val cursorForChatRoom = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY,
                MessageFactory.getDataBaseFactory(thisObject), SqlForGetFirstChatroom, null, null) as Cursor

        val chatRoomCount = cursorForChatRoom.count
        val officialCount = cursorForOfficial.count

        if (officialCount != 0 && chatRoomCount != 0) {
            cursorForOfficial.moveToNext()
            val firstOfficialUsername = cursorForOfficial.getString(0)
            cursorForChatRoom.moveToNext()
            val firstChatRoomUsername = cursorForChatRoom.getString(0)
            return Pair(firstOfficialUsername, firstChatRoomUsername)
        } else {
            return if (officialCount == 0 && chatRoomCount != 0) {
                cursorForChatRoom.moveToNext()
                val firstChatRoomUsername = cursorForChatRoom.getString(0)
                Pair("", firstChatRoomUsername)
            } else if (officialCount != 0 && chatRoomCount == 0) {
                cursorForOfficial.moveToNext()
                val firstOfficialUsername = cursorForOfficial.getString(0)
                Pair(firstOfficialUsername, "")
            } else {
                Pair("", "")
            }
        }

    }

    fun executeHook() {
        val database =
                XposedHelpers.findClass(WXObject.Message.C.SQLiteDatabase, RuntimeInfo.classloader)
        val databaseFactory =
                XposedHelpers.findClass(WXObject.Message.C.SQLiteDatabaseCursorFactory, RuntimeInfo.classloader)
        val databaseCancellationSignal =
                XposedHelpers.findClass(WXObject.Message.C.SQLiteCancellationSignal, RuntimeInfo.classloader)

        val queryHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                LogUtils.log("MessageHandler, queryHook, invoke")

                val thisObject = param.thisObject
                val factory = param.args[0]
                val sql = param.args[1] as String
                val selectionArgs = param.args[2] as Array<String>?
                val editTable = param.args[3] as String?
                val cancellation = param.args[4]

                val path = thisObject.toString()

                if (path.endsWith(MessageDBName)) {
                    if (MessageDatabaseObject !== thisObject) {
                        MessageDatabaseObject = thisObject
                    }
                }

                LogUtils.log("MessageHandler, queryHook, sql = $sql")
                if (!sql.contains("parentRef is null")) return

                val cursor = param.result as Cursor

                when {
                    /**
                     * 如果本次查询是查询全部回话时（包括服务号和群聊）
                     * 修改返回结果为全部联系人回话（不包括服务号和群聊）
                     */
                    isQueryOriginAllConversation(sql) -> {

                        LogUtils.log("MessageHandler, isQueryOriginAllConversation")
                        LogUtils.log("MessageHandler, originConversationSize = ${cursor.count}")

                        /**
                         * 先获取两个助手的 username (包含Sql查询)
                         */
                        refreshEntryUsername(thisObject).let { pair ->
                            val firstChatRoomUsername = pair.first
                            val firstOfficialUsername = pair.second
                            iMainAdapterRefreshes.forEach { it.onEntryInit(firstChatRoomUsername, firstOfficialUsername) }
                        }

                        /**
                         * 刷新两个助手的列表
                         */
                        RuntimeInfo.chatRoomViewPresenter?.refreshList(false, Any())
                        RuntimeInfo.officialViewPresenter?.refreshList(false, Any())

                        /**
                         * 获取两个群聊和服务号的白名单
                         */
                        val list = AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_CHAT_ROOM).plus(AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_OFFICIAL))

                        /**
                         * sqlForAllConversation 这个变量是新的sql查询语句，用来过滤主页的群聊和服务号
                         */
                        val sqlForAllConversation =

                                //没有白名单，直接全部拼起来就完事
                                if (list.isEmpty()) {
                                    SqlForNewAllContactConversation.joinToString("") { it }
                                }

                                //有白名单就需要将白名单列表也在 sql 中拼起来
                                else {
                                    val mutableList = SqlForNewAllContactConversation.toMutableList()

                                    val postfix = mutableList.last()//最后一个
                                    val prefix = mutableList.dropLast(1).joinToString("") { it }//除去最后一个拼接字符串

                                    //组合字符串
                                    prefix + list.joinToString("' or rconversation.username = '", "or ( rconversation.username = rcontact.username and ( rconversation.username = '", "' )) ") { it } + postfix
                                }

                        LogUtils.log("MessageHandler, sqlForAllConversation =  $sqlForAllConversation")

                        val result = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, sqlForAllConversation, selectionArgs, editTable, cancellation)
                        param.result = result
                    }

                    /**
                     * 当请求全部联系人回话时（不包括服务号和群聊）
                     */
                    isQueryNewAllConversation(sql) && !sql.contains(", rconversation.flag desc") -> {

                        LogUtils.log("MessageHandler, isQueryNewAllConversation")
                        LogUtils.log("MessageHandler, SqlForNewAllContactConversation size = ${SqlForNewAllContactConversation.joinToString("") { it }}")

                        //额外查询两次，找到当前最新的服务号和群聊的最近消息时间
                        val cursorForOfficial = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, SqlForGetFirstOfficial, null, null) as Cursor
                        val cursorForChatRoom = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, SqlForGetFirstChatroom, null, null) as Cursor


                        val firstOfficialConversationTime: Long = if (cursorForOfficial.count > 0) {
                            try {
                                cursorForOfficial.moveToNext()
                                cursorForOfficial.getLong(cursorForOfficial.getColumnIndex("flag"))
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                0L
                            }

                        } else {
                            0L
                        }

                        val firstChatRoomConversationTime: Long = if (cursorForChatRoom.count > 0) {
                            try {
                                cursorForChatRoom.moveToNext()
                                cursorForChatRoom.getLong(cursorForChatRoom.getColumnIndex("flag"))
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                0L
                            }
                        } else {
                            0L
                        }

                        var officialPosition = -1
                        var chatRoomPosition = -1

                        /**
                         * 根据时间先后排序，确定入口的位置
                         *遍历每一条回话，比较会话时间
                         */
                        while (cursor.moveToNext()) {
                            val conversationTime = cursor.getLong(cursor.columnNames.indexOf("flag"))

                            if (conversationTime < firstOfficialConversationTime && officialPosition == -1) {
                                officialPosition = cursor.position
                            }

                            if (conversationTime < firstChatRoomConversationTime && chatRoomPosition == -1) {
                                chatRoomPosition = cursor.position
                            }
                        }

                        /**
                         * 根据入口先后调整插入的位置
                         */
                        if (officialPosition != -1 && chatRoomPosition != -1) {
                            if (officialPosition > chatRoomPosition) {
                                officialPosition += 1
                            } else if (officialPosition < chatRoomPosition) {
                                chatRoomPosition += 1
                            } else if (officialPosition == chatRoomPosition) {
                                if (firstOfficialConversationTime > firstChatRoomConversationTime) chatRoomPosition += 1 else officialPosition += 1
                            }
                        } else if (officialPosition == -1 && chatRoomPosition != -1) {
                            officialPosition = cursor.count + 1
                        } else if (officialPosition != -1 && chatRoomPosition == -1) {
                            chatRoomPosition = cursor.count + 1
                        } else {
                            chatRoomPosition = cursor.count + 1
                            officialPosition = cursor.count
                        }

                        LogUtils.log("MessageHook 2019-04-12 16:05:28, chatRoomPosition = $chatRoomPosition, officialPosition = $officialPosition")

                        iMainAdapterRefreshes.forEach { it.onEntryPositionChanged(chatRoomPosition, officialPosition) }


                        totalUnReadCount = 0
                        while (cursor.moveToNext()) {
                            val unReadCount = cursor.getLong(cursor.columnNames.indexOf("unReadCount"))
                            totalUnReadCount += unReadCount.toInt()
                        }


                        LogUtils.log("MessageHandler, refreshNewAllConversation size = ${cursor.count}")

                        /**
                         * 2021-09-18
                         * 新逻辑
                         *
                         * 2022-01-07 加 rconversation.flag desc 是为了避免重复请求的循坏
                         */
                        val newSqlForAllConversation = "$sql, rconversation.flag desc"
                        val result = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, newSqlForAllConversation, selectionArgs, editTable, cancellation)
                        param.result = result
                    }


                    /**
                     * 当查询未读数时的修改逻辑
                     *
                     * verifyFlag 0为服务号
                     */
                    isQueryOriginAllUnReadCount(sql) -> {


                        if (sql.contains("verifyFlag")) return

                        val officialList = AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_OFFICIAL)


                        var newUnReadCountSql = "$sql and ( rcontact.username = rconversation.username and ( rcontact.verifyFlag = 0"

                        if (officialList.size != 0) {
                            newUnReadCountSql += officialList.joinToString("' or rconversation.username = '", " or  rconversation.username = '", "'") { it }
                        }

                        newUnReadCountSql += " ))"


                        /**
                         * 白名单群聊逻辑暂时去除 非免打扰群聊本来就正确显示未读数
                         */
//                        try {
//                            val unMuteChatRoomList = MessageFactory.getUnMuteChatRoomList(MessageFactory.getSpecChatRoom()).map { it.field_username }
//                            if (unMuteChatRoomList.isNotEmpty()) {
//                                newUnReadCountSql += unMuteChatRoomList.joinToString("' and rconversation.username != '", " and rconversation.username != '", "' ") { it }
//                            }
//                        } catch (e: Throwable) {
//                            e.printStackTrace()
//                        }

                        LogUtils.log("MessageHandler, sqlForAllUnReadCount =  $newUnReadCountSql")

                        param.result = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, newUnReadCountSql, selectionArgs, editTable, cancellation)


                    }
                }

            }
        }

        try {
            XposedHelpers.findAndHookMethod(database, WXObject.Message.M.QUERY, databaseFactory,
                    String::class.java, Array<Any>::class.java, String::class.java,
                    databaseCancellationSignal, queryHook)
            LogUtils.log("PluginEntry, MessageHandler hook query DataBase")

        } catch (e: NoSuchMethodError) {
            XposedHelpers.findAndHookMethod(database, WXObject.Message.M.QUERY, databaseFactory,
                    String::class.java, Array<Any>::class.java, String::class.java,
                    databaseCancellationSignal, queryHook)
            LogUtils.log("PluginEntry, MessageHandler  hook query DataBase NoSuchMethodError")
        }

        XposedHelpers.findAndHookMethod(database, WXObject.Message.M.INSERT,
                String::class.java, String::class.java, ContentValues::class.java, Int::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {

                        val thisObject = param.thisObject
                        val table = param.args[0] as String
                        val nullColumnHack = param.args[1] as String?
                        val initialValues = param.args[2] as ContentValues?
                        val conflictAlgorithm = param.args[3] as Int

                        if (table == "message") {

                            if (initialValues == null) return
                            if (!initialValues.containsKey("msgId")) return
                            if (!initialValues.containsKey("talker")) return
                            if (!initialValues.containsKey("createTime")) return
                            if (!initialValues.containsKey("content")) return

                            val pair = refreshEntryUsername(thisObject)

                            iMainAdapterRefreshes.forEach { it.onEntryRefresh(pair.first, pair.second) }

                            val talker = initialValues.getAsString("talker")
                            val createTime = initialValues.getAsLong("createTime")
                            val content = initialValues.get("content")

                            iMainAdapterRefreshes.forEach { it.onNewMessageCreate(talker, createTime, content) }

                        }
                    }
                })
        LogUtils.log("PluginEntry, MessageHandler  hook INSERT DataBase ")
        XposedHelpers.findAndHookMethod(database, WXObject.Message.M.UPDATE,
                String::class.java, ContentValues::class.java, String::class.java,
                Array<String>::class.java, Int::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {

                        val thisObject = param.thisObject

                        val path = thisObject.toString()
                        if (path.endsWith(MessageDBName)) {
                            if (MessageDatabaseObject !== thisObject) {
                                MessageDatabaseObject = thisObject
                            }
                        }
                    }
                })
        LogUtils.log("PluginEntry, MessageHandler  hook UPDATE DataBase ")

    }
}