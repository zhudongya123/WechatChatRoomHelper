package     com.zdy.project.wechat_chatroom_helper.wechat.plugins

import android.annotation.SuppressLint
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.ConfigInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.log.LogRecord
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.main.adapter.ConversationItemHandler
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.main.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.main.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils

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

        if (p0.processName == Constants.WECHAT_PACKAGE_NAME) {

            classloader = p0.classLoader

            WechatJsonUtils.init(null)
            val configJson = AppSaveInfo.getConfigJson()

            WXObject.Adapter.C.ConversationWithAppBrandListView = configJson.get("conversationWithAppBrandListView").asString
            WXObject.Adapter.C.ConversationWithCacheAdapter = configJson.get("conversationWithCacheAdapter").asString
            WXObject.Adapter.C.ConversationAvatar = configJson.get("conversationAvatar").asString
            WXObject.Adapter.C.ConversationClickListener = configJson.get("conversationClickListener").asString
            WXObject.Tool.C.Logcat = configJson.get("logcat").asString


            MessageHandler.executeHook()
            MainAdapter.executeHook()
            MainLauncherUI.executeHook()
            LogRecord.executeHook()
        }

    }
}