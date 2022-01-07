package com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message

import android.database.Cursor
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.ConversationReflectFunction
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapter
import de.robv.android.xposed.XposedHelpers
import java.nio.ByteBuffer

object MessageFactory {

    private const val SqlForGetAllOfficial = "select unReadCount, status, isSend, flag, conversationTime, rcontact.usernameFlag, " +
            "rconversation.username, rcontact.nickname, rcontact.lvbuff, content, msgType ,digest, digestUser, attrflag, editingMsg, " +
            "atCount, unReadMuteCount, UnReadInvite from rconversation, rcontact " +
            "where ( rcontact.username = rconversation.username and rcontact.verifyFlag != 0)" +
            "and ( parentRef is null  or parentRef = '' or parentRef = 'message_fold' )  " +
            "and ( 1 !=1 or rconversation.username like '%@chatroom' or rconversation.username like '%@openim' or rconversation.username not like '%@%' )  " +
            "and rconversation.username != 'qmessage' " +
            "and rconversation.username != 'message_fold' " +

            "order by flag desc"

    private const val SqlForGetAllChatRoom = "select unReadCount, status, isSend, flag, conversationTime, rcontact.usernameFlag, " +
            "rconversation.username, rcontact.nickname, rcontact.lvbuff, content, msgType, digest, digestUser, attrflag, editingMsg, " +
            "atCount, unReadMuteCount, UnReadInvite from rconversation, rcontact " +
            "where rcontact.username = rconversation.username " +
            "and rconversation.username like '%chatroom' " +
            "and ( parentRef is null  or parentRef = '' or parentRef = 'message_fold' )  " +
            "order by flag desc"

    private fun SqlForByUsername(field_username: String) = "select unReadCount, status, flag, isSend, conversationTime, rcontact.usernameFlag, " +
            "rconversation.username, rcontact.nickname, rcontact.lvbuff, content, msgType, digest, digestUser, attrflag, editingMsg, " +
            "atCount, unReadMuteCount, UnReadInvite from rconversation, rcontact " +
            "where rconversation.username = rcontact.username and rconversation.username = '$field_username'"


    @JvmStatic
    fun getDataBaseFactory(any: Any) = XposedHelpers.findField(any::class.java, "mCursorFactory").apply { isAccessible = true }.get(any)


    fun getAllChatRoom(): ArrayList<ChatInfoModel> {
        val cursor = XposedHelpers.callMethod(MessageHandler.MessageDatabaseObject, "rawQuery", SqlForGetAllChatRoom, null) as Cursor
        val list = arrayListOf<ChatInfoModel>()

        LogUtils.log("MessageFactory, chatRoomCount = ${cursor.count}")

        while (cursor.moveToNext()) {
            list.add(buildChatInfoModelByCursor(cursor))
        }
        return list
    }

    fun getWechatTeam(): Any {
        val cursor = XposedHelpers.callMethod(MessageHandler.MessageDatabaseObject, "rawQuery", SqlForByUsername("weixin"), null) as Cursor
        cursor.moveToNext()
        return buildChatInfoModelByCursor(cursor)
    }


    fun getAllOfficial(): ArrayList<ChatInfoModel> {
        val cursor = XposedHelpers.callMethod(MessageHandler.MessageDatabaseObject, "rawQuery", SqlForGetAllOfficial, null) as Cursor
        val list = arrayListOf<ChatInfoModel>()

        LogUtils.log("MessageFactory, officialCount = ${cursor.count}")

        while (cursor.moveToNext()) {
            list.add(buildChatInfoModelByCursor(cursor))
        }
        return list
    }

    fun getSpecChatRoom(): ArrayList<ChatInfoModel> {
        val list = getAllChatRoom()
        val chatroomList = AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_CHAT_ROOM)
        val data = ArrayList(list.filterNot { chatroomList.contains(it.field_username) })

