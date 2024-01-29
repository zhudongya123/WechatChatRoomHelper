package com.zdy.project.wechat_chatroom_helper.helper

import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.ClassParserRepository
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.NewConfigViewModel
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.WechatClassParserTask
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val myAppModules = module {

    factory { NewConfigViewModel(get()) }

    factory { ClassParserRepository() }

    factory {
        WechatClassParserTask(androidApplication())
    }
}