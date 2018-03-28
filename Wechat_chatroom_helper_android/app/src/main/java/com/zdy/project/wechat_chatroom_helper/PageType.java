package com.zdy.project.wechat_chatroom_helper;

/**
 * Created by zhudo on 2018/3/28.
 */

public class PageType {


    private static final int MODE_SHIFT = 30;
    private static final int MODE_MASK = 0x3 << MODE_SHIFT;

    public static final int CHAT_ROOMS = 2 << MODE_SHIFT;
    public static final int OFFICIAL = 3 << MODE_SHIFT;
    public static final int MAIN = 0 << MODE_SHIFT;
    public static final int CHATTING = 1 << MODE_SHIFT;

    public static final int CHATTING_WITH_CHAT_ROOMS = (1 << MODE_SHIFT) + 2;
    public static final int CHATTING_WITH_OFFICIAL = (1 << MODE_SHIFT) + 3;

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