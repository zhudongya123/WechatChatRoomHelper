package com.zdy.project.wechat_chatroom_helper;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.services.BaseService;

import static com.zdy.project.wechat_chatroom_helper.Constants.WECHAT_PACKAGE_NAME;

/**
 * Created by zhudo on 2017/7/2.
 */

public class HookLogic implements IXposedHookLoadPackage {

    /**
     * com.tencent.mm.ui.e 应当是ListView的 adapter 的父类
     */
    String Conversation_List_View_Adapter = ".ui.conversation.g";
    String Conversation_List_View_Adapter_Parent = ".ui.e";


    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    private int firstMutePosition = -1;

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
         * Conversation_List_View_Adapter
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

                        //  XposedHelpers.setObjectField(param.args[0], "field_digest", "喫屎咧梁非凡");
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });

        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter_Parent,
                loadPackageParam.classLoader, "getCount", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        int result = (int) param.getResult();

                        String clazzName = param.thisObject.getClass().getSimpleName();

                        if (!clazzName.equals("g")) return;

                        XposedBridge.log("XposedBridge, getCount itemCount = " + result);

                        muteListInAdapterPositions.clear();

                        if (result == 0) return;

                        int muteCount = 0;

                        firstMutePosition = -1;

                        for (int i = 0; i < result; i++) {
                            Object value = XposedHelpers.callMethod(param.thisObject, "ev", i);
                            Object messageStatus = XposedHelpers.callMethod(param.thisObject, "j", value);

                            boolean uyI = XposedHelpers.getBooleanField(messageStatus, "uyI");
                            boolean uXX = XposedHelpers.getBooleanField(messageStatus, "uXX");

                            if (uyI && uXX) {

                                if (firstMutePosition == -1)
                                    firstMutePosition = i;

                                muteCount++;
                                muteListInAdapterPositions.add(i);
                            }

//                            if (uyI)//是否为群
//                                XposedBridge.log("XposedBridge, getCount position = " + i + ", uyI" + uyI);
//
//                            if (uXX)
//                                XposedBridge.log("XposedBridge, getCount position = " + i + ", uXX" + uXX);
                        }

                        int count = result - muteCount;//减去免打扰消息

                        count++;//增加入口位置

                        param.setResult(count);

                        XposedBridge.log("XposedBridge, getCount count = " + count);
                    }
                });


        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter_Parent,
                loadPackageParam.classLoader, "ev", int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        int index = (int) param.args[0];

                        String clazzName = param.thisObject.getClass().getSimpleName();

                        if (!clazzName.equals("g")) return;

                        Object tMb = XposedHelpers.getObjectField(param.thisObject, "tMb");

                        Object hdB = XposedHelpers.getObjectField(tMb, "hdB");

                        Object bean = XposedHelpers.callMethod(hdB, "ev", index);

                        XposedHelpers.callMethod(bean, "tE");

                        param.setResult(bean);
                    }
                });

        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter, loadPackageParam
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

                        MessageEntity entity = new MessageEntity(value);

                        XposedBridge.log("XposedBridge, getView MessageEntity = " + entity.toString());

                        Object viewHolder = itemView.getTag();

                        Object textView = XposedHelpers.getObjectField(viewHolder, "usj");

                        //     XposedHelpers.callMethod(textView, "setText", "王倩是腿真细才怪");

                        //免打扰icon
                        ImageView usm = (ImageView) XposedHelpers.getObjectField(viewHolder, "usm");

                        Object messageStatus = XposedHelpers.callMethod(param.thisObject, "j", value);

                        boolean uyI = XposedHelpers.getBooleanField(messageStatus, "uyI");
                        boolean uXX = XposedHelpers.getBooleanField(messageStatus, "uXX");

                        if (uyI && uXX) {
                            XposedBridge.log("XposedBridge, " + entity.field_content + ", 这条消息是免打扰的");
                        }
                    }
                });


        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter,
                loadPackageParam.classLoader, "onResume", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        Object listPackage = XposedHelpers.getObjectField(param.thisObject, "tMb");

                        Object hdB = XposedHelpers.getObjectField(listPackage, "hdB");

                        XposedBridge.log("XposedBridge,afterHookedMethod hdB = " + hdB.toString());
                    }

                });

        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter,
                loadPackageParam.classLoader, "a", HashSet.class, SparseArray[].class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HashSet hashSet = (HashSet) param.args[0];
                        for (Object next : hashSet) {
                            XposedBridge.log("XposedBridge, hashSet =" + next.toString());
                        }

                        SparseArray<String>[] sparseArrays = (SparseArray<String>[]) param.args[1];

                        for (SparseArray<String> sparseArray : sparseArrays) {

                            XposedBridge.log("XposedBridge, sparseArray =" + sparseArray.toString());
                        }
                    }
                });


        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter,
                loadPackageParam.classLoader, "a", "com.tencent.mm.ui.e.b", SparseArray.class, HashMap.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        SparseArray sparseArray = (SparseArray) param.args[1];

                        XposedBridge.log("XposedBridge, sparseArray =" + sparseArray.toString());


                        HashMap HashMap = (HashMap) param.args[2];
                        Iterator iterator = HashMap.entrySet().iterator();


                        while (iterator.hasNext()) {
                            Object next = iterator.next();

                        }
                    }
                });


        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + ".ui.conversation.e",
                loadPackageParam.classLoader, "onItemClick", AdapterView.class, View.class,
                int.class, long.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                    }
                });


        hookLog(loadPackageParam);
    }

    private void hookLog(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod("com.tencent.mm.sdk.platformtools.v",
                loadPackageParam.classLoader, "d", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("WechatLog d= " + ", args[0] = " + param.args[0].toString()
                                + ", args[1] = " + param.args[1]);
                    }
                });

        XposedHelpers.findAndHookMethod("com.tencent.mm.sdk.platformtools.v",
                loadPackageParam.classLoader, "i", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("WechatLog i= " + ", args[0] = " + param.args[0].toString()
                                + ", args[1] = " + param.args[1]);
                    }
                });
        XposedHelpers.findAndHookMethod("com.tencent.mm.sdk.platformtools.v",
                loadPackageParam.classLoader, "v", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("WechatLog v= " + ", args[0] = " + param.args[0].toString()
                                + ", args[1] = " + param.args[1]);
                    }
                });


    }

}
