package network

import android.os.Build
import okhttp3.*
import utils.AppSaveInfoUtils
import java.io.IOException

object ApiManager {

    var okHttpClient = OkHttpClient()

    private var HOST = "http://116.62.247.71:8080/"

    object UrlPath {
        var CLASS_MAPPING = HOST + "wechat/class/mapping"
        var USER_STATISTICS = HOST + "wechat/user/statistics"
    }

    fun sendRequestForUserStatistics(action: String, uuid: String, model: String) {

        val sendTime = AppSaveInfoUtils.apiRecordTimeInfo()
        if (System.currentTimeMillis() - sendTime < 60 * 60 * 22) return

        val requestBody: RequestBody = FormBody.Builder()
                .add("action", action)
                .add("uuidCode", uuid)
                .add("model", model)
                .add("version", AppSaveInfoUtils.helpVersionCodeInfo())
                .add("wechat_version", AppSaveInfoUtils.wechatVersionInfo())
                .add("android_version", Build.VERSION.SDK)
                .build()

        val request = Request.Builder()
                .url(UrlPath.USER_STATISTICS)
                .post(requestBody)
                .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call?, response: Response?) {
                AppSaveInfoUtils.setApiRecordTime((System.currentTimeMillis() / 1000).toInt())
            }
        })
    }

}