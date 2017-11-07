package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

public interface ChatRoomContract {


    interface Presenter {

        void start();

        Object getOriginAdapter();

        void setMessageRefresh(String targetUserName);

        ViewGroup getPresenterView();
    }

    interface View {

        void setOnDialogItemClickListener(ChatRoomRecyclerViewAdapter.OnDialogItemClickListener listener);

        boolean isShowing();

        void show();

        void dismiss();

        void show(int offest);

        void dismiss(int offest);

        void showMessageRefresh(String targetUserName);

        void showMessageRefresh(ArrayList<Integer> muteListInAdapterPositions);

        void init();

        void setPresenter(Presenter presenter);
    }
}
