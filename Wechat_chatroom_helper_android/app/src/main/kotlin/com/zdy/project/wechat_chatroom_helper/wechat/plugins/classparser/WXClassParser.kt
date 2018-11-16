package com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser

import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.LogUtils
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

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
            val clazz = classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.ui.conversation") }
                    .firstOrNull { it.methods.any { it.name == "clearCache" } }!!


            return clazz
        }

        fun getConversationWithAppBrandListView(classes: MutableList<Class<*>>): Class<*>? {
            return classes.firstOrNull { it.name == "${Constants.WECHAT_PACKAGE_NAME}.ui.conversation.ConversationWithAppBrandListView" }
        }

        fun getConversationClickListener(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.ui.conversation") }
                    .filterNot { it.name.contains("$") }
                    .filter {
                        it.methods.any {
                            it.name == "onItemClick" &&
                                    it.parameterTypes.size == 4 &&
                                    it.parameterTypes[0] == AdapterView::class.java &&
                                    it.parameterTypes[1] == View::class.java &&
                                    it.parameterTypes[2] == Int::class.java &&
                                    it.parameterTypes[3] == Long::class.java
                        }
                    }
                    .filter { it.interfaces.size == 1 }
                    .firstOrNull { it.enclosingClass == null }
        }

        fun getConversationLongClickListener(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.ui.conversation") }
                    .filterNot { it.name.contains("$") }
                    .filter {
                        it.methods.any {
                            it.name == "onItemLongClick" && it.parameterTypes.size == 4 &&
                                    it.parameterTypes[0] == AdapterView::class.java &&
                                    it.parameterTypes[1] == View::class.java &&
                                    it.parameterTypes[2] == Int::class.java &&
                                    it.parameterTypes[3] == Long::class.java
                        }
                    }
                    .firstOrNull { it.constructors.any { it.parameterTypes.size == 4 } }

        }

        fun getConversationMenuItemSelectedListener(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.ui.conversation") }
                    .filter { it.name.split("$").size == 2 }
                    .filter {
                        it.methods.any {
                            it.name == "onMMMenuItemSelected" &&
                                    it.parameterTypes.size == 2 &&
                                    it.parameterTypes[0] == MenuItem::class.java &&
                                    it.parameterTypes[1] == Int::class.java
                        }
                    }
                    .firstOrNull {
                        try {
                            LogUtils.log(it.declaredConstructors.first().toString())
                            it.declaredConstructors.size == 1 && it.declaredConstructors.any { it.parameterTypes.size == 1 }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            false
                        }
                    }

        }

        fun getConversationAvatar(classes: MutableList<Class<*>>): Class<*>? {
            return classes
                    .filter { it.name.contains("com.tencent.mm.pluginsdk.ui") }
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


        fun getConversationStickyHeaderHandler(classes: MutableList<Class<*>>): Class<*>? {

            val conversationWithCacheAdapter = getConversationWithCacheAdapter(classes)!!
            val beanClass = (conversationWithCacheAdapter.genericSuperclass as ParameterizedType).actualTypeArguments[1]

            return classes.filter { Modifier.isFinal(it.modifiers) }
                    .firstOrNull { it ->
                        it.methods.any { method ->
                            method.parameterTypes.size == 3 &&
                                    Modifier.isStatic(method.modifiers) &&
                                    method.parameterTypes[0] == beanClass &&
                                    method.parameterTypes[1] == Int::class.java &&
                                    method.parameterTypes[2] == Long::class.java &&
                                    method.returnType == Long::class.java
                        }
                    }
        }

    }
}