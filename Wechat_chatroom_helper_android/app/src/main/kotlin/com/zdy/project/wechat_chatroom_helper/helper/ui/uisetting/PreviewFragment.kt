package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomRecyclerViewAdapter
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import utils.AppSaveInfo

/**
 * 此类为 UiSetting 中的预览 Fragment
 *
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
        mRootView = (LayoutInflater.from(thisActivity).inflate(R.layout.fragment_preview, container, false) as ViewGroup)
                .apply {
                    findViewById<LinearLayout>(R.id.fragment_preview_content)
                            .also {
                                it.addView(initToolbar())
                                it.addView(initRecycler())
                            }
                }

        notifyUIToChangeColor()

        return mRootView
    }


    private fun initToolbar(): View {
        mToolbarContainer = RelativeLayout(thisActivity)

        val height = ScreenUtils.dip2px(thisActivity, 48f)

        mToolbar = Toolbar(thisActivity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            title = "群消息助手"
        }.also {
            it.setNavigationIcon(R.drawable.arrow_icon)
            it.setTitleTextColor(0xFFFFFFFF.toInt())
        }

        val clazz: Class<*> = Class.forName("android.widget.Toolbar")
        val mTitleTextView = clazz.getDeclaredField("mTitleTextView").apply { isAccessible = true }
        (mTitleTextView.get(mToolbar) as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

        (clazz.getDeclaredField("mNavButtonView").apply { isAccessible = true }
                .get(mToolbar) as ImageButton)
                .also {
                    (it.layoutParams as Toolbar.LayoutParams)
                            .also {
                                it.height = height
                                it.gravity = Gravity.CENTER
                            }
                    it.requestLayout()
        }

        val imageView = ImageView(thisActivity)
                .also {
                    it.setPadding(height / 5, height / 5, height / 5, height / 5)
                    it.setImageResource(R.drawable.setting_icon)
                }
                .apply {
                    layoutParams = RelativeLayout.LayoutParams(height, height).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT) }
                    drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                }

        mToolbarContainer.addView(mToolbar)
        mToolbarContainer.addView(imageView)

        return mToolbarContainer
    }

    private fun initRecycler(): RecyclerView {
        mRecyclerView = RecyclerView(thisActivity)
                .apply {
                    layoutManager = LinearLayoutManager(thisActivity)
                    chatRoomRecyclerViewAdapter = ChatRoomRecyclerViewAdapter(thisActivity)
                            .also {
                                it.data = arrayList()
                                it.notifyDataSetChanged()
                            }
                    adapter = chatRoomRecyclerViewAdapter
                }
        return mRecyclerView
    }


     fun notifyUIToChangeColor() {
        mToolbarContainer.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.toolbarColorInfo()))
        mRecyclerView.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.helperColorInfo()))
        chatRoomRecyclerViewAdapter.notifyDataSetChanged()
    }

    private fun arrayList() = ArrayList<ChatInfoModel>()
            .also {
                it.add(ChatInfoModel()
                        .apply {
                            nickname = "欢迎使用微信群消息助手"
                            content = "这是回话的消息内容"
                            time = "07:30"
                            unReadCount = 1
                        })
                it.add(ChatInfoModel()
                        .apply {
                            nickname = "Welcome to WechatChatRoomHelper"
                            content = "this is the content of the conversation"
                            time = "07:30"
                            unReadCount = 0
                        })
            }


}