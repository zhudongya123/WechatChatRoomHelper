package com.zdy.project.wechat_chatroom_helper.plugins.message

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.MessageEventNotifyListener
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHandler : IDatabaseHook {

    private const val SqlForGetFirstOfficial = "select rconversation.username from rconversation,rcontact where ( rcontact.username = rconversation.username and rcontact.verifyFlag = 24) and ( parentRef is null  or parentRef = '' )  and ( 1 !=1 or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  and rconversation.username != 'qmessage' order by flag desc limit 1"
    private const val SqlForGetFirstChatroom = "select username from rconversation where  username like '%@chatroom' order by flag desc limit 1"

    private val SqlForAllConversationList =
            arrayOf("select unReadCount, status, isSend, conversationTime, username, content, msgType",
                    "digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite",
                    "( parentRef is null  or parentRef = '' )",
                    "( 1 != 1  or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )",
                    "and rconversation.username != 'qmessage'",
                    "order by flag desc")

    private const val KeyWordFilterAllConversation1 = "UnReadInvite"
    private const val KeyWordFilterAllConversation2 = "by flag desc"


    var sqlForAllConversationAndEntry = ""

    private var iMainAdapterRefreshes = ArrayList<MessageEventNotifyListener>()

    fun addMessageEventNotifyListener(messageEventNotifyListener: MessageEventNotifyListener) {
        iMainAdapterRefreshes.add(messageEventNotifyListener)
    }

    override fun onDatabaseOpened(path: String, factory: Any?, flags: Int, errorHandler: Any?, result: Any?): Operation<Any?> {
        Log.v("MessageHandler", "onDatabaseOpened, path = $path, factory = $factory ,flags = $flags ,errorHandler = $errorHandler, result = $result")
        return super.onDatabaseOpened(path, factory, flags, errorHandler, result)
    }


    override fun onDatabaseQuerying(thisObject: Any, factory: Any?, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: Any?): Operation<Any?> {

        var stringArray = ""

        selectionArgs?.let { it.forEach { stringArray += "$it ," } }

        Log.v("MessageHandler", "onDatabaseQuerying, thisObject = $thisObject, sql = $sql, selectionArgs  = $stringArray,  ")

        return super.onDatabaseQuerying(thisObject, factory, sql, selectionArgs, editTable, cancellationSignal)
    }

    override fun onDatabaseQueried(thisObject: Any, factory: Any?, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: Any?, result: Any?): Operation<Any?> {

        val path = thisObject.toString()
        if (path.endsWith("EnMicroMsg.db")) {
            if (WechatGlobal.MainDatabaseObject !== thisObject) {
                WechatGlobal.MainDatabaseObject = thisObject
            }
        }


//        try {
//
//            Log.v("MessageHandler", "onDatabaseQueried, thisObject = $thisObject, sql = $sql sql getCount  = ${(result as Cursor).count}")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

        val onDatabaseQueried = Operation.nop<Any>()

        if (!sql.contains(KeyWordFilterAllConversation1)) return onDatabaseQueried
        if (!sql.contains(KeyWordFilterAllConversation2)) return onDatabaseQueried

        if (SqlForAllConversationList.all { sql.contains(it) }) {


            try {
                XposedBridge.log("MessageHooker2.10, QUERY ALL CONVERSATION")

                val (firstOfficialUsername, firstChatRoomUsername) = refreshEntryUsername(thisObject)

                sqlForAllConversationAndEntry = "select unReadCount, status, isSend, conversationTime, rconversation.username, " +
                        "content, msgType, flag, digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite " +
                        "from rconversation, rcontact where  ( parentRef is null  or parentRef = ''  ) " +
                        "and " +
                        "(" + "(" +
                        "rconversation.username = rcontact.username and rcontact.verifyFlag = 0" +
                        ") or ( " +
                        "rconversation.username = rcontact.username and rcontact.username = '" + firstOfficialUsername + "'" +
                        ")) and ( " +
                        "1 != 1 or rconversation.username like '%@openim' or rconversation.username not like '%@%' " +
                        ") and " +
                        "rconversation.username != 'qmessage' " +
                        "or (" +
                        "rconversation.username = rcontact.username " +
                        "and  " +
                        "rcontact.username = '" + firstChatRoomUsername + "'" +
                        ") " +
                        "order by flag desc"


                PluginEntry.chatRoomViewPresenter.setListInAdapterPositions(arrayListOf())
                PluginEntry.officialViewPresenter.setListInAdapterPositions(arrayListOf())


                val result = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, sqlForAllConversationAndEntry, selectionArgs, editTable, cancellationSignal)

                return Operation(result, 0, true)

            } catch (e: Exception) {
                e.printStackTrace()
                return onDatabaseQueried
            }
        }
        //        else if (sql == sqlForAllConversationAndEntry) {
//
//            val cursorForOfficial = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, SqlForGetFirstOfficial, null, null) as Cursor
//            val cursorForChatroom = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, SqlForGetFirstChatroom, null, null) as Cursor
//
//            cursorForOfficial.moveToNext()
//            val firstOfficialNickname = cursorForOfficial.getString(0)
//
//            cursorForChatroom.moveToNext()
//            val firstChatRoomNickname = cursorForChatroom.getString(0)
//
//            val cursor = result as Cursor
//
//            var officialChatInfoModel = ChatInfoModel()
//            var chatRoomChatInfoModel = ChatInfoModel()
//
//            var officialPosition = -1
//            var chatRoomPosition = -1
//
//            while (cursor.moveToNext()) {
//
//                val nickname = cursor.getString(cursor.columnNames.indexOf("username"))
////
////                val chatInfoModel = ChatInfoModel().also {
////                    it.nickname = nickname
////                    it.time = cursor.getString(cursor.columnNames.indexOf("conversationTime"))
////                    it.unReadCount = cursor.getInt(cursor.columnNames.indexOf("unReadCount"))
////                    it.content = cursor.getString(cursor.columnNames.indexOf("digest"))
////                    it.avatarString = cursor.getString(cursor.columnNames.indexOf("unReadMuteCount"))
////                }
//                XposedBridge.log("MessageHooker2.5,nickname = $nickname, cursor.position = ${cursor.position}, $firstOfficialNickname, $firstChatRoomNickname")
//
//
//                if (nickname == firstOfficialNickname) {
//                //    officialChatInfoModel = chatInfoModel
//                    officialPosition = cursor.position
//
//                    XposedBridge.log("MessageHooker2.5,找到了第一个公众号 firstOfficialInfoModel = $nickname, cursor.position = ${cursor.position}")
//
//                }
//
//                if (nickname == firstChatRoomNickname) {
//                 //   chatRoomChatInfoModel = chatInfoModel
//                    chatRoomPosition = cursor.position
//
//                   XposedBridge.log("MessageHooker2.5,找到了第一个聊天 firstChatRoomNickname = $nickname, cursor.position = ${cursor.position}")
//                }
//            }
////
//            XposedBridge.log("MessageHooker2, firstChatroomPosition = $chatRoomPosition, firstChatRoomNickname = $firstChatRoomNickname \n")
//            XposedBridge.log("MessageHooker2, firstOfficialPosition = $officialPosition, firstOfficialNickname = $firstOfficialNickname \n")
////
//            iMainAdapterRefreshes.forEach { it.onEntryRefresh(chatRoomPosition, chatRoomChatInfoModel, officialPosition, officialChatInfoModel) }
//
//            cursor.move(0)
//
//            return onDatabaseQueried
//
//        }
        else return onDatabaseQueried
    }

    private fun refreshEntryUsername(thisObject: Any): Pair<String, String> {
        val mCursorFactory = XposedHelpers.findField(thisObject::class.java, "mCursorFactory").apply { isAccessible = true }.get(thisObject)


        val cursorForOfficial = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", MessageFactory.getDataBaseFactory(thisObject), SqlForGetFirstOfficial, null, null) as Cursor
        val cursorForChatroom = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", MessageFactory.getDataBaseFactory(thisObject), SqlForGetFirstChatroom, null, null) as Cursor

        cursorForOfficial.moveToNext()
        val firstOfficialUsername = cursorForOfficial.getString(0)

        cursorForChatroom.moveToNext()
        val firstChatRoomUsername = cursorForChatroom.getString(0)

        iMainAdapterRefreshes.forEach { it.onEntryRefresh(firstChatRoomUsername,  firstOfficialUsername) }
        return Pair(firstOfficialUsername, firstChatRoomUsername)
    }

    override fun onDatabaseInserted(thisObject: Any, table: String, nullColumnHack: String?, initialValues: ContentValues?, conflictAlgorithm: Int, result: Long): Operation<Long?> {

        val operation = super.onDatabaseInserted(thisObject, table, nullColumnHack, initialValues, conflictAlgorithm, result)


        if (table == "message") {
            if (initialValues == null) return operation
            if (!initialValues.containsKey("msgId")) return operation
            if (!initialValues.containsKey("talker")) return operation
            if (!initialValues.containsKey("createTime")) return operation
            if (!initialValues.containsKey("content")) return operation

            refreshEntryUsername(thisObject)

            var talker = initialValues.getAsString("talker")
            var createTime = initialValues.getAsLong("createTime")
            var content = initialValues.get("content")

        }

        Log.v("MessageHandler", "onDatabaseInserted, thisObject = $thisObject, table = $table ,nullColumnHack = $nullColumnHack ,initialValues = $initialValues, conflictAlgorithm = $conflictAlgorithm, result = $result")

        return operation
    }

    override fun onDatabaseUpdated(thisObject: Any, table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?, conflictAlgorithm: Int, result: Int): Operation<Int?> {
        Log.v("MessageHandler", "onDatabaseUpdated, thisObject = $thisObject, table = $table ,values = $values ,whereClause = $whereClause, whereArgs = ${whereArgs.toString()}, conflictAlgorithm = $conflictAlgorithm, result = $result")

        val path = thisObject.toString()
        if (path.endsWith("EnMicroMsg.db")) {
            if (WechatGlobal.MainDatabaseObject !== thisObject) {
                WechatGlobal.MainDatabaseObject = thisObject
            }
        }

        return super.onDatabaseUpdated(thisObject, table, values, whereClause, whereArgs, conflictAlgorithm, result)

    }

    override fun onDatabaseDeleted(thisObject: Any, table: String, whereClause: String?, whereArgs: Array<String>?, result: Int): Operation<Int?> {

        Log.v("MessageHandler", "onDatabaseDeleted, thisObject = $thisObject, table = $table ,whereClause = $whereClause ,whereArgs = $whereArgs, result = $result")

        return super.onDatabaseDeleted(thisObject, table, whereClause, whereArgs, result)
    }

    override fun onDatabaseExecuted(thisObject: Any, sql: String, bindArgs: Array<Any?>?, cancellationSignal: Any?) {
        Log.v("MessageHandler", "onDatabaseExecuted, thisObject = $thisObject, sql = $sql ,bindArgs = $bindArgs ,cancellationSignal = $cancellationSignal")
        super.onDatabaseExecuted(thisObject, sql, bindArgs, cancellationSignal)
    }


}