package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.provider.SyncStateContract
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
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.helper.utils.ColorUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.wechat.manager.DrawableMaker

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
    val id_unread_mark = 6
    @IdRes
    val id_divider = 8
    @IdRes
    val id_mute = 9
    @IdRes
    val id_unread_count = 10


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
        val unreadMark = View(mContext)
        val unreadCount = TextView(mContext)
        val divider = View(mContext)
        val mute = ImageView(mContext)

        avatar.id = id_avatar
        avatarContainer.id = id_avatar_container
        nickName.id = id_nickname
        time.id = id_time
        msgState.id = id_msg_state
        content.id = id_content
        unreadMark.id = id_unread_mark
        divider.id = id_divider
        mute.id = id_mute
        unreadCount.id = id_unread_count


        nickName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        unreadCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)

        nickName.ellipsize = TextUtils.TruncateAt.END
        nickName.setSingleLine()

        unreadCount.gravity = Gravity.CENTER
        unreadCount.setTextColor(Color.WHITE)
        unreadCount.paint.isFakeBoldText = true

        contentContainer.orientation = LinearLayout.HORIZONTAL

        val avatarContainerParams = RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 72f), ScreenUtils.dip2px(mContext, 64f))
        avatarContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        avatarContainerParams.addRule(RelativeLayout.CENTER_VERTICAL)

        val avatarParams = RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 48f), ScreenUtils.dip2px(mContext, 48f))
        avatarParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        avatarContainer.addView(avatar, avatarParams)

        val nickNameParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        nickNameParams.setMargins(0, ScreenUtils.dip2px(mContext, Constants.defaultValue.CONVERSATION_ITEM_NICKNAME_PADDING_TOP), 0, 0)
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
        contentContainer.setPadding(0, 0, ScreenUtils.dip2px(mContext, 48f), ScreenUtils.dip2px(mContext, Constants.defaultValue.CONVERSATION_ITEM_CONTENT_PADDING_BOTTOM))

        val msgStateParams = LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, 20f), ScreenUtils.dip2px(mContext, 20f))

        msgState.visibility = View.GONE

        content.ellipsize = TextUtils.TruncateAt.END
        content.setSingleLine()
        content.gravity = Gravity.CENTER_VERTICAL

        contentContainer.addView(msgState, msgStateParams)
        contentContainer.addView(content, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ScreenUtils.dip2px(mContext, 18f)))

        val unReadMarkParams = RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 10f), ScreenUtils.dip2px(mContext, 10f))
        unReadMarkParams.addRule(RelativeLayout.ALIGN_RIGHT, avatarContainer.id)
        unReadMarkParams.addRule(RelativeLayout.ALIGN_TOP, avatarContainer.id)
        unReadMarkParams.setMargins(0, ScreenUtils.dip2px(mContext, 5f), ScreenUtils.dip2px(mContext, 7f), 0)

        val unReadCountParams = RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 18f), ScreenUtils.dip2px(mContext, 18f))
        unReadCountParams.addRule(RelativeLayout.ALIGN_RIGHT, unreadMark.id)
        unReadCountParams.addRule(RelativeLayout.ALIGN_TOP, unreadMark.id)

        val dividerParams =
                RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
        dividerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        if (Constants.defaultValue.isWechatUpdate7) {
            dividerParams.addRule(RelativeLayout.RIGHT_OF, avatarContainer.id)
        }

        mute.setImageBitmap(DrawableMaker.getMuteBitMap())

        val muteParams = RelativeLayout.LayoutParams(ScreenUtils.dip2px(mContext, 24f), ScreenUtils.dip2px(mContext, 24f))
        muteParams.addRule(RelativeLayout.ALIGN_BOTTOM, avatarContainer.id)
        muteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        muteParams.setMargins(0, 0, ScreenUtils.dip2px(mContext, 8f), ScreenUtils.dip2px(mContext, 8f))

        contentView.addView(avatarContainer, avatarContainerParams)
        contentView.addView(nickName, nickNameParams)
        contentView.addView(time, timeParams)
        contentView.addView(contentContainer, contentContainerParams)
        contentView.addView(unreadMark, unReadMarkParams)
        contentView.addView(unreadCount, unReadCountParams)
        contentView.addView(divider, dividerParams)
        contentView.addView(mute, muteParams)

        itemView.addView(contentView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))

        itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, Constants.defaultValue.CONVERSATION_ITEM_HEIGHT))
        return itemView
    }


    fun getItemViewBackground(context: Context): Drawable? {
        val attr = intArrayOf(android.R.attr.selectableItemBackground)
        val typedArray = context.obtainStyledAttributes(attr)
        val drawable = typedArray.getDrawable(0)
        typedArray.recycle()
        return drawable
    }

    fun getItemViewBackgroundSticky(): Drawable? {
        val drawable = StateListDrawable()
        drawable.addState(intArrayOf(), ColorDrawable(ColorUtils.getColorInt(AppSaveInfo.highLightColorInfo())))
        return drawable
    }

}
