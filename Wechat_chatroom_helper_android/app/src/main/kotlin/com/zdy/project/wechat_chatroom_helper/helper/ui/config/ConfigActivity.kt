package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.*
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.ConfigActivity.SyncHandler.Companion.HANDLER_TEXT_ADDITION
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.WXClassParser
import dalvik.system.DexClassLoader
import manager.PermissionHelper
import me.omico.base.activity.SetupWizardBaseActivity
import net.dongliu.apk.parser.ApkFile
import com.zdy.project.wechat_chatroom_helper.helper.utils.WechatJsonUtils
import ui.MainActivity
import java.io.File
import java.lang.ref.WeakReference
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*

class ConfigActivity : SetupWizardBaseActivity(), View.OnClickListener {

    private val PAGE_WELCOME = 0
    private val PAGE_WRITE_AND_READ_FILE = 1
    private val PAGE_WRITE_CONFIG = 2


    private var setupStep: Int = 0

    private lateinit var syncHandler: SyncHandler

    override fun onClick(v: View) {
        when (v.id) {
            R.id.config_step2_button1 -> {
                val check = PermissionHelper.check(this)
                when (check) {
                    PermissionHelper.ALLOW -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_success, R.color.material_deep_teal_500)
                        setNavigationBarNextButtonEnabled(true)
                    }
                    PermissionHelper.ASK -> PermissionHelper.requestPermission(this)
                    PermissionHelper.DENY -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_fail, R.color.error_color_material)
                        setNavigationBarNextButtonEnabled(false)
                    }
                }
            }
            else -> {
            }
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            if (setupStep == PAGE_WRITE_AND_READ_FILE) {
                val check = PermissionHelper.check(this)
                when (check) {
                    PermissionHelper.ALLOW -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_success, R.color.material_deep_teal_500)
                        setNavigationBarNextButtonEnabled(true)
                    }
                    PermissionHelper.DENY -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_fail, R.color.error_color_material)
                        setNavigationBarNextButtonEnabled(false)
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStep = intent.getIntExtra("EXTRA_SETUP_STEP", 0)
    }

    override fun initLayout(viewGroup: ViewGroup) {
        when (setupStep) {

            PAGE_WELCOME -> {
                navigationBar.backButton.visibility = View.GONE
                initLayout(viewGroup, R.layout.config_layout_step_1, R.string.config_step1_title, true)
            }
            PAGE_WRITE_AND_READ_FILE -> {
                initLayout(viewGroup, R.layout.config_layout_step_2, R.string.config_step2_title, false)
                findViewById<View>(R.id.config_step2_button1).setOnClickListener(this)
            }
            PAGE_WRITE_CONFIG -> {
                initLayout(viewGroup, R.layout.config_layout_step_3, R.string.config_step3_title, false)
                parseApkClasses()
            }
        }
    }


    override fun onNavigateBack() {
        onBackPressed()
    }

    override fun onNavigateNext() {

        when (setupStep) {
            PAGE_WELCOME, PAGE_WRITE_AND_READ_FILE -> {
                intentNextStep()
            }
            PAGE_WRITE_CONFIG -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    private fun intentNextStep() {
        val intent = Intent(this, ConfigActivity::class.java)
        intent.putExtra("EXTRA_SETUP_STEP", setupStep + 1)
        startActivity(intent)
    }


    private fun initLayout(viewGroup: ViewGroup, @LayoutRes layout: Int, @StringRes title: Int, nextButtonEnable: Boolean) {
        val inflate = LayoutInflater.from(this).inflate(layout, viewGroup, false)
        viewGroup.addView(inflate)
        setupWizardLayout.setHeaderText(title)
        setupWizardLayout.headerTextView.setTextColor(ContextCompat.getColor(this, R.color.white_100))
        setNavigationBarNextButtonEnabled(nextButtonEnable)
    }


    private fun initColorTextView(@IdRes id: Int, @StringRes text: Int, @ColorRes color: Int) {
        val textView = findViewById<TextView>(id)
        textView.setText(text)
        textView.setTextColor(ContextCompat.getColor(this, color))
    }


    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_guide)
    }

    private fun parseApkClasses() {

        val publicSourceDir = this.packageManager.getApplicationInfo(Constants.WECHAT_PACKAGE_NAME, 0).publicSourceDir
        val optimizedDirectory = getDir("dex", 0).absolutePath

        syncHandler = SyncHandler(this)
        val task = ClassParseSyncTask(syncHandler, this)
        task.execute(publicSourceDir, optimizedDirectory)

    }

    class ClassParseSyncTask(syncHandler: SyncHandler, activity: Activity) : AsyncTask<String, Unit, Unit>() {

        private val weakH = WeakReference<SyncHandler>(syncHandler)
        private val weakA = WeakReference<Activity>(activity)

        private val random = Random()

        private val RANDOM_CHANGE_CLASS_NUMBER = 1000
        private var CURRENT_RANDOM_CURSOR = 1

        private var configData = hashMapOf<String, String>()


        override fun doInBackground(vararg params: String) {
            val srcPath = params[0]
            val optimizedDirectory = params[1]

            val classes = mutableListOf<Class<*>>()

            val apkFile = ApkFile(File(srcPath))
            val dexClasses = apkFile.dexClasses
            val classLoader = DexClassLoader(srcPath, optimizedDirectory, null, weakA.get()?.classLoader)


            sendMessageToHandler(HANDLER_TEXT_ADDITION, weakA.get()!!.getString(R.string.config_step3_text1),
                    srcPath, apkFile.apkMeta.versionName, apkFile.apkMeta.versionCode.toString())
            sendMessageToHandler(HANDLER_TEXT_ADDITION, weakA.get()!!.getString(R.string.config_step3_text2))


            dexClasses.map { it.classType.substring(1, it.classType.length - 1).replace("/", ".") }
                    .filter { it.contains(Constants.WECHAT_PACKAGE_NAME) }
                    .forEachIndexed { index, className ->

                        try {
                            val clazz = classLoader.loadClass(className)
                            classes.add(clazz)

                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }

                        if (index == CURRENT_RANDOM_CURSOR) {

                            CURRENT_RANDOM_CURSOR += random.nextInt(RANDOM_CHANGE_CLASS_NUMBER)


                            sendMessageToHandler(SyncHandler.HANDLER_TEXT_CHANGE_LINE,
                                    weakA.get()!!.getString(R.string.config_step3_text6), index + 1, classes.size)
                        }
                    }

            configData["conversationWithCacheAdapter"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithCacheAdapter(classes))
            configData["conversationWithAppBrandListView"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithAppBrandListView(classes))
            configData["conversationAvatar"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatar(classes))
            configData["conversationClickListener"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationClickListener(classes))
            configData["logcat"] = parseAnnotatedElementToName(WXClassParser.PlatformTool.getLogcat(classes))

            writeNewConfig()

            sendMessageToHandler(HANDLER_TEXT_ADDITION, weakA.get()!!.getString(R.string.config_step3_text3),
                    WechatJsonUtils.configPath, apkFile.apkMeta.versionName, apkFile.apkMeta.versionCode.toString())
        }


        override fun onPostExecute(result: Unit?) {
            sendMessageToHandler(SyncHandler.HANDLER_SHOW_NEXT_BUTTON, String())
        }

        @Throws(Exception::class)
        private fun parseAnnotatedElementToName(element: AnnotatedElement?): String {
            return if (element == null) throw ClassNotFoundException()
            else {
                sendMessageToHandler(HANDLER_TEXT_ADDITION, weakA.get()!!.getString(R.string.config_step3_text4), element)
                when (element) {
                    is Method -> element.name
                    is Class<*> -> element.name
                    else -> ""
                }
            }
        }

        private fun writeNewConfig() {
            WechatJsonUtils.getFileString()
            configData.forEach { key, value ->
                AppSaveInfo.addConfigItem(key, value)
                sendMessageToHandler(HANDLER_TEXT_ADDITION, weakA.get()!!.getString(R.string.config_step3_text5), key, value)
            }
        }

        private fun sendMessageToHandler(type: Int, text: String, vararg args: Any) {
            when (type) {
                SyncHandler.HANDLER_TEXT_ADDITION,
                SyncHandler.HANDLER_TEXT_CHANGE_LINE -> {
                    weakH.get()?.sendMessage(Message.obtain(weakH.get(), type,
                            String.format(Locale.CHINESE, text, *args)))
                }
                SyncHandler.HANDLER_SHOW_NEXT_BUTTON -> {
                    weakH.get()?.sendMessage(Message.obtain(weakH.get(), type))
                }
            }
        }

    }


    class SyncHandler(val activity: ConfigActivity) : Handler() {

        companion object {

            const val HANDLER_TEXT_CHANGE_LINE = 1
            const val HANDLER_TEXT_ADDITION = 2
            const val HANDLER_SHOW_NEXT_BUTTON = 3

        }

        private val configTextView = activity.findViewById<TextView>(R.id.config_step3_text1)

        override fun handleMessage(msg: Message) {

            val time = SimpleDateFormat("HH:mm:ss", Locale.CHINESE).format(Calendar.getInstance().time)

            when (msg.what) {
                HANDLER_TEXT_ADDITION,
                HANDLER_TEXT_CHANGE_LINE -> {

                    val spannableStringBuilder =
                            if (configTextView.text == "") {
                                val spannableStringBuilder = SpannableStringBuilder()
                                configTextView.text = spannableStringBuilder
                                spannableStringBuilder
                            } else SpannableStringBuilder(configTextView.text as SpannedString)

                    val format = activity.getString(R.string.config_step3_text_ex)
                    val part1 = time
                    val part2 = msg.obj as String


                    val singleString = String.format(Locale.CHINESE, format, part1, part2)

                    if (msg.what == HANDLER_TEXT_CHANGE_LINE) {
                        spannableStringBuilder.delete(0, spannableStringBuilder.toString().indexOfFirst { it == '\n' } + 1)
                    }

                    spannableStringBuilder.insert(0, singleString)

                    val startLength = 0
                    val endLength = singleString.length

                    spannableStringBuilder.setSpan(ForegroundColorSpan(Color.GRAY), startLength, endLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    configTextView.text = spannableStringBuilder
                }


                HANDLER_SHOW_NEXT_BUTTON -> {
                    activity.setNavigationBarNextButtonEnabled(true)
                }
            }


        }
    }
}