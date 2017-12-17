package com.zdy.project.wechat_chatroom_helper.ui.helper.uisetting

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import utils.AppSaveInfoUtils

/**
 * Created by zhudo on 2017/12/2.
 */
class PreviewFragment : Fragment() {

    private lateinit var settingViewHolder: SettingViewModel

    private lateinit var thisActivity: UISettingActivity

    private lateinit var mRootView: ViewGroup

    private lateinit var mToolbarContainer: RelativeLayout

    private lateinit var mToolbar: Toolbar

    private lateinit var mRecyclerView: RecyclerView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        thisActivity = context as UISettingActivity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = LayoutInflater.from(thisActivity).inflate(R.layout.fragment_preview, container, false) as ViewGroup
        mRootView.findViewById<RelativeLayout>(R.id.fragment_preview_content).addView(initToolbar())
        return mRootView
    }


    private fun initToolbar(): View {
        mToolbarContainer = RelativeLayout(thisActivity)
        mToolbar = Toolbar(thisActivity)
        val height = ScreenUtils.dip2px(thisActivity, 48f)

        mToolbar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        mToolbar.setNavigationIcon(R.drawable.arrow_icon)

        mToolbar.setBackgroundColor(Color.parseColor("#" + AppSaveInfoUtils.toolbarColorInfo()))
//        mRecyclerView.setBackgroundColor(Color.parseColor("#" + AppSaveInfoUtils.helperColorInfo()))

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}