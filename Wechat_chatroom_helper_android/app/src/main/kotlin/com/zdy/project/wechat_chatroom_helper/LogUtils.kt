package com.zdy.project.wechat_chatroom_helper

import android.util.Log
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo

/**
 * Created by zhudo on 2018/2/11.
 */
object LogUtils {

    fun log(s: String) {
         if (!AppSaveInfo.openLogInfo()) return
        Log.v("WCRH", " : $s")
    }

    fun weixinLog(s: String) {
        if (!AppSaveInfo.openLogInfo()) return
        Log.v("Weixin", " : $s")
    }

    fun isOpen(): Boolean {
        return AppSaveInfo.openLogInfo()
    }
}