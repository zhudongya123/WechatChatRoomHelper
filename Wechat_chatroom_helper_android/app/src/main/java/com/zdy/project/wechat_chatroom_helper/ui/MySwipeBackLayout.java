package com.zdy.project.wechat_chatroom_helper.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils;

import cn.bingoogolapple.swipebacklayout.BGASwipeBackLayout2;

import static cn.bingoogolapple.swipebacklayout.UIUtil.NO_NAVIGATION_BAR_MODEL_SET;
import static cn.bingoogolapple.swipebacklayout.UIUtil.newCheckDeviceHasNavigationBar;

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

        setVisibility(INVISIBLE);

    }

    @Override
    protected boolean isSwipeBackEnable() {
        return mSwipeBackEnable;
    }

    @Override
    public boolean closePane() {
        if (getVisibility() == INVISIBLE) setVisibility(VISIBLE);
        return super.closePane();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            if (isInEditMode()) {
                // Don't crash the layout editor. Consume all of the space if specified
                // or pick a magic number from thin air otherwise.
                // TODO Better communication with tools of this bogus state.
                // It will crash on a real device.
                if (widthMode == MeasureSpec.AT_MOST) {
                    widthMode = MeasureSpec.EXACTLY;
                } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthMode = MeasureSpec.EXACTLY;
                    widthSize = 300;
                }
            } else {
                throw new IllegalStateException("Width must have an exact value or MATCH_PARENT");
            }
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            if (isInEditMode()) {
                // Don't crash the layout editor. Pick a magic number from thin air instead.
                // TODO Better communication with tools of this bogus state.
                // It will crash on a real device.
                if (heightMode == MeasureSpec.UNSPECIFIED) {
                    heightMode = MeasureSpec.AT_MOST;
                    heightSize = 300;
                }
            } else {
                throw new IllegalStateException("Height must not be UNSPECIFIED");
            }
        }

        int layoutHeight = 0;
        int maxLayoutHeight = -1;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                layoutHeight = maxLayoutHeight = heightSize - getPaddingTop() - getPaddingBottom();
                break;
            case MeasureSpec.AT_MOST:
                maxLayoutHeight = heightSize - getPaddingTop() - getPaddingBottom();
                break;
        }

        // ======================== 新加的 START ========================
      //  maxLayoutHeight -= getNavigationBarHeight((Activity) mContentView.getContext());
        // ======================== 新加的 END ========================

        float weightSum = 0;
        boolean canSlide = false;
        final int widthAvailable = widthSize - getPaddingLeft() - getPaddingRight();
        int widthRemaining = widthAvailable;
        final int childCount = getChildCount();

