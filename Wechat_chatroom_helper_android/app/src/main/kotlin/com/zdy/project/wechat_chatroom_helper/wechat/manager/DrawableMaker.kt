package com.zdy.project.wechat_chatroom_helper.wechat.manager

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import androidx.annotation.ColorInt
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo

/**
 * Created by Mr.Zdy on 2018/3/2.
 */

object DrawableMaker {

    private const val AVATAR_BLUE = 0xFF12B7F6.toInt()
    private const val AVATAR_AMBER = 0xFFF5CB00.toInt()

    private lateinit var chatroomDrawable: BitmapDrawable
    private lateinit var officialDrawable: BitmapDrawable

    private const val DrawableSize = 512f
    private const val ContentSize = 256f

    fun handleAvatarDrawable(context: Context, type: Int): BitmapDrawable {
        return when (type) {
            PageType.OFFICIAL -> {
                if (!this::officialDrawable.isInitialized) {
                    officialDrawable = handleAvatarDrawable(context, type, AVATAR_AMBER, Color.WHITE)
                }
                officialDrawable
            }
            PageType.CHAT_ROOMS -> {
                if (!this::chatroomDrawable.isInitialized) {
                    chatroomDrawable = handleAvatarDrawable(context, type, AVATAR_BLUE, Color.WHITE)
                }
                chatroomDrawable
            }
            else -> throw IllegalStateException("Error type = $type")
        }
    }

    fun handleAvatarDrawable(context: Context, type: Int, @ColorInt backgroundColor: Int, @ColorInt foregroundColor: Int): BitmapDrawable {
        val drawableSize = DrawableSize
        val contentSize = ContentSize

        val paint = Paint()
        val drawableBitmap = Bitmap.createBitmap(drawableSize.toInt(), drawableSize.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(drawableBitmap)


        paint.apply {
            color = backgroundColor
            strokeWidth = 0f
            style = Paint.Style.FILL_AND_STROKE
            paint.isAntiAlias = true
        }
        if (AppSaveInfo.isCircleAvatarInfo()) {
            canvas.drawCircle(contentSize, contentSize, contentSize, paint)
        } else {
            if (Constants.defaultValue.isWechatUpdate7) {
                val radius = drawableSize / 12f
                canvas.drawRoundRect(RectF(0f, 0f, drawableSize, drawableSize), radius, radius, paint)
            } else {
                canvas.drawColor(backgroundColor)
            }
        }

        when (type) {
            PageType.CHAT_ROOMS -> {

                paint.color = foregroundColor

                val size = canvas.width.toFloat()

                val singleHeaderBitMap = singleHeaderBitMap(foregroundColor)

                val smallHead = Bitmap.createScaledBitmap(singleHeaderBitMap, (size * 0.3f).toInt(), (size * 0.3f).toInt(), false)
                val bigHead = Bitmap.createScaledBitmap(singleHeaderBitMap, (size * 0.4f).toInt(), (size * 0.4f).toInt(), false)
                val maskHead = Bitmap.createScaledBitmap(singleHeaderBitMap, (size * 0.5f).toInt(), (size * 0.5f).toInt(), false)

                paint.xfermode = null

                //隔离图层
                val saveLayer = canvas.saveLayer(RectF(0f, 0f, size, size), paint, Canvas.ALL_SAVE_FLAG)

                //先画小的
                canvas.drawBitmap(smallHead, null, RectF(size * 0.16f, size * 0.33f, size * 0.46f, size * 0.63f), paint)
                canvas.drawBitmap(smallHead, null, RectF(size * 0.53f, size * 0.33f, size * 0.83f, size * 0.63f), paint)

                //扣除中间的部分，然后绘制到底层画布上
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

                canvas.drawBitmap(maskHead, null, RectF(size * 0.244f, size * 0.21f, size * 0.744f, size * 0.71f), paint)
                canvas.restoreToCount(saveLayer)

                //绘制中间的完整的
                paint.xfermode = null
                canvas.drawBitmap(bigHead, null, RectF(size * 0.295f, size * 0.29f, size * 0.695f, size * 0.69f), paint)

            }

            PageType.OFFICIAL -> {
                val circleBitmap = Bitmap.createBitmap(contentSize.toInt(), contentSize.toInt(), Bitmap.Config.ARGB_8888)
                val logoCanvas = Canvas(circleBitmap)

                paint.apply {
                    strokeWidth = contentSize / 10f
                    style = Paint.Style.STROKE
                    color = foregroundColor  //随机颜色
                }

                logoCanvas.drawCircle((contentSize / 4 + contentSize / 10), (contentSize / 2), (contentSize / 4), paint)
                logoCanvas.drawCircle((contentSize - contentSize / 4 - contentSize / 10), (contentSize / 2), (contentSize / 4), paint)

                canvas.drawBitmap(circleBitmap, 0.25f * drawableSize, 0.25f * drawableSize, paint)
            }

            else -> {
            }

        }
        return BitmapDrawable(context.resources, drawableBitmap)
    }


    fun getMuteBitMap(): Bitmap {

        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            color = 0xFFD9D9D9.toInt()
            style = Paint.Style.FILL_AND_STROKE
        }

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val path1 = Path()

        path1.moveTo(22f, 24f)
        path1.quadTo(19.0f, 17.0f, 27.6f, 19f)
        path1.lineTo(38.6f, 30.1f)
        path1.quadTo(41.6f, 26.7f, 47.8f, 27.1f)
        path1.lineTo(47.8f, 24.1f)
        path1.quadTo(50.8f, 17.7f, 55.3f, 24.1f)
        path1.lineTo(55.3f, 27.3f)
        path1.quadTo(70f, 33.3f, 70f, 44.1f)
        path1.lineTo(70f, 61f)
        path1.lineTo(80.8f, 72.7f)
        path1.quadTo(83f, 79f, 76.6f, 77.3f)
        path1.close()

        canvas.drawPath(path1, paint)

        val path2 = Path()

        path2.moveTo(29.3f, 72f)
        path2.quadTo(24.4f, 68f, 32f, 64f)
        path2.lineTo(32.1f, 40.6f)
        path2.lineTo(63.7f, 72f)
        path2.close()

        canvas.drawPath(path2, paint)

        val path3 = Path()

        path3.moveTo(44.4f, 76.8f)
        path3.quadTo(51.3f, 88.2f, 58f, 76.8f)
        path3.lineTo(58f, 76f)
        path3.lineTo(44.4f, 76f)
        path3.close()

        canvas.drawPath(path3, paint)
        return bitmap
    }

