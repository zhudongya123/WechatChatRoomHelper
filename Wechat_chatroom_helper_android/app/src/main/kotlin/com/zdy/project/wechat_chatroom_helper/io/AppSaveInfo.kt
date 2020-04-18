package com.zdy.project.wechat_chatroom_helper.io

import android.content.Context
import com.google.gson.JsonPrimitive
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting.SettingViewModel
import com.zdy.project.wechat_chatroom_helper.helper.utils.ActivityUtils


/**
 *
 * 此类声明了所有需要存储的字段和相应的get set 方法
 * Created by zhudo on 2017/11/4.
 */
object AppSaveInfo {

    private const val OPEN = "open"//总开关
    private const val IS_CIRCLE_AVATAR = "is_circle_avatar"//是否圆头像，默认否
    private const val IS_AUTO_CLOSE = "is_auto_close"//返回自动关闭群助手，默认否
    private const val IS_OPEN_LOG = "is_open_log"//日志开关
    private const val IS_LAUNCHER_ENTRY = "is_hide_launcher_entry"//launcher 入口开关，默认为否
    private const val IS_BIZ_USE_OLD_STYLE = "is_biz_use_old_style"//开启旧版订阅号样式


    private const val SHOW_INFO = "show_info"//适配信息

    private const val TOOLBAR_COLOR = "toolbar_color"
    private const val HELPER_COLOR = "helper_color"
    private const val NICKNAME_COLOR = "nickname_color"
    private const val CONTENT_COLOR = "content_color"
    private const val TIME_COLOR = "time_color"
    private const val DIVIDER_COLOR = "divider_color"
    private const val HIGHLIGHT_COLOR = "highlight_color"

    private const val HAS_SUIT_WECHAT_DATA = "has_suit_wechat_data"
    private const val IS_PLAY_VERSION = "is_play_version"
    private const val HELPER_VERSIONCODE = "helper_versionCode"
    private const val WECHAT_VERSION = "wechat_version"
    private const val WECHAT_VERSION_NAME = "wechat_version_name"
    private const val IS_DONATION = "is_donation"
    private const val COLOR_MODE = "color_mode"

    private const val JSON = "json"
    private const val CHAT_ROOM_TYPE = "chatRoom_type"

    const val WHITE_LIST_CHAT_ROOM = "white_list_chat_room"
    const val WHITE_LIST_OFFICIAL = "white_list_official"

    private const val API_RECORD_TIME = "api_record_time"//上次请求的时间
    private const val HELPER_STICKY_INFO = "helper_sticky_info"//助手置顶位置

    fun setColorMode(value: Int) {
        WechatJsonUtils.putJsonValue(COLOR_MODE, value)
    }

    fun getColorMode() = WechatJsonUtils.getJsonValue(COLOR_MODE, 0)

    fun setDonation(value: Boolean) {
        WechatJsonUtils.putJsonValue(IS_DONATION, value)
    }

    fun isDonation() = WechatJsonUtils.getJsonValue(IS_DONATION, false)

    fun isBizUseOldStyle(): Boolean {
        return WechatJsonUtils.getJsonValue(IS_BIZ_USE_OLD_STYLE, true)
    }

    fun setBizUseOldStyle(value: Boolean) {
        WechatJsonUtils.putJsonValue(IS_BIZ_USE_OLD_STYLE, value)
    }

    fun getHelperStickyInfo(): Int {
        return WechatJsonUtils.getJsonValue(HELPER_STICKY_INFO, 0)
    }

    fun setHelperStickyInfo(value: Int) {
        WechatJsonUtils.putJsonValue(HELPER_STICKY_INFO, value)
        WechatJsonUtils.putFileString()
    }

    fun apiRecordTimeInfo(): Int {
        return WechatJsonUtils.getJsonValue(API_RECORD_TIME, (System.currentTimeMillis() / 1000).toInt())
    }

    fun setApiRecordTime(time: Int) {
        WechatJsonUtils.putJsonValue(API_RECORD_TIME, time)
    }

