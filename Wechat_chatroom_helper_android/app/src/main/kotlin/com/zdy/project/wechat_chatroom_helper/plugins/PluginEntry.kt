package     com.zdy.project.wechat_chatroom_helper.plugins

import android.annotation.SuppressLint
import com.gh0u1l5.wechatmagician.spellbook.SpellBook
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.plugins.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageHooker
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter
import com.zdy.project.wechat_chatroom_helper.wechat.manager.RuntimeInfo
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

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

        if (p0.processName != Constants.WECHAT_PACKAGE_NAME) return

        classloader = p0.classLoader

        try {
            SpellBook.startup(p0, listOf(MessageHooker, MainAdapter, MainLauncherUI), listOf())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        MainLauncherUI.executeHook()
        MainAdapter.executeHook()
        //LogRecord.executeHook()
    }
}