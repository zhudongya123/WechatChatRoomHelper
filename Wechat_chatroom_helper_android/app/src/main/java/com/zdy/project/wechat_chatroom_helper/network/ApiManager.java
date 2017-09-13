package com.zdy.project.wechat_chatroom_helper.network;

import android.os.Build;

import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.zdy.project.wechat_chatroom_helper.network.ApiManager.UrlPath.CLASS_MAPPING;
import static com.zdy.project.wechat_chatroom_helper.network.ApiManager.UrlPath.ERROR_RECEIVER;
import static com.zdy.project.wechat_chatroom_helper.network.ApiManager.UrlPath.HOME_INFO;

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
        public static String ERROR_RECEIVER = "http://116.62.247.71:8080/wechat/error/receiver";
        public static String HOME_INFO = "http://116.62.247.71:8080/wechat/home/info";
    }


    public void sendRequestForCrashReport(File file) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        RequestBody requestBody = MultipartBody.create(MediaType.parse("text/txt"), file);


        builder.addFormDataPart("file", "crashFile", requestBody);

        builder.addFormDataPart("versionCode", String.valueOf(PreferencesUtils.getVersionCode()));
        builder.addFormDataPart("sdkVersion", String.valueOf(Build.VERSION.SDK_INT));
        builder.addFormDataPart("deviceName", Build.MODEL);

        MultipartBody multipartBody = builder.build();

        try {
            getINSTANCE().getClient().newCall(new Request.Builder().post(multipartBody).url(ERROR_RECEIVER).build()).execute();
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequestForHomeInfo(String helpVersion,Callback callback) {

        RequestBody requestBody = new FormBody.Builder()
                .add("versionCode", helpVersion)
                .build();

        final Request request = new Request.Builder()
                .url(HOME_INFO)
                .post(requestBody)
                .build();

        ApiManager.getINSTANCE().getClient().newCall(request).enqueue(callback);
    }
}
