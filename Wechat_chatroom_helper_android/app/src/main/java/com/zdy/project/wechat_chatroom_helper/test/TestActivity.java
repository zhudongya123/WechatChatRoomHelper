package com.zdy.project.wechat_chatroom_helper.test;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;

import com.zdy.project.wechat_chatroom_helper.PageType;
import com.zdy.project.wechat_chatroom_helper.R;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;
import com.zdy.project.wechat_chatroom_helper.wechat.manager.DrawableMaker;

import cn.bingoogolapple.swipebacklayout.MySwipeBackLayout;

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
        mainView.setBackground(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{0xAA888888, 0x00888888}));

        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
                ScreenUtils.getScreenWidth(this), ViewGroup.LayoutParams.MATCH_PARENT, 0, 0);

        swipeBackLayout = new MySwipeBackLayout(this);
        swipeBackLayout.attachToView(mainView, this);
        content.addView(swipeBackLayout, params);


        ((ImageView) findViewById(R.id.image_1)).setImageDrawable(DrawableMaker.INSTANCE.handleAvatarDrawable(this, PageType.CHAT_ROOMS,0xF0f2B7F6,0xFF0FF000));
        ((ImageView) findViewById(R.id.image_2)).setImageDrawable(DrawableMaker.INSTANCE.handleAvatarDrawable(this, PageType.OFFICIAL,0xFF05CB00,0xFF000000));

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
