package com.zdy.project.wechat_chatroom_helper.helper.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SPUtils
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.helper.ui.main.MainActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.PermissionResult

/**
 * Created by Mr.Zdy on 2017/11/3.
 */
class PermissionHelper(private var context: Context) {

    companion object {

        @JvmStatic
        val ALLOW = 0

        @JvmStatic
        val DENY = 2

        @JvmStatic
        val ASK = 1

        @JvmStatic
        fun checkFile(activity: Activity): PermissionHelper {

            val permissionHelper = PermissionHelper(activity)

            //没有权限
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //拒絕過權限授予，直接提醒跳轉到設置
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    && SPUtils.getInstance().getBoolean("Permission_Flag", false)
                ) {
                }
                //請求權限授予
                else {
                }
            }
            //有权限，初始化文件
            else {
                WechatJsonUtils.init(activity)
            }
            return permissionHelper
        }

        @JvmStatic
        fun requestPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            } else {
                SPUtils.getInstance().put("Permission_Flag", true)
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    MainActivity.WRITE_EXTERNAL_STORAGE_RESULT_CODE
                )
            }
        }

        @JvmStatic
        fun gotoPermissionPage(activity: Activity) {
            val intent = Intent().also { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
        }


        @SuppressLint("NewApi")
        @JvmStatic
        fun check(activity: Activity): PermissionResult {
            //没有权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return PermissionResult.Pass
            } else {
                val isHavePermission = ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                return if (isHavePermission) {
                    //拒絕過權限授予
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        && SPUtils.getInstance().getBoolean("Permission_Flag", false)
                    ) {
                        PermissionResult.Deny
                    }
                    //請求權限授予
                    else {
                        PermissionResult.Ask
                    }
                } else PermissionResult.Pass
            }
        }
    }

}