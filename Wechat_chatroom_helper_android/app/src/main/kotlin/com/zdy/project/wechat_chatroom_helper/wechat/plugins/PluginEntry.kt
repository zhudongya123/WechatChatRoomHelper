package     com.zdy.project.wechat_chatroom_helper.wechat.plugins

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapterLongClick
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.log.LogRecord
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.other.OtherHook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by Mr.Zdy on 2018/3/31.
 */

@SuppressLint("StaticFieldLeak")
class PluginEntry : IXposedHookLoadPackage {

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {

        if (param.packageName != "com.tencent.mm") return
        /**
         * 验证微信数据环境
         */

        findHookTinkerClassLoader(param.classLoader)
        //findHookTinkerClassLoader2(param.classLoader)
        //setClassloaderAndExecuteHook(param.classLoader)
    }

    private fun findHookTinkerClassLoader(classLoader: ClassLoader) {
        val tinkerApplicationClass = classLoader.loadClass("com.tencent.tinker.loader.app.TinkerApplication")
        XposedHelpers.findAndHookMethod(tinkerApplicationClass, "attachBaseContext", Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("PluginEntry, TinkerApplication, attach = ${param.thisObject}")
                        val application = param.thisObject as Application
                        val tinkerClassLoader = application.classLoader
                        setClassloaderAndExecuteHook(tinkerClassLoader)
                    }
                })
    }



    private fun setClassloaderAndExecuteHook(classLoader: ClassLoader) {
        XposedBridge.log("PluginEntry, setClassloaderAndExecuteHook")
        RuntimeInfo.classloader = classLoader
        getConfigInfo()
        hookList()
    }

    /**
     * 初始化配置项和数据
     */
    private fun getConfigInfo() {
        WechatJsonUtils.init(null)
        val configJson = AppSaveInfo.getConfigJson()

        WXObject.Adapter.C.ConversationWithCacheAdapter = configJson.get("conversationWithCacheAdapter").asString
        WXObject.Adapter.C.ConversationAvatar = configJson.get("conversationAvatar").asString
        WXObject.Adapter.C.ConversationClickListener = configJson.get("conversationClickListener").asString
        WXObject.Adapter.C.ConversationLongClickListener = configJson.get("conversationLongClickListener").asString
        WXObject.Adapter.C.ConversationMenuItemSelectedListener = configJson.get("conversationMenuItemSelectedListener").asString
        WXObject.Adapter.C.ConversationStickyHeaderHandler = configJson.get("conversationStickyHeaderHandler").asString
        WXObject.Adapter.C.ConversationHashMapBean = configJson.get("conversationHashMapBean").asString
        WXObject.Tool.C.Logcat = configJson.get("logcat").asString
    }

    /**
     * 注入Hook
     */
    private fun hookList() {
        try {
            MessageHandler.executeHook()
            MainAdapter.executeHook()
            MainAdapterLongClick.executeHook()
            MainLauncherUI.executeHook()
            if (AppSaveInfo.openLogInfo()) {
                LogRecord.executeHook()
            }
            OtherHook.executeHook()


        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun findHookTinkerClassLoader2(classLoader: ClassLoader) {
        val tinkerClassLoaderClass = classLoader.loadClass("com.tencent.tinker.loader.TinkerClassLoader")
        XposedBridge.hookAllConstructors(tinkerClassLoaderClass,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("PluginEntry, `findHookTinkerClassLoader`, constructor = ${param.thisObject}")
                        setClassloaderAndExecuteHook(param.thisObject::class.java.classLoader)
                    }
                })
    }
}