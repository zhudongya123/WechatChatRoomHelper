package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by Mr.Zdy on 2017/8/25.
 */

public class ChatRoomViewPresenter implements ChatRoomContract.Presenter {


    public Context mContext;


    private Object mAdapter;

    private ChatRoomContract.View mChatRoomView;

    public ChatRoomViewPresenter(Context context, ViewGroup chatRoomView) {
        mContext = context;
        mChatRoomView = new ChatRoomView(context, chatRoomView);
        mChatRoomView.setPresenter(this);
    }


    public void setOnDialogItemClickListener(ChatRoomRecyclerViewAdapter.OnDialogItemClickListener listener) {
        mChatRoomView.setOnDialogItemClickListener(listener);
    }

    public void setAdapter(Object mAdapter) {
        this.mAdapter = mAdapter;
    }


    public void setMuteListInAdapterPositions(ArrayList<Integer> muteListInAdapterPositions) {
        mChatRoomView.showMessageRefresh(muteListInAdapterPositions);
    }

    @Override
    public void setMessageRefresh(String targetUserName) {
        mChatRoomView.showMessageRefresh(targetUserName);
    }

    public boolean isShowing() {
        return mChatRoomView.isShowing();
    }


    public Object getAdapter() {
        return mAdapter;
    }


    public void show() {
        mChatRoomView.show();
    }

    public void dismiss() {
        mChatRoomView.dismiss();
    }


    @Override
    public void start() {
        mChatRoomView.init();
    }

    @Override
    public Object getOriginAdapter() {
        return mAdapter;
    }

}
