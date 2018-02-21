package com.zdy.project.wechat_chatroom_helper.utils

import de.robv.android.xposed.XposedBridge
import utils.AppSaveInfoUtils

/**
 * Created by zhudo on 2018/2/11.
 */
object LogUtils {


    fun log(s: String) {
        if (!AppSaveInfoUtils.openLogInfo()) return
        XposedBridge.log("WechatChatRoomHelper : "+s)
    }


    fun isOpen(): Boolean {
        return AppSaveInfoUtils.openLogInfo()
    }
}