package com.zdy.project.wechat_chatroom_helper

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.*
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.zdy.project.wechat_chatroom_helper.Constants.*
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.utils.SoftKeyboardUtil
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomRecyclerViewAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter
import com.zdy.project.wechat_chatroom_helper.wechat.manager.AvatarMaker
import com.zdy.project.wechat_chatroom_helper.wechat.manager.RuntimeInfo
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import utils.AppSaveInfo
import java.util.*


/**
 *
 * Created by zhudo on 2017/7/2.
 */

class HookLogic : IXposedHookLoadPackage {

    //免打扰群组的数据位置
    private val chatRoomListInAdapterPositions = ArrayList<Int>()

    //记录当前有多少个免打扰群有新消息
    private val unReadCountListForChatRoom = SparseIntArray()

    //第一个免打扰群组的下标
    private var firstChatRoomPosition = -1

    //免打扰公众号的数据位置
    private val officialListInAdapterPositions = ArrayList<Int>()

    //记录当前有多少个公众号有新消息
    private val unReadCountListForOfficial = SparseIntArray()

    //第一个公众号的下标
    private var firstOfficialPosition = -1


    //映射出现在主界面的回话的数据位置和实际View位置
    private val newViewPositionWithDataPositionList = SparseIntArray()

    private var chatRoomViewPresenter: ChatRoomViewPresenter? = null
    private var officialViewPresenter: ChatRoomViewPresenter? = null


    //标记位，当点击Dialog内的免打扰群组时，防止onItemClick与getObject方法的position冲突
    private var clickChatRoomFlag = false

    //标记位，数据刷新时不更新微信主界面的ListView
    private var notifyList = true

    //是否在聊天界面
    private var isInChatting = false

    //软键盘是否打开
    private var isSoftKeyBoardOpen = false

    private var context: Context? = null

    private var maskView: View? = null


    @Throws(Throwable::class)
    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        if (loadPackageParam.packageName != WECHAT_PACKAGE_NAME) return

        if (!AppSaveInfo.initVariableName()) return //判断是否获取了配置

        if (!AppSaveInfo.openInfo()) return

        RuntimeInfo.mClassLoader = loadPackageParam.classLoader

