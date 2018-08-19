package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.os.Handler
import android.os.Message
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.WXClassParser
import dalvik.system.DexClassLoader
import me.omico.base.activity.SetupWizardBaseActivity
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import kotlin.concurrent.thread

class ConfigActivity : SetupWizardBaseActivity() {


    private val PAGE_WELCOME = 0
    private val PAGE_WRITE_AND_READ_FILE = 1
    private val PAGE_GETTING_MODE_PERMISSION = 2
    private val PAGE_GETTING_OVERLAY_PERMISSION = 3


    private var setupStep: Int = 0

    override fun initLayout(viewGroup: ViewGroup) {
        when (setupStep) {

            PAGE_WELCOME -> {
                navigationBar.backButton.visibility = View.GONE
                initLayout(viewGroup, R.layout.config_layout_step_1, R.string.config_string_step1_text1, true)
            }
            PAGE_WRITE_AND_READ_FILE -> {
                initLayout(viewGroup, R.layout.config_layout_step_1, R.string.config_string_step1_text1, true)
            }
        }
    }


    override fun onNavigateBack() {
    }

    override fun onNavigateNext() {
    }


    private fun initLayout(viewGroup: ViewGroup, @LayoutRes layout: Int, @StringRes title: Int, nextButtonEnable: Boolean) {
        val inflate = LayoutInflater.from(this).inflate(layout, viewGroup, false)
        viewGroup.addView(inflate)
        setupWizardLayout.setHeaderText(title)
        setupWizardLayout.headerTextView.setTextColor(viewGroup.context.getColor(R.color.colorAccent))
        setNavigationBarNextButtonEnabled(nextButtonEnable)
    }

    private lateinit var text1: TextView

    private var parseThread: Thread? = null

    private lateinit var textHandler: TextHandler

    private var configHashMap = hashMapOf<String, String>()


    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_guide)
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

                configHashMap["conversationWithCacheAdapter"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithCacheAdapter(classes))
                configHashMap["conversationWithAppBrandListView"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithAppBrandListView(classes))
                configHashMap["conversationAvatar"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatar(classes))
                configHashMap["conversationClickListener"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationClickListener(classes))
                configHashMap["logcat"] = parseAnnotatedElementToName(WXClassParser.PlatformTool.getLogcat(classes))

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
        configHashMap.forEach { k, v -> AppSaveInfo.addConfigItem(k, v) }
    }

    inner class TextHandler : Handler() {
        override fun handleMessage(msg: Message) {
            text1.text = msg.obj as String
        }
    }
}