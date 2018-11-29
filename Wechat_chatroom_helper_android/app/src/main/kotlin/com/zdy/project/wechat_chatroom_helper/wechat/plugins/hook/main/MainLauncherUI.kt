package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

@SuppressLint("StaticFieldLeak")
/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainLauncherUI {

    lateinit var launcherUI: Activity

    lateinit var decorView: ViewGroup
    var fitSystemWindowLayoutView: ViewGroup? = null

    fun executeHook() {

//        hookAllConstructors(RuntimeInfo.classloader.loadClass(WXObject.MainUI.C.FitSystemWindowLayoutView), object : XC_MethodHook() {
//
//            override fun afterHookedMethod(param: MethodHookParam) {
//                val fitSystemWindowLayoutView = param.thisObject as ViewGroup
//                fitSystemWindowLayoutView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
//                    override fun onChildViewAdded(parent: View, child: View) {
//                        handleAddView(fitSystemWindowLayoutView)
//                    }
//
//                    override fun onChildViewRemoved(parent: View?, child: View?) {}
//                })
//            }
//        })


        findAndHookMethod(WXObject.MainUI.C.LauncherUI, RuntimeInfo.classloader,
                WXObject.MainUI.M.DispatchKeyEventOfLauncherUI, KeyEvent::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookDispatchKeyEvent(param)
            }
        })

        findAndHookMethod(Activity::class.java, WXObject.MainUI.M.OnCreate, Bundle::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.thisObject::class.java.name == WXObject.MainUI.C.LauncherUI) {

                    launcherUI = param.thisObject as Activity
                    decorView = launcherUI.window.decorView as ViewGroup

                    decorView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                        override fun onChildViewRemoved(parent: View?, child: View?) {
                            LogUtils.log("MainLauncherUI, onChildViewRemoved, parent = $parent, child = $child")
                        }

                        override fun onChildViewAdded(parent: View?, child: View) {
                            LogUtils.log("MainLauncherUI, onChildViewAdded, parent = $parent, child = $child")

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
                }
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                LogUtils.log("MainLauncherUI, activity onCreate, ${param.thisObject::class.java.name}")

                if (param.thisObject::class.java.name == WXObject.MainUI.C.LauncherUI) {

                    LogUtils.log("MainLauncherUI, ChatRoomViewPresenter init")
                    launcherUI = param.thisObject as Activity

                    RuntimeInfo.chatRoomViewPresenter = ChatRoomViewPresenter(launcherUI, PageType.CHAT_ROOMS)
                    RuntimeInfo.officialViewPresenter = ChatRoomViewPresenter(launcherUI, PageType.OFFICIAL)
                }
            }
        })

        findAndHookMethod(Activity::class.java, WXObject.MainUI.M.OnResume, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                LogUtils.log("MainLauncherUI, activity onResume, ${param.thisObject::class.java.name}")
                handleAddView(fitSystemWindowLayoutView)
            }
        })

        findAndHookMethod(Activity::class.java, "onPause", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                LogUtils.log("MainLauncherUI, activity onPause, ${param.thisObject::class.java.name}")

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

        if (fitSystemWindowLayoutView.childCount != fitWindowChildCount) return
        if (fitSystemWindowLayoutView.getChildAt(0) !is LinearLayout) return
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
        val activity = launcherUI
        activity.finish()
        activity.startActivity(Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
        })
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}