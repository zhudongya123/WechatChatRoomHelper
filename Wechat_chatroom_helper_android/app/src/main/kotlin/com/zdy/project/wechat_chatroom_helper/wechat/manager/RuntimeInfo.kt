package com.zdy.project.wechat_chatroom_helper.wechat.manager

import com.zdy.project.wechat_chatroom_helper.utils.LogUtils

/**
 *  存储一些运行时的状态和消息
 *
 * Created by Mr.Zdy on 2018/3/4.
 */
object RuntimeInfo {

    var currentPage = 0

    fun changeCurrentPage(page: Int) {
        currentPage = page

        when (currentPage) {
            com.zdy.project.wechat_chatroom_helper.wechat.manager.PageType.CHATTING -> LogUtils.log("currentPage = CHATTING")
            com.zdy.project.wechat_chatroom_helper.wechat.manager.PageType.CHATTING_WITH_CHAT_ROOMS -> LogUtils.log("currentPage = CHATTING_WITH_CHAT_ROOMS")
            com.zdy.project.wechat_chatroom_helper.wechat.manager.PageType.CHATTING_WITH_OFFICIAL -> LogUtils.log("currentPage = CHATTING_WITH_OFFICIAL")
            com.zdy.project.wechat_chatroom_helper.wechat.manager.PageType.CHAT_ROOMS -> LogUtils.log("currentPage = CHAT_ROOMS")
            com.zdy.project.wechat_chatroom_helper.wechat.manager.PageType.OFFICIAL -> LogUtils.log("currentPage = OFFICIAL")
            com.zdy.project.wechat_chatroom_helper.wechat.manager.PageType.MAIN -> LogUtils.log("currentPage = MAIN")

        }
    }

}