package com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser

import android.util.Log
import android.widget.ImageView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.io.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.ParameterizedType

object ConversationReflectFunction {

    val conversationWithCacheAdapter = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithCacheAdapter, RuntimeInfo.classloader)
    val conversationAvatar = XposedHelpers.findClass(WXObject.Adapter.C.ConversationAvatar, RuntimeInfo.classloader)
    val conversationListView = XposedHelpers.findClass(WXObject.Adapter.C.ConversationListView, RuntimeInfo.classloader)
    val conversationClickListener = XposedHelpers.findClass(WXObject.Adapter.C.ConversationClickListener, RuntimeInfo.classloader)
    val conversationStickyHeaderHandler = XposedHelpers.findClass(WXObject.Adapter.C.ConversationStickyHeaderHandler, RuntimeInfo.classloader)
    val conversationHashMapBean = XposedHelpers.findClass(WXObject.Adapter.C.ConversationHashMapBean, RuntimeInfo.classloader)


    /**
     * 这个就是获取itemView里面的model的那个对象的方法 一般命名为getItem(index) 方法
     * 早几年微信不这么命名 而是自己混淆了一个方法 现在又回来了 不混淆了
     */
    val conversationWithCacheAdapterGetItemMethodName: String = conversationWithCacheAdapter.superclass.methods
            .filter { it.parameterTypes.size == 1 && it.parameterTypes[0] == Int::class.java }
            .filter { it.returnType != Int::class.java }
            .first { it.name != "getItemId" }.name

    private val conversationTimeStringMethod = conversationWithCacheAdapter.declaredMethods
            .filter { !it.isAccessible }
            .filter { it.returnType == CharSequence::class.java }
            .first { it.parameterTypes.size == 1 }

    /**
     * @since 2021-08-05 21:18:10
     * 微信 1940 后 改成 private 方法
     */
    private val conversationAvatarMethod = conversationAvatar.declaredMethods
            .first {
                it.parameterTypes.size == 4
                        && it.parameterTypes[0].name == ImageView::class.java.name
                        && it.parameterTypes[1].name == String::class.java.name
                        && it.parameterTypes[2].name == Float::class.java.name
                        && it.parameterTypes[3].name == Boolean::class.java.name
            }

    val conversationGetHashMapMethod = conversationHashMapBean.methods
            .filter { it.returnType == HashMap::class.java }
            .first { it.parameterTypes.isEmpty() }


    val beanClass = ((conversationWithCacheAdapter.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<*>)
    val beanConstructor = beanClass.constructors
            .filter { it.parameterTypes.size == 1 }
            .first { it.parameterTypes[0] == String::class.java }

    val stickyHeaderHandlerMethod = conversationStickyHeaderHandler.methods.first { it.parameterTypes.size == 3 }


    fun getConversationTimeString(adapter: Any, conversationTime: Long): CharSequence {

        conversationTimeStringMethod.let { _ ->

            beanConstructor?.let {
                val obj = beanConstructor.newInstance("")

                setupItemClassField(obj, "field_status", 0)
                setupItemClassField(obj, "field_conversationTime", conversationTime)

                return XposedHelpers.callMethod(adapter, conversationTimeStringMethod.name, obj) as CharSequence
            }
        }
        return ""
    }


    /**
     * @since 2021-08-05 21:18:10
     * 微信 1940 后 首页聊天列表中的item 字段全部变为私有 只能通过父类的class对象来设置
     */
    fun setupItemClassField(item: Any, fieldName: String, value: Any) {
        beanClass.superclass?.superclass?.let { clazz ->
            clazz.getDeclaredField(fieldName).apply { isAccessible = true }.set(item, value)
        }
    }

    /**
     * 0.1 代表图片圆角半径 越大越圆
     */
    fun getConversationAvatar(field_username: String, imageView: ImageView) =
            XposedHelpers.callStaticMethod(conversationAvatar, conversationAvatarMethod.name, imageView, field_username, 0.1f, false)


    fun getConversationNickname(adapter: Any, chatInfoModel: ChatInfoModel): CharSequence {

        val getNicknameMethod = conversationWithCacheAdapter.declaredMethods
                .filter { it.parameterTypes.size == 1 }
                .filter {
                    it.parameterTypes[0].simpleName == beanClass.simpleName
                }.single {
                    it.returnType.enclosingClass != null
                            && it.returnType.enclosingClass.name == conversationWithCacheAdapter.name
                }

        val aeConstructor = beanClass.constructors.filter { it.parameterTypes.size == 1 }
                .firstOrNull { it.parameterTypes[0] == String::class.java }!!

        val bean = aeConstructor.newInstance(chatInfoModel.field_username)

        val result = XposedHelpers.callMethod(adapter, getNicknameMethod.name, bean)
        val nicknameResult = XposedHelpers.getObjectField(result, "nickName") as CharSequence

        return if (nicknameResult.isEmpty()) "群聊" else nicknameResult
    }

    fun getConversationContent(adapter: Any, chatInfoModel: ChatInfoModel): CharSequence {

        val method = conversationWithCacheAdapter.declaredMethods
                .filter { it.parameterTypes.size >= 4 }
                .single {
                    it.parameterTypes[0].simpleName == beanClass.simpleName &&
                            it.parameterTypes[1].simpleName == Int::class.java.simpleName &&
                            it.parameterTypes[3].simpleName == Boolean::class.java.simpleName
                }

        val aeConstructor = beanClass.constructors.filter { it.parameterTypes.size == 1 }
                .firstOrNull { it.parameterTypes[0] == String::class.java }!!

        val bean = aeConstructor.newInstance(chatInfoModel.field_username)

        setupItemClassField(bean, "field_editingMsg", chatInfoModel.field_editingMsg)
        setupItemClassField(bean, "field_atCount", chatInfoModel.field_atCount)
        setupItemClassField(bean, "field_unReadCount", chatInfoModel.field_unReadCount)
        setupItemClassField(bean, "field_unReadMuteCount", chatInfoModel.field_unReadMuteCount)
        setupItemClassField(bean, "field_msgType", chatInfoModel.field_msgType)
        setupItemClassField(bean, "field_username", chatInfoModel.field_username)
        setupItemClassField(bean, "field_content", chatInfoModel.field_content)
        setupItemClassField(bean, "field_digest", chatInfoModel.field_digest)
        setupItemClassField(bean, "field_digestUser", chatInfoModel.field_digestUser)
        setupItemClassField(bean, "field_isSend", chatInfoModel.field_isSend)
        setupItemClassField(bean, "field_UnReadInvite", chatInfoModel.field_UnReadInvite)
        setupItemClassField(bean, "field_atCount", chatInfoModel.field_atCount)
        setupItemClassField(bean, "field_flag", chatInfoModel.field_flag)

        val textSize = (ScreenUtils.getScreenDensity() * 13f).toInt()
        val content = try {
            if (method.parameterTypes.size == 5) {
                val clazz = method.parameterTypes[4]
                val newInstance = clazz.newInstance()
                XposedHelpers.callMethod(adapter, method.name, bean, textSize, null, true, newInstance) as CharSequence
            } else {
                XposedHelpers.callMethod(adapter, method.name, bean, textSize, null, true) as CharSequence
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            ""
        }

//        LogUtils.log("MessageHook2019-04-01 16:25:57, flag = ${chatInfoModel.field_flag}, username = ${chatInfoModel.field_username}")
//        val secondBeanMethodName = conversationWithCacheAdapter.declaredMethods
//                .filter { it.parameterTypes.size == 1 }
//                .single {
//                    LogUtils.log("getConversationContent, name = $it, name = ${beanClass.name}")
//                    it.parameterTypes[0].name == beanClass.name
//                }.name
//
//        val secondBean = XposedHelpers.callMethod(adapter, secondBeanMethodName, beanClass)
//
//        val fields = secondBean::class.java.fields
//
//        var fieldString = ""
//        fields.forEach {
//            fieldString = "getConversationContent, fieldName = ${it.name}, fieldValue = ${it.get(secondBean)}\n"
//        }
//
//        com.zdy.project.wechat_chatroom_helper.LogUtils.log("getConversationContent = $fieldString")
        return if (content.isEmpty()) chatInfoModel.field_content else content
    }


}