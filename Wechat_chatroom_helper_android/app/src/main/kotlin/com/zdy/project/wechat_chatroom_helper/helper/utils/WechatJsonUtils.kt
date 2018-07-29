package utils

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.zdy.project.wechat_chatroom_helper.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset


/**
 * Created by Mr.Zdy on 2017/11/2.
 */
object WechatJsonUtils {

    private val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/WechatChatroomHelper"
    private val configPath = Environment.getExternalStorageDirectory().absolutePath + "/WechatChatroomHelper/config.xml"
    private val parser = JsonParser()

    private lateinit var currentString: String
    private lateinit var currentJson: JsonObject


    fun init(activity: Activity) {

        val folder = File(folderPath)
        val config = File(folderPath, "config.xml")

        if (!folder.exists())
            folder.mkdirs()

        if (!config.exists()) {
            config.createNewFile()
            config.setWritable(true)
            config.setReadable(true)
        }
        activity.sendBroadcast(Intent(Constants.FILE_INIT_SUCCESS))

        getFileString()
    }

    fun getJsonValue(key: String, defaultValue: String): String {
        val jsonObject = currentJson
        return if (jsonObject.has(key)) {
            jsonObject.get(key).asString
        } else {
            jsonObject.addProperty(key, defaultValue)
            putFileString(jsonObject.toString())
            defaultValue
        }
    }

    fun getJsonValue(key: String, defaultValue: Boolean): Boolean {
        val jsonObject = currentJson
        return if (jsonObject.has(key)) {
            jsonObject.get(key).asBoolean
        } else {
            jsonObject.addProperty(key, defaultValue)
            putFileString(jsonObject.toString())
            defaultValue
        }
    }

    fun getJsonValue(key: String, defaultValue: Int): Int {
        val jsonObject = currentJson
        return if (jsonObject.has(key)) {
            jsonObject.get(key).asInt
        } else {
            jsonObject.addProperty(key, defaultValue)
            putFileString(jsonObject.toString())
            defaultValue
        }
    }


    fun putJsonValue(key: String, value: Boolean) {
        val jsonObject = currentJson
        jsonObject.addProperty(key, value)
        putFileString(jsonObject.toString())
    }

    fun putJsonValue(key: String, value: Int) {
        val jsonObject = currentJson
        jsonObject.addProperty(key, value)
        putFileString(jsonObject.toString())
    }

    fun putJsonValue(key: String, value: String) {
        val jsonObject = currentJson
        jsonObject.addProperty(key, value)
        putFileString(jsonObject.toString())
    }


    private fun getFileString(): String {
        val fis = FileInputStream(File(configPath))
        val length = fis.available()
        val buffer = ByteArray(length)
        fis.read(buffer)
        val res = String(buffer, Charset.forName("UTF-8"))
        fis.close()
        Log.v("WechatJsonUtils", "getFileString = $res")

        currentString = res
        currentJson = parser.parse(currentString).asJsonObject

        return res
    }

    private fun putFileString(string: String) {
        Log.v("WechatJsonUtils", "putFileString = $string")
        try {
            val fos = FileOutputStream(File(configPath))
            fos.write(string.toByteArray())
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        getFileString()
    }
}