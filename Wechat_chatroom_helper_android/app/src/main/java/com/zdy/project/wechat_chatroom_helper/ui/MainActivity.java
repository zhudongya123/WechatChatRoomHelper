package com.zdy.project.wechat_chatroom_helper.ui;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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


    private FrameLayout fragmentContent;

    SettingFragment settingFragment;

    int versionCode;
    String versionName;

    SharedPreferences sharedPreferences;


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
        sharedPreferences
                = getSharedPreferences(this.getPackageName() + "_preferences", MODE_WORLD_READABLE);

        getVersion();

        int saveVersionCode = sharedPreferences.getInt("saveVersionCode", 0);

        sendRequest(saveVersionCode);

        settingFragment = new SettingFragment();
        getFragmentManager().beginTransaction().replace(fragmentContent.getId(), settingFragment).commit();


        textView.setText(Html.fromHtml(
                "<p><a href=\"https://github.com/zhudongya123/WechatChatroomHelper/issues\">反馈地址</a></p>\n" + "鸣谢:<br>\n" +
                        "<p><a href=\"https://www.coolapk.com/apk/com.toshiba_dealin.developerhelper\">开发者助手开发者（东芝）</a></p>\n" +
                        "<p><a href=\"https://github.com/veryyoung\">微信红包开发者（veryyoung）</a></p>"));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void getVersion() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {

            if (packageInfo.packageName.equals("com.tencent.mm")) {
                versionCode = packageInfo.versionCode;
                versionName = packageInfo.versionName;
            }
        }
    }

    private void sendRequest(int saveVersionCode) {
        if (versionCode != saveVersionCode) {


            RequestBody requestBody = new FormBody.Builder()
                    .add("versionCode", String.valueOf(versionCode)).build();

            final Request request = new Request.Builder()
                    .url(CLASS_MAPPING)
                    .post(requestBody)
                    .build();

            ApiManager.getINSTANCE().getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    setFailText(versionName + "(" + versionCode + ")");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        setFailText(versionName + "(" + versionCode + ")");
                    }
                }


            });
        } else setSuccessText(versionName + "(" + versionCode + ")");
    }


    private void setFailText(final String versionInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detail.setText("当前微信版本" + versionInfo + "暂未适配，或者出现其他问题，请等待开发者解决。");
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

            final EditTextPreference toolbarColor = ((EditTextPreference) findPreference("toolbar_color"));
            setToolbarColor(toolbarColor);
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
