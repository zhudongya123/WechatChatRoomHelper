package com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser

import android.app.Activity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils
import ui.MyApplication
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
                    .filter { it.methods.any { it.name == "onNotifyChange" } }
                    .firstOrNull { clazz ->
                        val genericSuperclass = clazz.genericSuperclass ?: return@firstOrNull false
                        if (genericSuperclass !is ParameterizedType) return@firstOrNull false
                        genericSuperclass.actualTypeArguments.size == 2
                    }!!
            return clazz
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
                    .filter { it.constructors.any { constructor -> constructor.parameterTypes.size == 3 &&
                            constructor.parameterTypes[2] == Activity::class.java} }
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
//                    .filter { it.name.split("$").size == 2 }
                    .filter { it.name.split(".").none { it.contains("UI") } }
                    .filter {
                        it.methods.any {
                            it.name == "onMMMenuItemSelected" &&
                                    it.parameterTypes.size == 2 &&
                                    it.parameterTypes[0] == MenuItem::class.java &&
                                    it.parameterTypes[1] == Int::class.java
                        }
                    }
                    .filter { it.declaredFields.size == 1 }
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

        /**
         * 这个地方8031改的很离谱，方法所在的类不在com.tencent.mm包内
         *
         * 特征：该类有两个方法的参数是两个 且第一个都是ImageView
         * 有四个参数的方法 第一个参数是ImageView
         */
        fun getConversationAvatar(classes: MutableList<Class<*>>): Class<*>? {
            return classes
                    .filter { it.simpleName.length < 2 }
                    .filter { Modifier.isStatic(it.modifiers) }
                    .filter {
                        it.methods.count { method ->
                            method.parameterTypes.size == 2 &&
                                    method.parameterTypes[0].name == ImageView::class.java.name
                        } == 2
                    }
                    .firstOrNull {
                        it.methods.any { method ->
                            Modifier.isStatic(method.modifiers) &&
                                    method.parameterTypes.size == 4 &&
                                    method.parameterTypes[0].name == ImageView::class.java.name
                        }
                    }
        }


        /**
         * 获取置顶的标记方法
         * 这个地方8031改的很离谱，方法所在的类不在com.tencent.mm包内
         */
        fun getConversationStickyHeaderHandler(classes: MutableList<Class<*>>): Class<*>? {

            val conversationWithCacheAdapter = getConversationWithCacheAdapter(classes)!!
            val beanClass = (conversationWithCacheAdapter.genericSuperclass as ParameterizedType).actualTypeArguments[1]

            return classes
                    .firstOrNull {
                        try {
                            it.methods.any { method ->
                                method.parameterTypes.size == 3 &&
                                        Modifier.isStatic(method.modifiers) &&
                                        method.parameterTypes[0] == beanClass &&
                                        method.parameterTypes[1] == Int::class.java &&
                                        method.parameterTypes[2] == Long::class.java &&
                                        method.returnType == Long::class.java
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            false
                        }
                    }
        }


        /**
         * 下面两个方法开了混淆就失效了，暂时弃用
         */
        fun getConversationItemHighLightSelectorBackGroundInt(classes: MutableList<Class<*>>): Int {
            val backgroundClass = classes
                    // .filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.plugin") }
                    .first {
                        try {
                            it.fields.any { field ->
                                field.name == "comm_item_highlight_selector" &&
                                        Modifier.isFinal(field.modifiers) &&
                                        Modifier.isStatic(field.modifiers) &&
                                        Modifier.isPublic(field.modifiers) &&
                                        field.type == Int::class.java
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            false
                        }
                    }

            val field = backgroundClass.getField("comm_item_highlight_selector")
            return field.getInt(null)
        }

        fun getConversationItemSelectorBackGroundInt(classes: MutableList<Class<*>>): Int {
            val backgroundClass = classes
                    // .filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.plugin") }
                    .first {
                        try {
                            it.fields.any { field ->
                                field.name == "comm_list_item_selector" &&
                                        Modifier.isFinal(field.modifiers) &&
                                        Modifier.isStatic(field.modifiers) &&
                                        Modifier.isPublic(field.modifiers) &&
                                        field.type == Int::class.java
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            false
                        }
                    }


            val field =
                    if (DeviceUtils.isWechatUpdate7(MyApplication.get()))
                        backgroundClass.getField("white_list_item_selector")
                    else backgroundClass.getField("comm_list_item_selector")
            return field.getInt(null)
        }
    }
}