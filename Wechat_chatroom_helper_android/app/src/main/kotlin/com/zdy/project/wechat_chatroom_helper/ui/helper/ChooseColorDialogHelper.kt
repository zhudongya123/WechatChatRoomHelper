package com.zdy.project.wechat_chatroom_helper.ui.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.zdy.project.wechat_chatroom_helper.utils.ColorUtils
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import utils.AppSaveInfoUtils

/**
 * Created by zhudo on 2017/11/14.
 */
class ChooseColorDialogHelper {

    enum class TYPE {
        Toolbar, Helper, Nickname, Content, Time, Divider
    }

    companion object {

        fun getDialog(mContext: Context, type: TYPE): AlertDialog {

            val subView = getDialogEditText(mContext)
            val editText = subView.findViewById<EditText>(android.R.id.edit)
            val color = when (type) {
                TYPE.Toolbar -> AppSaveInfoUtils.toolbarColorInfo()
                TYPE.Helper -> AppSaveInfoUtils.helperColorInfo()
                TYPE.Nickname -> AppSaveInfoUtils.nicknameColorInfo()
                TYPE.Content -> AppSaveInfoUtils.contentColorInfo()
                TYPE.Time -> AppSaveInfoUtils.timeColorInfo()
                TYPE.Divider -> AppSaveInfoUtils.dividerColorInfo()
            }

            editText.backgroundTintList = ColorStateList.valueOf(ColorUtils.getColorInt(color))
            editText.backgroundTintMode = PorterDuff.Mode.SRC_IN
            editText.setTextColor(ColorUtils.getColorInt(color))
            editText.hint = "当前值" + color
            editText.setSingleLine()
            editText.setSelection(editText.text.length)

            val alertDialog = AlertDialog.Builder(mContext)
                    .setTitle(when (type) {
                        TYPE.Toolbar -> "助手ToolBar颜色"
                        TYPE.Helper -> "助手背景颜色"
                        TYPE.Nickname -> "会话列表标题颜色"
                        TYPE.Content -> "会话列表内容颜色"
                        TYPE.Time -> "会话列表时间颜色"
                        TYPE.Divider -> "会话列表分割线颜色"
                    })
                    .setMessage("请输入6位颜色值代码，示例：FF0000（红色），不支持alpha通道（透明度）")
                    .setView(subView)
                    .setPositiveButton("确认") { dialog, which ->
                        dialog.dismiss()

                        val value = editText.text.toString()

                        when (type) {
                            TYPE.Toolbar -> AppSaveInfoUtils.setToolbarColorInfo(value)
                            TYPE.Helper -> AppSaveInfoUtils.setHelperColorInfo(value)
                            TYPE.Nickname -> AppSaveInfoUtils.setNicknameColorInfo(value)
                            TYPE.Content -> AppSaveInfoUtils.setContentColorInfo(value)
                            TYPE.Time -> AppSaveInfoUtils.setTimeColorInfo(value)
                            TYPE.Divider -> AppSaveInfoUtils.setDividerColorInfo(value)
                        }

                    }
                    .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.create()

            alertDialog.setOnShowListener {
                editText.addTextChangedListener(ToolBarColorTextWatcher(editText, alertDialog.findViewById(android.R.id.button1)!!))
                alertDialog.findViewById<Button>(android.R.id.button1)!!.isEnabled = false
            }
            return alertDialog
        }


        private fun getDialogEditText(context: Context): View {
            val linearLayout = LinearLayout(context)
            val editText = EditText(context)
            editText.maxLines = 1
            editText.setSingleLine()
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val size = ScreenUtils.dip2px(context, 12f)
            params.setMargins(size, size / 2, size, size / 2)
            linearLayout.setPadding(size, 0, size, 0)
            editText.layoutParams = params
            editText.id = android.R.id.edit
            linearLayout.addView(editText)
            return linearLayout
        }




        class ToolBarColorTextWatcher  constructor(private var editText: EditText, private var button: View) : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 6)
                    try {
                        val color = ColorUtils.getColorInt(s)
                        val editText = editText
                        editText.setTextColor(color)
                        editText.backgroundTintList = ColorStateList.valueOf(color)
                        editText.backgroundTintMode = PorterDuff.Mode.SRC_IN
                        button.isEnabled = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        button.isEnabled = false
                    }
                else {
                    button.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable) {
            }
        }
    }


}