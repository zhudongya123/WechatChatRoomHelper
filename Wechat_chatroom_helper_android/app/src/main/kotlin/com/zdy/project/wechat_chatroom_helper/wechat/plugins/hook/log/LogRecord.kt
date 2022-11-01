package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.log

import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters

object LogRecord {

    fun executeHook() {

        val logcatClass = XposedHelpers.findClass(WXObject.Tool.C.Logcat, RuntimeInfo.classloader)
        val logcatLogMethods = findMethodsByExactParameters(logcatClass, null, String::class.java, String::class.java, Array<Any>::class.java)
        logcatLogMethods.filter { it.name.length == 1 }.forEach { method ->
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                }

                override fun afterHookedMethod(param: MethodHookParam) {

                    if (!AppSaveInfo.openLogInfo()) return

                    try {
                        val str1 = param.args[0] as String
                        val str2 = param.args[1] as String

                        if (param.args[2] == null) {
                            LogUtils.weixinLog("level = " + param.method.name + ", name = $str1, value = $str2")
                        } else {
                            val objArr = param.args[2] as Array<Any>
                            val format = String.format(str2, *objArr)
                            LogUtils.weixinLog("level = " + param.method.name + ", name = $str1, value = $format")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }


    }


}