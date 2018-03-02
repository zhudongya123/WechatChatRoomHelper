package com.zdy.project.wechat_chatroom_helper.ui.helper.avatar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import utils.AppSaveInfoUtils;

/**
 * Created by Mr.Zdy on 2018/3/2.
 */

public class AvatarMaker {


    //自造群消息助手头像
    public static void makeChatRoomBitmap(Canvas canvas, Paint paint, int size, Bitmap drawable) {
        Bitmap whiteMask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        whiteMask.eraseColor(Color.WHITE);

        drawable = Bitmap.createScaledBitmap(drawable, size / 2, size / 2, false).copy(Bitmap.Config.ARGB_8888, false);

        //生成图
        Bitmap raw = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas bitmapCanvas = new Canvas(raw);

        //绘制logo
        bitmapCanvas.drawBitmap(drawable, size / 4, size / 4, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        //给logo染色
        bitmapCanvas.drawBitmap(whiteMask, 0, 0, paint);

        paint.setXfermode(null);

        if (AppSaveInfoUtils.INSTANCE.isCircleAvatarInfo()) {
            paint.setColor(0xFF12B7F6);
            canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        } else {
            canvas.drawColor(0xFF12B7F6);
        }

        canvas.drawBitmap(raw, 0, 0, paint);

    }

    //自造公众号助手头像
    public static void makeOfficialBitmap(Canvas canvas, Paint paint, int size) {
        Bitmap whiteMask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        whiteMask.eraseColor(Color.WHITE);

        Bitmap drawable = Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888);

        Canvas logoCanvas = new Canvas(drawable);

        paint.setStrokeWidth(size / 20);

        paint.setStyle(Paint.Style.STROKE);

        paint.setColor(0xFF9F289F);

        logoCanvas.drawCircle(size / 8 + size / 20, size / 4, size / 8, paint);
        logoCanvas.drawCircle(size / 2 - size / 8 - size / 20, size / 4, size / 8, paint);

        //生成图
        Bitmap raw = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas bitmapCanvas = new Canvas(raw);

        //绘制logo
        bitmapCanvas.drawBitmap(drawable, size / 4, size / 4, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        //给logo染色
        bitmapCanvas.drawBitmap(whiteMask, 0, 0, paint);

        paint.setXfermode(null);

        if (AppSaveInfoUtils.INSTANCE.isCircleAvatarInfo()) {
            paint.setColor(0xFFF5CB00);
            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        } else {
            canvas.drawColor(0xFFF5CB00);
        }

        canvas.drawBitmap(raw, 0, 0, paint);

    }

}
