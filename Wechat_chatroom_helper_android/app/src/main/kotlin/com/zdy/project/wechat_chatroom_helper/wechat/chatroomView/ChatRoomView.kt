package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.bingoogolapple.swipebacklayout.BGASwipeBackLayout2
import cn.bingoogolapple.swipebacklayout.MySwipeBackLayout
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.wechat.dialog.WhiteListDialogBuilder
import com.zdy.project.wechat_chatroom_helper.wechat.manager.DrawableMaker
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapterLongClick
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageFactory
import de.robv.android.xposed.XposedHelpers
import network.ApiManager


/**
 * Created by Mr.Zdy on 2017/8/27.
 */

class ChatRoomView(private val mContext: Context, mContainer: ViewGroup, private val pageType: Int) : ChatRoomContract.View {


    private lateinit var mLongClickListener: Any

    private lateinit var mPresenter: ChatRoomContract.Presenter
    private lateinit var swipeBackLayout: MySwipeBackLayout

    private val mainView: LinearLayout
    private val mRecyclerView: RecyclerView
    private lateinit var mToolbarContainer: ViewGroup
    private lateinit var mToolbar: Toolbar

    private lateinit var mAdapter: ChatRoomRecyclerViewAdapter

    private var uuid = "0"

    override val isShowing: Boolean get() = !swipeBackLayout.isOpen

    init {

        val params = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.MATCH_PARENT)

