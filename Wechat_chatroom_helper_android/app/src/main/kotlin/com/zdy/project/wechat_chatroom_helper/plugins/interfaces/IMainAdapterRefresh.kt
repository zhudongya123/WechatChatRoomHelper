package com.zdy.project.wechat_chatroom_helper.plugins.interfaces

import com.zdy.project.wechat_chatroom_helper.ChatInfoModel

interface IMainAdapterRefresh {

    fun onFirstChatroomRefresh(position: Int, chatInfoModel: ChatInfoModel)


    fun onFirstOfficialRefresh(position: Int, chatInfoModel: ChatInfoModel)

}