package com.zdy.project.wechat_chatroom_helper.plugins.message

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.MessageEventNotifyListener
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHandler {

    private const val SqlForGetFirstOfficial = "select rconversation.username, flag from rconversation,rcontact where ( rcontact.username = rconversation.username and rcontact.verifyFlag = 24) and ( parentRef is null  or parentRef = '' )  and ( 1 !=1 or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  and rconversation.username != 'qmessage' order by flag desc limit 1"
    private const val SqlForGetFirstChatroom = "select username, flag from rconversation where  username like '%@chatroom' order by flag desc limit 1"

    private val SqlForAllConversationList =
            arrayOf("select unReadCount, status, isSend, conversationTime, username, content, msgType",
                    "digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite",
                    "( parentRef is null  or parentRef = '' )",
                    "( 1 != 1  or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )",
                    "and rconversation.username != 'qmessage'",
                    "order by flag desc")

    private const val KeyWordFilterAllConversation1 = "UnReadInvite"
    private const val KeyWordFilterAllConversation2 = "by flag desc"


    var MessageDatabaseObject: Any? = null

    var sqlForAllConversationAndEntry = ""

    private var iMainAdapterRefreshes = ArrayList<MessageEventNotifyListener>()

    fun addMessageEventNotifyListener(messageEventNotifyListener: MessageEventNotifyListener) {
        iMainAdapterRefreshes.add(messageEventNotifyListener)
    }

    private fun refreshEntryUsername(thisObject: Any): Pair<String, String> {

        val cursorForOfficial = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", MessageFactory.getDataBaseFactory(thisObject), SqlForGetFirstOfficial, null, null) as Cursor
        val cursorForChatroom = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", MessageFactory.getDataBaseFactory(thisObject), SqlForGetFirstChatroom, null, null) as Cursor

        cursorForOfficial.moveToNext()
        val firstOfficialUsername = cursorForOfficial.getString(0)

        cursorForChatroom.moveToNext()
        val firstChatRoomUsername = cursorForChatroom.getString(0)

        return Pair(firstOfficialUsername, firstChatRoomUsername)
    }

    fun executeHook() {

        val database =
                XposedHelpers.findClass(WXObject.SQLiteDatabase, PluginEntry.classloader)
        val databaseFactory =
                XposedHelpers.findClass(WXObject.SQLiteDatabaseCursorFactory, PluginEntry.classloader)
        val databaseCancellationSignal =
                XposedHelpers.findClass(WXObject.SQLiteCancellationSignal, PluginEntry.classloader)

        val queryHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {

                val thisObject = param.thisObject
                val factory = param.args[0]
                val sql = param.args[1] as String
                val selectionArgs = param.args[2] as Array<String>?
                val editTable = param.args[3] as String?
                val cancellation = param.args[4]

                val path = thisObject.toString()

                if (path.endsWith("EnMicroMsg.db")) {
                    if (MessageDatabaseObject !== thisObject) {
                        MessageDatabaseObject = thisObject
                    }
                }

                if (!sql.contains(KeyWordFilterAllConversation1)) return
                if (!sql.contains(KeyWordFilterAllConversation2)) return

                if (SqlForAllConversationList.all { sql.contains(it) }) {

                    try {
                        XposedBridge.log("MessageHooker2.10, QUERY ALL CONVERSATION")

                        val (firstOfficialUsername, firstChatRoomUsername) = refreshEntryUsername(thisObject)
                        iMainAdapterRefreshes.forEach { it.onEntryInit(firstChatRoomUsername, firstOfficialUsername) }

                        sqlForAllConversationAndEntry = "select unReadCount, status, isSend, conversationTime, rconversation.username, " +
                                "content, msgType, flag, digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite " +
                                "from rconversation, rcontact where  ( parentRef is null  or parentRef = ''  ) " +
                                "and " +
//                        "(" +
                                "(" +
                                "rconversation.username = rcontact.username and rcontact.verifyFlag = 0" +
                                ") " +
//                        "or ( " +
//                        "rconversation.username = rcontact.username and rcontact.username = '" + firstOfficialUsername + "'" +
//                        ")" +
//                        ") " +
                                "and ( " +
                                "1 != 1 or rconversation.username like '%@openim' or rconversation.username not like '%@%' " +
                                ") and " +
                                "rconversation.username != 'qmessage' " +
//                        "or (" +
//                        "rconversation.username = rcontact.username " +
//                        "and  " +
//                        "rcontact.username = '" + firstChatRoomUsername + "'" +
//                        ") " +
                                "order by flag desc"

                        PluginEntry.chatRoomViewPresenter?.run { presenterView.post { setListInAdapterPositions(arrayListOf()) } }
                        PluginEntry.officialViewPresenter?.run { presenterView.post { setListInAdapterPositions(arrayListOf()) } }

                        val result = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, sqlForAllConversationAndEntry, selectionArgs, editTable, cancellation)

                        param.result = result

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (sql == sqlForAllConversationAndEntry) {

                    val cursorForOfficial = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, SqlForGetFirstOfficial, null, null) as Cursor
                    val cursorForChatroom = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, SqlForGetFirstChatroom, null, null) as Cursor


                    var firstOfficialFlag: Long = 0
                    var firstChatRoomFlag: Long = 0

                    try {
                        cursorForOfficial.moveToNext()
                        firstOfficialFlag = cursorForOfficial.getLong(cursorForOfficial.getColumnIndex("flag"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        cursorForChatroom.moveToNext()
                        firstChatRoomFlag = cursorForChatroom.getLong(cursorForChatroom.getColumnIndex("flag"))
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }

                    val cursor = param.result as Cursor

                    var officialPosition = -1
                    var chatRoomPosition = -1

                    while (cursor.moveToNext()) {

                        //   val nickname = cursor.getString(cursor.columnNames.indexOf("username"))

//                val chatInfoModel = ChatInfoModel().also {
//                    it.nickname = nickname
//                    it.time = cursor.getString(cursor.columnNames.indexOf("conversationTime"))
//                    it.unReadCount = cursor.getInt(cursor.columnNames.indexOf("unReadCount"))
//                    it.content = cursor.getString(cursor.columnNames.indexOf("digest"))
//                    it.avatarString = cursor.getString(cursor.columnNames.indexOf("unReadMuteCount"))
//                }
//                XposedBridge.log("MessageHooker2.5,nickname = $nickname, cursor.position = ${cursor.position}, $firstOfficialNickname, $firstChatRoomNickname")

                        val flag = cursor.getLong(cursor.columnNames.indexOf("flag"))


                        if (flag < firstOfficialFlag && officialPosition == -1) {
                            officialPosition = cursor.position
                        }


                        if (flag < firstChatRoomFlag && chatRoomPosition == -1) {
                            chatRoomPosition = cursor.position
                        }


//                if (nickname == firstOfficialNickname) {
//                    //    officialChatInfoModel = chatInfoModel
//                    officialPosition = cursor.position
//
//                    XposedBridge.log("MessageHooker2.5,找到了第一个公众号 firstOfficialInfoModel = $nickname, cursor.position = ${cursor.position}")
//
//                }
//
//                if (nickname == firstChatRoomNickname) {
//                    //   chatRoomChatInfoModel = chatInfoModel
//                    chatRoomPosition = cursor.position
//
//                    XposedBridge.log("MessageHooker2.5,找到了第一个聊天 firstChatRoomNickname = $nickname, cursor.position = ${cursor.position}")
//                }
                    }

                    if (officialPosition != -1 && chatRoomPosition != -1) {
                        if (officialPosition > chatRoomPosition) {
                            officialPosition += 1
                        } else if (officialPosition < chatRoomPosition) {
                            chatRoomPosition += 1
                        } else if (officialPosition == chatRoomPosition) {
                            if (firstOfficialFlag > firstChatRoomFlag) chatRoomPosition += 1 else officialPosition += 1
                        }
                    }

                    iMainAdapterRefreshes.forEach { it.onEntryPositionChanged(chatRoomPosition, officialPosition) }


//            XposedBridge.log("MessageHooker2, firstChatroomPosition = $chatRoomPosition, firstChatRoomNickname = $firstChatRoomNickname \n")
//            XposedBridge.log("MessageHooker2, firstOfficialPosition = $officialPosition, firstOfficialNickname = $firstOfficialNickname \n")

//            iMainAdapterRefreshes.forEach { it.onEntryRefresh(chatRoomPosition, chatRoomChatInfoModel, officialPosition, officialChatInfoModel) }

                    cursor.move(0)

                }

            }
        }
        XposedHelpers.findAndHookMethod(database, "rawQueryWithFactory", databaseFactory,
                String::class.java, Array<String>::class.java, String::class.java,
                databaseCancellationSignal, queryHook)

        XposedHelpers.findAndHookMethod(database, "insertWithOnConflict",
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

                        Log.v("MessageHandler", "onDatabaseInserted, thisObject = $thisObject, table = $table ,nullColumnHack = $nullColumnHack ,initialValues = $initialValues, conflictAlgorithm = $conflictAlgorithm, result = $result")

                    }
                })
        XposedHelpers.findAndHookMethod(database, "updateWithOnConflict",
                String::class.java, ContentValues::class.java, String::class.java,
                Array<String>::class.java, Int::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {

                        val thisObject = param.thisObject

                        val path = thisObject.toString()
                        if (path.endsWith("EnMicroMsg.db")) {
                            if (MessageDatabaseObject !== thisObject) {
                                MessageDatabaseObject = thisObject
                            }
                        }
                    }
                })
    }


}