    fun launcherEntryInfo(): Boolean {
        return WechatJsonUtils.getJsonValue(IS_LAUNCHER_ENTRY, false)
    }

    fun setLauncherEntry(checked: Boolean) {
        WechatJsonUtils.putJsonValue(IS_LAUNCHER_ENTRY, checked)
    }

    fun openLogInfo(): Boolean {
        return WechatJsonUtils.getJsonValue(IS_OPEN_LOG, false)
    }

    fun setOpenLog(value: Boolean) {
        WechatJsonUtils.putJsonValue(IS_OPEN_LOG, value)
    }

    fun openInfo(): Boolean {
        return WechatJsonUtils.getJsonValue(OPEN, true)
    }

    fun setOpen(value: Boolean) {
        WechatJsonUtils.putJsonValue(OPEN, value)
    }

    fun getJson(): String {
        return WechatJsonUtils.getJsonValue(JSON, "")
    }

    fun setJson(value: String) {
        WechatJsonUtils.putJsonValue(JSON, value)
    }

    fun isCircleAvatarInfo(): Boolean {
        return WechatJsonUtils.getJsonValue(IS_CIRCLE_AVATAR, false)
    }

    fun setCircleAvatarInfo(value: Boolean) {
        WechatJsonUtils.putJsonValue(IS_CIRCLE_AVATAR, value)
    }

    fun showInfo(): String {
        return WechatJsonUtils.getJsonValue(SHOW_INFO, "")
    }

    fun setShowInfo(value: String) {
        WechatJsonUtils.putJsonValue(SHOW_INFO, value)
    }

    fun autoCloseInfo(): Boolean {
        return WechatJsonUtils.getJsonValue(IS_AUTO_CLOSE, false)
    }

    fun setAutoCloseInfo(value: Boolean) {
        WechatJsonUtils.putJsonValue(IS_AUTO_CLOSE, value)
    }

    fun toolbarColorInfo(context: Context?): String {
        return if (getColorMode() == SettingViewModel.ColorMode.Manual)
            WechatJsonUtils.getJsonValue(TOOLBAR_COLOR, Constants.defaultValue.DEFAULT_LIGHT_TOOLBAR_COLOR)
        else {
            if (context == null) {
                WechatJsonUtils.getJsonValue(TOOLBAR_COLOR, Constants.defaultValue.DEFAULT_LIGHT_TOOLBAR_COLOR)
            } else {
                if (ActivityUtils.isNightMode(context)) {
                    Constants.defaultValue.DEFAULT_DARK_TOOLBAR_COLOR
                } else {
                    Constants.defaultValue.DEFAULT_LIGHT_TOOLBAR_COLOR
                }
            }
        }
    }

    fun setToolbarColorInfo(value: String) {
        WechatJsonUtils.putJsonValue(TOOLBAR_COLOR, value)
    }

    fun helperColorInfo(context: Context?): String {
        return if (getColorMode() == SettingViewModel.ColorMode.Manual)
            WechatJsonUtils.getJsonValue(HELPER_COLOR, Constants.defaultValue.DEFAULT_LIGHT_HELPER_COLOR)
        else {
            if (context == null) {
                WechatJsonUtils.getJsonValue(HELPER_COLOR, Constants.defaultValue.DEFAULT_LIGHT_HELPER_COLOR)
            } else {
                if (ActivityUtils.isNightMode(context)) {
                    Constants.defaultValue.DEFAULT_DARK_HELPER_COLOR
                } else {
                    Constants.defaultValue.DEFAULT_LIGHT_HELPER_COLOR
                }
            }
        }
    }

    fun setHelperColorInfo(value: String) {
        WechatJsonUtils.putJsonValue(HELPER_COLOR, value)
    }

