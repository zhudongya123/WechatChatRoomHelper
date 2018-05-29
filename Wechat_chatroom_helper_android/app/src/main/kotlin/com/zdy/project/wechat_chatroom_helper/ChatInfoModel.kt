package com.zdy.project.wechat_chatroom_helper

/**
 * Created by Mr.Zdy on 2017/11/7.
 */
class ChatInfoModel {

    var username: CharSequence = ""
    var nickname: CharSequence = ""
    var content: CharSequence = ""
    var digest: CharSequence = ""
    var digestUser: CharSequence = ""
    var editingMsg: CharSequence = ""
    var msgType: CharSequence = ""

    var conversationTime: Long = 0L

    var isSend: Int = 0
    var status: Int = 0
    var attrflag: Int = 0
    var atCount: Int = 0
    var unReadMuteCount: Int = 0
    var UnReadInvite: Int = 0
    var unReadCount: Int = 0

    companion object {

//        fun convertFromObject(obj: Any, originAdapter: Any, context: Context): ChatInfoModel {
//
//            val model = ChatInfoModel()
//            try {
//                val j = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_Status_Bean, obj)
//
//                val content = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_True_Content,
//                        obj, ScreenUtils.dip2px(context, 13f), XposedHelpers.getBooleanField(j, Constants.Value_Message_True_Content_Params)) as CharSequence
//
//                val time = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_True_Time, obj) as CharSequence
//
//                model.nickname = XposedHelpers.getObjectField(j, Constants.Value_Message_Bean_NickName) as CharSequence
//                model.content = content
//                model.time = time
//                model.avatarString = XposedHelpers.getObjectField(obj, "field_username") as String
//                model.unReadCount = XposedHelpers.getObjectField(obj, "field_unReadCount") as Int
//
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//            return model
//        }
    }

    override fun toString(): String {
        return "ChatInfoModel(username=$username, nickname=$nickname, content=$content, digest=$digest, digestUser=$digestUser, editingMsg=$editingMsg, msgType=$msgType, conversationTime=$conversationTime, isSend=$isSend, status=$status, attrflag=$attrflag, atCount=$atCount, unReadMuteCount=$unReadMuteCount, UnReadInvite=$UnReadInvite, unReadCount=$unReadCount)"
    }

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