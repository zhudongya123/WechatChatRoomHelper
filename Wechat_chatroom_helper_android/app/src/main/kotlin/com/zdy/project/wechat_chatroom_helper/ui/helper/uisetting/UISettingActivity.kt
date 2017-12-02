package com.zdy.project.wechat_chatroom_helper.ui.helper.uisetting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.utils.ActivityUtils
import ui.MyApplication

/**
 * Created by zhudo on 2017/12/2.
 */
class UISettingActivity : AppCompatActivity() {


    private lateinit var previewViewModel: PreviewViewModel
    private lateinit var settingViewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ui_setting)

        setupViewModel()

        setupViewFragment()

    }

    private fun setupViewModel() {
        previewViewModel = PreviewViewModel(MyApplication.get())
        settingViewModel = SettingViewModel(MyApplication.get())
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
}