package com.zdy.project.wechat_chatroom_helper;

/**
 * Created by zhudo on 2018/3/28.
 */

public class PageType {


    public static final int CHAT_ROOMS = 2;
    public static final int OFFICIAL = 3;
    public static final int MAIN = 0;

    public static final int CHATTING = 4;
    public static final int CHATTING_WITH_CHAT_ROOMS = CHATTING + 1;
    public static final int CHATTING_WITH_OFFICIAL = CHATTING + 2;


    public static String printPageType(int pagetype) {
        switch (pagetype) {
            case CHAT_ROOMS:
                return "CHAT_ROOMS";
            case OFFICIAL:
                return "OFFICIAL";
            case MAIN:
                return "MAIN";
            case CHATTING:
                return "CHATTING";
            case CHATTING_WITH_CHAT_ROOMS:
                return "CHATTING_WITH_CHAT_ROOMS";
            case CHATTING_WITH_OFFICIAL:
                return "CHATTING_WITH_OFFICIAL";
        }
        return "NaN";
    }
}