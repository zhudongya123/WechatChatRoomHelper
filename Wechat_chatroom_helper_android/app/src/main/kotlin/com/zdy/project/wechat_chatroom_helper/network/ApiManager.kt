package network

import okhttp3.*
import utils.AppSaveInfoUtils
import java.io.IOException

object ApiManager {

    var okHttpClient = OkHttpClient()
    private var sendTime: Long = 0


    object UrlPath {
        var CLASS_MAPPING = getHost() + "wechat/class/mapping"
        var HOME_INFO = getHost() + "wechat/home/info"
        var USER_STATISTICS = getHost() + "wechat/user/statistics"
    }


    private fun getHost(): String {
        return "http://116.62.247.71:8080/"
    }

    fun sendRequestForUserStatistics(action: String, uuid: String, model: String) {

        if (System.currentTimeMillis() - sendTime < 60000) return

        val requestBody: RequestBody = FormBody.Builder()
                .add("action", action)
                .add("uuidCode", uuid)
                .add("model", model)
                .add("version", AppSaveInfoUtils.helpVersionCodeInfo())
                .add("wechat_version", AppSaveInfoUtils.wechatVersionInfo())
                .build()

        val request = Request.Builder()
                .url(UrlPath.USER_STATISTICS)
                .post(requestBody)
                .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call?, response: Response?) {
                val string = response?.body()?.string()
                sendTime = System.currentTimeMillis()
            }
        })
    }

    fun sendRequestForHomeInfo(helpVersion: String, callback: Callback) {

        val requestBody = FormBody.Builder()
                .add("versionCode", helpVersion)
                .build()

        val request = Request.Builder()
                .url(UrlPath.HOME_INFO)
                .post(requestBody)
                .build()

        okHttpClient.newCall(request).enqueue(callback)
    }

}