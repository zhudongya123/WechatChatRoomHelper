package com.zdy.project.wechat_chatroom_helper.plugins.main.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.Methods.MMBaseAdapter_getItemInternal
import com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.conversation.Classes
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.plugins.interfaces.IMainAdapterRefresh
import com.zdy.project.wechat_chatroom_helper.plugins.message.MessageHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainAdapter : IAdapterHook {

    var originAdapter: BaseAdapter? = null

    private var firstChatroomPosition = 0
    private var firstOfficialPosition = 1

    var firstChatroomInfoModel = ChatInfoModel()
    var firstOfficialInfoModel = ChatInfoModel()


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

                    override fun beforeHookedMethod(param: MethodHookParam) {

                        val position = param.args[0] as Int
                        val view = param.args[1] as View?
                        val viewGroup = param.args[2] as ViewGroup

                        XposedBridge.log("MMBaseAdapter_getView, index = " + position)

                        if (position == firstChatroomPosition || position == firstOfficialPosition) {
                            view?.setBackgroundColor(0xffEEEEEE.toInt())

                            param.result = null
                        }

                    }


                })


        findAndHookMethod(conversationWithCacheAdapter.superclass, MMBaseAdapter_getItemInternal,
                Int::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return

                val originAdapter = param.thisObject
                var index = param.args[0] as Int

                XposedBridge.log("MMBaseAdapter_getItemInternal, index = " + index)

//                if (flag) {
//
//                    val trueIndex = removeIndexMask(index)
//
//                    XposedBridge.log("MMBaseAdapter_getItemInternal, trueIndex = " + trueIndex)
//
//                    param.result = callGetObjectByIndex(originAdapter, MMBaseAdapter_getItemInternal, trueIndex)
//
//                    return
//
//                } else {
//
                if (index >= 2) index -= 2

                param.args[0] = index
//
//                    val maskIndex = addIndexMask(index)
//
//                    XposedBridge.log("MMBaseAdapter_getItemInternal, maskIndex = " + maskIndex)
//
//                    param.result = callGetObjectByIndex(originAdapter, MMBaseAdapter_getItemInternal, index)
//
//
//                    flag = true
//                }

            }
        })

        MessageHooker.addAdapterRefreshListener(object : IMainAdapterRefresh {
            override fun onFirstChatroomRefresh(position: Int, chatInfoModel: ChatInfoModel) {
                firstChatroomPosition = position
                firstChatroomInfoModel = chatInfoModel
   //             originAdapter?.notifyDataSetChanged()
            }

            override fun onFirstOfficialRefresh(position: Int, chatInfoModel: ChatInfoModel) {
                firstOfficialPosition = position
                firstOfficialInfoModel = chatInfoModel
       //         originAdapter?.notifyDataSetChanged()
            }
        })
    }


    private fun callGetObjectByIndex(adapter: Any, methodName: String, index: Int) = XposedHelpers.callMethod(adapter, methodName, index)

    private fun addIndexMask(index: Int): Int {
        return (1 shl MODE_SHIFT) + index
    }

    private fun isIndexMask(index: Int): Boolean {
        return index shr MODE_SHIFT != 0
    }

    private fun removeIndexMask(index: Int): Int {
        return index - (1 shl MODE_SHIFT)
    }

    private val MODE_SHIFT = 30
    private val MODE_MASK = 0x3 shl MODE_SHIFT

    val UNSPECIFIED = 0 shl MODE_SHIFT


}