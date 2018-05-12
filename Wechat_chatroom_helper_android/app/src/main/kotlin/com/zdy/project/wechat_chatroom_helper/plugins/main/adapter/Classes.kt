package com.zdy.project.wechat_chatroom_helper.plugins.main.adapter

import android.database.Cursor
import android.widget.ImageView
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.conversation.Classes
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import de.robv.android.xposed.XposedHelpers

object Classes {

    val ConversationWithAppBrandListView: Class<*> by WechatGlobal.wxLazy("ConversationWithAppBrandListView") {
        ReflectionUtil.findClassIfExists("${WechatGlobal.wxPackageName}.ui.conversation.ConversationWithAppBrandListView", WechatGlobal.wxLoader)
    }

    val WechatClasses by WechatGlobal.wxLazy("WechatClasses") {
        WechatGlobal.wxClasses!!.filter { it.contains(WechatGlobal.wxPackageName) }.map { XposedHelpers.findClass(it, PluginEntry.classloader) }
    }

    val ClassesByCursor by WechatGlobal.wxLazy("ClassesByCursor") {
        WechatClasses.filter { it.interfaces.contains(Cursor::class.java) }
                .flatMap {
                    val clazz = it
                    WechatClasses.filter { it.interfaces.contains(clazz) }
                }
    }

    val SetAvatarClass by WechatGlobal.wxLazy("SetAvatarClass") {
        WechatClasses.filter { it.name.contains("com.tencent.mm.pluginsdk.ui") }
                .filter { it.declaredClasses.isNotEmpty() }
                .firstOrNull { it.declaredClasses.any { it.methods.map { it.name }.contains("doInvalidate") } }
                ?.declaredClasses
                ?.firstOrNull {
                    it.methods.any {
                        it.parameterTypes.isNotEmpty() &&
                                it.parameterTypes[0].name == ImageView::class.java.name
                    }
                }!!
    }
    val SetAvatarMethod by WechatGlobal.wxLazy("SetAvatarMethod") {
        SetAvatarClass.methods.firstOrNull {
            it.parameterTypes.isNotEmpty() && it.parameterTypes[0].name == ImageView::class.java.name
        }!!.name
    }

    val ConversationClickListener: Class<*> by WechatGlobal.wxLazy("ConversationClickListener") {
        ReflectionUtil.findClassesFromPackage(WechatGlobal.wxLoader!!, WechatGlobal.wxClasses!!, "${WechatGlobal.wxPackageName}.ui.conversation")
                .filterByMethod(null, "onItemClick", C.AdapterView, C.View, C.Int, C.Long)
                .firstOrNull()

    }

    fun getConversationTimeString(adapter: Any, conversationTime: Long): CharSequence {

        val method = Classes.ConversationWithCacheAdapter.declaredMethods
                .filter { !it.isAccessible }
                .filter { it.returnType == CharSequence::class.java }
                .firstOrNull { it.parameterTypes.size == 1 }

        method?.let {

            val aeClass = XposedHelpers.findClass(it.parameterTypes[0].name, PluginEntry.classloader)

            val constructor = aeClass.constructors.filter { it.parameterTypes.size == 1 }.firstOrNull { it.parameterTypes[0] == String::class.java }

            constructor?.let {
                val obj = constructor.newInstance("")

                aeClass.getField("field_status").set(obj, 0)
                aeClass.getField("field_conversationTime").set(obj, conversationTime)

                return XposedHelpers.callMethod(adapter, method.name, obj) as CharSequence
            }
        }
        return ""
    }


    fun getConversationAvatar(field_username: String, imageView: ImageView) {
        XposedHelpers.callStaticMethod(SetAvatarClass, SetAvatarMethod, imageView, field_username)

    }

}