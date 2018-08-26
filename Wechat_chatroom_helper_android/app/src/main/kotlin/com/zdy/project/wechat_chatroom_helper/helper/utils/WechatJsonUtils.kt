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
    val configPath = Environment.getExternalStorageDirectory().absolutePath + "/WechatChatroomHelper/config.xml"
    val parser = JsonParser()

    private lateinit var currentString: String
    private lateinit var currentJson: JsonObject


    fun init(activity: Activity?) {

        val folder = File(folderPath)
        val config = File(folderPath, "config.xml")

        if (!folder.exists())
            folder.mkdirs()

        if (!config.exists()) {
            config.createNewFile()
            config.setWritable(true)
            config.setReadable(true)
        }
        activity?.sendBroadcast(Intent(Constants.FILE_INIT_SUCCESS))

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


    fun getFileString(): String {
        val fis: FileInputStream
        try {
            fis = FileInputStream(File(configPath))
        } catch (e: Exception) {
            e.printStackTrace()
            init(null)
            return getFileString()
        }
        val length = fis.available()
        val buffer = ByteArray(length)
        fis.read(buffer)
        var res = String(buffer, Charset.forName("UTF-8"))
        fis.close()
        Log.v("WechatJsonUtils", "getFileString = $res")
        if (res.isEmpty()) res = "{}"

        currentString = res
        currentJson = parser.parse(currentString).asJsonObject

        return res
    }

    private fun putFileString(result: String) {
        Log.v("WechatJsonUtils", "putFileString = $result")
        try {
            val fos = FileOutputStream(File(configPath))
            fos.write(result.toByteArray())
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        currentString = result
        currentJson = parser.parse(currentString).asJsonObject
    }
}