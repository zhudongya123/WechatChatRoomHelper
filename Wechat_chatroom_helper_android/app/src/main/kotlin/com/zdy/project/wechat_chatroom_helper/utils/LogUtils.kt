package com.zdy.project.wechat_chatroom_helper.utils

import android.util.Log
import utils.AppSaveInfoUtils

/**
 * Created by zhudo on 2018/2/11.
 */
object LogUtils {


    fun log(s: String) {
        if (!AppSaveInfoUtils.openlog()) return
        Log.v("WechatChatRoomHelper", s)
    }
}