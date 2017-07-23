package com.zdy.project.wechat_chatroom_helper.utils;


import de.robv.android.xposed.XSharedPreferences;

public class PreferencesUtils {

    private static XSharedPreferences instance = null;

    private static XSharedPreferences getInstance() {
        if (instance == null) {
            instance = new XSharedPreferences("com.zdy.project.wechat_chatroom_helper");
            instance.makeWorldReadable();
        } else {
            instance.reload();
        }
        return instance;
    }

    public static boolean open() {
        return getInstance().getBoolean("open", false);
    }

    public static boolean auto_close() {
        return getInstance().getBoolean("auto_close", false);
    }


}