        findAndHookConstructor("com.tencent.mm.ui.HomeUI.FitSystemWindowLayoutView",
                loadPackageParam.classLoader, Context::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookFitSystemWindowLayoutViewConstructor(param)

            }
        })

        findAndHookConstructor("com.tencent.mm.ui.HomeUI.FitSystemWindowLayoutView",
                loadPackageParam.classLoader, Context::class.java, AttributeSet::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookFitSystemWindowLayoutViewConstructor(param)

            }
        })

        findAndHookConstructor(Class_Conversation_List_View_Adapter_Name, loadPackageParam.classLoader,
                Context::class.java, XposedHelpers.findClass(Method_Conversation_List_View_Adapter_Param,
                loadPackageParam.classLoader), object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                LogUtils.log("hookAdapterInit")
                hookAdapterInit(param)
            }
        })

        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader,
                "onCreate", Bundle::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                context = param.thisObject as Context

                SoftKeyboardUtil.observeSoftKeyboard(context as Activity?, SoftKeyboardUtil.OnSoftKeyboardChangeListener { softKeyboardHeight, visible ->
                    isSoftKeyBoardOpen = visible
                    if (chatRoomViewPresenter == null) return@OnSoftKeyboardChangeListener
                    if (officialViewPresenter == null) return@OnSoftKeyboardChangeListener
                    if (chatRoomViewPresenter!!.isShowing || officialViewPresenter!!.isShowing) {
                        if (isSoftKeyBoardOpen)
                            maskView!!.visibility = View.VISIBLE
                        else
                            maskView!!.visibility = View.INVISIBLE
                    }
                })
            }
        })

        findAndHookMethod("android.widget.BaseAdapter", loadPackageParam.classLoader,
                "notifyDataSetChanged", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookNotifyDataSetChanged(param)
            }
        })

        findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, "getCount", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookGetCount(param)
            }
        })

        findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, Method_Adapter_Get_Object, Int::class.javaPrimitiveType, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookGetObject(param)
            }
        })

        findAndHookMethod(Class_Conversation_List_View_Adapter_Name,
                loadPackageParam.classLoader, "getView", Int::class.javaPrimitiveType, View::class.java,
                ViewGroup::class.java, object : XC_MethodHook() {

            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookGetView(param)
            }
        })

        findAndHookMethod(Class_Conversation_List_Adapter_OnItemClickListener_Name,
                loadPackageParam.classLoader, "onItemClick", AdapterView::class.java, View::class.java,
                Int::class.javaPrimitiveType, Long::class.javaPrimitiveType, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookOnItemClick(param)
            }
        })

        findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader,
                "dispatchKeyEvent", KeyEvent::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookDispatchKeyEvent(param)
            }
        })

        findAndHookMethod("android.view.ViewGroup", loadPackageParam.classLoader,
                "dispatchTouchEvent", MotionEvent::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {

                if (param.thisObject.javaClass.simpleName == "TestTimeForChatting") {

                    val motionEvent = param.args[0] as MotionEvent
                    if (!isInChatting) {
                        maskView!!.visibility = View.INVISIBLE
                        return
                    }

                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                        }
                        MotionEvent.ACTION_MOVE -> if (isSoftKeyBoardOpen)
                            maskView!!.visibility = View.INVISIBLE
                        MotionEvent.ACTION_UP -> {
                        }
                    }
                }
            }
        })


        hookLog(loadPackageParam)
    }

    private fun hookDispatchKeyEvent(param: XC_MethodHook.MethodHookParam) {
        val keyEvent = param.args[0] as KeyEvent

        //手指离开返回键的事件
        if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {

            //不在聊天界面
            if (!isInChatting) {
                //群消息助手在屏幕上显示
                if (chatRoomViewPresenter!!.isShowing) {
                    LogUtils.log("dispatchKeyEvent, chatRoomViewPresenter.isShowing")
                    chatRoomViewPresenter!!.dismiss()
                    param.result = true
                }

                //公众号助手在屏幕上显示
                if (officialViewPresenter!!.isShowing) {
                    LogUtils.log("dispatchKeyEvent, officialViewPresenter.isShowing")
                    officialViewPresenter!!.dismiss()
                    param.result = true
                }
            } else {
                LogUtils.log("dispatchKeyEvent, isInChatting")
            }
        }
    }

    private fun isWechatHighVersion(wechatVersion: String): Boolean {
        return wechatVersion == "1140" || wechatVersion == "1160" || Integer.valueOf(wechatVersion) > 1160
    }

    /**
     * 此方法完成的逻辑：
     * fitSystemWindowLayoutView 为微信主界面的 rootView 往此 View 中添加子 View 来实现助手界面
     * 注意添加下标一定要小于聊天View -> TestTimeChatting 的下标
     *
     *
     * 此部分同时包含一个黑色遮罩的逻辑，用来防止在助手界面软键盘弹出瞬间的View穿透问题，此逻辑一般不会触发，可以省略。
     *
     *
     * 同时为了确保助手View 尺寸正确，将会获取主界面聊天列表的布局参数，复制到助手的布局参数中，添加此逻辑主要来规避某些手机上虚拟按键和状态栏不准确的问题。
     *
     * @param param
     */
    private fun hookFitSystemWindowLayoutViewConstructor(param: XC_MethodHook.MethodHookParam) {
        val fitSystemWindowLayoutView = param.thisObject as ViewGroup
        fitSystemWindowLayoutView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {

                val chattingView: View//聊天View
                val chattingViewPosition: Int//聊天View的下標
                var fitWindowChildCount = 0//fitSystemWindowLayoutView的 child 数量
                var chatRoomViewPosition = 0
                var officialViewPosition = 0
                var maskViewPosition = 0

                /*
                 * 微信在某个版本之后 View 数量发生变化，下标也要相应刷新
                 **/
                if (isWechatHighVersion(AppSaveInfo.wechatVersionInfo())) {
                    fitWindowChildCount = 3
                    chattingViewPosition = 2
                    chatRoomViewPosition = 2
                    officialViewPosition = 3
                    maskViewPosition = 4
                } else {
                    fitWindowChildCount = 2
                    chattingViewPosition = 1
                    chatRoomViewPosition = 1
                    officialViewPosition = 2
                    maskViewPosition = 3
                }

                LogUtils.log("FitSystemWindowLayoutView Constructor")

                chattingView = fitSystemWindowLayoutView.getChildAt(chattingViewPosition)
                if (fitSystemWindowLayoutView.childCount != fitWindowChildCount) return
                if (fitSystemWindowLayoutView.getChildAt(0) !is LinearLayout) return
                if (chattingView.javaClass.simpleName != "TestTimeForChatting") return

                if (chatRoomViewPresenter == null)
                    chatRoomViewPresenter = ChatRoomViewPresenter(context!!, PageType.CHAT_ROOMS)
                if (officialViewPresenter == null)
                    officialViewPresenter = ChatRoomViewPresenter(context!!, PageType.OFFICIAL)

                val chatRoomViewParent = chatRoomViewPresenter!!.presenterView.parent
                if (chatRoomViewParent != null) {
                    (chatRoomViewParent as ViewGroup).removeView(chatRoomViewPresenter!!.presenterView)
                }

                val officialViewParent = officialViewPresenter!!.presenterView.parent
                if (officialViewParent != null) {
                    (chatRoomViewParent as ViewGroup).removeView(officialViewPresenter!!.presenterView)
                }

                fitSystemWindowLayoutView.addView(chatRoomViewPresenter!!.presenterView, chatRoomViewPosition)
                fitSystemWindowLayoutView.addView(officialViewPresenter!!.presenterView, officialViewPosition)

                //黑色遮罩，逻辑可忽略
                maskView = View(context)
                maskView!!.setBackgroundColor(-0x1000000)
                maskView!!.visibility = View.INVISIBLE
                val maskParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT)
                maskView!!.layoutParams = maskParams
                fitSystemWindowLayoutView.addView(maskView, maskViewPosition)


                //複製佈局參數邏輯
                //此逻辑并不完美，属于拆东墙补西墙

                if ((fitSystemWindowLayoutView.getChildAt(0) as ViewGroup).childCount != 2)
                    return
                val mainView = (fitSystemWindowLayoutView.getChildAt(0) as ViewGroup).getChildAt(1)
                mainView.viewTreeObserver
                        .addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener {
                            val left = mainView.left
                            val right = mainView.right
                            val top = mainView.top
                            val bottom = mainView.bottom

                            val width = right - left
                            val height = bottom - top

                            if (width == 0 || height == 0) return@OnGlobalLayoutListener

                            val chatRoomViewPresenterPresenterView = chatRoomViewPresenter!!.presenterView
                            val officialViewPresenterPresenterView = officialViewPresenter!!.presenterView


                            val left1 = chatRoomViewPresenterPresenterView.left
                            val top1 = chatRoomViewPresenterPresenterView.top
                            val right1 = chatRoomViewPresenterPresenterView.right
                            val bottom1 = chatRoomViewPresenterPresenterView.bottom

                            if (Rect(left1, top1, right1, bottom1) == Rect(left, top, right, bottom))
                                return@OnGlobalLayoutListener

                            val params = FrameLayout.LayoutParams(width, height)
                            params.setMargins(0, top, 0, 0)

                            chatRoomViewPresenterPresenterView.layoutParams = params
                            officialViewPresenterPresenterView.layoutParams = params
                            maskView!!.layoutParams = params
                        })
            }

            override fun onChildViewRemoved(parent: View, child: View) {}
        })

    }

    private fun hookAdapterInit(param: XC_MethodHook.MethodHookParam) {
        if (chatRoomViewPresenter == null)
            chatRoomViewPresenter = ChatRoomViewPresenter(context!!, PageType.CHAT_ROOMS)
        if (officialViewPresenter == null)
            officialViewPresenter = ChatRoomViewPresenter(context!!, PageType.OFFICIAL)

        chatRoomViewPresenter!!.setAdapter(param.thisObject)
        chatRoomViewPresenter!!.start()

        officialViewPresenter!!.setAdapter(param.thisObject)
        officialViewPresenter!!.start()
    }

    private fun hookOnItemClick(param: XC_MethodHook.MethodHookParam) {

        val itemView = param.args[1] as View
        var position = param.args[2] as Int
        val id = param.args[3] as Long


        val listView = XposedHelpers.getObjectField(param.thisObject, Value_ListView)
        val headerViewsCount = XposedHelpers.callMethod(listView, "getHeaderViewsCount") as Int

        LogUtils.log("hookOnItemClick, position = " + position + ", headerViewsCount =" +
                headerViewsCount + ", view = " + itemView + " adapterView  = " + param.args[0])

        if (itemView.measuredHeight != ScreenUtils.dip2px(context, 64f)) {
            //修正点击空白区域的问题
            param.result = null
            return
        }

        //移除頭部View的position
        position = position - headerViewsCount


        //如果点击的是免打扰消息的入口，且不是在群消息助手里面所做的模拟点击（注意！此方法本身就为点击后的处理方法）
        if (position == firstChatRoomPosition && !clickChatRoomFlag) {
            chatRoomViewPresenter!!.setListInAdapterPositions(chatRoomListInAdapterPositions)
            chatRoomViewPresenter!!.setOnDialogItemClickListener(object : ChatRoomRecyclerViewAdapter.OnDialogItemClickListener {
                override fun onItemLongClick(relativePosition: Int) {
                    try {
                        val mOnItemLongClickListener = XposedHelpers.findField(listView.javaClass,
                                "mOnItemLongClickListener").get(listView) as AdapterView.OnItemLongClickListener

                        mOnItemLongClickListener.onItemLongClick(listView as AdapterView<*>, null,
                                newViewPositionWithDataPositionList.get(relativePosition) + headerViewsCount, id)

                        LogUtils.log("hookOnItemClick, onItemLongClick, relativePosition = " +
                                relativePosition + ", headerViewsCount =" +
                                headerViewsCount + ", view = " + itemView + " adapterView  = " + param.args[0])

                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }

                }

                override fun onItemClick(relativePosition: Int) {
                    clickChatRoomFlag = true
                    XposedHelpers.callMethod(param.thisObject, "onItemClick", param.args[0], itemView, relativePosition + headerViewsCount, id)

                    LogUtils.log("hookOnItemClick, onItemClick, relativePosition = " + relativePosition + "," +
                            " headerViewsCount =" +
                            headerViewsCount + ", view = " + itemView + " adapterView  = " + param.args[0])

                    if (AppSaveInfo.autoCloseInfo()) chatRoomViewPresenter!!.dismiss()
                }
            })
            chatRoomViewPresenter!!.show()
            RuntimeInfo.changeCurrentPage(PageType.CHAT_ROOMS)
            param.result = null
        }

        if (position == firstOfficialPosition && !clickChatRoomFlag) {
            officialViewPresenter!!.setListInAdapterPositions(officialListInAdapterPositions)
            officialViewPresenter!!.setOnDialogItemClickListener(object : ChatRoomRecyclerViewAdapter.OnDialogItemClickListener {
                override fun onItemLongClick(relativePosition: Int) {

                }

                override fun onItemClick(relativePosition: Int) {
                    clickChatRoomFlag = true
                    XposedHelpers.callMethod(param.thisObject, "onItemClick", param.args[0], itemView, relativePosition + headerViewsCount, id)

                    if (AppSaveInfo.autoCloseInfo()) officialViewPresenter!!.dismiss()
                }
            })
            officialViewPresenter!!.show()
            RuntimeInfo.changeCurrentPage(PageType.OFFICIAL)
            param.result = null
        }
    }

    private fun hookGetView(param: XC_MethodHook.MethodHookParam) {

        if (param.args[1] == null) return

        val position = param.args[0] as Int
        val itemView = param.args[1] as View

        if (itemView.tag == null) return

        //修改群消息助手入口itemView
        val viewHolder = itemView.tag


        val title = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Title)
        val avatar = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Avatar)
        val content = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Content)


        //將第一個免打擾的itemView更改為群消息助手入口，更新其UI
        if (position == firstChatRoomPosition) {

            val context = itemView.context

            //修改头像
            val shapeDrawable = ShapeDrawable(object : Shape() {
                override fun draw(canvas: Canvas, paint: Paint) {
                    AvatarMaker.handleAvatarDrawable(context, canvas, paint, PageType.CHAT_ROOMS)
                }
            })
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", shapeDrawable)
            XposedHelpers.callMethod(avatar, "setImageDrawable", shapeDrawable)
            XposedHelpers.callMethod(avatar, "setVisibility", View.VISIBLE)

            //重新设定消息未读数
            var newMessageCount = 0
            for (k in 0 until unReadCountListForChatRoom.size()) {
                val itemValue = unReadCountListForChatRoom.valueAt(k)

                if (itemValue > 0) {
                    newMessageCount++
                }
            }

            val parent = (avatar as ImageView).parent as ViewGroup
            parent.getChildAt(1).visibility = View.INVISIBLE

            if (unReadCountListForChatRoom.valueAt(0) > 0)
                parent.getChildAt(2).visibility = View.VISIBLE

            //更新消息内容
            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个群有新消息]")
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72))
            } else {
                XposedHelpers.callMethod(content, "setText", getNoMeasuredTextViewText(title).toString()
                        + " : " + getNoMeasuredTextViewText(content))
            }

            //修改nickname
            XposedHelpers.callMethod(title, "setText", "群消息助手")
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149))

        } else if (position == firstOfficialPosition) {

            //修改头像
            val shapeDrawable = ShapeDrawable(object : Shape() {
                override fun draw(canvas: Canvas, paint: Paint) {
                    val size = canvas.width
                    AvatarMaker.handleAvatarDrawable(context!!, canvas, paint, PageType.OFFICIAL)
                }
            })
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", shapeDrawable)
            XposedHelpers.callMethod(avatar, "setImageDrawable", shapeDrawable)
            XposedHelpers.callMethod(avatar, "setVisibility", View.VISIBLE)

            //重新设定消息未读数
            var newMessageCount = 0
            for (k in 0 until unReadCountListForOfficial.size()) {
                val itemValue = unReadCountListForOfficial.valueAt(k)

                if (itemValue > 0) {
                    newMessageCount++
                }
            }

            val parent = (avatar as ImageView).parent as ViewGroup
            parent.getChildAt(1).visibility = View.INVISIBLE

            if (unReadCountListForOfficial.valueAt(0) > 0)
                parent.getChildAt(2).visibility = View.VISIBLE

            //更新消息内容
            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个服务号有新消息]")
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72))
            } else {
                XposedHelpers.callMethod(content, "setText", getNoMeasuredTextViewText(title).toString()
                        + " : " + getNoMeasuredTextViewText(content))
            }

            //修改nickname
            XposedHelpers.callMethod(title, "setText", "服务号助手")
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149))

        } else
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", BitmapDrawable())


        LogUtils.log("hookGetView , position = " + position +
                ", nickname = " + getNoMeasuredTextViewText(title))
    }

    private fun getNoMeasuredTextViewText(textView: Any): CharSequence {
        var clazz: Class<*>? = null
        try {
            clazz = XposedHelpers.findClass("com.tencent.mm.ui.base.NoMeasuredTextView", RuntimeInfo.mClassLoader)

            val field = clazz!!.getDeclaredField("mText")
            field.isAccessible = true
            return field.get(textView) as CharSequence
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        return ""
    }

    private fun hookGetObject(param: XC_MethodHook.MethodHookParam) {

        var index = param.args[0] as Int//要取的数据下标

        val clazzName = param.thisObject.javaClass.simpleName

        if (clazzName != Class_Conversation_List_View_Adapter_SimpleName)
            return

        if (newViewPositionWithDataPositionList.size() != 0)
            index = newViewPositionWithDataPositionList.get(index, index)

        //如果刚刚点击了群消息助手中的item，则因为模拟分发点击事件会调用getObject方法，
        // 则这一次getObject方法，不再修改数据和View的位置
        if (clickChatRoomFlag) {
            index = param.args[0] as Int//重置数据位置
            clickChatRoomFlag = false
        }


        LogUtils.log("hookGetObject, originIndex = " + param.args[0] + ", actuallyIndex = " + index)

        val bean = getMessageBeanForOriginIndex(param.thisObject, index)

        param.result = bean

    }

    private fun hookGetCount(param: XC_MethodHook.MethodHookParam) {

        val clazzName = param.thisObject.javaClass.simpleName

        if (clazzName != Class_Conversation_List_View_Adapter_SimpleName)
            return //是否为正确的Adapter

        val tMb = XposedHelpers.getObjectField(param.thisObject, Method_Adapter_Get_Object_Step_1)
        val result = XposedHelpers.callMethod(tMb, "getCount") as Int

        if (result == 0) return

        if (notifyList) {

            val chatRoomSize = chatRoomListInAdapterPositions.size
            val officialSize = officialListInAdapterPositions.size

            var count = result - chatRoomSize + if (chatRoomSize > 0) 1 else 0//减去群的數量
            count = count - officialSize + if (officialSize > 0) 1 else 0//减去公众号的数量

            param.result = count

            LogUtils.log("hookGetCount, origin = " + result + ", chatRoom = "
                    + chatRoomSize + ", official = " + officialSize + ", return = " + count)
        } else {
            LogUtils.log("hookGetCount, originSize = $result")
            param.result = result
        }
    }

    private fun hookNotifyDataSetChanged(param: XC_MethodHook.MethodHookParam) {

        val clazzName = param.thisObject.javaClass.simpleName

        //  在聊天界面直接跳过
        if (isInChatting) return

        //是否为正确的Adapter
        if (clazzName != Class_Conversation_List_View_Adapter_SimpleName) return

        notifyList = false

        //代码保护区，此段执行时getCount逻辑跳过
        run {

            chatRoomListInAdapterPositions.clear()
            unReadCountListForChatRoom.clear()
            firstChatRoomPosition = -1

            officialListInAdapterPositions.clear()
            unReadCountListForOfficial.clear()
            firstOfficialPosition = -1

            newViewPositionWithDataPositionList.clear()

            officialNickNameEntries = ArrayList()
            muteChatRoomNickNameEntries = ArrayList()
            allChatRoomNickNameEntries = ArrayList()

            val tMb = XposedHelpers.getObjectField(param.thisObject, Method_Adapter_Get_Object_Step_1)
            val originCount = XposedHelpers.callMethod(tMb, "getCount") as Int

            LogUtils.log("hookNotifyDataSetChanged, originCount = $originCount")

            for (i in 0 until originCount) {
                val value = getMessageBeanForOriginIndex(param.thisObject, i)

                val messageStatus = XposedHelpers.callMethod(param.thisObject,
                        Method_Message_Status_Bean, value)


                val chatInfoModel = ChatInfoModel.convertFromObject(value, param.thisObject, context!!)

                //是否为群组
                val isChatRoomConversation = isChatRoomConversation(messageStatus)

                //是否为公众号
                val isOfficialConversation = isOfficialConversation(value, messageStatus)

                if (isChatRoomConversation) {
                    if (firstChatRoomPosition == -1) {
                        firstChatRoomPosition = i

                        if (officialListInAdapterPositions.size != 0)
                            firstChatRoomPosition = firstChatRoomPosition - officialListInAdapterPositions.size + 1
                    }

                    chatRoomListInAdapterPositions.add(i)
                    unReadCountListForChatRoom.put(i, chatInfoModel.unReadCount)
                }

                if (isOfficialConversation) {

                    if (firstOfficialPosition == -1) {
                        firstOfficialPosition = i

                        if (chatRoomListInAdapterPositions.size != 0)
                            firstOfficialPosition = firstOfficialPosition - chatRoomListInAdapterPositions.size + 1
                    }

                    officialListInAdapterPositions.add(i)
                    unReadCountListForOfficial.put(i, chatInfoModel.unReadCount)
                }


                LogUtils.log("i = " + i + "/" + originCount + ", nickname = " + chatInfoModel.nickname
                        + ", isChatRoom = " + isChatRoomConversation + " , isOfficial = " + isOfficialConversation)


                val chatRoomCount = chatRoomListInAdapterPositions.size
                val officialCount = officialListInAdapterPositions.size


                //非群免打扰消息或者是公众号消息 或者是最新的群消息和公众号消息（入口）   即需要在微信主界面展示的回话
                if (!isChatRoomConversation && !isOfficialConversation ||
                        chatRoomCount == 1 && isChatRoomConversation && !isOfficialConversation ||
                        officialCount == 1 && isOfficialConversation && !isChatRoomConversation) {
                    var key = i - if (chatRoomCount >= 1) chatRoomCount - 1 else chatRoomCount
                    key = key - if (officialCount >= 1) officialCount - 1 else officialCount
                    newViewPositionWithDataPositionList.put(key, i)
                }
            }
        }
        notifyList = true

        if (chatRoomViewPresenter != null) {
            chatRoomViewPresenter!!.setListInAdapterPositions(chatRoomListInAdapterPositions)
        }

        if (officialViewPresenter != null) {
            officialViewPresenter!!.setListInAdapterPositions(officialListInAdapterPositions)
        }
    }

    private fun isOfficialConversation(value: Any, messageStatus: Any): Boolean {
        val username = XposedHelpers.getObjectField(messageStatus, Constants.Value_Message_Bean_NickName).toString()

        val list = AppSaveInfo.getWhiteList("white_list_official")

        val wcY = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_OFFICIAL_1)
        val wcU = XposedHelpers.getIntField(messageStatus, Value_Message_Status_Is_OFFICIAL_2)
        val field_username = XposedHelpers.getObjectField(value, Value_Message_Status_Is_OFFICIAL_3) as String

        val isOfficial = "gh_43f2581f6fd6" != field_username && wcY && (wcU == 1 || wcU == 2 || wcU == 3)

        if (isOfficial) {
            officialNickNameEntries.add(username)

            for (s in list) {
                if (s.trim { it <= ' ' } == username) return false
            }
        }

        return isOfficial
    }

    private fun isChatRoomConversation(messageStatus: Any): Boolean {

        val uyI = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_1)
        val uXX = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_2)

        val list = AppSaveInfo.getWhiteList("white_list_chat_room")

        val username = XposedHelpers.getObjectField(messageStatus, Constants.Value_Message_Bean_NickName).toString()


        //这是个群聊
        if (uXX) {
            //搜集所有群聊的标记
            if (AppSaveInfo.chatRoomTypeInfo() == "1") {
                allChatRoomNickNameEntries.add(username)
                for (s in list) {
                    if (s.trim { it <= ' ' } == username) return false
                }
                return true
            }

            //还是一个免打扰的群聊
            if (uyI) {
                muteChatRoomNickNameEntries.add(username)
                for (s in list)
                    if (s.trim { it <= ' ' } == username) return false

                return true
            }
        }
        return false
    }

    private fun hookLog(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(Class_Tencent_Log, loadPackageParam.classLoader, "i",
                String::class.java, String::class.java, Array<Any>::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                if (!AppSaveInfo.openInfo()) return

                try {
                    val desc = param.args[1].toString()
                    val objArr = param.args[2] as Array<Any>
                    try {
                        //       LogUtils.INSTANCE.log("Xposed_Log, key = " + param.args[0] + " value = " + String.format
                        //             (desc, objArr));
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    //无奈之举，只能使用拦截日志的做法来实现部分功能
                    //关闭聊天窗口
                    if (desc.contains("closeChatting")) {
                        isInChatting = false
                        LogUtils.log("closeChatting")
                        when (RuntimeInfo.currentPage) {
                            PageType.CHATTING_WITH_OFFICIAL -> RuntimeInfo.changeCurrentPage(PageType.OFFICIAL)
                            PageType.CHATTING_WITH_CHAT_ROOMS -> RuntimeInfo.changeCurrentPage(PageType.CHAT_ROOMS)
                            PageType.CHATTING -> RuntimeInfo.changeCurrentPage(PageType.MAIN)
                        }
                    }
                    if (desc.contains("startChatting")) {
                        isInChatting = true
                        LogUtils.log("startChatting")

                        when (RuntimeInfo.currentPage) {
                            PageType.OFFICIAL -> RuntimeInfo.changeCurrentPage(PageType.CHATTING_WITH_OFFICIAL)
                            PageType.CHAT_ROOMS -> RuntimeInfo.changeCurrentPage(PageType.CHATTING_WITH_CHAT_ROOMS)
                            PageType.MAIN -> RuntimeInfo.changeCurrentPage(PageType.CHATTING)
                        }
                    }

                    //收到新消息
                    if (desc.contains("summerbadcr updateConversation talker")) {

                        val sendUsername = objArr[0] as String
                        if (sendUsername.contains("chatroom")) {
                            if (chatRoomViewPresenter != null) {
                                chatRoomViewPresenter!!.setMessageRefresh(sendUsername)
                            }
                        }
                        if (officialViewPresenter != null) {
                            officialViewPresenter!!.setMessageRefresh(sendUsername)
                        }
                    }
                } catch (e: Exception) {
                }

            }
        })
    }

    companion object {

        lateinit var allChatRoomNickNameEntries: ArrayList<String>
        lateinit var muteChatRoomNickNameEntries: ArrayList<String>
        lateinit var officialNickNameEntries: ArrayList<String>

        /**
         * 根据下标返回消息列表里的消息条目，不受免打扰影响
         * 即为原数据
         */
        fun getMessageBeanForOriginIndex(adapter: Any, index: Int): Any {
            val bean: Any

            val tMb = XposedHelpers.getObjectField(adapter, Method_Adapter_Get_Object_Step_1)

            val hdB = XposedHelpers.getObjectField(tMb, Method_Adapter_Get_Object_Step_2)

            bean = XposedHelpers.callMethod(hdB, Method_Adapter_Get_Object_Step_3, index)

            return bean
        }

        fun setAvatar(avatar: ImageView, field_username: String) {
            try {
                XposedHelpers.callStaticMethod(Class.forName(Class_Set_Avatar, false, RuntimeInfo.mClassLoader),
                        Constants.Method_Conversation_List_Get_Avatar, avatar, field_username)
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }
    }

}
