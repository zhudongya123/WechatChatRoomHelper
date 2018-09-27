package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.bingoogolapple.swipebacklayout.BGASwipeBackLayout2
import cn.bingoogolapple.swipebacklayout.MySwipeBackLayout
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.ConfigInfo
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.wechat.dialog.WhiteListDialogBuilder
import com.zdy.project.wechat_chatroom_helper.wechat.manager.AvatarMaker
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.main.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.message.MessageFactory
import network.ApiManager
import java.util.*


/**
 * Created by Mr.Zdy on 2017/8/27.
 */

class ChatRoomView(private val mContext: Context, mContainer: ViewGroup, private val pageType: Int) : ChatRoomContract.View {

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

        mRecyclerView = RecyclerView(mContext)
        mRecyclerView.id = android.R.id.list
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)

        mainView.addView(initToolbar())
        mainView.addView(mRecyclerView)
        mainView.isClickable = true

        mainView.setBackgroundColor(Color.parseColor("#" + ConfigInfo.helperColor))

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


    override fun setOnDialogItemClickListener(listener: ChatRoomRecyclerViewAdapter.OnDialogItemClickListener) {
        mAdapter.setOnDialogItemClickListener(listener)
    }


    override fun show() {
        show(ScreenUtils.getScreenWidth(mContext))
    }

    override fun dismiss() {
        dismiss(0)
    }

    override fun show(offest: Int) {
        swipeBackLayout.closePane()
    }

    override fun dismiss(offest: Int) {
        swipeBackLayout.openPane()
    }


    override fun init() {
        mAdapter = ChatRoomRecyclerViewAdapter(mContext)
        LogUtils.log("mRecyclerView = $mRecyclerView, mAdapter = $mAdapter")
        mRecyclerView.adapter = mAdapter
    }


    override fun refreshList(data: Any) {

        AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_OFFICIAL)

        val newDatas =
                if (pageType == PageType.CHAT_ROOMS) MessageFactory.getSpecChatRoom()
                else MessageFactory.getSpecOfficial()

        val oldDatas = mAdapter.data

//        val diffResult = DiffUtil.calculateDiff(DiffCallBack(newDatas, oldDatas), true)
//        diffResult.dispatchUpdatesTo(mAdapter)
        mAdapter.data = newDatas
        mAdapter.notifyDataSetChanged()

        LogUtils.log("showMessageRefresh for all recycler view , pageType = " + PageType.printPageType(pageType))
    }

    internal class DiffCallBack(private var mOldDatas: ArrayList<ChatInfoModel>,
                                private var mNewDatas: ArrayList<ChatInfoModel>) : DiffUtil.Callback() {

        init {
            LogUtils.log("oldData = ${mOldDatas.joinToString { it.toString() }}")
            LogUtils.log("newData = ${mNewDatas.joinToString { it.toString() }}")

        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                mOldDatas[oldItemPosition].field_username == mNewDatas[newItemPosition].field_username

        override fun getOldListSize() = mOldDatas.size

        override fun getNewListSize() = mNewDatas.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                mOldDatas[oldItemPosition] == mNewDatas[newItemPosition]


        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {

            val oldItem = mOldDatas[oldItemPosition]
            val newItem = mNewDatas[newItemPosition]

            LogUtils.log("getChangePayload, oldItem = $oldItem, newItem = $newItem")
            return null
        }
    }

    private fun initToolbar(): View {
        mToolbarContainer = RelativeLayout(mContext)

        mToolbar = Toolbar(mContext)

        val height = ScreenUtils.dip2px(mContext, 48f)

        mToolbar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)

        mToolbar.setNavigationOnClickListener { dismiss() }
        mToolbar.setBackgroundColor(Color.parseColor("#" + ConfigInfo.toolbarColor))
        mRecyclerView.setBackgroundColor(Color.parseColor("#" + ConfigInfo.helperColor))

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
            imageButton.layoutParams = layoutParams

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
        imageView.setImageDrawable(AvatarMaker.handleAvatarDrawable(mContext, pageType, 0x00000000))

        imageView.setOnClickListener {
            val whiteListDialogBuilder = WhiteListDialogBuilder()
            when (pageType) {
                PageType.OFFICIAL -> whiteListDialogBuilder.pageType = PageType.OFFICIAL
                PageType.CHAT_ROOMS -> whiteListDialogBuilder.pageType = PageType.CHAT_ROOMS
            }
            val dialog = whiteListDialogBuilder.getWhiteListDialog(mContext)
            dialog.show()
            dialog.setOnDismissListener {
                MainAdapter.originAdapter.notifyDataSetChanged()
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
