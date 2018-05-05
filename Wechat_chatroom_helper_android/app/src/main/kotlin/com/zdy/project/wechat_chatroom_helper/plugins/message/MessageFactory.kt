package com.zdy.project.wechat_chatroom_helper.plugins.message

import android.database.Cursor
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal
import com.zdy.project.wechat_chatroom_helper.ChatInfoModel
import de.robv.android.xposed.XposedHelpers

object MessageFactory {

    private const val SqlForGetAllOfficial = "select  unReadCount, status, isSend, conversationTime," +
            "rconversation.username, content, msgType ,digest, digestUser, attrflag, editingMsg, " +
            "atCount, unReadMuteCount, UnReadInvite from rconversation,rcontact " +
            "where ( rcontact.username = rconversation.username and rcontact.verifyFlag = 24) and ( parentRef is null  or parentRef = '' )  " +
            "and ( 1 !=1 or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  " +
            "and rconversation.username != 'qmessage' order by flag desc"

    private const val SqlForGetAllChatroom = "select unReadCount, status, isSend, conversationTime, " +
            "username, content, msgType, digest, digestUser, attrflag, editingMsg, atCount, " +
            "unReadMuteCount, UnReadInvite from rconversation where  username like '%@chatroom' order by flag desc"

    private fun SqlForByUsername(field_username: String) = "select unReadCount, status, isSend, " +
            "conversationTime, rconversation.username, rcontact.nickname, content, msgType, digest," +
            "digestUser, attrflag, editingMsg, atCount, unReadMuteCount, UnReadInvite " +
            "from rconversation, rcontact " +
            "where rconversation.username = rcontact.username and rconversation.username = '$field_username'"


    @JvmStatic
    fun getDataBaseFactory(any: Any) = XposedHelpers.findField(any::class.java, "mCursorFactory").apply { isAccessible = true }.get(any)


    fun getAllChatroom(): ArrayList<ChatInfoModel> {
        //     XposedBridge.log("getAllChatroom = ${WechatGlobal.MainDatabaseObject}")

        //  val dataBaseFactory = getDataBaseFactory(WechatGlobal.MainDatabaseObject!!)

        //   XposedBridge.log("getAllChatroom = $dataBaseFactory")

        val cursor = XposedHelpers.callMethod(WechatGlobal.MainDatabaseObject, "rawQuery", SqlForGetAllChatroom, null) as Cursor

        val list = arrayListOf<ChatInfoModel>()

        while (cursor.moveToNext()) {

            list.add(
                    ChatInfoModel().apply {
                        nickname = cursor.getString(cursor.getColumnIndex("username"))
                        content = cursor.getString(cursor.getColumnIndex("digest"))
                        time = cursor.getString(cursor.getColumnIndex("conversationTime"))
                        unReadMuteCount = cursor.getString(cursor.getColumnIndex("unReadMuteCount"))
                    })

        }
        return list
    }


    fun getSingle(field_username: String) {
        val cursor = XposedHelpers.callMethod(WechatGlobal.MainDatabaseObject, "rawQueryWithFactory",
                getDataBaseFactory(WechatGlobal.MainDatabaseObject!!), SqlForByUsername(field_username), null, null) as Cursor
    }


    fun getAllOfficial(): ArrayList<ChatInfoModel> {
        val cursor = XposedHelpers.callMethod(WechatGlobal.MainDatabaseObject, "rawQuery", SqlForGetAllOfficial, null) as Cursor

        val list = arrayListOf<ChatInfoModel>()

        while (cursor.moveToNext()) {

            list.add(
                    ChatInfoModel().apply {
                        nickname = cursor.getString(cursor.getColumnIndex("username"))
                        content = cursor.getString(cursor.getColumnIndex("digest"))
                        time = cursor.getString(cursor.getColumnIndex("conversationTime"))
                        unReadMuteCount = cursor.getString(cursor.getColumnIndex("unReadMuteCount"))
                    })

        }
        return list
    }

}