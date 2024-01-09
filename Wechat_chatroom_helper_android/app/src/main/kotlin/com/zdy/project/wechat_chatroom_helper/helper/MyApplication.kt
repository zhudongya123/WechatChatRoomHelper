package ui

import android.app.Application
import android.content.pm.PackageManager
import com.zdy.project.wechat_chatroom_helper.helper.myAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

/**
 * Created by Mr.Zdy on 2017/10/19.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this


        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(myAppModules)
        }
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
