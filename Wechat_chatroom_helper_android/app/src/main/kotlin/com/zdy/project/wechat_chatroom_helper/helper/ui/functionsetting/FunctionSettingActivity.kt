package com.zdy.project.wechat_chatroom_helper.helper.ui.functionsetting

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.ui.BaseActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting.UISettingActivity
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import ui.MyApplication

class FunctionSettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

//    private fun initSetting() {
//        val titles = arrayOf("功能开关", "我使用的是play版本", "助手圆形头像", "进入聊天界面自动关闭助手", "群助手UI设置", "Xposed日志开关", "隐藏程序入口")
//        val subTitles = arrayOf("功能开关", "我使用的是play版本", "助手圆形头像", "进入聊天界面自动关闭助手", "群助手UI设置", "Xposed日志开关", "隐藏程序入口")
//
//        repeat(titles.size) {
//
//            title = titles[it]
//
//            val itemView = LayoutInflater.from(thisActivity).inflate(R.layout.layout_setting_item, listContent, false)
//            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
//            val text2 = itemView.findViewById<TextView>(android.R.id.text2)
//            val switch = itemView.findViewById<SwitchCompat>(android.R.id.button1)
//
//            text1.text = title
//
//            itemView.setOnClickListener { switch.performClick() }
//
//            when (it) {
//                0 -> {
//                    switch.isChecked = AppSaveInfo.openInfo()
//                    switch.setOnCheckedChangeListener { _, isChecked -> AppSaveInfo.setOpen(isChecked) }
//                }
//                1 -> {
//                    switch.isChecked = AppSaveInfo.isPlayVersionInfo()
//                    switch.setOnCheckedChangeListener { _, isChecked ->
//                        AppSaveInfo.setPlayVersionInfo(isChecked)
//                       // sendRequest(MyApplication.get().getWechatVersionCode().toString(), AppSaveInfo.isPlayVersionInfo())
//                    }
//                }
//                2 -> {
//                    switch.isChecked = AppSaveInfo.isCircleAvatarInfo()
//                    switch.setOnCheckedChangeListener { _, isChecked -> AppSaveInfo.setCircleAvatarInfo(isChecked) }
//                }
//                3 -> {
//                    switch.isChecked = AppSaveInfo.autoCloseInfo()
//                    switch.setOnCheckedChangeListener { _, isChecked -> AppSaveInfo.setAutoCloseInfo(isChecked) }
//                }
//                4 -> {
//                    switch.visibility = View.INVISIBLE
//                    switch.setOnClickListener { startActivity(Intent(thisActivity, UISettingActivity::class.java)) }
//                }
//                5 -> {
//                    switch.isChecked = AppSaveInfo.openLogInfo()
//                    switch.setOnCheckedChangeListener { _, isChecked -> AppSaveInfo.setOpenLog(isChecked) }
//                }
//                6 -> {
//                    switch.isChecked = AppSaveInfo.launcherEntryInfo()
//                    switch.setOnCheckedChangeListener { _, isChecked ->
//                        AppSaveInfo.setLauncherEntry(isChecked)
//                        showHideLauncherIcon(!isChecked)
//                    }
//                }
//
//            }
//
//          //  listContent.addView(itemView)
//        }
//
//        title = "微信群消息助手"
//    }

    private fun showHideLauncherIcon(show: Boolean) {
        val p = packageManager
        val componentName = ComponentName(this, "$packageName.LauncherDelegate")
        p.setComponentEnabledSetting(componentName,
                if (show) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
    }

}