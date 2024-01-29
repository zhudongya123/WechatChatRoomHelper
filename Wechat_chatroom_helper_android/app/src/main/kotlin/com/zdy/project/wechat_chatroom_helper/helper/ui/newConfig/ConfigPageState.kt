package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

sealed class ConfigPageState {
    data object WelcomePage : ConfigPageState()

    class CheckPermissionPage(val result: PermissionResult) : ConfigPageState()

    class WriteFilePage(
        val latestMessage: String? = null,
        val state: WriteConfigState
    ) : ConfigPageState()
}

sealed class PermissionResult {
    data object Wait : PermissionResult()
    data object Ask : PermissionResult()
    data object Deny : PermissionResult()
    data object Pass : PermissionResult()
}

sealed class WriteConfigState {
    data object Wait : WriteConfigState()
    data object InProcess : WriteConfigState()
    data object Success : WriteConfigState()
    data object Failure : WriteConfigState()
}
