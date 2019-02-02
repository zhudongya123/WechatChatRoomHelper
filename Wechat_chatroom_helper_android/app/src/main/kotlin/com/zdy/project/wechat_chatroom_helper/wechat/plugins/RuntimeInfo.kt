package com.zdy.project.wechat_chatroom_helper.wechat.plugins

import android.annotation.SuppressLint
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter

object RuntimeInfo {

    var currentPage = PageType.MAIN

    lateinit var classloader: ClassLoader

    @SuppressLint("StaticFieldLeak")
    lateinit var chatRoomViewPresenter: ChatRoomViewPresenter

    @SuppressLint("StaticFieldLeak")
    lateinit var officialViewPresenter: ChatRoomViewPresenter
}