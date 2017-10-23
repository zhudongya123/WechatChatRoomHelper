package com.zdy.project.wechat_chatroom_helper.kt.ui

import android.app.Application
import com.tencent.bugly.Bugly
import com.zdy.project.wechat_chatroom_helper.ui.MyApplication

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
        private var instance: com.zdy.project.wechat_chatroom_helper.kt.ui.MyApplication? = null

        fun get(): com.zdy.project.wechat_chatroom_helper.kt.ui.MyApplication {
            return instance!!
        }

    }
}
