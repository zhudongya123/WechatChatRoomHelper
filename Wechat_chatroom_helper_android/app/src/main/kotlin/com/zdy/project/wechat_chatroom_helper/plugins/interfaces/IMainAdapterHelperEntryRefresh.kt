package com.zdy.project.wechat_chatroom_helper.plugins.interfaces

import com.zdy.project.wechat_chatroom_helper.ChatInfoModel

interface IMainAdapterHelperEntryRefresh {

    fun onFirstChatroomRefresh(chatRoomPosition: Int, chatRoomChatInfoModel: ChatInfoModel, officialPosition: Int, officialChatInfoModel: ChatInfoModel)

}