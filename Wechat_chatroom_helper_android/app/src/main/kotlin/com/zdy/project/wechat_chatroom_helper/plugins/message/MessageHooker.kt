package com.zdy.project.wechat_chatroom_helper.plugins.message

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.IMainAdapterRefresh
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHooker : IDatabaseHook {

    private const val SqlForGetFirstOfficial = "select rconversation.username from rconversation,rcontact where (rconversation.username = rcontact.username and rcontact.verifyFlag != 0 ) order by flag desc limit 1"
    private const val SqlForGetFirstChatroom = "select username from rconversation where  rconversation.username like '%@chatroom' order by flag desc limit 1"

    private const val KeyWordAllConversation1 = "digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite"
    private const val KeyWordAllConversation2 = "( 1 != 1  or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )"

    var mainAdapterRefresh = ArrayList<IMainAdapterRefresh>()

    fun addAdapterRefreshListener(adapterRefresh: IMainAdapterRefresh) {
        mainAdapterRefresh.add(adapterRefresh)
    }


    override fun onDatabaseOpened(path: String, factory: Any?, flags: Int, errorHandler: Any?, result: Any?): Operation<Any?> {

        Log.v("MessageHooker", "onDatabaseOpened, path = $path, factory = $factory ,flags = $flags ,errorHandler = $errorHandler, result = $result")
        return super.onDatabaseOpened(path, factory, flags, errorHandler, result)
    }


    override fun onDatabaseQueried(thisObject: Any, factory: Any?, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: Any?, result: Any?): Operation<Any?> {

//        Log.v("MessageHooker", "onDatabaseQueried, thisObject = $thisObject, factory = $factory ,sql = $sql " +
//                ",selectionArgs = ${with(selectionArgs) {
//
//                    when {
//                        selectionArgs == null -> "null selectionArgs"
//
//                        selectionArgs.isNotEmpty() -> {
//                            var string = ""
//                            selectionArgs.forEach { string += " $it" }
//                            string
//                        }
//                        else -> "empty selectionArgs"
//                    }
//
//                }}, editTable = $editTable, cancellationSignal = $cancellationSignal")

        val onDatabaseQueried = Operation.nop<Any>()


        if (sql.contains(KeyWordAllConversation1) && sql.contains(KeyWordAllConversation2)) {

            val cursorForOfficial = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, SqlForGetFirstOfficial, null, null) as Cursor
            val cursorForChatroom = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, SqlForGetFirstChatroom, null, null) as Cursor

            cursorForOfficial.moveToNext()
            val firstOfficialUsername = cursorForOfficial.getString(0)

            cursorForChatroom.moveToNext()
            val firstChatRoomUsername = cursorForChatroom.getString(0)



            XposedBridge.log("ConversationMsg, firstOfficialUsername  = $firstOfficialUsername")
            XposedBridge.log("ConversationMsg, firstChatRoomUsername  = $firstChatRoomUsername")


            val cursor = result as Cursor

            while (cursor.moveToNext()) {

                val username = cursor.getString(cursor.columnNames.indexOf("username"))

                if (username == firstOfficialUsername) {
                    XposedBridge.log("ConversationMsg, firstOfficialUsername, position = " + cursor.position + "username = $username")

                    mainAdapterRefresh.forEach {
                        it.onFirstOfficialRefresh(cursor.position, ChatInfoModel().apply {
                            nickname = cursor.getString(cursor.columnNames.indexOf("username"))
                            content = cursor.getString(cursor.columnNames.indexOf("digest"))
                            time = cursor.getString(cursor.columnNames.indexOf("conversationTime"))
                            unReadCount = cursor.getInt(cursor.columnNames.indexOf("unReadCount"))
                        })
                    }
                }

                if (username == firstChatRoomUsername) {
                    XposedBridge.log("ConversationMsg, firstChatRoomUsername, position = " + cursor.position + "username = $username")


                    mainAdapterRefresh.forEach {
                        it.onFirstOfficialRefresh(cursor.position, ChatInfoModel().apply {
                            nickname = cursor.getString(cursor.columnNames.indexOf("username"))
                            content = cursor.getString(cursor.columnNames.indexOf("digest"))
                            time = cursor.getString(cursor.columnNames.indexOf("conversationTime"))
                            unReadCount = cursor.getInt(cursor.columnNames.indexOf("unReadCount"))
                        })
                    }
                }

            }


            Log.v("MessageHooker1", "firstOfficialUsername = $firstOfficialUsername, firstChatRoomUsername = $firstChatRoomUsername")

            val sql1 = "select unReadCount, status, isSend, conversationTime, rconversation.username, content, msgType, flag, digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite " +
                    "from rconversation, rcontact where  ( parentRef is null  or parentRef = ''  ) " +
                    "and " +
                    // "(" +
                    "(rconversation.username = rcontact.username and rcontact.verifyFlag = 0) " +
                    //"or " +
                    //  "( rconversation.username = rcontact.username and rcontact.username = '" + firstOfficialUsername + "') " +
                    //    ") " +
                    "and " +
                    "( 1 != 1 or rconversation.username like '%@openim' or rconversation.username not like '%@%' ) " +
                    "and " +
                    "rconversation.username != 'qmessage' " +
                    //  "or " +
                    //  "(rconversation.username = rcontact.username " +
                    //  "and  " +
                    //  "rcontact.username = '" + firstChatRoomUsername + "'" +
                    //   ") " +
                    "order by flag desc"


//                val sql1 = "select unReadCount, status, isSend, conversationTime, rconversation.username, content, msgType, flag, digest, digestUser, " +
//                        "attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite " +
//                        "from rconversation, rcontact where  ( parentRef is null  or parentRef = '' )  " +
//                        "and (rconversation.username = rcontact.username and rcontact.verifyFlag = 0)" +
//                        "and ( 1 != 1 or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  " +
//                        "and rconversation.username != 'qmessage' order by flag desc"

            Log.v("MessageHooker1", "onDatabaseQueried, thisObject = $thisObject, factory = $factory ,sql = $sql1 " +
                    ",selectionArgs = ${with(selectionArgs) {
                        when {
                            selectionArgs == null -> "null selectionArgs"

                            selectionArgs.isNotEmpty() -> {
                                var string = ""
                                selectionArgs.forEach { string += " $it" }
                                string
                            }
                            else -> "empty selectionArgs"
                        }

                    }}, editTable = $editTable, cancellationSignal = $cancellationSignal")

            val result = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, sql1, selectionArgs, editTable, cancellationSignal)

            return Operation(result, 0, true)


        } else return onDatabaseQueried

    }

    override fun onDatabaseInserted(thisObject: Any, table: String, nullColumnHack: String?, initialValues: ContentValues?, conflictAlgorithm: Int, result: Long): Operation<Long?> {

        Log.v("MessageHooker", "onDatabaseInserted, thisObject = $thisObject, table = $table ,nullColumnHack = $nullColumnHack ,initialValues = $initialValues, conflictAlgorithm = $conflictAlgorithm, result = $result")

        return super.onDatabaseInserted(thisObject, table, nullColumnHack, initialValues, conflictAlgorithm, result)
    }

    override fun onDatabaseUpdated(thisObject: Any, table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?, conflictAlgorithm: Int, result: Int): Operation<Int?> {
        Log.v("MessageHooker", "onDatabaseUpdated, thisObject = $thisObject, table = $table ,values = $values ,whereClause = $whereClause, whereArgs = $whereArgs, conflictAlgorithm = $conflictAlgorithm, result = $result")
        return super.onDatabaseUpdated(thisObject, table, values, whereClause, whereArgs, conflictAlgorithm, result)
    }

    override fun onDatabaseDeleted(thisObject: Any, table: String, whereClause: String?, whereArgs: Array<String>?, result: Int): Operation<Int?> {

        Log.v("MessageHooker", "onDatabaseDeleted, thisObject = $thisObject, table = $table ,whereClause = $whereClause ,whereArgs = $whereArgs, result = $result")

        return super.onDatabaseDeleted(thisObject, table, whereClause, whereArgs, result)
    }

    override fun onDatabaseExecuted(thisObject: Any, sql: String, bindArgs: Array<Any?>?, cancellationSignal: Any?) {
        Log.v("MessageHooker", "onDatabaseExecuted, thisObject = $thisObject, sql = $sql ,bindArgs = $bindArgs ,cancellationSignal = $cancellationSignal")
        super.onDatabaseExecuted(thisObject, sql, bindArgs, cancellationSignal)
    }
}