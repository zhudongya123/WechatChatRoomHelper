package com.zdy.project.wechat_chatroom_helper.wechat.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo

class WhiteListDialog(private var mContext: Context) : Dialog(mContext) {

    private lateinit var listener: View.OnClickListener

    lateinit var list: ArrayList<String>
    var pageType = 0
    lateinit var keyName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        keyName = if (pageType == PageType.OFFICIAL) AppSaveInfo.WHITE_LIST_OFFICIAL else AppSaveInfo.WHITE_LIST_CHAT_ROOM
        setContentView(getContentView())

        val attributes = window.attributes
        attributes.width = ScreenUtils.dip2px(mContext, 320f)
        window.attributes = attributes

    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    private fun getContentView(): ViewGroup {

        val rootView = LinearLayout(mContext)
        rootView.gravity = Gravity.RIGHT
        rootView.orientation = LinearLayout.VERTICAL
        val padding = ScreenUtils.dip2px(mContext, 16f)
        rootView.setPadding(padding, padding, padding, padding)

        val title = TextView(mContext)
        title.setTextColor(0xff000000.toInt())
        title.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, 48f))
        title.gravity = Gravity.CENTER_VERTICAL
        title.text = "请选择不需要显示在助手中的条目"
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)


        val button = TextView(mContext)
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ScreenUtils.dip2px(mContext, 32f))
        layoutParams.setMargins(0, padding / 2, padding / 2, 0)
        button.layoutParams = layoutParams
        button.gravity = Gravity.CENTER_VERTICAL
        button.setPadding(padding / 2, 0, padding / 2, 0)
        button.text = "确认"
        button.background = mContext.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).getDrawable(0)


        val existList = AppSaveInfo.getWhiteList(keyName)
        val listView = LinearLayout(mContext)
        listView.orientation = LinearLayout.VERTICAL

        for (s in list) {
            val switch = Switch(mContext)

            switch.text = s

            switch.buttonTintList = ColorStateList.valueOf(Color.rgb(26, 173, 25))

            val params = LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, 250f), ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, ScreenUtils.dip2px(mContext, 12f))


            existList
                    .filter { it == s }
                    .forEach { switch.isChecked = true }

            switch.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) AppSaveInfo.setWhiteList(keyName, buttonView.text.toString())
                else AppSaveInfo.removeWhitList(keyName, buttonView.text.toString())
            }

            listView.addView(switch, params)
        }

        val scrollView = ScrollView(mContext)

        if (list.size > 6) {
            scrollView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    ScreenUtils.dip2px(mContext, 210f))
        }
        scrollView.addView(listView)

        rootView.addView(title)
        rootView.addView(scrollView)
        rootView.addView(button)

        button.setOnClickListener {

            val unSelectCount = (0 until listView.childCount).count { !(listView.getChildAt(it) as Switch).isChecked }

            if (unSelectCount == 0) {
                Toast.makeText(mContext, "您不能移除助手里面的所有会话", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            dismiss()
            listener.onClick(button)
        }

        return rootView
    }


}