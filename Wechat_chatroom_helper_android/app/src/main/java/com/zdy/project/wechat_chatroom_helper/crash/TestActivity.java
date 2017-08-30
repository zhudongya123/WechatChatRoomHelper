package com.zdy.project.wechat_chatroom_helper.crash;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

/**
 * Created by Zdy on 2016/12/16.
 */

public class TestActivity extends BaseActivity {


    FrameLayout container;
    LinearLayout contentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = new Button(thisActivity);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

        button.setLayoutParams(new ViewGroup.LayoutParams(ScreenUtils.dip2px(48), ScreenUtils.dip2px(48)));
        container = new FrameLayout(thisActivity);
        container.addView(button);

        setContentView(container);

        contentView = new LinearLayout(thisActivity);
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ScreenUtils.getScreenWidth(thisActivity)
                        + ScreenUtils.dip2px(thisActivity, 16), ViewGroup.LayoutParams.MATCH_PARENT);

        View maskView = new View(thisActivity);
        maskView.setLayoutParams(new ViewGroup.LayoutParams(ScreenUtils.dip2px(thisActivity, 16),
                ViewGroup.LayoutParams.MATCH_PARENT));
        maskView.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, new int[]{0x55000000, 0x2A000000, 0x00000000}));

        LinearLayout mainView = new LinearLayout(thisActivity);
        mainView.setLayoutParams(new ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(thisActivity),
                ViewGroup.LayoutParams.MATCH_PARENT));
        mainView.setOrientation(LinearLayout.VERTICAL);


        mainView.setBackground(new ColorDrawable(ContextCompat.getColor(thisActivity, R.color.colorPrimary)));

        contentView.addView(maskView);
        contentView.addView(mainView);
        container.addView(contentView, params);


        button.setText("open");

        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float translationX = contentView.getTranslationX();
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        JJMLog.d("action = " + "ACTION_DOWN");
                        moveX = x;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        JJMLog.d("action = " + "ACTION_MOVE");
                        float v1 = x - moveX;
                        float value = translationX + v1;
                        if (value >= -ScreenUtils.dip2px(16)
                                && value <= ScreenUtils.getScreenWidth(thisActivity)) {
                            contentView.setTranslationX(value);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        JJMLog.d("action = " + "ACTION_UP");


                        setResetAnim(translationX);

                        break;
                }

                return false;
            }
        });

    }

    private void setResetAnim(float translationX) {
        JJMLog.d("translationX = " + translationX + "");
        int screenWidth = ScreenUtils.getScreenWidth(thisActivity);
        if (translationX >= screenWidth / 2) {
            dismiss(((int) translationX));
        } else {
            show(((int) translationX));
        }
    }

    private float moveX;

    @Override
    public void onBackPressed() {
        dismiss();
    }


    public void show(int offest) {
        //      if (contentView.getVisibility() == View.VISIBLE) return;

        contentView.setVisibility(View.VISIBLE);

      //  int duration = 200 * offest / (ScreenUtils.getScreenWidth(thisActivity) + ScreenUtils.dip2px(thisActivity, 16));

        ValueAnimator animator = ValueAnimator.ofInt(offest, -ScreenUtils.dip2px(thisActivity, 16));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                contentView.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        animator.setDuration(200);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setTarget(contentView);
        animator.start();
    }

    public void show() {
        show(1080);
    }


    public void dismiss() {
        dismiss(-ScreenUtils.dip2px(thisActivity, 16));
    }

    public void dismiss(int offest) {
        if (contentView.getVisibility() == View.GONE) return;

        int screenWidth = ScreenUtils.getScreenWidth(thisActivity);
     //   int duration = 200 * (screenWidth - offest) / (screenWidth + ScreenUtils.dip2px(thisActivity, 16));
        ValueAnimator animator = ValueAnimator.ofInt(offest, screenWidth);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                contentView.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                contentView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setTarget(contentView);
        animator.setDuration(Math.abs(200));
        animator.start();
    }
}
