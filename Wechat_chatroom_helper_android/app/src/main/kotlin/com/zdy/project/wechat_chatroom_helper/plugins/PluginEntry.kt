package     com.zdy.project.wechat_chatroom_helper.plugins

import android.annotation.SuppressLint
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.plugins.log.LogRecord
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.ConversationItemHandler
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.plugins.main.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import utils.WechatJsonUtils

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

            WechatJsonUtils.getFileString()
            val configJson = AppSaveInfo.getConfigJson()

            WXObject.Adapter.C.ConversationWithAppBrandListView = configJson.get("conversationWithAppBrandListView").asString
            WXObject.Adapter.C.ConversationWithCacheAdapter = configJson.get("conversationWithCacheAdapter").asString
            WXObject.Adapter.C.ConversationAvatar = configJson.get("conversationAvatar").asString
            WXObject.Adapter.C.ConversationClickListener = configJson.get("conversationClickListener").asString

            WXObject.Tool.C.Logcat = configJson.get("logcat").asString


            ConversationItemHandler
            MessageHandler.executeHook()
            MainAdapter.executeHook()
            MainLauncherUI.executeHook()
            LogRecord.executeHook()
        }

    }
}