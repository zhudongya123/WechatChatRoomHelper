package com.zdy.project.wechat_chatroom_helper.wechat.plugins.message

import android.content.ContentValues
import android.database.Cursor
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.interfaces.MessageEventNotifyListener
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHandler {

    private const val MessageDBName = "EnMicroMsg.db"


    //查询当前第一个服务号会话信息
    private const val SqlForGetFirstOfficial = "select rconversation.username, flag from rconversation,rcontact " +
            "where ( rcontact.username = rconversation.username and rcontact.verifyFlag = 24) and ( parentRef is null  or parentRef = '' )  " +
            "and ( 1 !=1 or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  " +
            "and rconversation.username != 'qmessage' order by flag desc limit 1"

    //查询当前第一个群聊会话信息
    private const val SqlForGetFirstChatroom = "select username, flag from rconversation where  username like '%@chatroom' order by flag desc limit 1"

    private var SqlForAllContactConversation = arrayOf("select unReadCount, status, isSend, conversationTime, rconversation.username, ",
            "content, msgType, flag, digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite ",
            "from rconversation, rcontact where  ( parentRef is null  or parentRef = ''  ) ",
            "and ( rconversation.username = rcontact.username and rcontact.verifyFlag = 0 ) ",
            "and ( 1 != 1 or rconversation.username like '%@openim' or rconversation.username not like '%@%'  ) ",
            "and rconversation.username != 'qmessage' ",
            "order by flag desc")

    private var SqlForAllUnreadCount = "select sum(unReadCount) from rconversation, rcontact where rconversation.unReadCount > 0 " +
            "AND (rconversation.parentRef is null or parentRef = '' ) " +
            "AND rconversation.username = rcontact.username " +
            "AND rcontact.verifyFlag != 24 " +
            "AND ( 1 != 1  or rconversation.username like '%@im.chatroom' or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  " +
            "AND ( type & 512 ) == 0 " +
            "AND rcontact.username != 'officialaccounts' " +
            "AND rconversation.username != 'floatbottle' " +
            "AND rconversation.username != 'notifymessage'"


    private val FilterListForOriginAllConversation =
            arrayOf("select unReadCount, status, isSend, conversationTime, username, content, msgType",
                    "digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite",
                    "( parentRef is null  or parentRef = '' )",
                    "( 1 != 1  or rconversation.username like",
                    "'%@chatroom' or rconversation.username like '%@openim'",
                    "or rconversation.username not like '%@%' )",
                    "and rconversation.username != 'qmessage'",
                    "order by flag desc")

    private val FilterListForOriginAllUnread1 =
            arrayOf("select sum(unReadCount) from rconversation, rcontact",
                    "(rconversation.parentRef is null or parentRef = '' )",
                    "1 != 1  or rconversation.username like",
                    "rconversation.username like '%@chatroom'",
                    "( type & 512 ) == 0",
                    "rcontact.username != 'officialaccounts'",
                    "rconversation.username != 'notifymessage'")

    private const val FilterListForOriginAllUnread2 = "AND rcontact.verifyFlag != 24"

    private fun isQueryOriginAllUnReadCount(sql: String) = FilterListForOriginAllUnread1.all { sql.contains(it) } && !sql.contains(FilterListForOriginAllUnread2)

    private fun isQueryOriginAllConversation(sql: String) = FilterListForOriginAllConversation.all { sql.contains(it) }


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

        if (cursorForOfficial.count != 0 && cursorForChatRoom.count != 0) {
            cursorForOfficial.moveToNext()
            val firstOfficialUsername = cursorForOfficial.getString(0)
            cursorForChatRoom.moveToNext()
            val firstChatRoomUsername = cursorForChatRoom.getString(0)
            return Pair(firstOfficialUsername, firstChatRoomUsername)
        } else {
            return if (cursorForOfficial.count == 0 && cursorForChatRoom.count != 0) {
                cursorForChatRoom.moveToNext()
                val firstChatRoomUsername = cursorForChatRoom.getString(0)
                Pair("", firstChatRoomUsername)
            } else if (cursorForOfficial.count != 0 && cursorForChatRoom.count == 0) {
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
                XposedHelpers.findClass(WXObject.Message.C.SQLiteDatabase, PluginEntry.classloader)
        val databaseFactory =
                XposedHelpers.findClass(WXObject.Message.C.SQLiteDatabaseCursorFactory, PluginEntry.classloader)
        val databaseCancellationSignal =
                XposedHelpers.findClass(WXObject.Message.C.SQLiteCancellationSignal, PluginEntry.classloader)

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

                if (!sql.contains("unReadCount")) return

                //如果本次查询是查询全部回话时，修改返回结果为全部联系人回话（不包括服务号和群聊）
                when {
                    isQueryOriginAllConversation(sql) -> {
                        try {
                            val (firstOfficialUsername, firstChatRoomUsername) = refreshEntryUsername(thisObject)
                            iMainAdapterRefreshes.forEach { it.onEntryInit(firstChatRoomUsername, firstOfficialUsername) }

                            PluginEntry.chatRoomViewPresenter.run { presenterView.post { setListInAdapterPositions(arrayListOf()) } }
                            PluginEntry.officialViewPresenter.run { presenterView.post { setListInAdapterPositions(arrayListOf()) } }


                            val list = AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_CHAT_ROOM).apply { addAll(AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_OFFICIAL)) }

                            val sqlForAllConversation = if (list.size == 0) {
                                SqlForAllContactConversation.joinToString("", "", "", -1, "") { it }
                            } else {
                                val mutableList = SqlForAllContactConversation.toMutableList()

                                val postfix = mutableList.last()
                                val prefix = mutableList
                                        .apply { removeAt(SqlForAllContactConversation.size - 1) }
                                        .joinToString("", "", "", -1, "") { it }

                                prefix + list.joinToString("", "", "", -1, "") { " or rconversation.username = $it " } + postfix
                            }

                            LogUtils.log("sqlForAllConversation =  $sqlForAllConversation")

                            val result = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, sqlForAllConversation, selectionArgs, editTable, cancellation)

                            param.result = result
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    isQueryOriginAllUnReadCount(sql) -> {
                        try {
                            param.result = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, SqlForAllUnreadCount, selectionArgs, editTable, cancellation)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                //当请求全部联系人回话时
                //确定服务号和群聊的入口位置
                    sql == SqlForAllContactConversation.joinToString("", "", "", -1, "") { it } -> {

                        //      LogUtils.log("MessageHooker2.17,size = $SqlForAllContactConversation")

                        //额外查询两次，找到当前最新的服务号和群聊的最近消息时间
                        val cursorForOfficial = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, SqlForGetFirstOfficial, null, null) as Cursor
                        val cursorForChatRoom = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, SqlForGetFirstChatroom, null, null) as Cursor

                        var firstOfficialConversationTime: Long = 0
                        var firstChatRoomConversationTime: Long = 0

                        try {
                            if (cursorForOfficial.count > 0) {
                                cursorForOfficial.moveToNext()
                                firstOfficialConversationTime = cursorForOfficial.getLong(cursorForOfficial.getColumnIndex("flag"))
                            }

                            if (cursorForChatRoom.count > 0) {
                                cursorForChatRoom.moveToNext()
                                firstChatRoomConversationTime = cursorForChatRoom.getLong(cursorForChatRoom.getColumnIndex("flag"))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        val cursor = param.result as Cursor

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
                        }

                        LogUtils.log("MessageHooker2.17, chatRoomPosition = $chatRoomPosition, officialPosition = $officialPosition")


                        iMainAdapterRefreshes.forEach { it.onEntryPositionChanged(chatRoomPosition, officialPosition) }

                        //恢复数据库游标为起始位置
                        cursor.move(0)
                    }
                }

            }
        }
        XposedHelpers.findAndHookMethod(database, WXObject.Message.M.QUERY, databaseFactory,
                String::class.java, Array<String>::class.java, String::class.java,
                databaseCancellationSignal, queryHook)

        XposedHelpers.findAndHookMethod(database, WXObject.Message.M.INSERT,
                String::class.java, String::class.java, ContentValues::class.java, Int::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {

                        val thisObject = param.thisObject
                        val table = param.args[0] as String
                        val nullColumnHack = param.args[1] as String?
                        val initialValues = param.args[2] as ContentValues?
                        val conflictAlgorithm = param.args[3] as Int
                        val result = param.result as Long


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