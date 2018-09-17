package manager

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.blankj.utilcode.util.SPUtils
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils

/**
 * Created by Mr.Zdy on 2017/11/3.
 */
class PermissionHelper(private var activity: Activity) {

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
            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //拒絕過權限授予，直接提醒跳轉到設置
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && SPUtils.getInstance().getBoolean("Permission_Flag", false)) {
                    permissionHelper.settingDialog.show()
                }
                //請求權限授予
                else {
                    permissionHelper.remindDialog.show()
                }

            }
            //有权限，初始化文件
            else {
                WechatJsonUtils.init(activity)
            }
            return permissionHelper
        }


        @JvmStatic
        fun check(activity: Activity): Int {
            //没有权限
            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //拒絕過權限授予
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && SPUtils.getInstance().getBoolean("Permission_Flag", false)) {
                    return DENY
                }
                //請求權限授予
                else {
                    return ASK
                }

            }
            //有权限，初始化文件
            else return ALLOW
        }

        @JvmStatic
        fun requestPermission(activity: Activity) {
            SPUtils.getInstance().put("Permission_Flag", true)
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Constants.WRITE_EXTERNAL_STORAGE_RESULT_CODE)
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
    }

    private var remindDialog: AlertDialog = AlertDialog.Builder(activity)
            .setMessage("因为Android 7.0 的一些变化，我们需要读写本地存储的权限后才能正常工作。\n" +
                    "同时也请不要删除sdcard/WechatChatroomHelper下的文件，以免影响使用。")
            .setTitle("授予权限消息")
            .setPositiveButton("授权") { dialog, _ ->
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        Constants.WRITE_EXTERNAL_STORAGE_RESULT_CODE)
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

    private var settingDialog: AlertDialog = AlertDialog.Builder(activity)
            .setMessage("我们必须获得读写文件的权限才能正常工作，请前往设置项授予相应权限。")
            .setTitle("授予权限消息")
            .setPositiveButton("去设置") { dialog, _ ->
                gotoPermissionPage(activity)
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()


    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (Constants.WRITE_EXTERNAL_STORAGE_RESULT_CODE == requestCode) {
            //回调中获得了权限
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                WechatJsonUtils.init(activity)
            }
            //刚拒绝了权限
            else {
                settingDialog.show()
            }
        }
    }


}