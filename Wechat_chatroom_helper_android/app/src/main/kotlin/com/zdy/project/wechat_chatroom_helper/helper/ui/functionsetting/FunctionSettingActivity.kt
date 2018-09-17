package com.zdy.project.wechat_chatroom_helper.helper.ui.functionsetting

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.ui.BaseActivity
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import manager.PermissionHelper

class FunctionSettingActivity : BaseActivity() {


    private lateinit var listContent: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setTitle(R.string.title_function_setting_string)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_function_setting)
        listContent = findViewById(R.id.list_content)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            if (PermissionHelper.check(this) == PermissionHelper.ALLOW) {
                //加載可配置項的佈局
                initSetting()
            }
        }
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


    private fun initSetting() {
        listContent.removeAllViews()

        val titles =
                arrayOf(getString(R.string.title_function_settting_item_1),
                        getString(R.string.title_function_settting_item_2),
                        getString(R.string.title_function_settting_item_3),
                        getString(R.string.title_function_settting_item_4))

        repeat(titles.size) {

            title = titles[it]

            val itemView = LayoutInflater.from(thisActivity).inflate(R.layout.layout_setting_item, listContent, false)
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)
            val switch = itemView.findViewById<SwitchCompat>(android.R.id.button1)

            switch.visibility = View.VISIBLE

            text1.text = title

            itemView.setOnClickListener { switch.performClick() }

            when (title) {

                getString(R.string.title_function_settting_item_1) -> {
                    switch.isChecked = AppSaveInfo.isCircleAvatarInfo()
                    switch.setOnCheckedChangeListener { _, isChecked -> AppSaveInfo.setCircleAvatarInfo(isChecked) }
                }

                getString(R.string.title_function_settting_item_2) -> {
                    switch.isChecked = AppSaveInfo.autoCloseInfo()
                    switch.setOnCheckedChangeListener { _, isChecked -> AppSaveInfo.setAutoCloseInfo(isChecked) }
                }

                getString(R.string.title_function_settting_item_3) -> {
                    switch.isChecked = AppSaveInfo.openLogInfo()
                    switch.setOnCheckedChangeListener { _, isChecked -> AppSaveInfo.setOpenLog(isChecked) }
                }
                getString(R.string.title_function_settting_item_4) -> {
                    switch.isChecked = AppSaveInfo.launcherEntryInfo()
                    switch.setOnCheckedChangeListener { _, isChecked ->
                        AppSaveInfo.setLauncherEntry(isChecked)
                        showHideLauncherIcon(!isChecked)
                    }
                }

            }
            listContent.addView(itemView)
        }
    }

    private fun showHideLauncherIcon(show: Boolean) {
        val p = packageManager
        val componentName = ComponentName(this, "$packageName.LauncherDelegate")
        p.setComponentEnabledSetting(componentName,
                if (show) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
    }


    override fun onPause() {
        super.onPause()
        WechatJsonUtils.putFileString()
    }
}