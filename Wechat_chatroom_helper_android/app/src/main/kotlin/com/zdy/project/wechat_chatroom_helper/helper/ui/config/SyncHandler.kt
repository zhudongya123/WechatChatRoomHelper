package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R
import java.text.SimpleDateFormat
import java.util.*


class SyncHandler(val activity: ConfigActivity) : Handler() {

    companion object {

        const val HANDLER_TEXT_CHANGE_LINE = 1
        const val HANDLER_TEXT_ADDITION = 2
        const val HANDLER_SHOW_NEXT_BUTTON = 3

        const val TEXT_COLOR_NORMAL = 1
        const val TEXT_COLOR_PASS = 2
        const val TEXT_COLOR_ERROR = 3

        fun makeTypeSpec(type: Int, color: Int) = type * 100 + color

        fun getType(spec: Int) = spec / 100

        fun getColor(spec: Int) = spec % 100
    }

    private val configTextView = activity.findViewById<TextView>(R.id.config_step3_text1)

    override fun handleMessage(msg: Message) {

        val time = SimpleDateFormat("HH:mm:ss", Locale.CHINESE).format(Calendar.getInstance().time)

        val type = getType(msg.what)
        when (type) {
            HANDLER_TEXT_ADDITION,
            HANDLER_TEXT_CHANGE_LINE -> {

                val spannableStringBuilder =
                        if (configTextView.text == "") {
                            val spannableStringBuilder = SpannableStringBuilder()
                            configTextView.text = spannableStringBuilder
                            spannableStringBuilder
                        } else SpannableStringBuilder(configTextView.text as SpannedString)

                val format = activity.getString(R.string.config_step3_text_ex)
                val part1 = time
                val part2 = msg.obj as String


                val singleString = String.format(Locale.CHINESE, format, part1, part2)

                if (type == HANDLER_TEXT_CHANGE_LINE && spannableStringBuilder.toString().contains("个类")) {
                    spannableStringBuilder.delete(0, spannableStringBuilder.toString().indexOfFirst { it == '\n' } + 1)
                }

                spannableStringBuilder.insert(0, singleString)

                val startLength = 0
                val endLength = singleString.length

                val color = when (getColor(msg.what)) {

                    TEXT_COLOR_NORMAL -> Color.GRAY
                    TEXT_COLOR_PASS -> ContextCompat.getColor(activity, R.color.right_color)
                    TEXT_COLOR_ERROR -> ContextCompat.getColor(activity, R.color.error_color)
                    else -> Color.GRAY
                }
                spannableStringBuilder.setSpan(ForegroundColorSpan(color), startLength, endLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                configTextView.text = spannableStringBuilder
            }

            HANDLER_SHOW_NEXT_BUTTON -> {
                activity.setNavigationBarNextButtonEnabled(true)
            }
        }
    }
}