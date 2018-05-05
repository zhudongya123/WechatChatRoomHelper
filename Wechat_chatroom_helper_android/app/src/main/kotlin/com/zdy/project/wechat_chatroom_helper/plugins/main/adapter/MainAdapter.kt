package com.zdy.project.wechat_chatroom_helper.plugins.main.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.Methods.MMBaseAdapter_getItemInternal
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.conversation.Classes
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.MessageEventNotifyListener
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.Classes.ClassesByCursor
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.Classes.ConversationClickListener
import com.zdy.project.wechat_chatroom_helper.plugins.main.adapter.Classes.ConversationWithAppBrandListView
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageHandler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

@SuppressLint("StaticFieldLeak")
/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainAdapter : IAdapterHook {

    private var originAdapter: BaseAdapter? = null
    private var listView: ListView? = null

    private var firstChatroomPosition = -1
    private var firstOfficialPosition = -1

    var firstChatroomUserName = ""
    var firstOfficialUserName = ""


    override fun onConversationAdapterCreated(adapter: BaseAdapter) {
        super.onConversationAdapterCreated(adapter)
        originAdapter = adapter
    }

    fun executeHook() {

        val conversationWithCacheAdapter = Classes.ConversationWithCacheAdapter

        findAndHookMethod(ConversationWithAppBrandListView, "setAdapter", ListAdapter::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                listView = param.thisObject as ListView
            }
        })

        ClassesByCursor.forEach { XposedBridge.log("MessageHooker2.11, className = ${it.name}") }


        ClassesByCursor.forEach {
            findAndHookMethod(it, "getCount", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    //    XposedBridge.log("MessageHooker2.15, className = ${param.thisObject::class.java.name} getCount = ${param.result}")

                }
            })
        }


        findAndHookMethod(conversationWithCacheAdapter.superclass, "getCount", object : XC_MethodHook() {

//            override fun beforeHookedMethod(param: MethodHookParam) {
//                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return
//
//                param.result = MessageHandler.conversationSize
//            }

//            override fun afterHookedMethod(param: MethodHookParam) {
//                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return
//
//
//                XposedBridge.log("MessageHooker2.10, getCount")
//
//                param.result = param.result as Int + 2
//            }

        })

        findAndHookMethod(conversationWithCacheAdapter, "notifyDataSetChanged", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                XposedBridge.log("MessageHooker2.10, notifyDataSetChanged")

            }
        })

        findAndHookMethod(ConversationClickListener, "onItemClick", C.AdapterView, C.View, C.Int, C.Long, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val position = (param.args[2] as Int) - listView!!.headerViewsCount


//                var field = XposedHelpers.findFirstFieldByExactType(param.thisObject.javaClass, conversationWithCacheAdapter)
//
//                var get = field.get(param.thisObject)

                val field_username = XposedHelpers.getObjectField(XposedHelpers.callMethod(originAdapter, MMBaseAdapter_getItemInternal, position), "field_username") as String

                XposedBridge.log("MessageHooker2.6,position = $position, field_username = $field_username, " +
                        "firstChatroomUserName = $firstChatroomUserName ,firstOfficialUserName = $firstOfficialUserName \n")

                if (field_username == firstChatroomUserName) {

                    XposedBridge.log("MessageHooker2.6,position = $position, firstChatroomUserName equal")

                    PluginEntry.chatRoomViewPresenter.show()

                    param.result = null
                }
                if (field_username == firstOfficialUserName) {

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

                        refreshEntryView(param.result as View, position, param)
                    }

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

                        val field_username = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.thisObject, MMBaseAdapter_getItemInternal, position), "field_username") as String

//                        XposedBridge.log("MessageHooker2.6,position = $position, field_username = $field_username, " +
//                                "firstChatroomUserName = $firstChatroomUserName ,firstOfficialUserName = $firstOfficialUserName \n")

                        if (field_username == firstChatroomUserName) {

                            setTextForNoMeasuredTextView(nickname, "群消息")
                            setTextForNoMeasuredTextView(content, "")
                            avatar.setImageDrawable(BitmapDrawable())

                        }
                        if (field_username == firstOfficialUserName) {

                            setTextForNoMeasuredTextView(nickname, "服务号")
                            setTextForNoMeasuredTextView(content, "")
                            avatar.setImageDrawable(BitmapDrawable())

                        }
                    }

                })


//        findAndHookMethod(conversationWithCacheAdapter.superclass, MMBaseAdapter_getItemInternal,
//                Int::class.java, object : XC_MethodHook() {
//
//            override fun beforeHookedMethod(param: MethodHookParam) {
//
//                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return
//
//                val index = param.args[0] as Int
//
//                XposedBridge.log("MMBaseAdapter_getItemInternal, index = $index, firstChatroomPosition = $firstChatroomPosition ,firstOfficialPosition = $firstOfficialPosition")
//
//                if (firstChatroomPosition == -1 || firstOfficialPosition == -1) return
//
//                val min = min(firstChatroomPosition, firstOfficialPosition)
//                val max = max(firstChatroomPosition, firstOfficialPosition)
//
//                val newIndex = when (index) {
//                    in 0 until min -> index
//                    min -> index //TODO
//                    in min + 1 until max -> index - 1
//                    max -> index //TODO
//                    in max + 1 until Int.MAX_VALUE -> index - 2
//                    else -> index
//                }
//
//                XposedBridge.log("MessageHooker2.7, min = $min, max = $max, oldIndex = ${param.args[0]}, newIndex = $newIndex")
//
//
//                param.args[0] = newIndex
//
//
//            }
//        })

        MessageHandler.addMessageEventNotifyListener(
                object : MessageEventNotifyListener {
                    override fun onEntryRefresh(chatRoomUsername: String, officialUsername: String) {

                        this@MainAdapter.firstChatroomUserName = chatRoomUsername
                        this@MainAdapter.firstOfficialUserName = officialUsername

                    }

                    override fun onNewMessageCreate(talker: String, createTime: Long, content: Any) {

                    }


                    fun onFirstChatroomRefresh(chatRoomPosition: Int, chatRoomChatInfoModel: ChatInfoModel, officialPosition: Int, officialChatInfoModel: ChatInfoModel) {

                        firstChatroomPosition = chatRoomPosition
                        firstOfficialPosition = officialPosition

//                firstChatroomInfoModel = chatRoomChatInfoModel
//                firstOfficialInfoModel = officialChatInfoModel


                        /**
                         *
                         * notifyDataSetChanged 之后会调用
                         *
                         *
                         */
//                originAdapter?.let {
//                    it.notifyDataSetChanged()
//                }

//                updateItem(firstChatroomPosition, listView!!)
//                updateItem(firstOfficialPosition, listView!!)


//                        XposedBridge.log("MessageHooker2.6, firstChatroomPosition = $chatRoomPosition \n")
//                        XposedBridge.log("MessageHooker2.6, firstOfficialPosition = $officialPosition \n")
                    }
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
            originAdapter?.getView(position, view, listView)
        }

    }


    fun setTextForNoMeasuredTextView(noMeasuredTextView: Any, charSequence: CharSequence) = XposedHelpers.callMethod(noMeasuredTextView, "setText", charSequence)
    fun getTextFromNoMeasuredTextView(noMeasuredTextView: Any): CharSequence {
        val mTextField = XposedHelpers.findField(XposedHelpers.findClass("com.tencent.mm.ui.base.NoMeasuredTextView", PluginEntry.classloader), "mText")
        mTextField.isAccessible = true
        return mTextField.get(noMeasuredTextView) as CharSequence
    }
}