package com.zdy.project.wechat_chatroom_helper.wechat.plugins.interfaces

import com.zdy.project.wechat_chatroom_helper.LogUtils

interface MessageEventNotifyListener {

    fun onEntryRefresh(chatRoomUsername: String, officialUsername: String) {
        LogUtils.log("MessageEventNotifyListener, onEntryRefresh, chatRoomUsername = $chatRoomUsername, officialUsername = $officialUsername")
    }

    fun onEntryInit(chatRoomUsername: String, officialUsername: String) {
        LogUtils.log("MessageEventNotifyListener, onEntryInit, chatRoomUsername = $chatRoomUsername, officialUsername = $officialUsername")

    }

    fun onEntryPositionChanged(chatroomPosition: Int, officialPosition: Int) {
        LogUtils.log("MessageEventNotifyListener, onEntryPositionChanged, chatroomPosition = $chatroomPosition, officialPosition = $officialPosition")
    }

    fun onNewMessageCreate(talker: String, createTime: Long, content: Any) {
        LogUtils.log("MessageEventNotifyListener, onEntryRefresh, talker = $talker, createTime = $createTime, content = $content")
    }
}