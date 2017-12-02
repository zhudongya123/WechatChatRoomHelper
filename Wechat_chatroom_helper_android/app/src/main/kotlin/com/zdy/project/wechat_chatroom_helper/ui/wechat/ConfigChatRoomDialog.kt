package com.zdy.project.wechat_chatroom_helper.ui.wechat

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import utils.AppSaveInfoUtils


/**
 * Created by zhudo on 2017/11/6.
 */
class ConfigChatRoomDialog(private var mContext: Context) : Dialog(mContext) {


    private lateinit var rootView: LinearLayout
    private lateinit var radioGroup: RadioGroup
    private lateinit var button: Button

    private var onModeChangedListener: OnModeChangedListener? = null
    private var onWhiteListClickListener: OnWhiteListClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView(mContext))


        if (AppSaveInfoUtils.chatRoomTypeInfo().toInt() == 2) {
            (radioGroup.findViewById<RadioButton>(MUTE_CHAT_ROOM_ID)).isChecked = true
        } else (radioGroup.findViewById<RadioButton>(ALL_CHAT_ROOM_ID)).isChecked = true

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (AppSaveInfoUtils.chatRoomTypeInfo().toInt() != checkedId) {
                AppSaveInfoUtils.clearWhiteList(AppSaveInfoUtils.WHITE_LIST_CHAT_ROOM)
                AppSaveInfoUtils.setChatRoomType(checkedId.toString())
                if (onModeChangedListener != null)
                    onModeChangedListener!!.onChanged()
                dismiss()
            }
        }

        button.setOnClickListener {
            if (onWhiteListClickListener != null)
                onWhiteListClickListener!!.onClick()
            dismiss()
        }
    }

    fun setOnModeChangedListener(onModeChangedListener: OnModeChangedListener) {
        this.onModeChangedListener = onModeChangedListener
    }


    fun setOnWhiteListClickListener(onWhiteListClickListener: OnWhiteListClickListener) {
        this.onWhiteListClickListener = onWhiteListClickListener
    }


    private fun getContentView(context: Context): View {

        val padding = ScreenUtils.dip2px(context, 16f)
        rootView = LinearLayout(context)
        rootView.orientation = LinearLayout.VERTICAL
        rootView.setPadding(padding, padding, padding, padding)

        val name = TextView(context)
        name.text = "设置群消息助手要显示的群"
        name.setTextColor(0xff000000.toInt())
        name.textSize = 16f


        radioGroup = RadioGroup(context)
        val radioButton1 = RadioButton(context)

        val layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, 0, 0, padding)
        radioButton1.layoutParams = layoutParams

        val radioButton2 = RadioButton(context)

        radioButton1.text = "所有群"
        radioButton1.id = ALL_CHAT_ROOM_ID
        radioButton2.text = "免打扰消息的群"
        radioButton2.id = MUTE_CHAT_ROOM_ID


        radioGroup.addView(radioButton1)
        radioGroup.addView(radioButton2)

        radioGroup.setPadding(0, padding, 0, padding)

        button = Button(context)
        button.text = "排除群消息助手中不显示上述条件的群（白名单）"

        rootView.addView(name)
        rootView.addView(radioGroup)
        rootView.addView(button)

        return rootView
    }

    @IdRes
    private var ALL_CHAT_ROOM_ID = 1
    @IdRes
    private var MUTE_CHAT_ROOM_ID = 2


    interface OnModeChangedListener {
        fun onChanged()
    }

    interface OnWhiteListClickListener {
        fun onClick()
    }

}