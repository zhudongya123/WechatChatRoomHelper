package com.zdy.project.wechat_chatroom_helper

import android.content.Context

/**
 * Created by Mr.Zdy on 2017/11/7.
 */
class ChatInfoModel {

    var field_username: CharSequence = ""
    var field_nickname: CharSequence = ""
    var field_content: CharSequence = ""
    var field_digest: CharSequence = ""
    var field_digestUser: CharSequence = ""
    var field_editingMsg: CharSequence = ""
    var field_msgType: CharSequence = ""

    var field_conversationTime: Long = 0L

    var field_isSend: Int = 0
    var field_status: Int = 0
    var field_attrflag: Int = 0
    var field_atCount: Int = 0
    var field_unReadMuteCount: Int = 0
    var field_UnReadInvite: Int = 0
    var field_unReadCount: Int = 0

    var nickname: CharSequence = ""
    var content: CharSequence = ""
    var conversationTime: CharSequence = ""
    var unReadMuteCount: Int = 0
    var unReadCount: Int = 0



    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ChatInfoModel) return false

        if (other.nickname != nickname) return false
        if (other.content != content) return false
        if (other.conversationTime != conversationTime) return false
        if (other.unReadMuteCount != unReadMuteCount) return false
        if (other.unReadCount != unReadCount) return false

        return true
    }



}