package com.zdy.project.wechat_chatroom_helper.helper.utils

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
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
            FileIOUtils.writeFileFromString(config, "{}")
        }
        activity?.sendBroadcast(Intent(Constants.FILE_INIT_SUCCESS))

        getFileString()
    }

    fun getJsonValue(key: String, defaultValue: String): String {
        return if (currentJson.has(key)) {
            currentJson.get(key).asString
        } else {
            currentJson.addProperty(key, defaultValue)
            defaultValue
        }
    }

    fun getJsonValue(key: String, defaultValue: Boolean): Boolean {
        return if (currentJson.has(key)) {
            currentJson.get(key).asBoolean
        } else {
            currentJson.addProperty(key, defaultValue)
            defaultValue
        }
    }

    fun getJsonValue(key: String, defaultValue: Int): Int {
        return if (currentJson.has(key)) {
            currentJson.get(key).asInt
        } else {
            currentJson.addProperty(key, defaultValue)
            defaultValue
        }
    }

    fun putJsonValue(key: String, value: Boolean) {
        val jsonObject = currentJson
        jsonObject.addProperty(key, value)
    }

    fun putJsonValue(key: String, value: Int) {
        val jsonObject = currentJson
        jsonObject.addProperty(key, value)
    }

    fun putJsonValue(key: String, value: String) {
        val jsonObject = currentJson
        jsonObject.addProperty(key, value)
    }


    private fun getFileString(): String {

        val res = FileIOUtils.readFile2String(configPath, "UTF-8")
        if (TextUtils.isEmpty(res)) {
            init(null)
            return getFileString()
        }

        Log.v("WechatJsonUtils", "getFileString = $res")
        currentJson = parser.parse(res).asJsonObject
        return res
    }

    fun putFileString() {
        Log.v("WechatJsonUtils", "putFileString = $currentJson")

        FileIOUtils.writeFileFromString(File(configPath), currentJson.toString())
        currentJson = parser.parse(getFileString()).asJsonObject
    }
}