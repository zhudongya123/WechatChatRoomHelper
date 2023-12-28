package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class MainItemViewHolder(val itemView: ViewGroup) {

    val containerView: ViewGroup = itemView.getChildAt(0) as ViewGroup
    val avatarContainer = containerView.getChildAt(0) as ViewGroup
    val textContainer = containerView.getChildAt(1) as ViewGroup

    val avatar = avatarContainer.getChildAt(0) as ImageView
    val unReadCount = avatarContainer.getChildAt(1) as TextView
    val unMuteReadIndicators = avatarContainer.getChildAt(2) as ImageView

    val nickname =
        textContainer.getViewChildAt(0).getViewChildAt(0).getViewChildAt(0).getViewChildAt(0)
    val time = textContainer.getViewChildAt(0).getViewChildAt(0).getViewChildAt(1)

    val sendStatus =
        textContainer.getViewChildAt(0).getViewChildAt(1).getViewChildAt(0).getViewChildAt(0)
    val content =
        textContainer.getViewChildAt(0).getViewChildAt(1).getViewChildAt(0).getViewChildAt(1)
    val muteImage =
        textContainer.getViewChildAt(0).getViewChildAt(1).getViewChildAt(1).getViewChildAt(0)

    companion object {

        const val Conversation_Light_Text_Color = 0xFFF57C00.toInt()
        const val Conversation_Red_Text_Color = 0xFFF44336.toInt()
    }

    fun View.getViewChildAt(index: Int): View {
        return (this as ViewGroup).getChildAt(index)
    }
}