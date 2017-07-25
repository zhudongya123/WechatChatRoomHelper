package com.zdy.project.wechat_chatroom_helper.ui;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zdy.project.wechat_chatroom_helper.R;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private FrameLayout fragmentContent;

    SettingFragment settingFragment;

    int versionCode;
    String versionName;


    private TextView textView;
    private Button button;
    private TextView detail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        detail = (TextView) findViewById(R.id.detail);

        fragmentContent = (FrameLayout) findViewById(R.id.fragment_content);

        final SharedPreferences sharedPreferences = getSharedPreferences(this.getPackageName() + "_preferences",
                MODE_WORLD_READABLE);


        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {

            if (packageInfo.packageName.equals("com.tencent.mm")) {
                versionCode = packageInfo.versionCode;
                versionName = packageInfo.versionName;
            }
        }

        int saveVersionCode = sharedPreferences.getInt("saveVersionCode", 0);

        if (versionCode != saveVersionCode) {

            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody requestBody = new FormBody.Builder().add("versionCode", String.valueOf(versionCode)).build();

            final Request request = new Request.Builder().url("http://116.62.247.71:8080/wechat/class/mapping").post
                    (requestBody)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    setFailText(versionName + "(" + versionCode + ")");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String string = response.body().string();
                    Log.v("result = ", string);

                    JsonObject jsonObject = new JsonParser().parse(string).getAsJsonObject();

                    int code = jsonObject.get("code").getAsInt();

                    if (code == 0) {
                        SharedPreferences.Editor edit = sharedPreferences.edit();
                        edit.putString("json", jsonObject.get("data").toString());
                        edit.putInt("saveVersionCode", versionCode);
                        edit.apply();

                        setSuccessText(versionName + "(" + versionCode + ")");
                    } else setFailText(versionName + "(" + versionCode + ")");
                }


            });
        } else setSuccessText(versionName + "(" + versionCode + ")");


        settingFragment = new SettingFragment();
        getFragmentManager().beginTransaction().replace(fragmentContent.getId(), settingFragment).commit();
    }

    private void setFailText(final String versionInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detail.setText("当前微信版本" + versionInfo + "暂未成功适配，请等待开发者适配。");
                detail.setTextColor(0xFFFF0000);
            }
        });

    }

    private void setSuccessText(final String versionInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detail.setText("微信版本" + versionInfo + "已经成功适配，如未有效果，请重启微信客户端查看。");
            }
        });
    }

    public static class SettingFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.pref_setting);
        }
    }


}
