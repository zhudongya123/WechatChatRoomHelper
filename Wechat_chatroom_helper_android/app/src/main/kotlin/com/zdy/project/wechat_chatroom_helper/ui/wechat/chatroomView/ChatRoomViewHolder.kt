package com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var avatar: ImageView = itemView.findViewById(ChatRoomViewHelper.id_avatar)
    var nickname: TextView = itemView.findViewById(ChatRoomViewHelper.id_nickname)
    var time: TextView = itemView.findViewById(ChatRoomViewHelper.id_time)
    var msgState: ImageView = itemView.findViewById(ChatRoomViewHelper.id_msg_state)
    var content: TextView = itemView.findViewById(ChatRoomViewHelper.id_content)
    var unread: TextView = itemView.findViewById(ChatRoomViewHelper.id_unread)
    var divider: View = itemView.findViewById(ChatRoomViewHelper.id_divider)

}
