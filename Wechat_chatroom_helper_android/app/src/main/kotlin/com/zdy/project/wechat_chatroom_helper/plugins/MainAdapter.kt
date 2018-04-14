package com.zdy.project.wechat_chatroom_helper.plugins

import android.content.ContentValues
import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.conversation.Classes
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainAdapter : IAdapterHook, IDatabaseHook {

    override fun onConversationAdapterCreated(adapter: BaseAdapter) {
        super.onConversationAdapterCreated(adapter)
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

    override fun onDatabaseUpdated(thisObject: Any, table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?, conflictAlgorithm: Int, result: Int): Operation<Int?> {
        return super.onDatabaseUpdated(thisObject, table, values, whereClause, whereArgs, conflictAlgorithm, result)
    }

    fun executeHook() {


        val conversationWithCacheAdapter = Classes.ConversationWithCacheAdapter

        findAndHookMethod(BaseAdapter::class.java, "getCount", object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return

            }
        })

        findAndHookMethod(BaseAdapter::class.java, "notifyDataSetChanged", object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return

            }
        })


        findAndHookMethod(conversationWithCacheAdapter, "getView", object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

            }
        })

    }
}