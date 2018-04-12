package com.zdy.project.wechat_chatroom_helper.plugins.log

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters

object LogRecord {

    fun executeHook() {


        val findMethodsByExactParameters = findMethodsByExactParameters(Classes.Log, null, String::class.java, String::class.java, Array<Any>::class.java)

        findMethodsByExactParameters.forEach {
            findAndHookMethod(Classes.Log, it.name, String::class.java, String::class.java, Array<Any>::class.java,
                    object : XC_MethodHook() {

                        override fun afterHookedMethod(param: MethodHookParam) {
                            super.afterHookedMethod(param)

//                            if (!PluginEntry.runtimeInfo.isOpenLog) return

                            val str1 = param.args[0] as String
                            val str2 = param.args[1] as String

                            val objArr = param.args[2] as Array<Any>

                            val format = String.format(str2, objArr)

                            XposedBridge.log("str1 = $str1, format = $format")
                        }


                    })
        }
    }

}