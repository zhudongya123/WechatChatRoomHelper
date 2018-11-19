package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter

import android.app.Activity
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageFactory
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor

object MainAdapterLongClick {


    var currentLongClickUsername = ""
    var onItemLongClickMethodInvokeGetItemFlagNickName = ""
    var onCreateContextMenuMethodInvokeGetChatRoomFlag = false
    var onCreateContextMenuMethodInvokeGetOfficialFlag = false

    var chatRoomStickyValue = 0
    var officialStickyValue = 0

    const val MENU_ITEM_CLEAR_UNREAD_CHATROOM = 1024
    const val MENU_ITEM_CLEAR_UNREAD_OFFICIAL = 1025

    const val MENU_ITEM_STICK_HEADER_CHATROOM_ENABLE = 1034
    const val MENU_ITEM_STICK_HEADER_OFFICIAL_ENABLE = 1035

    const val MENU_ITEM_STICK_HEADER_CHATROOM_DISABLE = 1044
    const val MENU_ITEM_STICK_HEADER_OFFICIAL_DISABLE = 1045

    val CoordinateField = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader).declaredFields.firstOrNull { it.type == IntArray::class.java }!!

    fun getConversationLongClickClassConstructor(): Constructor<*> {
        val longClickClass = XposedHelpers.findClass(WXObject.Adapter.C.ConversationLongClickListener, RuntimeInfo.classloader)
        return longClickClass.getConstructor(MainAdapter.originAdapter::class.java, ListView::class.java, Activity::class.java, IntArray::class.java)
    }

    fun parseStickyInfo(value: Int, function: (Pair<Int, Int>) -> Unit) {

        val chatRoomSticky = value shr 16

        val officialSticky = value and 65535

        function(Pair(chatRoomSticky, officialSticky))


    }

    fun saveStickyInfo(chatRoomValue: Int, officialValue: Int, function: (Int) -> Unit) {
        function((chatRoomValue shl 16) + officialValue)
        LogUtils.log("saveStickyInfo, ${(chatRoomValue shl 16) + officialValue}")
    }


    fun executeHook() {

        AppSaveInfo.getHelperStickyInfo()


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

                val position = XposedHelpers.getIntField(contextMenuInfo, "position")

                if (onCreateContextMenuMethodInvokeGetChatRoomFlag) {
                    onCreateContextMenuMethodInvokeGetChatRoomFlag = false
                    LogUtils.log("OnCreateContextMenu, method = onCreateContextMenuMethodInvokeGetChatRoomFlag")

                    XposedHelpers.callMethod(contextMenu, "clear")
                    XposedHelpers.callMethod(contextMenu, "add", position, MENU_ITEM_CLEAR_UNREAD_CHATROOM, 0, "所有群聊标为已读")
                    if (chatRoomStickyValue > 0)
                        XposedHelpers.callMethod(contextMenu, "add", position, MENU_ITEM_STICK_HEADER_CHATROOM_DISABLE, 0, "取消群聊助手置顶")
                    else
                        XposedHelpers.callMethod(contextMenu, "add", position, MENU_ITEM_STICK_HEADER_CHATROOM_ENABLE, 0, "群聊助手置顶")

                }

                if (onCreateContextMenuMethodInvokeGetOfficialFlag) {
                    onCreateContextMenuMethodInvokeGetOfficialFlag = false
                    LogUtils.log("OnCreateContextMenu, method = onCreateContextMenuMethodInvokeGetOfficialFlag")

                    XposedHelpers.callMethod(contextMenu, "clear")
                    XposedHelpers.callMethod(contextMenu, "add", position, MENU_ITEM_CLEAR_UNREAD_OFFICIAL, 0, "所有服务号标为已读")
                    if (officialStickyValue > 0)
                        XposedHelpers.callMethod(contextMenu, "add", position, MENU_ITEM_STICK_HEADER_OFFICIAL_DISABLE, 0, "取消服务号助手置顶")
                    else
                        XposedHelpers.callMethod(contextMenu, "add", position, MENU_ITEM_STICK_HEADER_OFFICIAL_ENABLE, 0, "服务号助手置顶")

                }
            }

        })


        XposedHelpers.findAndHookMethod(conversationMenuItemSelectedListener, WXObject.Adapter.M.OnMMMenuItemSelected,
                MenuItem::class.java, Int::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

                val menuItem = param.args[0] as MenuItem

                when (menuItem.itemId) {

                    MENU_ITEM_CLEAR_UNREAD_CHATROOM -> {
                        MessageFactory.clearSpecChatRoomUnRead()
                        RuntimeInfo.chatRoomViewPresenter.refreshList(false, Any())
                        MainAdapter.notifyDataSetChangedForOriginAdapter()
                        param.result = null
                    }
                    MENU_ITEM_CLEAR_UNREAD_OFFICIAL -> {
                        MessageFactory.clearSpecOfficialUnRead()
                        RuntimeInfo.officialViewPresenter.refreshList(false, Any())
                        MainAdapter.notifyDataSetChangedForOriginAdapter()
                        param.result = null
                    }
                    MENU_ITEM_STICK_HEADER_CHATROOM_ENABLE -> {
                        if (officialStickyValue > 0) {
                            saveStickyInfo(officialStickyValue + 1, officialStickyValue) {
                                AppSaveInfo.setHelperStickyInfo(it)
                            }
                        } else {
                            saveStickyInfo(1, 0) {
                                AppSaveInfo.setHelperStickyInfo(it)
                            }
                        }
                        parseStickyInfo(AppSaveInfo.getHelperStickyInfo()) {
                            chatRoomStickyValue = it.first
                            officialStickyValue = it.second
                        }

                        //MainAdapter.notifyDataSetChangedForOriginAdapter()
                        MainLauncherUI.refreshListMainUI()
                        param.result = null
                    }
                    MENU_ITEM_STICK_HEADER_OFFICIAL_ENABLE -> {
                        if (chatRoomStickyValue > 0) {
                            saveStickyInfo(chatRoomStickyValue, chatRoomStickyValue + 1) {
                                AppSaveInfo.setHelperStickyInfo(it)
                            }
                        } else {
                            saveStickyInfo(0, 1) {
                                AppSaveInfo.setHelperStickyInfo(it)
                            }
                        }

                        parseStickyInfo(AppSaveInfo.getHelperStickyInfo()) {
                            chatRoomStickyValue = it.first
                            officialStickyValue = it.second
                        }

                        //MainAdapter.notifyDataSetChangedForOriginAdapter()
                        MainLauncherUI.refreshListMainUI()
                        param.result = null
                    }

                    MENU_ITEM_STICK_HEADER_CHATROOM_DISABLE -> {
                        saveStickyInfo(0, officialStickyValue) {
                            AppSaveInfo.setHelperStickyInfo(it)
                        }
                        parseStickyInfo(AppSaveInfo.getHelperStickyInfo()) {
                            chatRoomStickyValue = it.first
                            officialStickyValue = it.second
                        }

                        //MainAdapter.notifyDataSetChangedForOriginAdapter()
                        MainLauncherUI.refreshListMainUI()
                        param.result = null

                    }

                    MENU_ITEM_STICK_HEADER_OFFICIAL_DISABLE -> {
                        saveStickyInfo(chatRoomStickyValue, 0) {
                            AppSaveInfo.setHelperStickyInfo(it)
                        }
                        parseStickyInfo(AppSaveInfo.getHelperStickyInfo()) {
                            chatRoomStickyValue = it.first
                            officialStickyValue = it.second
                        }

                        //MainAdapter.notifyDataSetChangedForOriginAdapter()
                        MainLauncherUI.refreshListMainUI()
                        param.result = null

                    }
                }
            }

            override fun afterHookedMethod(param: MethodHookParam?) {

                if (RuntimeInfo.chatRoomViewPresenter.getCurrentData().none { it.field_username == currentLongClickUsername } &&
                        RuntimeInfo.officialViewPresenter.getCurrentData().none { it.field_username == currentLongClickUsername }) return

                /**
                 * 如果当前的点击项是助手里面的某项,刷新助手的数据
                 *
                 * ps:点击了删除此会话时，需要更新adapter
                 */
                RuntimeInfo.chatRoomViewPresenter.refreshList(false, Any())
                RuntimeInfo.officialViewPresenter.refreshList(false, Any())

            }


        })
    }
}