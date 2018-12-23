package com.zdy.project.wechat_chatroom_helper

import android.graphics.Color
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils

/**
 * Created by zhudo on 2017/7/2.
 */

object Constants {

    val WECHAT_PACKAGE_NAME = "com.tencent.mm"


    lateinit var defaultValue: DefaultValue

    class DefaultValue(val isWechatUpdate7: Boolean) {

        val DEFAULT_TOOLBAR_COLOR = if (!isWechatUpdate7) "303030" else "F2F2F2"
        val DEFAULT_HELPER_COLOR = "FFFFFF"
        val DEFAULT_NICKNAME_COLOR = "353535"
        val DEFAULT_CONTENT_COLOR = "999999"
        val DEFAULT_TIME_COLOR = "BBBBBB"
        val DEFAULT_DIVIDER_COLOR = "DADADA"
        val DEFAULT_HIGHLIGHT_COLOR = "F0F0F0"

        val CONVERSATION_ITEM_HEIGHT = if (!isWechatUpdate7) 64f else 72f
        val DEFAULT_TOOLBAR_TINT_COLOR = Color.parseColor("#" + if (!isWechatUpdate7) "FFFFFF" else "181818")

        val CONVERSATION_ITEM_NICKNAME_PADDING_TOP = if (!isWechatUpdate7) 10f else 14f
        val CONVERSATION_ITEM_CONTENT_PADDING_BOTTOM = if (!isWechatUpdate7) 12f else 16f


    }
}
