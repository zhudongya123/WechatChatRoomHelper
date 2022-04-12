package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.setupwizardlib.SetupWizardLayout
import com.android.setupwizardlib.view.NavigationBar
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import manager.PermissionHelper

class ConfigActivity : AppCompatActivity() {

    enum class PageName {
        WELCOME,//欢迎页面
        WRITE_AND_READ_FILE,//读写权限页面
        WRITE_CONFIG//写配置页面
    }


    private var currentPage: PageName = PageName.WELCOME

    private lateinit var setupWizardLayout: SetupWizardLayout
    private lateinit var mContainerLayout: ViewGroup
    private lateinit var syncHandler: SyncHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        setupWizardLayout = findViewById(R.id.setup_wizard_layout)
        mContainerLayout = findViewById(R.id.fragment_container)

        setupPage()

        setupWizardLayout.navigationBar.setNavigationBarListener(object : NavigationBar.NavigationBarListener {
            override fun onNavigateBack() {
                //后退
                gotoPriorPage()
            }

            override fun onNavigateNext() {
                //前进
                gotoNextPage()
            }
        })
    }

    /**
     * 进入下一个页面
     */
    fun gotoNextPage() {
        when (currentPage) {
            PageName.WELCOME -> {
                currentPage = PageName.WRITE_AND_READ_FILE
            }
            PageName.WRITE_AND_READ_FILE -> {
                currentPage = PageName.WRITE_CONFIG
            }
            PageName.WRITE_CONFIG -> {
                finish()
                return
            }
        }
        setupPage()
    }


    /**
     * 进入上一个页面
     */
    fun gotoPriorPage() {
        when (currentPage) {
            PageName.WELCOME -> {
                finish()
                return
            }
            PageName.WRITE_AND_READ_FILE -> {
                currentPage = PageName.WELCOME
            }
            PageName.WRITE_CONFIG -> {
                currentPage = PageName.WRITE_AND_READ_FILE
            }
        }
        setupPage()
    }


    private fun setupPage() {
        when (currentPage) {
            PageName.WELCOME -> {
                setupWizardLayout.navigationBar.backButton.visibility = View.GONE
                initLayout(mContainerLayout, R.layout.config_layout_step_1, R.string.config_step1_title, true)
            }
            PageName.WRITE_AND_READ_FILE -> {
                setupWizardLayout.navigationBar.backButton.visibility = View.VISIBLE
                initLayout(mContainerLayout, R.layout.config_layout_step_2, R.string.config_step2_title, false)
                checkPermission()
            }
            PageName.WRITE_CONFIG -> {
                setupWizardLayout.navigationBar.backButton.visibility = View.VISIBLE
                initLayout(mContainerLayout, R.layout.config_layout_step_3, R.string.config_step3_title, false)
                parseApkClasses()
            }
        }
    }




    private fun initLayout(viewGroup: ViewGroup, @LayoutRes layout: Int, @StringRes title: Int, nextButtonEnable: Boolean) {
        viewGroup.removeAllViews()
        val inflate = LayoutInflater.from(this).inflate(layout, viewGroup, false)
        viewGroup.addView(inflate)
        setupWizardLayout.setHeaderText(title)
        setupWizardLayout.headerTextView.setTextColor(ContextCompat.getColor(this, R.color.white_100))
        setNavigationBarNextButtonEnabled(nextButtonEnable)
    }


    /**
     * 设置继续按钮是否可以点击
     */
    fun setNavigationBarNextButtonEnabled(nextButtonEnable: Boolean) {
        setupWizardLayout.navigationBar.nextButton.isEnabled = nextButtonEnable
    }

    private fun initColorTextView(@IdRes id: Int, @StringRes text: Int, @ColorRes color: Int) {
        val textView = findViewById<TextView>(id)
        textView.setText(text)
        textView.setTextColor(ContextCompat.getColor(this, color))
    }

    /**
     * 检查权限设置
     */
    private fun checkPermission() {
        when (PermissionHelper.check(this)) {
            PermissionHelper.ALLOW -> {
                initColorTextView(R.id.config_step2_text1, R.string.config_permission_success, R.color.right_color)
                WechatJsonUtils.init(null)
                setNavigationBarNextButtonEnabled(true)
            }
            PermissionHelper.DENY -> {
                initColorTextView(R.id.config_step2_text1, R.string.config_permission_fail, R.color.error_color)
                setNavigationBarNextButtonEnabled(false)
            }
        }
        findViewById<View>(R.id.config_step2_button1).setOnClickListener {
            when (PermissionHelper.check(this)) {
                PermissionHelper.ALLOW -> {
                    initColorTextView(R.id.config_step2_text1, R.string.config_permission_success, R.color.right_color)
                    setNavigationBarNextButtonEnabled(true)
                    WechatJsonUtils.init(this)
                }
                PermissionHelper.ASK -> PermissionHelper.requestPermission(this)
                PermissionHelper.DENY -> {
                    initColorTextView(R.id.config_step2_text1, R.string.config_permission_fail, R.color.error_color)
                    PermissionHelper.gotoPermissionPage(this)
                    setNavigationBarNextButtonEnabled(false)
                }
            }
        }

    }

    /**
     * 查找微信配置类
     */
    private fun parseApkClasses() {
        AppSaveInfo.setSuitWechatDataInfo(false)
        WechatJsonUtils.putFileString()

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

}