    private fun singleHeaderBitMap(foregroundColor: Int): Bitmap {

        val leftBottomPoint = Pair(0.067f, 1f)//左·底部基点
        val leftBottomAnchor = Pair(-0.01f, 1.01f)//左·底部锚点

        val leftShoulderPointStart = Pair(0f, 0.92f)//左·肩开始点（和底部基点链接）
        val leftShoulderPointEnd = Pair(0.149f, 0.755f)//左·肩结束点
        val leftShoulderAnchor = Pair(0f, 0.82f)//左·肩锚点

        val leftNeckPointEnd = Pair(0.355f, 0.485f)//左·颈部结束点（和肩结束点链接）
        val leftNeckAnchor1 = Pair(0.272f, 0.696f)//左·颈部锚点1
        val leftNeckAnchor2 = Pair(0.469f, 0.628f)//左·颈部锚点2

        val leftHeadPoint = Pair(0.35f, 0.06f)//左·头部基点
        val leftHeadAnchor = Pair(0.18f, 0.222f)//左·头部锚点
        val centerHeadAnchor = Pair(0.5f, -0.05f)//中心·头顶部锚点
        val rightHeadAnchor = Pair(0.82f, 0.222f)//右·头部基点（与左头部基点链接）
        val rightHeadPoint = Pair(0.65f, 0.06f)

        val rightShoulderPointStart = Pair(1f, 0.92f)
        val rightShoulderPointEnd = Pair(0.861f, 0.76f)
        val rightShoulderAnchor = Pair(1f, 0.82f)

        val rightNeckPointEnd = Pair(0.645f, 0.485f)
        val rightNeckAnchor1 = Pair(0.728f, 0.696f)
        val rightNeckAnchor2 = Pair(0.531f, 0.628f)

        val rightBottomPoint = Pair(0.933f, 1f)
        val rightBottomAnchor = Pair(1.01f, 1.01f)


        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            color = foregroundColor
            style = Paint.Style.FILL_AND_STROKE
        }

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val path = Path()

