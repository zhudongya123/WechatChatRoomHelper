package com.zdy.project.wechat_chatroom_helper.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Mr.Zdy on 2017/9/24.
 */

public class DeviceUtils {

    public static String getIMELCode(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

}
