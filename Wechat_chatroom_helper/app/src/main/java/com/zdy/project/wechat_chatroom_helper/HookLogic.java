package com.zdy.project.wechat_chatroom_helper;

import android.content.Context;
import android.provider.Telephony;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.zdy.project.wechat_chatroom_helper.Constants.WECHAT_PACKAGE_NAME;

/**
 * Created by zhudo on 2017/7/2.
 */

public class HookLogic implements IXposedHookLoadPackage {

    /**
     * com.tencent.mm.ui.e 应当是ListView的 adapter 的父类
     */
    String Conversation_List_View = ".ui.conversation.g";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) return;


        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + ".ui.conversation.g", loadPackageParam
                        .classLoader, "a",
                "com.tencent.mm.ui.e.b", SparseArray.class, HashMap.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        SparseArray sparseArray = (SparseArray) param.args[1];
                        Log.v(".ui.conversation.g.a", sparseArray.toString() + "");

                        HashMap<String, Object> hashMap = (HashMap) param.args[2];
                        for (Map.Entry<String, Object> stringObjectEntry : hashMap.entrySet()) {

                            String key = stringObjectEntry.getKey();
                            Object value = stringObjectEntry.getValue();


                            Log.v(".ui.conversation.g.a", "key = " + key + ", value = " + value);
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });


        /**
         * Conversation_List_View
         *
         * com.tencent.mm.storage.ad 为实体类model
         *
         * 其中conversation.g类中，方法b 为返回消息内容（nickname），参数表 b(com.tencent.mm.storage.ad paramad, int paramInt, boolean paramBoolean)
         *
         *
         */

        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + ".ui.conversation.g", loadPackageParam
                        .classLoader, "b",
                "com.tencent.mm.storage.ad", int.class, boolean.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object ad = param.args[0];

                        Object hdc = XposedHelpers.getObjectField(ad, "hdc");

                        Object tww = XposedHelpers.getObjectField(hdc, "tww");


                        Log.v("storage.ad", "field_username = " + XposedHelpers.getObjectField(ad, "field_username").toString());
                        Log.v("storage.ad", "field_content = " + XposedHelpers.getObjectField(ad, "field_content").toString());
                        Log.v("storage.ad", "field_msgCount = " + XposedHelpers.getObjectField(ad, "field_msgCount").toString());
                        Log.v("storage.ad", "field_conversationTime = " + new Date(XposedHelpers.getLongField(ad, "field_conversationTime") * 1000).toString());

                        XposedHelpers.setObjectField(param.args[0], "field_digest", "喫屎咧梁非凡");
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });


        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View, loadPackageParam
                        .classLoader, "getView",
                int.class, View.class, ViewGroup.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                        int position = (int) param.args[0];
                        View itemView = (View) param.args[1];
                        ViewGroup parent = (ViewGroup) param.args[2];


                        XposedBridge.log("XposedBridge, getView itemView = " + itemView.toString());

                        Object value = XposedHelpers.callMethod(param.thisObject, "ev", position);

                        MessageEntity entity=new MessageEntity(value);

                        XposedBridge.log("XposedBridge, getView MessageEntity = " + entity.toString());


                        Object viewHolder = itemView.getTag();

                        Object textView = XposedHelpers.getObjectField(viewHolder, "usj");

                        XposedHelpers.callMethod(textView,"setText","王倩是傻逼");

                    }
                });
    }


}
