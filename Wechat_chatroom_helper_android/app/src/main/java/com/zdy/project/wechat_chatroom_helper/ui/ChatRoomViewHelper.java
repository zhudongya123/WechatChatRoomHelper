package com.zdy.project.wechat_chatroom_helper.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.zdy.project.wechat_chatroom_helper.HookLogic;
import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;
import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Arrow;
import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Setting;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_Status_Bean;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_True_Content;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_True_Time;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Bean_NickName;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_True_Content_Params;

/**
 * Created by Mr.Zdy on 2017/8/25.
 */

public class ChatRoomViewHelper {

    private ViewGroup chatRoomView;

    private Context mContext;

    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    private OnDialogItemClickListener onDialogItemClickListener;

    private Object mAdapter;

    private ViewGroup contentView;


    public ChatRoomViewHelper(ViewGroup chatRoomView) {
        this.chatRoomView = chatRoomView;
        mContext = chatRoomView.getContext();

        requestLayout();
    }

    public void requestLayout() {
        chatRoomView.removeAllViews();
        contentView = (ViewGroup) getContentView();
        chatRoomView.addView(contentView);
        bindData();
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

    public boolean isShowing() {
        return chatRoomView.getVisibility() == View.VISIBLE;
    }

    public interface OnDialogItemClickListener {
        void onItemClick(int relativePosition);
    }

    public Object getAdapter() {
        return mAdapter;
    }


    public void show() {
        chatRoomView.setVisibility(View.VISIBLE);
    }

    public void dismiss() {
        chatRoomView.setVisibility(View.GONE);
    }

    private View getContentView() {
        LinearLayout rootView = new LinearLayout(mContext);
        rootView.setOrientation(LinearLayout.VERTICAL);

        LinearLayout listView = new LinearLayout(mContext);
        listView.setOrientation(LinearLayout.VERTICAL);
        listView.setId(android.R.id.list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            RelativeLayout head = new RelativeLayout(mContext);

            Toolbar actionBar = new Toolbar(mContext);

            int height = ScreenUtils.dip2px(mContext, 48);

            actionBar.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            actionBar.setNavigationIcon(mContext.getResources()
                    .getIdentifier(Drawable_String_Arrow, "drawable", mContext.getPackageName()));
            actionBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View chatroomView = (View) contentView.getParent();
                    if (chatroomView.getVisibility() == View.VISIBLE)
                        chatroomView.setVisibility(View.GONE);

                }
            });
            actionBar.setBackgroundColor(Color.parseColor("#" + PreferencesUtils.getToolBarColor()));
            actionBar.setTitle("群消息助手");
            actionBar.setTitleTextColor(0xFFFAFAFA);


            Class<?> clazz;
            try {
                clazz = Class.forName("android.widget.Toolbar");
                Field field = clazz.getDeclaredField("mTitleTextView");
                field.setAccessible(true);
                TextView textView = (TextView) field.get(actionBar);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }


            ImageView imageView = new ImageView(mContext);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(height, height);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            imageView.setLayoutParams(params);
            imageView.setPadding(height / 5, height / 5, height / 5, height / 5);
            imageView.setImageResource(mContext.getResources().
                    getIdentifier(Drawable_String_Setting, "drawable", mContext.getPackageName()));

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    ComponentName cn = new ComponentName("com.zdy.project.wechat_chatroom_helper",
                            "com.zdy.project.wechat_chatroom_helper.ui.MainActivity");
                    intent.setComponent(cn);
                    mContext.startActivity(intent);
                }
            });

            imageView.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

            head.addView(actionBar);
            head.addView(imageView);

            rootView.addView(head);
        }


        for (int i = 0; i < muteListInAdapterPositions.size(); i++) {
            final Integer integer = muteListInAdapterPositions.get(i);

            //      Object value = HookLogic.getMessageBeanForOriginIndex(mAdapter, integer);

            View itemView = getItemView();
            listView.addView(itemView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ScreenUtils.dip2px(mContext, 64)));

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

        ScrollView scrollView = new ScrollView(mContext);
        scrollView.addView(listView);

        rootView.addView(scrollView);
        rootView.setBackground(new ColorDrawable(0xFFFFFFFF));
        return rootView;

    }

    private void bindData() {
        for (int i = 0; i < muteListInAdapterPositions.size(); i++) {
            final Integer integer = muteListInAdapterPositions.get(i);

            Object value = HookLogic.getMessageBeanForOriginIndex(mAdapter, integer);

            View itemView = ((ViewGroup) contentView.findViewById(android.R.id.list)).getChildAt(i);

            MessageEntity entity = new MessageEntity(value);

            try {
                Object j = XposedHelpers.callMethod(mAdapter, Method_Message_Status_Bean, value);

                boolean param1 = XposedHelpers.getBooleanField(j, Value_Message_True_Content_Params);
                CharSequence content = (CharSequence) XposedHelpers.callMethod(mAdapter, Method_Message_True_Content,
                        value, ScreenUtils.dip2px(mContext, 13), param1);

                CharSequence time = (CharSequence) XposedHelpers.callMethod(mAdapter, Method_Message_True_Time, value);

                ((TextView) itemView.findViewById(id_nickname)).setText((CharSequence) XposedHelpers.getObjectField(j,
                        Value_Message_Bean_NickName));
                ((TextView) itemView.findViewById(id_content)).setText(content == null ? entity.field_digest : content);
                ((TextView) itemView.findViewById(id_time)).setText(time);

                XposedBridge.log("content =" + content + ", field_digest = " + entity.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            HookLogic.setAvatar(((ImageView) itemView.findViewById(id_avatar)), entity.field_username);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDialogItemClickListener.onItemClick(integer);
                }
            });


            if (entity.field_unReadCount > 0)
                itemView.findViewById(id_unread).setBackground(new ShapeDrawable(new Shape() {
                    @Override
                    public void draw(Canvas canvas, Paint paint) {
                        int size = canvas.getWidth();

                        paint.setAntiAlias(true);
                        paint.setColor(0xFFFF0000);
                        paint.setStyle(Paint.Style.FILL_AND_STROKE);
                        canvas.drawCircle(size / 2, size / 2, size / 2, paint);
                    }
                }));
            else
                itemView.findViewById(id_unread).setBackground(new BitmapDrawable(mContext.getResources()));

            itemView.setBackground(getItemViewBackground());
        }
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


    private View getItemView() {

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
        nickName.setMaxEms(12);
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

        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(ScreenUtils.dip2px(mContext, 12), ScreenUtils.dip2px(mContext, 12),
                ScreenUtils.dip2px(mContext, 12), ScreenUtils.dip2px(mContext, 12));
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams contentContainerParams = new RelativeLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        contentContainerParams.addRule(RelativeLayout.RIGHT_OF, avatarContainer.getId());
        contentContainerParams.setMargins(0, 0, 0, ScreenUtils.dip2px(mContext, 12));

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

        return itemView;
    }

}
