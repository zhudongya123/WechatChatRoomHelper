package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel

import android.app.Application
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXClassParser
import dalvik.system.DexClassLoader
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.Locale
import kotlin.random.Random

class WechatClassParserTask(private val application: Application) {
    suspend fun parseClasses(messageCallback: suspend (ConfigMessage) -> Unit): Result<WechatClasses> {
        messageCallback(ConfigMessage(Type.Normal, "开始"))

        val srcPath = application.packageManager.getApplicationInfo(Constants.WECHAT_PACKAGE_NAME, 0).publicSourceDir
        val optimizedDirectory = application.getDir("dex", 0).absolutePath

        val wechatClasses = WechatClasses()

        try {
            val classes = mutableListOf<Class<*>>()

            val apkFile = ApkFile(File(srcPath))
            val dexClasses = apkFile.dexClasses
            val classLoader = DexClassLoader(srcPath, optimizedDirectory, null, application.classLoader)

            val message = String.format(
                Locale.getDefault(),
                application.getString(R.string.config_step3_text1),
                srcPath, apkFile.apkMeta.versionName, apkFile.apkMeta.versionCode.toString()
            )
            messageCallback(ConfigMessage(Type.Normal, message))

            val randomChangeClassesNumber = 1000
            var currentCursor = 1

            dexClasses
                .map {
                    it.classType
                        .substring(1, it.classType.length - 1)
                        .replace("/", ".")
                }.forEachIndexed { index, className ->
                    try {
                        val clazz = classLoader.loadClass(className)
                        classes.add(clazz)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        messageCallback(ConfigMessage(Type.Normal, "解析类时出现错误，类名$className"))
                    }

                    if (index == currentCursor) {
                        currentCursor += Random.nextInt(randomChangeClassesNumber)

                        val message = String.format(
                            Locale.getDefault(), application.getString(R.string.config_step3_text6), index + 1, classes.size
                        )
                        messageCallback(ConfigMessage(Type.Top, message))
                    }
                }

            wechatClasses.conversationWithCacheAdapter = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithCacheAdapter(classes))
            wechatClasses.conversationLongClickListener = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationLongClickListener(classes))
            wechatClasses.conversationClickListener = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationClickListener(classes))
            wechatClasses.conversationMenuItemSelectedListener = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationMenuItemSelectedListener(classes))
            wechatClasses.conversationStickyHeaderHandler = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationStickyHeaderHandler(classes))
            wechatClasses.conversationAvatar = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatar(classes))

//            toMap(wechatClasses).forEach { entry ->
//                val key = entry.key
//                val value = entry.value.toString()
//                messageCallback(ConfigMessage(Type.Normal, "配置-> $key, 位置-> $value"))
//            }

            messageCallback(ConfigMessage(Type.Normal, "成功"))
            return Result.success(wechatClasses)
        } catch (e: Throwable) {
            messageCallback(ConfigMessage(Type.Normal, e.stackTrace.joinToString("\n") { it.toString() }))
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    @Throws(Exception::class)
    private fun parseAnnotatedElementToName(element: AnnotatedElement?): String {
        return if (element == null) throw ClassNotFoundException()
        else {
            when (element) {
                is Method -> element.name
                is Class<*> -> element.name
                else -> ""
            }
        }
    }
}