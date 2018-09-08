package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.utils.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import manager.PermissionHelper
import me.omico.base.activity.SetupWizardBaseActivity
import ui.MainActivity

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
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_success, R.color.right_color)
                        setNavigationBarNextButtonEnabled(true)
                    }
                    PermissionHelper.ASK -> PermissionHelper.requestPermission(this)
                    PermissionHelper.DENY -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_fail, R.color.error_color)
                        PermissionHelper.gotoPermissionPage(this)
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
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_success, R.color.right_color)
                        setNavigationBarNextButtonEnabled(true)
                    }
                    PermissionHelper.DENY -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_fail, R.color.error_color)
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
            PAGE_WELCOME,
            PAGE_WRITE_AND_READ_FILE -> {
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
        AppSaveInfo.setSuitWechatDataInfo(false)

        val publicSourceDir = this.packageManager.getApplicationInfo(Constants.WECHAT_PACKAGE_NAME, 0).publicSourceDir
        val optimizedDirectory = getDir("dex", 0).absolutePath

        syncHandler = SyncHandler(this)
        task = ClassParseSyncTask(syncHandler, this)
        task?.execute(publicSourceDir, optimizedDirectory)
    }

    var task: ClassParseSyncTask? = null

    override fun onDestroy() {
        super.onDestroy()
        task?.cancel(true)

    }


    fun setNavigationBarNextButtonEnabled2(result: Boolean) {
        this.setNavigationBarNextButtonEnabled(result)
    }
}