package com.zdy.project.wechat_chatroom_helper.ui.helper.uisetting

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView.ChatRoomRecyclerViewAdapter
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import utils.AppSaveInfoUtils

/**
 * Created by zhudo on 2017/12/2.
 */
class PreviewFragment : Fragment() {

    private lateinit var thisActivity: UISettingActivity

    private lateinit var mRootView: ViewGroup

    private lateinit var mToolbarContainer: RelativeLayout

    private lateinit var mToolbar: Toolbar

    private lateinit var mRecyclerView: RecyclerView

    private lateinit var chatRoomRecyclerViewAdapter: ChatRoomRecyclerViewAdapter

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        thisActivity = context as UISettingActivity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = LayoutInflater.from(thisActivity).inflate(R.layout.fragment_preview, container, false) as ViewGroup
        mRootView.findViewById<LinearLayout>(R.id.fragment_preview_content).addView(initToolbar())
        mRootView.findViewById<LinearLayout>(R.id.fragment_preview_content).addView(initRecycler())

        notifyUIToChangeColor()

        return mRootView
    }


    private fun initToolbar(): View {
        mToolbarContainer = RelativeLayout(thisActivity)
        mToolbar = Toolbar(thisActivity)
        val height = ScreenUtils.dip2px(thisActivity, 48f)

        mToolbar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        mToolbar.setNavigationIcon(R.drawable.arrow_icon)


        mToolbar.title = "群消息助手"
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

        val imageView = ImageView(thisActivity)

        val params = RelativeLayout.LayoutParams(height, height)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        imageView.layoutParams = params
        imageView.setPadding(height / 5, height / 5, height / 5, height / 5)
        imageView.setImageResource(R.drawable.setting_icon)

        imageView.drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        mToolbarContainer.addView(mToolbar)
        mToolbarContainer.addView(imageView)

        return mToolbarContainer
    }

    private fun initRecycler(): RecyclerView {
        mRecyclerView = RecyclerView(thisActivity)

        chatRoomRecyclerViewAdapter = ChatRoomRecyclerViewAdapter(thisActivity)

        chatRoomRecyclerViewAdapter.data = arrayList()
        chatRoomRecyclerViewAdapter.notifyDataSetChanged()

        mRecyclerView.layoutManager = LinearLayoutManager(thisActivity)
        mRecyclerView.adapter = chatRoomRecyclerViewAdapter
        return mRecyclerView
    }


     fun notifyUIToChangeColor() {
        mToolbarContainer.setBackgroundColor(Color.parseColor("#" + AppSaveInfoUtils.toolbarColorInfo()))
        mRecyclerView.setBackgroundColor(Color.parseColor("#" + AppSaveInfoUtils.helperColorInfo()))

        chatRoomRecyclerViewAdapter.notifyDataSetChanged()
    }

    private fun arrayList(): ArrayList<ChatInfoModel> {
        val data = ArrayList<ChatInfoModel>()

        val element1 = ChatInfoModel()
        element1.nickname = "示例消息"
        element1.content = "这是一条消息内容"
        element1.time = "18:19"
        element1.unReadCount = 1
        data.add(element1)

        val element2 = ChatInfoModel()
        element2.nickname = "标题"
        element2.content = "这又是一条消息内容"
        element2.time = "18:20"
        element2.unReadCount = 0
        data.add(element2)
        return data
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}