package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewConfigViewModel : ViewModel() {

    private val _stateFlow: MutableStateFlow<ConfigState> = MutableStateFlow(ConfigState.Welcome)
    val stateFlow: StateFlow<ConfigState> = _stateFlow.asStateFlow()

    fun handleNextButton() {
        val value = _stateFlow.value
        when (value) {
            is ConfigState.Welcome -> {
                viewModelScope.launch {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        _stateFlow.emit(ConfigState.WriteFile)
                    } else {
                        _stateFlow.emit(ConfigState.CheckPermission(PermissionResult.Wait))
                    }
                }
            }

            is ConfigState.CheckPermission -> {
                if (value.result is PermissionResult.Pass) {
                    viewModelScope.launch {
                        _stateFlow.emit(ConfigState.WriteFile)
                    }
                }
            }

            else -> {}
        }
    }

    fun handlePreviousButton() {
        when (_stateFlow.value) {
            is ConfigState.CheckPermission -> {
                viewModelScope.launch {
                    _stateFlow.emit(ConfigState.Welcome)
                }
            }

            is ConfigState.WriteFile -> {
                viewModelScope.launch {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        _stateFlow.emit(ConfigState.Welcome)
                    } else {
                        _stateFlow.emit(ConfigState.CheckPermission(PermissionResult.Wait))
                    }
                }
            }

            else -> {}
        }
    }

    fun handlePermissionResult(checkResult: PermissionResult) {
        viewModelScope.launch {
            val configState = _stateFlow.value
            if (configState is ConfigState.CheckPermission) {
                _stateFlow.emit(ConfigState.CheckPermission(checkResult))
            }
        }
    }

    fun isNextButtonEnable(): Boolean {
        return when (val state = _stateFlow.value) {
            is ConfigState.CheckPermission -> {
                state.result is PermissionResult.Pass
            }

            ConfigState.Welcome -> {
                true
            }

            ConfigState.WriteFile -> {
                true
            }

            else -> false
        }
    }

    fun isPreviousButtonEnable(): Boolean {
        return when (val state = _stateFlow.value) {
            is ConfigState.CheckPermission -> {
                true
            }

            ConfigState.Welcome -> {
                true
            }

            ConfigState.WriteFile -> {
                true
            }

            else -> false
        }
    }
}