package com.zdy.project.wechat_chatroom_helper.model;

import de.robv.android.xposed.XposedHelpers;

/**
 * Created by Mr.Zdy on 2017/7/8.
 */

public class MessageEntity {
    public int field_UnDeliverCount;
    public int field_UnReadInvite;
    public int field_atCount;
    public int field_attrflag;
    public int field_chatmode;
    public String field_content;
    public long field_conversationTime;
 //   private String field_customNotify;
    public String field_digest;
    public String field_digestUser;
    public String field_editingMsg;
    public long field_firstUnDeliverSeq;
    public long field_flag;
 //   private int field_hasTrunc;
    public int field_isSend;
    public long field_lastSeq;
    public int field_msgCount;
    public String field_msgType;
//    public String field_parentRef;
    public int field_showTips;
    public long field_sightTime;
    public int field_status;
    public int field_unReadCount;
    public int field_unReadMuteCount;
    public String field_username;


    public  MessageEntity(Object value) {
        field_UnDeliverCount = XposedHelpers.getIntField(value, "field_UnDeliverCount");
        field_UnReadInvite = XposedHelpers.getIntField(value, "field_UnReadInvite");
        field_atCount = XposedHelpers.getIntField(value, "field_atCount");
        field_attrflag = XposedHelpers.getIntField(value, "field_attrflag");

        field_chatmode = XposedHelpers.getIntField(value, "field_chatmode");
        field_content = XposedHelpers.getObjectField(value, "field_content").toString();
        field_conversationTime = XposedHelpers.getLongField(value, "field_conversationTime");

    //    field_customNotify = XposedHelpers.getObjectField(value, "field_customNotify").toString();
        field_digest = XposedHelpers.getObjectField(value, "field_digest").toString();
        field_digestUser = XposedHelpers.getObjectField(value, "field_digestUser").toString();
        field_editingMsg = XposedHelpers.getObjectField(value, "field_editingMsg").toString();

        field_firstUnDeliverSeq = XposedHelpers.getLongField(value, "field_firstUnDeliverSeq");
        field_flag = XposedHelpers.getLongField(value, "field_flag");

  //      field_hasTrunc = XposedHelpers.getIntField(value, "field_hasTrunc");
        field_isSend = XposedHelpers.getIntField(value, "field_isSend");

        field_lastSeq = XposedHelpers.getLongField(value, "field_lastSeq");
        field_msgCount = XposedHelpers.getIntField(value, "field_msgCount");

        field_msgType = XposedHelpers.getObjectField(value, "field_msgType").toString();
    //    field_parentRef = XposedHelpers.getObjectField(value, "field_parentRef").toString();

        field_showTips = XposedHelpers.getIntField(value, "field_showTips");
        field_sightTime = XposedHelpers.getLongField(value, "field_sightTime");

        field_status = XposedHelpers.getIntField(value, "field_status");
        field_unReadCount = XposedHelpers.getIntField(value, "field_unReadCount");
        field_unReadMuteCount = XposedHelpers.getIntField(value, "field_unReadMuteCount");

        field_username = XposedHelpers.getObjectField(value, "field_username").toString();
    }


    @Override
    public String toString() {
        return "MessageEntity{" +
                "field_UnDeliverCount=" + field_UnDeliverCount +
                ", field_UnReadInvite=" + field_UnReadInvite +
                ", field_atCount=" + field_atCount +
                ", field_attrflag=" + field_attrflag +
                ", field_chatmode=" + field_chatmode +
                ", field_content='" + field_content + '\'' +
                ", field_conversationTime=" + field_conversationTime +
              //  ", field_customNotify='" + field_customNotify + '\'' +
                ", field_digest='" + field_digest + '\'' +
                ", field_digestUser='" + field_digestUser + '\'' +
                ", field_editingMsg='" + field_editingMsg + '\'' +
                ", field_firstUnDeliverSeq=" + field_firstUnDeliverSeq +
                ", field_flag=" + field_flag +
          //      ", field_hasTrunc=" + field_hasTrunc +
                ", field_isSend=" + field_isSend +
                ", field_lastSeq=" + field_lastSeq +
                ", field_msgCount=" + field_msgCount +
                ", field_msgType='" + field_msgType + '\'' +
       //         ", field_parentRef='" + field_parentRef + '\'' +
                ", field_showTips=" + field_showTips +
                ", field_sightTime=" + field_sightTime +
                ", field_status=" + field_status +
                ", field_unReadCount=" + field_unReadCount +
                ", field_unReadMuteCount=" + field_unReadMuteCount +
                ", field_username='" + field_username + '\'' +
                '}';
    }
}
