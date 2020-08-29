package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AbsoluteLayout
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.ConversationReflectFunction
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapter
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.lang.reflect.Modifier

@SuppressLint("StaticFieldLeak")
/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainLauncherUI {

    var NOTIFY_MAIN_LAUNCHER_UI_LIST_VIEW_FLAG = false

    lateinit var launcherUI: Activity

    var container: ViewGroup? = null


    fun executeHook() {

        /**
         * 通过hook Constructor来监听fitSystemView 解决方案1
         */
        fitSystemWindowConstructorHook()

        findAndHookMethod(Activity::class.java, WXObject.MainUI.M.OnCreate, Bundle::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject::class.java.name == WXObject.MainUI.C.LauncherUI) {
                    LogUtils.log("MainLauncherUI, onCreate")

                    launcherUI = param.thisObject as Activity

                    RuntimeInfo.chatRoomViewPresenter = ChatRoomViewPresenter(launcherUI, PageType.CHAT_ROOMS)
                    RuntimeInfo.officialViewPresenter = ChatRoomViewPresenter(launcherUI, PageType.OFFICIAL)

                    /**
                     * 通过decorView 及其子View来判断fitSystemView 解决方案2
                     */
                    handleDetectFitWindowView(launcherUI)
                }
            }
        })


        /**
         * hook 按下返回键 修改返回键逻辑
         */
        findAndHookMethod(WXObject.MainUI.C.LauncherUI, RuntimeInfo.classloader,
                WXObject.MainUI.M.DispatchKeyEventOfLauncherUI, KeyEvent::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                hookDispatchKeyEvent(param)
            }
        })
        /**
         * hook 关闭聊天窗口和开启聊天窗口时 改变页面状态
         */
        findAndHookMethod(WXObject.MainUI.C.LauncherUI, RuntimeInfo.classloader,
                WXObject.MainUI.M.StartChattingOfLauncherUI, String::class.java, Bundle::class.java, Boolean::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        LogUtils.log("MainLauncherUI, startChatting")
                        if (RuntimeInfo.currentPage == PageType.OFFICIAL) RuntimeInfo.currentPage = PageType.CHATTING_WITH_OFFICIAL
                        else if (RuntimeInfo.currentPage == PageType.CHAT_ROOMS) RuntimeInfo.currentPage = PageType.CHATTING_WITH_CHAT_ROOMS

                        if (AppSaveInfo.autoCloseInfo()) {
                            if (RuntimeInfo.currentPage == PageType.CHATTING_WITH_OFFICIAL) RuntimeInfo.officialViewPresenter.dismiss()
                            else if (RuntimeInfo.currentPage == PageType.CHATTING_WITH_CHAT_ROOMS) RuntimeInfo.chatRoomViewPresenter.dismiss()
                        }
                    }
                })
        findAndHookMethod(WXObject.MainUI.C.LauncherUI, RuntimeInfo.classloader,
                WXObject.MainUI.M.CloseChattingOfLauncherUI, Boolean::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        LogUtils.log("MainLauncherUI, closeChatting")
                        if (RuntimeInfo.currentPage == PageType.CHATTING_WITH_OFFICIAL) {
                            if (RuntimeInfo.officialViewPresenter.isShowing)
                                RuntimeInfo.currentPage = PageType.OFFICIAL
                            else RuntimeInfo.currentPage = PageType.MAIN

                        } else if (RuntimeInfo.currentPage == PageType.CHATTING_WITH_CHAT_ROOMS) {
                            if (RuntimeInfo.chatRoomViewPresenter.isShowing)
                                RuntimeInfo.currentPage = PageType.CHAT_ROOMS
                            else RuntimeInfo.currentPage = PageType.MAIN
                        }
                    }

                })


        /**
         * 开始绑定助手的业务
         */
        findAndHookMethod(ConversationReflectFunction.conversationWithAppBrandListView,
                WXObject.Adapter.M.SetAdapter, ListAdapter::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                MainAdapter.listView = param.thisObject as ListView
                val adapter = param.args[0]

                RuntimeInfo.chatRoomViewPresenter.setAdapter(adapter)
                RuntimeInfo.officialViewPresenter.setAdapter(adapter)

                RuntimeInfo.chatRoomViewPresenter.start()
                RuntimeInfo.officialViewPresenter.start()
            }
        })

        try {
            findAndHookMethod(ConversationReflectFunction.conversationListView,
                    WXObject.Adapter.M.SetActivity,
                    XposedHelpers.findClass(WXObject.MainUI.C.MMFragmentActivity, RuntimeInfo.classloader), object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {

                    val adapter = param.args[0]
                    MainAdapter.listView = param.thisObject as ListView

                    if (RuntimeInfo.chatRoomViewPresenter.isStarted() || RuntimeInfo.officialViewPresenter.isStarted()) return

                    RuntimeInfo.chatRoomViewPresenter.setAdapter(adapter)
                    RuntimeInfo.officialViewPresenter.setAdapter(adapter)

                    RuntimeInfo.chatRoomViewPresenter.start()
                    RuntimeInfo.officialViewPresenter.start()
                }
            })
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }



    /**
     * 通过 Activity 的 LauncherUI 的DecorView 来获取子元素FitSystemWindowLayoutView 来添加群助手View
     */
    fun handleDetectFitWindowView(activity: Activity) {

        /**
         * 寻找并获得fitSystemView
         */
        fun addListenerForFitWindowView(parentView: ViewGroup, callBack: (result: Boolean) -> Unit) {
            for (index in 0 until parentView.childCount) {

                val childView = parentView.getChildAt(index)
                LogUtils.log("MainLauncherUI, handleDetectFitWindowView, getChildAt, child = $childView")
                if (childView::class.java.name == WXObject.MainUI.C.FitSystemWindowLayoutView) {
                    val fitSystemWindowLayoutView = childView as ViewGroup
                    addHelperViewToFitSystemView(fitSystemWindowLayoutView)

                    callBack(true)
                    return
                }
            }

            callBack(false)
        }

        LogUtils.log("MainLauncherUI, handleDetectFitWindowView, activity = $activity")
        if (activity.isDestroyed || activity.isFinishing) return

        val decorView = activity.window.decorView as ViewGroup?

        when {
            decorView == null -> {
                LogUtils.log("MainLauncherUI, handleDetectFitWindowView, decorView = null")
                Handler(launcherUI.mainLooper).postDelayed({
                    handleDetectFitWindowView(activity)
                }, 200)
            }
            decorView.childCount == 0 -> {
                LogUtils.log("MainLauncherUI, handleDetectFitWindowView, decorView, childCount = 0")
                Handler(launcherUI.mainLooper).postDelayed({
                    handleDetectFitWindowView(activity)
                }, 200)
            }
            decorView.childCount == 1 && decorView.getChildAt(0) is LinearLayout -> {
                LogUtils.log("MainLauncherUI, handleDetectFitWindowView, decorView, childCount = 1")
                val parentView = decorView.getChildAt(0) as ViewGroup
                addListenerForFitWindowView(parentView) {
                    if (!it) {
                        Handler(launcherUI.mainLooper).postDelayed({
                            handleDetectFitWindowView(activity)
                        }, 200)
                    }
                }
            }
            else -> {
                LogUtils.log("MainLauncherUI, handleDetectFitWindowView, decorView, childCount = more")
                addListenerForFitWindowView(decorView) {
                    if (!it) {
                        Handler(launcherUI.mainLooper).postDelayed({
                            handleDetectFitWindowView(activity)
                        }, 200)
                    }
                }
            }
        }

    }

    /**
     * 通过 FitSystemWindowLayoutView的构造函数 来添加群助手View
     */
    private fun fitSystemWindowConstructorHook() {
        fun execute(param: XC_MethodHook.MethodHookParam) {
            val fitSystemWindowLayoutView = param.thisObject as ViewGroup
            fitSystemWindowLayoutView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View, child: View) {

                    /**
                     * 成功获得fitSystemView 进行添加逻辑
                     */
                    addHelperViewToFitSystemView(fitSystemWindowLayoutView)
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {}
            })
        }

        val fitSystemWindowLayoutViewClass = XposedHelpers.findClass(WXObject.MainUI.C.FitSystemWindowLayoutView, RuntimeInfo.classloader)


        /**
         * 构造方法1
         */
        findAndHookConstructor(fitSystemWindowLayoutViewClass,
                Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val thisObject = param.thisObject

                        LogUtils.log("MainLauncherUI, FitSystemWindowLayoutView, 1 params, class  = ${thisObject::class.java}")
                        execute(param)
                    }
                })

        /**
         * 构造方法2
         */
        findAndHookConstructor(fitSystemWindowLayoutViewClass,
                Context::class.java,
                AttributeSet::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val thisObject = param.thisObject

                        LogUtils.log("MainLauncherUI, FitSystemWindowLayoutView, 2 params, class  = ${thisObject::class.java}")
                        execute(param)
                    }
                })
    }

    /**
     * 将助手View添加到微信的相应View中
     */
    private fun addHelperViewToFitSystemView(fitSystemWindowLayoutView: ViewGroup?) {
        if (fitSystemWindowLayoutView == null) return

        LogUtils.log("MainLauncherUI handleAddView, fitSystemWindowLayoutView = $fitSystemWindowLayoutView, fitSystemWindowLayoutView.childCount = ${fitSystemWindowLayoutView.childCount}")

        var containerPosition = -1
        var currentHelperPosition = -1

        for (index in 0 until fitSystemWindowLayoutView.childCount) {

            if (fitSystemWindowLayoutView.getChildAt(index)::class.java.name.contains("AbsoluteLayout")) {
                currentHelperPosition = index
            }

            if (fitSystemWindowLayoutView.getChildAt(index)::class.java.name.contains("TestTimeForChatting")) {
                containerPosition = index
            }
        }


        LogUtils.log("MainLauncherUI, containerPosition = $containerPosition, currentHelperPosition = $currentHelperPosition")

        if (containerPosition == -1) {
            return
        }

        if (currentHelperPosition == -1) {
            container = AbsoluteLayout(launcherUI)
            fitSystemWindowLayoutView.addView(container, containerPosition)
        } else {
            if (currentHelperPosition + 1 != containerPosition) {
                fitSystemWindowLayoutView.removeView(container)
                fitSystemWindowLayoutView.addView(container, containerPosition)
            }
        }

        onFitSystemWindowLayoutViewReady(fitSystemWindowLayoutView)

    }

    /**
     * 微信自1140之後，fitSystemWindowLayoutView 的 child 數量有所變化
     */
    private fun isWechatHighVersion(wechatVersion: Int): Boolean {
        return wechatVersion >= 1140
    }


    private fun hookDispatchKeyEvent(param: XC_MethodHook.MethodHookParam) {
        val keyEvent = param.args[0] as KeyEvent

        //手指离开返回键的事件
        if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {

            //群消息助手在屏幕上显示
            if (RuntimeInfo.currentPage == PageType.CHAT_ROOMS && RuntimeInfo.chatRoomViewPresenter.isShowing) {
                RuntimeInfo.chatRoomViewPresenter.dismiss()
                param.result = true

            }
            //公众号助手在屏幕上显示
            if (RuntimeInfo.currentPage == PageType.OFFICIAL && RuntimeInfo.officialViewPresenter.isShowing) {
                RuntimeInfo.officialViewPresenter.dismiss()
                param.result = true
            }
        }
    }


    private fun onFitSystemWindowLayoutViewReady(fitSystemWindowLayoutView: ViewGroup) {

        LogUtils.log("MainLauncherUI, onFitSystemWindowLayoutViewReady, fitSystemWindowLayoutView = $fitSystemWindowLayoutView")

        val chatRoomViewParent = RuntimeInfo.chatRoomViewPresenter.presenterView.parent
        if (chatRoomViewParent != null) {
            (chatRoomViewParent as ViewGroup).removeView(RuntimeInfo.chatRoomViewPresenter.presenterView)
        }

        val officialViewParent = RuntimeInfo.officialViewPresenter.presenterView.parent
        if (officialViewParent != null) {
            (chatRoomViewParent as ViewGroup).removeView(RuntimeInfo.officialViewPresenter.presenterView)
        }

        container?.apply {
            removeAllViews()
            addView(RuntimeInfo.chatRoomViewPresenter.presenterView)
            addView(RuntimeInfo.officialViewPresenter.presenterView)
        }

        LogUtils.log("MainLauncherUI, onFitSystemWindowLayoutViewReady, addViewFinish, container = $container, " +
                "chatRoom = ${RuntimeInfo.chatRoomViewPresenter.presenterView}" +
                "official = ${RuntimeInfo.officialViewPresenter.presenterView}")

        if ((fitSystemWindowLayoutView.getChildAt(0) as ViewGroup).childCount != 2)
            return
        val mainView = (fitSystemWindowLayoutView.getChildAt(0) as ViewGroup).getChildAt(1)


        LogUtils.log("MainLauncherUI, onFitSystemWindowLayoutViewReady, mainView = $mainView")

        //调整布局大小，解决部分虛擬按鍵手機的問題
        mainView.viewTreeObserver
                .addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener {
                    val left = mainView.left
                    val right = mainView.right
                    val top = mainView.top
                    val bottom = mainView.bottom

                    val width = right - left
                    val height = bottom - top

                    if (width == 0 || height == 0) return@OnGlobalLayoutListener

                    val chatRoomViewPresenterPresenterView = RuntimeInfo.chatRoomViewPresenter.presenterView
                    val officialViewPresenterPresenterView = RuntimeInfo.officialViewPresenter.presenterView


                    val left1 = chatRoomViewPresenterPresenterView.left
                    val top1 = chatRoomViewPresenterPresenterView.top
                    val right1 = chatRoomViewPresenterPresenterView.right
                    val bottom1 = chatRoomViewPresenterPresenterView.bottom

                    if (Rect(left1, top1, right1, bottom1) == Rect(left, top, right, bottom))
                        return@OnGlobalLayoutListener

                    val params = ViewGroup.MarginLayoutParams(width, height)
                    params.setMargins(0, top, 0, 0)

                    chatRoomViewPresenterPresenterView.layoutParams = params
                    officialViewPresenterPresenterView.layoutParams = params

                    LogUtils.log("MainLauncherUI, onFitSystemWindowLayoutViewReady, setLayoutParams = $mainView")
                })
    }


    fun refreshListMainUI() {

        val adapterClass = MainAdapter.originAdapter::class.java
        val adapterSuperclass = adapterClass.superclass

        //其实筛选有两个方法，混淆之后刚好在前面的那个是 notify 方法
        try {
            val notifyMethod = adapterSuperclass.methods.filter { it.parameterTypes.size == 1 }
                    .filter { Modifier.isFinal(it.modifiers) }
                    .first { it.parameterTypes[0].name == Boolean::class.java.name }

            NOTIFY_MAIN_LAUNCHER_UI_LIST_VIEW_FLAG = true
            notifyMethod.invoke(MainAdapter.originAdapter, false)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

    fun restartMainActivity() {
        val activity = launcherUI
        activity.finish()
        activity.startActivity(Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
        })
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}