package utils

import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.zdy.project.wechat_chatroom_helper.Constants


/**
 * Created by zhudo on 2017/11/4.
 */
 class AppSaveInfoUtils {

    companion object {

        var OPEN = "open"
        var IS_CIRCLE_AVATAR = "is_circle_avatar"
        var SHOW_INFO = "show_info"
        var AUTO_CLOSE = "auto_close"
        var TOOLBAR_COLOR = "toolbar_color"
        var HAS_SUIT_WECHAT_DATA = "has_suit_wechat_data"
        var IS_PLAY_VERSION = "is_play_version"
        var HELPER_VERSIONCODE = "helper_versionCode"
        var WECHAT_VERSION = "wechat_version"
        var JSON = "json"
        var CHAT_ROOM_TYPE = "chatRoom_type"

        var WHITE_LIST_CHAT_ROOM = "white_list_chat_room"
        var WHITE_LIST_OFFICIAL = "white_list_official"

        fun openInfo(): Boolean {
            return FileUtils.getJsonValue(OPEN, true)
        }

        fun setJson(value: String) {
            return FileUtils.putJsonValue(JSON, value)
        }

        fun getJson(): String {
            return FileUtils.getJsonValue(JSON, "")
        }

        fun isCircleAvatarInfo(): Boolean {
            return FileUtils.getJsonValue(IS_CIRCLE_AVATAR, false)
        }

        fun showInfo(): String {
            return FileUtils.getJsonValue(SHOW_INFO, "")
        }

        fun setShowInfo(value: String) {
            FileUtils.putJsonValue(SHOW_INFO, value)
        }

        fun autoCloseInfo(): Boolean {
            return FileUtils.getJsonValue(AUTO_CLOSE, false)
        }

        fun toolbarColorInfo(): String {
            return FileUtils.getJsonValue(TOOLBAR_COLOR, Constants.DEFAULT_TOOLBAR_COLOR)
        }

        fun hasSuitWechatDataInfo(): Boolean {
            return FileUtils.getJsonValue(HAS_SUIT_WECHAT_DATA, false)
        }

        fun setHasSuitWechatDataInfo(value: Boolean) {
            FileUtils.putJsonValue(HAS_SUIT_WECHAT_DATA, value)
        }

        fun isplayVersionInfo(): Boolean {
            return FileUtils.getJsonValue(IS_PLAY_VERSION, false)
        }

        fun helpVersionCodeInfo(): String {
            return FileUtils.getJsonValue(HELPER_VERSIONCODE, "0")
        }

        fun setHelpVersionCodeInfo(value: String) {
            FileUtils.putJsonValue(HELPER_VERSIONCODE, value)
        }

        fun wechatVersionInfo(): String {
            return FileUtils.getJsonValue(WECHAT_VERSION, "0")
        }

        fun setWechatVersionInfo(value: String) {
            FileUtils.putJsonValue(WECHAT_VERSION, value)
        }

        fun chatRoomTypeInfo(): String {
            return FileUtils.getJsonValue(CHAT_ROOM_TYPE, "2")
        }

        fun setChatRoomType(value: String) {
            FileUtils.putJsonValue(CHAT_ROOM_TYPE, value)
        }


        fun getWhiteList(key: String): ArrayList<String> {
            val value = FileUtils.getJsonValue(key, "[]")
            val jsonArray = JsonParser().parse(value).asJsonArray
            val arrayList = ArrayList<String>()
            jsonArray.mapTo(arrayList) { it.asString }
            return arrayList
        }

        fun removeWhitList(key: String, item: String) {
            val value = FileUtils.getJsonValue(key, "[]")
            val jsonArray = JsonParser().parse(value).asJsonArray

            jsonArray.remove(JsonPrimitive(item))
            FileUtils.putJsonValue(key, jsonArray.toString())
        }

        fun setWhiteList(key: String, item: String) {
            val value = FileUtils.getJsonValue(key, "[]")
            val jsonArray = JsonParser().parse(value).asJsonArray

            for (i in 0 until jsonArray.size()) {
                val string = jsonArray[i].asString
                if (item == string)
                    return
            }

            jsonArray.add(item)
            FileUtils.putJsonValue(key, jsonArray.toString())
        }

        fun clearWhiteList(key: String) {
            FileUtils.putJsonValue(key, "[]")
        }


        fun initVariableName(): Boolean {

            val json = FileUtils.getJsonValue("json", "")

            try {

                val jsonObject = JsonParser().parse(json).asJsonObject

                Constants.Class_Conversation_List_View_Adapter_Name = jsonObject.get("cclvan").asString
                Constants.Class_Conversation_List_View_Adapter_Parent_Name = jsonObject.get("cclvapn").asString
                Constants.Class_Conversation_List_Adapter_OnItemClickListener_Name = jsonObject.get("cclaon").asString

                Constants.Class_Conversation_List_View_Adapter_SimpleName = jsonObject.get("cclvas").asString
                Constants.Class_Conversation_List_View_Adapter_Parent_SimpleName = jsonObject.get("cclvaps").asString

                Constants.Method_Message_Status_Bean = jsonObject.get("mmsb").asString
                Constants.Method_Adapter_Get_Object = jsonObject.get("mago").asString

                Constants.Value_Message_Status_Is_Mute_1 = jsonObject.get("vmsim1").asString
                Constants.Value_Message_Status_Is_Mute_2 = jsonObject.get("vmsim2").asString
                Constants.Value_ListView_Adapter = jsonObject.get("vla").asString
                Constants.Value_ListView = jsonObject.get("vl").asString

                Constants.Value_ListView_Adapter_ViewHolder_Title = jsonObject.get("vlavt").asString
                Constants.Value_ListView_Adapter_ViewHolder_Avatar = jsonObject.get("vlava").asString
                Constants.Value_ListView_Adapter_ViewHolder_Content = jsonObject.get("vlavc").asString

                Constants.Method_Adapter_Get_Object_Step_1 = jsonObject.get("magos1").asString
                Constants.Method_Adapter_Get_Object_Step_2 = jsonObject.get("magos2").asString
                Constants.Method_Adapter_Get_Object_Step_3 = jsonObject.get("magos3").asString

                Constants.Class_Tencent_Log = jsonObject.get("ctl").asString
                Constants.Class_Set_Avatar = jsonObject.get("csa").asString

                Constants.Drawable_String_Arrow = jsonObject.get("dsa").asString
                Constants.Drawable_String_Setting = jsonObject.get("dss").asString
                Constants.Drawable_String_Chatroom_Avatar = jsonObject.get("dsca").asString

                Constants.Value_Message_Bean_Content = jsonObject.get("vmbc").asString
                Constants.Value_Message_Bean_NickName = jsonObject.get("vmbn").asString
                Constants.Value_Message_Bean_Time = jsonObject.get("vmbt").asString

                Constants.Method_Message_True_Content = jsonObject.get("mmtc").asString
                Constants.Value_Message_True_Content_Params = jsonObject.get("vmtcp").asString

                Constants.Method_Message_True_Time = jsonObject.get("mmtt").asString

                Constants.Class_Tencent_Home_UI = jsonObject.get("cthu").asString
                Constants.Method_Home_UI_Inflater_View = jsonObject.get("mhuiv").asString
                Constants.Value_Home_UI_Activity = jsonObject.get("vhua").asString
                Constants.Method_Conversation_List_View_Adapter_Param = jsonObject.get("mclvap").asString

                Constants.Method_Conversation_List_Get_Avatar = jsonObject.get("mclga").asString

                Constants.Value_Message_Status_Is_OFFICIAL_1 = jsonObject.get("vmsio1").asString
                Constants.Value_Message_Status_Is_OFFICIAL_2 = jsonObject.get("vmsio2").asString
                Constants.Value_Message_Status_Is_OFFICIAL_3 = jsonObject.get("vmsio3").asString

                return true
            } catch (e: Exception) {
                return false
            }

        }
    }
}