    fun nicknameColorInfo(context: Context?): String {
        return if (getColorMode() == SettingViewModel.ColorMode.Manual)
            WechatJsonUtils.getJsonValue(NICKNAME_COLOR, Constants.defaultValue.DEFAULT_LIGHT_NICKNAME_COLOR)
        else {
            if (context == null) {
                WechatJsonUtils.getJsonValue(NICKNAME_COLOR, Constants.defaultValue.DEFAULT_LIGHT_NICKNAME_COLOR)
            } else {
                if (ActivityUtils.isNightMode(context)) {
                    Constants.defaultValue.DEFAULT_DARK_NICKNAME_COLOR
                } else {
                    Constants.defaultValue.DEFAULT_LIGHT_NICKNAME_COLOR
                }
            }
        }
    }

    fun setNicknameColorInfo(value: String) {
        WechatJsonUtils.putJsonValue(NICKNAME_COLOR, value)
    }

    fun contentColorInfo(context: Context?): String {
        return if (getColorMode() == SettingViewModel.ColorMode.Manual)
            WechatJsonUtils.getJsonValue(CONTENT_COLOR, Constants.defaultValue.DEFAULT_LIGHT_CONTENT_COLOR)
        else {
            if (context == null) {
                WechatJsonUtils.getJsonValue(CONTENT_COLOR, Constants.defaultValue.DEFAULT_LIGHT_CONTENT_COLOR)
            } else {
                if (ActivityUtils.isNightMode(context)) {
                    Constants.defaultValue.DEFAULT_DARK_CONTENT_COLOR
                } else {
                    Constants.defaultValue.DEFAULT_LIGHT_CONTENT_COLOR
                }
            }
        }
    }

    fun setContentColorInfo(value: String) {
        WechatJsonUtils.putJsonValue(CONTENT_COLOR, value)
    }

    fun timeColorInfo(context: Context?): String {
        return if (getColorMode() == SettingViewModel.ColorMode.Manual)
            WechatJsonUtils.getJsonValue(TIME_COLOR, Constants.defaultValue.DEFAULT_LIGHT_TIME_COLOR)
        else {
            if (context == null) {
                WechatJsonUtils.getJsonValue(TIME_COLOR, Constants.defaultValue.DEFAULT_LIGHT_TIME_COLOR)
            } else {
                if (ActivityUtils.isNightMode(context)) {
                    Constants.defaultValue.DEFAULT_DARK_TIME_COLOR
                } else {
                    Constants.defaultValue.DEFAULT_LIGHT_TIME_COLOR
                }
            }
        }
    }

    fun setTimeColorInfo(value: String) {
        WechatJsonUtils.putJsonValue(TIME_COLOR, value)
    }

    fun dividerColorInfo(context: Context?): String {
        return if (getColorMode() == SettingViewModel.ColorMode.Manual)
            WechatJsonUtils.getJsonValue(DIVIDER_COLOR, Constants.defaultValue.DEFAULT_LIGHT_DIVIDER_COLOR)
        else {
            if (context == null) {
                WechatJsonUtils.getJsonValue(DIVIDER_COLOR, Constants.defaultValue.DEFAULT_LIGHT_DIVIDER_COLOR)
            } else {
                if (ActivityUtils.isNightMode(context)) {
                    Constants.defaultValue.DEFAULT_DARK_DIVIDER_COLOR
                } else {
                    Constants.defaultValue.DEFAULT_LIGHT_DIVIDER_COLOR
                }
            }
        }
    }

    fun setDividerColorInfo(value: String) {
        WechatJsonUtils.putJsonValue(DIVIDER_COLOR, value)
    }

    fun highLightColorInfo(context: Context?): String {
        return if (getColorMode() == SettingViewModel.ColorMode.Manual)
            WechatJsonUtils.getJsonValue(HIGHLIGHT_COLOR, Constants.defaultValue.DEFAULT_LIGHT_HIGHLIGHT_COLOR)
        else {
            if (context == null) {
                WechatJsonUtils.getJsonValue(HIGHLIGHT_COLOR, Constants.defaultValue.DEFAULT_LIGHT_HIGHLIGHT_COLOR)
            } else {
                if (ActivityUtils.isNightMode(context)) {
                    Constants.defaultValue.DEFAULT_DARK_HIGHLIGHT_COLOR
                } else {
                    Constants.defaultValue.DEFAULT_LIGHT_HIGHLIGHT_COLOR
                }
            }
        }
    }

