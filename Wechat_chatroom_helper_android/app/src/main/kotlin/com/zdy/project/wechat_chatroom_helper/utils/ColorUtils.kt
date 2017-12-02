package com.zdy.project.wechat_chatroom_helper.utils

import android.graphics.Color

/**
 * Created by zhudo on 2017/12/2.
 */
object ColorUtils {

    fun getColorInt(colorString: CharSequence): Int {
        return Color.parseColor("#" + colorString)
    }
}