package     com.zdy.project.wechat_chatroom_helper.plugins

import android.annotation.SuppressLint
import com.gh0u1l5.wechatmagician.spellbook.SpellBook
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.plugins.log.LogRecord
import com.zdy.project.wechat_chatroom_helper.plugins.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by Mr.Zdy on 2018/3/31.
 */

@SuppressLint("StaticFieldLeak")
class PluginEntry : IXposedHookLoadPackage {


    companion object {

        lateinit var classloader: ClassLoader

        lateinit var chatRoomViewPresenter: ChatRoomViewPresenter

        lateinit var officialViewPresenter: ChatRoomViewPresenter
    }


    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam) {


        if (p0.packageName == "com.ss.android.ugc.aweme") {
            XposedHelpers.findAndHookMethod(OkHttpClient::class.java, "newCall",
                    Request::class.java, object : XC_MethodHook() {

                override fun beforeHookedMethod(param: MethodHookParam) {
                    val request = param.args[0] as Request

                    val url = request.url()
                    XposedBridge.log("aweme_detect, url = $url")

                    val headers = request.headers()

                    XposedBridge.log("aweme_detect, headers = $headers")

                }

            })
        }

        if (p0.processName != Constants.WECHAT_PACKAGE_NAME) return

        classloader = p0.classLoader

        try {
            SpellBook.startup(p0, listOf(MainLauncherUI, MessageHandler, MainAdapter), listOf())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        MainLauncherUI.executeHook()
        MainAdapter.executeHook()
        LogRecord.executeHook()
    }
}