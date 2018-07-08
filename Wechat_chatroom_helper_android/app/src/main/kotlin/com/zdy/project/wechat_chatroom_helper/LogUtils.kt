package com.zdy.project.wechat_chatroom_helper

import android.util.Log
import utils.AppSaveInfo

/**
 * Created by zhudo on 2018/2/11.
 */
object LogUtils {

    fun log(s: String) {
        // if (!PluginEntry.runtimeInfo.isOpenLog) return
        Log.v("WCRH", " $s")
    }


    fun isOpen(): Boolean {
        return AppSaveInfo.openLogInfo()
    }
}