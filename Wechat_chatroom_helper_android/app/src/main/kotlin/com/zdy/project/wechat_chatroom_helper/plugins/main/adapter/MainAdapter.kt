package com.zdy.project.wechat_chatroom_helper.plugins.main.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.ui.Methods.MMBaseAdapter_getItemInternal
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.ui.conversation.Classes.ConversationWithCacheAdapter
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.MessageEventNotifyListener
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageFactory
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.WCRHClasses
import com.zdy.project.wechat_chatroom_helper.wechat.manager.AvatarMaker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

@SuppressLint("StaticFieldLeak")
/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainAdapter {

    lateinit var originAdapter: BaseAdapter
    private lateinit var listView: ListView


    var firstChatroomUserName = ""
    var firstOfficialUserName = ""


    private var firstChatroomPosition = -1
    private var firstOfficialPosition = -1

    fun executeHook() {

        val conversationWithCacheAdapter = XposedHelpers.findClass(WCRHClasses.ConversationWithCacheAdapter, PluginEntry.classloader)
        val conversationWithAppBrandListView = XposedHelpers.findClass(WCRHClasses.ConversationWithAppBrandListView, PluginEntry.classloader)
        val conversationClickListener =XposedHelpers.findClass(WCRHClasses.ConversationClickListener,PluginEntry.classloader)

        XposedBridge.hookAllConstructors(ConversationWithCacheAdapter, object : XC_MethodHook() {
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
                var count = param.result as Int + (if (firstChatroomPosition != -1) 1 else 0)
                count += (if (firstOfficialPosition != -1) 1 else 0)
                param.result = count
            }
        })

        findAndHookMethod(conversationClickListener, "onItemClick", C.AdapterView, C.View, C.Int, C.Long, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val position = (param.args[2] as Int) - listView.headerViewsCount

                val field_username = XposedHelpers.getObjectField(XposedHelpers.callMethod(originAdapter, MMBaseAdapter_getItemInternal, position), "field_username") as String

                XposedBridge.log("MessageHooker2.6,position = $position, field_username = $field_username, " +
                        "firstChatroomUserName = $firstChatroomUserName ,firstOfficialUserName = $firstOfficialUserName \n")

                if (position == firstChatroomPosition) {

                    XposedBridge.log("MessageHooker2.6,position = $position, firstChatroomUserName equal")

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

//                    override fun beforeHookedMethod(param: MethodHookParam) {
//
//                        val position = param.args[0] as Int
//                        val view = param.args[1] as View?
//
//                        XposedBridge.log("MMBaseAdapter_getView, beforeHookedMethod, index = " + position)
//
//                        if (view == null) {
//                            /**
//                             * 此时为第一次刷新，处理逻辑放在{@link XC_MethodHook.afterHookedMethod}
//                             */
//                        } else {
//                            /**
//                             * 后续刷新
//                             * 一旦符合条件，只做相应自定义UI刷新，跳过微信处理逻辑
//                             */
//                            refreshEntryView(view, position, param)
//                        }
//                    }

                    override fun afterHookedMethod(param: MethodHookParam) {

                        val position = param.args[0] as Int

                        if (param.result != null)
                            refreshEntryView(param.result as View, position)
                    }

                    private fun refreshEntryView(view: View?, position: Int) {
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
                                "firstChatroomPosition = $firstChatroomPosition ,firstOfficialPosition = $firstOfficialPosition \n")

                        if (position == firstChatroomPosition) {
                            setTextForNoMeasuredTextView(nickname, "群消息")
                            setTextForNoMeasuredTextView(content, "")
                            avatar.post { avatar.setImageDrawable(AvatarMaker.handleAvatarDrawable(avatar, PageType.CHAT_ROOMS)) }
                            setTextForNoMeasuredTextView(time, ConversationItemHandler.getConversationTimeString(originAdapter, MessageFactory.getSingle(firstChatroomUserName).conversationTime))

                        }
                        if (position == firstOfficialPosition) {
                            setTextForNoMeasuredTextView(nickname, "服务号")
                            setTextForNoMeasuredTextView(content, "")
                            avatar.post { avatar.setImageDrawable(AvatarMaker.handleAvatarDrawable(avatar, PageType.OFFICIAL)) }
                            setTextForNoMeasuredTextView(time, ConversationItemHandler.getConversationTimeString(originAdapter, MessageFactory.getSingle(firstOfficialUserName).conversationTime))
                        }

                    }

                })


        findAndHookMethod(conversationWithCacheAdapter.superclass, MMBaseAdapter_getItemInternal,
                Int::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return

                val index = param.args[0] as Int

                XposedBridge.log("MMBaseAdapter_getItemInternal, index = $index, firstChatroomPosition = $firstChatroomPosition ,firstOfficialPosition = $firstOfficialPosition")

                if (firstChatroomPosition == -1 || firstOfficialPosition == -1) return

                val min = Math.min(firstChatroomPosition, firstOfficialPosition)
                val max = Math.max(firstChatroomPosition, firstOfficialPosition)

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
                    override fun onEntryRefresh(chatRoomUsername: String, officialUsername: String) {
                        super.onEntryRefresh(chatRoomUsername, officialUsername)
//                        refreshFlag = true
//
//                        if (firstChatroomUserName == chatRoomUsername) {
//                            refreshFlag = false
//
//                            listView.post { updateItem(listView.headerViewsCount, listView) }
//                        }
//
//                        if (firstOfficialUserName == officialUsername) {
//                            refreshFlag = false
//
//                            listView.post { updateItem(listView.headerViewsCount, listView) }
//                        }

                        this@MainAdapter.firstChatroomUserName = chatRoomUsername
                        this@MainAdapter.firstOfficialUserName = officialUsername

                    }


                    override fun onEntryInit(chatRoomUsername: String, officialUsername: String) {
                        super.onEntryInit(chatRoomUsername, officialUsername)

                        this@MainAdapter.firstChatroomUserName = chatRoomUsername
                        this@MainAdapter.firstOfficialUserName = officialUsername
                    }

                    override fun onNewMessageCreate(talker: String, createTime: Long, content: Any) {
                        super.onNewMessageCreate(talker, createTime, content)


                    }

                    override fun onEntryPositionChanged(chatroomPosition: Int, officialPosition: Int) {
                        super.onEntryPositionChanged(chatroomPosition, officialPosition)

                        if (firstOfficialPosition != officialPosition || firstChatroomPosition == chatroomPosition)
//                                originAdapter.notifyDataSetChanged()

                            firstChatroomPosition = chatroomPosition
                        firstOfficialPosition = officialPosition
                    }


                    /**
                     *
                     * notifyDataSetChanged 之后会调用
                     *
                     *
                     */
                })
    }

    private fun updateItem(position: Int, listView: ListView) {
        /**第一个可见的位置 */
        val firstVisiblePosition = listView.getFirstVisiblePosition()
        /**最后一个可见的位置 */
        val lastVisiblePosition = listView.getLastVisiblePosition()

        /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新 */
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            /**获取指定位置view对象 */
            val view = listView.getChildAt(position - firstVisiblePosition)
            originAdapter.getView(position, view, listView)
        }

    }


    fun setTextForNoMeasuredTextView(noMeasuredTextView: Any, charSequence: CharSequence) = XposedHelpers.callMethod(noMeasuredTextView, "setText", charSequence)
    fun getTextFromNoMeasuredTextView(noMeasuredTextView: Any): CharSequence {
        val mTextField = XposedHelpers.findField(XposedHelpers.findClass("com.tencent.mm.ui.base.NoMeasuredTextView", PluginEntry.classloader), "mText")
        mTextField.isAccessible = true
        return mTextField.get(noMeasuredTextView) as CharSequence
    }
}