package     com.zdy.project.wechat_chatroom_helper.wechat.plugins

import android.annotation.SuppressLint
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.LogUtils
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
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by Mr.Zdy on 2018/3/31.
 */

@SuppressLint("StaticFieldLeak")
class PluginEntry : IXposedHookLoadPackage {

    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam) {

        if (p0.processName == Constants.WECHAT_PACKAGE_NAME) {

            /**
             * 验证微信数据环境
             */
            try {
                XposedHelpers.findClass(WXObject.Message.C.SQLiteDatabase, p0.classLoader)
            } catch (e: Throwable) {
                e.printStackTrace()
                return
            }

            /**
             * 初始化配置项和数据
             */
            RuntimeInfo.classloader = p0.classLoader

            WechatJsonUtils.init(null)
            val configJson = AppSaveInfo.getConfigJson()

            WXObject.Adapter.C.ConversationWithCacheAdapter = configJson.get("conversationWithCacheAdapter").asString
            WXObject.Adapter.C.ConversationAvatar = configJson.get("conversationAvatar").asString
            WXObject.Adapter.C.ConversationClickListener = configJson.get("conversationClickListener").asString
            WXObject.Adapter.C.ConversationLongClickListener = configJson.get("conversationLongClickListener").asString
            WXObject.Adapter.C.ConversationMenuItemSelectedListener = configJson.get("conversationMenuItemSelectedListener").asString
            WXObject.Adapter.C.ConversationStickyHeaderHandler = configJson.get("conversationStickyHeaderHandler").asString
            WXObject.Adapter.C.ConversationHashMapBean = configJson.get("conversationHashMapBean").asString


//            WXObject.Adapter.F.ConversationItemHighLightSelectorBackGroundInt = configJson.get("conversationItemHighLightSelectorBackGroundInt").asString.toInt()
//            WXObject.Adapter.F.ConversationItemSelectorBackGroundInt = configJson.get("conversationItemSelectorBackGroundInt").asString.toInt()

            WXObject.Tool.C.Logcat = configJson.get("logcat").asString

            MainAdapterLongClick.parseStickyInfo(AppSaveInfo.getHelperStickyInfo()) {
                MainAdapterLongClick.chatRoomStickyValue = it.first
                MainAdapterLongClick.officialStickyValue = it.second
            }

            Constants.defaultValue = Constants.DefaultValue(AppSaveInfo.getWechatVersionName().startsWith("7"))


            /**
             * 注入Hook
             */
            try {
                MessageHandler.executeHook()
                MainAdapter.executeHook()
                MainAdapterLongClick.executeHook()
                MainLauncherUI.executeHook()
//                if (AppSaveInfo.openLogInfo()) {
//                    LogRecord.executeHook()
//                }
                OtherHook.executeHook()


//                val fClass = XposedHelpers.findClass("com.tencent.mm.ui.f", RuntimeInfo.classloader)
//                val fClass2 = XposedHelpers.findClass("com.tencent.mm.storagebase.a.f", RuntimeInfo.classloader)
//
//                XposedHelpers.findAndHookMethod("com.tencent.mm.storagebase.a.f", RuntimeInfo.classloader, "fdA", object : XC_MethodHook() {
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        LogUtils.log("MessageHook 2019-04-05 14:11:46, supportHashMap = ${MainAdapter.supportHashMap}")
//                        param.result = MainAdapter.supportHashMap
//                    }
//
//                    override fun afterHookedMethod(param: MethodHookParam) {
//                        val result = param.result as HashMap<*, *>
//
//
//                        result.entries.forEach {
//                            LogUtils.log("storagebase, itemKey = ${it.key}, itemValue = ${it.value}")
//                        }
//
//                    }
//                })
//
//
//                val abClass = XposedHelpers.findClass("com.tencent.mm.storagebase.a.b", RuntimeInfo.classloader)
//                val aaClass = XposedHelpers.findClass("com.tencent.mm.storagebase.a.a", RuntimeInfo.classloader)
//
//
//                LogUtils.log("storagebase, line 109")
//                XposedHelpers.findAndHookMethod(abClass, "b", Any::class.java, aaClass, object : XC_MethodHook() {
//
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//
//                        val key = param.args[0]
//                        val value = param.args[1]
//
//                        LogUtils.log("storagebase, key = $key, value = $value")
//                    }
//
//                })
//                LogUtils.log("storagebase, line 121")


            } catch (e: Throwable) {
                e.printStackTrace()


            }


        }

    }
}