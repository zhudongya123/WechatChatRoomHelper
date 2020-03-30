package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.utils.ActivityUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import ui.MyApplication

/**
 * UiSetting Activity
 *
 * Created by zhudo on 2017/12/2.
 */
class UISettingActivity : AppCompatActivity() {


    private lateinit var previewViewModel: PreviewViewModel
    private lateinit var settingViewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ui_setting)

        Constants.defaultValue = Constants.DefaultValue(AppSaveInfo.getWechatVersionName().startsWith("7"))

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
            if (!uiSettingActivity::previewViewModel.isInitialized) {
                uiSettingActivity.previewViewModel = PreviewViewModel(MyApplication.get())
            }
            return uiSettingActivity.previewViewModel
        }

        fun obtainSettingViewModel(uiSettingActivity: UISettingActivity): SettingViewModel {
            if (!uiSettingActivity::settingViewModel.isInitialized) {
                uiSettingActivity.settingViewModel = SettingViewModel(MyApplication.get())
            }
            return uiSettingActivity.settingViewModel
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
        settingViewModel.refreshColorInfo()
        (supportFragmentManager.findFragmentByTag(PreviewFragment::class.java.simpleName) as PreviewFragment).notifyUIToChangeColor()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ui_setting_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "助手UI设置"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.ui_setting_reset_light -> {
                AppSaveInfo.setToolbarColorInfo(Constants.defaultValue.DEFAULT_TOOLBAR_COLOR)
                AppSaveInfo.setHelperColorInfo(Constants.defaultValue.DEFAULT_HELPER_COLOR)
                AppSaveInfo.setNicknameColorInfo(Constants.defaultValue.DEFAULT_NICKNAME_COLOR)
                AppSaveInfo.setContentColorInfo(Constants.defaultValue.DEFAULT_CONTENT_COLOR)
                AppSaveInfo.setDividerColorInfo(Constants.defaultValue.DEFAULT_DIVIDER_COLOR)
                AppSaveInfo.setTimeColorInfo(Constants.defaultValue.DEFAULT_TIME_COLOR)
                AppSaveInfo.setHighLightColorInfo(Constants.defaultValue.DEFAULT_HIGHLIGHT_COLOR)

                settingViewModel.refreshColorInfo()
                (supportFragmentManager.findFragmentByTag(PreviewFragment::class.java.simpleName) as PreviewFragment).notifyUIToChangeColor()

                true
            }
            R.id.ui_setting_reset_dark->{
                AppSaveInfo.setToolbarColorInfo(Constants.defaultValue.DEFAULT_DARK_TOOLBAR_COLOR)
                AppSaveInfo.setHelperColorInfo(Constants.defaultValue.DEFAULT_DARK_HELPER_COLOR)
                AppSaveInfo.setNicknameColorInfo(Constants.defaultValue.DEFAULT_DARK_NICKNAME_COLOR)
                AppSaveInfo.setContentColorInfo(Constants.defaultValue.DEFAULT_DARK_CONTENT_COLOR)
                AppSaveInfo.setDividerColorInfo(Constants.defaultValue.DEFAULT_DARK_DIVIDER_COLOR)
                AppSaveInfo.setTimeColorInfo(Constants.defaultValue.DEFAULT_DARK_TIME_COLOR)
                AppSaveInfo.setHighLightColorInfo(Constants.defaultValue.DEFAULT_DARK_HIGHLIGHT_COLOR)

                settingViewModel.refreshColorInfo()
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