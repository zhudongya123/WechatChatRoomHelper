package     com.zdy.project.wechat_chatroom_helper.wechat.plugins

import android.annotation.SuppressLint
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.log.LogRecord
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageHandler
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by Mr.Zdy on 2018/3/31.
 */

@SuppressLint("StaticFieldLeak")
class PluginEntry : IXposedHookLoadPackage {


    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam) {

        if (p0.processName == Constants.WECHAT_PACKAGE_NAME) {

            try {
                XposedHelpers.findClass(WXObject.Message.C.SQLiteDatabase, p0.classLoader)
            } catch (e: Throwable) {
                e.printStackTrace()
                return
            }

            RuntimeInfo.classloader = p0.classLoader

            WechatJsonUtils.init(null)
            val configJson = AppSaveInfo.getConfigJson()

            WXObject.Adapter.C.ConversationWithAppBrandListView = configJson.get("conversationWithAppBrandListView").asString
            WXObject.Adapter.C.ConversationWithCacheAdapter = configJson.get("conversationWithCacheAdapter").asString
            WXObject.Adapter.C.ConversationAvatar = configJson.get("conversationAvatar").asString
            WXObject.Adapter.C.ConversationClickListener = configJson.get("conversationClickListener").asString
            WXObject.Adapter.C.ConversationLongClickListener = configJson.get("conversationLongClickListener").asString
            WXObject.Tool.C.Logcat = configJson.get("logcat").asString


            try {
                MessageHandler.executeHook()
                MainAdapter.executeHook()
                MainLauncherUI.executeHook()
                LogRecord.executeHook()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }
}