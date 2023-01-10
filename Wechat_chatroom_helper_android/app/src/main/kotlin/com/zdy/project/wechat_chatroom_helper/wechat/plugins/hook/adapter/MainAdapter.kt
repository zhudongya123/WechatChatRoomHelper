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
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewFactory
import com.zdy.project.wechat_chatroom_helper.wechat.manager.DrawableMaker
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.ConversationReflectFunction
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

    private var chatRoomItemModel: Any? = null
    private var officialItemModel: Any? = null

    fun isOriginAdapterIsInitialized() = MainAdapter::originAdapter.isInitialized

    fun executeHook() {

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
                            RuntimeInfo.chatRoomViewPresenter?.show()
                            param.result = null
                        }
                        if (position == firstOfficialPosition) {
                            LogUtils.log("TrackHelperCan'tOpen, MainAdapter -> HookItemClickListener -> onItemClick -> officialClickPerform, RuntimeInfo.officialViewPresenter = ${RuntimeInfo.officialViewPresenter}")
                            RuntimeInfo.officialViewPresenter?.show()
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
                                refreshChatRoomEntryView(view, position)
                                param.result = view
                            }
                            firstOfficialPosition -> {
                                refreshOfficialView(view, position)
                                param.result = view
                            }

                            else -> {


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

                    private fun refreshChatRoomEntryView(view: View?, position: Int) {
                        LogUtils.log("MessageHooker2.6,position = $position, position = $position, " +
                                "firstChatRoomPosition = $firstChatRoomPosition ,firstOfficialPosition = $firstOfficialPosition \n")

                        val itemView = view as ViewGroup
                        val mainItemViewHolder = MainItemViewHolder(itemView)

                        mainItemViewHolder.unReadCount.visibility = View.GONE
                        XposedHelpers.callMethod(mainItemViewHolder.content, "setDrawLeftDrawable", false)
                        setTextForNoMeasuredTextView(mainItemViewHolder.nickname, "群聊消息")
                        mainItemViewHolder.avatar.setImageDrawable(DrawableMaker.handleAvatarDrawable(mainItemViewHolder.avatar.context, PageType.CHAT_ROOMS))
                        mainItemViewHolder.sendStatus.visibility = View.GONE
                        mainItemViewHolder.muteImage.visibility = View.GONE


                        /**
                         * 首先使用存储的临时列表，如果在首页或者临时列表为空，使用sql语句查询实时数据
                         */
                        var allChatRoom = RuntimeInfo.chatRoomViewPresenter?.getCurrentData()
                                ?: arrayListOf()
                        if (allChatRoom.isEmpty() || RuntimeInfo.currentPage == PageType.MAIN) {
                            allChatRoom = MessageFactory.getSpecChatRoom()
                        }

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

                            if (MessageFactory.getStickFlagInfo(chatRoomItemModel) != 0L) {
                                mainItemViewHolder.containerView.background = ChatRoomViewFactory.getItemViewBackgroundSticky(view.context)
                            } else {
                                mainItemViewHolder.containerView.background = ChatRoomViewFactory.getItemViewBackground(view.context)
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
                        setTextForNoMeasuredTextView(mainItemViewHolder.nickname, "服务号消息")
                        mainItemViewHolder.avatar.setImageDrawable(DrawableMaker.handleAvatarDrawable(mainItemViewHolder.avatar.context, PageType.OFFICIAL))
                        mainItemViewHolder.sendStatus.visibility = View.GONE
                        mainItemViewHolder.muteImage.visibility = View.GONE

                        /**
                         * 首先使用存储的临时列表，如果在首页或者临时列表为空，使用sql语句查询实时数据
                         */
                        var allOfficial = RuntimeInfo.officialViewPresenter?.getCurrentData()
                                ?: arrayListOf()
                        if (allOfficial.isEmpty() || RuntimeInfo.currentPage == PageType.MAIN) {
                            allOfficial = MessageFactory.getSpecOfficial()
                        }

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


                            if (MessageFactory.getStickFlagInfo(officialItemModel) != 0L) {
                                mainItemViewHolder.containerView.background = ChatRoomViewFactory.getItemViewBackgroundSticky(view.context)
                            } else {
                                mainItemViewHolder.containerView.background = ChatRoomViewFactory.getItemViewBackground(view.context)
                            }

                        }
                    }
                })

        /**
         * 修改 getObject 的数据下标 【 插入两个view 原来getObject的位置也要发生变化】
         */

        LogUtils.log("MessageHook 2023-01-09 17:02:00, ${ConversationReflectFunction.conversationWithCacheAdapterGetItemMethodName}")
        findAndHookMethod(ConversationReflectFunction.conversationWithCacheAdapter.superclass,
                ConversationReflectFunction.conversationWithCacheAdapterGetItemMethodName,
                Int::class.java, object : XC_MethodHook() {

            private var getItemChatRoomFlag = false
            private var getItemOfficialFlag = false

            override fun beforeHookedMethod(param: MethodHookParam) {
                LogUtils.log("MessageHook 2023-01-09 17:02:00, GetItemMethodName invoke GetItemMethodName = ${param.thisObject::class.java.name}")
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


                LogUtils.log("MessageHook 2019-04-02 09:18:31, size = ${originAdapter.count}, firstChatRoomPosition = $firstChatRoomPosition, firstOfficialPosition = $firstOfficialPosition")

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

                val index = param.args[0] as Int

                LogUtils.log("MessageHook 2020-07-04 16:02:08, index = $index, getItemChatRoomFlag = $getItemChatRoomFlag, getItemOfficialFlag = $getItemOfficialFlag")

                when {
                    /**
                     * adapter需要群助手的的adapter model 然后返还给它
                     */
                    getItemChatRoomFlag -> {
                        getItemChatRoomFlag = false

                        val result = getCustomItemForEntry(firstChatRoomPosition)
                        param.result = result
                    }
                    /**
                     * adapter需要服务号的的adapter model 然后返还给它
                     */
                    getItemOfficialFlag -> {
                        getItemOfficialFlag = false

                        val result = getCustomItemForEntry(firstOfficialPosition)
                        param.result = result
                    }
                    else -> {
                        try {
                            val result = param.result

                            //todo 返回了空的数据，此时getcount和getitem已经无法对应 所以直接刷新list
                            if (result == null) {
                                LogUtils.log("MessageHook 2019-04-01 15:30:00, index = $index")
                                // MainLauncherUI.restartMainActivity()
                                param.result = getCustomItemForEntry(firstOfficialPosition)
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }

            }

            /**
             * 判断传入的位置是不是助手的位置
             */
            private fun handleEntryPosition(index: Int): Int {
                if (firstChatRoomPosition == index) {
                    getItemChatRoomFlag = true
                } else if (firstOfficialPosition == index) {
                    getItemOfficialFlag = true
                }
                return 0
            }


            /**
             * 构造助手的Bean然后返回
             */
            private fun getCustomItemForEntry(currentPosition: Int): Any {

                val field_conversationTime: Long
                val field_flag: Long

                val item: Any

                when {
                    firstChatRoomPosition == currentPosition -> {

                        var currentData = RuntimeInfo.chatRoomViewPresenter?.getCurrentData()
                                ?: arrayListOf()
                        if (currentData.isEmpty() || RuntimeInfo.currentPage == PageType.MAIN) currentData = MessageFactory.getSpecChatRoom()
                        val first = currentData.first()
                        field_flag = first.field_flag
                        field_conversationTime = first.field_conversationTime

                        if (chatRoomItemModel == null)
                            chatRoomItemModel = getSpecItemForPlaceHolder("chatRoomItem")

                        item = chatRoomItemModel as Any

                    }
                    firstOfficialPosition == currentPosition -> {

                        var currentData = RuntimeInfo.officialViewPresenter?.getCurrentData()
                                ?: arrayListOf()
                        if (currentData.isEmpty() || RuntimeInfo.currentPage == PageType.MAIN) currentData = MessageFactory.getSpecOfficial()
                        val first = currentData.first()
                        field_flag = first.field_flag
                        field_conversationTime = first.field_conversationTime

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


//        findAndHookMethod(ConversationReflectFunction.conversationWithCacheAdapter.superclass, "getChangeType", object : XC_MethodHook() {
//
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                if (MainLauncherUI.NOTIFY_MAIN_LAUNCHER_UI_LIST_VIEW_FLAG) {
//                    MainLauncherUI.NOTIFY_MAIN_LAUNCHER_UI_LIST_VIEW_FLAG = false
//                    param.result = 2
//                }
//            }
//        })

        findAndHookMethod(ConversationReflectFunction.conversationWithCacheAdapter,
                "onNotifyChange",
                Int::class.java,
                ConversationReflectFunction.mStorageExClass,
                Any::class.java,
                object : XC_MethodHook() {

                    override fun beforeHookedMethod(param: MethodHookParam) {

                        val args = param.args!!

                        val type = args[0] as Int
                        val username = args[2] as String

                        LogUtils.log("MessageHook 2021-09-16 23:11:46, type = $type, username = $username")

                        var officialData = RuntimeInfo.officialViewPresenter?.getCurrentData()
                                ?: arrayListOf()
                        var chatRoomData = RuntimeInfo.chatRoomViewPresenter?.getCurrentData()
                                ?: arrayListOf()

                        if (officialData.isEmpty()) officialData = MessageFactory.getSpecOfficial()
                        if (chatRoomData.isEmpty()) chatRoomData = MessageFactory.getSpecChatRoom()

                        val isInOfficial = officialData.map { it.field_username }.any { it == username }
                        val isInChatRoom = chatRoomData.map { it.field_username }.any { it == username }

                        LogUtils.log("MessageHook 2021-09-16 23:11:46, " +
                                "isInOfficial = $isInOfficial, " +
                                "isInChatRoom = $isInChatRoom, " +
                                "officialData size = ${officialData.size}, " +
                                "chatRoomData size = ${chatRoomData.size}")

                        when {
                            isInChatRoom -> {
                                RuntimeInfo.chatRoomViewPresenter?.refreshList(false, Any())
                                XposedHelpers.callMethod(originAdapter,
                                        ConversationReflectFunction.notifyPartialConversationListMethod.name, "chatRoomItem", 2, null, true)
                                param.result = null
                            }
                            isInOfficial -> {
                                RuntimeInfo.officialViewPresenter?.refreshList(false, Any())
                                XposedHelpers.callMethod(originAdapter,
                                        ConversationReflectFunction.notifyPartialConversationListMethod.name, "officialItem", 2, null, true)
                                param.result = null
                            }
                        }
                    }

                })

//        /**
//         * 2020年6月27日
//         * 新版微信 大于 7.0.16 刷新聊天界面（其实就是取全部聊天最新数据时）回取HashMap来计算未读数
//         *
//         * 这里因为改变拦截方式，所以直接取消系统的计算方式，强制返回 空
//         */
//
//        findAndHookMethod(ConversationReflectFunction.conversationHashMapBean, ConversationReflectFunction.conversationGetHashMapMethod.name, object : XC_MethodHook() {
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                param.result = HashMap<Any, Any>()
//            }
//        })


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
            }

        })

        val notifyPartialMethod = ConversationReflectFunction.notifyPartialConversationListMethod
        findAndHookMethod(ConversationReflectFunction.conversationWithCacheAdapter.superclass,
                notifyPartialMethod.name,
                Any::class.java,
                Int::class.java,
                notifyPartialMethod.parameterTypes[2],
                Boolean::class.java,
                object : XC_MethodHook() {

                    override fun afterHookedMethod(param: MethodHookParam) {
                        val args = param.args
                        val s = args[0] as String
                        val i = args[1] as Int

                        LogUtils.log("MessageHook 2021-09-18 17:02:00, s = $s, i = $i")
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