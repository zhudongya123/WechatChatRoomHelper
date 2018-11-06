package com.zdy.project.wechat_chatroom_helper.plugins.addition.hook

import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.plugins.addition.SpecialPluginEntry
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object LogHook {


    fun hook(classLoader: ClassLoader) {
        val logClass = XposedHelpers.findClass(SpecialPluginEntry.Logclass, classLoader)

        val list = logClass.methods.filter { it.genericParameterTypes.size == 3 }
                .filter { it.parameterTypes[0].name == String::class.java.name }
                .filter { it.parameterTypes[1].name == String::class.java.name }


        list.forEach {
            XposedHelpers.findAndHookMethod(logClass, it.name, it.parameterTypes[0].canonicalName,
                    it.parameterTypes[1].canonicalName, it.parameterTypes[2].canonicalName, object : XC_MethodHook() {

                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        val str1 = param.args[0] as String
                        val str2 = param.args[1] as String

                        if (param.args[2] == null) {
                            LogUtils.log("QWEA, LogHook, level = " + param.method.name + ", name = $str1, value = $str2")

                        } else {
                            val objArr = param.args[2] as Array<Any>

                            val format = String.format(str2, *objArr)

                            LogUtils.log("QWEA, LogHook, level = " + param.method.name + ", name = $str1, value = $format")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })
        }
    }
}