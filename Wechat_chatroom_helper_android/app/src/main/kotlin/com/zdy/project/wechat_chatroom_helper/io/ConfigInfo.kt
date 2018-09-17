package com.zdy.project.wechat_chatroom_helper.io

/**
 *  存储一些运行时的状态和消息
 *
 *  进入微信运行状态后，所有数据都应该只能读，不会写，在wechat 包内应该全部使用此类获取数据
 *
 * Created by Mr.Zdy on 2018/3/4.
 */
object ConfigInfo {

    var isOpen = AppSaveInfo.openInfo()
    var hasSuitWechatData = AppSaveInfo.hasSuitWechatDataInfo()
    var isCircleAvatar = AppSaveInfo.isCircleAvatarInfo()
    var isAutoClose = AppSaveInfo.autoCloseInfo()

    var isPlayVersion = AppSaveInfo.isPlayVersionInfo()
    var helperVersionCode = AppSaveInfo.helpVersionCodeInfo().toInt()
    var wechatVersion = AppSaveInfo.wechatVersionInfo().toInt()

    var isOpenLog = AppSaveInfo.openLogInfo()

    var toolbarColor = AppSaveInfo.toolbarColorInfo()
    var helperColor = AppSaveInfo.helperColorInfo()
    var nicknameColor = AppSaveInfo.nicknameColorInfo()
    var contentColor = AppSaveInfo.contentColorInfo()
    var timeColor = AppSaveInfo.contentColorInfo()
    var dividerColor = AppSaveInfo.dividerColorInfo()
}