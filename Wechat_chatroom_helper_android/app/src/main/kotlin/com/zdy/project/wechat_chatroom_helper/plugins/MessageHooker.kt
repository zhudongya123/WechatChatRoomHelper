package com.zdy.project.wechat_chatroom_helper.plugins

import android.content.ContentValues
import android.util.Log
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHooker : IDatabaseHook {

    private var flag = true


    override fun onDatabaseOpened(path: String, factory: Any?, flags: Int, errorHandler: Any?, result: Any?): Operation<Any?> {

        Log.v("MessageHooker", "onDatabaseOpened, path = $path, factory = $factory ,flags = $flags ,errorHandler = $errorHandler, result = $result")
        return super.onDatabaseOpened(path, factory, flags, errorHandler, result)
    }

    override fun onDatabaseQueried(thisObject: Any, factory: Any?, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: Any?, result: Any?): Operation<Any?> {

        Log.v("MessageHooker", "onDatabaseQueried, thisObject = $thisObject, factory = $factory ,sql = $sql " +
                ",selectionArgs = ${with(selectionArgs) {
                    var string = ""
                    selectionArgs?.forEach {
                        string += " $it"

                    }
                    return@with string

                }}, editTable = $editTable, cancellationSignal = $cancellationSignal")

        val onDatabaseQueried = Operation.nop<Any>()

        if (sql.contains("UnReadInvite")) return onDatabaseQueried
        if (sql.contains("rconversation.username like '%@openim'")) return onDatabaseQueried
        if (sql.contains("rconversation.username like '%@chatroom'")) return onDatabaseQueried
        if (sql.contains("( parentRef is null  or parentRef = '' )  and ( 1 != 1  or")) return onDatabaseQueried
        if (sql.contains("(rconversation.username = rcontact.username and rcontact.verifyFlag == 0)")) return onDatabaseQueried
        if (selectionArgs?.size != 0) return onDatabaseQueried
        if (editTable != null) return onDatabaseQueried
        if (cancellationSignal != null) return onDatabaseQueried


        try {
            val sql1 = "select rconversation.unReadCount, rconversation.status, rconversation.isSend, rconversation.conversationTime, rconversation.username, rconversation.content, rconversation.msgType, rconversation.flag, rconversation.digest, rconversation.digestUser, rconversation.attrflag, rconversation.editingMsg, rconversation.atCount, rconversation.unReadMuteCount, rconversation.UnReadInvite\n" +
                    "    from rconversation, rcontact where  ( parentRef is null  or parentRef = '' )  \n" +
                    "    and (rconversation.username = rcontact.username and rcontact.verifyFlag == 0)\n" +
                    "    and ( 1 != 1  or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  \n" +
                    "    and rconversation.username != 'qmessage' order by flag desc"

            Log.v("MessageHooker1", "onDatabaseQueried, thisObject = $thisObject, factory = $factory ,sql = $sql1 " +
                    ",selectionArgs = ${with(selectionArgs) {
                        var string = ""
                        selectionArgs.forEach {
                            string += " $it"

                        }
                        return@with string

                    }}, editTable = $editTable, cancellationSignal = $cancellationSignal")

            val result = XposedHelpers.callMethod(thisObject, "rawQueryWithFactory", factory, sql1, null, editTable, cancellationSignal)

            return Operation(result, 0, true)

        } catch (e: Exception) {
            e.printStackTrace()
            return onDatabaseQueried
        }


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