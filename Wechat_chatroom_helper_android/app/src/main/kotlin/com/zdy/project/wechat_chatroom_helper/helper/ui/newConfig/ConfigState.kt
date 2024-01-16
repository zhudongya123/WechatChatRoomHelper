package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

sealed class ConfigState {
    object Welcome : ConfigState()

    class CheckPermission(val result: PermissionResult) : ConfigState() {

    }

    object WriteFile : ConfigState()
}

sealed class PermissionResult {
    object Wait : PermissionResult()
    object Ask : PermissionResult()
    object Deny : PermissionResult()
    object Pass : PermissionResult()
}
