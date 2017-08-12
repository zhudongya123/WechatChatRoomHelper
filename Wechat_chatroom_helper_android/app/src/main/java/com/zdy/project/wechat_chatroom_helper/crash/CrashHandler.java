package com.zdy.project.wechat_chatroom_helper.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.zdy.project.wechat_chatroom_helper.network.ApiManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mr.Zdy on 2017/8/9.
 */

public class CrashHandler {

    public static final String TAG = "CrashHandler";
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    private static Map<String, String> collectDeviceInfo(Context ctx) {
        Map<String, String> infos = new HashMap<String, String>();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
        return infos;
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    public static void saveCrashInfo2File(Throwable ex, Context context) {
        try {
            if (context == null) return;

            Map<String, String> infos = collectDeviceInfo(context);
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key + "=" + value + "\n");
            }

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            sb.append(result);
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";

            String path = context.getFilesDir().getAbsolutePath();

            final File file = new File(path + fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ApiManager.getINSTANCE().sendRequestForCrashReport(file);
                }
            }).start();
        } catch (Throwable e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
    }
}