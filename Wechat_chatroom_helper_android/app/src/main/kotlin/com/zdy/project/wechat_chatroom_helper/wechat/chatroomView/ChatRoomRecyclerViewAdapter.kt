package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.plugins.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.plugins.main.MainLauncherUI.launcherUI
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.Classes
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.MainAdapter
import de.robv.android.xposed.XposedHelpers
import java.util.*

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

class ChatRoomRecyclerViewAdapter internal constructor(private val mContext: Context) : RecyclerView.Adapter<ChatRoomViewHolder>() {

    var muteListInAdapterPositions = ArrayList<Int>()

    private lateinit var onDialogItemClickListener: OnDialogItemClickListener

    var data = ArrayList<ChatInfoModel>()

    fun setOnDialogItemClickListener(onDialogItemClickListener: OnDialogItemClickListener) {
        this.onDialogItemClickListener = onDialogItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder(ChatRoomViewHelper.getItemView(mContext))
    }

    private fun getObject(position: Int): ChatInfoModel {
        return data[position]
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {

        val item = getObject(position)

        holder.nickname.text = item.nickname
       // holder.content.text = Classes.getConversationContent(MainAdapter.originAdapter, item, position)
        holder.time.text = Classes.getConversationTimeString(MainAdapter.originAdapter, item.conversationTime)

//        try {
//            HookLogic.setAvatar(holder.avatar, item.avatarString)
//        } catch (e: Throwable) {
//            e.printStackTrace()
//            holder.avatar.setImageResource(R.mipmap.ic_launcher)
//        }

        Classes.getConversationAvatar(item.username.toString(), holder.avatar)

        if (item.unReadCount > 0)
            holder.unread.background = ShapeDrawable(object : Shape() {
                override fun draw(canvas: Canvas, paint: Paint) {
                    val size = (canvas.width / 2).toFloat()

                    paint.isAntiAlias = true
                    paint.color = -0x10000
                    paint.style = Paint.Style.FILL_AND_STROKE
                    canvas.drawCircle(size, size, size, paint)
                }
            })
        else
            holder.unread.background = BitmapDrawable(mContext.resources)

        holder.itemView.background = ChatRoomViewHelper.getItemViewBackground(mContext)
        holder.itemView.setOnClickListener {
            //            try {
//                onDialogItemClickListener.onItemClick(muteListInAdapterPositions[position])
//            } catch (t: Throwable) {
//                t.printStackTrace()
//            }

            XposedHelpers.callMethod(MainLauncherUI.launcherUI, "startChatting", item.username, null, true)

        }
        holder.itemView.setOnLongClickListener {
            try {
                onDialogItemClickListener.onItemLongClick(muteListInAdapterPositions[position])
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return@setOnLongClickListener true
        }

//        holder.nickname.setTextColor(Color.parseColor("#" + AppSaveInfo.nicknameColorInfo()))
//        holder.content.setTextColor(Color.parseColor("#" + AppSaveInfo.contentColorInfo()))
//        holder.time.setTextColor(Color.parseColor("#" + AppSaveInfo.timeColorInfo()))
//        holder.divider.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.dividerColorInfo()))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface OnDialogItemClickListener {
        fun onItemClick(relativePosition: Int)

        fun onItemLongClick(relativePosition: Int)
    }


}
