package com.zdy.project.wechat_chatroom_helper.ui.helper.uisetting

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import utils.AppSaveInfoUtils

/**
 * Created by zhudo on 2017/12/2.
 */
class SettingViewModel(context: Application) : AndroidViewModel(context) {


    var toolbarColor = MutableLiveData<String>()
    var helperColor = MutableLiveData<String>()
    var nicknameColor = MutableLiveData<String>()
    var contentColor = MutableLiveData<String>()
    var timeColor = MutableLiveData<String>()


    fun start() {
        refreshColorInfo()
    }

     fun refreshColorInfo() {
        toolbarColor.value = AppSaveInfoUtils.toolbarColorInfo()
        helperColor.value = AppSaveInfoUtils.helperColorInfo()
        nicknameColor.value = AppSaveInfoUtils.nicknameColorInfo()
        contentColor.value = AppSaveInfoUtils.contentColorInfo()
        timeColor.value = AppSaveInfoUtils.timeColorInfo()
    }
}