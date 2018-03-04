package com.zdy.project.wechat_chatroom_helper.ui.helper.avatar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

import utils.AppSaveInfoUtils

/**
 * Created by Mr.Zdy on 2018/3/2.
 */

object AvatarMaker {

    val AVATAR_BLUE = 0xFF12B7F6.toInt()
    private val AVATAR_AMBER = 0xFFF5CB00.toInt()

    //自造群消息助手头像
    fun makeChatRoomBitmap(canvas: Canvas, paint: Paint, size: Int, originDrawable: Bitmap) {
        val drawable = Bitmap.createScaledBitmap(originDrawable, size / 2, size / 2, false)
                .copy(Bitmap.Config.ARGB_8888, false)

        val whiteMask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        whiteMask.eraseColor(Color.WHITE)

        //生成图
        val raw = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val bitmapCanvas = Canvas(raw)

        //绘制logo
        bitmapCanvas.drawBitmap(drawable, (size / 4).toFloat(), (size / 4).toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        //给logo染色
        bitmapCanvas.drawBitmap(whiteMask, 0f, 0f, paint)

        paint.xfermode = null

        if (AppSaveInfoUtils.isCircleAvatarInfo()) {
            paint.color = AVATAR_BLUE
            canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), paint)
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
