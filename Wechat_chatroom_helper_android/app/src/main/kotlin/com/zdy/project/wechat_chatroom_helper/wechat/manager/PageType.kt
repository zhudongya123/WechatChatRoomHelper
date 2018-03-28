package com.zdy.project.wechat_chatroom_helper.wechat.manager

/**
 * Created by zhudo on 2017/11/8.
 */
object PageType {


    private val MODE_SHIFT = 30
    private val MODE_MASK = 0x3 shl MODE_SHIFT

    val CHAT_ROOMS = 2 shl MODE_SHIFT
    val OFFICIAL = 3 shl MODE_SHIFT
    val MAIN = 0 shl MODE_SHIFT
    val CHATTING = 1 shl MODE_SHIFT

    val CHATTING_WITH_CHAT_ROOMS = (1 shl MODE_SHIFT) + 2
    val CHATTING_WITH_OFFICIAL = (1 shl MODE_SHIFT) + 3

    fun printPageType(pageType: Int): String {
        when (pageType) {
            CHAT_ROOMS -> return "CHAT_ROOMS"
            OFFICIAL -> return "OFFICIAL"
            MAIN -> return "MAIN"
            CHATTING -> return "CHATTING"
            CHATTING_WITH_CHAT_ROOMS -> return "CHATTING_WITH_CHAT_ROOMS"
            CHATTING_WITH_OFFICIAL -> return "CHATTING_WITH_OFFICIAL"
        }
        return "NaN"
    }
}