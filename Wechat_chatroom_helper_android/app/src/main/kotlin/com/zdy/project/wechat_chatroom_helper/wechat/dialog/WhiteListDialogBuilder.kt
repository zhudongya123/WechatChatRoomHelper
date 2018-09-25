package com.zdy.project.wechat_chatroom_helper.wechat.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.message.MessageFactory


class WhiteListDialogBuilder {

    var pageType = 0
    lateinit var keyName: String

    fun getWhiteListDialog(mContext: Context): AlertDialog {
        val whiteListAdapter = WhiteListAdapter(mContext)

        if (pageType == PageType.OFFICIAL) {
            keyName = AppSaveInfo.WHITE_LIST_OFFICIAL
            whiteListAdapter.list = MessageFactory.getAllOfficial()

        } else if (pageType == PageType.CHAT_ROOMS) {
            keyName = AppSaveInfo.WHITE_LIST_CHAT_ROOM
            whiteListAdapter.list = MessageFactory.getAllChatRoom()
        }

        return AlertDialog.Builder(mContext)
                .setTitle("请选择不需要显示在助手中的条目")
                .setPositiveButton("确认") { dialog, which ->
                    WechatJsonUtils.putFileString()
                    dialog.dismiss()
                }
                .setAdapter(whiteListAdapter) { _, _ -> }
                .create()

    }


    inner class WhiteListAdapter(private val mContext: Context) : BaseAdapter() {

        var list = mutableListOf<ChatInfoModel>()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            val viewHolder: WhiteItemViewHolder
            val itemView: View

            if (convertView == null) {
                viewHolder = WhiteItemViewHolder()
                itemView = getItemView()

                viewHolder.switch = itemView.findViewById(android.R.id.checkbox) as Switch
                viewHolder.text1 = itemView.findViewById(android.R.id.text1) as TextView
                viewHolder.text2 = itemView.findViewById(android.R.id.text2) as TextView

                itemView.tag = viewHolder
            } else {
                itemView = convertView
                viewHolder = convertView.tag as WhiteItemViewHolder
            }

            val nickname = getItem(position).field_nickname.toString()
            val username = getItem(position).field_username.toString()

            viewHolder.text1.text = nickname
            viewHolder.text2.text = username

            val existList = AppSaveInfo.getWhiteList(keyName)
            viewHolder.switch.setOnCheckedChangeListener(null)

            val size = existList.size
            for (index in 0 until size) {
                val itemUsername = existList[index]
                if (itemUsername == username) {
                    viewHolder.switch.isChecked = true
                    break
                }
                if (index == existList.size - 1) {
                    viewHolder.switch.isChecked = false
                }
            }
            viewHolder.switch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) AppSaveInfo.setWhiteList(keyName, username)
                else AppSaveInfo.removeWhitList(keyName, username)
            }
            return itemView
        }

        override fun getItem(position: Int) = list[position]

        override fun getItemId(position: Int) = position.toLong()

        override fun getCount() = list.size

        inner class WhiteItemViewHolder {

            lateinit var text1: TextView
            lateinit var text2: TextView
            lateinit var switch: Switch
        }

        private fun getItemView(): View {
            val itemView = RelativeLayout(mContext)

            itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, 64f))
            itemView.setPadding(ScreenUtils.dip2px(mContext, 16f), 0, ScreenUtils.dip2px(mContext, 16f), 0)

            val textContainer = LinearLayout(mContext)
            textContainer.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            textContainer.gravity = Gravity.CENTER_VERTICAL
            textContainer.orientation = LinearLayout.VERTICAL

            val text1 = TextView(mContext)
            text1.maxLines = 1
            text1.id = android.R.id.text1

            val text2 = TextView(mContext)
            text2.maxLines = 1
            text2.id = android.R.id.text2
            text2.setPadding(0, ScreenUtils.dip2px(mContext, 4f), 0, 0)

            textContainer.addView(text1)
            textContainer.addView(text2)

            val switch = Switch(mContext)
            switch.id = android.R.id.checkbox
            switch.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
            }

            DrawableCompat.setTintList(switch.thumbDrawable, ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(0xFF12B7F6.toInt(), 0xFFf1f1f1.toInt())))

            itemView.addView(textContainer)
            itemView.addView(switch)

            return itemView
        }
    }


}