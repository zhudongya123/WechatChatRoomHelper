package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import ui.MyApplication

/**
 * Created by zhudo on 2017/12/2.
 */
class SettingViewModel(context: Application) : AndroidViewModel(context) {


    var colorMode = MutableLiveData<Int>()

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
        colorMode.value = AppSaveInfo.getColorMode()
        toolbarColor.value = AppSaveInfo.toolbarColorInfo(MyApplication.get())
        helperColor.value = AppSaveInfo.helperColorInfo(MyApplication.get())
        nicknameColor.value = AppSaveInfo.nicknameColorInfo(MyApplication.get())
        contentColor.value = AppSaveInfo.contentColorInfo(MyApplication.get())
        timeColor.value = AppSaveInfo.timeColorInfo(MyApplication.get())
        dividerColor.value = AppSaveInfo.dividerColorInfo(MyApplication.get())
        highlightColor.value = AppSaveInfo.highLightColorInfo(MyApplication.get())
    }

    annotation class ColorMode {

        companion object {

            val Auto = 0
            val Manual = 1
        }
    }
}