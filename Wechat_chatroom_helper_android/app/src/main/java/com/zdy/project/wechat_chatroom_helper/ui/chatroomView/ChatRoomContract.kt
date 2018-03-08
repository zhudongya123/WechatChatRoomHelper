package com.zdy.project.wechat_chatroom_helper.ui.chatroomView

import android.view.ViewGroup

import com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView.ChatRoomRecyclerViewAdapter

import java.util.ArrayList

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

interface ChatRoomContract {

    interface Presenter {

        val originAdapter: Any

        val presenterView: ViewGroup

        fun start()

        fun setMessageRefresh(targetUserName: String)
    }

    interface View {

        val isShowing: Boolean

        fun setOnDialogItemClickListener(listener: ChatRoomRecyclerViewAdapter.OnDialogItemClickListener)

        fun show()

        fun dismiss()

        fun show(offest: Int)

        fun dismiss(offest: Int)

        fun showMessageRefresh(targetUserName: String)

        fun showMessageRefresh(muteListInAdapterPositions: ArrayList<Int>)

        fun init()

        fun setPresenter(presenter: Presenter)
    }
}
