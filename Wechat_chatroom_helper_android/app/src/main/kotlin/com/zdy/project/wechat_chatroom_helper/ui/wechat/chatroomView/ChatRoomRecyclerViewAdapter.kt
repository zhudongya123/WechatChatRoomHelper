package com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.zdy.project.wechat_chatroom_helper.HookLogic
import com.zdy.project.wechat_chatroom_helper.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomViewHelper
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomViewHolder
import java.util.*

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

class ChatRoomRecyclerViewAdapter internal constructor(private val mContext: Context, private val originAdapter: Any) : RecyclerView.Adapter<ChatRoomViewHolder>() {

    var muteListInAdapterPositions = ArrayList<Int>()

    private var onDialogItemClickListener: OnDialogItemClickListener? = null

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
        holder.content.text = item.content
        holder.time.text = item.time

        HookLogic.setAvatar(holder.avatar, item.avatarString)

        if (item.unReadCount!! > 0)
            holder.unread.background = ShapeDrawable(object : Shape() {
                override fun draw(canvas: Canvas, paint: Paint) {
                    val size = canvas.width

                    paint.isAntiAlias = true
                    paint.color = -0x10000
                    paint.style = Paint.Style.FILL_AND_STROKE
                    canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), paint)
                }
            })
        else
            holder.unread.background = BitmapDrawable(mContext.resources)

        holder.itemView.background = ChatRoomViewHelper.getItemViewBackground()

        holder.itemView.setOnClickListener { onDialogItemClickListener!!.onItemClick(muteListInAdapterPositions[position]) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface OnDialogItemClickListener {
        fun onItemClick(relativePosition: Int)
    }
}
