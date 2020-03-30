package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class MainItemViewHolder(val itemView: ViewGroup) {

    val avatarContainer = itemView.getChildAt(0) as ViewGroup
    val textContainer = itemView.getChildAt(1) as ViewGroup

    val avatar = avatarContainer.getChildAt(0) as ImageView
    val unReadCount = avatarContainer.getChildAt(1) as TextView
    val unMuteReadIndicators = avatarContainer.getChildAt(2) as ImageView

    val nickname = ((textContainer.getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0)
    val time = (textContainer.getChildAt(0) as ViewGroup).getChildAt(1)

    val sendStatus = ((textContainer.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0)
    val content = ((textContainer.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(1)
    val muteImage = ((textContainer.getChildAt(1) as ViewGroup).getChildAt(1) as ViewGroup).getChildAt(1)


    companion object {

        const val Conversation_Light_Text_Color = 0xFFF57C00.toInt()
        const val Conversation_Normal_Text_Color = 0xFF999999.toInt()
    }
}