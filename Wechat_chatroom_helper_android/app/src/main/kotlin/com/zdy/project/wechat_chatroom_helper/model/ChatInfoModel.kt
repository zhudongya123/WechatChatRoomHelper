package com.zdy.project.wechat_chatroom_helper.model

import android.content.Context
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2017/11/7.
 */
class ChatInfoModel {

    var nickname: CharSequence? = null
    var content: CharSequence? = null
    var time: CharSequence? = null
    var avatarString: String? = null
    var unReadCount: Int? = null

    companion object {

        fun convertFromObject(obj: Any, originAdapter: Any, context: Context): ChatInfoModel {
            val entity = MessageEntity(obj)


            val model = ChatInfoModel()
            try {
                val j = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_Status_Bean, obj)

                val content = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_True_Content,
                        obj, ScreenUtils.dip2px(context, 13f), XposedHelpers.getBooleanField(j, Constants.Value_Message_True_Content_Params)) as CharSequence

                val time = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_True_Time, obj) as CharSequence

                model.nickname = XposedHelpers.getObjectField(j, Constants.Value_Message_Bean_NickName) as CharSequence
                model.content = content
                model.time = time
                model.avatarString = entity.field_username
                model.unReadCount = entity.field_unReadCount


            } catch (e: Exception) {
                e.printStackTrace()
            }
            return model
        }
    }
}