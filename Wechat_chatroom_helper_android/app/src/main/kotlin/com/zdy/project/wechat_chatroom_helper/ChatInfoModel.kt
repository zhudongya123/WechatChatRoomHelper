package com.zdy.project.wechat_chatroom_helper

import android.content.Context
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import de.robv.android.xposed.XposedHelpers

/**
 * Created by Mr.Zdy on 2017/11/7.
 */
class ChatInfoModel {

    lateinit var nickname: CharSequence
    lateinit var content: CharSequence
    lateinit var time: CharSequence
    lateinit var avatarString: String
    var unReadCount = 0

    companion object {

        fun convertFromObject(obj: Any, originAdapter: Any, context: Context): ChatInfoModel {

            val model = ChatInfoModel()
            try {
                val j = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_Status_Bean, obj)

                val content = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_True_Content,
                        obj, ScreenUtils.dip2px(context, 13f), XposedHelpers.getBooleanField(j, Constants.Value_Message_True_Content_Params)) as CharSequence

                val time = XposedHelpers.callMethod(originAdapter, Constants.Method_Message_True_Time, obj) as CharSequence

                model.nickname = XposedHelpers.getObjectField(j, Constants.Value_Message_Bean_NickName) as CharSequence
                model.content = content
                model.time = time
                model.avatarString = XposedHelpers.getObjectField(obj, "field_username") as String
                model.unReadCount = XposedHelpers.getObjectField(obj, "field_unReadCount") as Int


            } catch (e: Exception) {
                e.printStackTrace()
            }
            return model
        }
    }
}