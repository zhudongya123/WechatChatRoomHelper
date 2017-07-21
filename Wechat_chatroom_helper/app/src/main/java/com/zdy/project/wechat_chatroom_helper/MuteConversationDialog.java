package com.zdy.project.wechat_chatroom_helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;

import java.util.ArrayList;

import de.robv.android.xposed.XposedHelpers;

/**
 * Created by zhudo on 2017/7/20.
 */

public class MuteConversationDialog extends Dialog {

    private Context mContext;

    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    OnDialogItemClickListener onDialogItemClickListener;

    private Object mAdapter;

    public MuteConversationDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }


    public void setAdapter(Object mAdapter) {
        this.mAdapter = mAdapter;
    }

    public void setMuteListInAdapterPositions(ArrayList<Integer> muteListInAdapterPositions) {
        this.muteListInAdapterPositions = muteListInAdapterPositions;
    }


    public void setOnDialogItemClickListener(OnDialogItemClickListener onDialogItemClickListener) {
        this.onDialogItemClickListener = onDialogItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configWindow();

        setContentView(getContentView());
    }

    private View getContentView() {
        LinearLayout rootView = new LinearLayout(mContext);
        rootView.setOrientation(LinearLayout.VERTICAL);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Toolbar actionBar = new Toolbar(mContext);
            actionBar.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, 48)));
            actionBar.setNavigationIcon(mContext.getResources()
                    .getIdentifier("rj", "drawable", mContext.getPackageName()));
            actionBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            actionBar.setBackgroundColor(0xFF313235);

            rootView.addView(actionBar);
        }


        for (int i = 0; i < muteListInAdapterPositions.size(); i++) {
            final Integer integer = muteListInAdapterPositions.get(i);

            Object value = HookLogic.getMessageBeanForOriginIndex(mAdapter, integer);

            MessageEntity entity = new MessageEntity(value);

            View itemView = getItemView(rootView, integer);
            rootView.addView(itemView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ScreenUtils.dip2px(mContext, 64)));

            Object j = XposedHelpers.callMethod(mAdapter, "j", value);

            ((TextView) itemView.findViewById(id_nickname))
                    .setText((CharSequence) XposedHelpers.getObjectField(j, "nickName"));

            CharSequence uXP = (CharSequence) XposedHelpers.getObjectField(j, "uXP");


//            entity.field_digestUser + ":"
//                    + (uXP == null || uXP.length() == 0 ? entity.field_digest : uXP)

            ((TextView) itemView.findViewById(id_content)).setText(entity.field_digest);
            ((TextView) itemView.findViewById(id_time)).setText((CharSequence) XposedHelpers.getObjectField(j, "uXO"));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDialogItemClickListener.onItemClick(integer);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    }, 200);
                }
            });


            itemView.setBackground(getItemViewBackground());
        }

        rootView.setBackground(new ColorDrawable(0xFFFFFFFF));
        return rootView;
    }


    private Drawable getItemViewBackground() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(0xFFCBCBCB));
        stateListDrawable.addState(new int[]{}, new ColorDrawable(0xFFFFFFFF));
        return stateListDrawable;
    }

    @IdRes
    private int id_avatar_container = 7;
    @IdRes
    private int id_avatar = 1;
    @IdRes
    private int id_nickname = 2;
    @IdRes
    private int id_time = 3;
    @IdRes
    private int id_msg_state = 4;
    @IdRes
    private int id_content = 5;
    @IdRes
    private int id_unread = 6;


    private View getItemView(LinearLayout rootView, final Integer integer) {

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
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        unread.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        nickName.setEllipsize(TextUtils.TruncateAt.END);
        nickName.setMaxEms(12);
        nickName.setSingleLine();

        contentContainer.setOrientation(LinearLayout.HORIZONTAL);
        //  contentContainer.setPadding(ScreenUtils.dip2px(mContext, 30), 0, 0, 0);

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
        nickNameParams.setMargins(0, ScreenUtils.dip2px(mContext, 12), 0, 0);
        nickNameParams.addRule(RelativeLayout.RIGHT_OF, avatarContainer.getId());

        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(ScreenUtils.dip2px(mContext, 12), ScreenUtils.dip2px(mContext, 12),
                ScreenUtils.dip2px(mContext, 12), ScreenUtils.dip2px(mContext, 12));
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams contentContainerParams = new RelativeLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        contentContainerParams.addRule(RelativeLayout.RIGHT_OF, avatarContainer.getId());
        contentContainerParams.setMargins(0, 0, 0, ScreenUtils.dip2px(mContext, 8));

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
                new RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 20), ScreenUtils.dip2px(mContext, 20));
        unReadParams.addRule(RelativeLayout.ALIGN_RIGHT, avatarContainer.getId());
        unReadParams.addRule(RelativeLayout.ALIGN_TOP, avatarContainer.getId());
        unReadParams.setMargins(0, ScreenUtils.dip2px(mContext, 3), 0, ScreenUtils.dip2px(mContext, 7));

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


        return itemView;
    }

    private void configWindow() {
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.getDecorView().setBackground(null);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.TOP;
//        layoutParams.height = ScreenUtils.getScreenHeight(mContext) - ScreenUtils.getStatusHeight(mContext) - 300;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
    }


    interface OnDialogItemClickListener {
        void onItemClick(int relativePosition);
    }

}
