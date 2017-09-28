package com.zdy.project.wechat_chatroom_helper.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ExpandedMenuView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zdy.project.wechat_chatroom_helper.Constants;
import com.zdy.project.wechat_chatroom_helper.R;
import com.zdy.project.wechat_chatroom_helper.network.ApiManager;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.zdy.project.wechat_chatroom_helper.network.ApiManager.UrlPath.CLASS_MAPPING;

public class MainActivity extends AppCompatActivity {


    SettingFragment settingFragment;

    SharedPreferences sharedPreferences;


    private TextView textView;
    private Button button;
    private TextView detail;

    AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        detail = (TextView) findViewById(R.id.detail);

        FrameLayout fragmentContent = (FrameLayout) findViewById(R.id.fragment_content);
        sharedPreferences = getSharedPreferences(this.getPackageName() + "_preferences", MODE_WORLD_READABLE);


        settingFragment = new SettingFragment();
        getFragmentManager().beginTransaction().replace(fragmentContent.getId(), settingFragment).commit();

        ApiManager.getINSTANCE().sendRequestForHomeInfo(String.valueOf(getHelperVersionCode(MainActivity.this)),
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        final String result = response.body().string();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                WebView webView = new WebView(MainActivity.this);
                                webView.loadData(result, "text/html; charset=UTF-8", null);
                                alertDialog = new AlertDialog.Builder(MainActivity.this)
                                        .setView(webView).create();

                            }
                        });
                    }
                });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertDialog != null)
                    alertDialog.show();
            }
        });


        //是否已经适配了合适的数据 的标记
        boolean has_suit_wechat_data = sharedPreferences.getBoolean("has_suit_wechat_data", false);

        //play开关是否打开 的标记
        boolean play_version = sharedPreferences.getBoolean("play_version", false);

        //当前主程序的版本号
        int helper_versionCode = sharedPreferences.getInt("helper_versionCode", 0);

        //当前保存的微信版本号
        int wechat_version = sharedPreferences.getInt("wechat_version", 0);


        int wechatVersionCode = getWechatVersionCode();

        //如果没有适合的数据，或者刚刚更新了主程序版本
        if ((wechatVersionCode != wechat_version && wechat_version != 0) || !has_suit_wechat_data
                || helper_versionCode != getHelperVersionCode(this)) {

            //则发送数据请求
            sendRequest(wechatVersionCode, play_version);
        } else {

            //否则则取出上次保存的合适的信息
            detail.setTextColor(0xFF888888);
            detail.setText(sharedPreferences.getString("show_info", ""));
        }
    }

    /**
     * 请求服务器配置
     *
     * @param versionCode  微信的版本号
     * @param play_version 是否为play版本
     */
    public void sendRequest(int versionCode, boolean play_version) {

        RequestBody requestBody = new FormBody.Builder()
                .add("versionCode", String.valueOf(versionCode))
                .add("isPlayVersion", play_version ? "1" : "0")
                .build();

        final Request request = new Request.Builder()
                .url(CLASS_MAPPING)
                .post(requestBody)
                .build();

        ApiManager.getINSTANCE().getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                setFailText("从服务器获取数据失败，请检查网络后再试");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String string = response.body().string();
                    Log.v("result = ", string);

                    JsonObject jsonObject = new JsonParser().parse(string).getAsJsonObject();

                    int code = jsonObject.get("code").getAsInt();
                    String msg = jsonObject.get("msg").getAsString();

                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    if (code == 0) {
                        edit.putString("json", jsonObject.get("data").toString());
                        setSuccessText(msg);

                    } else {
                        edit.putString("json", "");
                        setFailText(msg);
                    }

                    edit.putInt("helper_versionCode", getHelperVersionCode(MainActivity.this));
                    edit.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                    setFailText("从服务器获取数据失败，请联系开发者解决问题");
                }
            }

        });
    }

    private void setFailText(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detail.setText(msg);
                detail.setTextColor(0xFFFF0000);


                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("has_suit_wechat_data", false);
                edit.putString("show_info", msg);
                edit.apply();
            }
        });
    }

    private void setSuccessText(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detail.setTextColor(0xFF888888);
                detail.setText(msg);

                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("has_suit_wechat_data", true);
                edit.putInt("wechat_version", getWechatVersionCode());
                edit.putString("show_info", msg);
                edit.apply();
            }
        });
    }

    private int getWechatVersionCode() {

        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        try {
            for (PackageInfo packageInfo : packageInfoList) {

                if (packageInfo.packageName.equals("com.tencent.mm")) {
                    if (packageInfo.versionName.equals("6.5.14"))
                        return 1120;
                    return packageInfo.versionCode;
                }
            }
            return 1060;

        } catch (Throwable e) {
            e.printStackTrace();
            return 1060;
        }
    }

    private int getHelperVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        int versionCode = 0;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    public static class SettingFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.pref_setting);

            final EditTextPreference toolbarColor = ((EditTextPreference) findPreference("toolbar_color"));
            setToolbarColor(toolbarColor);


            final SwitchPreference play_version = (SwitchPreference) findPreference("play_version");
            setCheckPlayVersion(play_version);
        }

        private void setCheckPlayVersion(final SwitchPreference play_version) {
            play_version.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    MainActivity activity = (MainActivity) getActivity();
                    activity.sendRequest(activity.getWechatVersionCode(), (Boolean) newValue);

                    return true;
                }
            });
        }

        private void setToolbarColor(final EditTextPreference preference) {

            final PreferenceTextWatcher watcher = new PreferenceTextWatcher(preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(final Preference preference) {

                    String toolbar_color = preference.getSharedPreferences().
                            getString("toolbar_color", Constants.DEFAULT_TOOLBAR_COLOR);

                    final EditText editText = ((EditTextPreference) preference).getEditText();
                    editText.setBackgroundTintList(ColorStateList.valueOf(getColorInt(toolbar_color)));
                    editText.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
                    editText.setTextColor(getColorInt(toolbar_color));
                    editText.setHint("当前值：" + toolbar_color);
                    editText.setSingleLine();
                    editText.setSelection(editText.getText().length());
                    editText.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                        @Override
                        public void onViewAttachedToWindow(View v) {
                            editText.addTextChangedListener(watcher);
                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {
                            editText.removeTextChangedListener(watcher);
                        }
                    });

                    return false;
                }
            });
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        getColorInt((String) newValue);
                        Toast.makeText(getActivity(), "颜色已更新", Toast.LENGTH_SHORT).show();
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            });
        }

        private int getColorInt(CharSequence colorString) {
            return Color.parseColor("#" + colorString);
        }

        private class PreferenceTextWatcher implements TextWatcher {

            EditTextPreference preference;

            PreferenceTextWatcher(EditTextPreference preference) {
                this.preference = preference;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                View button = preference.getDialog().findViewById(android.R.id.button1);
                if (s.length() == 6)
                    try {
                        int color = getColorInt(s);
                        EditText editText = preference.getEditText();
                        editText.setTextColor(color);
                        editText.setBackgroundTintList(ColorStateList.valueOf(color));
                        editText.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
                        button.setEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        button.setEnabled(false);
                    }
                else {
                    button.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        }

    }


}
