package com.zdy.project.wechat_chatroom_helper.hooker

import com.gh0u1l5.wechatmagician.spellbook.SpellBook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by Mr.Zdy on 2018/3/31.
 */
class WCHNewHook: IXposedHookLoadPackage {
    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam) {


        SpellBook.startup(p0, listOf(MessageHooker), listOf())

    }
}