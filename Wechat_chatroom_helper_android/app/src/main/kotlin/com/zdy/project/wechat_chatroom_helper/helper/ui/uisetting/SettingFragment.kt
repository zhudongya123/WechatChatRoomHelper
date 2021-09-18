package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.arch.lifecycle.Observer
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.ui.base.BaseFragment
import com.zdy.project.wechat_chatroom_helper.helper.utils.ColorUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo

/**
 * 此类为 UiSetting 中的设置 Fragment
 *
 * Created by zhudo on 2017/12/2.
 */
class SettingFragment : BaseFragment() {

    companion object {
        private val Common_Title = arrayOf("助手ToolBar颜色", "助手背景颜色", "会话列表标题颜色", "会话列表内容颜色", "会话列表时间颜色", "会话列表分割线颜色", "会话列表置顶项背景颜色")
        private var Common_Types = arrayOf(ChooseColorDialogHelper.ColorType.Toolbar, ChooseColorDialogHelper.ColorType.Helper, ChooseColorDialogHelper.ColorType.Nickname,
                ChooseColorDialogHelper.ColorType.Content, ChooseColorDialogHelper.ColorType.Time, ChooseColorDialogHelper.ColorType.Divider, ChooseColorDialogHelper.ColorType.HighLight)

    }

    private lateinit var mUiSettingActivity: UISettingActivity
    private lateinit var settingViewHolder: SettingViewModel

    private lateinit var mFormView: ViewGroup
    private lateinit var mRadioGroup: RadioGroup


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mUiSettingActivity = context as UISettingActivity
    }

    override fun getLayoutResourceId() = R.layout.fragment_setting_color

    override fun bindView() {
        mRadioGroup = mContentView.findViewById(R.id.fragment_setting_color_radio_group)
        mFormView = mContentView.findViewById(R.id.fragment_setting_color_form)

        /**
         * ui变化时通知数据
         */
        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.fragment_setting_color_manual -> {
                    AppSaveInfo.setColorMode(SettingViewModel.ColorMode.Manual)
                }
                R.id.fragment_setting_color_auto -> {
                    AppSaveInfo.setColorMode(SettingViewModel.ColorMode.Auto)
                }
            }
            settingViewHolder.colorMode.value = AppSaveInfo.getColorMode()
        }

        val mLayoutInflater = LayoutInflater.from(mUiSettingActivity)
        repeat(Common_Title.size) { index ->
            val itemView = mLayoutInflater.inflate(R.layout.layout_color_setting_item, mFormView, false)
            itemView.apply {
                findViewById<TextView>(android.R.id.text1).text = Common_Title[index]
                setOnClickListener { ChooseColorDialogHelper.getDialog(mUiSettingActivity, Common_Types[index]).show() }
            }
            mFormView.addView(itemView)
        }

        settingViewHolder = UISettingActivity.obtainSettingViewModel(mUiSettingActivity)
        setupSettingColor()
    }


    override fun onResume() {
        super.onResume()
        settingViewHolder.start()
    }

    private fun setupSettingColor() {

        /**
         * 数据变化时通知ui
         */
        settingViewHolder.run {
            toolbarColor.observe(mUiSettingActivity, Observer<String> {
                setItemColor(0, it)
            })
            helperColor.observe(mUiSettingActivity, Observer<String> {
                setItemColor(1, it)
            })
            nicknameColor.observe(mUiSettingActivity, Observer<String> {
                setItemColor(2, it)
            })
            contentColor.observe(mUiSettingActivity, Observer<String> {
                setItemColor(3, it)
            })
            timeColor.observe(mUiSettingActivity, Observer<String> {
                setItemColor(4, it)
            })
            dividerColor.observe(mUiSettingActivity, Observer<String> {
                setItemColor(5, it)
            })
            highlightColor.observe(mUiSettingActivity, Observer<String> {
                setItemColor(6, it)
            })

            colorMode.observe(mUiSettingActivity, Observer {
                when (it) {
                    SettingViewModel.ColorMode.Auto -> {
                        mRadioGroup.check(R.id.fragment_setting_color_auto)
                        mFormView.visibility = View.GONE
                    }
                    SettingViewModel.ColorMode.Manual -> {
                        mRadioGroup.check(R.id.fragment_setting_color_manual)
                        mFormView.visibility = View.VISIBLE

                    }
                }
            })
        }
    }

    private fun setItemColor(index: Int, value: String?) {
        mFormView.getChildAt(index).findViewById<TextView>(android.R.id.text2)
                .also { view ->
                    view.text = value
                    view.setTextColor(ColorUtils.getColorInt(value.toString()))
                }
    }


}