package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.ConfigMessage
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.WechatClassParserTask
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.WechatClasses
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.java.KoinJavaComponent.inject

class ClassParserRepository {
    suspend fun getParserMessageAndResult(
        messageListFlow: MutableSharedFlow<ConfigMessage>,
        parseResultFlow: MutableSharedFlow<Result<WechatClasses>>
    ) {

        val wechatClassParserTask: WechatClassParserTask by inject(WechatClassParserTask::class.java)
        val wechatClassesResult = wechatClassParserTask.parseClasses {
            messageListFlow.emit(it)
        }
        parseResultFlow.emit(wechatClassesResult)
    }
}