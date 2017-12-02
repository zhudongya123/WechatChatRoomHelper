package com.zdy.project.wechat_chatroom_helper.ui.helper.uisetting

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R

/**
 * Created by zhudo on 2017/12/2.
 */
class SettingFragment : Fragment() {


    private lateinit var thisActivity: Activity
    private lateinit var contentLayout: LinearLayout


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        thisActivity = context as Activity
    }

    private var titles = arrayOf("助手ToolBar颜色", "助手背景颜色", "会话列表标题颜色", "会话列表内容颜色", "会话列表时间颜色")

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        contentLayout = LinearLayout(thisActivity)
        contentLayout.orientation = LinearLayout.VERTICAL


        for (i in 0 until 5) {
            val itemView = LayoutInflater.from(thisActivity).inflate(R.layout.layout_color_setting_item, contentLayout, false)
        val text1 = itemView.findViewById<TextView>(android.R.id.text1)
//        val text2 = itemView.findViewById<TextView>(android.R.id.text2)
            contentLayout.addView(itemView)
        }

        return contentLayout

    }

}