package com.zdy.project.wechat_chatroom_helper.utils;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zdy.project.wechat_chatroom_helper.Constants;

import java.io.File;

import de.robv.android.xposed.XSharedPreferences;

public class PreferencesUtils {

    private static XSharedPreferences instance = null;

    private static XSharedPreferences getInstance() {
        if (instance == null) {
            instance = new XSharedPreferences(new File("data/data/com.zdy.project.wechat_chatroom_helper/" +
                    "shared_prefs/com.zdy.project.wechat_chatroom_helper_preferences.xml"));
            instance.makeWorldReadable();
        } else {
            instance.reload();
        }
        return instance;
    }


    public static int helper_versionCode() {
        return getInstance().getInt("helper_versionCode", 0);
    }

    public static boolean open() {
        return getInstance().getBoolean("open", false);
    }

    public static boolean auto_close() {
        return getInstance().getBoolean("auto_close", false);
    }

    public static int getVersionCode() {
        return getInstance().getInt("saveVersionCode", 0);
    }

    public static String getToolBarColor() {
        return getInstance().getString("toolbar_color", Constants.DEFAULT_TOOLBAR_COLOR);
    }

    public static boolean getCircleAvatar() {
        return getInstance().getBoolean("is_circle_avatar", false);
    }

    public static boolean initVariableName() {

        String json = getInstance().getString("json", "");

        try {

            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

            Constants.Class_Conversation_List_View_Adapter_Name = jsonObject.get("cclvan").getAsString();
            Constants.Class_Conversation_List_View_Adapter_Parent_Name = jsonObject.get("cclvapn").getAsString();
            Constants.Class_Conversation_List_Adapter_OnItemClickListener_Name = jsonObject.get("cclaon").getAsString();

            Constants.Class_Conversation_List_View_Adapter_SimpleName = jsonObject.get("cclvas").getAsString();
            Constants.Class_Conversation_List_View_Adapter_Parent_SimpleName = jsonObject.get("cclvaps").getAsString();

            Constants.Method_Message_Status_Bean = jsonObject.get("mmsb").getAsString();
            Constants.Method_Adapter_Get_Object = jsonObject.get("mago").getAsString();

            Constants.Value_Message_Status_Is_Mute_1 = jsonObject.get("vmsim1").getAsString();
            Constants.Value_Message_Status_Is_Mute_2 = jsonObject.get("vmsim2").getAsString();
            Constants.Value_ListView_Adapter = jsonObject.get("vla").getAsString();
            Constants.Value_ListView = jsonObject.get("vl").getAsString();

            Constants.Value_ListView_Adapter_ViewHolder_Title = jsonObject.get("vlavt").getAsString();
            Constants.Value_ListView_Adapter_ViewHolder_Avatar = jsonObject.get("vlava").getAsString();
            Constants.Value_ListView_Adapter_ViewHolder_Content = jsonObject.get("vlavc").getAsString();

            Constants.Method_Adapter_Get_Object_Step_1 = jsonObject.get("magos1").getAsString();
            Constants.Method_Adapter_Get_Object_Step_2 = jsonObject.get("magos2").getAsString();
            Constants.Method_Adapter_Get_Object_Step_3 = jsonObject.get("magos3").getAsString();

            Constants.Class_Tencent_Log = jsonObject.get("ctl").getAsString();
            Constants.Class_Set_Avatar = jsonObject.get("csa").getAsString();

            Constants.Drawable_String_Arrow = jsonObject.get("dsa").getAsString();
            Constants.Drawable_String_Setting = jsonObject.get("dss").getAsString();
            Constants.Drawable_String_Chatroom_Avatar = jsonObject.get("dsca").getAsString();

            Constants.Value_Message_Bean_Content = jsonObject.get("vmbc").getAsString();
            Constants.Value_Message_Bean_NickName = jsonObject.get("vmbn").getAsString();
            Constants.Value_Message_Bean_Time = jsonObject.get("vmbt").getAsString();

            Constants.Method_Message_True_Content = jsonObject.get("mmtc").getAsString();
            Constants.Value_Message_True_Content_Params = jsonObject.get("vmtcp").getAsString();

            Constants.Method_Message_True_Time = jsonObject.get("mmtt").getAsString();

            Constants.Class_Tencent_Home_UI = jsonObject.get("cthu").getAsString();
            Constants.Method_Home_UI_Inflater_View = jsonObject.get("mhuiv").getAsString();
            Constants.Value_Home_UI_Activity = jsonObject.get("vhua").getAsString();
            Constants.Method_Conversation_List_View_Adapter_Param = jsonObject.get("mclvap").getAsString();

            Constants.Method_Conversation_List_Get_Avatar = jsonObject.get("mclga").getAsString();

            Constants.Value_Message_Status_Is_OFFICIAL_1 = jsonObject.get("vmsio1").getAsString();
            Constants.Value_Message_Status_Is_OFFICIAL_2 = jsonObject.get("vmsio2").getAsString();
            Constants.Value_Message_Status_Is_OFFICIAL_3 = jsonObject.get("vmsio3").getAsString();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


