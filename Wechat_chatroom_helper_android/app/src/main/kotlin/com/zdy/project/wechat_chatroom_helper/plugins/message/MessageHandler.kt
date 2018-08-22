package com.zdy.project.wechat_chatroom_helper.plugins.message

import android.content.ContentValues
import android.database.Cursor
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.MessageEventNotifyListener
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHandler {

    //查询当前第一个服务号会话信息
    private const val SqlForGetFirstOfficial = "select rconversation.username, flag from rconversation,rcontact " +
            "where ( rcontact.username = rconversation.username and rcontact.verifyFlag = 24) and ( parentRef is null  or parentRef = '' )  " +
            "and ( 1 !=1 or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  " +
            "and rconversation.username != 'qmessage' order by flag desc limit 1"

    //查询当前第一个群聊会话信息
    private const val SqlForGetFirstChatroom = "select username, flag from rconversation where  username like '%@chatroom' order by flag desc limit 1"


    private const val KeyWordFilterAllConversation1 = "UnReadInvite"
    private const val KeyWordFilterAllConversation2 = "by flag desc"

    private var sqlForAllContactConversation = "select unReadCount, status, isSend, conversationTime, rconversation.username, " +
            "content, msgType, flag, digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite " +
            "from rconversation, rcontact where  ( parentRef is null  or parentRef = ''  ) " +
            "and ( rconversation.username = rcontact.username and rcontact.verifyFlag = 0 ) " +
            "and ( 1 != 1 or rconversation.username like '%@openim' or rconversation.username not like '%@%'  ) " +
            "and rconversation.username != 'qmessage' " +
            "order by flag desc"

    private const val MessageDBName = "EnMicroMsg.db"

    //查询所有会话信息的筛选关键字
    private val SqlForAllConversationList =
            arrayOf("select unReadCount, status, isSend, conversationTime, username, content, msgType",
                    "digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite",
                    "( parentRef is null  or parentRef = '' )",
                    "( 1 != 1  or rconversation.username like '%@chatroom' or rconversation.username " +
                            "like '%@openim' or rconversation.username not like '%@%' )",
                    "and rconversation.username != 'qmessage'",
                    "order by flag desc")


    private fun isQueryAllConversation(sql: String) = SqlForAllConversationList.all { sql.contains(it) }


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

        cursorForOfficial.moveToNext()
        val firstOfficialUsername = cursorForOfficial.getString(0)

        cursorForChatRoom.moveToNext()
        val firstChatRoomUsername = cursorForChatRoom.getString(0)

        return Pair(firstOfficialUsername, firstChatRoomUsername)
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

                if (!sql.contains(KeyWordFilterAllConversation1)) return
                if (!sql.contains(KeyWordFilterAllConversation2)) return

                //如果本次查询是查询全部回话时，修改返回结果为全部联系人回话（不包括服务号和群聊）
                if (isQueryAllConversation(sql)) {

                    try {
                        XposedBridge.log("MessageHooker2.10, QUERY ALL CONVERSATION")

                        val (firstOfficialUsername, firstChatRoomUsername) = refreshEntryUsername(thisObject)
                        iMainAdapterRefreshes.forEach { it.onEntryInit(firstChatRoomUsername, firstOfficialUsername) }

                        PluginEntry.chatRoomViewPresenter.run { presenterView.post { setListInAdapterPositions(arrayListOf()) } }
                        PluginEntry.officialViewPresenter.run { presenterView.post { setListInAdapterPositions(arrayListOf()) } }

                        val result = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, sqlForAllContactConversation, selectionArgs, editTable, cancellation)

                        param.result = result

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                //当请求全部联系人回话时
                //确定服务号和群聊的入口位置
                else if (sql == sqlForAllContactConversation) {

                    XposedBridge.log("MessageHooker2.17,size = $sqlForAllContactConversation")


                    //额外查询两次，找到当前最新的服务号和群聊的最近消息时间
                    val cursorForOfficial = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, SqlForGetFirstOfficial, null, null) as Cursor
                    val cursorForChatRoom = XposedHelpers.callMethod(thisObject, WXObject.Message.M.QUERY, factory, SqlForGetFirstChatroom, null, null) as Cursor

                    var firstOfficialFlag: Long = 0
                    var firstChatRoomFlag: Long = 0

                    try {
                        cursorForOfficial.moveToNext()
                        firstOfficialFlag = cursorForOfficial.getLong(cursorForOfficial.getColumnIndex("flag"))

                        cursorForChatRoom.moveToNext()
                        firstChatRoomFlag = cursorForChatRoom.getLong(cursorForChatRoom.getColumnIndex("flag"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val cursor = param.result as Cursor


                    var officialPosition = -1
                    var chatRoomPosition = -1

                    //根据时间先后排序，确定入口的位置
                    while (cursor.moveToNext()) {

                        val flag = cursor.getLong(cursor.columnNames.indexOf("flag"))

                        if (flag < firstOfficialFlag && officialPosition == -1) {
                            officialPosition = cursor.position
                        }

                        if (flag < firstChatRoomFlag && chatRoomPosition == -1) {
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
                            if (firstOfficialFlag > firstChatRoomFlag) chatRoomPosition += 1 else officialPosition += 1
                        }
                    }

                    XposedBridge.log("MessageHooker2.17, chatRoomPosition = $chatRoomPosition, officialPosition = $officialPosition")


                    iMainAdapterRefreshes.forEach { it.onEntryPositionChanged(chatRoomPosition, officialPosition) }

                    //恢复数据库游标为起始位置
                    cursor.move(0)
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