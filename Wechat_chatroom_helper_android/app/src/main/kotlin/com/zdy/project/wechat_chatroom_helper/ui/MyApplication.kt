package ui

import android.app.Application
import com.tencent.bugly.Bugly

/**
 * Created by Mr.Zdy on 2017/10/19.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Bugly.init(applicationContext, "ed7bb0e103", false)

        instance = this

    }

    companion object {
        private var instance: MyApplication? = null

        fun get(): MyApplication {
            return instance!!
        }

    }
}
