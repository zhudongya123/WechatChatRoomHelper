package com.zdy.project.wechat_chatroom_helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zdy.project.wechat_chatroom_helper.crash.CrashHandler;
import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomRecyclerViewAdapter;
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomViewPresenter;
import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_Adapter_OnItemClickListener_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_Parent_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_SimpleName;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Set_Avatar;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Tencent_Home_UI;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Tencent_Log;
import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Chatroom_Avatar;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_1;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_2;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_3;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Conversation_List_View_Adapter_Param;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Home_UI_Inflater_View;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_Status_Bean;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Home_UI_Activity;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView_Adapter_ViewHolder_Avatar;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView_Adapter_ViewHolder_Content;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView_Adapter_ViewHolder_Title;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Status_Is_Mute_1;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Status_Is_Mute_2;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Status_Is_OFFICIAL_1;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Status_Is_OFFICIAL_2;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Status_Is_OFFICIAL_3;
import static com.zdy.project.wechat_chatroom_helper.Constants.WECHAT_PACKAGE_NAME;

/**
 * Created by zhudo on 2017/7/2.
 */

public class HookLogic implements IXposedHookLoadPackage {

    //免打扰群组的数据位置
    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    //记录当前有多少个免打扰群有新消息
    private SparseIntArray unReadCountListForMute = new SparseIntArray();

    //第一个免打扰群组的下标
    private int firstMutePosition = -1;


    //免打扰公众号的数据位置
    private ArrayList<Integer> officialListInAdapterPositions = new ArrayList<>();

    //记录当前有多少个公众号有新消息
    private SparseIntArray unReadCountListForOfficial = new SparseIntArray();

    //第一个公众号的下标
    private int firstOfficialPosition = -1;


    //映射出现在主界面的回话的数据位置和实际View位置
    private SparseIntArray newViewPositionWithDataPositionListForOfficial = new SparseIntArray();


    private ChatRoomViewPresenter muteChatRoomViewPresenter;
    private ChatRoomViewPresenter officialChatRoomViewPresenter;


    //标记位，当点击Dialog内的免打扰群组时，防止onItemClick与getObject方法的position冲突
    private boolean clickChatRoomFlag = false;

    //标记位，数据刷新时不更新微信主界面的ListView
    private boolean notifyList = true;

    //是否在聊天界面
    private boolean isInChatting = false;

    private static ClassLoader mClassLoader;
    private Context context;


