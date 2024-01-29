package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel

import android.graphics.Matrix
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.ClassParserRepository
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.ConfigPageState
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.PermissionResult
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.WriteConfigState
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.MyApplication

class NewConfigViewModel(
    private val classParserRepository: ClassParserRepository
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<ConfigPageState> = MutableStateFlow(ConfigPageState.WelcomePage)
    val stateFlow: StateFlow<ConfigPageState> = _stateFlow.asStateFlow()

    val messageListFlow: MutableSharedFlow<ConfigMessage> = MutableSharedFlow()
    val parseResultFlow: MutableSharedFlow<Result<WechatClasses>> = MutableSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun handleWriteConfig(
//        messageListFlow: MutableSharedFlow<ConfigMessage>,
//        parseResultFlow: MutableSharedFlow<Result<WechatClasses>>
    ) {
        messageListFlow.resetReplayCache()
        parseResultFlow.resetReplayCache()

        viewModelScope.launch(Dispatchers.IO) {
            classParserRepository.getParserMessageAndResult(messageListFlow, parseResultFlow)
        }

        parseResultFlow.collect {
            if (it.isSuccess) {
                val wechatClasses: WechatClasses = it.getOrThrow()

                toMap(wechatClasses).forEach { entry ->
                    val key = entry.key
                    val value = entry.value.toString()
                    AppSaveInfo.addConfigItem(key, value)
                }
                AppSaveInfo.setSuitWechatDataInfo(true)
                AppSaveInfo.setWechatVersionInfo(DeviceUtils.getWechatVersionCode(MyApplication.get()).toString())
                AppSaveInfo.setWechatVersionName(DeviceUtils.getWechatVersionName(MyApplication.get()))
                AppSaveInfo.setHelpVersionCodeInfo(MyApplication.get().getHelperVersionCode().toString())
                WechatJsonUtils.putFileString()
            }
        }
    }



    fun handleNextButton() {
        when (val value = _stateFlow.value) {
            is ConfigPageState.WelcomePage -> {
                viewModelScope.launch {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        _stateFlow.emit(ConfigPageState.WriteFilePage(state = WriteConfigState.Wait))
                    } else {
                        _stateFlow.emit(ConfigPageState.CheckPermissionPage(PermissionResult.Wait))
                    }
                }
            }

            is ConfigPageState.CheckPermissionPage -> {
                if (value.result is PermissionResult.Pass) {
                    viewModelScope.launch {
                        _stateFlow.emit(ConfigPageState.WriteFilePage(state = WriteConfigState.Wait))
                    }
                }
            }

            else -> {}
        }
    }

    fun handlePreviousButton() {
        when (_stateFlow.value) {
            is ConfigPageState.CheckPermissionPage -> {
                viewModelScope.launch {
                    _stateFlow.emit(ConfigPageState.WelcomePage)
                }
            }

            is ConfigPageState.WriteFilePage -> {
                viewModelScope.launch {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        _stateFlow.emit(ConfigPageState.WelcomePage)
                    } else {
                        _stateFlow.emit(ConfigPageState.CheckPermissionPage(PermissionResult.Wait))
                    }
                }
            }

            else -> {}
        }
    }

    fun handlePermissionResult(checkResult: PermissionResult) {
        viewModelScope.launch {
            val configState = _stateFlow.value
            if (configState is ConfigPageState.CheckPermissionPage) {
                _stateFlow.emit(ConfigPageState.CheckPermissionPage(checkResult))
            }
        }
    }

    fun isNextButtonEnable(): Boolean {
        return when (val state = _stateFlow.value) {
            is ConfigPageState.CheckPermissionPage -> {
                state.result is PermissionResult.Pass
            }

            ConfigPageState.WelcomePage -> {
                true
            }

            is ConfigPageState.WriteFilePage -> {
                true
            }

            else -> false
        }
    }

    fun isPreviousButtonEnable(): Boolean {
        return when (val state = _stateFlow.value) {
            is ConfigPageState.CheckPermissionPage -> {
                true
            }

            ConfigPageState.WelcomePage -> {
                true
            }

            is ConfigPageState.WriteFilePage -> {
                true
            }

            else -> false
        }
    }
}