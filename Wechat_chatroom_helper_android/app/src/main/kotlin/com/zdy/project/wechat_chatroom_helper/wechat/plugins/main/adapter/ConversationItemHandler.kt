package com.zdy.project.wechat_chatroom_helper.wechat.plugins.main.adapter

import android.widget.ImageView
import com.blankj.utilcode.util.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.ParameterizedType

object ConversationItemHandler {

    private val conversationWithCacheAdapter = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithCacheAdapter, PluginEntry.classloader)
    private val conversationAvatar = XposedHelpers.findClass(WXObject.Adapter.C.ConversationAvatar, PluginEntry.classloader)

    private val conversationTimeStringMethod = conversationWithCacheAdapter.declaredMethods
            .filter { !it.isAccessible }.filter { it.returnType == CharSequence::class.java }
            .first { it.parameterTypes.size == 1 }

    private val conversationAvatarMethod = conversationAvatar.methods
            .first { it.parameterTypes.isNotEmpty() && it.parameterTypes[0].name == ImageView::class.java.name }

    private val beanClass = ((conversationWithCacheAdapter.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<*>)
    private val beanConstructor = beanClass.constructors.filter { it.parameterTypes.size == 1 }.firstOrNull { it.parameterTypes[0] == String::class.java }


    fun getConversationTimeString(adapter: Any, conversationTime: Long): CharSequence {

        conversationTimeStringMethod.let {

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
            XposedHelpers.callStaticMethod(conversationAvatar, conversationAvatarMethod.name, imageView, field_username)


    fun getConversationContent(adapter: Any, chatInfoModel: ChatInfoModel): CharSequence? {

        val getContentMethod = conversationWithCacheAdapter.declaredMethods
                .filter { it.parameterTypes.size == 3 }
                .single {
                    it.parameterTypes[0].simpleName == beanClass.simpleName &&
                            it.parameterTypes[1].simpleName == Int::class.java.simpleName &&
                            it.parameterTypes[2].simpleName == Boolean::class.java.simpleName
                }

        val aeConstructor = beanClass.constructors.filter { it.parameterTypes.size == 1 }
                .firstOrNull { it.parameterTypes[0] == String::class.java }!!
        val ae = aeConstructor.newInstance(chatInfoModel.field_username)

        beanClass.getField("field_editingMsg").set(ae, chatInfoModel.field_editingMsg)
        beanClass.getField("field_atCount").set(ae, chatInfoModel.field_atCount)
        beanClass.getField("field_unReadCount").set(ae, chatInfoModel.field_unReadCount)
        beanClass.getField("field_unReadMuteCount").set(ae, chatInfoModel.field_unReadMuteCount)
        beanClass.getField("field_msgType").set(ae, chatInfoModel.field_msgType)
        beanClass.getField("field_username").set(ae, chatInfoModel.field_username)
        beanClass.getField("field_content").set(ae, chatInfoModel.field_content)
        beanClass.getField("field_digest").set(ae, chatInfoModel.field_digest)
        beanClass.getField("field_digestUser").set(ae, chatInfoModel.field_digestUser)
        beanClass.getField("field_isSend").set(ae, chatInfoModel.field_isSend)
        beanClass.getField("field_UnReadInvite").set(ae, chatInfoModel.field_UnReadInvite)
        beanClass.getField("field_atCount").set(ae, chatInfoModel.field_atCount)


        val textSize = (ScreenUtils.getScreenDensity() * 13f).toInt()


        val content = XposedHelpers.callMethod(adapter, getContentMethod.name, ae, textSize, true) as CharSequence

        LogUtils.log("getConversationContent,  content1 =  $content")
        LogUtils.log("getConversationContent,  content2 =  ${XposedHelpers.callMethod(adapter, getContentMethod.name, ae, textSize, false) as CharSequence}")


        return if (content.isEmpty()) null else content
    }

}