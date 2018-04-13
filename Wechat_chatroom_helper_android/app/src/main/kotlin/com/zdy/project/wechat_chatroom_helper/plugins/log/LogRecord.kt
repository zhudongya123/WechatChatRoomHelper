package com.zdy.project.wechat_chatroom_helper.plugins.log

import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.sdk.platformtools.Classes
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters

object LogRecord {

    fun executeHook() {



        val findMethodsByExactParameters = findMethodsByExactParameters(Classes.Logcat, null, String::class.java, String::class.java, Array<Any>::class.java)

        findMethodsByExactParameters.forEach {
            findAndHookMethod(Classes.Logcat, it.name, String::class.java, String::class.java, Array<Any>::class.java,
                    object : XC_MethodHook() {

                        override fun afterHookedMethod(param: MethodHookParam) {
                            super.afterHookedMethod(param)

                        //    if (!PluginEntry.runtimeInfo.isOpenLog) return

                            val str1 = param.args[0] as String
                            val str2 = param.args[1] as String

                            if (param.args[2] == null) {
                                LogUtils.log("level = " + param.method.name + ", name = $str1, value = $str2")
                            } else {
                                val objArr = param.args[2] as Array<Any>

                                val format = String.format(str2, *objArr)

                                LogUtils.log("level = " + param.method.name + ", name = $str1, value = $format")
                            }

                        }

                    })
        }
    }

}