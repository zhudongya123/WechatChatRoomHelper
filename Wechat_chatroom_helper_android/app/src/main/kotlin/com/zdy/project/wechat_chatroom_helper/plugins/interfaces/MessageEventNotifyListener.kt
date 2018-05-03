package com.zdy.project.wechat_chatroom_helper.plugins.interfaces

import android.provider.ContactsContract
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel

interface MessageEventNotifyListener {

    fun onEntryRefresh(chatRoomUsername: String, officialUsername: String)

    fun onNewMessageCreate(talker: String, createTime: Long, content: Any)
}