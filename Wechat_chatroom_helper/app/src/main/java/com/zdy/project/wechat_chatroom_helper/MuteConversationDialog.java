package com.zdy.project.wechat_chatroom_helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;

import java.util.ArrayList;

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

            RelativeLayout relativeLayout = new RelativeLayout(mContext);

            TextView textView = new TextView(mContext);

            Object value = HookLogic.getMessageBeanForOriginIndex(mAdapter, integer);

            MessageEntity entity = new MessageEntity(value);

            textView.setText(entity.field_username + entity.field_digest);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);

            relativeLayout.addView(textView, params);

            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDialogItemClickListener.onItemClick(integer);
                }
            });

            relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 180));
            rootView.addView(relativeLayout);
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
