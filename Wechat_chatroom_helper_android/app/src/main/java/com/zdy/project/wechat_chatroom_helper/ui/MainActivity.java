package com.zdy.project.wechat_chatroom_helper.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zdy.project.wechat_chatroom_helper.R;

public class MainActivity extends AppCompatActivity {


    private FrameLayout fragmentContent;
    private TextView textView;

    SettingFragment settingFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.textView);
        fragmentContent = (FrameLayout) findViewById(R.id.fragment_content);


        settingFragment = new SettingFragment();
        getFragmentManager().beginTransaction().replace(fragmentContent.getId(), settingFragment).commit();
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
