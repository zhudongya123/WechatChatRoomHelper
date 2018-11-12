package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter

import android.app.Activity
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor

object MainAdapterLongClick {

    var onItemLongClickMethodInvokeGetItemFlag = ""
    var onCreateContextMenuMethodInvokeGetNameFlag = -1

    val CoordinateField = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader).declaredFields.firstOrNull { it.type == IntArray::class.java }!!

    fun getConversationLongClickClassConstructor(): Constructor<*> {
        val longClickClass = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader)
        return longClickClass.getConstructor(MainAdapter.originAdapter::class.java, ListView::class.java, Activity::class.java, IntArray::class.java)
    }

}