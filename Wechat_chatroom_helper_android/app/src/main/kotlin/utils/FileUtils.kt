package utils

import android.os.Environment
import java.io.File

/**
 * Created by Mr.Zdy on 2017/11/2.
 */
class FileUtils {

    companion object {
        fun init() {
            val externalStorageDirectory = Environment.getExternalStorageDirectory()

            val folder = File(externalStorageDirectory.absolutePath + "/WechatChatroomHelper")
            val config = File(externalStorageDirectory.absolutePath + "/WechatChatroomHelper/config.xml")


            if (!folder.exists())
                folder.mkdirs()

            if (!config.exists())
                config.createNewFile()
        }
    }
}