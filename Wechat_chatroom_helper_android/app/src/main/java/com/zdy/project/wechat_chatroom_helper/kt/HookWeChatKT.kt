package com.zdy.project.wechat_chatroom_helper.kt

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedBridge.*
import de.robv.android.xposed.XposedHelpers.*
/**
 * Created by Mr.Zdy on 2017/10/23.
 */
class HookWeChatKT: IXposedHookLoadPackage{
    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam?) {


    }
}