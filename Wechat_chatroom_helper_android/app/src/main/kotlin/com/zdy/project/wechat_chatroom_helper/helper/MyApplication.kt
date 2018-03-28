package ui

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.tencent.bugly.Bugly
import com.zdy.project.wechat_chatroom_helper.Constants

/**
 * Created by Mr.Zdy on 2017/10/19.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Bugly.init(applicationContext, "ed7bb0e103", false)

        instance = this

    }

     fun getWechatVersionCode(): Int {
        val list = packageManager.getInstalledPackages(0) as List<PackageInfo>

        var wechatVersionCode = -1

        try {
            for (packageInfo in list) {
                if (packageInfo.packageName == Constants.WECHAT_PACKAGE_NAME) {
                    wechatVersionCode = if (packageInfo.versionName == "6.5.14") 1101
                    else packageInfo.versionCode
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return wechatVersionCode
    }

    fun getHelperVersionCode(): Int {
        val packageManager = packageManager as PackageManager
        var versionCode = 0

        try {
            versionCode = packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return versionCode
    }


    companion object {
        private var instance: MyApplication? = null

        fun get(): MyApplication {
            return instance!!
        }

    }
}
