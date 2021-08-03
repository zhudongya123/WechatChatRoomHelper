package com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser

import android.util.Log
import android.widget.ImageView
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



    private val conversationTimeStringMethod = conversationWithCacheAdapter.declaredMethods
            .filter { !it.isAccessible }
            .filter { it.returnType == CharSequence::class.java }
            .first { it.parameterTypes.size == 1 }

    private val conversationAvatarMethod = conversationAvatar.methods
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

                beanClass.getField("field_status").set(obj, 0)
                beanClass.getField("field_conversationTime").set(obj, conversationTime)

                return XposedHelpers.callMethod(adapter, conversationTimeStringMethod.name, obj) as CharSequence
            }
        }
        return ""
    }


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

        Log.v("getConversationContent","beanClass = ${beanClass.simpleName}")
        val getContentMethod = conversationWithCacheAdapter.declaredMethods
                .filter { it.parameterTypes.size == 3 }
                .single {
                    it.parameterTypes[0].simpleName == beanClass.simpleName &&
                            it.parameterTypes[1].simpleName == Int::class.java.simpleName &&
                            it.parameterTypes[2].simpleName == Boolean::class.java.simpleName
                }

        val aeConstructor = beanClass.constructors.filter { it.parameterTypes.size == 1 }
                .firstOrNull { it.parameterTypes[0] == String::class.java }!!

        val bean = aeConstructor.newInstance(chatInfoModel.field_username)

        beanClass.getField("field_editingMsg").set(bean, chatInfoModel.field_editingMsg)
        beanClass.getField("field_atCount").set(bean, chatInfoModel.field_atCount)
        beanClass.getField("field_unReadCount").set(bean, chatInfoModel.field_unReadCount)
        beanClass.getField("field_unReadMuteCount").set(bean, chatInfoModel.field_unReadMuteCount)
        beanClass.getField("field_msgType").set(bean, chatInfoModel.field_msgType)
        beanClass.getField("field_username").set(bean, chatInfoModel.field_username)
        beanClass.getField("field_content").set(bean, chatInfoModel.field_content)
        beanClass.getField("field_digest").set(bean, chatInfoModel.field_digest)
        beanClass.getField("field_digestUser").set(bean, chatInfoModel.field_digestUser)
        beanClass.getField("field_isSend").set(bean, chatInfoModel.field_isSend)
        beanClass.getField("field_UnReadInvite").set(bean, chatInfoModel.field_UnReadInvite)
        beanClass.getField("field_atCount").set(bean, chatInfoModel.field_atCount)
        beanClass.getField("field_flag").set(bean, chatInfoModel.field_flag)

        val textSize = (ScreenUtils.getScreenDensity() * 13f).toInt()
        val content = try {
            XposedHelpers.callMethod(adapter, getContentMethod.name, bean, textSize, true) as CharSequence
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