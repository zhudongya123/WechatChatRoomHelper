package ui

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.gson.JsonParser
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.ui.helper.InfoDialogBuilder
import com.zdy.project.wechat_chatroom_helper.ui.helper.ToolBarColorDialogHelper
import manager.PermissionHelper
import network.ApiManager
import okhttp3.*
import utils.AppSaveInfoUtils
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var thisActivity: MainActivity

    private lateinit var clickMe: Button
    private lateinit var detail: TextView
    private lateinit var listContent: LinearLayout

    private lateinit var receiver: PermissionBroadCastReceiver
    private var permissionHelper: PermissionHelper? = null


    inner class PermissionBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            bindView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thisActivity = this@MainActivity

        //权限检查广播
        receiver = PermissionBroadCastReceiver()
        registerReceiver(receiver, IntentFilter(Constants.FILE_INIT_SUCCESS))

        //检查权限
        permissionHelper = PermissionHelper.check(thisActivity)


        //加載佈局
        setContentView(R.layout.activity_main)
        clickMe = findViewById(R.id.button) as Button
        detail = findViewById(R.id.detail) as TextView
        listContent = findViewById(R.id.list_content) as LinearLayout


        //加載可配置項的佈局
        initSetting()
    }

    private fun initSetting() {
        val titles = arrayOf("功能开关", "群消息助手开关", "公众号助手开关", "助手圆形头像", "进入聊天界面自动关闭助手", "群助手Toolbar颜色")

        for (i in 0 until titles.size) {
            title = titles[i]

            val itemView = LayoutInflater.from(thisActivity).inflate(R.layout.layout_setting_item, listContent, false)
            val text = itemView.findViewById(android.R.id.text1) as TextView
            val switch = itemView.findViewById(android.R.id.button1) as SwitchCompat

            text.text = title

            itemView.setOnClickListener { switch.performClick() }

            when (i) {
                0 -> switch.isChecked = AppSaveInfoUtils.openInfo()
                1 -> switch.isChecked = AppSaveInfoUtils.isChatRoomOpen()
                2 -> switch.isChecked = AppSaveInfoUtils.isOfficialOpen()
                3 -> switch.isChecked = AppSaveInfoUtils.isCircleAvatarInfo()
                4 -> switch.isChecked = AppSaveInfoUtils.autoCloseInfo()
                5 -> {
                    switch.visibility = View.INVISIBLE
                }
            }

            switch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    when (i) {
                        0 -> AppSaveInfoUtils.setOpen(isChecked)
                        1 -> AppSaveInfoUtils.setChatRoom(isChecked)
                        2 -> AppSaveInfoUtils.setOfficial(isChecked)
                        3 -> AppSaveInfoUtils.setCircleAvatarInfo(isChecked)
                        4 -> AppSaveInfoUtils.setAutoCloseInfo(isChecked)
                    }
                }

            })

            switch.setOnClickListener {

                if (i == 5) {
                    ToolBarColorDialogHelper.getDialog(thisActivity).show()
                }

            }

            listContent.addView(itemView)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun bindView() {

        //获取公告及其Dialog
        InfoDialogBuilder.buildInfoDialog(thisActivity, object : InfoDialogBuilder.CallBack {
            override fun receive(dialog: AlertDialog) {
                clickMe.setOnClickListener { dialog.show() }
            }
        })

        //檢查配置
        loadConfig()
    }

    private fun loadConfig() {
        //是否已经适配了合适的数据的标记(已经成功匹配过)
        val hasSuitWechatData = AppSaveInfoUtils.hasSuitWechatDataInfo()

        //play开关是否打开的标记
        val playVersion = AppSaveInfoUtils.isplayVersionInfo()

        //当前保存的微信版本号
        val saveWechatVersionCode = AppSaveInfoUtils.wechatVersionInfo()

        //当前保存的主程序版本号
        val saveHelperVersionCode = AppSaveInfoUtils.helpVersionCodeInfo()

        //当前的微信版本号
        val wechatVersionCode = MyApplication.get().getWechatVersionCode().toString()

        //当前的主程序版本号
        val helperVersionCode = MyApplication.get().getHelperVersionCode().toString()

        //  如果微信版本号发生了变化且保存过版本号（上次使用别的版本加载过）
        //  或者保存的数据中是 没有适合的数据的标记
        //  或者主程序版本号发生了改变
        if (wechatVersionCode != saveWechatVersionCode && saveWechatVersionCode != "0"
                || !hasSuitWechatData
                || saveHelperVersionCode != helperVersionCode) {

            //则发送数据请求
            sendRequest(wechatVersionCode, playVersion)
        } else {

            //否则则取出上次保存的合适的信息
            detail.setTextColor(0xFF888888.toInt())
            detail.text = AppSaveInfoUtils.showInfo()
        }
    }

    private fun sendRequest(versionCode: String, play_version: Boolean) {

        val requestBody = FormBody.Builder()
                .add("versionCode", versionCode)
                .add("isPlayVersion", if (play_version) "1" else "0").build()

        val request = Request.Builder().url(ApiManager.UrlPath.CLASS_MAPPING).post(requestBody).build()

        ApiManager.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call, response: Response) {

                try {
                    val string = response.body()!!.string()
                    val jsonObject = JsonParser().parse(string).asJsonObject
                    val code = jsonObject.get("code").asInt
                    val msg = jsonObject.get("msg").asString

                    //保存匹配到的数据
                    if (code == 0) {
                        AppSaveInfoUtils.setJson(jsonObject.get("data").toString())
                        setSuccessText(msg)
                    } else {
                        AppSaveInfoUtils.setJson("")
                        setFailText(msg)
                    }

                    //保存主程序版本号
                    AppSaveInfoUtils.setHelpVersionCodeInfo(MyApplication.get().getHelperVersionCode().toString())

                } catch (e: Exception) {
                    e.printStackTrace()
                    setFailText("从服务器获取数据失败，请联系检查网络链接")
                }
            }
        })
    }


    private fun setFailText(msg: String) {
        runOnUiThread {
            detail.text = msg
            detail.setTextColor(0xFFFF0000.toInt())

            AppSaveInfoUtils.setSuitWechatDataInfo(false)
            AppSaveInfoUtils.setShowInfo(msg)
        }
    }

    private fun setSuccessText(msg: String) {
        runOnUiThread {
            detail.setTextColor(0xFF888888.toInt())
            detail.text = msg

            AppSaveInfoUtils.setWechatVersionInfo(MyApplication.get().getWechatVersionCode().toString())
            AppSaveInfoUtils.setSuitWechatDataInfo(true)
            AppSaveInfoUtils.setShowInfo(msg)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onRestart() {
        super.onRestart()
        permissionHelper = PermissionHelper.check(thisActivity)
    }

}