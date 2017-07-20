package com.zdy.project.wechat_chatroom_helper;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;

import java.util.ArrayList;
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
    String Conversation_List_View_Adapter = ".ui.conversation.g";
    String Conversation_List_View_Adapter_Parent = ".ui.e";


    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    //映射免打扰群组的数据位置和实际View位置
    SparseIntArray newViewPositionWithDataPositionList = new SparseIntArray();

    //第一个免打扰群组的下标
    private int firstMutePosition = -1;

    //标记位，当点击Dialog内的免打扰群组时，防止onItemClick与getObject方法的position冲突
    private boolean clickChatRoomFlag = false;

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

        /**
         * 消息列表数量
         */
        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter_Parent,
                loadPackageParam.classLoader, "getCount", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        int result = (int) param.getResult();

                        String clazzName = param.thisObject.getClass().getSimpleName();

                        if (!clazzName.equals("g")) return;

                        if (result == 0) return;

                        newViewPositionWithDataPositionList.clear();
                        muteListInAdapterPositions.clear();
                        int muteCount = 0;//免打扰消息群组的的數量
                        firstMutePosition = -1;

                        for (int i = 0; i < result; i++) {
                            Object value = getMessageBeanForOriginIndex(param, i);
                            Object messageStatus = XposedHelpers.callMethod(param.thisObject, "j", value);

                            boolean uyI = XposedHelpers.getBooleanField(messageStatus, "uyI");
                            boolean uXX = XposedHelpers.getBooleanField(messageStatus, "uXX");

                            if (uyI && uXX) {
                                if (firstMutePosition == -1)
                                    firstMutePosition = i;

                                muteCount++;
                                muteListInAdapterPositions.add(i);
                            }
                            if (!(uyI && uXX) || muteCount == 1) {
                                newViewPositionWithDataPositionList.put(i - (muteCount >= 1 ? muteCount - 1 : muteCount), i);
                            }

//                            if (uyI)//是否为群
//                                XposedBridge.log("XposedBridge, getCount position = " + i + ", uyI" + uyI);
//
//                            if (uXX)
//                                XposedBridge.log("XposedBridge, getCount position = " + i + ", uXX" + uXX);
                        }

                        int count = result - muteCount;//减去免打扰消息的數量

                        count++;//增加入口位置

                        param.setResult(count);
//
//                        XposedBridge.log("XposedBridge, getCount muteCount = " + muteCount);
//
//                        XposedBridge.log("XposedBridge, getCount count = " + result);
//
//                        XposedBridge.log("XposedBridge, getCount mutePosition = " + muteListInAdapterPositions.toString());
//
//                        XposedBridge.log("XposedBridge, getCount dataRelativePosition= " + newViewPositionWithDataPositionList.toString());
                    }
                });

