package com.zdy.project.wechat_chatroom_helper.ui.helper.uisetting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.utils.ActivityUtils
import ui.MyApplication

/**
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


    private fun setupToolbar() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "助手UI设置"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}