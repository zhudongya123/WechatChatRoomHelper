package com.zdy.project.wechat_chatroom_helper.network;

import okhttp3.OkHttpClient;

/**
 * Created by zhudo on 2017/8/11.
 */

public class ApiManager {

    private OkHttpClient okHttpClient;

    private static final ApiManager INSTANCE = new ApiManager();

    public static ApiManager getINSTANCE() {
        return INSTANCE;
    }

    public OkHttpClient getClient() {
        return okHttpClient;
    }

    private ApiManager() {
        okHttpClient = new OkHttpClient();
    }


    public static class UrlPath {
        public static String CLASS_MAPPING = "http://116.62.247.71:8080/wechat/class/mapping";
    }
}