    fun setHighLightColorInfo(value: String) {
        WechatJsonUtils.putJsonValue(HIGHLIGHT_COLOR, value)
    }

    fun hasSuitWechatDataInfo(): Boolean {
        return WechatJsonUtils.getJsonValue(HAS_SUIT_WECHAT_DATA, false)
    }

    fun setSuitWechatDataInfo(value: Boolean) {
        WechatJsonUtils.putJsonValue(HAS_SUIT_WECHAT_DATA, value)
    }

    fun isPlayVersionInfo(): Boolean {
        return WechatJsonUtils.getJsonValue(IS_PLAY_VERSION, false)
    }

    fun setPlayVersionInfo(value: Boolean) {
        WechatJsonUtils.putJsonValue(IS_PLAY_VERSION, value)
    }

    fun helpVersionCodeInfo(): String {
        return WechatJsonUtils.getJsonValue(HELPER_VERSIONCODE, "0")
    }

    fun setHelpVersionCodeInfo(value: String) {
        WechatJsonUtils.putJsonValue(HELPER_VERSIONCODE, value)
    }

    fun wechatVersionInfo(): String {
        return WechatJsonUtils.getJsonValue(WECHAT_VERSION, "0")
    }


    fun getWechatVersionName() = WechatJsonUtils.getJsonValue(WECHAT_VERSION_NAME, "")

    fun setWechatVersionName(string: String) {
        WechatJsonUtils.putJsonValue(WECHAT_VERSION_NAME, string)
        WechatJsonUtils.putFileString()
    }

    fun setWechatVersionInfo(value: String) {
        WechatJsonUtils.putJsonValue(WECHAT_VERSION, value)
    }

    fun chatRoomTypeInfo(): String {
        return WechatJsonUtils.getJsonValue(CHAT_ROOM_TYPE, "2")
    }

    fun setChatRoomType(value: String) {
        WechatJsonUtils.putJsonValue(CHAT_ROOM_TYPE, value)
    }


    fun getWhiteList(key: String): ArrayList<String> {
        val value = WechatJsonUtils.getJsonValue(key, "[]")
        val jsonArray = WechatJsonUtils.parser.parse(value).asJsonArray
        val arrayList = ArrayList<String>()
        jsonArray.mapTo(arrayList) { it.asString }
        return arrayList
    }

    fun removeWhitList(key: String, item: String) {
        val value = WechatJsonUtils.getJsonValue(key, "[]")
        val jsonArray = WechatJsonUtils.parser.parse(value).asJsonArray

        jsonArray.remove(JsonPrimitive(item))
        WechatJsonUtils.putJsonValue(key, jsonArray.toString())
    }

    fun setWhiteList(key: String, item: String) {
        val value = WechatJsonUtils.getJsonValue(key, "[]")
        val jsonArray = WechatJsonUtils.parser.parse(value).asJsonArray

        for (i in 0 until jsonArray.size()) {
            val string = jsonArray[i].asString
            if (item == string)
                return
        }

        jsonArray.add(item)
        WechatJsonUtils.putJsonValue(key, jsonArray.toString())
    }

    fun clearWhiteList(key: String) {
        WechatJsonUtils.putJsonValue(key, "[]")
    }

    fun addConfigItem(key: String, value: String) {
        val jsonObject = WechatJsonUtils.parser
                .parse(WechatJsonUtils.getJsonValue("current_config", "{}"))
                .asJsonObject
        jsonObject.addProperty(key, value)
        WechatJsonUtils.putJsonValue("current_config", jsonObject.toString())
    }

    fun clearAddConfig() {
        WechatJsonUtils.putJsonValue("current_config", "{}")
    }

    fun getConfigJson() = WechatJsonUtils
            .parser
            .parse(WechatJsonUtils.getJsonValue("current_config", "{}"))
            .asJsonObject

}