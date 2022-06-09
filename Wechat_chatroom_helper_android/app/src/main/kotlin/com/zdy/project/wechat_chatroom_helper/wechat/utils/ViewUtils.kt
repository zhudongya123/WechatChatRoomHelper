package com.zdy.project.wechat_chatroom_helper.wechat.utils

import android.view.View
import android.view.ViewGroup

fun ViewGroup.findViewByClassName(className: String): View? {
    var view: View? = null
    for (index in 0 until childCount) {
        val child = getChildAt(index)
        if (child::class.java.name == className) {
            view = child
            break
        }
        if (child is ViewGroup) {
            val innerView = child.findViewByClassName(className)
            if (innerView != null) return innerView
        }
    }
    return view
}