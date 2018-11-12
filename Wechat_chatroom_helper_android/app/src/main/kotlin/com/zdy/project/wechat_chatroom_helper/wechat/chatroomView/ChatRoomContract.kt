package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.view.ViewGroup

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

interface ChatRoomContract {

    interface Presenter {

        val originAdapter: Any

        val presenterView: ViewGroup

        fun start()

        fun refreshList(isForce: Boolean, data: Any?)

        fun show()

        fun dismiss()
    }

    interface View {

        fun init()

        val isShowing: Boolean

        fun setOnItemActionListener(listener: ChatRoomRecyclerViewAdapter.OnItemActionListener)

        fun show()

        fun dismiss()

        fun show(offest: Int)

        fun dismiss(offest: Int)

        fun refreshList(isForce: Boolean, data: Any?)

        fun setPresenter(presenter: Presenter)
    }
}
