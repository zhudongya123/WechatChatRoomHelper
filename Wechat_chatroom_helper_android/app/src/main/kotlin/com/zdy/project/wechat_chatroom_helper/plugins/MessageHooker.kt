package com.zdy.project.wechat_chatroom_helper.plugins

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHooker : IDatabaseHook {


    override fun onDatabaseOpened(path: String, factory: Any?, flags: Int, errorHandler: Any?, result: Any?): Operation<Any?> {

        Log.v("MessageHooker", "onDatabaseOpened, path = $path, factory = $factory ,flags = $flags ,errorHandler = $errorHandler, result = $result")
        return super.onDatabaseOpened(path, factory, flags, errorHandler, result)
    }

    /**
     *
     * select sum(unReadCount) from rconversation,
    rcontact where rconversation.unReadCount > 0
    AND (rconversation.parentRef is null or parentRef = '' )
    AND rconversation.username = rcontact.username
    and ( 1 != 1  or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )
    AND ( type & 512 ) == 0 AND rcontact.username != 'officialaccounts'




    select unReadCount, status, isSend, conversationTime, username, content, msgType, flag, digest, digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite
    from rconversation where  ( parentRef is null  or parentRef = '' )
    and ( 1 != 1  or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )
    and rconversation.username != 'qmessage' order by flag desc
     *
     *
     */
    override fun onDatabaseQuerying(thisObject: Any, factory: Any?, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: Any?): Operation<Any?> {


        if (sql.contains("rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username")) {

            Log.v("MessageHooker", "onDatabaseQuerying, thisObject = $thisObject, factory = $factory ,sql = $sql ,selectionArgs = ${selectionArgs.toString()}, editTable = $editTable, cancellationSignal = $cancellationSignal")

            val sql1 = sql.replace("rconversation.username like '%@openim' or", " ")

            Log.v("MessageHooker", "onDatabaseQuerying, thisObject = $thisObject, factory = $factory ,sql = $sql1 ,selectionArgs = ${selectionArgs.toString()}, editTable = $editTable, cancellationSignal = $cancellationSignal")

//            return

            return Operation(super.onDatabaseQuerying(thisObject, factory, sql1, selectionArgs, editTable, cancellationSignal), 0, true)

        }




        return Operation(super.onDatabaseQuerying(thisObject, factory, sql, selectionArgs, editTable, cancellationSignal), 0, true)
    }

    override fun onDatabaseQueried(thisObject: Any, factory: Any?, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: Any?, result: Any?): Operation<Any?> {

        Log.v("MessageHooker", "onDatabaseQueried, thisObject = $thisObject, factory = $factory ,sql = $sql ,selectionArgs = ${selectionArgs.toString()}, editTable = $editTable, cancellationSignal = $cancellationSignal, result = $result")

//
//        if (!sql.contains("rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username"))
//            return super.onDatabaseQueried(thisObject, factory, sql, selectionArgs, editTable, cancellationSignal, result)
//        val cursor = result as Cursor
//
//        var toString = cursor.columnNames.let {
//
//            var string = ""
//
//            it.forEach { string += (it + " , ") }
//
//            return@let string
//
//        }
//
//        var moveToFirst = cursor.moveToFirst()
//
//        while (cursor.moveToNext()) {
//
//            var find: String? = cursor.columnNames.find { it == "username" } ?: break
//
//
//
//            Log.v("MessageHooker", "onDatabaseQueried, username = " + cursor.getString(cursor.getColumnIndex(find)))
//        }
//
//        cursor.moveToFirst()

        //      Log.v("MessageHooker", "onDatabaseQueried, cursor.columnNames = $toString, cursor.count =${cursor.count}")

        return super.onDatabaseQueried(thisObject, factory, sql, selectionArgs, editTable, cancellationSignal, result)
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