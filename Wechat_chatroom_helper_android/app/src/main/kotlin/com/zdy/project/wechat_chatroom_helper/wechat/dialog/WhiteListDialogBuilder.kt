package com.zdy.project.wechat_chatroom_helper.wechat.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.message.MessageFactory


class WhiteListDialogBuilder {

    private lateinit var listener: View.OnClickListener
    var pageType = 0
    lateinit var keyName: String

    fun getWhiteListDialog(mContext: Context): AlertDialog {

        keyName = if (pageType == PageType.OFFICIAL) AppSaveInfo.WHITE_LIST_OFFICIAL else AppSaveInfo.WHITE_LIST_CHAT_ROOM

        return AlertDialog.Builder(mContext).setMessage("请选择不需要显示在助手中的条目")
                .setPositiveButton("确认", DialogInterface.OnClickListener { dialog, which ->
                    //                    val unSelectCount = (0 until listView.childCount).count { !(listView.getChildAt(it) as Switch).isChecked }
//                    if (unSelectCount == 0) {
//                        Toast.makeText(mContext, "您不能移除助手里面的所有会话", Toast.LENGTH_SHORT).show()
//                        return@OnClickListener
//                    }

                    dialog.dismiss()
                    listener.onClick(null)
                }).setAdapter(WhiteListAdapter(mContext)
                        .apply {
                            list = if (pageType == PageType.OFFICIAL)
                                MessageFactory.getAllOfficial()
                            else MessageFactory.getAllChatRoom()
                        }) { dialog, which -> }.create()

    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
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
            existList.forEach {
                if (it == username) {
                    viewHolder.switch.isChecked = true
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
                    .apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, 64f))
                        setPadding(ScreenUtils.dip2px(mContext, 16f), 0, ScreenUtils.dip2px(mContext, 16f), 0)
                    }

            val textContainer = LinearLayout(mContext)
            textContainer.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            textContainer.gravity = Gravity.CENTER_VERTICAL
            textContainer.orientation = LinearLayout.VERTICAL


            val text1 = TextView(mContext)
            text1.maxLines = 1
            text1.id = android.R.id.text1

            val text2 = TextView(mContext)
            text2.maxLines = 1
            text1.id = android.R.id.text2
            text2.setPadding(0, ScreenUtils.dip2px(mContext, 4f), 0, 0)

            textContainer.addView(text1)
            textContainer.addView(text2)


            val switch = Switch(mContext)
            switch.id = android.R.id.checkbox
            switch.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
            }

            itemView.addView(textContainer)
            itemView.addView(switch)

            return itemView
        }
    }


    var list = mutableListOf<String>()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//
//        setContentView(getContentView())
//
//        val attributes = window.attributes
//        attributes.width = ScreenUtils.dip2px(mContext, 320f)
//        window.attributes = attributes
//
//    }


//    private fun getContentView(): ViewGroup {
//
//        val rootView = LinearLayout(mContext)
//        rootView.gravity = Gravity.RIGHT
//        rootView.orientation = LinearLayout.VERTICAL
//        val padding = ScreenUtils.dip2px(mContext, 16f)
//        rootView.setPadding(padding, padding, padding, padding)
//
//        val title = TextView(mContext)
//        title.setTextColor(0xff000000.toInt())
//        title.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, 48f))
//        title.gravity = Gravity.CENTER_VERTICAL
//        title.text = "请选择不需要显示在助手中的条目"
//        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//
//
//        val button = TextView(mContext)
//        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ScreenUtils.dip2px(mContext, 32f))
//        layoutParams.setMargins(0, padding / 2, padding / 2, 0)
//        button.layoutParams = layoutParams
//        button.gravity = Gravity.CENTER_VERTICAL
//        button.setPadding(padding / 2, 0, padding / 2, 0)
//        button.text = "确认"
//        button.background = mContext.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).getDrawable(0)
//
//
//        val existList = AppSaveInfo.getWhiteList(keyName)
//        val listView = LinearLayout(mContext)
//        listView.orientation = LinearLayout.VERTICAL
//
//        for (s in list) {
//            val switch = Switch(mContext)
//
//            switch.text = s
//
//            switch.buttonTintList = ColorStateList.valueOf(Color.rgb(26, 173, 25))
//            val params = LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, 250f), ViewGroup.LayoutParams.WRAP_CONTENT)
//            params.setMargins(0, 0, 0, ScreenUtils.dip2px(mContext, 12f))
//
//            existList
//                    .filter { it == s }
//                    .forEach { switch.isChecked = true }
//
//            switch.setOnCheckedChangeListener { buttonView, isChecked ->
//
//                if (isChecked) AppSaveInfo.setWhiteList(keyName, buttonView.text.toString())
//                else AppSaveInfo.removeWhitList(keyName, buttonView.text.toString())
//            }
//
//            listView.addView(switch, params)
//        }
//
//        val scrollView = ScrollView(mContext)
//
//        if (list.size > 6) {
//            scrollView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                    ScreenUtils.dip2px(mContext, 210f))
//        }
//        scrollView.addView(listView)
//
//        //  rootView.addView(title)
//        rootView.addView(scrollView)
//        //  rootView.addView(button)
//
////        button.setOnClickListener {
////
////
////        }
//
//        return rootView
//    }


}