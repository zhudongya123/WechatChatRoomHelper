package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.zdy.project.wechat_chatroom_helper.helper.utils.ColorUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils

/**
 * Created by zhudo on 2017/11/14.
 */
object ChooseColorDialogHelper {

    enum class ColorType {
        Toolbar, Helper, Nickname, Content, Time, Divider, HighLight
    }


    fun getDialog(mContext: Context, colorType: ColorType): AlertDialog {

        val subView = getDialogEditText(mContext)

        //构建EditText
        val editText = subView.findViewById<EditText>(android.R.id.edit)
                .apply {
                    val color = when (colorType) {
                        ColorType.Toolbar -> AppSaveInfo.toolbarColorInfo(mContext)
                        ColorType.Helper -> AppSaveInfo.helperColorInfo(mContext)
                        ColorType.Nickname -> AppSaveInfo.nicknameColorInfo(mContext)
                        ColorType.Content -> AppSaveInfo.contentColorInfo(mContext)
                        ColorType.Time -> AppSaveInfo.timeColorInfo(mContext)
                        ColorType.Divider -> AppSaveInfo.dividerColorInfo(mContext)
                        ColorType.HighLight -> AppSaveInfo.highLightColorInfo(mContext)
                    }

                    backgroundTintList = ColorStateList.valueOf(ColorUtils.getColorInt(color))
                    backgroundTintMode = PorterDuff.Mode.SRC_IN
                    setTextColor(ColorUtils.getColorInt(color))
                    hint = "当前值$color"
                    setSingleLine()
                    setSelection(text.length)

                }

        return AlertDialog.Builder(mContext)
                .setTitle(when (colorType) {
                    ColorType.Toolbar -> "助手ToolBar颜色"
                    ColorType.Helper -> "助手背景颜色"
                    ColorType.Nickname -> "会话列表标题颜色"
                    ColorType.Content -> "会话列表内容颜色"
                    ColorType.Time -> "会话列表时间颜色"
                    ColorType.Divider -> "会话列表分割线颜色"
                    ColorType.HighLight -> "置顶会话颜色"
                })
                .setMessage("请输入6位颜色值代码，示例：FF0000（红色），不支持alpha通道（透明度）")
                .setView(subView)
                .setPositiveButton("确认") { dialog, which ->
                    dialog.dismiss()

                    val value = editText.text.toString()

                    when (colorType) {
                        ColorType.Toolbar -> AppSaveInfo.setToolbarColorInfo(value)
                        ColorType.Helper -> AppSaveInfo.setHelperColorInfo(value)
                        ColorType.Nickname -> AppSaveInfo.setNicknameColorInfo(value)
                        ColorType.Content -> AppSaveInfo.setContentColorInfo(value)
                        ColorType.Time -> AppSaveInfo.setTimeColorInfo(value)
                        ColorType.Divider -> AppSaveInfo.setDividerColorInfo(value)
                       ColorType.HighLight -> AppSaveInfo.setHighLightColorInfo(value)
                    }

                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }
                .create()
                .also {
                    it.setOnShowListener {
                        editText.addTextChangedListener(ToolBarColorTextWatcher(editText, (it as AlertDialog).findViewById(R.id.button1)!!))
                        it.findViewById<Button>(R.id.button1)!!.isEnabled = false
                    }
                }
    }

    /**
     * 构建带Margin 和 padding 的 EditText
     */
    private fun getDialogEditText(context: Context): View {

        val marginSize = ScreenUtils.dip2px(context, 12f)

        val linearLayout = LinearLayout(context).also { it.setPadding(marginSize, 0, marginSize, 0) }
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .also { it.setMargins(marginSize, marginSize / 2, marginSize, marginSize / 2) }

        val editText = EditText(context).apply {
            maxLines = 1
            layoutParams = params
            id = android.R.id.edit

        }.also { it.setSingleLine() }

        return linearLayout.also { it.addView(editText) }
    }


    class ToolBarColorTextWatcher constructor(
            private var editText: EditText,
            private var button: View) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s.length == 6)
                try {
                    val color = ColorUtils.getColorInt(s)

                    editText.apply {
                        backgroundTintList = ColorStateList.valueOf(color)
                        backgroundTintMode = PorterDuff.Mode.SRC_IN
                    }.also { it.setTextColor(color) }

                    button.isEnabled = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    button.isEnabled = false
                }
            else {
                button.isEnabled = false
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }


}