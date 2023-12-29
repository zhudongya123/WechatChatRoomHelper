package com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.wechat.chatroomView.ChatRoomRecyclerViewAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.manager.DrawableMaker
import java.text.SimpleDateFormat
import java.util.*

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        thisActivity = context as UISettingActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        val tintColor = Color.parseColor("#" + AppSaveInfo.nicknameColorInfo(thisActivity))

        mToolbar = Toolbar(thisActivity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            title = "群消息助手"
        }.also {
            it.navigationIcon = BitmapDrawable(thisActivity.resources, DrawableMaker.getArrowBitMapForBack(tintColor))
            it.setTitleTextColor(tintColor)
        }

        val clazz: Class<*> = Toolbar::class.java
        val mTitleTextView = clazz.getDeclaredField("mTitleTextView").apply { isAccessible = true }
        (mTitleTextView.get(mToolbar) as TextView).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(tintColor)
        }

        val imageButton = clazz.getDeclaredField("mNavButtonView").apply { isAccessible = true }
                .get(mToolbar) as ImageButton
        imageButton
                .also {
                    (it.layoutParams as Toolbar.LayoutParams)
                            .also {
                                it.height = height
                                it.width = ScreenUtils.dip2px(thisActivity, 56f)
                                it.gravity = Gravity.CENTER
                            }
                    it.requestLayout()
                }
        imageButton.scaleType = ImageView.ScaleType.FIT_CENTER

        val imageView = ImageView(thisActivity)
                .also {
                    it.setPadding(height / 5, height / 5, height / 5, height / 5)
                    it.setImageResource(R.drawable.setting_icon)
                }
                .apply {
                    layoutParams = RelativeLayout.LayoutParams(height, height).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT) }
                    drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
                }

        mToolbarContainer.addView(mToolbar)
        mToolbarContainer.addView(imageView)

        return mToolbarContainer
    }

    private fun initRecycler(): RecyclerView {
        mRecyclerView = RecyclerView(thisActivity)
                .apply {
                    layoutManager =
                        LinearLayoutManager(
                            thisActivity
                        )
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
        mToolbarContainer.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.toolbarColorInfo(thisActivity)))
        mRecyclerView.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.helperColorInfo(thisActivity)))
        chatRoomRecyclerViewAdapter.notifyDataSetChanged()
    }

    private fun arrayList() = ArrayList<ChatInfoModel>()
            .also {
                val timeString = SimpleDateFormat("HH:mm:ss", Locale.CHINESE).format(Calendar.getInstance().time)

                it.add(ChatInfoModel()
                        .apply {
                            nickname = "欢迎使用微信群消息助手"
                            content = "这是会话的消息内容"
                            conversationTime = timeString
                            unReadCount = 1
                            field_username = ""
                            stickyFlag = 1
                        })
                it.add(ChatInfoModel()
                        .apply {
                            nickname = "Welcome to WechatChatRoomHelper"
                            content = "this is the content of the conversation"
                            conversationTime = timeString
                            unReadCount = 0
                            field_username = ""

                        })
            }


}