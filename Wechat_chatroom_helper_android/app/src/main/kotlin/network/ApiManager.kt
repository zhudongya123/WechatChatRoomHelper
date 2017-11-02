package network

import android.util.Log
import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils
import okhttp3.*
import java.io.IOException

/**
 * Created by Mr.Zdy on 2017/10/23.
 */
object ApiManager {

    var okHttpClient = OkHttpClient()
    private var sendTime: Long = 0


    object UrlPath {
        var CLASS_MAPPING = getHost() + "wechat/class/mapping"
        var ERROR_RECEIVER = getHost() + "wechat/error/receiver"
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
                .add("version", PreferencesUtils.helper_versionCode().toString())
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
                Log.v(string, string)
                sendTime = System.currentTimeMillis()
            }
        })
    }

    public fun sendRequestForHomeInfo(helpVersion: String, callback: Callback) {

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