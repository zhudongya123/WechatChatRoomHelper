package com.zdy.project.wechat_chatroom_helper.ui.helper.uisetting

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.manager.Type
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomContract
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomView

/**
 * Created by zhudo on 2017/12/2.
 */
class PreviewFragment : Fragment() {

    private lateinit var settingViewHolder: SettingViewModel

    private lateinit var thisActivity: UISettingActivity

    private lateinit var mRootView: ViewGroup

    private lateinit var mChatRoomView : ChatRoomContract.View


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        thisActivity = context as UISettingActivity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = LayoutInflater.from(thisActivity).inflate(R.layout.fragment_preview, container, false) as ViewGroup
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mChatRoomView = ChatRoomView(thisActivity, mRootView.findViewById(R.id.fragment_preview_content), Type.CHAT_ROOMS)
    }

}