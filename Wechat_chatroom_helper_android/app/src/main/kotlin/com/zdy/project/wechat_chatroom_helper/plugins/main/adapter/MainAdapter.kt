package com.zdy.project.wechat_chatroom_helper.plugins.main.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.MessageEventNotifyListener
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageFactory
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.manager.AvatarMaker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

@SuppressLint("StaticFieldLeak")
/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainAdapter {

    lateinit var originAdapter: BaseAdapter
    private lateinit var listView: ListView

    private var firstChatRoomPosition = -1
    private var firstOfficialPosition = -1

    fun executeHook() {

        val conversationWithCacheAdapter = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithCacheAdapter, PluginEntry.classloader)
        val conversationWithAppBrandListView = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithAppBrandListView, PluginEntry.classloader)
        val conversationClickListener = XposedHelpers.findClass(WXObject.Adapter.C.ConversationClickListener, PluginEntry.classloader)
        val conversationWithCacheAdapter_getItem = conversationWithCacheAdapter.superclass.declaredMethods
                .filter { it.parameterTypes.size == 1 && it.parameterTypes[0] == Int::class.java }
                .first { it.name != "getItem" && it.name != "getItemId" }.name


        hookAllConstructors(conversationWithCacheAdapter, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val adapter = param.thisObject as? BaseAdapter ?: return
                originAdapter = adapter
            }
        })

        findAndHookMethod(conversationWithAppBrandListView, "setAdapter", ListAdapter::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                listView = param.thisObject as ListView

                PluginEntry.chatRoomViewPresenter.start()
                PluginEntry.officialViewPresenter.start()
            }
        })


        findAndHookMethod(conversationWithCacheAdapter.superclass, "getCount", object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                var count = param.result as Int + (if (firstChatRoomPosition != -1) 1 else 0)
                count += (if (firstOfficialPosition != -1) 1 else 0)
                param.result = count
            }
        })

        findAndHookMethod(conversationClickListener, "onItemClick",
                AdapterView::class.java, View::class.java, Int::class.java, Long::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val position = (param.args[2] as Int) - listView.headerViewsCount

                        val field_username = XposedHelpers.getObjectField(XposedHelpers.callMethod(originAdapter, conversationWithCacheAdapter_getItem, position), "field_username") as String

//                        XposedBridge.log("MessageHooker2.6,position = $position, field_username = $field_username, " +
//                                "firstChatRoomUserName = $firstChatRoomUserName ,firstOfficialUserName = $firstOfficialUserName \n")

                        if (position == firstChatRoomPosition) {

                            XposedBridge.log("MessageHooker2.6,position = $position, firstChatRoomUserName equal")

                            PluginEntry.chatRoomViewPresenter.show()

                            param.result = null
                        }
                        if (position == firstOfficialPosition) {

                            XposedBridge.log("MessageHooker2.6,position = $position, firstOfficialUserName equal")

                            PluginEntry.officialViewPresenter.show()

                            param.result = null
                        }


                    }
                })

        findAndHookMethod(conversationWithCacheAdapter, "getView",
                Int::class.java, View::class.java, ViewGroup::class.java,
                object : XC_MethodHook() {

                    override fun beforeHookedMethod(param: MethodHookParam) {

                        val position = param.args[0] as Int
                        val view = param.args[1] as View?

                        XposedBridge.log("MMBaseAdapter_getView, beforeHookedMethod, index = " + position)

                        if (view == null) {
                            /**
                             * 此时为第一次刷新，处理逻辑放在{@link XC_MethodHook.afterHookedMethod}
                             */
                        } else {
                            /**
                             * 后续刷新
                             * 一旦符合条件，只做相应自定义UI刷新，跳过微信处理逻辑
                             */
                            refreshEntryView(view, position, param)
                        }
                    }

//                    override fun afterHookedMethod(param: MethodHookParam) {
//
//                        val position = param.args[0] as Int
//
//                        if (param.result != null)
//                            refreshEntryView(param.result as View, position, param)
//                    }

                    private fun refreshEntryView(view: View?, position: Int, param: MethodHookParam) {
                        val itemView = view as ViewGroup

                        val avatarContainer = itemView.getChildAt(0) as ViewGroup
                        val contentContainer = itemView.getChildAt(1) as ViewGroup

                        val avatar = avatarContainer.getChildAt(0) as ImageView
                        val unReadCount = avatarContainer.getChildAt(1) as TextView
                        val unMuteReadIndicators = avatarContainer.getChildAt(2) as ImageView

                        val nickname = ((contentContainer.getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0)
                        val time = (contentContainer.getChildAt(0) as ViewGroup).getChildAt(1)

                        val content = ((contentContainer.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(1)

//                        val field_username = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.thisObject, MMBaseAdapter_getItemInternal, position), "field_username") as String

                        XposedBridge.log("MessageHooker2.6,position = $position, position = $position, " +
                                "firstChatRoomPosition = $firstChatRoomPosition ,firstOfficialPosition = $firstOfficialPosition \n")

                        if (position == firstChatRoomPosition) {
                            setTextForNoMeasuredTextView(nickname, "群消息")
                            setTextForNoMeasuredTextView(content, "")
                            avatar.post { avatar.setImageDrawable(AvatarMaker.handleAvatarDrawable(avatar, PageType.CHAT_ROOMS)) }
                            setTextForNoMeasuredTextView(time, ConversationItemHandler.getConversationTimeString(originAdapter, MessageFactory.getAllChatRoom().first().conversationTime))
                            param.result = view
                        }
                        if (position == firstOfficialPosition) {
                            setTextForNoMeasuredTextView(nickname, "服务号")
                            setTextForNoMeasuredTextView(content, "")
                            avatar.post { avatar.setImageDrawable(AvatarMaker.handleAvatarDrawable(avatar, PageType.OFFICIAL)) }
                            setTextForNoMeasuredTextView(time, ConversationItemHandler.getConversationTimeString(originAdapter, MessageFactory.getAllOfficial().first().conversationTime))
                            param.result = view

                        }
                    }
                })


        findAndHookMethod(conversationWithCacheAdapter.superclass, conversationWithCacheAdapter_getItem,
                Int::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return

                val index = param.args[0] as Int

                XposedBridge.log("MMBaseAdapter_getItemInternal, index = $index, firstChatRoomPosition = $firstChatRoomPosition ,firstOfficialPosition = $firstOfficialPosition")

                if (firstChatRoomPosition == -1 || firstOfficialPosition == -1) return

                val min = Math.min(firstChatRoomPosition, firstOfficialPosition)
                val max = Math.max(firstChatRoomPosition, firstOfficialPosition)

                val newIndex = when (index) {
                    in 0 until min -> index
                    min -> index //TODO
                    in min + 1 until max -> index - 1
                    max -> index //TODO
                    in max + 1 until Int.MAX_VALUE -> index - 2
                    else -> index
                }

                XposedBridge.log("MessageHooker2.7,size = ${originAdapter.count}, min = $min, max = $max, oldIndex = ${param.args[0]}, newIndex = $newIndex")

                param.args[0] = newIndex
            }
        })

        MessageHandler.addMessageEventNotifyListener(
                object : MessageEventNotifyListener {

                    override fun onNewMessageCreate(talker: String, createTime: Long, content: Any) {
                        super.onNewMessageCreate(talker, createTime, content)
                    }

                    override fun onEntryPositionChanged(chatroomPosition: Int, officialPosition: Int) {
                        super.onEntryPositionChanged(chatroomPosition, officialPosition)

                        if (firstOfficialPosition != officialPosition || firstChatRoomPosition == chatroomPosition) {
                            firstChatRoomPosition = chatroomPosition
                            firstOfficialPosition = officialPosition
                        }
                    }


                })
    }

    fun setTextForNoMeasuredTextView(noMeasuredTextView: Any, charSequence: CharSequence) = XposedHelpers.callMethod(noMeasuredTextView, "setText", charSequence)
    fun getTextFromNoMeasuredTextView(noMeasuredTextView: Any): CharSequence {
        val mTextField = XposedHelpers.findField(XposedHelpers.findClass("com.tencent.mm.ui.base.NoMeasuredTextView", PluginEntry.classloader), "mText")
        mTextField.isAccessible = true
        return mTextField.get(noMeasuredTextView) as CharSequence
    }
}