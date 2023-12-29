package com.zdy.project.wechat_chatroom_helper.helper.ui.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseFragment : Fragment() {

    protected lateinit var mContentView: ViewGroup
    protected lateinit var thisActivity: Activity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        thisActivity = context as Activity
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(getLayoutResourceId(), container, false) as ViewGroup
        bindView()
        return mContentView

    }

    abstract fun bindView()

    abstract fun getLayoutResourceId(): Int

}