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
import com.zdy.project.wechat_chatroom_helper.ui.MuteConversationDialog;
import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.zdy.project.wechat_chatroom_helper.Constants.*;

/**
 * Created by zhudo on 2017/7/2.
 */

public class HookLogic implements IXposedHookLoadPackage {

    /**
     * com.tencent.mm.ui.e 应当是ListView的 adapter 的父类
     */


    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    //映射免打扰群组的数据位置和实际View位置
    SparseIntArray newViewPositionWithDataPositionList = new SparseIntArray();

    //第一个免打扰群组的下标
    private int firstMutePosition = -1;

    //标记位，当点击Dialog内的免打扰群组时，防止onItemClick与getObject方法的position冲突
    private boolean clickChatRoomFlag = false;
    private boolean closeMuteConsversationFlag = false;

    MuteConversationDialog muteConversationDialog;


    private static ClassLoader mClassLoader;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {


        if (!loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) return;

        mClassLoader = loadPackageParam.classLoader;

        XposedHelpers.findAndHookMethod(Conversation_List_View_Adapter, loadPackageParam
                        .classLoader, "a",
                "com.tencent.mm.ui.e.b", SparseArray.class, HashMap.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;
                        SparseArray sparseArray = (SparseArray) param.args[1];
                        Log.v(".ui.conversation.g.a", sparseArray.toString() + "");

                        HashMap<String, Object> hashMap = (HashMap) param.args[2];
                        for (Map.Entry<String, Object> stringObjectEntry : hashMap.entrySet()) {

                            String key = stringObjectEntry.getKey();
                            Object value = stringObjectEntry.getValue();

                            Log.v(".ui.conversation.g.a", "key = " + key + ", value = " + value);
                        }
                    }
                });

        /**
         * 消息列表数量
         */
        XposedHelpers.findAndHookMethod(Conversation_List_View_Adapter_Parent, loadPackageParam.classLoader, "getCount", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!PreferencesUtils.open()) return;

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

                }

                int count = result - muteCount;//减去免打扰消息的數量

                count++;//增加入口位置

                param.setResult(count);
            }
        });

/**
 * 此方法等于getObject
 */
        XposedHelpers.findAndHookMethod(Conversation_List_View_Adapter_Parent,
                loadPackageParam.classLoader, "ev", int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;

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
                            closeMuteConsversationFlag = true;
                        }

                        Object bean = getMessageBeanForOriginIndex(param, index);

                        XposedBridge.log("XposedBridge, ev position = " + index);

                        param.setResult(bean);
                    }
                });

        XposedHelpers.findAndHookMethod(Conversation_List_View_Adapter, loadPackageParam
                        .classLoader, "getView",
                int.class, View.class, ViewGroup.class, new XC_MethodHook() {


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;

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


        XposedHelpers.findAndHookMethod(Conversation_List_Adapter_OnItemClickListener,
                loadPackageParam.classLoader, "onItemClick", AdapterView.class, View.class,
                int.class, long.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;


                        final View view = (View) param.args[1];
                        int position = (int) param.args[2];
                        final long id = (long) param.args[3];

                        XposedBridge.log("XposedBridge, onItemClick, view =" + view + " ,position = " + position + " ,id = " + id);

                        XposedBridge.log("XposedBridge, onItemClick, originPosition =" + position);

                        Object uWH = XposedHelpers.getObjectField(param.thisObject, "uWH");
                        final int headerViewsCount = (int) XposedHelpers.callMethod(uWH, "getHeaderViewsCount");

                        position = position - headerViewsCount;


                        XposedBridge.log("XposedBridge, onItemClick, getHeaderViewsCount =" + headerViewsCount);

                        XposedBridge.log("XposedBridge, onItemClick, position =" + position);

                        if (position == firstMutePosition && !clickChatRoomFlag) {

                            XposedBridge.log("XposedBridge, onItemClick, firstMutePosition");

                            Context context = view.getContext();

                            Object uXk = XposedHelpers.getObjectField(param.thisObject, "uXk");
                            if (muteConversationDialog == null) {
                                muteConversationDialog = new MuteConversationDialog(context);
                                muteConversationDialog.setAdapter(uXk);
                                muteConversationDialog.setMuteListInAdapterPositions(muteListInAdapterPositions);
                                muteConversationDialog.setOnDialogItemClickListener(new MuteConversationDialog.OnDialogItemClickListener() {

                                    @Override
                                    public void onItemClick(int relativePosition) {
                                        clickChatRoomFlag = true;
                                        XposedHelpers.callMethod(param.thisObject, "onItemClick"
                                                , param.args[0], view, relativePosition + headerViewsCount, id);
                                    }
                                });
                            }
                            muteConversationDialog.show();
                            param.setResult(null);
                            return;
                        }
                        afterHookedMethod(param);
                    }
                });


        hookLog(loadPackageParam);
    }

    public static void setAvatar(ImageView avatar, String field_username) {
        try {
            XposedHelpers.callStaticMethod(Class.forName(SET_AVATAR_CLASS, false, mClassLoader),
                    "h", avatar, field_username);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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

        XposedHelpers.findAndHookMethod(TENCENT_LOG_CLASS,
                loadPackageParam.classLoader, "i", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;


                        XposedBridge.log("WechatLog i= " + ", args[0] = " + param.args[0].toString()
                                + ", args[1] = " + param.args[1]);

                        Object arg = param.args[1];
                        if (arg instanceof String) {
                            if (((String) arg).contains("closeChatting")) {
                                if (closeMuteConsversationFlag && !PreferencesUtils.auto_close()) {
                                    muteConversationDialog.show();
                                    closeMuteConsversationFlag = false;
                                }
                            }
                        }
                    }
                });


    }

}
