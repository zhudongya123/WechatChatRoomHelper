package com.zdy.project.wechat_chatroom_helper.wechat.manager

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.zdy.project.wechat_chatroom_helper.PageType

/**
 * Created by Mr.Zdy on 2018/3/2.
 */

object AvatarMaker {

    private val AVATAR_BLUE = 0xFF12B7F6.toInt()
    private val AVATAR_AMBER = 0xFFF5CB00.toInt()


    fun handleAvatarDrawable(imageView: ImageView, type: Int): BitmapDrawable {
        val contentSize = imageView.measuredHeight / 2

        val paint = Paint()
        val drawableBitmap = Bitmap.createBitmap(imageView.measuredHeight, imageView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(drawableBitmap)

        when (type) {
            PageType.CHAT_ROOMS ->
                makeAvatarBitmap(canvas, paint, AVATAR_BLUE,
                        {
//                            val identifier = context.resources.getIdentifier(Drawable_String_Chatroom_Avatar, "drawable", context.packageName)
//                            val rawDrawable = BitmapFactory.decodeResource(context.resources, identifier)
//                            Bitmap.createScaledBitmap(rawDrawable, contentSize, contentSize, false)
//                                    .copy(Bitmap.Config.ARGB_8888, false)
                            Bitmap.createBitmap(contentSize, contentSize, Bitmap.Config.ARGB_8888)
                        })


            PageType.OFFICIAL ->
                makeAvatarBitmap(canvas, paint, AVATAR_AMBER,
                        {
                            val rawDrawable = Bitmap.createBitmap(contentSize, contentSize, Bitmap.Config.ARGB_8888)
                            val logoCanvas = Canvas(rawDrawable)

                            with(paint) {
                                strokeWidth = contentSize.toFloat() / 10f
                                style = Paint.Style.STROKE
                                color = -0x60d761//随机颜色
                            }

                            logoCanvas.drawCircle((contentSize / 4 + contentSize / 10).toFloat(), (contentSize / 2).toFloat(), (contentSize / 4).toFloat(), paint)
                            logoCanvas.drawCircle((contentSize - contentSize / 4 - contentSize / 10).toFloat(), (contentSize / 2).toFloat(), (contentSize / 4).toFloat(), paint)
                            rawDrawable
                        })
            else -> {
            }
        }
        return BitmapDrawable(imageView.context.resources, drawableBitmap)
    }


    /**
     * 图片分为背景区域和内容区域
     *
     * 内容区域染成白色，背景區域為純色
     */
    private fun makeAvatarBitmap(canvas: Canvas, paint: Paint, backgroundColor: Int, block: () -> Bitmap) {

        val iconSize = canvas.width
        val contentSize = canvas.width / 2

        //創建内部内容區域，尺寸為icon尺寸的一半
        val contentDrawable = block()

        //内容區域染色布
        val whiteMask = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }

        //生成最终图
        val result = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val drawResult = Canvas(result)

        //绘制logo，区域是icon尺寸的一半
        drawResult.drawBitmap(contentDrawable, (contentSize / 2).toFloat(), (contentSize / 2).toFloat(), paint)

        //给logo染色
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        drawResult.drawBitmap(whiteMask, 0f, 0f, paint)
        paint.xfermode = null

        //填充背景
        canvas.run {
            if (ConfigInfo.isCircleAvatar)
                drawCircle(contentSize.toFloat(), contentSize.toFloat(), contentSize.toFloat(),
                        paint.apply {
                            color = backgroundColor
                            strokeWidth = 0f
                            style = Paint.Style.FILL_AND_STROKE
                        })
            else drawColor(backgroundColor)

            drawBitmap(result, 0f, 0f, paint)
        }

    }


}
