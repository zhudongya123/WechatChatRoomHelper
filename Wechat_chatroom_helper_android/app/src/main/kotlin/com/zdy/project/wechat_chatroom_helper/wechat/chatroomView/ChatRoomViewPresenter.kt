package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo

/**
 * Created by Mr.Zdy on 2017/8/25.
 */

class ChatRoomViewPresenter(mContext: Context, var pageType: Int) : ChatRoomContract.Presenter {


    override val presenterView: ViewGroup
    override lateinit var originAdapter: Any

    val isShowing: Boolean get() = mView.isShowing


    private val mView: ChatRoomContract.View

    init {
        presenterView = FrameLayout(mContext).also {
            it.layoutParams = ViewGroup.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        mView = ChatRoomView(mContext, presenterView, pageType)
        mView.setPresenter(this)
    }

    fun setAdapter(mAdapter: Any) {
        this.originAdapter = mAdapter
    }

    override fun refreshList(isForce: Boolean, data: Any?) {
        mView.refreshList(isForce, data)
    }

    override fun show() {
        LogUtils.log("TrackHelperCan'tOpen, ChatRoomViewPresenter -> show, mView = ${mView}")
        mView.show()
        RuntimeInfo.currentPage = pageType
    }

    override fun dismiss() {
        mView.dismiss()
        RuntimeInfo.currentPage = PageType.MAIN
    }

    override fun start() {
        mView.init()
    }

    override fun getCurrentData(): ArrayList<ChatInfoModel> {
        return mView.getCurrentData()
    }
}
