package com.zdy.project.wechat_chatroom_helper.hooker

import android.content.ContentValues
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
object MessageHooker : IDatabaseHook {
    override fun onDatabaseOpening(path: String, factory: Any?, flags: Int, errorHandler: Any?): Operation<Any?> {
        return super.onDatabaseOpening(path, factory, flags, errorHandler)
    }

    override fun onDatabaseOpened(path: String, factory: Any?, flags: Int, errorHandler: Any?, result: Any?): Operation<Any?> {
        return super.onDatabaseOpened(path, factory, flags, errorHandler, result)
    }

    override fun onDatabaseQuerying(thisObject: Any, factory: Any?, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: Any?): Operation<Any?> {
        return super.onDatabaseQuerying(thisObject, factory, sql, selectionArgs, editTable, cancellationSignal)
    }

    override fun onDatabaseQueried(thisObject: Any, factory: Any?, sql: String, selectionArgs: Array<String>?, editTable: String?, cancellationSignal: Any?, result: Any?): Operation<Any?> {
        return super.onDatabaseQueried(thisObject, factory, sql, selectionArgs, editTable, cancellationSignal, result)
    }

    override fun onDatabaseInserting(thisObject: Any, table: String, nullColumnHack: String?, initialValues: ContentValues?, conflictAlgorithm: Int): Operation<Long?> {
        return super.onDatabaseInserting(thisObject, table, nullColumnHack, initialValues, conflictAlgorithm)
    }

    override fun onDatabaseInserted(thisObject: Any, table: String, nullColumnHack: String?, initialValues: ContentValues?, conflictAlgorithm: Int, result: Long): Operation<Long?> {
        return super.onDatabaseInserted(thisObject, table, nullColumnHack, initialValues, conflictAlgorithm, result)
    }

    override fun onDatabaseUpdating(thisObject: Any, table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?, conflictAlgorithm: Int): Operation<Int?> {
        return super.onDatabaseUpdating(thisObject, table, values, whereClause, whereArgs, conflictAlgorithm)
    }

    override fun onDatabaseUpdated(thisObject: Any, table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?, conflictAlgorithm: Int, result: Int): Operation<Int?> {
        return super.onDatabaseUpdated(thisObject, table, values, whereClause, whereArgs, conflictAlgorithm, result)
    }

    override fun onDatabaseDeleting(thisObject: Any, table: String, whereClause: String?, whereArgs: Array<String>?): Operation<Int?> {
        return super.onDatabaseDeleting(thisObject, table, whereClause, whereArgs)
    }

    override fun onDatabaseDeleted(thisObject: Any, table: String, whereClause: String?, whereArgs: Array<String>?, result: Int): Operation<Int?> {
        return super.onDatabaseDeleted(thisObject, table, whereClause, whereArgs, result)
    }

    override fun onDatabaseExecuting(thisObject: Any, sql: String, bindArgs: Array<Any?>?, cancellationSignal: Any?): Boolean {
        return super.onDatabaseExecuting(thisObject, sql, bindArgs, cancellationSignal)
    }

    override fun onDatabaseExecuted(thisObject: Any, sql: String, bindArgs: Array<Any?>?, cancellationSignal: Any?) {
        super.onDatabaseExecuted(thisObject, sql, bindArgs, cancellationSignal)
    }
}