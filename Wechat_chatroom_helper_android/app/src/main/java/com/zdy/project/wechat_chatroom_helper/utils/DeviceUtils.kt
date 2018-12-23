package com.zdy.project.wechat_chatroom_helper.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.telephony.TelephonyManager
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo

/**
 * Created by Mr.Zdy on 2017/9/24.
 */

object DeviceUtils {

    fun getIMELCode(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.deviceId
    }

    fun getWechatVersionCode(context: Context): Int {
        val list = context.packageManager.getInstalledPackages(0) as List<PackageInfo>

        var wechatVersionCode = -1

        try {
            for (packageInfo in list) {
                if (packageInfo.packageName == Constants.WECHAT_PACKAGE_NAME) {
                    wechatVersionCode = packageInfo.versionCode
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return wechatVersionCode
    }

    fun getWechatVersionName(context: Context): String {
        val list = context.packageManager.getInstalledPackages(0) as List<PackageInfo>

        var wechatVersionName = ""

        try {
            for (packageInfo in list) {
                if (packageInfo.packageName == Constants.WECHAT_PACKAGE_NAME) {
                    wechatVersionName = packageInfo.versionName
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return wechatVersionName
    }

    fun isWechatUpdate7(context: Context) = getWechatVersionName(context).startsWith("7")
}