        path.moveTo(leftBottomPoint.first * width, leftBottomPoint.second * height)
        path.quadTo(leftBottomAnchor.first * width, leftBottomAnchor.second * height,
                leftShoulderPointStart.first * width, leftShoulderPointStart.second * height)
        path.moveTo(leftShoulderPointStart.first * width, leftShoulderPointStart.second * height)
        path.quadTo(leftShoulderAnchor.first * width, leftShoulderAnchor.second * height,
                leftShoulderPointEnd.first * width, leftShoulderPointEnd.second * height)
        path.cubicTo(leftNeckAnchor1.first * width, leftNeckAnchor1.second * height,
                leftNeckAnchor2.first * width, leftNeckAnchor2.second * height,
                leftNeckPointEnd.first * width, leftNeckPointEnd.second * height)
        path.quadTo(leftHeadAnchor.first * width, leftHeadAnchor.second * height,
                leftHeadPoint.first * width, leftHeadPoint.second * height)
        path.quadTo(centerHeadAnchor.first * width, centerHeadAnchor.second * height,
                rightHeadPoint.first * width, rightHeadPoint.second * height)
        path.quadTo(rightHeadAnchor.first * width, rightHeadAnchor.second * height,
                rightNeckPointEnd.first * width, rightNeckPointEnd.second * height)

        path.cubicTo(
                rightNeckAnchor2.first * width, rightNeckAnchor2.second * height,
                rightNeckAnchor1.first * width, rightNeckAnchor1.second * height,
                rightShoulderPointEnd.first * width, rightShoulderPointEnd.second * height)
        path.quadTo(rightShoulderAnchor.first * width, rightShoulderAnchor.second * height,
                rightShoulderPointStart.first * width, rightShoulderPointStart.second * height)
        path.quadTo(rightBottomAnchor.first * width, rightBottomAnchor.second * height,
                rightBottomPoint.first * width, rightBottomPoint.second * height)

        path.lineTo(leftBottomPoint.first * width, leftBottomPoint.second * height)

        canvas.drawPath(path, paint)

        return bitmap
    }

    fun getArrowBitMapForBack(@ColorInt color: Int): Bitmap {

        val height = 240
        val width = 240

        val paint = Paint()
                .also {
                    it.color = color
                    it.style = Paint.Style.FILL_AND_STROKE
                }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.concat(Matrix().apply {
            setScale(0.5f, 0.5f)
            postTranslate(width * 0.25f, height * 0.25f)
        })

        val path = Path()

        path.moveTo(200f, 110f)
        path.lineTo(78.3f, 110f)
        path.rLineTo(55.9f, -55.9f)
        path.lineTo(120f, 40f)
        path.rLineTo(-80f, 80f)
        path.rLineTo(80f, 80f)
        path.rLineTo(14.1f, -14.1f)
        path.lineTo(78.3f, 130f)
        path.lineTo(200f, 130f)
        path.lineTo(200f, 110f)
        path.close()


        canvas.drawPath(path, paint)

        return bitmap
    }

    fun getRedCircleDrawable() = ShapeDrawable(object : Shape() {
        override fun draw(canvas: Canvas, paint: Paint) {
            val size = (canvas.width / 2).toFloat()

            paint.isAntiAlias = true
            paint.color = 0xFFFF3D3D.toInt()
            paint.style = Paint.Style.FILL_AND_STROKE
            canvas.drawCircle(size, size, size, paint)
        }
    })

}
