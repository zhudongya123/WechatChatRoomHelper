package manager

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.zdy.project.wechat_chatroom_helper.Constants
import utils.FileUtils

/**
 * Created by Mr.Zdy on 2017/11/3.
 */
class PermissionHelper(private var activity: Activity) {

    companion object {
        fun check(activity: Activity): PermissionHelper? {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                val permissionHelper = PermissionHelper(activity)

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    permissionHelper.settingDialog.show()
                } else {

                    permissionHelper.remindDialog.show()
                }

                return permissionHelper
            } else {
                FileUtils.init()
            }
            return null
        }
    }


    private var remindDialog: AlertDialog = AlertDialog.Builder(activity)
            .setMessage("因为Android 7.0 的一些变化，原来的存储方式已经不适用于所有的Android版本，" +
                    "故现在本地的配置文件已经移动到sdcard根目录，请授与我们读写本地存储的权限，" +
                    "同时也请不要删除sdcard/WechatChatroomHelper下的文件，以免影响使用。")
            .setTitle("消息")
            .setPositiveButton("授权", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {

                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            Constants.WRITE_EXTERNAL_STORAGE_RESULT_CODE)
                    dialog.dismiss()
                }
            })
            .setCancelable(false)
            .create()

    private var settingDialog: AlertDialog = AlertDialog.Builder(activity)
            .setMessage("您已经拒绝了我们的权限申请，前往设置项打开读写文件的权限")
            .setTitle("消息")
            .setPositiveButton("去设置") { dialog, _ ->
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                intent.data = Uri.fromParts("package", activity.packageName, null)
                activity.startActivity(intent)

                dialog.dismiss()
            }
            .setCancelable(false)
            .create()


    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.WRITE_EXTERNAL_STORAGE_RESULT_CODE -> {
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FileUtils.init()
                } else {
                    settingDialog.show()
                }
            }
        }
    }


}