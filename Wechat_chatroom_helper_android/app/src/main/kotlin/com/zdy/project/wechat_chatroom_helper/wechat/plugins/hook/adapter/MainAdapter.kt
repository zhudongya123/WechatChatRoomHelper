package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.manager.DrawableMaker
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.ConversationReflectFunction
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageFactory
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageHandler
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.interfaces.MessageEventNotifyListener
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.lang.reflect.ParameterizedType
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@SuppressLint("StaticFieldLeak")
/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainAdapter {

    lateinit var originAdapter: BaseAdapter
    lateinit var listView: ListView
    var supportHashMap = mutableMapOf<Any, Any>()

    var firstChatRoomPosition = -1
    var firstOfficialPosition = -1

    const val asyncFlag = false

    // 可能同时刷新 2 个，故开2个线程
    val asyncExecutor: Executor = Executors.newFixedThreadPool(2)

    // 如果用户接受在主线程刷消息，那就在主线程
    val mainThreadExecutor: Executor = Executor {
        it.run()
    }

    fun isOriginAdapterIsInitialized() = MainAdapter::originAdapter.isInitialized

    fun executeHook() {
        val conversationWithCacheAdapterGetItem = ConversationReflectFunction.conversationWithCacheAdapter.superclass.declaredMethods
                .filter { it.parameterTypes.size == 1 && it.parameterTypes[0] == Int::class.java }
                .first { it.name != "getItem" && it.name != "getItemId" }.name

        /**
         * 主页面的adapter构造时的回调
         */
        hookAllConstructors(ConversationReflectFunction.conversationWithCacheAdapter, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val adapter = param.thisObject as? BaseAdapter ?: return
                originAdapter = adapter
            }
        })

        /**
         * 修改主页面Adapter的返回数量 【服务号和群聊列表要新增两个的长度】
         */
        findAndHookMethod(ConversationReflectFunction.conversationWithCacheAdapter.superclass, WXObject.Adapter.M.GetCount, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                var count = param.result as Int + (if (firstChatRoomPosition != -1) 1 else 0)
                count += (if (firstOfficialPosition != -1) 1 else 0)
                param.result = count
            }
        })

        findAndHookMethod(ConversationReflectFunction.conversationClickListener, WXObject.Adapter.M.OnItemClick,
                AdapterView::class.java, View::class.java, Int::class.java, Long::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {

                        val view = param.result as View?
                        val position = (param.args[2] as Int) - listView.headerViewsCount

                        LogUtils.log("TrackHelperCan'tOpen, MainAdapter -> HookItemClickListener -> onItemClick. " +
                                "position = $position, firstChatRoomPosition = $firstChatRoomPosition, firstOfficialPosition = $firstOfficialPosition")

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

        /**
         * 将多出来的两个入口【其实就是ListView里面多出来的那两个View】 绑定ui
         */
        findAndHookMethod(ConversationReflectFunction.conversationWithCacheAdapter, WXObject.Adapter.M.GetView,
                Int::class.java, View::class.java, ViewGroup::class.java,
                object : XC_MethodHook() {

                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val position = param.args[0] as Int
                        val view = param.args[1] as View?

                        LogUtils.log("MMBaseAdapter_getView, beforeHookedMethod, index = $position, view = $view")

                        /**
                         * getView如果返回的View为空 说明是第一次初始化 直接return
                         */
                        if (view == null) {
                            return
                        }

                        /**
                         * 如果这个时候两个入口的View不为空，修改ui，然后直接返回，本身微信的getView就直接跳过
                         */
                        when (position) {
                            firstChatRoomPosition -> {
                                refreshChatEntryView(view, position)
                                param.result = view
                            }
                            firstOfficialPosition -> {
                                refreshOfficialView(view, position)
                                param.result = view
                            }
                        }

                        /**
                         *因为存在跳过原微信逻辑的情况，全局每个itemView都手动添加点击事件
                         */
                        view.apply {
                            setOnClickListener {
                                listView.performItemClick(view, position + listView.headerViewsCount, view.id.toLong())
                            }
                            setOnLongClickListener {
                                val onItemLongClickListener = XposedHelpers.getObjectField(listView, "mOnItemLongClickListener") as AdapterView.OnItemLongClickListener
                                onItemLongClickListener.onItemLongClick(listView, view, position + listView.headerViewsCount, view.id.toLong())
                            }
                        }
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        val position = param.args[0] as Int
                        val view = param.result as View?

                        LogUtils.log("MMBaseAdapter_getView, afterHookedMethod, index = $position, view = $view")
                    }

                    private fun refreshChatEntryView(view: View?, position: Int) {
                        LogUtils.log("MessageHooker2.6,position = $position, position = $position, " +
                                "firstChatRoomPosition = $firstChatRoomPosition ,firstOfficialPosition = $firstOfficialPosition \n")

                        val itemView = view as ViewGroup
                        val mainItemViewHolder = MainItemViewHolder(itemView)

                        mainItemViewHolder.unReadCount.visibility = View.GONE
                        XposedHelpers.callMethod(mainItemViewHolder.content, "setDrawLeftDrawable", false)
                        setTextForNoMeasuredTextView(mainItemViewHolder.nickname, "群聊消息" + (if (MainAdapterLongClick.chatRoomStickyValue > 0) " - 置顶" else ""))
                        mainItemViewHolder.avatar.setImageDrawable(DrawableMaker.handleAvatarDrawable(mainItemViewHolder.avatar.context, PageType.CHAT_ROOMS))
                        mainItemViewHolder.sendStatus.visibility = View.GONE
                        mainItemViewHolder.muteImage.visibility = View.GONE

                        if (asyncFlag) asyncExecutor else mainThreadExecutor
                                .execute {
                                    val allChatRoom = MessageFactory.getSpecChatRoom()
                                    val unReadCountItem = MessageFactory.getUnReadCountItem(allChatRoom)
                                    val totalUnReadCount = MessageFactory.getUnReadCount(allChatRoom)
                                    val unMuteUnReadCount = MessageFactory.getUnMuteUnReadCount(allChatRoom)
                                    LogUtils.log("getUnReadCountItemChatRoom " + allChatRoom.joinToString { "unReadCount = ${it.unReadCount}" })

                                    val chatInfoModel = allChatRoom.sortedBy { -it.field_conversationTime }.first()

                                    view.post {
                                        setTextForNoMeasuredTextView(mainItemViewHolder.time, chatInfoModel.conversationTime)
                                        if (unReadCountItem > 0) {
                                            val spannableStringBuilder = SpannableStringBuilder().apply {
                                                var firstLength = 0
                                                if (unMuteUnReadCount > 0) {
                                                    append("[${unMuteUnReadCount}条] ")
                                                    firstLength = length
                                                    setSpan(ForegroundColorSpan(MainItemViewHolder.Conversation_Red_Text_Color), 0, firstLength, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                                                }
                                                append("[ $unReadCountItem 个群聊收到 $totalUnReadCount 条新消息]")
                                                setSpan(ForegroundColorSpan(MainItemViewHolder.Conversation_Light_Text_Color), firstLength, length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                                            }
                                            setTextForNoMeasuredTextView(mainItemViewHolder.content, spannableStringBuilder)

                                            mainItemViewHolder.unMuteReadIndicators.visibility = View.VISIBLE
                                        } else {
                                            setTextColorForNoMeasuredTextView(mainItemViewHolder.content, Color.parseColor("#" + AppSaveInfo.contentColorInfo(mainItemViewHolder.content.context)))
                                            setTextForNoMeasuredTextView(mainItemViewHolder.content, "${chatInfoModel.nickname}：${chatInfoModel.content}")
                                            mainItemViewHolder.unMuteReadIndicators.visibility = View.GONE
                                        }
                                    }
                                }
                    }

                    private fun refreshOfficialView(view: View?, position: Int) {
                        LogUtils.log("MessageHooker2.6,position = $position, position = $position, " +
                                "firstChatRoomPosition = $firstChatRoomPosition ,firstOfficialPosition = $firstOfficialPosition \n")

                        val itemView = view as ViewGroup
                        val mainItemViewHolder = MainItemViewHolder(itemView)

                        mainItemViewHolder.unReadCount.visibility = View.GONE
                        XposedHelpers.callMethod(mainItemViewHolder.content, "setDrawLeftDrawable", false)
                        setTextForNoMeasuredTextView(mainItemViewHolder.nickname, "服务号消息" + (if (MainAdapterLongClick.officialStickyValue > 0) " - 置顶" else ""))
                        mainItemViewHolder.avatar.setImageDrawable(DrawableMaker.handleAvatarDrawable(mainItemViewHolder.avatar.context, PageType.OFFICIAL))
                        mainItemViewHolder.sendStatus.visibility = View.GONE
                        mainItemViewHolder.muteImage.visibility = View.GONE

                        if (asyncFlag) asyncExecutor else mainThreadExecutor
                                .execute {
                                    val allOfficial = MessageFactory.getSpecOfficial()
                                    val unReadCountItem = MessageFactory.getUnReadCountItem(allOfficial)
                                    val totalUnReadCount = MessageFactory.getUnReadCount(allOfficial)

                                    LogUtils.log("getUnReadCountItemChatRoom " + allOfficial.joinToString { "unReadCount = ${it.unReadCount}" })

                                    val chatInfoModel = allOfficial.sortedBy { -it.field_conversationTime }.first()

                                    view.post {
                                        setTextForNoMeasuredTextView(mainItemViewHolder.time, chatInfoModel.conversationTime)
                                        val oldOfficialCString = getTextFromNoMeasuredTextView(mainItemViewHolder.content)

                                        if (unReadCountItem > 0) {
                                            val newOfficialString = "[ $unReadCountItem 个服务号收到 $totalUnReadCount 条新消息]"
                                            if (oldOfficialCString != newOfficialString) {
                                                Log.v("refreshOfficialView", "newOfficialString = $newOfficialString, oldOfficialCString = $oldOfficialCString")
                                                setTextForNoMeasuredTextView(mainItemViewHolder.content, newOfficialString)
                                                setTextColorForNoMeasuredTextView(mainItemViewHolder.content, MainItemViewHolder.Conversation_Light_Text_Color)
                                            }
                                            mainItemViewHolder.unMuteReadIndicators.visibility = View.VISIBLE
                                        } else {
                                            val newOfficialString = "${chatInfoModel.nickname}：${chatInfoModel.content}"
                                            if (oldOfficialCString != newOfficialString) {
                                                setTextForNoMeasuredTextView(mainItemViewHolder.content, newOfficialString)
                                                setTextColorForNoMeasuredTextView(mainItemViewHolder.content, Color.parseColor("#" + AppSaveInfo.contentColorInfo(mainItemViewHolder.content.context)))
                                            }
                                            mainItemViewHolder.unMuteReadIndicators.visibility = View.GONE
                                        }
                                    }
                                }
                    }
                })

        /**
         * 修改 getObject 的数据下标 【 插入两个view 原来getObject的位置也要发生变化】
         */
        findAndHookMethod(ConversationReflectFunction.conversationWithCacheAdapter.superclass, conversationWithCacheAdapterGetItem,
                Int::class.java, object : XC_MethodHook() {

            private var getItemChatRoomFlag = false
            private var getItemOfficialFlag = false

            override fun beforeHookedMethod(param: MethodHookParam) {

                LogUtils.log("MessageHooker 2019-04-12 15:36:49, thisObject className = ${param.thisObject::class.java.name}, adapter className = ${ConversationReflectFunction.conversationWithCacheAdapter.name}")

                if (param.thisObject::class.java.name != ConversationReflectFunction.conversationWithCacheAdapter.name) return

                /**
                 * 附加长按逻辑
                 *
                 * 在 onItemLongClick 内会调用 getItem 方法来获取 bean ，使用 flag 来判断是否有必要拦截
                 */
                if (MainAdapterLongClick.onItemLongClickMethodInvokeGetItemFlagNickName != "") {
                    param.result = getSpecItemForPlaceHolder(MainAdapterLongClick.onItemLongClickMethodInvokeGetItemFlagNickName)
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


                LogUtils.log("MessageHooker 2019-04-02 09:18:31, size = ${originAdapter.count}, firstChatRoomPosition = $firstChatRoomPosition, firstOfficialPosition = $firstOfficialPosition")

                val newIndex =
                        //如果没有群助手和公众号
                        if (min == -1 && max == -1) {
                            index
                        }
                        //群助手和公众号只有一个
                        else if (min == -1 || max == -1) {
                            when {
                                index < max -> {
                                    index
                                }
                                index == max -> {
                                    handleEntryPosition(index)
                                }
                                index > max -> {
                                    index - 1
                                }
                                else -> {
                                    index
                                }
                            }
                        }
                        //群助手和公众号都存在
                        else {
                            if (index < min) {
                                index
                            } else if (index == min) {
                                handleEntryPosition(index)
                            } else if (index > min && index < max) {
                                index - 1
                            } else if (index == max) {
                                handleEntryPosition(index)
                            } else if (index > max) {
                                index - 2
                            } else {
                                index
                            }
                        }

                LogUtils.log("MessageHook 2019-04-03 15:30:00, size = ${originAdapter.count}, min = $min, max = $max, oldIndex = ${param.args[0]}, newIndex = $newIndex")

                param.args[0] = newIndex
            }

            override fun afterHookedMethod(param: MethodHookParam) {

                if (param.thisObject::class.java.name != ConversationReflectFunction.conversationWithCacheAdapter.name) return

                var index = param.args[0] as Int

                when {
                    getItemChatRoomFlag -> {
                        getItemChatRoomFlag = false
                        index = firstChatRoomPosition
                    }
                    getItemOfficialFlag -> {
                        getItemOfficialFlag = false
                        index = firstOfficialPosition
                    }
                    else -> {
                        try {
                            val result = param.result

                            //返回了空的数据，此时getcount和getitem已经无法对应 所以直接刷新list
                            if (result == null) {
                                MainLauncherUI.restartMainActivity()
                                return
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                        return
                    }
                }
                val result = getCustomItemForEntry(index)

                val field_flag = XposedHelpers.getLongField(result, "field_flag")
                val field_username = XposedHelpers.getObjectField(result, "field_username")
                val field_conversationTime = XposedHelpers.getLongField(result, "field_conversationTime")

                LogUtils.log("MessageHook 2019-04-01 16:25:57, index = $index, flag = $field_flag, username = $field_username, field_conversationTime = $field_conversationTime")

                supportHashMap[field_username] = result

                param.result = result
            }

            private fun handleEntryPosition(index: Int): Int {
                if (firstChatRoomPosition == index) {
                    getItemChatRoomFlag = true
                } else if (firstOfficialPosition == index) {
                    getItemOfficialFlag = true
                }
                return 0
            }


            private var chatRoomItemModel: Any? = null
            private var officialItemModel: Any? = null

            private fun getCustomItemForEntry(currentPosition: Int): Any {

                val field_conversationTime: Long
                val field_flag: Long

                val item: Any

                when {
                    firstChatRoomPosition == currentPosition -> {
                        if (MainAdapterLongClick.chatRoomStickyValue > 0) {
                            field_conversationTime = -1L
                            field_flag = System.currentTimeMillis() + (1L shl 62)//你在这秀你妈位运算呢
                        } else {
                            field_conversationTime = MessageFactory.getSpecChatRoom().first().field_conversationTime
                            field_flag = field_conversationTime
                        }

                        if (chatRoomItemModel == null)
                            chatRoomItemModel = getSpecItemForPlaceHolder("chatRoomItem")

                        item = chatRoomItemModel as Any

                    }
                    firstOfficialPosition == currentPosition -> {

                        if (MainAdapterLongClick.officialStickyValue > 0) {
                            field_conversationTime = -1L
                            field_flag = System.currentTimeMillis() + (1L shl 62)
                        } else {
                            field_conversationTime = MessageFactory.getSpecOfficial().first().field_conversationTime
                            field_flag = field_conversationTime
                        }


                        if (officialItemModel == null)
                            officialItemModel = getSpecItemForPlaceHolder("officialItem")

                        item = officialItemModel as Any
                    }
                    else -> throw RuntimeException("wrong position currentPosition = $currentPosition, firstChatRoomPosition = $firstChatRoomPosition, firstOfficialPosition = $firstOfficialPosition")
                }

                XposedHelpers.setLongField(item, "field_flag", field_flag)
                XposedHelpers.setLongField(item, "field_conversationTime", field_conversationTime)

                LogUtils.log("MessageHook 2019-04-05 14:11:46, index = $currentPosition, flag = $field_flag, field_conversationTime = $field_conversationTime")

                return item
            }

            fun getSpecItemForPlaceHolder(username: CharSequence): Any {
                val clazz = ConversationReflectFunction.conversationWithCacheAdapter
                val beanClass = (clazz.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<*>

                val constructor = beanClass.getConstructor(String::class.java)
                return constructor.newInstance(username)
            }

        })





        findAndHookMethod(ConversationReflectFunction.conversationWithCacheAdapter.superclass, "getChangeType", object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {
                if (MainLauncherUI.NOTIFY_MAIN_LAUNCHER_UI_LIST_VIEW_FLAG) {
                    MainLauncherUI.NOTIFY_MAIN_LAUNCHER_UI_LIST_VIEW_FLAG = false
                    param.result = 2
                }
            }
        })


        /**
         * 2020年6月27日
         * 新版微信 大于 7.0.16 刷新聊天界面（其实就是取全部聊天最新数据时）回取HashMap来计算未读数
         *
         * 这里因为改变拦截方式，所以直接取消系统的计算方式，强制返回 空
         */

        LogUtils.log("conversationGetHash, method = ${ConversationReflectFunction.conversationGetHashMapMethod}")
        findAndHookMethod(ConversationReflectFunction.conversationHashMapBean, ConversationReflectFunction.conversationGetHashMapMethod.name, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = HashMap<Any, Any>()
            }
        })
        LogUtils.log("conversationGetHash, method = ${ConversationReflectFunction.conversationGetHashMapMethod}")


        MessageHandler.addMessageEventNotifyListener(object : MessageEventNotifyListener {

            override fun onNewMessageCreate(talker: String, createTime: Long, content: Any) {
                super.onNewMessageCreate(talker, createTime, content)
            }

            override fun onEntryPositionChanged(chatroomPosition: Int, officialPosition: Int) {
                super.onEntryPositionChanged(chatroomPosition, officialPosition)

                if (firstOfficialPosition != officialPosition || firstChatRoomPosition != chatroomPosition) {
                    firstChatRoomPosition = chatroomPosition
                    firstOfficialPosition = officialPosition
                }

                LogUtils.log("onEntryPositionChanged, firstChatRoomPosition = ${firstChatRoomPosition}, firstOfficialPosition = ${firstOfficialPosition}")

                LogUtils.log("onEntryPositionChanged, chatRoomStickyValue = ${MainAdapterLongClick.chatRoomStickyValue}, firstOfficialPosition = ${MainAdapterLongClick.officialStickyValue}")

                /**
                 * 粘性头部~（置顶）
                 */
                if (MainAdapterLongClick.chatRoomStickyValue > 0 && MainAdapterLongClick.officialStickyValue == 0) {
                    if (firstChatRoomPosition > firstOfficialPosition) {
                        firstOfficialPosition += 1
                    }
                    firstChatRoomPosition = 0
                }
                if (MainAdapterLongClick.chatRoomStickyValue == 0 && MainAdapterLongClick.officialStickyValue > 0) {
                    if (firstOfficialPosition > firstChatRoomPosition) {
                        firstChatRoomPosition += 1
                    }
                    firstOfficialPosition = 0
                }
                if (MainAdapterLongClick.chatRoomStickyValue > 0 && MainAdapterLongClick.officialStickyValue > 0) {

                    if (MainAdapterLongClick.chatRoomStickyValue > MainAdapterLongClick.officialStickyValue) {
                        firstChatRoomPosition = 0
                        firstOfficialPosition = 1
                    }
                    if (MainAdapterLongClick.chatRoomStickyValue < MainAdapterLongClick.officialStickyValue) {
                        firstChatRoomPosition = 1
                        firstOfficialPosition = 0
                    }
                }

                LogUtils.log("onEntryPositionChanged2, firstChatRoomPosition = ${firstChatRoomPosition}, firstOfficialPosition = ${firstOfficialPosition}")
            }

        })
    }

    fun setTextForNoMeasuredTextView(noMeasuredTextView: Any, charSequence: CharSequence) = XposedHelpers.callMethod(noMeasuredTextView, "setText", charSequence)
    fun setTextColorForNoMeasuredTextView(noMeasuredTextView: Any, color: Int) = XposedHelpers.callMethod(noMeasuredTextView, "setTextColor", color)

    fun getTextFromNoMeasuredTextView(noMeasuredTextView: Any): CharSequence {
        val noMeasuredTextViewClass = XposedHelpers.findClass(WXObject.Adapter.C.NoMeasuredTextView, RuntimeInfo.classloader)

        val mTextField = noMeasuredTextViewClass.declaredFields.first { it.type.simpleName == java.lang.CharSequence::class.java.simpleName }

        LogUtils.log("getTextFromNoMeasuredTextView, mTextField = ${mTextField}")
        LogUtils.log("getTextFromNoMeasuredTextView, mTextField = ${XposedHelpers.getObjectField(noMeasuredTextView, mTextField.name)}")


//      val mTextField = XposedHelpers.findField(noMeasuredTextViewClass, "mText")
//      mTextField.isAccessible = true
//      return mTextField.get(noMeasuredTextView) as CharSequence

        return XposedHelpers.getObjectField(noMeasuredTextView, mTextField.name) as CharSequence
    }

    fun adapterIsInitialized() = MainAdapter::originAdapter.isInitialized
}