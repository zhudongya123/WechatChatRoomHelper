package com.zdy.project.wechat_chatroom_helper.ui.helper

import android.app.Activity
import android.app.AlertDialog
import android.webkit.WebView
import network.ApiManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import ui.MyApplication
import java.io.IOException

/**
 * Created by zhudo on 2017/11/11.
 */
class InfoDialogBuilder {

    companion object {

        fun buildInfoDialog(activity: Activity, dialogCallback: CallBack) {
            ApiManager.sendRequestForHomeInfo(MyApplication.get().getHelperVersionCode().toString(), object : Callback {
                override fun onResponse(call: Call?, response: Response) {
                    val result = response.body()?.string()
                    activity.runOnUiThread {
                        val webView = WebView(activity)
                        webView.loadData(result, "text/html; charset=UTF-8", null)
                        dialogCallback.receive(AlertDialog.Builder(activity).setView(webView).create())
                    }
                }

                override fun onFailure(call: Call?, e: IOException?) {}
            })
        }
    }

    interface CallBack {

        fun receive(dialog: AlertDialog)
    }
}