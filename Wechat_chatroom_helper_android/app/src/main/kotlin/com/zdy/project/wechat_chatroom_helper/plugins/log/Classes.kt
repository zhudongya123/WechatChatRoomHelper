package com.zdy.project.wechat_chatroom_helper.plugins.log

import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil

object Classes {

    private val classesInCurrentPackage by WechatGlobal.wxLazy("${WechatGlobal.wxPackageName}.sdk.platformtools") {
        ReflectionUtil.findClassesFromPackage(WechatGlobal.wxLoader!!, WechatGlobal.wxClasses!!, "${WechatGlobal.wxPackageName}.sdk.platformtools")
    }

    val Log: Class<*> by WechatGlobal.wxLazy("LogClass") {
        classesInCurrentPackage
                .filterByMethod(null, "printErrStackTrace")
                .firstOrNull()
    }
}