package com.zdy.project.wechat_chatroom_helper.plugins.main.main

import android.annotation.SuppressLint
import android.app.Activity
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
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry.Companion.chatRoomViewPresenter
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry.Companion.officialViewPresenter
import com.zdy.project.wechat_chatroom_helper.wechat.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomViewPresenter
import com.zdy.project.wechat_chatroom_helper.wechat.manager.RuntimeInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

@SuppressLint("StaticFieldLeak")
/**
 * Created by Mr.Zdy on 2018/4/1.
 */
object MainLauncherUI {

    lateinit var launcherUI: Activity

    fun executeHook() {

        hookAllConstructors(PluginEntry.classloader.loadClass(WXObject.MainUI.C.FitSystemWindowLayoutView), object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                val fitSystemWindowLayoutView = param.thisObject as ViewGroup
                fitSystemWindowLayoutView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewAdded(parent: View, child: View) {

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

                    override fun onChildViewRemoved(parent: View?, child: View?) {}
                })
            }
        })


        findAndHookMethod(WXObject.MainUI.C.LauncherUI, PluginEntry.classloader,
                WXObject.MainUI.M.DispatchKeyEventOfLauncherUI, KeyEvent::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                hookDispatchKeyEvent(param)
            }
        })

        findAndHookMethod(Activity::class.java, WXObject.MainUI.M.OnCreate, Bundle::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                launcherUI = param.thisObject as Activity

                PluginEntry.chatRoomViewPresenter = ChatRoomViewPresenter(launcherUI, PageType.CHAT_ROOMS)
                PluginEntry.officialViewPresenter = ChatRoomViewPresenter(launcherUI, PageType.OFFICIAL)
            }
        })

        findAndHookMethod(WXObject.MainUI.C.LauncherUI, PluginEntry.classloader,
                WXObject.MainUI.M.StartChattingOfLauncherUI, String::class.java, Bundle::class.java, Boolean::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                        LogUtils.log("MainLauncherUI, startChatting")
                        if (RuntimeInfo.currentPage == PageType.OFFICIAL) RuntimeInfo.currentPage = PageType.CHATTING_WITH_OFFICIAL
                        else if (RuntimeInfo.currentPage == PageType.CHAT_ROOMS) RuntimeInfo.currentPage = PageType.CHATTING_WITH_CHAT_ROOMS
                    }
                })


        findAndHookMethod(WXObject.MainUI.C.LauncherUI, PluginEntry.classloader,
                WXObject.MainUI.M.CloseChattingOfLauncherUI, Boolean::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                        LogUtils.log("MainLauncherUI, closeChatting")
                        if (RuntimeInfo.currentPage == PageType.CHATTING_WITH_OFFICIAL) RuntimeInfo.currentPage = PageType.OFFICIAL
                        else if (RuntimeInfo.currentPage == PageType.CHATTING_WITH_CHAT_ROOMS) RuntimeInfo.currentPage = PageType.CHAT_ROOMS
                    }
                })
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
            if (RuntimeInfo.currentPage == PageType.CHAT_ROOMS && chatRoomViewPresenter.isShowing) {
                chatRoomViewPresenter.dismiss()
                param.result = true

            }
            //公众号助手在屏幕上显示
            if (RuntimeInfo.currentPage == PageType.OFFICIAL && officialViewPresenter.isShowing) {
                officialViewPresenter.dismiss()
                param.result = true
            }
        }
    }


    fun onFitSystemWindowLayoutViewReady(chatRoomIndex: Int, officialIndex: Int, fitSystemWindowLayoutView: ViewGroup) {

        val chatRoomViewParent = PluginEntry.chatRoomViewPresenter.presenterView.parent
        if (chatRoomViewParent != null) {
            (chatRoomViewParent as ViewGroup).removeView(PluginEntry.chatRoomViewPresenter.presenterView)
        }

        val officialViewParent = PluginEntry.officialViewPresenter.presenterView.parent
        if (officialViewParent != null) {
            (chatRoomViewParent as ViewGroup).removeView(PluginEntry.officialViewPresenter.presenterView)
        }

        fitSystemWindowLayoutView.addView(PluginEntry.chatRoomViewPresenter.presenterView, chatRoomIndex)
        fitSystemWindowLayoutView.addView(PluginEntry.officialViewPresenter.presenterView, officialIndex)


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

                    val chatRoomViewPresenterPresenterView = PluginEntry.chatRoomViewPresenter.presenterView
                    val officialViewPresenterPresenterView = PluginEntry.officialViewPresenter.presenterView


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


}