package com.zdy.project.wechat_chatroom_helper.ui;

import android.app.Application;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;


/**
 * Created by zhudo on 2017/7/25.
 */

public class MyApplication extends Application {


    private static MyApplication INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();

        Bugly.init(getApplicationContext(), "ed7bb0e103", false);


        INSTANCE = this;
    }

    public static MyApplication getINSTANCE() {
        return INSTANCE;
    }
}
