package com.zdy.project.wechat_chatroom_helper.crash;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zdy.project.wechat_chatroom_helper.R;
import com.zdy.project.wechat_chatroom_helper.ui.MySwipeBackLayout;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import java.util.IllegalFormatCodePointException;

import cn.bingoogolapple.swipebacklayout.BGASwipeBackLayout2;

/**
 * Created by Zdy on 2016/12/16.
 */

public class TestActivity extends Activity {


    private Button button;
    private AbsoluteLayout content;
    MySwipeBackLayout swipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_test);

        button = (Button) findViewById(R.id.button);
        content = (AbsoluteLayout) findViewById(R.id.content);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("TestActivity", "button onClick");
                swipeBackLayout.closePane();
            }
        });
        View mainView = new View(this);

        mainView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mainView.setBackground(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
                new int[]{0xFF000000, 0x2A000000, 0xFF000000}));


        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
                ScreenUtils.getScreenWidth(this), ViewGroup.LayoutParams.MATCH_PARENT, 0, 0);

        swipeBackLayout = new MySwipeBackLayout(this);
        swipeBackLayout.attachToView(mainView, this);
        content.addView(swipeBackLayout, params);

//        swipeBackLayout.setPanelSlideListener(new BGASwipeBackLayout2.PanelSlideListener() {
//            @Override
//            public void onPanelSlide(View panel, float slideOffset) {
//
//            }
//
//            @Override
//            public void onPanelOpened(View panel) {
//                content.setClickable(false);
//            }
//
//            @Override
//            public void onPanelClosed(View panel) {
//                content.setClickable(true);
//            }
//        });


        swipeBackLayout.openPane();

    }

    @Override
    public void onBackPressed() {
        if (!swipeBackLayout.isOpen()) {

            swipeBackLayout.openPane();
            return;
        }

        super.onBackPressed();
    }
}
