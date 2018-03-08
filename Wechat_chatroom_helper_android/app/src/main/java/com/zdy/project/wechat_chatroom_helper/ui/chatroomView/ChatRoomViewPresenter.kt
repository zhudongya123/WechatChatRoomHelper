package com.zdy.project.wechat_chatroom_helper.ui.chatroomView

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView.ChatRoomRecyclerViewAdapter
import java.util.*

/**
 * Created by Mr.Zdy on 2017/8/25.
 */

class ChatRoomViewPresenter(private val mContext: Context, pageType: Int) : ChatRoomContract.Presenter {

    override lateinit var originAdapter: Any
        private set

    val isShowing: Boolean
        get() = mView.isShowing


    private val mView: ChatRoomContract.View

    override val presenterView: ViewGroup

    init {
        presenterView = FrameLayout(mContext)
        presenterView.layoutParams = ViewGroup.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        mView = ChatRoomView(mContext, presenterView, pageType)

        mView.setPresenter(this)
    }


    fun setOnDialogItemClickListener(listener: ChatRoomRecyclerViewAdapter.OnDialogItemClickListener) {
        mView.setOnDialogItemClickListener(listener)
    }

    fun setAdapter(mAdapter: Any) {
        this.originAdapter = mAdapter
    }


    fun setListInAdapterPositions(listInAdapterPositions: ArrayList<Int>) {
        mView.showMessageRefresh(listInAdapterPositions)
    }

    override fun setMessageRefresh(targetUserName: String) {
        mView.showMessageRefresh(targetUserName)
    }

    fun show() {
        mView.show()
    }

    fun dismiss() {
        mView.dismiss()
    }

    override fun start() {
        mView.init()
    }


}
