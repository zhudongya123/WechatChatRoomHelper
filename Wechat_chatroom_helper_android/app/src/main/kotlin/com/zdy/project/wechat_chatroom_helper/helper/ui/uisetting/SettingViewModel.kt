package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo

/**
 * Created by zhudo on 2017/12/2.
 */
class SettingViewModel(context: Application) : AndroidViewModel(context) {


    var toolbarColor = MutableLiveData<String>()
    var helperColor = MutableLiveData<String>()
    var nicknameColor = MutableLiveData<String>()
    var contentColor = MutableLiveData<String>()
    var timeColor = MutableLiveData<String>()
    var dividerColor = MutableLiveData<String>()
    var highlightColor = MutableLiveData<String>()

    fun start() {
        refreshColorInfo()
    }

    fun refreshColorInfo() {
        toolbarColor.value = AppSaveInfo.toolbarColorInfo()
        helperColor.value = AppSaveInfo.helperColorInfo()
        nicknameColor.value = AppSaveInfo.nicknameColorInfo()
        contentColor.value = AppSaveInfo.contentColorInfo()
        timeColor.value = AppSaveInfo.timeColorInfo()
        dividerColor.value = AppSaveInfo.dividerColorInfo()
        highlightColor.value = AppSaveInfo.highLightColorInfo()
    }
}