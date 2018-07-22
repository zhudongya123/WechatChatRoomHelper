package com.zdy.project.wechat_chatroom_helper.test;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;

import com.google.gson.JsonArray;
import com.zdy.project.wechat_chatroom_helper.R;
import com.zdy.project.wechat_chatroom_helper.plugins.PluginEntry;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.DexClass;

import java.io.File;
import java.io.IOException;

import cn.bingoogolapple.swipebacklayout.MySwipeBackLayout;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import kotlin.reflect.jvm.internal.ReflectProperties;
import utils.AppSaveInfo;
import utils.FileUtils;

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


        try (ApkFile apkFile = new ApkFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WechatChatroomHelper/base.apk"))) {
            DexClass[] classes = apkFile.getDexClasses();


            JsonArray jsonArray = new JsonArray();

            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();


            for (DexClass dexClass : classes) {
                String classType = dexClass.getClassType();
                String className = classType.substring(1, classType.length() - 1).replace("/", ".");


                System.out.println(dexClass);
                jsonArray.add(dexClass.getClassType());


                try {
                    Class<?> clazz = PluginEntry.classloader.loadClass(className);
                    System.out.println(clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }


            FileUtils.Companion.putJsonValue("classData", jsonArray.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
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