/**
 * 此方法等于getObject
 */
        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter_Parent,
                loadPackageParam.classLoader, "ev", int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        int index = (int) param.args[0];

                        XposedBridge.log("XposedBridge, ev dataPosition = " + param.args[0]);

                        String clazzName = param.thisObject.getClass().getSimpleName();

                        if (!clazzName.equals("g")) return;

                        if (newViewPositionWithDataPositionList.size() != 0)
                            index = newViewPositionWithDataPositionList.get(index, index);

                        //如果剛剛點擊了Dialog中的item，則下一次getObject方法，不再修改數據和View的位置
                        if (clickChatRoomFlag) {
                            index = (int) param.args[0];
                            clickChatRoomFlag = false;
                        }

                        Object bean = getMessageBeanForOriginIndex(param, index);

                        XposedBridge.log("XposedBridge, ev position = " + index);

                        param.setResult(bean);
                    }
                });

        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + Conversation_List_View_Adapter, loadPackageParam
                        .classLoader, "getView",
                int.class, View.class, ViewGroup.class, new XC_MethodHook() {


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        int position = (int) param.args[0];
                        View itemView = (View) param.args[1];


                        XposedBridge.log("XposedBridge, getView itemView = " + itemView.toString());

                        Object value = XposedHelpers.callMethod(param.thisObject, "ev", position);

                        MessageEntity entity = new MessageEntity(value);

                        XposedBridge.log("XposedBridge, getView MessageEntity = " + entity.toString());

                        Object viewHolder = itemView.getTag();

                        //免打扰icon
                        ImageView usm = (ImageView) XposedHelpers.getObjectField(viewHolder, "usm");

                        Object messageStatus = XposedHelpers.callMethod(param.thisObject, "j", value);

                        boolean uyI = XposedHelpers.getBooleanField(messageStatus, "uyI");
                        boolean uXX = XposedHelpers.getBooleanField(messageStatus, "uXX");

                        if (uyI && uXX) {
                            XposedBridge.log("XposedBridge, " + entity.field_content + ", 这条消息是免打扰的");
                        }

                        Object title = XposedHelpers.getObjectField(viewHolder, "usj");

                        if (position == firstMutePosition) {
                            XposedHelpers.callMethod(title, "setText", "群助手消息");
                        }
                    }
                });


        XposedHelpers.findAndHookMethod(WECHAT_PACKAGE_NAME + ".ui.conversation.e",
                loadPackageParam.classLoader, "onItemClick", AdapterView.class, View.class,
                int.class, long.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                        View view = (View) param.args[1];
                        int position = (int) param.args[2];
                        final long id = (long) param.args[3];

                        XposedBridge.log("XposedBridge, onItemClick, view =" + view + " ,position = " + position + " ,id = " + id);


                        XposedBridge.log("XposedBridge, onItemClick, originPosition =" + position);

                        Object uWH = XposedHelpers.getObjectField(param.thisObject, "uWH");
                        final int headerViewsCount = (int) XposedHelpers.callMethod(uWH, "getHeaderViewsCount");

//                        if (id != -1) {
                        position = position - headerViewsCount;
                        //                      }

                        XposedBridge.log("XposedBridge, onItemClick, getHeaderViewsCount =" + headerViewsCount);

                        XposedBridge.log("XposedBridge, onItemClick, position =" + position);

                        if (position == firstMutePosition && !clickChatRoomFlag) {

                            XposedBridge.log("XposedBridge, onItemClick, firstMutePosition");

                            Context context = view.getContext();

                            //     LinearLayout linearLayout = new LinearLayout(context);
                            //         linearLayout.setOrientation(LinearLayout.VERTICAL);


//                            final Dialog dialog = new Dialog(context);

                            //   for (int i = 0; i < muteListInAdapterPositions.size(); i++) {
                            //        Integer muteListInAdapterPosition = muteListInAdapterPositions.get(i);


                            //         Object value = getMessageBeanForOriginIndex(uXk, muteListInAdapterPosition);

                            //   MessageEntity entity = new MessageEntity(value);

//                                final TextView textView = new TextView(context);
//                                textView.setText(entity.field_username + entity.field_digest);
//
//                                final int finalI = i;
//                                textView.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        dialog.dismiss();
//                                        clickChatRoomFlag = true;
//                                        XposedHelpers.callMethod(param.thisObject, "onItemClick"
//                                                , param.args[0], param.args[1],
//                                                muteListInAdapterPositions.get(finalI) + headerViewsCount, id);
//                                    }
//                                });


//                                linearLayout.addView(textView);
                            //      }
//
//                            dialog.setContentView(linearLayout);
//                            dialog.show();


                            Object uXk = XposedHelpers.getObjectField(param.thisObject, "uXk");
                            final MuteConversationDialog muteConversationDialog = new MuteConversationDialog(context);
                            muteConversationDialog.setAdapter(uXk);
                            muteConversationDialog.setMuteListInAdapterPositions(muteListInAdapterPositions);
                            muteConversationDialog.setOnDialogItemClickListener(new MuteConversationDialog
                                    .OnDialogItemClickListener() {

                                @Override
                                public void onItemClick(int relativePosition) {
                                    clickChatRoomFlag = true;
                                    XposedHelpers.callMethod(param.thisObject, "onItemClick"
                                            , param.args[0], param.args[1], relativePosition + headerViewsCount, id);
                                    muteConversationDialog.dismiss();
                                }
                            });


                            muteConversationDialog.show();
                            param.setResult(null);
                            return;
                        }
                        afterHookedMethod(param);
                    }
                });


        hookLog(loadPackageParam);
    }


    /**
     * 根据下标返回消息列表里的消息条目，不受免打扰影响
     * 即为原数据
     */
    private Object getMessageBeanForOriginIndex(XC_MethodHook.MethodHookParam param, int index) {
        Object tMb = XposedHelpers.getObjectField(param.thisObject, "tMb");

        Object hdB = XposedHelpers.getObjectField(tMb, "hdB");

        Object bean = XposedHelpers.callMethod(hdB, "ev", index);

        XposedHelpers.callMethod(bean, "tE");
        return bean;
    }

    public static Object getMessageBeanForOriginIndex(Object adapter, int index) {
        Object tMb = XposedHelpers.getObjectField(adapter, "tMb");

        Object hdB = XposedHelpers.getObjectField(tMb, "hdB");

        Object bean = XposedHelpers.callMethod(hdB, "ev", index);

        XposedHelpers.callMethod(bean, "tE");
        return bean;
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