//        if (childCount > 2) {
//            Log.e(TAG, "onMeasure: More than two child views are not supported.");
//        }

        // We'll find the current one below.
        mSlideableView = null;

        // First pass. Measure based on child LayoutParams width/height.
        // Weight will incur a second pass.
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (child.getVisibility() == GONE) {
                lp.dimWhenOffset = false;
                continue;
            }

            if (lp.weight > 0) {
                weightSum += lp.weight;

                // If we have no width, weight is the only contributor to the final size.
                // Measure this view on the weight pass only.
                if (lp.width == 0) continue;
            }

            int childWidthSpec;
            final int horizontalMargin = lp.leftMargin + lp.rightMargin;
            if (lp.width == LayoutParams.WRAP_CONTENT) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(widthAvailable - horizontalMargin,
                        MeasureSpec.AT_MOST);
            } else if (lp.width == LayoutParams.MATCH_PARENT) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(widthAvailable - horizontalMargin,
                        MeasureSpec.EXACTLY);
            } else {
                childWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            }

            int childHeightSpec;
            if (lp.height == LayoutParams.WRAP_CONTENT) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(maxLayoutHeight, MeasureSpec.AT_MOST);
            } else if (lp.height == LayoutParams.MATCH_PARENT) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(maxLayoutHeight, MeasureSpec.EXACTLY);
            } else {
                childHeightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            }

            child.measure(childWidthSpec, childHeightSpec);
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            if (heightMode == MeasureSpec.AT_MOST && childHeight > layoutHeight) {
                layoutHeight = Math.min(childHeight, maxLayoutHeight);
            }

            widthRemaining -= childWidth;
            canSlide |= lp.slideable = widthRemaining < 0;
            if (lp.slideable) {
                mSlideableView = child;
            }
        }

        // Resolve weight and make sure non-sliding panels are smaller than the full screen.
        if (canSlide || weightSum > 0) {
            final int fixedPanelWidthLimit = widthAvailable - mOverhangSize;

            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);

                if (child.getVisibility() == GONE) {
                    continue;
                }

                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                if (child.getVisibility() == GONE) {
                    continue;
                }

                final boolean skippedFirstPass = lp.width == 0 && lp.weight > 0;
                final int measuredWidth = skippedFirstPass ? 0 : child.getMeasuredWidth();
                if (canSlide && child != mSlideableView) {
                    if (lp.width < 0 && (measuredWidth > fixedPanelWidthLimit || lp.weight > 0)) {
                        // Fixed panels in a sliding configuration should
                        // be clamped to the fixed panel limit.
                        final int childHeightSpec;
                        if (skippedFirstPass) {
                            // Do initial height measurement if we skipped measuring this view
                            // the first time around.
                            if (lp.height == LayoutParams.WRAP_CONTENT) {
                                childHeightSpec = MeasureSpec.makeMeasureSpec(maxLayoutHeight,
                                        MeasureSpec.AT_MOST);
                            } else if (lp.height == LayoutParams.MATCH_PARENT) {
                                childHeightSpec = MeasureSpec.makeMeasureSpec(maxLayoutHeight,
                                        MeasureSpec.EXACTLY);
                            } else {
                                childHeightSpec = MeasureSpec.makeMeasureSpec(lp.height,
                                        MeasureSpec.EXACTLY);
                            }
                        } else {
                            childHeightSpec = MeasureSpec.makeMeasureSpec(
                                    child.getMeasuredHeight(), MeasureSpec.EXACTLY);
                        }
                        final int childWidthSpec = MeasureSpec.makeMeasureSpec(
                                fixedPanelWidthLimit, MeasureSpec.EXACTLY);
                        child.measure(childWidthSpec, childHeightSpec);
                    }
                } else if (lp.weight > 0) {
                    int childHeightSpec;
                    if (lp.width == 0) {
                        // This was skipped the first time; figure out a real height spec.
                        if (lp.height == LayoutParams.WRAP_CONTENT) {
                            childHeightSpec = MeasureSpec.makeMeasureSpec(maxLayoutHeight,
                                    MeasureSpec.AT_MOST);
                        } else if (lp.height == LayoutParams.MATCH_PARENT) {
                            childHeightSpec = MeasureSpec.makeMeasureSpec(maxLayoutHeight,
                                    MeasureSpec.EXACTLY);
                        } else {
                            childHeightSpec = MeasureSpec.makeMeasureSpec(lp.height,
                                    MeasureSpec.EXACTLY);
                        }
                    } else {
                        childHeightSpec = MeasureSpec.makeMeasureSpec(
                                child.getMeasuredHeight(), MeasureSpec.EXACTLY);
                    }

                    if (canSlide) {
                        // Consume available space
                        final int horizontalMargin = lp.leftMargin + lp.rightMargin;
                        final int newWidth = widthAvailable - horizontalMargin;
                        final int childWidthSpec = MeasureSpec.makeMeasureSpec(
                                newWidth, MeasureSpec.EXACTLY);
                        if (measuredWidth != newWidth) {
                            child.measure(childWidthSpec, childHeightSpec);
                        }
                    } else {
                        // Distribute the extra width proportionally similar to LinearLayout
                        final int widthToDistribute = Math.max(0, widthRemaining);
                        final int addedWidth = (int) (lp.weight * widthToDistribute / weightSum);
                        final int childWidthSpec = MeasureSpec.makeMeasureSpec(
                                measuredWidth + addedWidth, MeasureSpec.EXACTLY);
                        child.measure(childWidthSpec, childHeightSpec);
                    }
                }
            }
        }

        final int measuredWidth = widthSize;
        final int measuredHeight = layoutHeight + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(measuredWidth, measuredHeight);
        mCanSlide = canSlide;

        if (mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE && !canSlide) {
            // Cancel scrolling in progress, it's no longer relevant.
            mDragHelper.abort();
        }
    }


    private static int getNavigationBarHeight(Activity activity) {
        int navigationBarHeight = 0;
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier(resources.getConfiguration().orientation == Configuration
                        .ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen",
                "android");
        if (resourceId > 0 && checkDeviceHasNavigationBar(activity)) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        }

        Log.v("Xposed", "navigationBarHeight = " + String.valueOf(navigationBarHeight));

        return navigationBarHeight;
    }

    private static boolean checkDeviceHasNavigationBar(Activity activity) {

        boolean hasNavigationBar = false;

        if (PreferencesUtils.getForceHideNaviBar()) return false;

        if (NO_NAVIGATION_BAR_MODEL_SET.contains(Build.MODEL)) {
            hasNavigationBar = false;
        } else {
            hasNavigationBar = newCheckDeviceHasNavigationBar(activity);
        }
        return hasNavigationBar;
    }
}
