package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

public class ChatRoomViewHelper {

    @IdRes
    public static int id_avatar_container = 7;
    @IdRes
    public static int id_avatar = 1;
    @IdRes
    public static int id_nickname = 2;
    @IdRes
    public static int id_time = 3;
    @IdRes
    public static int id_msg_state = 4;
    @IdRes
    public static int id_content = 5;
    @IdRes
    public static int id_unread = 6;


    public static View getItemView(Context mContext) {

        RelativeLayout itemView = new RelativeLayout(mContext);
        RelativeLayout contentView = new RelativeLayout(mContext);
        RelativeLayout avatarContainer = new RelativeLayout(mContext);
        ImageView avatar = new ImageView(mContext);
        TextView nickName = new TextView(mContext);
        TextView time = new TextView(mContext);

        LinearLayout contentContainer = new LinearLayout(mContext);
        ImageView msgState = new ImageView(mContext);
        TextView content = new TextView(mContext);
        TextView unread = new TextView(mContext);

        View divider = new View(mContext);

        avatar.setId(id_avatar);
        avatarContainer.setId(id_avatar_container);
        nickName.setId(id_nickname);
        time.setId(id_time);
        msgState.setId(id_msg_state);
        content.setId(id_content);
        unread.setId(id_unread);

        nickName.setTextColor(0xFF353535);
        content.setTextColor(0xFFAAAAAA);
        time.setTextColor(0xFFAAAAAA);
        unread.setTextColor(0xFFFFFFFF);
        divider.setBackgroundColor(0xFFDADADA);

        nickName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        unread.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        nickName.setEllipsize(TextUtils.TruncateAt.END);
        //nickName.setMaxEms(12);
        nickName.setSingleLine();

        contentContainer.setOrientation(LinearLayout.HORIZONTAL);

        RelativeLayout.LayoutParams avatarContainerParams =
                new RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 72), ScreenUtils.dip2px(mContext, 64));
        avatarContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        avatarContainerParams.addRule(RelativeLayout.CENTER_VERTICAL);

        {
            RelativeLayout.LayoutParams avatarParams =
                    new RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 48), ScreenUtils.dip2px(mContext, 48));
            avatarParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            avatarContainer.addView(avatar, avatarParams);
        }

        RelativeLayout.LayoutParams nickNameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nickNameParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
        nickNameParams.addRule(RelativeLayout.RIGHT_OF, avatarContainer.getId());
        nickNameParams.addRule(RelativeLayout.LEFT_OF, time.getId());

        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(ScreenUtils.dip2px(mContext, 12), ScreenUtils.dip2px(mContext, 12),
                ScreenUtils.dip2px(mContext, 12), ScreenUtils.dip2px(mContext, 12));
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams contentContainerParams = new RelativeLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        contentContainerParams.addRule(RelativeLayout.RIGHT_OF, avatarContainer.getId());
        contentContainer.setPadding(0, 0, 0, ScreenUtils.dip2px(mContext, 12));

        {
            LinearLayout.LayoutParams msgStateParams = new LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, 20)
                    , ScreenUtils.dip2px(mContext, 20));

            msgState.setVisibility(View.GONE);

            content.setEllipsize(TextUtils.TruncateAt.END);
            content.setMaxEms(15);
            content.setSingleLine();

            contentContainer.addView(msgState, msgStateParams);
            contentContainer.addView(content);
        }

        RelativeLayout.LayoutParams unReadParams =
                new RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 8), ScreenUtils.dip2px(mContext, 8));
        unReadParams.addRule(RelativeLayout.ALIGN_RIGHT, avatarContainer.getId());
        unReadParams.addRule(RelativeLayout.ALIGN_TOP, avatarContainer.getId());
        unReadParams.setMargins(0, ScreenUtils.dip2px(mContext, 8), ScreenUtils.dip2px(mContext, 10), ScreenUtils.dip2px(mContext, 7));

        RelativeLayout.LayoutParams dividerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, 1);
        dividerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        contentView.addView(avatarContainer, avatarContainerParams);
        contentView.addView(nickName, nickNameParams);
        contentView.addView(time, timeParams);
        contentView.addView(contentContainer, contentContainerParams);
        contentView.addView(unread, unReadParams);
        contentView.addView(divider, dividerParams);

        itemView.addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, 64)));
        return itemView;
    }


    public static Drawable getItemViewBackground() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(0xFFCBCBCB));
        stateListDrawable.addState(new int[]{}, new ColorDrawable(0xFFFFFFFF));
        return stateListDrawable;
    }

}
