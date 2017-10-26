package cn.bingoogolapple.swipebacklayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;


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

    }

    public void attachToView(View childView, Context context) {

        setSliderFadeColor(Color.TRANSPARENT);

        mShadowView = new BGASwipeBackShadowView(((Activity) context));
        mShadowView.setIsWeChatStyle(true);
        mShadowView.setIsNeedShowShadow(true);
        addView(mShadowView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mContentView = childView;
        addView(mContentView, 1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setVisibility(INVISIBLE);

        mActivity = ((Activity) context);
    }


    @Override
    public boolean isSwipeBackEnable() {
        return !isOpen();
    }

    @Override
    public boolean closePane() {
        if (getVisibility() == INVISIBLE) setVisibility(VISIBLE);
        return super.closePane();
    }

}
