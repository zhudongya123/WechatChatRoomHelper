package utils

import android.app.Activity
import android.content.Intent
import android.os.Environment
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
class FileUtils {

    companion object {

        val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/WechatChatroomHelper"
        val configPath = Environment.getExternalStorageDirectory().absolutePath + "/WechatChatroomHelper/config.xml"

        val parser = JsonParser()

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
        }


         fun getJsonValue(key: String, defaultValue: String): String {
            val jsonObject: JsonObject
            try {
                jsonObject = parser.parse(getFileString()).asJsonObject
            } catch (e: Exception) {
                putFileString(JsonObject().toString())
                return defaultValue
            }
            return if (jsonObject.has(key)) {
                if (jsonObject.get(key).asString === "") {
                    jsonObject.addProperty(key, defaultValue)
                    putFileString(jsonObject.toString())
                    defaultValue
                } else jsonObject.get(key).asString
            } else {
                jsonObject.addProperty(key, defaultValue)
                putFileString(jsonObject.toString())
                defaultValue
            }
        }

         fun getJsonValue(key: String, defaultValue: Boolean): Boolean {
            val jsonObject: JsonObject
            try {
                jsonObject = parser.parse(getFileString()).asJsonObject
            } catch (e: Exception) {
                putFileString(JsonObject().toString())
                return defaultValue
            }
            return if (jsonObject.has(key)) {
                jsonObject.get(key).asBoolean
            } else {
                jsonObject.addProperty(key, defaultValue)
                putFileString(jsonObject.toString())
                defaultValue
            }
        }

        fun putJsonValue(key: String, value: Boolean) {
            val jsonObject: JsonObject? = try {
                parser.parse(getFileString()).asJsonObject
            } catch (e: Exception) {
                JsonObject()
            }
            jsonObject!!.addProperty(key, value)
            putFileString(jsonObject.toString())
        }


        fun putJsonValue(key: String, value: String) {
            val jsonObject: JsonObject? = try {
                parser.parse(getFileString()).asJsonObject
            } catch (e: Exception) {
                JsonObject()
            }
            jsonObject!!.addProperty(key, value)
            putFileString(jsonObject.toString())
        }

        private fun getFileString(): String {

            val fis = FileInputStream(File(configPath))
            val length = fis.available()
            val buffer = ByteArray(length)
            fis.read(buffer)
            val res = String(buffer, Charset.forName("UTF-8"))
            fis.close()
            return res
        }

        private fun putFileString(string: String) {
            try {
                val fos = FileOutputStream(File(configPath))
                fos.write(string.toByteArray())
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
    }
}