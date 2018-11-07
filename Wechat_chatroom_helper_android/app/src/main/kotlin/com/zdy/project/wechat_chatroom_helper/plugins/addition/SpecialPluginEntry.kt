package com.zdy.project.wechat_chatroom_helper.plugins.addition

import com.zdy.project.wechat_chatroom_helper.plugins.addition.hook.DataBaseHook
import com.zdy.project.wechat_chatroom_helper.plugins.addition.hook.FConversationHook
import com.zdy.project.wechat_chatroom_helper.plugins.addition.hook.LBSFriendHook
import com.zdy.project.wechat_chatroom_helper.plugins.addition.hook.LogHook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import javax.xml.parsers.DocumentBuilderFactory

class SpecialPluginEntry : IXposedHookLoadPackage {

    companion object {

        val Logclass = "com.tencent.mm.sdk.platformtools.x"
        val DB = "com.tencent.wcdb.database.SQLiteDatabase"
        val DBF = "com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory"
        val DBSIGN = "com.tencent.wcdb.support.CancellationSignal"

        var time = 3000L

        var dbf = DocumentBuilderFactory.newInstance()
    }

    lateinit var classLoader: ClassLoader
    lateinit var C: Clazz


    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {


        classLoader = lpparam.classLoader

        if (lpparam.processName == "com.tencent.mm") {
//            C = Clazz(lpparam.classLoader)
//
//
//            DataBaseHook.C = C
//            FConversationHook.C = C
//            LBSFriendHook.C = C
//
//
//            LogHook.hook(classLoader)
//            DataBaseHook.hook(classLoader)
//            FConversationHook.hook(classLoader)
//            LBSFriendHook.hook(classLoader)
        }

        try {
            XposedHelpers.findClass("com.tencent.mm.ui.LauncherUI", lpparam.classLoader)

            C = Clazz(lpparam.classLoader)


            DataBaseHook.C = C
            FConversationHook.C = C
            LBSFriendHook.C = C


            LogHook.hook(classLoader)
            DataBaseHook.hook(classLoader)
            FConversationHook.hook(classLoader)
            LBSFriendHook.hook(classLoader)


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}