package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import static com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomViewHelper.*;

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

public class ChatRoomViewHolder extends RecyclerView.ViewHolder {

    public ImageView avatar;
    public TextView nickname;
    public TextView time;
    public ImageView msgState;
    public TextView content;
    public TextView unread;


    public ChatRoomViewHolder(View itemView) {
        super(itemView);

        avatar = (ImageView) itemView.findViewById(id_avatar);
        nickname = (TextView) itemView.findViewById(id_nickname);
        time = (TextView) itemView.findViewById(id_time);
        msgState = (ImageView) itemView.findViewById(id_msg_state);
        content = (TextView) itemView.findViewById(id_content);
        unread = (TextView) itemView.findViewById(id_unread);
    }


}
