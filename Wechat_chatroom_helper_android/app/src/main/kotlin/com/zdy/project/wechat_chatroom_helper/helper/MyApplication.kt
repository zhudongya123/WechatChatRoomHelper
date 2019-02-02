package ui

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.tencent.bugly.crashreport.CrashReport
import com.zdy.project.wechat_chatroom_helper.Constants

/**
 * Created by Mr.Zdy on 2017/10/19.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        CrashReport.initCrashReport(applicationContext, "ed7bb0e103", false)

        instance = this

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

        @JvmStatic
        fun get(): MyApplication {
            return instance!!
        }
    }
}
