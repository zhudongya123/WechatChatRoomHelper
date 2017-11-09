package com.zdy.project.wechat_chatroom_helper.kt

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import android.util.SparseIntArray
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.zdy.project.wechat_chatroom_helper.Constants.*
import com.zdy.project.wechat_chatroom_helper.HookLogic
import com.zdy.project.wechat_chatroom_helper.HookLogic.getMessageBeanForOriginIndex
import com.zdy.project.wechat_chatroom_helper.manager.Type
import com.zdy.project.wechat_chatroom_helper.model.MessageEntity
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomRecyclerViewAdapter
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomViewPresenter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import utils.AppSaveInfoUtils
import java.util.*

/**
 * Created by Mr.Zdy on 2017/10/23.
 */
class HookWeChatKT : IXposedHookLoadPackage {

    //免打扰群组的数据位置
    private val muteListInAdapterPositions = ArrayList<Int>()

    //记录当前有多少个免打扰群有新消息
    private val unReadCountListForMute = SparseIntArray()

    //第一个免打扰群组的下标
    private var firstMutePosition = -1


    //免打扰公众号的数据位置
    private val officialListInAdapterPositions = ArrayList<Int>()

    //记录当前有多少个公众号有新消息
    private val unReadCountListForOfficial = SparseIntArray()

    //第一个公众号的下标
    private var firstOfficialPosition = -1

    //映射出现在主界面的回话的数据位置和实际View位置
    private val newViewPositionWithDataPositionListForOfficial = SparseIntArray()

    private lateinit var muteChatRoomViewPresenter: ChatRoomViewPresenter
    private lateinit var officialChatRoomViewPresenter: ChatRoomViewPresenter


    //标记位，当点击Dialog内的免打扰群组时，防止onItemClick与getObject方法的position冲突
    private var clickChatRoomFlag = false

    //标记位，数据刷新时不更新微信主界面的ListView
    private var notifyList = true

    //是否在聊天界面
    private var isInChatting = false

