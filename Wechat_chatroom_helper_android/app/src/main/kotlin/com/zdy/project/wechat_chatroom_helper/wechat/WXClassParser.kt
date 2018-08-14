package com.zdy.project.wechat_chatroom_helper.wechat

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import com.zdy.project.wechat_chatroom_helper.Constants
import java.lang.reflect.Method

object WXClassParser {

    object PlatformTool {

        fun getLogcat(classes: MutableList<Class<*>>): Class<*>? {

            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.sdk.platformtools") }
                    .filter { it.enclosingClass == null }
                    .firstOrNull {
                        try {
                            return@firstOrNull it.getMethod("getLogLevel") != null
                        } catch (e: Throwable) {
                            return@firstOrNull false
                        }
                    }
        }
    }


    object Adapter {

        fun getConversationWithCacheAdapter(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.ui.conversation") }
                    .firstOrNull {
                        try {
                            return@firstOrNull it.getMethod("clearCache") != null
                        } catch (e: Throwable) {
                            return@firstOrNull false
                        }
                    }
        }

        fun getConversationWithAppBrandListView(classes: MutableList<Class<*>>): Class<*>? {
            return classes.firstOrNull { it.name == "${Constants.WECHAT_PACKAGE_NAME}.ui.conversation.ConversationWithAppBrandListView" }
        }

        fun getConversationClickListener(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.ui.conversation") }
                    .filterNot { it.name.contains("$") }
                    .filter filter1@{
                        try {
                            return@filter1 it.getMethod("onItemClick", AdapterView::class.java, View::class.java, Int::class.java, Long::class.java) != null
                        } catch (e: Throwable) {
                            return@filter1 false
                        }
                    }.firstOrNull { it.enclosingClass == null }
        }


        fun getConversationAvatar(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("com.tencent.mm.pluginsdk.ui") }
                    .filter { it.declaredClasses.isNotEmpty() }
                    .firstOrNull { it.declaredClasses.any { it.methods.map { it.name }.contains("doInvalidate") } }!!
                    .declaredClasses!!
                    .firstOrNull {
                        it.methods.any {
                            it.parameterTypes.isNotEmpty() &&
                                    it.parameterTypes[0].name == ImageView::class.java.name
                        }
                    }
        }


    }
}