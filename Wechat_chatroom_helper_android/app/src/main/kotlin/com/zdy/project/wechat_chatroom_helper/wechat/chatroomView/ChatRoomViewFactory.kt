package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.support.annotation.IdRes
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.helper.utils.ColorUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils

/**
 * Created by Mr.Zdy on 2017/8/27.
 */
@SuppressWarnings("ResourceType")
object ChatRoomViewFactory {

    @IdRes
    private val id_avatar_container = 7
    @IdRes
    val id_avatar = 1
    @IdRes
    val id_nickname = 2
    @IdRes
    val id_time = 3
    @IdRes
    val id_msg_state = 4
    @IdRes
    val id_content = 5
    @IdRes
    val id_unread = 6
    @IdRes
    val id_divider = 8


    fun getItemView(mContext: Context): View {

        val itemView = RelativeLayout(mContext)
        val contentView = RelativeLayout(mContext)
        val avatarContainer = RelativeLayout(mContext)
        val avatar = ImageView(mContext)
        val nickName = TextView(mContext)
        val time = TextView(mContext)

        val contentContainer = LinearLayout(mContext)
        val msgState = ImageView(mContext)
        val content = TextView(mContext)
        val unread = TextView(mContext)

        val divider = View(mContext)

        avatar.id = id_avatar
        avatarContainer.id = id_avatar_container
        nickName.id = id_nickname
        time.id = id_time
        msgState.id = id_msg_state
        content.id = id_content
        unread.id = id_unread
        divider.id = id_divider

        nickName.setTextColor(0xFF353535.toInt())
        content.setTextColor(0xFFAAAAAA.toInt())
        time.setTextColor(0xFFAAAAAA.toInt())
        unread.setTextColor(0xFFFFFFFF.toInt())
        //divider.setBackgroundColor(0xFFDADADA);

        nickName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        unread.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)

        nickName.ellipsize = TextUtils.TruncateAt.END
        nickName.setSingleLine()

        contentContainer.orientation = LinearLayout.HORIZONTAL

        val avatarContainerParams = RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 72f), ScreenUtils.dip2px(mContext, 64f))
        avatarContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        avatarContainerParams.addRule(RelativeLayout.CENTER_VERTICAL)

        val avatarParams = RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 48f), ScreenUtils.dip2px(mContext, 48f))
        avatarParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        avatarContainer.addView(avatar, avatarParams)

        val nickNameParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        nickNameParams.setMargins(0, ScreenUtils.dip2px(mContext, 10f), 0, 0)
        nickNameParams.addRule(RelativeLayout.RIGHT_OF, avatarContainer.id)
        nickNameParams.addRule(RelativeLayout.LEFT_OF, time.id)

        val timeParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        timeParams.setMargins(ScreenUtils.dip2px(mContext, 12f), ScreenUtils.dip2px(mContext, 12f),
                ScreenUtils.dip2px(mContext, 12f), ScreenUtils.dip2px(mContext, 12f))
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        val contentContainerParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        contentContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        contentContainerParams.addRule(RelativeLayout.RIGHT_OF, avatarContainer.id)
        contentContainer.setPadding(0, 0, ScreenUtils.dip2px(mContext, 48f), ScreenUtils.dip2px(mContext, 12f))

        val msgStateParams = LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, 20f), ScreenUtils.dip2px(mContext, 20f))

        msgState.visibility = View.GONE

        content.ellipsize = TextUtils.TruncateAt.END
        content.setSingleLine()
        content.gravity = Gravity.CENTER_VERTICAL

        contentContainer.addView(msgState, msgStateParams)
        contentContainer.addView(content, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ScreenUtils.dip2px(mContext, 18f)))

        val unReadParams = RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 10f), ScreenUtils.dip2px(mContext, 10f))
        unReadParams.addRule(RelativeLayout.ALIGN_RIGHT, avatarContainer.id)
        unReadParams.addRule(RelativeLayout.ALIGN_TOP, avatarContainer.id)
        unReadParams.setMargins(0, ScreenUtils.dip2px(mContext, 5f), ScreenUtils.dip2px(mContext, 7f), 0)

        val dividerParams =
                RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
        dividerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        contentView.addView(avatarContainer, avatarContainerParams)
        contentView.addView(nickName, nickNameParams)
        contentView.addView(time, timeParams)
        contentView.addView(contentContainer, contentContainerParams)
        contentView.addView(unread, unReadParams)
        contentView.addView(divider, dividerParams)

        itemView.addView(contentView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))

        itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, 64f))
        return itemView
    }


    fun getItemViewBackground(context: Context): Drawable? {
        val attr = intArrayOf(android.R.attr.selectableItemBackground)
        val typedArray = context.obtainStyledAttributes(attr)
        val drawable = typedArray.getDrawable(0)
        typedArray.recycle()
        return drawable
    }

    fun getItemViewBackgroundSticky(context: Context): Drawable? {

        val drawable = StateListDrawable()
        drawable.addState(intArrayOf(), ColorDrawable(ColorUtils.getColorInt(AppSaveInfo.highLightColorInfo())))
        return drawable
    }

}
