package ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.ui.BaseActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.ConfigActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.functionsetting.FunctionSettingActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.more.MoreSettingActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting.UISettingActivity
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils
import com.zdy.project.wechat_chatroom_helper.helper.manager.PermissionHelper


class MainActivity : BaseActivity() {

    companion object {
        @JvmStatic
        val WRITE_EXTERNAL_STORAGE_RESULT_CODE = 124

        @JvmStatic
        val FILE_INIT_SUCCESS = "file_init_success"
    }

    private lateinit var listContent: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setTitle(R.string.app_name)

        //加載佈局
        setContentView(R.layout.activity_main)
        listContent = findViewById(R.id.list_content)


    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            when (PermissionHelper.check(this)) {
                PermissionHelper.ALLOW -> {
                    //加載可配置項的佈局
                    WechatJsonUtils.init(this)
                    initSetting(arrayOf("群消息助手状态",
                            getString(R.string.title_function_setting_string),
                            getString(R.string.title_ui_setting_string),
                            getString(R.string.title_question_string),
                            getString(R.string.title_other_setting_string),
                            getString(R.string.sub_title_about_item_1)))
                }
                PermissionHelper.ASK -> {
                    initSetting(arrayOf("群消息助手状态"))
                }
                PermissionHelper.DENY -> {
                    initSetting(arrayOf("群消息助手状态"))
                }
            }
        }

    }

    private fun initSetting(array: Array<String>) {

        listContent.removeAllViews()

        repeat(array.size) { index ->

            title = array[index]

            val itemView = LayoutInflater.from(thisActivity).inflate(R.layout.layout_setting_item, listContent, false)
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)
            val switch = itemView.findViewById<SwitchCompat>(android.R.id.button1)

            text1.text = title

            itemView.setOnClickListener { switch.performClick() }

            when (title) {
                "群消息助手状态" -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(thisActivity, ConfigActivity::class.java))
                    }

                    when (PermissionHelper.check(thisActivity)) {
                        PermissionHelper.ALLOW -> {
                            if (AppSaveInfo.hasSuitWechatDataInfo()) {
                                val saveWechatVersionInfo = AppSaveInfo.wechatVersionInfo()
                                val saveWechatVersionName = AppSaveInfo.getWechatVersionName()
                                val saveHelpVersionInfo = AppSaveInfo.helpVersionCodeInfo()
                                val currentWechatVersionInfo = DeviceUtils.getWechatVersionCode(MyApplication.get()).toString()
                                val currentWechatVersionName = DeviceUtils.getWechatVersionName(MyApplication.get())
                                val currentHelperVersionInfo = MyApplication.get().getHelperVersionCode().toString()

                                if (saveWechatVersionInfo == currentWechatVersionInfo && currentWechatVersionName == saveWechatVersionName) {
                                    if (currentHelperVersionInfo == saveHelpVersionInfo) {
                                        setSuccessText(text2, "本地适配文件适用于 $saveWechatVersionName ($saveWechatVersionInfo) 版本微信，已适配。")
                                    } else {
                                        setWarmText(text2, "本地适配文件由老版本的助手生成，请点击生成新版本的适配文件。")
                                    }
                                } else {
                                    setFailText(text2, "本地适配文件适用于 $saveWechatVersionName ($saveWechatVersionInfo) 版本微信，当前微信版本：$currentWechatVersionName ($currentWechatVersionInfo)， 点击获取新的适配文件。")
                                }
                            } else {
                                setFailText(text2, "本地未发现适配文件， 点击获取新的适配文件。")
                            }
                        }
                        PermissionHelper.ASK -> {
                            setWarmText(text2, "未获得外部存储存储权限，点击获取并创建新的适配文件。")
                        }
                        PermissionHelper.DENY -> {
                            setFailText(text2, "您已经拒绝了我们的权限授予，点击手动授予权限。")
                        }
                    }
                }

                getString(R.string.title_question_string) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zhudongya123/WechatChatRoomHelper/wiki")))
                    }
                    text2.setText(R.string.sub_title_question_string)
                }

                getString(R.string.title_ui_setting_string) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(thisActivity, UISettingActivity::class.java))
                    }
                    text2.setText(R.string.sub_title_ui_setting_string)
                }

                getString(R.string.title_function_setting_string) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(thisActivity, FunctionSettingActivity::class.java))
                    }
                    text2.setText(R.string.sub_title_function_setting_string)
                }
                getString(R.string.title_other_setting_string) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(thisActivity, MoreSettingActivity::class.java))
                    }
                }
                getString(R.string.sub_title_about_item_1) -> {
                    text2.setText(R.string.sub_title_about_item_1_)
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://159.75.116.26:8080/wechat/wechat_download.jsp")))
                    }
                }

            }

            listContent.addView(itemView)
        }

    }

    private fun setWarmText(view: TextView, msg: String) {
        view.text = msg
        view.setTextColor(ContextCompat.getColor(thisActivity, R.color.warm_color))
    }

    private fun setFailText(view: TextView, msg: String) {
        view.text = msg
        view.setTextColor(ContextCompat.getColor(thisActivity, R.color.error_color))
    }

    private fun setSuccessText(view: TextView, msg: String) {
        view.text = msg
        view.setTextColor(ContextCompat.getColor(thisActivity, R.color.right_color))
    }


}