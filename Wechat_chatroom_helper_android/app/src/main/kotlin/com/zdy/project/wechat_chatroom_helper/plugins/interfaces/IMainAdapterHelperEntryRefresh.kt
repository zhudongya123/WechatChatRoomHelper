package com.zdy.project.wechat_chatroom_helper.plugins.interfaces

import android.provider.ContactsContract
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel

interface IMainAdapterHelperEntryRefresh {

    fun onFirstChatroomRefresh(chatRoomNickname: String, chatRoomUsername: String, officialNickname: String, officialUsername: String)

}