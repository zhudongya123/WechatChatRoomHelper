package com.zdy.project.wechat_chatroom_helper.wechat

import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import com.zdy.project.wechat_chatroom_helper.Constants
import java.lang.reflect.Method

object WXClassParser {


    class Platformtool {

        fun getLogcat(classes: MutableList<Class<*>>): Class<*>? {

            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.sdk.platformtools") }
                    .filter { it.enclosingClass == null }
                    .firstOrNull {
                        it.methods.any {
                            it.parameterTypes.size == 1
                                    && it.parameterTypes[0].name == Int::class.java.name
                                    && it.name == "getLogLevel"
                        }
                    }
        }
    }


    class Adapter {

        fun getConversationWithCacheAdapter(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.ui.conversation") }
                    .firstOrNull { it.methods.any { it.returnType == null && it.name == "clearCache" } }
        }

        fun getConversationWithAppBrandListView(classes: MutableList<Class<*>>): Class<*>? {
            return classes.firstOrNull { it.name == "${Constants.WECHAT_PACKAGE_NAME}.ui.conversation.ConversationWithAppBrandListView" }
        }

        fun getConversationClickListener(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("${Constants.WECHAT_PACKAGE_NAME}.ui.conversation") }.firstOrNull {
                it.methods.any {
                    it.returnType == null && it.name == "onItemClick"
                            && it.parameterTypes[0] == AdapterView::class.java && it.parameterTypes[1] == View::class.java
                            && it.parameterTypes[2] == Int::class.java
                }
            }
        }


        fun getConversationAvatar(classes: MutableList<Class<*>>): Class<*>? {
            return classes.filter { it.name.contains("com.tencent.mm.pluginsdk.ui") }
                    .filter { it.declaredClasses.isNotEmpty() }
                    .firstOrNull { it.declaredClasses.any { it.methods.map { it.name }.contains("doInvalidate") } }
                    ?.declaredClasses
                    ?.firstOrNull {
                        it.methods.any {
                            it.parameterTypes.isNotEmpty() &&
                                    it.parameterTypes[0].name == ImageView::class.java.name
                        }
                    }
        }

        fun getConversationAvatarMethod(classes: MutableList<Class<*>>): Method? {
            return getConversationAvatar(classes)?.methods?.firstOrNull {
                it.parameterTypes.isNotEmpty() && it.parameterTypes[0].name == ImageView::class.java.name
            }
        }

        fun getConversationContentMethod(classes: MutableList<Class<*>>): Method? {
            return getConversationWithCacheAdapter(classes)?.declaredMethods?.filter { !it.isAccessible }!!
                    .filter { it.returnType == CharSequence::class.java }
                    .firstOrNull { it.parameterTypes.size == 1 }
        }

    }
}