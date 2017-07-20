package com.zdy.project.wechat_chatroom_helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;

import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;
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

        for (int i = 0; i < muteListInAdapterPositions.size(); i++) {
            final Integer integer = muteListInAdapterPositions.get(i);


            Object value = HookLogic.getMessageBeanForOriginIndex(mAdapter, integer);

            MessageEntity entity = new MessageEntity(value);


//            TextView textView = new TextView(mContext);
//
//            textView.setText(entity.field_username + entity.field_digest);
//
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//
//            RelativeLayout relativeLayout = new RelativeLayout(mContext);
//
//            relativeLayout.addView(textView, params);
//
//            relativeLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onDialogItemClickListener.onItemClick(integer);
//                }
//            });
//
//            relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext,64)));
           // rootView.addView(relativeLayout);

            @LayoutRes
            int resourceId=2130903463;

            View itemView = LayoutInflater.from(mContext).inflate(resourceId, rootView, false);
            rootView.addView(itemView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ScreenUtils.dip2px(mContext,64)));

            View time = itemView.findViewById(2131756656);
            View nickName = itemView.findViewById(2131756655);
            View content = itemView.findViewById(2131756657);


            Object j = XposedHelpers.callMethod(mAdapter, "j", value);

            XposedHelpers.callMethod(nickName,"setTextColor",0xff000000);
            XposedHelpers.callMethod(nickName,"setText",XposedHelpers.getObjectField(j, "nickName"));
            XposedHelpers.callMethod(nickName,"F",ScreenUtils.dip2px(mContext,16));

            XposedHelpers.callMethod(content,"setTextColor",0xff000000);
            XposedHelpers.callMethod(content,"setText", XposedHelpers.getObjectField(j,entity.field_digestUser+":"+ entity.field_digest));
            XposedHelpers.callMethod(content,"F",ScreenUtils.dip2px(mContext,14));

            XposedHelpers.callMethod(time,"setTextColor",0xff000000);
            XposedHelpers.callMethod(time,"setText", XposedHelpers.getObjectField(j, "uXO"));
            XposedHelpers.callMethod(time,"F",ScreenUtils.dip2px(mContext,12));

            itemView.findViewById(2131756663).setVisibility(View.GONE);
            itemView.findViewById(2131756659).setVisibility(View.GONE);


//            XposedHelpers.callMethod(itemView.findViewById(2131756656),"setText",entity.field_username + entity.field_digest);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDialogItemClickListener.onItemClick(integer);
                }
            });
        }

        rootView.setBackground(new ColorDrawable(0xFFFFFFFF));
        return rootView;
    }

    private void configWindow() {
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.getDecorView().setBackground(null);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
    }


    interface OnDialogItemClickListener {
        void onItemClick(int relativePosition);
    }

}
