package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.wechat.WXClassParser
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import dalvik.system.DexClassLoader
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.*
import kotlin.concurrent.thread

class ConfigActivity : AppCompatActivity() {


    private var parseThread: Thread? = null

    private lateinit var textHandler: TextHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)


        textHandler = TextHandler()

        findViewById<View>(R.id.button1).setOnClickListener {
            parseThread?.start()
        }
    }


    override fun onResume() {
        super.onResume()

        parseApkClasses()
    }

    private fun parseApkClasses() {

        if (parseThread != null) {
            if (parseThread!!.isAlive) {
                parseThread!!.interrupt()
            }
            parseThread = null
        }

        parseThread = thread {

            val classes = mutableListOf<Class<*>>()

            try {
                val applicationInfo = this.packageManager.getApplicationInfo(Constants.WECHAT_PACKAGE_NAME, 0)
                val publicSourceDir = applicationInfo.publicSourceDir

                val apkFile = ApkFile(File(publicSourceDir))
                val dexClasses = apkFile.getDexClasses()

                val optimizedDirectory = getDir("dex", 0).absolutePath
                val classLoader = DexClassLoader(publicSourceDir, optimizedDirectory, null, classLoader)


                dexClasses.forEach {
                    val classType = it.classType
                    val className = classType.substring(1, classType.length - 1).replace("/", ".")

                    try {
                        val clazz = classLoader.loadClass(className)
                        classes.add(clazz)

                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }

                WXObject.ConversationAvatar = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatar(classes))
                WXObject.ConversationAvatarMethod = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatarMethod(classes))
                WXObject.ConversationClickListener = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationClickListener(classes))
                WXObject.ConversationWithAppBrandListView = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithAppBrandListView(classes))
                WXObject.ConversationWithCacheAdapter = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithCacheAdapter(classes))
                WXObject.ConversationContentMethod = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationContentMethod(classes))

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }


    private fun parseAnnotatedElementToName( element: AnnotatedElement?): String {
        return if (element == null) ""
        else {
            LogUtils.log("parseAnnotatedElementToName, element = $element")

            when (element) {
                is Method -> element.name
                is Class<*> -> element.name
                else -> ""
            }

        }

    }

    inner class TextHandler : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

}