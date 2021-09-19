package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.other

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object OtherHook {

    fun executeHook() {


        if (AppSaveInfo.isBizUseOldStyle()) {

            val newBizConversationUIClass = XposedHelpers.findClass("com.tencent.mm.ui.conversation.NewBizConversationUI", RuntimeInfo.classloader)


            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.tencent.mm.plugin.brandservice.ui.timeline.BizTimeLineUI", RuntimeInfo.classloader),
                    "onCreate", Bundle::class.java, object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity

                    activity.startActivity(Intent(activity, newBizConversationUIClass))
                    activity.finish()

                }
            })

        }

    }
}