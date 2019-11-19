package ui

import android.app.Application
import android.content.pm.PackageManager

/**
 * Created by Mr.Zdy on 2017/10/19.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
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
