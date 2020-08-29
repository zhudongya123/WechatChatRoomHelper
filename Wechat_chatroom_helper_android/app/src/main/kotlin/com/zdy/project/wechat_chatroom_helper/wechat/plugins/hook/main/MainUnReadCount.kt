package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main

import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageHandler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

object MainUnReadCount {


    fun executeHook() {

        val appBrandDesktopContainerLoaderClass = XposedHelpers.findClass("com.tencent.mm.plugin.appbrand.widget.desktop.AppBrandDesktopContainer", RuntimeInfo.classloader)
        val launcherUITabViewClass = XposedHelpers.findClass("com.tencent.mm.ui.LauncherUITabView", RuntimeInfo.classloader)
        val launcherUIBottomTabViewClass = XposedHelpers.findClass("com.tencent.mm.ui.LauncherUIBottomTabView", RuntimeInfo.classloader)

        findAndHookMethod(appBrandDesktopContainerLoaderClass, "setActionBarTitle", String::class.java, object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {

                val titleString = param.args[0] as String

                LogUtils.log("MainUnReadCount, appBrandDesktopContainerLoader, titleString = $titleString")

                param.result = "微信（${MessageHandler.totalUnReadCount}）"
            }
        })

        findAndHookMethod(launcherUITabViewClass, "getMainTabUnread", object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {

                val unreadCount = param.result as Int

                LogUtils.log("MainUnReadCount, launcherUITabViewClass, unreadCount = $unreadCount")

                param.result = MessageHandler.totalUnReadCount
            }
        })
        findAndHookMethod(launcherUIBottomTabViewClass, "getMainTabUnread", object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                val unreadCount = param.result as Int

                LogUtils.log("MainUnReadCount, launcherUIBottomTabViewClass, unreadCount = $unreadCount")

                param.result = MessageHandler.totalUnReadCount
            }
        })
    }
}