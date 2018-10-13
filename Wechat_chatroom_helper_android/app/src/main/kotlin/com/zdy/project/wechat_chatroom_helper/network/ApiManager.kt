package network

import android.os.Build
import okhttp3.*
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import java.io.IOException

object ApiManager {

    var okHttpClient = OkHttpClient()

    private const val CLASS_MAPPING = "http://122.152.202.233:8080/wechat/class/mapping"
    private const val USER_STATISTICS = "http://122.152.202.233:8080/wechat/user/statistics"

    /**
     * 发送用户统计请求，自带一天只发送一次的逻辑~
     */
    fun sendRequestForUserStatistics(action: String, uuid: String, model: String) {

        val sendTime = AppSaveInfo.apiRecordTimeInfo()
        if (System.currentTimeMillis() - sendTime < 60 * 60 * 22) return

        val requestBody: RequestBody = FormBody.Builder()
                .add("action", action)
                .add("uuidCode", uuid)
                .add("model", model)
                .add("version", AppSaveInfo.helpVersionCodeInfo())
                .add("wechat_version", AppSaveInfo.wechatVersionInfo())
                .add("android_version", Build.VERSION.SDK)
                .build()

        val request = Request.Builder()
                .url(USER_STATISTICS)
                .post(requestBody)
                .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call?, response: Response?) {
                AppSaveInfo.setApiRecordTime((System.currentTimeMillis() / 1000).toInt())
            }
        })
    }

    fun getClassMappingRequest(versionCode: String, play_version: Boolean): Request {

        val requestBody = FormBody.Builder()
                .add("versionCode", versionCode)
                .add("isPlayVersion", if (play_version) "1" else "0").build()

        return Request.Builder().url(CLASS_MAPPING).post(requestBody).build()
    }


    fun sendRequestForClassMapping(request: Request, onResponse: (response: Response) -> Unit) {
        ApiManager.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException?) {
            }

            override fun onResponse(call: Call, response: Response) {
                onResponse(response)
            }
        })

    }
}