        LogUtils.log("MessageFactory, getSpecChatRoom, list = ${list.joinToString { it.toString() }}, data = ${data.joinToString { it.toString() }}")
        return data
    }

    fun getSpecOfficial(): ArrayList<ChatInfoModel> {
        val list = getAllOfficial()
        val officialList = AppSaveInfo.getWhiteList(AppSaveInfo.WHITE_LIST_OFFICIAL)
        val data = ArrayList(list.filterNot { officialList.contains(it.field_username) })

        LogUtils.log("MessageFactory, getSpecOfficial, list = ${list.joinToString { it.toString() }}, data = ${data.joinToString { it.toString() }}")
        return data
    }

    fun clearSpecChatRoomUnRead() {
        clearListUnRead(getSpecChatRoom())
    }

    fun clearSpecOfficialUnRead() {
        clearListUnRead(getSpecOfficial())
    }

    private fun clearListUnRead(list: ArrayList<ChatInfoModel>) {
        list.forEach { chatInfoModel: ChatInfoModel ->
            if (chatInfoModel.field_unReadCount > 0) {
                XposedHelpers.callMethod(MessageHandler.MessageDatabaseObject, WXObject.Message.M.EXECSQL,
                        "update rconversation set unReadCount = 0 where username = '${chatInfoModel.field_username}'")
            }
        }
    }

    fun getUnReadCountItem(list: ArrayList<ChatInfoModel>) = list.count { it.unReadCount > 0 }

    fun getUnMuteUnReadCount(list: ArrayList<ChatInfoModel>) = list.filter { it.chatRoomMuteFlag }.sumBy { it.unReadCount }

    fun getUnReadCount(list: ArrayList<ChatInfoModel>) = list.sumBy { it.unReadCount }

    fun getUnMuteChatRoomList(list: ArrayList<ChatInfoModel>) = list.filter { it.chatRoomMuteFlag }

    fun getSingle(field_username: String) =
            buildChatInfoModelByCursor((XposedHelpers.callMethod(MessageHandler.MessageDatabaseObject,
                    WXObject.Message.M.QUERY, getDataBaseFactory(MessageHandler.MessageDatabaseObject!!),
                    SqlForByUsername(field_username), null, null) as Cursor).apply { moveToNext() })

    fun getStickFlagInfo(obj: Any?) =
            XposedHelpers.callStaticMethod(ConversationReflectFunction.conversationStickyHeaderHandler,
                    ConversationReflectFunction.stickyHeaderHandlerMethod.name, obj, 4, 0) as Long

    private fun buildChatInfoModelByCursor(cursor: Cursor): ChatInfoModel {

        return ChatInfoModel().apply {
            field_username = cursor.getString(cursor.getColumnIndex("username"))
            field_nickname = cursor.getString(cursor.getColumnIndex("nickname"))
            field_content = cursor.getString(cursor.getColumnIndex("content"))
            field_digest = cursor.getString(cursor.getColumnIndex("digest"))
            field_digestUser = cursor.getString(cursor.getColumnIndex("digestUser"))
            field_editingMsg = cursor.getString(cursor.getColumnIndex("editingMsg"))
            field_msgType = cursor.getString(cursor.getColumnIndex("msgType"))
            field_conversationTime = cursor.getLong(cursor.getColumnIndex("conversationTime"))
            field_isSend = cursor.getInt(cursor.getColumnIndex("isSend"))
            field_status = cursor.getInt(cursor.getColumnIndex("status"))
            field_flag = cursor.getLong(cursor.getColumnIndex("flag"))
            field_attrflag = cursor.getInt(cursor.getColumnIndex("attrflag"))
            field_atCount = cursor.getInt(cursor.getColumnIndex("atCount"))
            field_unReadMuteCount = cursor.getInt(cursor.getColumnIndex("unReadMuteCount"))
            field_UnReadInvite = cursor.getInt(cursor.getColumnIndex("UnReadInvite"))
            field_unReadCount = cursor.getInt(cursor.getColumnIndex("unReadCount"))
            field_lvbuff = cursor.getBlob(cursor.getColumnIndex("lvbuff"))
            field_usernameFlag = cursor.getInt(cursor.getColumnIndex("usernameFlag"))

            stickyFlag = kotlin.run {
                val obj = ConversationReflectFunction.beanConstructor.newInstance("")
                ConversationReflectFunction.setupItemClassField(obj, "field_flag", field_flag)
                return@run getStickFlagInfo(obj)
            }

            if (MainAdapter.isOriginAdapterIsInitialized()) {
                content = ConversationReflectFunction.getConversationContent(MainAdapter.originAdapter, this)
                nickname = if (field_nickname.isEmpty()) "群聊" else field_nickname
//                nickname = ConversationReflectFunction.getConversationNickname(MainAdapter.originAdapter, this)
            } else {
                content = field_content
                nickname = if (field_nickname.isEmpty()) "群聊" else field_nickname
            }

            chatRoomMuteFlag = ByteBuffer.wrap(field_lvbuff).apply { position(1) }.getInt(39) > 0

            conversationTime = if (MainAdapter.adapterIsInitialized()) {
                ConversationReflectFunction.getConversationTimeString(MainAdapter.originAdapter, field_conversationTime)
            } else {
                "error"
            }

            unReadCount = field_unReadCount
            unReadMuteCount = field_unReadMuteCount
        }
    }


}