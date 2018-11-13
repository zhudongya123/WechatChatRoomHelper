package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter

import android.app.Activity
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor

object MainAdapterLongClick {

    var onItemLongClickMethodInvokeGetItemFlagNickName = ""
    var onCreateContextMenuMethodInvokeGetNameFlag = -1

    val CoordinateField = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader).declaredFields.firstOrNull { it.type == IntArray::class.java }!!

    fun getConversationLongClickClassConstructor(): Constructor<*> {
        val longClickClass = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader)
        return longClickClass.getConstructor(MainAdapter.originAdapter::class.java, ListView::class.java, Activity::class.java, IntArray::class.java)
    }


    fun executeHook() {

        val conversationLongClickListener = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader)

        XposedHelpers.findAndHookMethod(conversationLongClickListener, WXObject.Adapter.M.OnItemLongClick,
                AdapterView::class.java, View::class.java, Int::class.java, Long::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

                //当在助手里的长按事件
                if (onItemLongClickMethodInvokeGetItemFlagNickName != "") {
                    return
                }

                val index = param.args[2]

                /**
                 * 当在主页面的助手入口时，修改下标为0，这样会以为是headerView，没有长按事件
                 */
                if (MainAdapter.firstChatRoomPosition != -1 && (MainAdapter.firstChatRoomPosition + MainAdapter.listView.headerViewsCount) == index) {
                    param.args[2] = 0
                }

                if (MainAdapter.firstOfficialPosition != -1 && (MainAdapter.firstOfficialPosition + MainAdapter.listView.headerViewsCount) == index) {
                    param.args[2] = 0
                }
            }

        })
    }
}