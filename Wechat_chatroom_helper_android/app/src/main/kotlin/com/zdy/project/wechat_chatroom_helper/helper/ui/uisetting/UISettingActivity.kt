package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.utils.ActivityUtils
import com.zdy.project.wechat_chatroom_helper.helper.utils.WechatJsonUtils
import ui.MyApplication
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo

/**
 * UiSetting Activity
 *
 * Created by zhudo on 2017/12/2.
 */
class UISettingActivity : AppCompatActivity() {


    private var previewViewModel: PreviewViewModel? = null
    private var settingViewModel: SettingViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ui_setting)

        setupViewFragment()

        setupToolbar()

        setupViewModel()
    }

    private fun setupViewModel() {
        previewViewModel = obtainPreviewViewModel(this)
        settingViewModel = obtainSettingViewModel(this)
    }

    companion object {

        fun obtainPreviewViewModel(uiSettingActivity: UISettingActivity): PreviewViewModel {
            if (uiSettingActivity.previewViewModel == null) {
                uiSettingActivity.previewViewModel = PreviewViewModel(MyApplication.get())
            }
            return uiSettingActivity.previewViewModel!!
        }

        fun obtainSettingViewModel(uiSettingActivity: UISettingActivity): SettingViewModel {
            if (uiSettingActivity.settingViewModel == null) {
                uiSettingActivity.settingViewModel = SettingViewModel(MyApplication.get())
            }
            return uiSettingActivity.settingViewModel!!
        }
    }

    private fun setupViewFragment() {
        var previewFragment = supportFragmentManager.findFragmentById(R.id.activity_ui_setting_preview)
        if (previewFragment == null) {
            previewFragment = PreviewFragment()
            ActivityUtils.replaceFragmentInActivity(supportFragmentManager, previewFragment, R.id.activity_ui_setting_preview)
        }

        var settingFragment = supportFragmentManager.findFragmentById(R.id.activity_ui_setting_setting)
        if (settingFragment == null) {
            settingFragment = SettingFragment()
            ActivityUtils.replaceFragmentInActivity(supportFragmentManager, settingFragment, R.id.activity_ui_setting_setting)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        settingViewModel!!.refreshColorInfo()
        (supportFragmentManager.findFragmentByTag(PreviewFragment::class.java.simpleName) as PreviewFragment).notifyUIToChangeColor()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ui_setting_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupToolbar() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "助手UI设置"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.ui_setting_reset -> {
                AppSaveInfo.setToolbarColorInfo(Constants.DEFAULT_TOOLBAR_COLOR)
                AppSaveInfo.setHelperColorInfo(Constants.DEFAULT_HELPER_COLOR)
                AppSaveInfo.setNicknameColorInfo(Constants.DEFAULT_NICKNAME_COLOR)
                AppSaveInfo.setContentColorInfo(Constants.DEFAULT_CONTENT_COLOR)
                AppSaveInfo.setDividerColorInfo(Constants.DEFAULT_DIVIDER_COLOR)

                settingViewModel!!.refreshColorInfo()
                (supportFragmentManager.findFragmentByTag(PreviewFragment::class.java.simpleName) as PreviewFragment).notifyUIToChangeColor()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        WechatJsonUtils.putFileString()
    }
}