        mainView = LinearLayout(mContext)
        mainView.layoutParams = ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(mContext),
                ViewGroup.LayoutParams.MATCH_PARENT)
        mainView.orientation = LinearLayout.VERTICAL


        mRecyclerView = object : RecyclerView(mContext) {
            override fun dispatchTouchEvent(event: MotionEvent): Boolean {

                val rawX = event.rawX
                val rawY = event.rawY
                val coordinate = intArrayOf(rawX.toInt(), rawY.toInt())
                XposedHelpers.setObjectField(mLongClickListener, MainAdapterLongClick.CoordinateField.name, coordinate)

                return super.dispatchTouchEvent(event)
            }
        }
        mRecyclerView.id = android.R.id.list
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)

        mainView.addView(initToolbar())
        mainView.addView(mRecyclerView)
        mainView.isClickable = true

        mainView.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.helperColorInfo()))

        initSwipeBack()

        mContainer.addView(swipeBackLayout, params)

        uuid = DeviceUtils.getIMELCode(mContext)
        ApiManager.sendRequestForUserStatistics("init", uuid, Build.MODEL)

    }


    private fun initSwipeBack() {
        swipeBackLayout = MySwipeBackLayout(mContext)
        swipeBackLayout.attachToView(mainView, mContext)
        swipeBackLayout.setPanelSlideListener(object : BGASwipeBackLayout2.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
            }

            override fun onPanelOpened(panel: View) {
            }

            override fun onPanelClosed(panel: View) {

            }
        })
    }


    override fun setOnItemActionListener(listener: ChatRoomRecyclerViewAdapter.OnItemActionListener) {
        mAdapter.setOnItemActionListener(listener)
    }


    override fun show() {
        LogUtils.log("TrackHelperCan'tOpen, ChatRoomView -> show no params")
        show(ScreenUtils.getScreenWidth(mContext))
    }

    override fun dismiss() {
        dismiss(0)
    }

    override fun show(offest: Int) {
        LogUtils.log("TrackHelperCan'tOpen, ChatRoomView -> show, offest = ${offest}, swipeBackLayout = ${swipeBackLayout}")
        swipeBackLayout.closePane()
    }

    override fun dismiss(offest: Int) {
        swipeBackLayout.openPane()
    }

    override fun getCurrentData(): ArrayList<ChatInfoModel> {
        return mAdapter.data
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        mAdapter = ChatRoomRecyclerViewAdapter(mContext)
        LogUtils.log("mRecyclerView = $mRecyclerView, mAdapter = $mAdapter")
        mRecyclerView.adapter = mAdapter

        mLongClickListener = MainAdapterLongClick.getConversationLongClickClassConstructor()
                .newInstance(MainAdapter.originAdapter, MainAdapter.listView, MainLauncherUI.launcherUI, intArrayOf(300, 300))
        setOnItemActionListener(object : ChatRoomRecyclerViewAdapter.OnItemActionListener {
            override fun onItemClick(view: View, relativePosition: Int, chatInfoModel: ChatInfoModel) {
                XposedHelpers.callMethod(MainLauncherUI.launcherUI, WXObject.MainUI.M.StartChattingOfLauncherUI, chatInfoModel.field_username, null, true)
            }

            override fun onItemLongClick(view: View, relativePosition: Int, chatInfoModel: ChatInfoModel): Boolean {
                MainAdapterLongClick.onItemLongClickMethodInvokeGetItemFlagNickName = chatInfoModel.field_username.toString()
                XposedHelpers.callMethod(mLongClickListener, "onItemLongClick",
                        arrayOf(AdapterView::class.java, View::class.java, Int::class.java, Long::class.java),
                        MainAdapter.listView, view, 100000 + relativePosition + MainAdapter.listView.headerViewsCount, 0)
                return true
            }
        })


    }


    override fun refreshList(isForce: Boolean, data: Any?) {
        mainView.post {
            val newDatas =
                    if (pageType == PageType.CHAT_ROOMS) MessageFactory.getSpecChatRoom()
                    else MessageFactory.getSpecOfficial()

            mAdapter.data = newDatas
            mAdapter.notifyDataSetChanged()
        }
        LogUtils.log("showMessageRefresh for all recycler view , pageType = " + PageType.printPageType(pageType))
    }


    private fun initToolbar(): View {
        mToolbarContainer = RelativeLayout(mContext)

        mToolbar = Toolbar(mContext)

        val height = ScreenUtils.dip2px(mContext, 48f)

        mToolbar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        mToolbar.navigationIcon = BitmapDrawable(mContext.resources, DrawableMaker.getArrowBitMapForBack(Color.WHITE))

        mToolbar.setNavigationOnClickListener { dismiss() }
        mToolbar.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.toolbarColorInfo()))
        mRecyclerView.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.helperColorInfo()))

        when (pageType) {
            PageType.CHAT_ROOMS -> mToolbar.title = "群消息助手"
            PageType.OFFICIAL -> mToolbar.title = "服务号助手"
        }
        mToolbar.setTitleTextColor(-0x50506)

        val clazz: Class<*>
        try {
            clazz = Class.forName("android.widget.Toolbar")
            val mTitleTextView = clazz.getDeclaredField("mTitleTextView")
            mTitleTextView.isAccessible = true
            val textView = mTitleTextView.get(mToolbar) as TextView
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

            val mNavButtonView = clazz.getDeclaredField("mNavButtonView")
            mNavButtonView.isAccessible = true
            val imageButton = mNavButtonView.get(mToolbar) as ImageButton
            val layoutParams = imageButton.layoutParams
            layoutParams.height = height
            layoutParams.width = ScreenUtils.dip2px(mContext,56f)
            imageButton.layoutParams = layoutParams
            imageButton.scaleType = ImageView.ScaleType.FIT_CENTER

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        val imageView = ImageView(mContext)

        val params = RelativeLayout.LayoutParams(height, height)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        imageView.layoutParams = params
        val padding = height / 8
        imageView.setPadding(padding, padding, padding, padding)
        imageView.setImageDrawable(DrawableMaker.handleAvatarDrawable(mContext, pageType, 0x00000000))

        imageView.setOnClickListener {
            val whiteListDialogBuilder = WhiteListDialogBuilder()
            when (pageType) {
                PageType.OFFICIAL -> whiteListDialogBuilder.pageType = PageType.OFFICIAL
                PageType.CHAT_ROOMS -> whiteListDialogBuilder.pageType = PageType.CHAT_ROOMS
            }
            val dialog = whiteListDialogBuilder.getWhiteListDialog(mContext)
            dialog.show()
            dialog.setOnDismissListener {
                when (pageType) {
                    PageType.OFFICIAL -> RuntimeInfo.officialViewPresenter.refreshList(false, Any())
                    PageType.CHAT_ROOMS -> RuntimeInfo.chatRoomViewPresenter.refreshList(false, Any())
                }
                MainAdapter.notifyDataSetChangedForOriginAdapter()
            }
        }

        mToolbarContainer.addView(mToolbar)
        mToolbarContainer.addView(imageView)

        return mToolbarContainer
    }

    override fun setPresenter(presenter: ChatRoomContract.Presenter) {
        mPresenter = presenter
    }
}
