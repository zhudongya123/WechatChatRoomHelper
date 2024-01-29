package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel

data class ConfigMessage(
    val type: Type,
    val content: String? = null
)

enum class Type {
                Top,
    Loading, Normal;
}
