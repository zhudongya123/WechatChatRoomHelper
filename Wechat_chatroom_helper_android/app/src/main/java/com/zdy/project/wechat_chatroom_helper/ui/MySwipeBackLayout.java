package com.zdy.project.wechat_chatroom_helper.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import cn.bingoogolapple.swipebacklayout.BGASwipeBackLayout2;

/**
 * Created by Mr.Zdy on 2017/9/17.
 */

public class MySwipeBackLayout extends BGASwipeBackLayout2 {
    public MySwipeBackLayout(Context context) {
        this(context, null);
    }

    public MySwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mIsWeChatStyle = false;
    }


    public void attachToView(View childView, Context context) {

        setSliderFadeColor(Color.TRANSPARENT);

        mShadowView = new View(context);
        setIsNeedShowShadow(mIsNeedShowShadow);
        addView(mShadowView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mContentView = childView;
        addView(mContentView, 1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    protected boolean isSwipeBackEnable() {
        return mSwipeBackEnable;
    }


}
