package com.zdy.project.wechat_chatroom_helper.plugins.log

import android.util.Log
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters

object LogRecord {

    fun executeHook() {

        val logcatClass = XposedHelpers.findClass(WXObject.Logcat, PluginEntry.classloader)
        val logcatLogMethods = findMethodsByExactParameters(logcatClass, null, String::class.java, String::class.java, Array<Any>::class.java)


        logcatLogMethods.forEach { method ->
            val parameterTypes = method.parameterTypes
            Log.e("parameterTypes", parameterTypes.joinToString { it.toString() })
            findAndHookMethod(logcatClass, method.name, parameterTypes, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {

                    //    if (!PluginEntry.runtimeInfo.isOpenLog) return
                    try {

                        val str1 = param.args[0] as String
                        val str2 = param.args[1] as String


                        if (str1 != "MicroMsg.ConversationWithCacheAdapter") return

                        if (param.args[2] == null) {

                            LogUtils.log("level = " + param.method.name + ", name = $str1, value = $str2")

                        } else {
                            val objArr = param.args[2] as Array<Any>

                            val format = String.format(str2, *objArr)

                            LogUtils.log("level = " + param.method.name + ", name = $str1, value = $format")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }


    }


}