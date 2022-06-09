package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
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

        Constants.defaultValue = Constants.DefaultValue(true)

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


    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "助手UI设置"
        }
    }


    private fun showDonationDialog(function: () -> Unit) {
        if (AppSaveInfo.isDonation()) {
            function()
        } else {
            AlertDialog.Builder(this)
                    .setTitle("捐赠")
                    .setMessage("群助手上线已经第三年了，现在依然在保持更新，如果可以的话可以捐助一下嘛，现在一个包子都要好几块了呢。")
                    .setNegativeButton("下次再说吧") { dialog, which ->

                        function()
                        dialog.dismiss()

                    }.setNeutralButton("现在就去") { dialog, which ->

                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://QR.ALIPAY.COM/FKX09384NJXB5JXT9MLD11")))
                        AppSaveInfo.setDonation(true)
                        dialog.dismiss()

                    }.setPositiveButton("已经捐过了") { dialog, which ->

                        AppSaveInfo.setDonation(true)
                        function()
                        dialog.dismiss()
                    }.show()
        }
    }

    private fun setupDarkColorInfo() {
        AppSaveInfo.setToolbarColorInfo(Constants.defaultValue.DEFAULT_DARK_TOOLBAR_COLOR)
        AppSaveInfo.setHelperColorInfo(Constants.defaultValue.DEFAULT_DARK_HELPER_COLOR)
        AppSaveInfo.setNicknameColorInfo(Constants.defaultValue.DEFAULT_DARK_NICKNAME_COLOR)
        AppSaveInfo.setContentColorInfo(Constants.defaultValue.DEFAULT_DARK_CONTENT_COLOR)
        AppSaveInfo.setDividerColorInfo(Constants.defaultValue.DEFAULT_DARK_DIVIDER_COLOR)
        AppSaveInfo.setTimeColorInfo(Constants.defaultValue.DEFAULT_DARK_TIME_COLOR)
        AppSaveInfo.setHighLightColorInfo(Constants.defaultValue.DEFAULT_DARK_HIGHLIGHT_COLOR)

        settingViewModel.refreshColorInfo()
        (supportFragmentManager.findFragmentByTag(PreviewFragment::class.java.simpleName) as PreviewFragment).notifyUIToChangeColor()
    }

    private fun setupLightColorInfo() {
        AppSaveInfo.setToolbarColorInfo(Constants.defaultValue.DEFAULT_LIGHT_TOOLBAR_COLOR)
        AppSaveInfo.setHelperColorInfo(Constants.defaultValue.DEFAULT_LIGHT_HELPER_COLOR)
        AppSaveInfo.setNicknameColorInfo(Constants.defaultValue.DEFAULT_LIGHT_NICKNAME_COLOR)
        AppSaveInfo.setContentColorInfo(Constants.defaultValue.DEFAULT_LIGHT_CONTENT_COLOR)
        AppSaveInfo.setDividerColorInfo(Constants.defaultValue.DEFAULT_LIGHT_DIVIDER_COLOR)
        AppSaveInfo.setTimeColorInfo(Constants.defaultValue.DEFAULT_LIGHT_TIME_COLOR)
        AppSaveInfo.setHighLightColorInfo(Constants.defaultValue.DEFAULT_LIGHT_HIGHLIGHT_COLOR)

        settingViewModel.refreshColorInfo()
        (supportFragmentManager.findFragmentByTag(PreviewFragment::class.java.simpleName) as PreviewFragment).notifyUIToChangeColor()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    return true
                }
                else -> {
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        WechatJsonUtils.putFileString()
    }
}
