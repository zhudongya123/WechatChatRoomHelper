package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message

import android.content.ContentValues
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.BaseAdapter
import android.widget.ListAdapter
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapter
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
            "where ( rcontact.username = rconversation.username and rcontact.verifyFlag != 0) and ( parentRef is null  or parentRef = '' )  " +
            "and ( 1 !=1 or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  " +
            "and rconversation.username != 'qmessage' order by conversationTime desc limit 1"

    //查询当前第一个群聊的会话信息
    private const val SqlForGetFirstChatroom = "select username, flag, conversationTime from rconversation where  username like '%@chatroom' order by conversationTime desc limit 1"

    //查询除去服务号和群聊的sql语句，可以通过拼接添加自定义名单
    private var SqlForNewAllContactConversation = arrayOf("select unReadCount, status, isSend, conversationTime, rconversation.username, ",
            "content, msgType, flag, digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite, hasTodo ",
            "from rconversation, rcontact " +
                    "where  " +
                    "( rconversation.parentRef is null  or rconversation.parentRef = ''  ) ",
            "and ( rconversation.username = rcontact.username and rcontact.verifyFlag = 0 ) ",
            "and ( 1 != 1 " +
                    "or rconversation.username like '%@openim' " +
                    "or rconversation.username not like '%@%'  " +
                    ") ",
            "and rconversation.username != 'qmessage' and rconversation.username != 'appbrand_notify_message' ",
            "order by flag desc")

    //查询除去服务号的未读消息总数(不完整 查看相关逻辑)
    private var SqlForNewAllUnreadCount = "select sum(unReadCount) from rconversation, rcontact where rconversation.unReadCount > 0 " +
            "AND (rconversation.parentRef is null or parentRef = '' ) " +
            "AND rconversation.username = rcontact.username " +

            "AND ( 1 != 1  " +
            "or rconversation.username like '%@im.chatroom' " +
            "or rconversation.username like '%@chatroom' " +
            "or rconversation.username like '%@openim' " +
            "or rconversation.username not like '%@%' )  " +

            "AND ( type & 512 ) == 0 " +
            "AND rcontact.username != 'officialaccounts' " +
            "AND rconversation.username != 'floatbottle' " +
            "AND rconversation.username != 'notifymessage' "

    //微信原始的查询所有消息回话的语句，通过分段来筛选出相关逻辑
    private val FilterListForOriginAllConversation =
            arrayOf("select unReadCount, status, isSend, conversationTime, username, content, msgType",
                    "digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite",
                    "( parentRef is null  or parentRef = '' )",
                    "( 1 != 1  or rconversation.username like",
                    "'%@chatroom' or rconversation.username like '%@openim'",
                    "or rconversation.username not like '%@%' )",
                    "and rconversation.username != 'qmessage'",
                    "order by flag desc")

    //微信原始的查询消息未读数的语句，通过分段来筛选出相关逻辑
    private val FilterListForOriginAllUnread1 =
            arrayOf("select sum(unReadCount) from rconversation, rcontact",
                    "(rconversation.parentRef is null or parentRef = '' )",
                    "1 != 1  or rconversation.username like",
                    "rconversation.username like '%@chatroom'",
                    "( type & 512 ) == 0",
                    "rcontact.username != 'officialaccounts'")

    private const val FilterListForOriginAllUnread2 = "rcontact.verifyFlag == 0"

    //判断当前sql语句是否为微信原始的未读数逻辑
    private fun isQueryOriginAllUnReadCount(sql: String) = FilterListForOriginAllUnread1.all { sql.contains(it) } && !sql.contains(FilterListForOriginAllUnread2)

    //判断当前sql语句是否为微信原始的未读数逻辑
    private fun isQueryOriginAllConversation(sql: String) = FilterListForOriginAllConversation.all { sql.contains(it) }

    //判断当前sql语句是否为我们自定义的未读数逻辑
    private fun isQueryNewAllConversation(sql: String) = SqlForNewAllContactConversation.all { sql.contains(it) }

    var MessageDatabaseObject: Any? = null

    private var iMainAdapterRefreshes = ArrayList<MessageEventNotifyListener>()

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

                if (!sql.contains("parentRef is null")) return

                val cursor = param.result as Cursor

                when {
                    /**
                     * 如果本次查询是查询全部回话时
                     * 修改返回结果为全部联系人回话（不包括服务号和群聊）
                     */
                    isQueryOriginAllConversation(sql) -> {

                        LogUtils.log("MessageHandler, queryHook, sql = $sql")
                        LogUtils.log("MessageHandler, originConversationSize = ${cursor.count}")

                        LogUtils.log("MessageHandler, refreshAllConversation")

                        /**
                         * 先获取两个助手的 username (包含Sql查询)
                         */
                        refreshEntryUsername(thisObject).let {
                            val firstChatRoomUsername = it.first
                            val firstOfficialUsername = it.second
                            iMainAdapterRefreshes.forEach { it.onEntryInit(firstChatRoomUsername, firstOfficialUsername) }
                        }

                        /**
                         * 刷新两个助手的列表
                         */
                        RuntimeInfo.chatRoomViewPresenter.refreshList(false, Any())
                        RuntimeInfo.officialViewPresenter.refreshList(false, Any())

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

                        LogUtils.log("sqlForAllConversation =  $sqlForAllConversation")


                        val result = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, sqlForAllConversation, selectionArgs, editTable, cancellation)
                        param.result = result
                    }

                    /**
                     * 当请求全部联系人回话时（就是去除群聊和服务号的情况）
                     */
                    isQueryNewAllConversation(sql) -> {
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

                        //根据时间先后排序，确定入口的位置
                        //遍历每一条回话，比较会话时间
                        while (cursor.moveToNext()) {
                            val conversationTime = cursor.getLong(cursor.columnNames.indexOf("flag"))

                            if (conversationTime < firstOfficialConversationTime && officialPosition == -1) {
                                officialPosition = cursor.position
                            }

                            if (conversationTime < firstChatRoomConversationTime && chatRoomPosition == -1) {
                                chatRoomPosition = cursor.position
                            }
                        }

                        //根据入口先后调整插入的位置
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

                        //恢复数据库游标为起始位置
                        cursor.move(0)

                        LogUtils.log("MessageHandler, refreshNewAllConversation size = ${cursor.count}")
                    }


                    //当查询未读数时的修改逻辑
                    isQueryOriginAllUnReadCount(sql) -> {

                        /*
                        LogUtils.log("MessageHandler, refreshAllConversationUnReadCount")

                        val officialList = AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_OFFICIAL)

                        var sqlForAllUnReadCount =
                                if (officialList.size == 0) {
                                    "$SqlForNewAllUnreadCount and rcontact.verifyFlag == 0 "
                                } else {
                                    SqlForNewAllUnreadCount +
                                            officialList.joinToString("' or rconversation.username = '", " and ( rconversation.username = rcontact.username and ( rconversation.username = '", "' or rcontact.verifyFlag == 0 ))") { it }
                                }


                        try {
                            val unMuteChatRoomList = MessageFactory.getUnMuteChatRoomList(MessageFactory.getSpecChatRoom()).map { it.field_username }
                            if (unMuteChatRoomList.isNotEmpty()) {
                                sqlForAllUnReadCount += unMuteChatRoomList.joinToString("' and rconversation.username != '", " and rconversation.username != '", "' ") { it }
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }

                        LogUtils.log("sqlForAllUnReadCount =  $sqlForAllUnReadCount")
                        param.result = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, sqlForAllUnReadCount, selectionArgs, editTable, cancellation)

                         */
                    }
                }

            }
        }
        try {
            XposedHelpers.findAndHookMethod(database, WXObject.Message.M.QUERY, databaseFactory,
                    String::class.java, Array<Any>::class.java, String::class.java,
                    databaseCancellationSignal, queryHook)

            Log.v("PluginEntry", "MessageHandler line 318")

        } catch (e: NoSuchMethodError) {
            XposedHelpers.findAndHookMethod(database, WXObject.Message.M.QUERY, databaseFactory,
                    String::class.java, Array<Any>::class.java, String::class.java,
                    databaseCancellationSignal, queryHook)

            Log.v("PluginEntry", "MessageHandler line 325")
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


    }


}