package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewConfigViewModel : ViewModel() {

    private val _state: MutableStateFlow<ConfigState> = MutableStateFlow(ConfigState.Welcome)
    val state: Flow<ConfigState> = _state.asStateFlow()

    fun handleNextButton() {
        when (_state.value) {
            is ConfigState.Welcome -> {
                viewModelScope.launch {
                    _state.emit(ConfigState.CheckPermission)
                }
            }

            is ConfigState.CheckPermission -> {
                viewModelScope.launch {
                    _state.emit(ConfigState.WriteFile)
                }
            }

            else -> {}
        }
    }

    fun handlePreviousButton() {
        when (_state.value) {

            is ConfigState.Welcome -> {
                viewModelScope.launch {
                    _state.emit(ConfigState.Finish)
                }
            }

            is ConfigState.CheckPermission -> {
                viewModelScope.launch {
                    _state.emit(ConfigState.Welcome)
                }
            }

            is ConfigState.WriteFile -> {
                viewModelScope.launch {
                    _state.emit(ConfigState.CheckPermission)
                }
            }

            else -> {}
        }
    }
}