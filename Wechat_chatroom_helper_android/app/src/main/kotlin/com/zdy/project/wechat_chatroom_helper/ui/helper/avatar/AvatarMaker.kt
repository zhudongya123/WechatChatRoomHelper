package com.zdy.project.wechat_chatroom_helper.ui.helper.avatar

import android.content.Context
import android.graphics.*
import com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Chatroom_Avatar

import utils.AppSaveInfoUtils

/**
 * Created by Mr.Zdy on 2018/3/2.
 */

object AvatarMaker {

    private val AVATAR_BLUE = 0xFF12B7F6.toInt()
    private val AVATAR_AMBER = 0xFFF5CB00.toInt()

    //自造群消息助手头像
    fun makeChatRoomBitmap(context: Context, canvas: Canvas, paint: Paint) {

        val iconSize = canvas.width
        val contentSize = canvas.width / 2

        //創建内部内容區域，尺寸為icon尺寸的一半
        val drawable = with(context) {
            val identifier = context.resources.getIdentifier(Drawable_String_Chatroom_Avatar, "drawable", context.packageName)
            val rawDrawable = BitmapFactory.decodeResource(context.resources, identifier)
            return@with Bitmap.createScaledBitmap(rawDrawable, contentSize, contentSize, false)
                    .copy(Bitmap.Config.ARGB_8888, false)
        }


        val whiteMask = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        whiteMask.eraseColor(Color.WHITE)

        //生成图
        val raw = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)

        val bitmapCanvas = Canvas(raw)

        //绘制logo
        bitmapCanvas.drawBitmap(drawable, (contentSize / 2).toFloat(), (contentSize / 2).toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        //给logo染色
        bitmapCanvas.drawBitmap(whiteMask, 0f, 0f, paint)

        paint.xfermode = null

        if (AppSaveInfoUtils.isCircleAvatarInfo()) {
            paint.color = AVATAR_BLUE
            canvas.drawCircle(contentSize.toFloat(), contentSize.toFloat(), contentSize.toFloat(), paint)
        } else {
            canvas.drawColor(AVATAR_BLUE)
        }

        canvas.drawBitmap(raw, 0f, 0f, paint)

    }

    //自造公众号助手头像
    fun makeOfficialBitmap(canvas: Canvas, paint: Paint, size: Int) {
        val whiteMask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        whiteMask.eraseColor(Color.WHITE)

        val logoDrawable = Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888)
        val logoCanvas = Canvas(logoDrawable)
        paint.strokeWidth = (size / 20).toFloat()
        paint.style = Paint.Style.STROKE
        paint.color = -0x60d761//随机颜色
        logoCanvas.drawCircle((size / 8 + size / 20).toFloat(), (size / 4).toFloat(), (size / 8).toFloat(), paint)
        logoCanvas.drawCircle((size / 2 - size / 8 - size / 20).toFloat(), (size / 4).toFloat(), (size / 8).toFloat(), paint)

        //生成图
        val raw = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(raw)

        //绘制logo
        bitmapCanvas.drawBitmap(logoDrawable, (size / 4).toFloat(), (size / 4).toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        //给logo染色
        bitmapCanvas.drawBitmap(whiteMask, 0f, 0f, paint)

        paint.xfermode = null

        if (AppSaveInfoUtils.isCircleAvatarInfo()) {
            paint.color = AVATAR_AMBER
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL_AND_STROKE
            canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), paint)
        } else {
            canvas.drawColor(AVATAR_AMBER)
        }

        canvas.drawBitmap(raw, 0f, 0f, paint)

    }

}
