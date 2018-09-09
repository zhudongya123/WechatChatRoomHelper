package com.zdy.project.wechat_chatroom_helper.helper.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.zdy.project.wechat_chatroom_helper.helper.utils.WechatJsonUtils

open class BaseActivity : AppCompatActivity() {


    protected lateinit var thisActivity: BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("W.C.R.H.", "BaseActivity, onCreate")
        thisActivity = this
    }

    override fun onStart() {
        super.onStart()
        Log.v("W.C.R.H.", "BaseActivity, onStart")
    }


    override fun onResume() {
        super.onResume()
        Log.v("W.C.R.H.", "BaseActivity, onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.v("W.C.R.H.", "BaseActivity, onPause")

    }

    override fun onRestart() {
        super.onRestart()
        Log.v("W.C.R.H.", "BaseActivity, onRestart")
    }

    override fun onStop() {
        super.onStop()
        Log.v("W.C.R.H.", "BaseActivity, onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("W.C.R.H.", "BaseActivity, onDestroy")


    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.v("W.C.R.H.", "BaseActivity, onAttachedToWindow")

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.v("W.C.R.H.", "BaseActivity, onWindowFocusChanged, hasFocus = $hasFocus")

    }
}