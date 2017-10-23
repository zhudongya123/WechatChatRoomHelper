package com.zdy.project.wechat_chatroom_helper.network;

import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

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

import static com.zdy.project.wechat_chatroom_helper.network.ApiManager.UrlPath.ERROR_RECEIVER;
import static com.zdy.project.wechat_chatroom_helper.network.ApiManager.UrlPath.HOME_INFO;
import static com.zdy.project.wechat_chatroom_helper.network.ApiManager.UrlPath.USER_STATISTICS;

/**
 * Created by zhudo on 2017/8/11.
 */

public class ApiManager {

    private OkHttpClient okHttpClient=new OkHttpClient();

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

    private long sendTime = 0;


    public static class UrlPath {
        public static String CLASS_MAPPING = getHost() + "wechat/class/mapping";
        public static String ERROR_RECEIVER = getHost() + "wechat/error/receiver";
        public static String HOME_INFO = getHost() + "wechat/home/info";
        public static String USER_STATISTICS = getHost() + "wechat/user/statistics";
    }

    @NonNull
    private static String getHost() {
        return "http://116.62.247.71:8080/";
    }


    public void sendRequestForUserStatistics(String action, String uuid, String model) {

        if (System.currentTimeMillis() - sendTime < 60000) return;


        RequestBody requestBody = new FormBody.Builder()
                .add("action", action)
                .add("uuidCode", uuid)
                .add("model", model)
                .add("version", String.valueOf(PreferencesUtils.helper_versionCode()))
                .build();

        final Request request = new Request.Builder()
                .url(USER_STATISTICS)
                .post(requestBody)
                .build();

        ApiManager.getINSTANCE().getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.v(string, string);
                sendTime = System.currentTimeMillis();
            }
        });
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

    public void sendRequestForHomeInfo(String helpVersion, Callback callback) {

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
