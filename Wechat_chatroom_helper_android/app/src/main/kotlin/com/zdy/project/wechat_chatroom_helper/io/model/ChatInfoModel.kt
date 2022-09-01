package com.zdy.project.wechat_chatroom_helper.io.model


/**
 * Created by Mr.Zdy on 2017/11/7.
 */
class ChatInfoModel {

//    var field_showTips: Long = 0L
//    var field_remitMarkRed: Long = 0L
//    var field_parentRef: CharSequence = ""
//    var field_msgCount: Long = 0L
//    var field_lastSeq: Long = 0L
//    var field_hbMarkRed: Long = 0L
//    var field_hasTodo: Long = 0L
//    var field_hasSpecialFollow: Long = 0L
//    var field_firstUnDeliverSeq: Long = 0L
//    var field_editingQuoteMsgId: Long = 0L
//    var field_chatmode: Long = 0L
//    var field_UnDeliverCount: Long = 0L

    var field_username: CharSequence = ""
    var field_nickname: CharSequence = ""
    var field_content: CharSequence = ""
    var field_digest: CharSequence = ""
    var field_digestUser: CharSequence = ""
    var field_editingMsg: CharSequence = ""
    var field_msgType: CharSequence = ""
    var field_lvbuff = byteArrayOf()

    var field_conversationTime: Long = 0L

    var field_flag: Long = 0L
    var field_usernameFlag: Int = 0
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
    var chatRoomMuteFlag = false

    /**
     * 判断是否置顶
     */
    var stickyFlag: Long = 0


    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ChatInfoModel) return false

        if (other.field_username != field_username) return false
        if (other.content != content) return false
        if (other.conversationTime != conversationTime) return false
        if (other.unReadMuteCount != unReadMuteCount) return false
        if (other.unReadCount != unReadCount) return false

        return true
    }

    override fun toString(): String {
        return "ChatInfoModel, username = $field_username, nickname = $nickname, content = $content, " +
                "conversationTime = $conversationTime, unReadMuteCount = $unReadMuteCount, " +
                "unReadCount = $unReadCount, field_usernameFlag = $field_usernameFlag, " +
                "field_flag = $field_flag, field_attrflag = $field_attrflag"
    }

}