    var mClassLoader: ClassLoader? = null
    private var context: Context? = null

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName != WECHAT_PACKAGE_NAME) return

        mClassLoader = param.classLoader

        if (!AppSaveInfoUtils.initVariableName()) return //判断是否获取了配置

        findAndHookMethod(Class_Tencent_Home_UI, param.classLoader, Method_Home_UI_Inflater_View,
                Intent::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {

                val activity = getObjectField(param.thisObject, Value_Home_UI_Activity)
                val window = callMethod(activity, "getWindow") as Window
                val viewGroup = window.decorView as ViewGroup
                for (i in 0 until viewGroup.childCount) {

                    val simpleName = viewGroup.getChildAt(i).javaClass.simpleName

                    if (simpleName == "FitSystemWindowLayoutView") {

                        val fitSystemWindowLayoutView = viewGroup.getChildAt(i) as ViewGroup

                        if (fitSystemWindowLayoutView.childCount == 2) {
                            fitSystemWindowLayoutView.addView(muteChatRoomViewPresenter?.presenterView, 1)
                            fitSystemWindowLayoutView.addView(officialChatRoomViewPresenter?.presenterView, 2)
                        }

                    }
                }

            }
        })
        findAndHookConstructor(Class_Conversation_List_View_Adapter_Name, param.classLoader,
                Context::class.java, XposedHelpers.findClass(Method_Conversation_List_View_Adapter_Param,
                param.classLoader), object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                hookAdapterInit(param)
            }
        })

        XposedHelpers.findAndHookMethod("android.app.Activity", param.classLoader,
                "onCreate", Bundle::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                if (param!!.thisObject.javaClass.simpleName == "LauncherUI") {
                    context = param.thisObject as Context
                }
            }
        })

        XposedHelpers.findAndHookMethod("android.widget.BaseAdapter", param.classLoader,
                "notifyDataSetChanged", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                hookNotifyDataSetChanged(param!!)
            }
        })

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                param.classLoader, "getCount", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                param?.let { hookGetCount(it) }
            }
        })

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                param.classLoader, Method_Adapter_Get_Object, Int::class.javaPrimitiveType, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                hookGetObject(param!!)
            }
        })

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Name,
                param.classLoader, "getView", Int::class.javaPrimitiveType, View::class.java,
                ViewGroup::class.java, object : XC_MethodHook() {

            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                hookGetView(param!!)
            }
        })

        XposedHelpers.findAndHookMethod(Class_Conversation_List_Adapter_OnItemClickListener_Name,
                param.classLoader, "onItemClick", AdapterView::class.java, View::class.java,
                Int::class.javaPrimitiveType, Long::class.javaPrimitiveType, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                hookOnItemClick(param!!)
            }
        })

    }


    private fun hookAdapterInit(param: XC_MethodHook.MethodHookParam?) {
        muteChatRoomViewPresenter = ChatRoomViewPresenter(context, Type.CHAT_ROOMS)
        muteChatRoomViewPresenter?.setAdapter(param?.thisObject)
        muteChatRoomViewPresenter?.start()

        officialChatRoomViewPresenter = ChatRoomViewPresenter(context, Type.OFFICIAL)
        officialChatRoomViewPresenter?.setAdapter(param?.thisObject)
        officialChatRoomViewPresenter?.start()
    }


    private fun hookOnItemClick(param: XC_MethodHook.MethodHookParam) {
        if (!AppSaveInfoUtils.openInfo()) return

        val view = param.args[1] as View
        var position = param.args[2] as Int
        val id = param.args[3] as Long


        //移除頭部View的position
        val listView = XposedHelpers.getObjectField(param.thisObject, Value_ListView)
        val headerViewsCount = XposedHelpers.callMethod(listView, "getHeaderViewsCount") as Int

        position = position - headerViewsCount


        //如果点击的是免打扰消息的入口，且不是在群消息助手里面所做的模拟点击（注意！此方法本身就为点击后的处理方法）
        if (position == firstMutePosition && !clickChatRoomFlag) {

            muteChatRoomViewPresenter.setMuteListInAdapterPositions(muteListInAdapterPositions)
            muteChatRoomViewPresenter.setOnDialogItemClickListener(object : ChatRoomRecyclerViewAdapter.OnDialogItemClickListener {
                override fun onItemClick(relativePosition: Int) {
                    clickChatRoomFlag = true
                    XposedHelpers.callMethod(param.thisObject, "onItemClick", param.args[0], view, relativePosition + headerViewsCount, id)

                    if (AppSaveInfoUtils.autoCloseInfo())
                        muteChatRoomViewPresenter.dismiss()
                }

            })
            muteChatRoomViewPresenter.show()
            param.result = null
        }

        if (position == firstOfficialPosition && !clickChatRoomFlag) {
            officialChatRoomViewPresenter.setMuteListInAdapterPositions(officialListInAdapterPositions)
            officialChatRoomViewPresenter.setOnDialogItemClickListener(object : ChatRoomRecyclerViewAdapter.OnDialogItemClickListener {
                override fun onItemClick(relativePosition: Int) {
                    clickChatRoomFlag = true
                    XposedHelpers.callMethod(param.thisObject, "onItemClick", param.args[0], view, relativePosition + headerViewsCount, id)

                    if (AppSaveInfoUtils.autoCloseInfo())
                        officialChatRoomViewPresenter.dismiss()
                }
            })
            officialChatRoomViewPresenter.show()
            param.result = null
        }
    }

    private fun hookGetView(param: XC_MethodHook.MethodHookParam) {
        if (!AppSaveInfoUtils.openInfo()) return

        val position = param.args[0] as Int
        val itemView = param.args[1] as View

        if (itemView.tag == null) return

        //修改群消息助手入口itemView
        val viewHolder = itemView.tag


        val title = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Title)
        val avatar = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Avatar)
        val content = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Content)


        //將第一個免打擾的itemView更改為群消息助手入口，更新其UI
        if (position == firstMutePosition) {

            XposedHelpers.callMethod(title, "setText", "群消息助手")
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149))

            val context = itemView.context

            val shapeDrawable = ShapeDrawable(object : Shape() {
                override fun draw(canvas: Canvas, paint: Paint) {
                    paint.color = -0xed480a
                    val size = canvas.width
                    val drawableId = context.resources.getIdentifier(Drawable_String_Chatroom_Avatar, "drawable", context.packageName)
                    val temp = BitmapFactory.decodeResource(context.resources, drawableId)

                    HookLogic.handlerChatRoomBitmap(canvas, paint, size, temp)
                }
            })
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", shapeDrawable)
            XposedHelpers.callMethod(avatar, "setImageDrawable", shapeDrawable)
            XposedHelpers.callMethod(avatar, "setVisibility", View.VISIBLE)

            var newMessageCount = 0
            for (k in 0 until unReadCountListForMute.size()) {
                val itemValue = unReadCountListForMute.valueAt(k)

                XposedBridge.log("Message position = $k, unreadCount = $itemValue")
                if (itemValue > 0) {
                    newMessageCount++
                }
            }

            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个群有新消息]")
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72))
            }
        } else if (position == firstOfficialPosition) {

            XposedHelpers.callMethod(title, "setText", "公众号助手")
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149))

            val context = itemView.context

            val shapeDrawable = ShapeDrawable(object : Shape() {
                override fun draw(canvas: Canvas, paint: Paint) {
                    paint.color = -0xed480a
                    val size = canvas.width

                    val drawableId = context.resources.getIdentifier(Drawable_String_Chatroom_Avatar,
                            "drawable", context.packageName)
                    val temp = BitmapFactory.decodeResource(context.resources, drawableId)

                    HookLogic.handlerOfficialBitmap(canvas, paint, size, temp)
                }
            })
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", shapeDrawable)
            XposedHelpers.callMethod(avatar, "setImageDrawable", shapeDrawable)
            XposedHelpers.callMethod(avatar, "setVisibility", View.VISIBLE)

            var newMessageCount = 0
            for (k in 0 until unReadCountListForOfficial.size()) {
                val itemValue = unReadCountListForOfficial.valueAt(k)

                XposedBridge.log("Message position = $k, unreadCount = $itemValue")
                if (itemValue > 0) {
                    newMessageCount++
                }
            }

            val parent = (avatar as ImageView).parent as ViewGroup
            parent.getChildAt(1).visibility = View.INVISIBLE

            if (unReadCountListForOfficial.valueAt(0) > 0)
                parent.getChildAt(2).visibility = View.VISIBLE


            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个公众号有新消息]")
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72))
            }
        } else
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", BitmapDrawable())


    }

    private fun hookGetObject(param: XC_MethodHook.MethodHookParam) {
        if (!AppSaveInfoUtils.openInfo()) return //开关

        var index = param.args[0] as Int//要取的数据下标

        val clazzName = param.thisObject.javaClass.simpleName

        if (clazzName != Class_Conversation_List_View_Adapter_SimpleName)
            return

        if (newViewPositionWithDataPositionListForOfficial.size() != 0)
            index = newViewPositionWithDataPositionListForOfficial.get(index, index)

        //如果刚刚点击了群消息助手中的item，则因为模拟分发点击事件会调用getObject方法，
        // 则这一次getObject方法，不再修改数据和View的位置
        if (clickChatRoomFlag) {
            index = param.args[0] as Int//重置数据位置
            clickChatRoomFlag = false
        }
        val bean = HookLogic.getMessageBeanForOriginIndex(param.thisObject, index)

        param.result = bean

    }

    private fun hookGetCount(param: XC_MethodHook.MethodHookParam) {
        if (!AppSaveInfoUtils.openInfo()) return //开关

        val result = param.result as Int//原有会话数量

        val clazzName = param.thisObject.javaClass.simpleName

        if (clazzName != Class_Conversation_List_View_Adapter_SimpleName)
            return //是否为正确的Adapter

        if (result == 0) return

        if (notifyList) {
            var count = result - muteListInAdapterPositions.size//减去免打扰消息的數量
            count++//增加入口位置

            count = count - officialListInAdapterPositions.size//减去公众号的数量
            count++

            param.result = count
        }
    }

    private fun hookNotifyDataSetChanged(param: XC_MethodHook.MethodHookParam) {
        val clazzName = param.thisObject.javaClass.simpleName

        if (clazzName != Class_Conversation_List_View_Adapter_SimpleName)
            return //是否为正确的Adapter


        notifyList = false

        //代码保护区，此段执行时getCount逻辑跳过
        run {


            muteListInAdapterPositions.clear()
            unReadCountListForMute.clear()
            firstMutePosition = -1

            officialListInAdapterPositions.clear()
            unReadCountListForOfficial.clear()
            firstOfficialPosition = -1

            newViewPositionWithDataPositionListForOfficial.clear()

            for (i in 0 until (param.thisObject as BaseAdapter).count) {
                val value = getMessageBeanForOriginIndex(param.thisObject, i)

                val messageStatus = XposedHelpers.callMethod(param.thisObject,
                        Method_Message_Status_Bean, value)

                val entity = MessageEntity(value)

                //是否为免打扰群组
                val isMuteConversation = isMuteConversation(messageStatus)

                //是否为公众号
                val isOfficialConversation = isOfficialConversation(value, messageStatus)

                if (isMuteConversation) {
                    if (firstMutePosition == -1) {
                        firstMutePosition = i

                        if (officialListInAdapterPositions.size != 0)
                            firstMutePosition = firstMutePosition - officialListInAdapterPositions.size + 1
                    }

                    muteListInAdapterPositions.add(i)
                    unReadCountListForMute.put(i, entity.field_unReadCount)
                }

                if (isOfficialConversation) {

                    if (firstOfficialPosition == -1) {
                        firstOfficialPosition = i

                        if (muteListInAdapterPositions.size != 0)
                            firstOfficialPosition = firstOfficialPosition - muteListInAdapterPositions.size + 1
                    }

                    officialListInAdapterPositions.add(i)
                    unReadCountListForOfficial.put(i, entity.field_unReadCount)
                }

                val muteCount = muteListInAdapterPositions.size
                val officialCount = officialListInAdapterPositions.size


                //非群免打扰消息或者是公众号消息 或者是最新的群消息和公众号消息（入口）   即需要在微信主界面展示的回话
                if (!isMuteConversation && !isOfficialConversation ||
                        muteCount == 1 && isMuteConversation && !isOfficialConversation ||
                        officialCount == 1 && isOfficialConversation && !isMuteConversation) {
                    var key = i - if (muteCount >= 1) muteCount - 1 else muteCount
                    key -= if (officialCount >= 1) officialCount - 1 else officialCount
                    newViewPositionWithDataPositionListForOfficial.put(key, i)
                }


            }
        }
        notifyList = true

        if (muteChatRoomViewPresenter != null) {
            muteChatRoomViewPresenter.setMuteListInAdapterPositions(muteListInAdapterPositions)
        }

        if (officialChatRoomViewPresenter != null) {
            officialChatRoomViewPresenter.setMuteListInAdapterPositions(officialListInAdapterPositions)
        }
    }

    private fun isOfficialConversation(value: Any, messageStatus: Any): Boolean {

        val wcY = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_OFFICIAL_1)
        val wcU = XposedHelpers.getIntField(messageStatus, Value_Message_Status_Is_OFFICIAL_2)
        val field_username = XposedHelpers.getObjectField(value, Value_Message_Status_Is_OFFICIAL_3) as String

        return "gh_43f2581f6fd6" != field_username && wcY && (wcU == 1 || wcU == 2 || wcU == 3)
    }

    private fun isMuteConversation(messageStatus: Any): Boolean {

        val uyI = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_1)
        val uXX = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_2)

        return uyI && uXX
    }
}