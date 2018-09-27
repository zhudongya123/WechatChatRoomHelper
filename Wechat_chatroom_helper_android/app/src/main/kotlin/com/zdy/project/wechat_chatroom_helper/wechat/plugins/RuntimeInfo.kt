package com.zdy.project.wechat_chatroom_helper.wechat.plugins

import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter

object RuntimeInfo {

    var currentPage = PageType.MAIN

    lateinit var classloader: ClassLoader

    lateinit var chatRoomViewPresenter: ChatRoomViewPresenter

    lateinit var officialViewPresenter: ChatRoomViewPresenter
}