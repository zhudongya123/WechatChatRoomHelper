package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter

import android.app.Activity
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor

object MainAdapterLongClick {


    var currentLongClickUsername = ""
    var onItemLongClickMethodInvokeGetItemFlagNickName = ""
    var onCreateContextMenuMethodInvokeGetChatRoomFlag = false
    var onCreateContextMenuMethodInvokeGetOfficialFlag = false


    val CoordinateField = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader).declaredFields.firstOrNull { it.type == IntArray::class.java }!!

    fun getConversationLongClickClassConstructor(): Constructor<*> {
        val longClickClass = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader)
        return longClickClass.getConstructor(MainAdapter.originAdapter::class.java, ListView::class.java, Activity::class.java, IntArray::class.java)
    }


    fun executeHook() {

        val conversationLongClickListener = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader)
        val conversationMenuItemSelectedListener = XposedHelpers.findClass(WXObject.Adapter.C.ConversationMenuItemSelectedListener, RuntimeInfo.classloader)

        XposedHelpers.findAndHookMethod(conversationLongClickListener, WXObject.Adapter.M.OnItemLongClick,
                AdapterView::class.java, View::class.java, Int::class.java, Long::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {


                val longClickListener = param.thisObject
                currentLongClickUsername = XposedHelpers.getObjectField(longClickListener, "talker") as String



                //当在助手里的长按事件
                if (onItemLongClickMethodInvokeGetItemFlagNickName != "") {
                    return
                }

                val index = param.args[2]

                /**
                 * 当在主页面的助手入口时，修改下标为0，这样会以为是headerView，没有长按事件
                 */
                if (MainAdapter.firstChatRoomPosition != -1 && (MainAdapter.firstChatRoomPosition + MainAdapter.listView.headerViewsCount) == index) {
                    onCreateContextMenuMethodInvokeGetChatRoomFlag = true
                }

                if (MainAdapter.firstOfficialPosition != -1 && (MainAdapter.firstOfficialPosition + MainAdapter.listView.headerViewsCount) == index) {
                    onCreateContextMenuMethodInvokeGetOfficialFlag = true
                }
            }

        })


        XposedHelpers.findAndHookMethod(conversationLongClickListener, WXObject.Adapter.M.OnCreateContextMenu,
                ContextMenu::class.java, View::class.java, ContextMenu.ContextMenuInfo::class.java, object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {

                val contextMenu = param.args[0]
                val view = param.args[1]
                val contextMenuInfo = param.args[2]

                LogUtils.log("OnCreateContextMenu, contextMenu = $contextMenu, view = $view, contextMenuInfo = $contextMenuInfo")

                for (method in contextMenu::class.java.methods) {
                    LogUtils.log("OnCreateContextMenu, method = $method")
                }

                val position = XposedHelpers.getIntField(contextMenuInfo, "position")

                if (onCreateContextMenuMethodInvokeGetChatRoomFlag) {
                    onCreateContextMenuMethodInvokeGetChatRoomFlag = false
                    LogUtils.log("OnCreateContextMenu, method = onCreateContextMenuMethodInvokeGetChatRoomFlag")

                    XposedHelpers.callMethod(contextMenu, "clear")
                    XposedHelpers.callMethod(contextMenu, "add", position, 1024, 0, "清除所有群聊未读")
                    XposedHelpers.callMethod(contextMenu, "add", position, 1026, 0, "群聊助手置顶")

                }

                if (onCreateContextMenuMethodInvokeGetOfficialFlag) {
                    onCreateContextMenuMethodInvokeGetOfficialFlag = false
                    LogUtils.log("OnCreateContextMenu, method = onCreateContextMenuMethodInvokeGetOfficialFlag")

                    XposedHelpers.callMethod(contextMenu, "clear")
                    XposedHelpers.callMethod(contextMenu, "add", position, 1025, 0, "清除所有服务号号未读")
                    XposedHelpers.callMethod(contextMenu, "add", position, 1027, 0, "服务号助手置顶")

                }
            }

        })


        XposedHelpers.findAndHookMethod(conversationMenuItemSelectedListener, WXObject.Adapter.M.OnMMMenuItemSelected,
                MenuItem::class.java, Int::class.java, object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)


                if (RuntimeInfo.chatRoomViewPresenter.getCurrentData().none { it.field_username == currentLongClickUsername } &&
                        RuntimeInfo.officialViewPresenter.getCurrentData().none { it.field_username == currentLongClickUsername }) return

                /**
                 * 如果当前的点击项是助手里面的某项,刷新助手的数据
                 */
                RuntimeInfo.chatRoomViewPresenter.refreshList(false, Any())
                RuntimeInfo.officialViewPresenter.refreshList(false, Any())

            }


        })
    }
}