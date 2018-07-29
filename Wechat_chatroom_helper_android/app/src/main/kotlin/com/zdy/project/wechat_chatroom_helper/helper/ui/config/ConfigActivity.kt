package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.R
import dalvik.system.DexClassLoader
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.util.*

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        try {
            val applicationInfo = this.packageManager.getApplicationInfo(Constants.WECHAT_PACKAGE_NAME, 0)
            val publicSourceDir = applicationInfo.publicSourceDir

            val apkFile = ApkFile(File(publicSourceDir))
            val classes = apkFile.getDexClasses()

            val optimizedDirectory = getDir("dex", 0).absolutePath
            val classLoader = DexClassLoader(publicSourceDir, optimizedDirectory, null, classLoader)

            for (dexClass in classes) {
                val classType = dexClass.getClassType()
                val className = classType.substring(1, classType.length - 1).replace("/", ".")

                try {
                    val clazz = classLoader.loadClass(className)
                    println(clazz.toString() + "method = " + Arrays.toString(clazz.methods))
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


}