package com.zdy.project.wechat_chatroom_helper

import de.robv.android.xposed.XposedBridge
import utils.AppSaveInfo

/**
 * Created by zhudo on 2018/2/11.
 */
object LogUtils {

    fun log(s: String) {
       // if (!PluginEntry.runtimeInfo.isOpenLog) return
        XposedBridge.log("WCRH : $s")
    }


    fun isOpen(): Boolean {
        return AppSaveInfo.openLogInfo()
    }
}