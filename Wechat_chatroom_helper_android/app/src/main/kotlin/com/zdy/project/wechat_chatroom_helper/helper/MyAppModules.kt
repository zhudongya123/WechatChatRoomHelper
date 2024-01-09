package com.zdy.project.wechat_chatroom_helper.helper

import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.NewConfigViewModel
import org.koin.dsl.module

val myAppModules = module {

    factory { NewConfigViewModel() }
}