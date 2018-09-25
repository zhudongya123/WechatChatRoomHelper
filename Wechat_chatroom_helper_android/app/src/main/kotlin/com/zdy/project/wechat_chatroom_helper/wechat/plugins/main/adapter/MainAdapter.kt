package com.zdy.project.wechat_chatroom_helper.wechat.plugins.main.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.interfaces.MessageEventNotifyListener
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.message.MessageFactory
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.manager.AvatarMaker
import de.robv.android.xposed.XC_MethodHook
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
        ConversationItemHandler
        val conversationWithCacheAdapter = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithCacheAdapter, PluginEntry.classloader)
        val conversationWithAppBrandListView = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithAppBrandListView, PluginEntry.classloader)
        val conversationClickListener = XposedHelpers.findClass(WXObject.Adapter.C.ConversationClickListener, PluginEntry.classloader)
        val conversationWithCacheAdapterGetItem = conversationWithCacheAdapter.superclass.declaredMethods
                .filter { it.parameterTypes.size == 1 && it.parameterTypes[0] == Int::class.java }
                .first { it.name != "getItem" && it.name != "getItemId" }.name


        hookAllConstructors(conversationWithCacheAdapter, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val adapter = param.thisObject as? BaseAdapter ?: return
                originAdapter = adapter
            }
        })

        findAndHookMethod(conversationWithAppBrandListView, WXObject.Adapter.M.SetAdapter, ListAdapter::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                listView = param.thisObject as ListView
                val adapter = param.args[0]

                PluginEntry.chatRoomViewPresenter.setAdapter(adapter)
                PluginEntry.officialViewPresenter.setAdapter(adapter)

                PluginEntry.chatRoomViewPresenter.start()
                PluginEntry.officialViewPresenter.start()
            }
        })


        findAndHookMethod(conversationWithCacheAdapter.superclass, WXObject.Adapter.M.GetCount, object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                var count = param.result as Int + (if (firstChatRoomPosition != -1) 1 else 0)
                count += (if (firstOfficialPosition != -1) 1 else 0)
                param.result = count
            }
        })

        findAndHookMethod(conversationClickListener, WXObject.Adapter.M.OnItemClick,
                AdapterView::class.java, View::class.java, Int::class.java, Long::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val position = (param.args[2] as Int) - listView.headerViewsCount

                        val field_username = XposedHelpers.getObjectField(XposedHelpers.callMethod(originAdapter, conversationWithCacheAdapterGetItem, position), "field_username") as String
//                        LogUtils.log("MessageHooker2.6,position = $position, field_username = $field_username, " +
//                                "firstChatRoomUserName = $firstChatRoomUserName ,firstOfficialUserName = $firstOfficialUserName \n")

                        if (position == firstChatRoomPosition) {

                            //    LogUtils.log("MessageHooker2.6,position = $position, firstChatRoomUserName equal")
                            PluginEntry.chatRoomViewPresenter.show()
                            param.result = null
                        }
                        if (position == firstOfficialPosition) {

                            //      LogUtils.log("MessageHooker2.6,position = $position, firstOfficialUserName equal")
                            PluginEntry.officialViewPresenter.show()
                            param.result = null
                        }
                    }
                })

        findAndHookMethod(conversationWithCacheAdapter, WXObject.Adapter.M.GetView,
                Int::class.java, View::class.java, ViewGroup::class.java,
                object : XC_MethodHook() {

                    override fun beforeHookedMethod(param: MethodHookParam) {

                        val position = param.args[0] as Int
                        val view = param.args[1] as View?

                        LogUtils.log("MMBaseAdapter_getView, beforeHookedMethod, index = " + position + ", view = $view")

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

                    override fun afterHookedMethod(param: MethodHookParam) {
                        val position = param.args[0] as Int
                        val view = param.args[1] as View?

                        LogUtils.log("MMBaseAdapter_getView, afterHookedMethod, index = $position, view = $view")

                        if (view != null)
                            refreshEntryView(view, position, param)
                    }

                    private fun refreshEntryView(view: View?, position: Int, param: MethodHookParam) {
                        LogUtils.log("MessageHooker2.6,position = $position, position = $position, " +
                                "firstChatRoomPosition = $firstChatRoomPosition ,firstOfficialPosition = $firstOfficialPosition \n")

                        val itemView = view as ViewGroup

                        val avatarContainer = itemView.getChildAt(0) as ViewGroup
                        val contentContainer = itemView.getChildAt(1) as ViewGroup

                        val avatar = avatarContainer.getChildAt(0) as ImageView
                        val unReadCount = avatarContainer.getChildAt(1) as TextView
                        val unMuteReadIndicators = avatarContainer.getChildAt(2) as ImageView

                        val nickname = ((contentContainer.getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0)
                        val time = (contentContainer.getChildAt(0) as ViewGroup).getChildAt(1)

                        val content = ((contentContainer.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(1)

                        if (position == firstChatRoomPosition) {
                            unReadCount.visibility = View.GONE
                            unMuteReadIndicators.visibility = View.GONE

                            val allChatRoom = MessageFactory.getSpecChatRoom()
                            val unReadCountItem = MessageFactory.getUnReadCountItem(allChatRoom)
                            val totalUnReadCount = MessageFactory.getUnReadCount(allChatRoom)


                            setTextForNoMeasuredTextView(nickname, "群消息")
                            setTextForNoMeasuredTextView(time, allChatRoom.first().conversationTime)
                            avatar.setImageDrawable(AvatarMaker.handleAvatarDrawable(avatar.context, PageType.CHAT_ROOMS))

                            LogUtils.log("getUnReadCountItemChatRoom " + allChatRoom.joinToString { "unReadCount = ${it.unReadCount}" })

                            if (unReadCountItem > 0) {
                                setTextForNoMeasuredTextView(content, "[有${unReadCountItem}个群聊收到${totalUnReadCount}条新消息]")
                                setTextColorForNoMeasuredTextView(content, 0xFFF57C00.toInt())
                                unMuteReadIndicators.visibility = View.VISIBLE
                            } else {
                                setTextColorForNoMeasuredTextView(content, 0xFF999999.toInt())
                                setTextForNoMeasuredTextView(content, "${allChatRoom.first().nickname}：${allChatRoom.first().content}")
                                unMuteReadIndicators.visibility = View.GONE
                            }

                            param.result = view
                        }
                        if (position == firstOfficialPosition) {
                            unReadCount.visibility = View.GONE
                            unMuteReadIndicators.visibility = View.GONE

                            val allOfficial = MessageFactory.getSpecOfficial()
                            val unReadCountItem = MessageFactory.getUnReadCountItem(allOfficial)
                            val totalUnReadCount = MessageFactory.getUnReadCount(allOfficial)


                            setTextForNoMeasuredTextView(nickname, "服务号")
                            setTextForNoMeasuredTextView(time, allOfficial.first().conversationTime)
                            avatar.setImageDrawable(AvatarMaker.handleAvatarDrawable(avatar.context, PageType.OFFICIAL))

                            LogUtils.log("getUnReadCountItemChatRoom " + allOfficial.joinToString { "unReadCount = ${it.unReadCount}" })

                            if (unReadCountItem > 0) {
                                setTextForNoMeasuredTextView(content, "[有${unReadCountItem}个服务号收到${totalUnReadCount}条新消息]")
                                setTextColorForNoMeasuredTextView(content, 0xFFF57C00.toInt())
                                unMuteReadIndicators.visibility = View.VISIBLE
                            } else {
                                setTextForNoMeasuredTextView(content, "${allOfficial.first().nickname}：${allOfficial.first().content}")
                                setTextColorForNoMeasuredTextView(content, 0xFF999999.toInt())
                                unMuteReadIndicators.visibility = View.GONE

                            }

                            param.result = view
                        }
                    }
                })


        /**
         * 修改 getObject 的数据下标
         */
        findAndHookMethod(conversationWithCacheAdapter.superclass, conversationWithCacheAdapterGetItem,
                Int::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {
                LogUtils.log("MessageHooker2.7,size = ${originAdapter.count}")

                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return

                val index = param.args[0] as Int

                val min = Math.min(firstChatRoomPosition, firstOfficialPosition)
                val max = Math.max(firstChatRoomPosition, firstOfficialPosition)

                val newIndex =
                        if (min == -1 && max == -1) {
                            index
                        } else if (min == -1 || max == -1) {
                            when (index) {
                                in 0 until max -> index
                                max -> index //TODO
                                in max + 1 until Int.MAX_VALUE -> index - 1
                                else -> index //TODO
                            }
                        } else {
                            when (index) {
                                in 0 until min -> index
                                min -> index //TODO
                                in min + 1 until max -> index - 1
                                max -> index //TODO
                                in max + 1 until Int.MAX_VALUE -> index - 2
                                else -> index //TODO
                            }
                        }

                param.args[0] = newIndex

                LogUtils.log("MessageHooker2.7,size = ${originAdapter.count}, min = $min, max = $max, oldIndex = ${param.args[0]}, newIndex = $newIndex")
            }
        })

        MessageHandler.addMessageEventNotifyListener(
                object : MessageEventNotifyListener {

                    override fun onNewMessageCreate(talker: String, createTime: Long, content: Any) {
                        super.onNewMessageCreate(talker, createTime, content)
                    }

                    override fun onEntryPositionChanged(chatroomPosition: Int, officialPosition: Int) {
                        super.onEntryPositionChanged(chatroomPosition, officialPosition)

                        if (firstOfficialPosition != officialPosition || firstChatRoomPosition != chatroomPosition) {
                            firstChatRoomPosition = chatroomPosition
                            firstOfficialPosition = officialPosition
                        }
                    }


                })
    }

    fun setTextForNoMeasuredTextView(noMeasuredTextView: Any, charSequence: CharSequence) = XposedHelpers.callMethod(noMeasuredTextView, "setText", charSequence)
    fun setTextColorForNoMeasuredTextView(noMeasuredTextView: Any, color: Int) = XposedHelpers.callMethod(noMeasuredTextView, "setTextColor", color)

    fun getTextFromNoMeasuredTextView(noMeasuredTextView: Any): CharSequence {
        val mTextField = XposedHelpers.findField(XposedHelpers.findClass("com.tencent.mm.ui.base.NoMeasuredTextView", PluginEntry.classloader), "mText")
        mTextField.isAccessible = true
        return mTextField.get(noMeasuredTextView) as CharSequence
    }
}