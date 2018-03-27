package com.zdy.project.wechat_chatroom_helper.ui.helper.uisetting

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.ui.helper.dialog.ChooseColorDialogHelper
import com.zdy.project.wechat_chatroom_helper.utils.ColorUtils

/**
 * 此类为 UiSetting 中的设置 Fragment
 *
 * Created by zhudo on 2017/12/2.
 */
class SettingFragment : Fragment() {

    private lateinit var settingViewHolder: SettingViewModel
    private lateinit var thisActivity: UISettingActivity
    private lateinit var contentLayout: LinearLayout

    private var titles = arrayOf("助手ToolBar颜色", "助手背景颜色", "会话列表标题颜色", "会话列表内容颜色", "会话列表时间颜色", "会话列表分割线颜色")
    private var types = arrayOf(ChooseColorDialogHelper.ColorType.Toolbar, ChooseColorDialogHelper.ColorType.Helper, ChooseColorDialogHelper.ColorType.Nickname,
            ChooseColorDialogHelper.ColorType.Content, ChooseColorDialogHelper.ColorType.Time, ChooseColorDialogHelper.ColorType.Divider)


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        thisActivity = context as UISettingActivity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        contentLayout = LinearLayout(thisActivity).apply { orientation = LinearLayout.VERTICAL }

        repeat(titles.size) { index ->
            contentLayout.addView(LayoutInflater.from(thisActivity)
                    .inflate(R.layout.layout_color_setting_item, contentLayout, false)
                    .also {
                        it.findViewById<TextView>(android.R.id.text1).text = titles[index]
                        it.setOnClickListener { ChooseColorDialogHelper.getDialog(thisActivity, types[index]).show() }
                    })
        }
        return contentLayout

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        settingViewHolder = UISettingActivity.obtainSettingViewModel(thisActivity)
        setupSettingColor()
    }


    override fun onResume() {
        super.onResume()
        settingViewHolder.start()
    }

    private fun setupSettingColor() {
        settingViewHolder.toolbarColor.observe(thisActivity, Observer<String> {
            setItemColor(0, it)
        })
        settingViewHolder.helperColor.observe(thisActivity, Observer<String> {
            setItemColor(1, it)
        })
        settingViewHolder.nicknameColor.observe(thisActivity, Observer<String> {
            setItemColor(2, it)
        })
        settingViewHolder.contentColor.observe(thisActivity, Observer<String> {
            setItemColor(3, it)
        })
        settingViewHolder.timeColor.observe(thisActivity, Observer<String> {
            setItemColor(4, it)
        })
        settingViewHolder.dividerColor.observe(thisActivity, Observer<String> {
            setItemColor(5, it)
        })
    }

    private fun setItemColor(index: Int, value: String?) {
        contentLayout.getChildAt(index).findViewById<TextView>(android.R.id.text2)
                .also { view ->
                    view.text = value
                    view.setTextColor(ColorUtils.getColorInt(value.toString()))
                }
    }


}