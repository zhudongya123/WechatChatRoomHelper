package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by Mr.Zdy on 2017/8/30.
 */

public class ChatRoomContentView extends LinearLayout {
    public ChatRoomContentView(Context context) {
        super(context);
    }

    public ChatRoomContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRoomContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}
