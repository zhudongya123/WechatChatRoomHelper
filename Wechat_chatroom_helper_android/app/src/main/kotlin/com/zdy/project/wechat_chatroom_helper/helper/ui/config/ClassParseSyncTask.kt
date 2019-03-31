package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.app.Activity
import android.os.AsyncTask
import android.os.Message
import com.tencent.bugly.crashreport.CrashReport
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler.Companion.HANDLER_SHOW_NEXT_BUTTON
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler.Companion.HANDLER_TEXT_ADDITION
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler.Companion.HANDLER_TEXT_CHANGE_LINE
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler.Companion.TEXT_COLOR_ERROR
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler.Companion.TEXT_COLOR_NORMAL
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler.Companion.TEXT_COLOR_PASS
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler.Companion.getType
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.SyncHandler.Companion.makeTypeSpec
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXClassParser
import dalvik.system.DexClassLoader
import net.dongliu.apk.parser.ApkFile
import ui.MyApplication
import java.io.File
import java.lang.ref.WeakReference
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.*

class ClassParseSyncTask(syncHandler: SyncHandler, activity: Activity) : AsyncTask<String, Unit, Boolean>() {

    private val weakH = WeakReference<SyncHandler>(syncHandler)
    private val weakA = WeakReference<Activity>(activity)

    private val random = Random()

    private val RANDOM_CHANGE_CLASS_NUMBER = 1000
    private var CURRENT_RANDOM_CURSOR = 1

    private var configData = hashMapOf<String, String>()


    override fun doInBackground(vararg params: String): Boolean {
        val srcPath = params[0]
        val optimizedDirectory = params[1]

        try {
            val classes = mutableListOf<Class<*>>()


            val apkFile = ApkFile(File(srcPath))
            val dexClasses = apkFile.dexClasses
            val classLoader = DexClassLoader(srcPath, optimizedDirectory, null, weakA.get()?.classLoader)


            sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_ADDITION, TEXT_COLOR_NORMAL),
                    weakA.get()!!.getString(R.string.config_step3_text1),
                    srcPath, apkFile.apkMeta.versionName, apkFile.apkMeta.versionCode.toString())


            dexClasses.map { it.classType.substring(1, it.classType.length - 1).replace("/", ".") }
                    .filter { it.contains(Constants.WECHAT_PACKAGE_NAME) }
                    .forEachIndexed { index, className ->

                        try {
                            val clazz = classLoader.loadClass(className)
                            classes.add(clazz)
                        } catch (e: Throwable) {
                        }

                        if (index == CURRENT_RANDOM_CURSOR) {
                            CURRENT_RANDOM_CURSOR += random.nextInt(RANDOM_CHANGE_CLASS_NUMBER)
                            sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_CHANGE_LINE, TEXT_COLOR_NORMAL),
                                    weakA.get()!!.getString(R.string.config_step3_text6), index + 1, classes.size)
                        }
                    }

            configData["conversationWithCacheAdapter"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithCacheAdapter(classes))
            configData["conversationAvatar"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatar(classes))
            configData["conversationLongClickListener"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationLongClickListener(classes))
            configData["conversationClickListener"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationClickListener(classes))
            configData["conversationMenuItemSelectedListener"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationMenuItemSelectedListener(classes))
            configData["conversationStickyHeaderHandler"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationStickyHeaderHandler(classes))
//            configData["conversationItemHighLightSelectorBackGroundInt"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationItemHighLightSelectorBackGroundInt(classes))
//            configData["conversationItemSelectorBackGroundInt"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationItemSelectorBackGroundInt(classes))
            configData["logcat"] = parseAnnotatedElementToName(WXClassParser.PlatformTool.getLogcat(classes))

            writeNewConfig()

            sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_ADDITION, TEXT_COLOR_PASS),
                    weakA.get()!!.getString(R.string.config_step3_text3),
                    WechatJsonUtils.configPath, apkFile.apkMeta.versionName,
                    apkFile.apkMeta.versionCode.toString())

            return true
        } catch (e: Throwable) {
            CrashReport.postCatchedException(e)
            sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_ADDITION, TEXT_COLOR_ERROR),
                    e.toString() + "\n" + e.stackTrace.joinToString("\n") { it.toString() })
            e.printStackTrace()
            return false
        }

    }

    override fun onPreExecute() {
        sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_ADDITION, TEXT_COLOR_NORMAL),
                weakA.get()!!.getString(R.string.config_step3_text0))
        sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_ADDITION, TEXT_COLOR_NORMAL),
                weakA.get()!!.getString(R.string.config_step3_text2))
    }

    override fun onPostExecute(result: Boolean) {
        sendMessageToHandler(makeTypeSpec(HANDLER_SHOW_NEXT_BUTTON, TEXT_COLOR_NORMAL), String())
        if (result) {
            AppSaveInfo.setSuitWechatDataInfo(true)
            AppSaveInfo.setWechatVersionInfo(DeviceUtils.getWechatVersionCode(MyApplication.get()).toString())
            AppSaveInfo.setWechatVersionName(DeviceUtils.getWechatVersionName(MyApplication.get()).toString())
            AppSaveInfo.setHelpVersionCodeInfo(MyApplication.get().getHelperVersionCode().toString())
            WechatJsonUtils.putFileString()
        }
    }

    @Throws(Exception::class)
    private fun parseAnnotatedElementToName(element: AnnotatedElement?): String {
        return if (element == null) throw ClassNotFoundException()
        else {
            sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_ADDITION, TEXT_COLOR_NORMAL), weakA.get()!!.getString(R.string.config_step3_text4), element)
            when (element) {
                is Method -> element.name
                is Class<*> -> element.name
                else -> ""
            }
        }
    }

    @Throws(Exception::class)
    private fun parseAnnotatedElementToName(element: Int?): String {
        sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_ADDITION, TEXT_COLOR_NORMAL), weakA.get()!!.getString(R.string.config_step3_text4), element.toString())
        return element.toString()
    }

    private fun writeNewConfig() {
        configData.entries.forEach {
            val key = it.key
            val value = it.value

            AppSaveInfo.addConfigItem(key, value)
            sendMessageToHandler(makeTypeSpec(HANDLER_TEXT_ADDITION, TEXT_COLOR_NORMAL), weakA.get()!!.getString(R.string.config_step3_text5), key, value)
        }

    }

    private fun sendMessageToHandler(type: Int, text: String, vararg args: Any) {
        val syncHandler = weakH.get()!!
        when (getType(type)) {
            HANDLER_TEXT_ADDITION,
            HANDLER_TEXT_CHANGE_LINE -> {
                syncHandler.sendMessage(Message.obtain(syncHandler, type,
                        String.format(Locale.CHINESE, text, *args)))
            }
            HANDLER_SHOW_NEXT_BUTTON -> {
                syncHandler.sendMessage(Message.obtain(syncHandler, type))
            }
        }
    }

}
