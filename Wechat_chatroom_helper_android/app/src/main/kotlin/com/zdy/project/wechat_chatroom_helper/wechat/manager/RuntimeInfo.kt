package com.zdy.project.wechat_chatroom_helper.wechat.manager

import utils.AppSaveInfo

/**
 *  存储一些运行时的状态和消息
 *
 * Created by Mr.Zdy on 2018/3/4.
 */
object RuntimeInfo {


    var isOpen = false
    var hasSuitWechatData = false
    var isCircleAvatar = false
    var isAutoClose = false


    var isPlayVersion = false
    var helperVersionCode = 0
    var wechatVersion = 0


    var isOpenLog = true

    init {
        refresh()
    }

    fun refresh() {

        isOpen = AppSaveInfo.openInfo()
        hasSuitWechatData = AppSaveInfo.hasSuitWechatDataInfo()
        isCircleAvatar = AppSaveInfo.isCircleAvatarInfo()
        isAutoClose = AppSaveInfo.autoCloseInfo()

        isPlayVersion = AppSaveInfo.isPlayVersionInfo()
        helperVersionCode = AppSaveInfo.helpVersionCodeInfo().toInt()
        wechatVersion = AppSaveInfo.wechatVersionInfo().toInt()

        isOpenLog = AppSaveInfo.openLogInfo()
    }
}