    @Override

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) return;

        mClassLoader = loadPackageParam.classLoader;

        if (!PreferencesUtils.initVariableName()) return;//判断是否获取了配置

        XposedHelpers.findAndHookMethod(Class_Tencent_Home_UI, loadPackageParam.classLoader, Method_Home_UI_Inflater_View,
                Intent.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        Object activity = XposedHelpers.getObjectField(param.thisObject, Value_Home_UI_Activity);

                        Window window = (Window) XposedHelpers.callMethod(activity, "getWindow");

                        ViewGroup viewGroup = (ViewGroup) window.getDecorView();

                        for (int i = 0; i < viewGroup.getChildCount(); i++) {

                            String simpleName = viewGroup.getChildAt(i).getClass().getSimpleName();

                            if (simpleName.equals("FitSystemWindowLayoutView")) {

                                ViewGroup fitSystemWindowLayoutView = (ViewGroup) viewGroup.getChildAt(i);

                                if (fitSystemWindowLayoutView.getChildCount() == 2) {
                                    fitSystemWindowLayoutView.addView(muteChatRoomViewPresenter.getPresenterView(), 1);
                                    fitSystemWindowLayoutView.addView(officialChatRoomViewPresenter.getPresenterView(), 2);
                                }
                            }
                        }
                    }
                });

        XposedHelpers.findAndHookConstructor(Class_Conversation_List_View_Adapter_Name, loadPackageParam.classLoader,
                Context.class, XposedHelpers.findClass(Method_Conversation_List_View_Adapter_Param,
                        loadPackageParam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            hookAdapterInit(param);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            CrashHandler.saveCrashInfo2File(e, context);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader,
                "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.thisObject.getClass().getSimpleName().equals("LauncherUI")) {
                            context = (Context) param.thisObject;
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("android.widget.BaseAdapter", loadPackageParam.classLoader,
                "notifyDataSetChanged", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            hookNotifyDataSetChanged(param);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            CrashHandler.saveCrashInfo2File(e, context);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, "getCount", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            hookGetCount(param);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            CrashHandler.saveCrashInfo2File(e, context);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, Method_Adapter_Get_Object, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            hookGetObject(param);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            CrashHandler.saveCrashInfo2File(e, context);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Name,
                loadPackageParam.classLoader, "getView", int.class, View.class,
                ViewGroup.class, new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            hookGetView(param);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            CrashHandler.saveCrashInfo2File(e, context);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_Adapter_OnItemClickListener_Name,
                loadPackageParam.classLoader, "onItemClick", AdapterView.class, View.class,
                int.class, long.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                        try {
                            hookOnItemClick(param);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            CrashHandler.saveCrashInfo2File(e, context);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader,
                "dispatchKeyEvent", KeyEvent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        KeyEvent keyEvent = (KeyEvent) param.args[0];
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK
                                && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            if (!isInChatting) {

                                if (muteChatRoomViewPresenter.isShowing()) {
                                    muteChatRoomViewPresenter.dismiss();
                                    param.setResult(true);
                                }

                                if (officialChatRoomViewPresenter.isShowing()) {
                                    officialChatRoomViewPresenter.dismiss();
                                    param.setResult(true);
                                }
                            }
                        }
                    }
                });


        hookLog(loadPackageParam);

        //   if (!PreferencesUtils.getBugUnread()) return;

        //   fixUnread(loadPackageParam);

        XposedHelpers.findAndHookMethod(View.class, "dispatchTouchEvent",
                MotionEvent.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean result = (boolean) param.getResult();

                        XposedBridge.log("XposedHelpers, " + param.thisObject.toString() + ", dispatchTouchEvent return =" + result);
                    }
                });
        XposedHelpers.findAndHookMethod(ViewGroup.class, "onInterceptTouchEvent",
                MotionEvent.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean result = (boolean) param.getResult();

                        XposedBridge.log("XposedHelpers, " + param.thisObject.toString() + ",onInterceptTouchEvent return =" + result);
                    }
                });
        XposedHelpers.findAndHookMethod(View.class, "onTouchEvent",
                MotionEvent.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean result = (boolean) param.getResult();

                        XposedBridge.log("XposedHelpers, " + param.thisObject.toString() + ",onTouchEvent return =" + result);
                    }
                });
    }

    private void hookAdapterInit(XC_MethodHook.MethodHookParam param) {
        muteChatRoomViewPresenter = new ChatRoomViewPresenter(context, "群消息助手");
        muteChatRoomViewPresenter.setAdapter(param.thisObject);
        muteChatRoomViewPresenter.start();

        officialChatRoomViewPresenter = new ChatRoomViewPresenter(context, "公众号助手");
        officialChatRoomViewPresenter.setAdapter(param.thisObject);
        officialChatRoomViewPresenter.start();
    }


    private void hookOnItemClick(final XC_MethodHook.MethodHookParam param) {
        if (!PreferencesUtils.open()) return;

        final View view = (View) param.args[1];
        int position = (int) param.args[2];
        final long id = (long) param.args[3];

        //      XposedBridge.log("XposedBridge, onItemClick, view =" + view + " ,position = " + position + " ,id = " + id);

        //      XposedBridge.log("XposedBridge, onItemClick, originPosition =" + position);

        //移除頭部View的position
        Object listView = XposedHelpers.getObjectField(param.thisObject, Value_ListView);
        final int headerViewsCount = (int) XposedHelpers.callMethod(listView, "getHeaderViewsCount");

        position = position - headerViewsCount;

        //       XposedBridge.log("XposedBridge, onItemClick, getHeaderViewsCount =" + headerViewsCount);

        //      XposedBridge.log("XposedBridge, onItemClick, position =" + position);
        //     XposedBridge.log("XposedBridge, onItemClick, firstMutePosition =" + firstMutePosition);
        //       XposedBridge.log("XposedBridge, onItemClick, firstOfficialPosition =" + firstOfficialPosition);

//        XposedBridge.log("XposedBridge, onItemClick, newViewPositionWithDataPositionListForOfficial = " +
        //             newViewPositionWithDataPositionListForOfficial.toString());

        //如果点击的是免打扰消息的入口，且不是在群消息助手里面所做的模拟点击（注意！此方法本身就为点击后的处理方法）
        if (position == firstMutePosition && !clickChatRoomFlag) {
            //         XposedBridge.log("XposedBridge, onItemClick, firstMutePosition");

            muteChatRoomViewPresenter.setMuteListInAdapterPositions(muteListInAdapterPositions);
            muteChatRoomViewPresenter.setOnDialogItemClickListener(new ChatRoomRecyclerViewAdapter.OnDialogItemClickListener() {
                @Override
                public void onItemClick(int relativePosition) {
                    clickChatRoomFlag = true;
                    XposedHelpers.callMethod(param.thisObject, "onItemClick"
                            , param.args[0], view, relativePosition + headerViewsCount, id);

                    if (PreferencesUtils.auto_close())
                        muteChatRoomViewPresenter.dismiss();
                }
            });
            muteChatRoomViewPresenter.show();
            param.setResult(null);
        }

        if (position == firstOfficialPosition && !clickChatRoomFlag) {
            //       XposedBridge.log("XposedBridge, onItemClick, firstOfficialPosition");

            officialChatRoomViewPresenter.setMuteListInAdapterPositions(officialListInAdapterPositions);
            officialChatRoomViewPresenter.setOnDialogItemClickListener(new ChatRoomRecyclerViewAdapter
                    .OnDialogItemClickListener() {
                @Override
                public void onItemClick(int relativePosition) {
                    clickChatRoomFlag = true;
                    XposedHelpers.callMethod(param.thisObject, "onItemClick"
                            , param.args[0], view, relativePosition + headerViewsCount, id);

                    if (PreferencesUtils.auto_close())
                        muteChatRoomViewPresenter.dismiss();
                }
            });
            officialChatRoomViewPresenter.show();
            param.setResult(null);
        }
    }

    private void hookGetView(XC_MethodHook.MethodHookParam param) {
        if (!PreferencesUtils.open()) return;

        int position = (int) param.args[0];
        View itemView = (View) param.args[1];

        if (itemView == null) return;
        if (itemView.getTag() == null) return;

        //修改群消息助手入口itemView
        Object viewHolder = itemView.getTag();


        Object title = XposedHelpers.getObjectField(viewHolder,
                Value_ListView_Adapter_ViewHolder_Title);
        final Object avatar = XposedHelpers.getObjectField(viewHolder,
                Value_ListView_Adapter_ViewHolder_Avatar);
        final Object content = XposedHelpers.getObjectField(viewHolder,
                Value_ListView_Adapter_ViewHolder_Content);


        //將第一個免打擾的itemView更改為群消息助手入口，更新其UI
        if (position == firstMutePosition) {

            XposedHelpers.callMethod(title, "setText", "群消息助手");
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149));

            final Context context = itemView.getContext();

            ShapeDrawable shapeDrawable = new ShapeDrawable(new Shape() {
                @Override
                public void draw(Canvas canvas, Paint paint) {
                    paint.setColor(0xFF12B7F6);
                    int size = canvas.getWidth();
                    int drawableId = context.getResources().getIdentifier(Drawable_String_Chatroom_Avatar, "drawable", context.getPackageName());
                    Bitmap temp = BitmapFactory.decodeResource(context.getResources(), drawableId);

                    handlerChatRoomBitmap(canvas, paint, size, temp);
                }
            });
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", shapeDrawable);
            XposedHelpers.callMethod(avatar, "setImageDrawable", shapeDrawable);
            XposedHelpers.callMethod(avatar, "setVisibility", View.VISIBLE);

            int newMessageCount = 0;
            for (int k = 0; k < unReadCountListForMute.size(); k++) {
                int itemValue = unReadCountListForMute.valueAt(k);

                XposedBridge.log("Message position = " + k + ", unreadCount = " + itemValue);
                if (itemValue > 0) {
                    newMessageCount++;
                }
            }

            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个群有新消息]");
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72));
            }
        } else if (position == firstOfficialPosition) {

            XposedHelpers.callMethod(title, "setText", "公众号助手");
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149));

            final Context context = itemView.getContext();

            ShapeDrawable shapeDrawable = new ShapeDrawable(new Shape() {
                @Override
                public void draw(Canvas canvas, Paint paint) {
                    paint.setColor(0xFF12B7F6);
                    int size = canvas.getWidth();

                    int drawableId = context.getResources().getIdentifier(Drawable_String_Chatroom_Avatar,
                            "drawable", context.getPackageName());
                    Bitmap temp = BitmapFactory.decodeResource(context.getResources(), drawableId);

                    handlerOfficialBitmap(canvas, paint, size, temp);
                }
            });
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", shapeDrawable);
            XposedHelpers.callMethod(avatar, "setImageDrawable", shapeDrawable);
            XposedHelpers.callMethod(avatar, "setVisibility", View.VISIBLE);

            int newMessageCount = 0;
            for (int k = 0; k < unReadCountListForOfficial.size(); k++) {
                int itemValue = unReadCountListForOfficial.valueAt(k);

                XposedBridge.log("Message position = " + k + ", unreadCount = " + itemValue);
                if (itemValue > 0) {
                    newMessageCount++;
                }
            }

            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个公众号有新消息]");
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72));
            }
        } else
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", new BitmapDrawable());


    }

    private void hookGetObject(XC_MethodHook.MethodHookParam param) {
        if (!PreferencesUtils.open()) return;//开关

        int index = (int) param.args[0];//要取的数据下标

        String clazzName = param.thisObject.getClass().getSimpleName();

        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
            return;

        if (newViewPositionWithDataPositionListForOfficial.size() != 0)
            index = newViewPositionWithDataPositionListForOfficial.get(index, index);

        //如果刚刚点击了群消息助手中的item，则因为模拟分发点击事件会调用getObject方法，
        // 则这一次getObject方法，不再修改数据和View的位置
        if (clickChatRoomFlag) {
            index = (int) param.args[0];//重置数据位置
            clickChatRoomFlag = false;
        }
        Object bean = getMessageBeanForOriginIndex(param.thisObject, index);

        param.setResult(bean);

    }

    private void hookGetCount(XC_MethodHook.MethodHookParam param) {
        if (!PreferencesUtils.open()) return;//开关

        int result = (int) param.getResult();//原有会话数量

        String clazzName = param.thisObject.getClass().getSimpleName();

        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
            return;//是否为正确的Adapter

        if (result == 0) return;

        if (notifyList) {
            int count = result - muteListInAdapterPositions.size();//减去免打扰消息的數量
            count++;//增加入口位置

            count = count - officialListInAdapterPositions.size();//减去公众号的数量
            count++;

            XposedBridge.log("XposedBridge = adapter mute size = " + muteListInAdapterPositions.size());
            XposedBridge.log("XposedBridge = adapter official size = " + officialListInAdapterPositions.size());
            XposedBridge.log("XposedBridge = adapter size = " + count);
            param.setResult(count);
        }
    }

    private void hookNotifyDataSetChanged(XC_MethodHook.MethodHookParam param) {
        String clazzName = param.thisObject.getClass().getSimpleName();

        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
            return;//是否为正确的Adapter


        notifyList = false;

        //代码保护区，此段执行时getCount逻辑跳过
        {

            XposedBridge.log("newViewPositionWithDataPositionListForOfficial = " +
                    newViewPositionWithDataPositionListForOfficial.toString());

            muteListInAdapterPositions.clear();
            unReadCountListForMute.clear();
            firstMutePosition = -1;

            officialListInAdapterPositions.clear();
            unReadCountListForOfficial.clear();
            firstOfficialPosition = -1;

            newViewPositionWithDataPositionListForOfficial.clear();

            for (int i = 0; i < ((BaseAdapter) param.thisObject).getCount(); i++) {
                Object value = getMessageBeanForOriginIndex(param.thisObject, i);

                Object messageStatus = XposedHelpers.callMethod(param.thisObject,
                        Method_Message_Status_Bean, value);

                MessageEntity entity = new MessageEntity(value);

                //是否为免打扰群组
                boolean isMuteConversation = isMuteConversation(messageStatus);

                //是否为公众号
                boolean isOfficialConversation = isOfficialConversation(value, messageStatus);

                if (isMuteConversation) {
                    if (firstMutePosition == -1) {

                        firstMutePosition = i;

                        if (officialListInAdapterPositions.size() != 0)
                            firstMutePosition = firstMutePosition - officialListInAdapterPositions.size() + 1;
                    }

                    muteListInAdapterPositions.add(i);

                    unReadCountListForMute.put(i, entity.field_unReadCount);
                }

                String field_username = ((String) XposedHelpers.getObjectField(value, "field_username"));//

                if (isOfficialConversation) {

                    XposedBridge.log("恭喜 user = " + field_username + ", 是一个公众号");//

                    if (firstOfficialPosition == -1) {
                        firstOfficialPosition = i;

                        if (muteListInAdapterPositions.size() != 0)
                            firstOfficialPosition = firstOfficialPosition - muteListInAdapterPositions.size() + 1;
                    }

                    officialListInAdapterPositions.add(i);

                    unReadCountListForOfficial.put(i, entity.field_unReadCount);
                }

                int muteCount = muteListInAdapterPositions.size();
                int officialCount = officialListInAdapterPositions.size();


                //非群免打扰消息或者是公众号消息 或者是最新的群消息和公众号消息（入口）   即需要在微信主界面展示的回话
                if (!isMuteConversation && !isOfficialConversation ||
                        (muteCount == 1 && isMuteConversation && !isOfficialConversation) ||
                        (officialCount == 1 && isOfficialConversation && !isMuteConversation)) {
                    int key = i - (muteCount >= 1 ? (muteCount - 1) : muteCount);
                    key = key - (officialCount >= 1 ? (officialCount - 1) : officialCount);
                    newViewPositionWithDataPositionListForOfficial.put(key, i);
                }


            }
        }
        notifyList = true;

        if (muteChatRoomViewPresenter != null) {
            muteChatRoomViewPresenter.setMuteListInAdapterPositions(muteListInAdapterPositions);
        }

        if (officialChatRoomViewPresenter != null) {
            officialChatRoomViewPresenter.setMuteListInAdapterPositions(officialListInAdapterPositions);
        }
    }

    private boolean isOfficialConversation(Object value, Object messageStatus) {

        boolean wcY = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_OFFICIAL_1);
        int wcU = XposedHelpers.getIntField(messageStatus, Value_Message_Status_Is_OFFICIAL_2);
        String field_username = ((String) XposedHelpers.getObjectField(value, Value_Message_Status_Is_OFFICIAL_3));

        return !"gh_43f2581f6fd6".equals(field_username) && wcY && (wcU == 1 || wcU == 2 || wcU == 3);
    }

    private boolean isMuteConversation(Object messageStatus) {

        boolean uyI = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_1);
        boolean uXX = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_2);

        return uyI && uXX;
    }

    //自造群消息助手头像
    private static void handlerChatRoomBitmap(Canvas canvas, Paint paint, int size, Bitmap drawable) {
        Bitmap whiteMask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        whiteMask.eraseColor(Color.WHITE);

        drawable = Bitmap.createScaledBitmap(drawable, size / 2, size / 2, false).copy(Bitmap.Config.ARGB_8888, false);

        //生成图
        Bitmap raw = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas bitmapCanvas = new Canvas(raw);

        //绘制logo
        bitmapCanvas.drawBitmap(drawable, size / 4, size / 4, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        //给logo染色
        bitmapCanvas.drawBitmap(whiteMask, 0, 0, paint);

        paint.setXfermode(null);


        if (PreferencesUtils.getCircleAvatar()) {
            paint.setColor(0xFF12B7F6);
            canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        } else {
            canvas.drawColor(0xFF12B7F6);
        }

        canvas.drawBitmap(raw, 0, 0, paint);

    }

    //自造群消息助手头像
    private static void handlerOfficialBitmap(Canvas canvas, Paint paint, int size, Bitmap drawable) {
        Bitmap whiteMask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        whiteMask.eraseColor(Color.WHITE);

        //    drawable = Bitmap.createScaledBitmap(drawable, size / 2, size / 2, false).copy(Bitmap.Config.ARGB_8888,
        //            false);

        drawable = Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888);

        Canvas logoCanvas = new Canvas(drawable);

        paint.setStrokeWidth(size / 20);

        paint.setStyle(Paint.Style.STROKE);

        paint.setColor(0xFF9F289F);

        logoCanvas.drawCircle(size / 8 + size / 20, size / 4, size / 8, paint);
        logoCanvas.drawCircle(size / 2 - size / 8 - size / 20, size / 4, size / 8, paint);

        //生成图
        Bitmap raw = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas bitmapCanvas = new Canvas(raw);

        //绘制logo
        bitmapCanvas.drawBitmap(drawable, size / 4, size / 4, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        //给logo染色
        bitmapCanvas.drawBitmap(whiteMask, 0, 0, paint);

        paint.setXfermode(null);


        if (PreferencesUtils.getCircleAvatar()) {
            paint.setColor(0xFFF5CB00);
            canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        } else {
            canvas.drawColor(0xFFF5CB00);
        }

        canvas.drawBitmap(raw, 0, 0, paint);

    }

    public static void setAvatar(ImageView avatar, String field_username) {
        try {
            XposedHelpers.callStaticMethod(Class.forName(Class_Set_Avatar, false, mClassLoader),
                    Constants.Method_Conversation_List_Get_Avatar, avatar, field_username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据下标返回消息列表里的消息条目，不受免打扰影响
     * 即为原数据
     */
    public static Object getMessageBeanForOriginIndex(Object adapter, int index) {
        //  try {
        Object tMb = XposedHelpers.getObjectField(adapter, Method_Adapter_Get_Object_Step_1);

        Object hdB = XposedHelpers.getObjectField(tMb, Method_Adapter_Get_Object_Step_2);

        Object bean = XposedHelpers.callMethod(hdB, Method_Adapter_Get_Object_Step_3, index);

        return bean;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    private void hookLog(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(Class_Tencent_Log,
                loadPackageParam.classLoader, "i", String.class, String.class, Object[].class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;

                        String desc = (String) param.args[0];
                        String value = (String) param.args[1];
                        String params = "";
                        try {
                            if (param.args[2] != null) {
                                Object[] arg = (Object[]) param.args[2];

                                //    for (Object o : arg)
//                                    if (o != null)
//                                        params = params + o.toString() + " ";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        XposedBridge.log("XposedLogi, desc = " + desc + ", value = " + value + ", params = " + params);

                        //无奈之举，只能使用拦截日志的做法来实现部分功能
                        Object arg = param.args[1];
                        if (arg != null) {
                            //关闭聊天窗口
                            if (((String) arg).contains("closeChatting")) {
                                isInChatting = false;
                            }
                            if (((String) arg).contains("startChatting"))
                                isInChatting = true;

                            //收到新消息
                            if (((String) arg).contains("summerbadcr updateConversation talker")) {
                                Object[] objects = (Object[]) param.args[2];

                                String sendUsername = (String) objects[0];
                                if (sendUsername.contains("chatroom")) {
                                    if (muteChatRoomViewPresenter != null) {
                                        muteChatRoomViewPresenter.setMessageRefresh(sendUsername);
                                    }
                                }
                                if (officialChatRoomViewPresenter != null) {
                                    officialChatRoomViewPresenter.setMessageRefresh(sendUsername);
                                }
                            }
                        }
                    }
                });

    }


    private void fixUnread(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        int versionCode = PreferencesUtils.getVersionCode();
        String methodHomeUIGetUnRead = null;
        String actionbarName = null;
        String bottomViewName = null;
        String bottomViewIndicatorName = null;
        if (versionCode == 1080) {
            methodHomeUIGetUnRead = "yI";
            actionbarName = "Gx";
            bottomViewName = "uxn";
            bottomViewIndicatorName = "uzx";
        } else if (versionCode == 1060) {
            methodHomeUIGetUnRead = "xu";
            actionbarName = "Gy";
            bottomViewName = "tNs";
            bottomViewIndicatorName = "tPz";
        }

        final String finalActionbarName = actionbarName;
        final String finalBottomViewName = bottomViewName;
        final String finalBottomViewIndicatorName = bottomViewIndicatorName;
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.HomeUI",
                loadPackageParam.classLoader, methodHomeUIGetUnRead, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int arg = (int) param.args[0];

                        if (muteChatRoomViewPresenter == null) return;
                        Object adapter = muteChatRoomViewPresenter.getOriginAdapter();
                        int count = (int) XposedHelpers.callMethod(adapter, "getCount");

                        int unReadCount = 0;
                        String unReadString = "[";
                        for (int i = 0; i < count; i++) {
                            Object message = getMessageBeanForOriginIndex(adapter, i);
                            MessageEntity messageEntity = new MessageEntity(message);

                            unReadString = unReadString + messageEntity.field_unReadCount;

                            for (Integer muteListInAdapterPosition : muteListInAdapterPositions) {
                                if (muteListInAdapterPosition == i) break;
                            }

                            unReadCount = +messageEntity.field_unReadCount;

                            if (i != count - 1) unReadString += ",";
                            else unReadString += "]";
                        }

                        Object actionBar = XposedHelpers
                                .getObjectField(param.thisObject, finalActionbarName);

                        View customView = (View) XposedHelpers.callMethod(actionBar, "getCustomView");

                        @IdRes
                        int textViewId = 16908308;

                        TextView textView = (TextView) customView.findViewById(textViewId);
                        String text = "微信(" + unReadCount + ")";
                        if (unReadCount == 0)
                            text = "微信";
                        textView.setText(text);


                        Object bottomView = XposedHelpers.getObjectField(param.thisObject, finalBottomViewName);
                        XposedHelpers.setIntField(bottomView, finalBottomViewIndicatorName, unReadCount);

                        XposedBridge.log("currentUnreadCount = " + arg + ", trueUnreadCount = "
                                + unReadString + ", unReadCount = " + unReadCount);
                    }
                });
    }


}
