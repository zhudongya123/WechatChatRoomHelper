package com.zdy.project.wechat_chatroom_helper.plugins.interfaces

import android.util.Log

interface MessageEventNotifyListener {

    fun onEntryRefresh(chatRoomUsername: String, officialUsername: String) {
        Log.v("LogRecord", "MessageEventNotifyListener, onEntryRefresh, chatRoomUsername = $chatRoomUsername, officialUsername = $officialUsername")
    }

    fun onEntryInit(chatRoomUsername: String, officialUsername: String) {
        Log.v("LogRecord", "MessageEventNotifyListener, onEntryInit, chatRoomUsername = $chatRoomUsername, officialUsername = $officialUsername")

    }

    fun onEntryPositionChanged(chatroomPosition: Int, officialPosition: Int) {
        Log.v("LogRecord", "MessageEventNotifyListener, onEntryPositionChanged, chatroomPosition = $chatroomPosition, officialPosition = $officialPosition")
    }

    fun onNewMessageCreate(talker: String, createTime: Long, content: Any) {
        Log.v("LogRecord", "MessageEventNotifyListener, onEntryRefresh, talker = $talker, createTime = $createTime, content = $content")
    }
}