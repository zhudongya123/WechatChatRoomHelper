package com.zdy.project.wechat_chatroom_helper.wechat.manager

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.annotation.ColorInt
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils

/**
 * Created by Mr.Zdy on 2018/3/2.
 */

object AvatarMaker {

    private const val AVATAR_BLUE = 0xFF12B7F6.toInt()
    private const val AVATAR_AMBER = 0xFFF5CB00.toInt()


    fun handleAvatarDrawable(context: Context, type: Int): BitmapDrawable {
        return when (type) {
            PageType.OFFICIAL -> handleAvatarDrawable(context, type, AVATAR_AMBER)
            PageType.CHAT_ROOMS -> handleAvatarDrawable(context, type, AVATAR_BLUE)
            else -> throw IllegalStateException("Error type = $type")
        }
    }

    fun handleAvatarDrawable(context: Context, type: Int, @ColorInt color: Int): BitmapDrawable {
        val fullSize = ScreenUtils.dip2px(context, 96f)
        val contentSize = fullSize / 2

        val paint = Paint()
        val drawableBitmap = Bitmap.createBitmap(fullSize, fullSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(drawableBitmap)

        when (type) {
            PageType.CHAT_ROOMS -> {
                canvas.run {
                    if (AppSaveInfo.isCircleAvatarInfo())
                        drawCircle(contentSize.toFloat(), contentSize.toFloat(), contentSize.toFloat(),
                                paint.apply {
                                    setColor(color)
                                    strokeWidth = 0f
                                    style = Paint.Style.FILL_AND_STROKE
                                })
                    else drawColor(color)
                }

                paint.color = 0xFFFFFFFF.toInt()
                paint.isAntiAlias = true

                val size = canvas.width.toFloat()

                val singleHeaderBitMap = singleHeaderBitMap()

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


            PageType.OFFICIAL ->
                makeAvatarBitmap(canvas, paint, color) {
                    val rawDrawable = Bitmap.createBitmap(contentSize, contentSize, Bitmap.Config.ARGB_8888)
                    val logoCanvas = Canvas(rawDrawable)

                    with(paint) {
                        strokeWidth = contentSize.toFloat() / 10f
                        style = Paint.Style.STROKE
                        setColor(-0x60d761)  //随机颜色
                    }

                    logoCanvas.drawCircle((contentSize / 4 + contentSize / 10).toFloat(), (contentSize / 2).toFloat(), (contentSize / 4).toFloat(), paint)
                    logoCanvas.drawCircle((contentSize - contentSize / 4 - contentSize / 10).toFloat(), (contentSize / 2).toFloat(), (contentSize / 4).toFloat(), paint)
                    rawDrawable
                }
            else -> {
            }

        }
        return BitmapDrawable(context.resources, drawableBitmap)
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
            if (AppSaveInfo.isCircleAvatarInfo())
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


    private fun singleHeaderBitMap(): Bitmap {

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
            color = 0xFFFFFFFF.toInt()
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


}
