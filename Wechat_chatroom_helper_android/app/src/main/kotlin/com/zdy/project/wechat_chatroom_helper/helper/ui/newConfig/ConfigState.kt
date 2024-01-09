package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

sealed class ConfigState {

    object Finish : ConfigState()

    object Welcome : ConfigState()

    object CheckPermission : ConfigState()

    object WriteFile : ConfigState()
}