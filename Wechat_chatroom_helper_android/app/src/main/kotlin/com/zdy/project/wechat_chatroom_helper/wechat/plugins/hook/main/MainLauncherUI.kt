package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
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
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.log.LogRecord
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

    var fitSystemWindowLayoutView: ViewGroup? = null

    fun executeHook() {

        findAndHookMethod(Activity::class.java, WXObject.MainUI.M.OnCreate, Bundle::class.java, object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                LogUtils.log("MainLauncherUI, activity onCreate, ${param.thisObject::class.java.name}")

                if (param.thisObject::class.java.name == WXObject.MainUI.C.LauncherUI) {

                    LogUtils.log("MainLauncherUI, ChatRoomViewPresenter init")
                    launcherUI = param.thisObject as Activity

                    RuntimeInfo.chatRoomViewPresenter = ChatRoomViewPresenter(launcherUI, PageType.CHAT_ROOMS)
                    RuntimeInfo.officialViewPresenter = ChatRoomViewPresenter(launcherUI, PageType.OFFICIAL)

                 //   handleDetectFitWindowView(launcherUI)
                }
            }
        })

        findAndHookMethod(Activity::class.java, WXObject.MainUI.M.OnResume, object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject::class.java.name == WXObject.MainUI.C.LauncherUI) {

                    launcherUI = param.thisObject as Activity

               //     handleDetectFitWindowView(launcherUI)
                }
            }
        })

        findAndHookMethod(WXObject.MainUI.C.LauncherUI, RuntimeInfo.classloader,
                WXObject.MainUI.M.DispatchKeyEventOfLauncherUI, KeyEvent::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookDispatchKeyEvent(param)
            }
        })


        findAndHookMethod(WXObject.MainUI.C.LauncherUI, RuntimeInfo.classloader,
                WXObject.MainUI.M.StartChattingOfLauncherUI, String::class.java, Bundle::class.java, Boolean::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
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
                    override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
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


        findAndHookMethod(ConversationReflectFunction.conversationWithAppBrandListView, WXObject.Adapter.M.SetAdapter, ListAdapter::class.java, object : XC_MethodHook() {
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
            findAndHookMethod(ConversationReflectFunction.conversationListView, "setActivity",
                    XposedHelpers.findClass("com.tencent.mm.ui.MMFragmentActivity", RuntimeInfo.classloader), object : XC_MethodHook() {

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



        findAndHookMethod(ConversationReflectFunction.conversationWithAppBrandListView, WXObject.Adapter.M.SetAdapter, ListAdapter::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                MainAdapter.listView = param.thisObject as ListView
                val adapter = param.args[0]

                RuntimeInfo.chatRoomViewPresenter.setAdapter(adapter)
                RuntimeInfo.officialViewPresenter.setAdapter(adapter)

                RuntimeInfo.chatRoomViewPresenter.start()
                RuntimeInfo.officialViewPresenter.start()
            }
        })

        findAndHookConstructor(
                XposedHelpers.findClass(WXObject.MainUI.C.FitSystemWindowLayoutView, RuntimeInfo.classloader),
                Context::class.java,
                object : XC_MethodHook() {

                    override fun afterHookedMethod(param: MethodHookParam) {
                        fitSystemWindowLayoutView = param.thisObject as ViewGroup

                        handleAddView(fitSystemWindowLayoutView)

                        fitSystemWindowLayoutView?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                            override fun onChildViewAdded(parent: View, child: View) {
                                handleAddView(fitSystemWindowLayoutView)
                            }

                            override fun onChildViewRemoved(parent: View?, child: View?) {}
                        })
                    }
                })

        findAndHookConstructor(
                XposedHelpers.findClass(WXObject.MainUI.C.FitSystemWindowLayoutView, RuntimeInfo.classloader),
                Context::class.java,
                AttributeSet::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        fitSystemWindowLayoutView = param.thisObject as ViewGroup

                        handleAddView(fitSystemWindowLayoutView)

                        fitSystemWindowLayoutView?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                            override fun onChildViewAdded(parent: View, child: View) {
                                handleAddView(fitSystemWindowLayoutView)
                            }

                            override fun onChildViewRemoved(parent: View?, child: View?) {}
                        })
                    }
                })

        try {
            findAndHookMethod(ConversationReflectFunction.conversationListView, WXObject.Adapter.M.SetActivity,
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

    fun handleDetectFitWindowView(activity: Activity) {
        val decorView = activity.window.decorView as ViewGroup

        decorView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewRemoved(parent: View?, child: View?) {}

            override fun onChildViewAdded(parent: View?, child: View) {

                LogUtils.log("setOnHierarchyChangeListener, child = $child")

                if (child::class.java.name == WXObject.MainUI.C.FitSystemWindowLayoutView) {

                    fitSystemWindowLayoutView = child as ViewGroup

                    handleAddView(fitSystemWindowLayoutView)

                    fitSystemWindowLayoutView?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                        override fun onChildViewAdded(parent: View, child: View) {
                            handleAddView(fitSystemWindowLayoutView)
                        }

                        override fun onChildViewRemoved(parent: View?, child: View?) {}
                    })
                }

            }
        })

        handleAddView(fitSystemWindowLayoutView)
    }

    fun handleAddView(fitSystemWindowLayoutView: ViewGroup?) {


        if (fitSystemWindowLayoutView == null) return

        val chattingView: View//聊天View
        val chattingViewPosition: Int//聊天View的下標

        var fitWindowChildCount = 0//fitSystemWindowLayoutView的 child 数量
        var chatRoomViewPosition = 0
        var officialViewPosition = 0

        /*
         * 微信在某个版本之后 View 数量发生变化，下标也要相应刷新
         **/
        if (isWechatHighVersion(1140)) {

            fitWindowChildCount = 3
            chattingViewPosition = 2
            chatRoomViewPosition = 2
            officialViewPosition = 3

        } else {

            fitWindowChildCount = 2
            chattingViewPosition = 1
            chatRoomViewPosition = 1
            officialViewPosition = 2

        }
        LogUtils.log("handleAddView, fitSystemWindowLayoutView.childCount = ${fitSystemWindowLayoutView.childCount}")
        LogUtils.log("handleAddView, fitWindowChildCount = $fitWindowChildCount")


        if (fitSystemWindowLayoutView.childCount != fitWindowChildCount) return
//        if (fitSystemWindowLayoutView.getChildAt(0) !is LinearLayout) return
        chattingView = fitSystemWindowLayoutView.getChildAt(chattingViewPosition)
        if (chattingView.javaClass.simpleName != "TestTimeForChatting") return

        onFitSystemWindowLayoutViewReady(chatRoomViewPosition, officialViewPosition, fitSystemWindowLayoutView)
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


    fun onFitSystemWindowLayoutViewReady(chatRoomIndex: Int, officialIndex: Int, fitSystemWindowLayoutView: ViewGroup) {

        LogUtils.log("onFitSystemWindowLayoutViewReady, fitSystemWindowLayoutView = $fitSystemWindowLayoutView")

        val chatRoomViewParent = RuntimeInfo.chatRoomViewPresenter.presenterView.parent
        if (chatRoomViewParent != null) {
            (chatRoomViewParent as ViewGroup).removeView(RuntimeInfo.chatRoomViewPresenter.presenterView)
        }

        val officialViewParent = RuntimeInfo.officialViewPresenter.presenterView.parent
        if (officialViewParent != null) {
            (chatRoomViewParent as ViewGroup).removeView(RuntimeInfo.officialViewPresenter.presenterView)
        }

        fitSystemWindowLayoutView.addView(RuntimeInfo.chatRoomViewPresenter.presenterView, chatRoomIndex)
        fitSystemWindowLayoutView.addView(RuntimeInfo.officialViewPresenter.presenterView, officialIndex)


        if ((fitSystemWindowLayoutView.getChildAt(0) as ViewGroup).childCount != 2)
            return
        val mainView = (fitSystemWindowLayoutView.getChildAt(0) as ViewGroup).getChildAt(1)


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

                    val params = FrameLayout.LayoutParams(width, height)
                    params.setMargins(0, top, 0, 0)

                    chatRoomViewPresenterPresenterView.layoutParams = params
                    officialViewPresenterPresenterView.layoutParams = params
                })
    }


    fun refreshListMainUI() {

        val adapterClass = MainAdapter.originAdapter::class.java
        val adapterSuperclass = adapterClass.superclass

        //其实筛选有两个方法，混淆之后刚好在前面的那个是 notify 方法
        val notifyMethod = adapterSuperclass.methods.filter { it.parameterTypes.size == 1 }
                .filter { Modifier.isFinal(it.modifiers) }
                .first { it.parameterTypes[0].name == Boolean::class.java.name }

        NOTIFY_MAIN_LAUNCHER_UI_LIST_VIEW_FLAG = true
        notifyMethod.invoke(MainAdapter.originAdapter, false)

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