package com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser

import android.widget.ImageView
import com.blankj.utilcode.util.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

object ConversationReflectFunction {

    val conversationWithCacheAdapter: Class<*> = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithCacheAdapter, RuntimeInfo.classloader)
    val conversationListView: Class<*> = XposedHelpers.findClass(WXObject.Adapter.C.ConversationListView, RuntimeInfo.classloader)
    val conversationClickListener: Class<*> = XposedHelpers.findClass(WXObject.Adapter.C.ConversationClickListener, RuntimeInfo.classloader)
    val conversationStickyHeaderHandler: Class<*> = XposedHelpers.findClass(WXObject.Adapter.C.ConversationStickyHeaderHandler, RuntimeInfo.classloader)
    val mStorageExClass: Class<*> = XposedHelpers.findClass(WXObject.Adapter.C.MStorageEx, RuntimeInfo.classloader)

    private val conversationHashMapBean: Class<*> = XposedHelpers.findClass(WXObject.Adapter.C.ConversationHashMapBean, RuntimeInfo.classloader)
    private val conversationAvatar: Class<*> = XposedHelpers.findClass(WXObject.Adapter.C.ConversationAvatar, RuntimeInfo.classloader)

    /**
     * 这个就是获取itemView里面的model的那个对象的方法 一般命名为getItem(index) 方法
     * 早几年微信不这么命名 而是自己混淆了一个方法 现在又回来了 不混淆了
     * 所以微信就是一个傻逼
     */
    val conversationWithCacheAdapterGetItemMethodName: String = conversationWithCacheAdapter.superclass.methods
            .filter { it.parameterTypes.size == 1 && it.parameterTypes[0] == Int::class.java }
            .filter { it.returnType != Int::class.java }
            .first { it.name != "getItemId" }.name


    val notifyPartialConversationListMethodName: String = conversationWithCacheAdapter.methods
            .filter { it.parameterTypes.size == 3 }
            .filter { it.parameterTypes[1] == Int::class.java }
            .first { it.parameterTypes[2] == Boolean::class.java }.name

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

    val beanClass = ((conversationWithCacheAdapter.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<*>)
    val beanConstructor: Constructor<*> = beanClass.constructors
            .filter { it.parameterTypes.size == 1 }
            .first { it.parameterTypes[0] == String::class.java }

    val stickyHeaderHandlerMethod: Method = conversationStickyHeaderHandler.methods.first { it.parameterTypes.size == 3 }


    fun getConversationTimeString(adapter: Any, conversationTime: Long): CharSequence {

        conversationTimeStringMethod.let { _ ->

            beanConstructor.let {
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
//        beanClass.superclass.superclass.let {
//            when (value) {
//                is Int -> {
//                    XposedHelpers.setIntField(item, fieldName, value)
//                }
//                is Long -> {
//                    XposedHelpers.setLongField(item, fieldName, value)
//                }
//                else -> {
//                    XposedHelpers.setObjectField(item, fieldName, value)
//                }
//            }
//        }

        beanClass.superclass?.superclass?.let { clazz ->
            clazz.getDeclaredField(fieldName).apply { isAccessible = true }.set(item, value)
        }


//            try {
//                clazz.getField(fieldName).set(item, value)
//            } catch (e: NoSuchFieldException) {
//                e.printStackTrace()
//            }
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
                .filter { it.parameterTypes.size == 6 }
                .first {
                    it.parameterTypes.any { type -> type.simpleName == beanClass.simpleName } &&
                            it.parameterTypes.any { type -> type.simpleName == Int::class.java.simpleName } &&
                            it.parameterTypes.any { type -> type.simpleName == Boolean::class.java.simpleName }
                }

        val aeConstructor = beanClass.constructors.filter { it.parameterTypes.size == 1 }
                .firstOrNull { it.parameterTypes[0] == String::class.java }!!

        val bean = aeConstructor.newInstance(chatInfoModel.field_username)

        //setupItemClassField(bean, "field_UnDeliverCount", chatInfoModel.field_UnDeliverCount)
        setupItemClassField(bean, "field_UnReadInvite", chatInfoModel.field_UnReadInvite)
        setupItemClassField(bean, "field_atCount", chatInfoModel.field_atCount)
        setupItemClassField(bean, "field_attrflag", chatInfoModel.field_attrflag)
        //setupItemClassField(bean, "field_chatmode", chatInfoModel.field_chatmode)
        setupItemClassField(bean, "field_content", chatInfoModel.field_content)
        setupItemClassField(bean, "field_conversationTime", chatInfoModel.field_conversationTime)
        setupItemClassField(bean, "field_digest", chatInfoModel.field_digest)
        setupItemClassField(bean, "field_digestUser", chatInfoModel.field_digestUser)
        setupItemClassField(bean, "field_editingMsg", chatInfoModel.field_editingMsg)
        //setupItemClassField(bean, "field_editingQuoteMsgId", chatInfoModel.field_editingQuoteMsgId)
        //setupItemClassField(bean, "field_firstUnDeliverSeq", chatInfoModel.field_firstUnDeliverSeq)
        setupItemClassField(bean, "field_flag", chatInfoModel.field_flag)
        //setupItemClassField(bean, "field_hasSpecialFollow", chatInfoModel.field_hasSpecialFollow)
        //setupItemClassField(bean, "field_hasTodo", chatInfoModel.field_hasTodo)
        //setupItemClassField(bean, "field_hbMarkRed", chatInfoModel.field_hbMarkRed)
        setupItemClassField(bean, "field_isSend", chatInfoModel.field_isSend)
        //setupItemClassField(bean, "field_lastSeq", chatInfoModel.field_lastSeq)
        //setupItemClassField(bean, "field_msgCount", chatInfoModel.field_msgCount)
        setupItemClassField(bean, "field_msgType", chatInfoModel.field_msgType)
        //setupItemClassField(bean, "field_parentRef", chatInfoModel.field_parentRef)
        //setupItemClassField(bean, "field_remitMarkRed", chatInfoModel.field_remitMarkRed)
        //setupItemClassField(bean, "field_showTips", chatInfoModel.field_showTips)
        setupItemClassField(bean, "field_status", chatInfoModel.field_status)
        setupItemClassField(bean, "field_unReadCount", chatInfoModel.field_unReadCount)
        setupItemClassField(bean, "field_unReadMuteCount", chatInfoModel.field_unReadMuteCount)
        setupItemClassField(bean, "field_username", chatInfoModel.field_username)
        val textSize = (ScreenUtils.getScreenDensity() * 13f).toInt()
        val content = try {
            val clazzHas = method.parameterTypes[5]
            val hasNewInstance = clazzHas.newInstance()
            //(bd bdVar, d dVar, int i2, e eVar, boolean z, gtz gtz)
            //(bd bdVar, d dVar, int i2, SpannableStringBuilder spannableStringBuilder, CharSequence charSequence, boolean z, gtz gtz)
            // a(com.tencent.mm.storage.be beVar,
            // com.tencent.mm.ui.conversation.k.d dVar,
            // int i2,
            // com.tencent.mm.ui.conversation.k.e eVar,
            // boolean z,
            // com.tencent.mm.protocal.protobuf.has has) {

//val charSequence = XposedHelpers.callMethod(adapter, method.name, bean, null, textSize, SpannableStringBuilder(), chatInfoModel.field_content, true, newInstance) as CharSequence

            /**
             * for8027
             */
            val charSequence = XposedHelpers.callMethod(adapter, method.name, bean, null, textSize, null, true, hasNewInstance) as CharSequence
            LogUtils.log("getConversationContent = $charSequence")
            return charSequence
        } catch (e: Throwable) {
            e.printStackTrace()
            ""
        }

        return if (content.isEmpty()) chatInfoModel.field_content else content
    }


}