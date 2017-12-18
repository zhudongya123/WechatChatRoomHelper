package com.zdy.project.wechat_chatroom_helper;

import android.app.Activity;
import android.content.Context;
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
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zdy.project.wechat_chatroom_helper.manager.Type;
import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;
import com.zdy.project.wechat_chatroom_helper.ui.chatroomView.ChatRoomViewPresenter;
import com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView.ChatRoomRecyclerViewAdapter;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;
import com.zdy.project.wechat_chatroom_helper.utils.SoftKeyboardUtil;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import utils.AppSaveInfoUtils;

import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_Adapter_OnItemClickListener_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_Parent_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_SimpleName;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Set_Avatar;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Tencent_Log;
import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Chatroom_Avatar;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_1;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_2;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_3;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Conversation_List_View_Adapter_Param;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_Status_Bean;
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
    private ArrayList<Integer> chatRoomListInAdapterPositions = new ArrayList<>();

    //记录当前有多少个免打扰群有新消息
    private SparseIntArray unReadCountListForChatRoom = new SparseIntArray();

    //第一个免打扰群组的下标
    private int firstChatRoomPosition = -1;


    //免打扰公众号的数据位置
    private ArrayList<Integer> officialListInAdapterPositions = new ArrayList<>();

    //记录当前有多少个公众号有新消息
    private SparseIntArray unReadCountListForOfficial = new SparseIntArray();

    //第一个公众号的下标
    private int firstOfficialPosition = -1;


    //映射出现在主界面的回话的数据位置和实际View位置
    private SparseIntArray newViewPositionWithDataPositionList = new SparseIntArray();

    private ChatRoomViewPresenter chatRoomViewPresenter;
    private ChatRoomViewPresenter officialViewPresenter;


    //标记位，当点击Dialog内的免打扰群组时，防止onItemClick与getObject方法的position冲突
    private boolean clickChatRoomFlag = false;

    //标记位，数据刷新时不更新微信主界面的ListView
    private boolean notifyList = true;

    //是否在聊天界面
    private boolean isInChatting = false;

    //软键盘是否打开
    private boolean isSoftKeyBoardOpen = false;

    private static ClassLoader mClassLoader;
    private Context context;

    private View maskView;

    public static ArrayList<String> allChatRoomNickNameEntries;
    public static ArrayList<String> muteChatRoomNickNameEntries;
    public static ArrayList<String> officialNickNameEntries;


    @Override

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) return;

        mClassLoader = loadPackageParam.classLoader;

        if (!AppSaveInfoUtils.INSTANCE.initVariableName()) return;//判断是否获取了配置

        if (!AppSaveInfoUtils.INSTANCE.openInfo()) return;


        XposedHelpers.findAndHookConstructor("com.tencent.mm.ui.HomeUI.FitSystemWindowLayoutView",
                loadPackageParam.classLoader, Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookFitSystemWindowLayoutViewConstructor(param);
                    }
                });

        XposedHelpers.findAndHookConstructor("com.tencent.mm.ui.HomeUI.FitSystemWindowLayoutView",
                loadPackageParam.classLoader, Context.class, AttributeSet.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookFitSystemWindowLayoutViewConstructor(param);
                    }
                });

        XposedHelpers.findAndHookConstructor(Class_Conversation_List_View_Adapter_Name, loadPackageParam.classLoader,
                Context.class, XposedHelpers.findClass(Method_Conversation_List_View_Adapter_Param,
                        loadPackageParam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookAdapterInit(param);
                    }
                });

        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader,
                "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        context = (Context) param.thisObject;

                        SoftKeyboardUtil.observeSoftKeyboard((Activity) context, new SoftKeyboardUtil
                                .OnSoftKeyboardChangeListener() {

                            public void onSoftKeyBoardChange(int softKeyboardHeight, boolean visible) {
                                XposedBridge.log("SoftKeyboardUtil, observeSoftKeyboard, softKeyboardHeight = " +
                                        softKeyboardHeight + ", visible = " + visible);

                                isSoftKeyBoardOpen = visible;
                                if (chatRoomViewPresenter == null) return;
                                if (officialViewPresenter == null) return;
                                if (chatRoomViewPresenter.isShowing() || officialViewPresenter.isShowing()) {
                                    if (isSoftKeyBoardOpen) maskView.setVisibility(View.VISIBLE);
                                    else maskView.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                });

        XposedHelpers.findAndHookMethod("android.widget.BaseAdapter", loadPackageParam.classLoader,
                "notifyDataSetChanged", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookNotifyDataSetChanged(param);
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, "getCount", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookGetCount(param);
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, Method_Adapter_Get_Object, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookGetObject(param);
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Name,
                loadPackageParam.classLoader, "getView", int.class, View.class,
                ViewGroup.class, new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookGetView(param);
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_Adapter_OnItemClickListener_Name,
                loadPackageParam.classLoader, "onItemClick", AdapterView.class, View.class,
                int.class, long.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        hookOnItemClick(param);
                    }
                });

        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader,
                "dispatchKeyEvent", KeyEvent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        hookDispatchKeyEvent(param);
                    }
                });

        XposedHelpers.findAndHookMethod("android.view.ViewGroup", loadPackageParam.classLoader,
                "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        if (param.thisObject.getClass().getSimpleName().equals("TestTimeForChatting")) {
                            XposedBridge.log("TestTimeForChatting, dispatchKeyEvent, MotionEvent = " + param.args[0]
                                    .toString());

                            MotionEvent motionEvent = (MotionEvent) param.args[0];
                            if (!isInChatting) {
                                maskView.setVisibility(View.INVISIBLE);
                                return;
                            }

                            switch (motionEvent.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (isSoftKeyBoardOpen)
                                        maskView.setVisibility(View.INVISIBLE);
                                    break;
                                case MotionEvent.ACTION_UP:
                                    break;
                            }


                        }
                    }
                });

        hookLog(loadPackageParam);
    }

    private void hookDispatchKeyEvent(XC_MethodHook.MethodHookParam param) {
        KeyEvent keyEvent = (KeyEvent) param.args[0];

        //手指离开返回键的事件
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK
                && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {

            //不在聊天界面
            if (!isInChatting) {
                //群消息助手在屏幕上显示
                if (chatRoomViewPresenter.isShowing()) {
                    XposedBridge.log("dispatchKeyEvent, chatRoomViewPresenter.isShowing");
                    chatRoomViewPresenter.dismiss();
                    param.setResult(true);
                }

                //公众号助手在屏幕上显示
                if (officialViewPresenter.isShowing()) {
                    XposedBridge.log("dispatchKeyEvent, officialViewPresenter.isShowing");
                    officialViewPresenter.dismiss();
                    param.setResult(true);
                }
            } else {
                XposedBridge.log("dispatchKeyEvent, isInChatting");
            }
        }
    }


    private boolean isWechatHighVersion(String wechatVersion) {
        return wechatVersion.equals("1140") || wechatVersion.equals("1160") || Integer.valueOf(wechatVersion) > 1160;
    }

    private void hookFitSystemWindowLayoutViewConstructor(final XC_MethodHook.MethodHookParam param) {
        final ViewGroup fitSystemWindowLayoutView = (ViewGroup) param.thisObject;

        fitSystemWindowLayoutView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (isWechatHighVersion(AppSaveInfoUtils.INSTANCE.wechatVersionInfo())) {
                    if (fitSystemWindowLayoutView.getChildCount() != 3) return;

                    if (!fitSystemWindowLayoutView.getChildAt(0).getClass().getSimpleName().equals("LinearLayout"))
                        return;
                    if (!fitSystemWindowLayoutView.getChildAt(2).getClass().getSimpleName().equals
                            ("TestTimeForChatting"))
                        return;

                    fitSystemWindowLayoutView.addView(chatRoomViewPresenter.getPresenterView(), 2);
                    fitSystemWindowLayoutView.addView(officialViewPresenter.getPresenterView(), 3);


                    maskView = new View(context);
                    maskView.setBackgroundColor(0xff000000);
                    maskView.setVisibility(View.INVISIBLE);
                    FrameLayout.LayoutParams maskParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT);
                    maskParams.setMargins(0, ScreenUtils.dip2px(context, 30), 0, 0);
                    maskView.setLayoutParams(maskParams);

                    fitSystemWindowLayoutView.addView(maskView, 4);

                } else {
                    if (fitSystemWindowLayoutView.getChildCount() != 2) return;

                    if (!fitSystemWindowLayoutView.getChildAt(0).getClass().getSimpleName().equals("LinearLayout"))
                        return;
                    if (!fitSystemWindowLayoutView.getChildAt(1).getClass().getSimpleName().equals("TestTimeForChatting"))
                        return;

                    fitSystemWindowLayoutView.addView(chatRoomViewPresenter.getPresenterView(), 1);
                    fitSystemWindowLayoutView.addView(officialViewPresenter.getPresenterView(), 2);


                    maskView = new View(context);
                    maskView.setBackgroundColor(0xff000000);
                    maskView.setVisibility(View.INVISIBLE);
                    ViewGroup.LayoutParams maskParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    maskView.setLayoutParams(maskParams);

                    fitSystemWindowLayoutView.addView(maskView, 3);
                }

            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });

    }

    private void hookAdapterInit(XC_MethodHook.MethodHookParam param) {
        chatRoomViewPresenter = new ChatRoomViewPresenter(context, Type.CHAT_ROOMS);
        chatRoomViewPresenter.setAdapter(param.thisObject);
        chatRoomViewPresenter.start();

        officialViewPresenter = new ChatRoomViewPresenter(context, Type.OFFICIAL);
        officialViewPresenter.setAdapter(param.thisObject);
        officialViewPresenter.start();
    }


    private void hookOnItemClick(final XC_MethodHook.MethodHookParam param) {

        final View view = (View) param.args[1];
        int position = (int) param.args[2];
        final long id = (long) param.args[3];

        //移除頭部View的position
        Object listView = XposedHelpers.getObjectField(param.thisObject, Value_ListView);
        final int headerViewsCount = (int) XposedHelpers.callMethod(listView, "getHeaderViewsCount");

        position = position - headerViewsCount;


        //如果点击的是免打扰消息的入口，且不是在群消息助手里面所做的模拟点击（注意！此方法本身就为点击后的处理方法）
        if (position == firstChatRoomPosition && !clickChatRoomFlag) {

            chatRoomViewPresenter.setListInAdapterPositions(chatRoomListInAdapterPositions);
            chatRoomViewPresenter.setOnDialogItemClickListener(new ChatRoomRecyclerViewAdapter
                    .OnDialogItemClickListener() {
                @Override
                public void onItemClick(int relativePosition) {
                    clickChatRoomFlag = true;
                    XposedHelpers.callMethod(param.thisObject, "onItemClick"
                            , param.args[0], view, relativePosition + headerViewsCount, id);

                    if (AppSaveInfoUtils.INSTANCE.autoCloseInfo()) chatRoomViewPresenter.dismiss();
                }
            });
            chatRoomViewPresenter.show();
            param.setResult(null);
        }

        if (position == firstOfficialPosition && !clickChatRoomFlag) {
            officialViewPresenter.setListInAdapterPositions(officialListInAdapterPositions);
            officialViewPresenter.setOnDialogItemClickListener(new ChatRoomRecyclerViewAdapter
                    .OnDialogItemClickListener() {
                @Override
                public void onItemClick(int relativePosition) {
                    clickChatRoomFlag = true;
                    XposedHelpers.callMethod(param.thisObject, "onItemClick"
                            , param.args[0], view, relativePosition + headerViewsCount, id);

                    if (AppSaveInfoUtils.INSTANCE.autoCloseInfo()) officialViewPresenter.dismiss();
                }
            });
            officialViewPresenter.show();
            param.setResult(null);
        }
    }

    private void hookGetView(XC_MethodHook.MethodHookParam param) {

        int position = (int) param.args[0];
        View itemView = (View) param.args[1];

        if (itemView == null) return;
        if (itemView.getTag() == null) return;

        //修改群消息助手入口itemView
        Object viewHolder = itemView.getTag();


        Object title = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Title);
        final Object avatar = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Avatar);
        final Object content = XposedHelpers.getObjectField(viewHolder, Value_ListView_Adapter_ViewHolder_Content);


        //將第一個免打擾的itemView更改為群消息助手入口，更新其UI
        if (position == firstChatRoomPosition) {

            //修改nickname
            XposedHelpers.callMethod(title, "setText", "群消息助手");
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149));

            final Context context = itemView.getContext();

            //修改头像
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

            //重新设定消息未读数
            int newMessageCount = 0;
            for (int k = 0; k < unReadCountListForChatRoom.size(); k++) {
                int itemValue = unReadCountListForChatRoom.valueAt(k);

                if (itemValue > 0) {
                    newMessageCount++;
                }
            }

            ViewGroup parent = (ViewGroup) ((ImageView) avatar).getParent();
            parent.getChildAt(1).setVisibility(View.INVISIBLE);

            if (unReadCountListForChatRoom.valueAt(0) > 0)
                parent.getChildAt(2).setVisibility(View.VISIBLE);

            //更新消息内容
            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个群有新消息]");
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72));
            }
        } else if (position == firstOfficialPosition) {

            //修改nickname
            XposedHelpers.callMethod(title, "setText", "公众号助手");
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149));

            final Context context = itemView.getContext();

            //修改头像
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

            //重新设定消息未读数
            int newMessageCount = 0;
            for (int k = 0; k < unReadCountListForOfficial.size(); k++) {
                int itemValue = unReadCountListForOfficial.valueAt(k);

                if (itemValue > 0) {
                    newMessageCount++;
                }
            }

            ViewGroup parent = (ViewGroup) ((ImageView) avatar).getParent();
            parent.getChildAt(1).setVisibility(View.INVISIBLE);

            if (unReadCountListForOfficial.valueAt(0) > 0)
                parent.getChildAt(2).setVisibility(View.VISIBLE);

            //更新消息内容
            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个公众号有新消息]");
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72));
            }
        } else
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", new BitmapDrawable());

    }

    private void hookGetObject(XC_MethodHook.MethodHookParam param) {

        int index = (int) param.args[0];//要取的数据下标

        String clazzName = param.thisObject.getClass().getSimpleName();

        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
            return;

        if (newViewPositionWithDataPositionList.size() != 0)
            index = newViewPositionWithDataPositionList.get(index, index);

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

        int result = (int) param.getResult();//原有会话数量

        String clazzName = param.thisObject.getClass().getSimpleName();

        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
            return;//是否为正确的Adapter

        if (result == 0) return;

        if (notifyList) {

            int chatRoomSize = chatRoomListInAdapterPositions.size();
            int officialSize = officialListInAdapterPositions.size();
//            XposedBridge.log("originSize = " + result + ", currentChatRoomSize = " + chatRoomSize + ", " +
//                    "currentOfficialSize = " + officialSize);

            int count = result - chatRoomSize + (chatRoomSize > 0 ? 1 : 0);//减去群的數量
            count = count - officialSize + (officialSize > 0 ? 1 : 0);//减去公众号的数量

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

            chatRoomListInAdapterPositions.clear();
            unReadCountListForChatRoom.clear();
            firstChatRoomPosition = -1;

            officialListInAdapterPositions.clear();
            unReadCountListForOfficial.clear();
            firstOfficialPosition = -1;

            newViewPositionWithDataPositionList.clear();

            officialNickNameEntries = new ArrayList<>();
            muteChatRoomNickNameEntries = new ArrayList<>();
            allChatRoomNickNameEntries = new ArrayList<>();

            for (int i = 0; i < ((BaseAdapter) param.thisObject).getCount(); i++) {
                Object value = getMessageBeanForOriginIndex(param.thisObject, i);

                Object messageStatus = XposedHelpers.callMethod(param.thisObject,
                        Method_Message_Status_Bean, value);


                MessageEntity entity = new MessageEntity(value);

                //是否为群组
                boolean isChatRoomConversation = isChatRoomConversation(messageStatus);

                //是否为公众号
                boolean isOfficialConversation = isOfficialConversation(value, messageStatus);

                if (isChatRoomConversation) {
                    if (firstChatRoomPosition == -1) {
                        firstChatRoomPosition = i;

                        if (officialListInAdapterPositions.size() != 0)
                            firstChatRoomPosition = firstChatRoomPosition - officialListInAdapterPositions.size() + 1;
                    }

                    chatRoomListInAdapterPositions.add(i);
                    unReadCountListForChatRoom.put(i, entity.field_unReadCount);
                }

                if (isOfficialConversation) {

                    if (firstOfficialPosition == -1) {
                        firstOfficialPosition = i;

                        if (chatRoomListInAdapterPositions.size() != 0)
                            firstOfficialPosition = firstOfficialPosition - chatRoomListInAdapterPositions.size() + 1;
                    }

                    officialListInAdapterPositions.add(i);
                    unReadCountListForOfficial.put(i, entity.field_unReadCount);
                }

                int chatRoomCount = chatRoomListInAdapterPositions.size();
                int officialCount = officialListInAdapterPositions.size();


                //非群免打扰消息或者是公众号消息 或者是最新的群消息和公众号消息（入口）   即需要在微信主界面展示的回话
                if (!isChatRoomConversation && !isOfficialConversation ||
                        (chatRoomCount == 1 && isChatRoomConversation && !isOfficialConversation) ||
                        (officialCount == 1 && isOfficialConversation && !isChatRoomConversation)) {
                    int key = i - (chatRoomCount >= 1 ? (chatRoomCount - 1) : chatRoomCount);
                    key = key - (officialCount >= 1 ? (officialCount - 1) : officialCount);
                    newViewPositionWithDataPositionList.put(key, i);
                }


            }
        }
        notifyList = true;

        if (chatRoomViewPresenter != null) {
            chatRoomViewPresenter.setListInAdapterPositions(chatRoomListInAdapterPositions);
        }

        if (officialViewPresenter != null) {
            officialViewPresenter.setListInAdapterPositions(officialListInAdapterPositions);
        }
    }

    private boolean isOfficialConversation(Object value, Object messageStatus) {
        String username = XposedHelpers.getObjectField(messageStatus, Constants.Value_Message_Bean_NickName).toString();

        ArrayList<String> list = AppSaveInfoUtils.INSTANCE.getWhiteList("white_list_official");

        boolean wcY = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_OFFICIAL_1);
        int wcU = XposedHelpers.getIntField(messageStatus, Value_Message_Status_Is_OFFICIAL_2);
        String field_username = ((String) XposedHelpers.getObjectField(value, Value_Message_Status_Is_OFFICIAL_3));

        boolean isOfficial = !"gh_43f2581f6fd6".equals(field_username) && wcY && (wcU == 1 || wcU == 2 || wcU == 3);

        if (isOfficial) {
            officialNickNameEntries.add(username);

            for (String s : list) {
                if (s.trim().equals(username)) return false;
            }
        }

        return isOfficial;
    }

    private boolean isChatRoomConversation(Object messageStatus) {

        boolean uyI = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_1);
        boolean uXX = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_2);

        ArrayList<String> list = AppSaveInfoUtils.INSTANCE.getWhiteList("white_list_chat_room");

        String username = XposedHelpers.getObjectField(messageStatus, Constants.Value_Message_Bean_NickName).toString();


        //这是个群聊
        if (uXX) {
            //搜集所有群聊的标记
            if (AppSaveInfoUtils.INSTANCE.chatRoomTypeInfo().equals("1")) {
                allChatRoomNickNameEntries.add(username);
                for (String s : list) {
                    if (s.trim().equals(username)) return false;
                }
                return true;
            }

            //还是一个免打扰的群聊
            if (uyI) {
                muteChatRoomNickNameEntries.add(username);
                for (String s : list)
                    if (s.trim().equals(username)) return false;

                return true;
            }
        }
        return false;
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


        if (AppSaveInfoUtils.INSTANCE.isCircleAvatarInfo()) {
            paint.setColor(0xFF12B7F6);
            canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        } else {
            canvas.drawColor(0xFF12B7F6);
        }

        canvas.drawBitmap(raw, 0, 0, paint);

    }

    //自造公众号助手头像
    public static void handlerOfficialBitmap(Canvas canvas, Paint paint, int size, Bitmap drawable) {
        Bitmap whiteMask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        whiteMask.eraseColor(Color.WHITE);

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

        if (AppSaveInfoUtils.INSTANCE.isCircleAvatarInfo()) {
            paint.setColor(0xFFF5CB00);
            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据下标返回消息列表里的消息条目，不受免打扰影响
     * 即为原数据
     */
    public static Object getMessageBeanForOriginIndex(Object adapter, int index) {
        Object tMb = XposedHelpers.getObjectField(adapter, Method_Adapter_Get_Object_Step_1);

        Object hdB = XposedHelpers.getObjectField(tMb, Method_Adapter_Get_Object_Step_2);

        Object bean = XposedHelpers.callMethod(hdB, Method_Adapter_Get_Object_Step_3, index);

        return bean;
    }

    private void hookLog(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(Class_Tencent_Log, loadPackageParam.classLoader, "i",
                String.class, String.class, Object[].class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!AppSaveInfoUtils.INSTANCE.openInfo()) return;

                        //无奈之举，只能使用拦截日志的做法来实现部分功能
                        Object arg = param.args[1];
                        if (arg != null) {
                            //关闭聊天窗口
                            if (((String) arg).contains("closeChatting")) {
                                isInChatting = false;
                            }
                            if (((String) arg).contains("startChatting")) {
                                isInChatting = true;
                            }

                            //收到新消息
                            if (((String) arg).contains("summerbadcr updateConversation talker")) {
                                Object[] objects = (Object[]) param.args[2];

                                String sendUsername = (String) objects[0];
                                if (sendUsername.contains("chatroom")) {
                                    if (chatRoomViewPresenter != null) {
                                        chatRoomViewPresenter.setMessageRefresh(sendUsername);
                                    }
                                }
                                if (officialViewPresenter != null) {
                                    officialViewPresenter.setMessageRefresh(sendUsername);
                                }
                            }

                        }
                    }
                });
    }

}
