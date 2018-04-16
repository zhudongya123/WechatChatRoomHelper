package com.zdy.project.wechat_chatroom_helper.plugins.main.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.Methods.MMBaseAdapter_getItemInternal
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.conversation.Classes
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.IMainAdapterHelperEntryRefresh
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainAdapter : IAdapterHook {

    var originAdapter: BaseAdapter? = null

    private var firstChatroomPosition = -1
    private var firstOfficialPosition = -1

    lateinit var firstChatroomInfoModel: ChatInfoModel
    lateinit var firstOfficialInfoModel: ChatInfoModel


    override fun onConversationAdapterCreated(adapter: BaseAdapter) {
        super.onConversationAdapterCreated(adapter)
        originAdapter = adapter
    }

    fun executeHook() {

        val conversationWithCacheAdapter = Classes.ConversationWithCacheAdapter

        findAndHookMethod(conversationWithCacheAdapter.superclass, "getCount", object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return

                param.result = param.result as Int + 2
            }

        })

        findAndHookMethod(conversationWithCacheAdapter, "notifyDataSetChanged", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {

            }
        })

        findAndHookMethod(conversationWithCacheAdapter, "getView",
                Int::class.java, View::class.java, ViewGroup::class.java,
                object : XC_MethodHook() {

                    override fun afterHookedMethod(param: MethodHookParam) {

                        val position = param.args[0] as Int
                        val view = param.args[1] as View?
                        val viewGroup = param.args[2] as ViewGroup

                        XposedBridge.log("MMBaseAdapter_getView, index = " + position)

                        view?.let {
                            val itemView = view as ViewGroup

                            val avatarContainer = itemView.getChildAt(0) as ViewGroup
                            val contentContainer = itemView.getChildAt(1) as ViewGroup

                            val avatar = avatarContainer.getChildAt(0) as ImageView
                            val unReadCount = avatarContainer.getChildAt(1) as TextView
                            val unMuteReadIndicators = avatarContainer.getChildAt(2) as ImageView

                            val nickname = ((contentContainer.getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0)
                            val time = (contentContainer.getChildAt(0) as ViewGroup).getChildAt(1)

                            val content = ((contentContainer.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(1)

                            when (position) {
                                firstChatroomPosition -> {
                                    setTextForMeasureTextView(nickname, firstChatroomInfoModel.nickname)
                                    setTextForMeasureTextView(content, firstChatroomInfoModel.content)

                                    param.result = param.result

                                }
                                firstOfficialPosition -> {
                                    setTextForMeasureTextView(nickname, firstOfficialInfoModel.nickname)
                                    setTextForMeasureTextView(content, firstOfficialInfoModel.content)

                                    param.result = param.result
                                }
                                else -> {
                                }
                            }
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

                val firstEntryPosition = min(firstChatroomPosition, firstChatroomPosition)
                val secondEntryPosition = max(firstChatroomPosition, firstOfficialPosition)

                param.args[0] = when (index) {
                    in 0 until firstEntryPosition -> index
                    firstEntryPosition -> index //TODO
                    in firstEntryPosition + 1 until secondEntryPosition -> index - 1
                    secondEntryPosition -> index //TODO
                    in secondEntryPosition + 1 until Int.MAX_VALUE -> index - 2
                    else -> index
                }

                param.args[0]

            }
        })

        MessageHooker.addAdapterRefreshListener(object : IMainAdapterHelperEntryRefresh {
            override fun onFirstChatroomRefresh(chatRoomPosition: Int, chatRoomChatInfoModel: ChatInfoModel, officialPosition: Int, officialChatInfoModel: ChatInfoModel) {

                firstChatroomPosition = chatRoomPosition
                firstOfficialPosition = officialPosition

                firstChatroomInfoModel = chatRoomChatInfoModel
                firstOfficialInfoModel = officialChatInfoModel


                XposedBridge.log("MessageHooker2.6, firstChatroomPosition = $chatRoomPosition \n")
                XposedBridge.log("MessageHooker2.6, firstOfficialPosition = $officialPosition \n")
            }
        })
    }


    fun setTextForMeasureTextView(measureTextView: Any, charSequence: CharSequence) = XposedHelpers.callMethod(measureTextView, "setText", charSequence)

}