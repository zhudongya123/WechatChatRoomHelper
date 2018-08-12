package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.WXClassParser
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import dalvik.system.DexClassLoader
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import kotlin.concurrent.thread

class ConfigActivity : AppCompatActivity() {

    private lateinit var text1: TextView

    private var parseThread: Thread? = null

    private lateinit var textHandler: TextHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        textHandler = TextHandler()
        text1 = findViewById(R.id.text1)

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


                dexClasses.map { it.classType.substring(1, it.classType.length - 1).replace("/", ".") }
                        .filter { it.contains(Constants.WECHAT_PACKAGE_NAME) }
                        .forEachIndexed { index, className ->

                            try {
                                val clazz = classLoader.loadClass(className)
                                classes.add(clazz)

                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }

                            textHandler.sendMessage(Message.obtain(textHandler, 0, "遍历了${index + 1}个类，已经加载了 ${classes.size}个类"))
                        }

                WXObject.ConversationAvatar = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatar(classes))
                WXObject.ConversationAvatarMethod = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatarMethod(classes))
                WXObject.ConversationWithAppBrandListView = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithAppBrandListView(classes))
                WXObject.ConversationWithCacheAdapter = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithCacheAdapter(classes))
                WXObject.ConversationClickListener = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationClickListener(classes))
                WXObject.ConversationTimeStringMethod = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationTimeMethod(classes))
                WXObject.Logcat = parseAnnotatedElementToName(WXClassParser.Platformtool.getLogcat(classes))

                writeNewConfig()

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    @Throws(Exception::class)
    private fun parseAnnotatedElementToName(element: AnnotatedElement?): String {
        return if (element == null) throw ClassNotFoundException()
        else {
            LogUtils.log("parseAnnotatedElementToName, element = $element")

            when (element) {
                is Method -> element.name
                is Class<*> -> element.name
                else -> ""
            }

        }
    }

    private fun writeNewConfig() {
        AppSaveInfo.addConfigItem("conversationAvatar", WXObject.ConversationAvatar)
        AppSaveInfo.addConfigItem("conversationAvatarMethod", WXObject.ConversationAvatarMethod)
        AppSaveInfo.addConfigItem("conversationWithAppBrandListView", WXObject.ConversationWithAppBrandListView)
        AppSaveInfo.addConfigItem("conversationWithCacheAdapter", WXObject.ConversationWithCacheAdapter)
        AppSaveInfo.addConfigItem("conversationClickListener", WXObject.ConversationClickListener)
        AppSaveInfo.addConfigItem("conversationTimeStringMethod", WXObject.ConversationTimeStringMethod)
        AppSaveInfo.addConfigItem("logcat", WXObject.Logcat)

    }

    inner class TextHandler : Handler() {
        override fun handleMessage(msg: Message) {
            text1.setText(msg.obj as String)

        }
    }

}