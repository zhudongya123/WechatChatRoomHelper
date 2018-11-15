package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.wechat.manager.AvatarMaker
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageFactory
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.interfaces.MessageEventNotifyListener
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.lang.reflect.ParameterizedType

@SuppressLint("StaticFieldLeak")
/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainAdapter {

    lateinit var originAdapter: BaseAdapter
    lateinit var listView: ListView

    var firstChatRoomPosition = -1
    var firstOfficialPosition = -1

    fun executeHook() {
        ConversationItemHandler
        val conversationWithCacheAdapter = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithCacheAdapter, RuntimeInfo.classloader)
        val conversationWithAppBrandListView = XposedHelpers.findClass(WXObject.Adapter.C.ConversationWithAppBrandListView, RuntimeInfo.classloader)
        val conversationClickListener = XposedHelpers.findClass(WXObject.Adapter.C.ConversationClickListener, RuntimeInfo.classloader)
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

                RuntimeInfo.chatRoomViewPresenter.setAdapter(adapter)
                RuntimeInfo.officialViewPresenter.setAdapter(adapter)

                RuntimeInfo.chatRoomViewPresenter.start()
                RuntimeInfo.officialViewPresenter.start()
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

                        LogUtils.log("TrackHelperCan'tOpen, MainAdapter -> HookItemClickListener -> onItemClick ")

                        if (position == firstChatRoomPosition) {
                            LogUtils.log("TrackHelperCan'tOpen, MainAdapter -> HookItemClickListener -> onItemClick -> chatRoomClickPerform, RuntimeInfo.chatRoomViewPresenter = ${RuntimeInfo.chatRoomViewPresenter}")
                            RuntimeInfo.chatRoomViewPresenter.show()
                            param.result = null
                        }
                        if (position == firstOfficialPosition) {
                            LogUtils.log("TrackHelperCan'tOpen, MainAdapter -> HookItemClickListener -> onItemClick -> officialClickPerform, RuntimeInfo.officialViewPresenter = ${RuntimeInfo.officialViewPresenter}")
                            RuntimeInfo.officialViewPresenter.show()
                            param.result = null
                        }
                    }
                })

        findAndHookMethod(conversationWithCacheAdapter, WXObject.Adapter.M.GetView,
                Int::class.java, View::class.java, ViewGroup::class.java,
                object : XC_MethodHook() {

                    override fun afterHookedMethod(param: MethodHookParam) {
                        val position = param.args[0] as Int
                        val view = param.result as View?

                        LogUtils.log("MMBaseAdapter_getView, afterHookedMethod, index = $position, view = $view")

                        if (position == firstChatRoomPosition || position == firstOfficialPosition) {

                            if (view != null) {
                                refreshEntryView(view, position, param)
                            }
                        }
                    }

                    private fun refreshEntryView(view: View?, position: Int, param: MethodHookParam) {
                        LogUtils.log("MessageHooker2.6,position = $position, position = $position, " +
                                "firstChatRoomPosition = $firstChatRoomPosition ,firstOfficialPosition = $firstOfficialPosition \n")

                        val itemView = view as ViewGroup

                        val avatarContainer = itemView.getChildAt(0) as ViewGroup
                        val textContainer = itemView.getChildAt(1) as ViewGroup

                        val avatar = avatarContainer.getChildAt(0) as ImageView
                        val unReadCount = avatarContainer.getChildAt(1) as TextView
                        val unMuteReadIndicators = avatarContainer.getChildAt(2) as ImageView

                        val nickname = ((textContainer.getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0)
                        val time = (textContainer.getChildAt(0) as ViewGroup).getChildAt(1)

                        val sendStatus = ((textContainer.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0)
                        val content = ((textContainer.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(1)
                        val muteImage = ((textContainer.getChildAt(1) as ViewGroup).getChildAt(1) as ViewGroup).getChildAt(1)


                        if (position == firstChatRoomPosition) {
                            unReadCount.visibility = View.GONE
                            unMuteReadIndicators.visibility = View.GONE

                            val allChatRoom = MessageFactory.getSpecChatRoom()
                            val unReadCountItem = MessageFactory.getUnReadCountItem(allChatRoom)
                            val totalUnReadCount = MessageFactory.getUnReadCount(allChatRoom)

                            setTextForNoMeasuredTextView(nickname, "群消息")
                            setTextForNoMeasuredTextView(time, allChatRoom.first().conversationTime)
                            avatar.setImageDrawable(AvatarMaker.handleAvatarDrawable(avatar.context, PageType.CHAT_ROOMS))

                            sendStatus.visibility = View.GONE
                            muteImage.visibility = View.GONE

                            LogUtils.log("getUnReadCountItemChatRoom " + allChatRoom.joinToString { "unReadCount = ${it.unReadCount}" })

                            if (unReadCountItem > 0) {
                                setTextForNoMeasuredTextView(content, "[有 $unReadCountItem 个群聊收到 $totalUnReadCount 条新消息]")
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

                            sendStatus.visibility = View.GONE
                            muteImage.visibility = View.GONE

                            setTextForNoMeasuredTextView(nickname, "服务号")
                            setTextForNoMeasuredTextView(time, allOfficial.first().conversationTime)
                            avatar.setImageDrawable(AvatarMaker.handleAvatarDrawable(avatar.context, PageType.OFFICIAL))

                            LogUtils.log("getUnReadCountItemChatRoom " + allOfficial.joinToString { "unReadCount = ${it.unReadCount}" })

                            if (unReadCountItem > 0) {
                                setTextForNoMeasuredTextView(content, "[有 $unReadCountItem 个服务号收到 $totalUnReadCount 条新消息]")
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
                LogUtils.log("MessageHooker2.7, size = ${originAdapter.count}")

                if (param.thisObject::class.simpleName != conversationWithCacheAdapter.simpleName) return

                /**
                 * 附加长按逻辑
                 *
                 * 在 onItemLongClick 内会调用 getItem 方法来获取 bean ，使用 flag 来判断是否有必要拦截
                 */
                if (MainAdapterLongClick.onItemLongClickMethodInvokeGetItemFlagNickName != "") {
                    param.result = getSpecItemForPlaceHolder(MainAdapterLongClick.onItemLongClickMethodInvokeGetItemFlagNickName, param)
                    MainAdapterLongClick.onItemLongClickMethodInvokeGetItemFlagNickName = ""
                    return
                }
                /**
                 * 长按逻辑结束
                 */

                val index = param.args[0] as Int

                /**
                 * 比较两个入口的先后并确定位置
                 */
                val min = Math.min(firstChatRoomPosition, firstOfficialPosition)
                val max = Math.max(firstChatRoomPosition, firstOfficialPosition)


                val newIndex =
                //如果没有群助手和公众号
                        if (min == -1 && max == -1) {
                            index
                        }
                        //群助手和公众号只有一个
                        else if (min == -1 || max == -1) {
                            when (index) {
                                in 0 until max -> index
                                max -> {
                                    //param.result = getSpecItemForPlaceHolder(" ",param)//填充空数据
                                    //return
                                    index
                                }
                                in max + 1 until Int.MAX_VALUE -> index - 1
                                else -> index //TODO
                            }
                        }
                        //群助手和公众号都存在
                        else {
                            when (index) {
                                in 0 until min -> index
                                min -> {

                                    //param.result = getSpecItemForPlaceHolder(" ",param)//填充空数据
                                    //return
                                    index
                                }
                                in min + 1 until max -> index - 1
                                max -> {
                                    //param.result = getSpecItemForPlaceHolder(" ",param)//填充空数据
                                    //return
                                    index
                                }
                                in max + 1 until Int.MAX_VALUE -> index - 2
                                else -> index //TODO
                            }
                        }

                param.args[0] = newIndex

                LogUtils.log("MessageHooker2.7, size = ${originAdapter.count}, min = $min, max = $max, oldIndex = ${param.args[0]}, newIndex = $newIndex")
            }


            fun getSpecItemForPlaceHolder(username: CharSequence, param: MethodHookParam): Any {
                val beanClass = (param.thisObject::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<*>

                val constructor = beanClass.getConstructor(String::class.java)
                val newInstance = constructor.newInstance(username)

                return newInstance
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
        val mTextField = XposedHelpers.findField(XposedHelpers.findClass(WXObject.Adapter.C.NoMeasuredTextView, RuntimeInfo.classloader), "mText")
        mTextField.isAccessible = true
        return mTextField.get(noMeasuredTextView) as CharSequence
    }
}