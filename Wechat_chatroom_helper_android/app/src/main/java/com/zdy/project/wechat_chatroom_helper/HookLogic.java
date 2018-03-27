package com.zdy.project.wechat_chatroom_helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zdy.project.wechat_chatroom_helper.ui.helper.manager.PageType;
import com.zdy.project.wechat_chatroom_helper.model.ChatInfoModel;
import com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView.ChatRoomViewPresenter;
import com.zdy.project.wechat_chatroom_helper.ui.wechat.manager.RuntimeInfo;
import com.zdy.project.wechat_chatroom_helper.ui.helper.avatar.AvatarMaker;
import com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView.ChatRoomRecyclerViewAdapter;
import com.zdy.project.wechat_chatroom_helper.utils.LogUtils;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;
import com.zdy.project.wechat_chatroom_helper.utils.SoftKeyboardUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import utils.AppSaveInfoUtils;

import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_Adapter_OnItemClickListener_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_Parent_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_SimpleName;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Set_Avatar;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Tencent_Log;
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
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;



/**
 *
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

    private Context context;

    private View maskView;

    public static ArrayList<String> allChatRoomNickNameEntries;
    public static ArrayList<String> muteChatRoomNickNameEntries;
    public static ArrayList<String> officialNickNameEntries;


    @Override

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) return;

        RuntimeInfo.mClassLoader = loadPackageParam.classLoader;

        if (!AppSaveInfoUtils.INSTANCE.initVariableName()) return;//判断是否获取了配置

        if (!AppSaveInfoUtils.INSTANCE.openInfo()) return;

        findAndHookConstructor("com.tencent.mm.ui.HomeUI.FitSystemWindowLayoutView",
                loadPackageParam.classLoader, Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookFitSystemWindowLayoutViewConstructor(param);

                    }
                });

        findAndHookConstructor("com.tencent.mm.ui.HomeUI.FitSystemWindowLayoutView",
                loadPackageParam.classLoader, Context.class, AttributeSet.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookFitSystemWindowLayoutViewConstructor(param);

                    }
                });

        findAndHookConstructor(Class_Conversation_List_View_Adapter_Name, loadPackageParam.classLoader,
                Context.class, XposedHelpers.findClass(Method_Conversation_List_View_Adapter_Param,
                        loadPackageParam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        LogUtils.INSTANCE.log("hookAdapterInit");
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

        findAndHookMethod("android.widget.BaseAdapter", loadPackageParam.classLoader,
                "notifyDataSetChanged", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        hookNotifyDataSetChanged(param);
                    }
                });

        findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, "getCount", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookGetCount(param);
                    }
                });

        findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, Method_Adapter_Get_Object, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookGetObject(param);
                    }
                });

        findAndHookMethod(Class_Conversation_List_View_Adapter_Name,
                loadPackageParam.classLoader, "getView", int.class, View.class,
                ViewGroup.class, new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookGetView(param);
                    }
                });

        findAndHookMethod(Class_Conversation_List_Adapter_OnItemClickListener_Name,
                loadPackageParam.classLoader, "onItemClick", AdapterView.class, View.class,
                int.class, long.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        hookOnItemClick(param);
                    }
                });

        findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader,
                "dispatchKeyEvent", KeyEvent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        hookDispatchKeyEvent(param);
                    }
                });

        findAndHookMethod("android.view.ViewGroup", loadPackageParam.classLoader,
                "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        if (param.thisObject.getClass().getSimpleName().equals("TestTimeForChatting")) {

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
                    LogUtils.INSTANCE.log("dispatchKeyEvent, chatRoomViewPresenter.isShowing");
                    chatRoomViewPresenter.dismiss();
                    param.setResult(true);
                }

                //公众号助手在屏幕上显示
                if (officialViewPresenter.isShowing()) {
                    LogUtils.INSTANCE.log("dispatchKeyEvent, officialViewPresenter.isShowing");
                    officialViewPresenter.dismiss();
                    param.setResult(true);
                }
            } else {
                LogUtils.INSTANCE.log("dispatchKeyEvent, isInChatting");
            }
        }
    }

    private boolean isWechatHighVersion(String wechatVersion) {
        return wechatVersion.equals("1140") || wechatVersion.equals("1160") || Integer.valueOf(wechatVersion) > 1160;
    }

    /**
     * 此方法完成的逻辑：
     * fitSystemWindowLayoutView 为微信主界面的 rootView 往此 View 中添加子 View 来实现助手界面
     * 注意添加下标一定要小于聊天View -> TestTimeChatting 的下标
     * <p>
     * 此部分同时包含一个黑色遮罩的逻辑，用来防止在助手界面软键盘弹出瞬间的View穿透问题，此逻辑一般不会触发，可以省略。
     * <p>
     * 同时为了确保助手View 尺寸正确，将会获取主界面聊天列表的布局参数，复制到助手的布局参数中，添加此逻辑主要来规避某些手机上虚拟按键和状态栏不准确的问题。
     *
     * @param param
     */
    private void hookFitSystemWindowLayoutViewConstructor(final XC_MethodHook.MethodHookParam param) {
        final ViewGroup fitSystemWindowLayoutView = (ViewGroup) param.thisObject;
        fitSystemWindowLayoutView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {

                View chattingView;//聊天View
                int chattingViewPosition;//聊天View的下標
                int fitWindowChildCount = 0;//fitSystemWindowLayoutView的 child 数量
                int chatRoomViewPosition = 0;
                int officialViewPosition = 0;
                int maskViewPosition = 0;

                /*
                 * 微信在某个版本之后 View 数量发生变化，下标也要相应刷新
                 **/
                if (isWechatHighVersion(AppSaveInfoUtils.INSTANCE.wechatVersionInfo())) {
                    fitWindowChildCount = 3;
                    chattingViewPosition = 2;
                    chatRoomViewPosition = 2;
                    officialViewPosition = 3;
                    maskViewPosition = 4;
                } else {
                    fitWindowChildCount = 2;
                    chattingViewPosition = 1;
                    chatRoomViewPosition = 1;
                    officialViewPosition = 2;
                    maskViewPosition = 3;
                }

                LogUtils.INSTANCE.log("FitSystemWindowLayoutView Constructor");

                chattingView = fitSystemWindowLayoutView.getChildAt(chattingViewPosition);
                if (fitSystemWindowLayoutView.getChildCount() != fitWindowChildCount) return;
                if (!(fitSystemWindowLayoutView.getChildAt(0) instanceof LinearLayout)) return;
                if (!chattingView.getClass().getSimpleName().equals("TestTimeForChatting")) return;

                if (chatRoomViewPresenter == null)
                    chatRoomViewPresenter = new ChatRoomViewPresenter(context, PageType.CHAT_ROOMS);
                if (officialViewPresenter == null)
                    officialViewPresenter = new ChatRoomViewPresenter(context, PageType.OFFICIAL);

                ViewParent chatRoomViewParent = chatRoomViewPresenter.getPresenterView().getParent();
                if (chatRoomViewParent != null) {
                    ((ViewGroup) chatRoomViewParent).removeView(chatRoomViewPresenter.getPresenterView());
                }

                ViewParent officialViewParent = officialViewPresenter.getPresenterView().getParent();
                if (officialViewParent != null) {
                    ((ViewGroup) chatRoomViewParent).removeView(officialViewPresenter.getPresenterView());
                }

                fitSystemWindowLayoutView.addView(chatRoomViewPresenter.getPresenterView(), chatRoomViewPosition);
                fitSystemWindowLayoutView.addView(officialViewPresenter.getPresenterView(), officialViewPosition);

                //黑色遮罩，逻辑可忽略
                maskView = new View(context);
                maskView.setBackgroundColor(0xff000000);
                maskView.setVisibility(View.INVISIBLE);
                FrameLayout.LayoutParams maskParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                maskView.setLayoutParams(maskParams);
                fitSystemWindowLayoutView.addView(maskView, maskViewPosition);


                //複製佈局參數邏輯
                //此逻辑并不完美，属于拆东墙补西墙

                if (((ViewGroup) fitSystemWindowLayoutView.getChildAt(0)).getChildCount() != 2)
                    return;
                final View mainView = ((ViewGroup) fitSystemWindowLayoutView.getChildAt(0)).getChildAt(1);
                mainView.getViewTreeObserver()
                        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                int left = mainView.getLeft();
                                int right = mainView.getRight();
                                int top = mainView.getTop();
                                int bottom = mainView.getBottom();

                                int width = right - left;
                                int height = bottom - top;

                                if (width == 0 || height == 0) return;

                                ViewGroup chatRoomViewPresenterPresenterView = chatRoomViewPresenter.getPresenterView();
                                ViewGroup officialViewPresenterPresenterView = officialViewPresenter.getPresenterView();


                                int left1 = chatRoomViewPresenterPresenterView.getLeft();
                                int top1 = chatRoomViewPresenterPresenterView.getTop();
                                int right1 = chatRoomViewPresenterPresenterView.getRight();
                                int bottom1 = chatRoomViewPresenterPresenterView.getBottom();

                                if (new Rect(left1, top1, right1, bottom1).equals(new Rect(left, top, right, bottom)))
                                    return;

                                FrameLayout.LayoutParams params
                                        = new FrameLayout.LayoutParams(width, height);
                                params.setMargins(0, top, 0, 0);

                                chatRoomViewPresenterPresenterView.setLayoutParams(params);
                                officialViewPresenterPresenterView.setLayoutParams(params);
                                maskView.setLayoutParams(params);

                            }
                        });
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });

    }

    private void hookAdapterInit(XC_MethodHook.MethodHookParam param) {
        if (chatRoomViewPresenter == null)
            chatRoomViewPresenter = new ChatRoomViewPresenter(context, PageType.CHAT_ROOMS);
        if (officialViewPresenter == null)
            officialViewPresenter = new ChatRoomViewPresenter(context, PageType.OFFICIAL);

        chatRoomViewPresenter.setAdapter(param.thisObject);
        chatRoomViewPresenter.start();

        officialViewPresenter.setAdapter(param.thisObject);
        officialViewPresenter.start();
    }

    private void hookOnItemClick(final XC_MethodHook.MethodHookParam param) {

        final View itemView = (View) param.args[1];
        int position = (int) param.args[2];
        final long id = (long) param.args[3];


        final Object listView = XposedHelpers.getObjectField(param.thisObject, Value_ListView);
        final int headerViewsCount = (int) XposedHelpers.callMethod(listView, "getHeaderViewsCount");

        LogUtils.INSTANCE.log("hookOnItemClick, position = " + position + ", headerViewsCount =" +
                headerViewsCount + ", view = " + itemView + " adapterView  = " + param.args[0]);

        if (itemView.getMeasuredHeight() != ScreenUtils.dip2px(context, 64f)) {
            //修正点击空白区域的问题
            param.setResult(null);
            return;
        }

        //移除頭部View的position
        position = position - headerViewsCount;


        //如果点击的是免打扰消息的入口，且不是在群消息助手里面所做的模拟点击（注意！此方法本身就为点击后的处理方法）
        if (position == firstChatRoomPosition && !clickChatRoomFlag) {
            chatRoomViewPresenter.setListInAdapterPositions(chatRoomListInAdapterPositions);
            chatRoomViewPresenter.setOnDialogItemClickListener(new ChatRoomRecyclerViewAdapter
                    .OnDialogItemClickListener() {
                @Override
                public void onItemLongClick(int relativePosition) {
                    try {
                        AdapterView.OnItemLongClickListener mOnItemLongClickListener =
                                (AdapterView.OnItemLongClickListener) XposedHelpers.findField(listView.getClass(),
                                        "mOnItemLongClickListener").get(listView);

                        mOnItemLongClickListener.onItemLongClick((AdapterView<?>) listView, null,
                                newViewPositionWithDataPositionList.get(relativePosition) + headerViewsCount, id);

                        LogUtils.INSTANCE.log("hookOnItemClick, onItemLongClick, relativePosition = " +
                                relativePosition + ", headerViewsCount =" +
                                headerViewsCount + ", view = " + itemView + " adapterView  = " + param.args[0]);

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onItemClick(int relativePosition) {
                    clickChatRoomFlag = true;
                    XposedHelpers.callMethod(param.thisObject, "onItemClick"
                            , param.args[0], itemView, relativePosition + headerViewsCount, id);

                    LogUtils.INSTANCE.log("hookOnItemClick, onItemClick, relativePosition = " + relativePosition + "," +
                            " headerViewsCount =" +
                            headerViewsCount + ", view = " + itemView + " adapterView  = " + param.args[0]);

                    if (AppSaveInfoUtils.INSTANCE.autoCloseInfo()) chatRoomViewPresenter.dismiss();
                }
            });
            chatRoomViewPresenter.show();
            RuntimeInfo.INSTANCE.changeCurrentPage(PageType.CHAT_ROOMS);
            param.setResult(null);
        }

        if (position == firstOfficialPosition && !clickChatRoomFlag) {
            officialViewPresenter.setListInAdapterPositions(officialListInAdapterPositions);
            officialViewPresenter.setOnDialogItemClickListener(new ChatRoomRecyclerViewAdapter
                    .OnDialogItemClickListener() {
                @Override
                public void onItemLongClick(int relativePosition) {

                }

                @Override
                public void onItemClick(int relativePosition) {
                    clickChatRoomFlag = true;
                    XposedHelpers.callMethod(param.thisObject, "onItemClick"
                            , param.args[0], itemView, relativePosition + headerViewsCount, id);

                    if (AppSaveInfoUtils.INSTANCE.autoCloseInfo()) officialViewPresenter.dismiss();
                }
            });
            officialViewPresenter.show();
            RuntimeInfo.INSTANCE.changeCurrentPage(PageType.OFFICIAL);
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

            final Context context = itemView.getContext();

            //修改头像
            ShapeDrawable shapeDrawable = new ShapeDrawable(new Shape() {
                @Override
                public void draw(Canvas canvas, Paint paint) {
                    AvatarMaker.INSTANCE.handleAvatarDrawable(context, canvas, paint, PageType.CHAT_ROOMS);
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
            } else {
                XposedHelpers.callMethod(content, "setText", getNoMeasuredTextViewText(title)
                        + " : " + getNoMeasuredTextViewText(content));
            }

            //修改nickname
            XposedHelpers.callMethod(title, "setText", "群消息助手");
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149));

        } else if (position == firstOfficialPosition) {

            //修改头像
            ShapeDrawable shapeDrawable = new ShapeDrawable(new Shape() {
                @Override
                public void draw(Canvas canvas, Paint paint) {
                    int size = canvas.getWidth();
                    AvatarMaker.INSTANCE.handleAvatarDrawable(context, canvas, paint, PageType.OFFICIAL);
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
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个服务号有新消息]");
                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72));
            } else {
                XposedHelpers.callMethod(content, "setText", getNoMeasuredTextViewText(title)
                        + " : " + getNoMeasuredTextViewText(content));
            }

            //修改nickname
            XposedHelpers.callMethod(title, "setText", "服务号助手");
            XposedHelpers.callMethod(title, "setTextColor", Color.rgb(87, 107, 149));

        } else
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", new BitmapDrawable());


        LogUtils.INSTANCE.log("hookGetView , position = " + position +
                ", nickname = " + getNoMeasuredTextViewText(title));
    }

    private CharSequence getNoMeasuredTextViewText(Object textView) {
        Class clazz = null;
        try {
            clazz = XposedHelpers.findClass("com.tencent.mm.ui.base.NoMeasuredTextView", RuntimeInfo.mClassLoader);

            Field field = clazz.getDeclaredField("mText");
            field.setAccessible(true);
            return (CharSequence) field.get(textView);
        } catch ( IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return "";
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


        LogUtils.INSTANCE.log("hookGetObject, originIndex = " + param.args[0] + ", actuallyIndex = " + index);

        Object bean = getMessageBeanForOriginIndex(param.thisObject, index);

        param.setResult(bean);

    }

    /**
     * 根据下标返回消息列表里的消息条目，不受免打扰影响
     * 即为原数据
     */
    public static Object getMessageBeanForOriginIndex(Object adapter, int index) {
        Object bean;

        Object tMb = XposedHelpers.getObjectField(adapter, Method_Adapter_Get_Object_Step_1);

        Object hdB = XposedHelpers.getObjectField(tMb, Method_Adapter_Get_Object_Step_2);

        bean = XposedHelpers.callMethod(hdB, Method_Adapter_Get_Object_Step_3, index);

        return bean;
    }

    private void hookGetCount(XC_MethodHook.MethodHookParam param) {

        String clazzName = param.thisObject.getClass().getSimpleName();

        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
            return;//是否为正确的Adapter

        Object tMb = XposedHelpers.getObjectField(param.thisObject, Method_Adapter_Get_Object_Step_1);
        Integer result = (Integer) XposedHelpers.callMethod(tMb, "getCount");

        if (result == 0) return;

        if (notifyList) {

            int chatRoomSize = chatRoomListInAdapterPositions.size();
            int officialSize = officialListInAdapterPositions.size();

            int count = result - chatRoomSize + (chatRoomSize > 0 ? 1 : 0);//减去群的數量
            count = count - officialSize + (officialSize > 0 ? 1 : 0);//减去公众号的数量

            param.setResult(count);

            LogUtils.INSTANCE.log("hookGetCount, origin = " + result + ", chatRoom = "
                    + chatRoomSize + ", official = " + officialSize + ", return = " + count);
        } else {
            LogUtils.INSTANCE.log("hookGetCount, originSize = " + result);
            param.setResult(result);
        }
    }

    private void hookNotifyDataSetChanged(XC_MethodHook.MethodHookParam param) {

        String clazzName = param.thisObject.getClass().getSimpleName();

        //  在聊天界面直接跳过
        if (isInChatting) return;

        //是否为正确的Adapter
        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName)) return;

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

            Object tMb = XposedHelpers.getObjectField(param.thisObject, Method_Adapter_Get_Object_Step_1);
            Integer originCount = (Integer) XposedHelpers.callMethod(tMb, "getCount");

            LogUtils.INSTANCE.log("hookNotifyDataSetChanged, originCount = " + originCount);

            for (int i = 0; i < originCount; i++) {
                Object value = getMessageBeanForOriginIndex(param.thisObject, i);

                Object messageStatus = XposedHelpers.callMethod(param.thisObject,
                        Method_Message_Status_Bean, value);


                ChatInfoModel chatInfoModel = ChatInfoModel.Companion.convertFromObject(value, param.thisObject, context);

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
                    unReadCountListForChatRoom.put(i, chatInfoModel.getUnReadCount());
                }

                if (isOfficialConversation) {

                    if (firstOfficialPosition == -1) {
                        firstOfficialPosition = i;

                        if (chatRoomListInAdapterPositions.size() != 0)
                            firstOfficialPosition = firstOfficialPosition - chatRoomListInAdapterPositions.size() + 1;
                    }

                    officialListInAdapterPositions.add(i);
                    unReadCountListForOfficial.put(i, chatInfoModel.getUnReadCount());
                }


                LogUtils.INSTANCE.log("i = " + i + "/" + originCount + ", nickname = " + chatInfoModel.getNickname()
                        + ", isChatRoom = " + isChatRoomConversation + " , isOfficial = " + isOfficialConversation);


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

    public static void setAvatar(ImageView avatar, String field_username) {
        try {
            XposedHelpers.callStaticMethod(Class.forName(Class_Set_Avatar, false, RuntimeInfo.mClassLoader),
                    Constants.Method_Conversation_List_Get_Avatar, avatar, field_username);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void hookLog(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(Class_Tencent_Log, loadPackageParam.classLoader, "i",
                String.class, String.class, Object[].class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!AppSaveInfoUtils.INSTANCE.openInfo()) return;

                        String desc = String.valueOf(param.args[1]);
                        Object[] objArr = (Object[]) param.args[2];
                        try {
                            //       LogUtils.INSTANCE.log("Xposed_Log, key = " + param.args[0] + " value = " + String.format
                            //             (desc, objArr));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //无奈之举，只能使用拦截日志的做法来实现部分功能
                        //关闭聊天窗口
                        if (desc.contains("closeChatting")) {
                            isInChatting = false;
                            LogUtils.INSTANCE.log("closeChatting");
                            switch (RuntimeInfo.INSTANCE.getCurrentPage()) {
                                case PageType.CHATTING_WITH_OFFICIAL:
                                    RuntimeInfo.INSTANCE.changeCurrentPage(PageType.OFFICIAL);
                                    break;
                                case PageType.CHATTING_WITH_CHAT_ROOMS:
                                    RuntimeInfo.INSTANCE.changeCurrentPage(PageType.CHAT_ROOMS);
                                    break;
                                case PageType.CHATTING:
                                    RuntimeInfo.INSTANCE.changeCurrentPage(PageType.MAIN);
                                    break;
                            }
                        }
                        if (desc.contains("startChatting")) {
                            isInChatting = true;
                            LogUtils.INSTANCE.log("startChatting");

                            switch (RuntimeInfo.INSTANCE.getCurrentPage()) {
                                case PageType.OFFICIAL:
                                    RuntimeInfo.INSTANCE.changeCurrentPage(PageType.CHATTING_WITH_OFFICIAL);
                                    break;
                                case PageType.CHAT_ROOMS:
                                    RuntimeInfo.INSTANCE.changeCurrentPage(PageType.CHATTING_WITH_CHAT_ROOMS);
                                    break;
                                case PageType.MAIN:
                                    RuntimeInfo.INSTANCE.changeCurrentPage(PageType.CHATTING);
                                    break;
                            }
                        }

                        //收到新消息
                        if (desc.contains("summerbadcr updateConversation talker")) {

                            String sendUsername = (String) objArr[0];
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
                });
    }

}
