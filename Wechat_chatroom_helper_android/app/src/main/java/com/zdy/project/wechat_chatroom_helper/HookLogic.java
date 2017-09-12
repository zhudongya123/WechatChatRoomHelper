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

import java.util.ArrayList;

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

    //免打扰群组的数据位置
    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    //映射免打扰群组的数据位置和实际View位置
    private SparseIntArray newViewPositionWithDataPositionList = new SparseIntArray();

    //记录当前有多少个免打扰群有新消息
    private SparseIntArray unReadCountList = new SparseIntArray();

    //第一个免打扰群组的下标
    private int firstMutePosition = -1;

    //标记位，当点击Dialog内的免打扰群组时，防止onItemClick与getObject方法的position冲突
    private boolean clickChatRoomFlag = false;

    //标记位，数据刷新时不更新微信主界面的ListView
    private boolean notifyMuteList = true;

    //是否在聊天界面
    private boolean isInChatting = false;

    private static ClassLoader mClassLoader;
    private Context context;

    private ChatRoomViewPresenter chatRoomViewPresenter;


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
                                    fitSystemWindowLayoutView.addView(chatRoomViewPresenter.getPresenterView(), 1);
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
                            if (!isInChatting && chatRoomViewPresenter.isShowing()) {
                                chatRoomViewPresenter.dismiss();
                                param.setResult(true);
                            }
                        }
                    }
                });


        hookLog(loadPackageParam);

        //   if (!PreferencesUtils.getBugUnread()) return;

        //   fixUnread(loadPackageParam);
    }

    private void hookAdapterInit(XC_MethodHook.MethodHookParam param) {
        chatRoomViewPresenter = new ChatRoomViewPresenter(context);
        chatRoomViewPresenter.setAdapter(param.thisObject);
        chatRoomViewPresenter.start();
    }


    private void hookOnItemClick(final XC_MethodHook.MethodHookParam param) {
        if (!PreferencesUtils.open()) return;

        final View view = (View) param.args[1];
        int position = (int) param.args[2];
        final long id = (long) param.args[3];

        XposedBridge.log("XposedBridge, onItemClick, view =" + view + " ,position = " + position + " ,id = " + id);

        XposedBridge.log("XposedBridge, onItemClick, originPosition =" + position);

        //移除頭部View的position
        Object listView = XposedHelpers.getObjectField(param.thisObject, Value_ListView);
        final int headerViewsCount = (int) XposedHelpers.callMethod(listView, "getHeaderViewsCount");

        position = position - headerViewsCount;

        XposedBridge.log("XposedBridge, onItemClick, getHeaderViewsCount =" + headerViewsCount);

        XposedBridge.log("XposedBridge, onItemClick, position =" + position);


        //如果点击的是免打扰消息的入口，且不是在群消息助手里面所做的模拟点击（注意！此方法本身就为点击后的处理方法）
        if (position == firstMutePosition && !clickChatRoomFlag) {
            XposedBridge.log("XposedBridge, onItemClick, firstMutePosition");

            chatRoomViewPresenter.setMuteListInAdapterPositions(muteListInAdapterPositions);
            chatRoomViewPresenter.setOnDialogItemClickListener(new ChatRoomRecyclerViewAdapter.OnDialogItemClickListener() {
                @Override
                public void onItemClick(int relativePosition) {
                    clickChatRoomFlag = true;
                    XposedHelpers.callMethod(param.thisObject, "onItemClick"
                            , param.args[0], view, relativePosition + headerViewsCount, id);
                }
            });
            chatRoomViewPresenter.show();
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

                    handlerBitmap(canvas, paint, size, temp);
                }
            });
            XposedHelpers.callMethod(avatar, "setBackgroundDrawable", shapeDrawable);
            XposedHelpers.callMethod(avatar, "setImageDrawable", shapeDrawable);
            XposedHelpers.callMethod(avatar, "setVisibility", View.VISIBLE);

            int newMessageCount = 0;
            for (int k = 0; k < unReadCountList.size(); k++) {
                int itemValue = unReadCountList.valueAt(k);

                XposedBridge.log("Message position = " + k + ", unreadCount = " + itemValue);
                if (itemValue > 0) {
                    newMessageCount++;
                }
            }

            if (newMessageCount > 0) {
                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个群有新消息]");
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
        if (!PreferencesUtils.open()) return;//开关

        int result = (int) param.getResult();//原有会话数量

        String clazzName = param.thisObject.getClass().getSimpleName();

        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
            return;//是否为正确的Adapter

        if (result == 0) return;

        if (notifyMuteList) {
            int count = result - muteListInAdapterPositions.size();//减去免打扰消息的數量
            count++;//增加入口位置
            param.setResult(count);
        }
    }

    private void hookNotifyDataSetChanged(XC_MethodHook.MethodHookParam param) {
        String clazzName = param.thisObject.getClass().getSimpleName();

        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
            return;//是否为正确的Adapter


        notifyMuteList = false;

        //代码保护区，此段执行时getCount逻辑跳过
        {
            newViewPositionWithDataPositionList.clear();
            muteListInAdapterPositions.clear();
            unReadCountList.clear();
            //  muteCount = 0;//免打扰消息群组的的數量
            firstMutePosition = -1;


            //逐一判断是否为免打扰群组
            for (int i = 0; i < ((BaseAdapter) param.thisObject).getCount(); i++) {
                Object value = getMessageBeanForOriginIndex(param.thisObject, i);

                Object messageStatus = XposedHelpers.callMethod(param.thisObject,
                        Method_Message_Status_Bean,
                        value);

                boolean uyI = XposedHelpers.getBooleanField(messageStatus,
                        Value_Message_Status_Is_Mute_1);
                boolean uXX = XposedHelpers.getBooleanField(messageStatus,
                        Value_Message_Status_Is_Mute_2);


                if (uyI && uXX) {
                    if (firstMutePosition == -1)
                        firstMutePosition = i;

                    //    muteCount++;
                    muteListInAdapterPositions.add(i);

                    MessageEntity entity = new MessageEntity(value);

                    unReadCountList.put(i, entity.field_unReadCount);
                }
                int muteCount = muteListInAdapterPositions.size();
                if (!(uyI && uXX) || muteCount == 1) {
                    newViewPositionWithDataPositionList.put(i - (muteCount >= 1 ? muteCount - 1 :
                            muteCount), i);
                }

            }
        }
        notifyMuteList = true;

        if (chatRoomViewPresenter != null) {
            chatRoomViewPresenter.setMuteListInAdapterPositions(muteListInAdapterPositions);
        }
    }

    //自造群消息助手头像
    private static void handlerBitmap(Canvas canvas, Paint paint, int size, Bitmap drawable) {
        Bitmap whiteMask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        whiteMask.eraseColor(Color.WHITE);

        drawable = Bitmap.createScaledBitmap(drawable, size / 2, size / 2, false).copy(Bitmap.Config.ARGB_8888, false);

        Bitmap raw = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas bitmapCanvas = new Canvas(raw);

        bitmapCanvas.drawBitmap(drawable, size / 4, size / 4, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        bitmapCanvas.drawBitmap(whiteMask, 0, 0, paint);

        paint.setXfermode(null);

        canvas.drawColor(0xFF12B7F6);

        canvas.drawBitmap(raw, 0, 0, paint);

    }

    public static void setAvatar(ImageView avatar, String field_username) {
        try {
            String h = "h";
            if (PreferencesUtils.getVersionCode() == 1100) h = "a";
            XposedHelpers.callStaticMethod(Class.forName(Class_Set_Avatar, false, mClassLoader),
                    h, avatar, field_username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据下标返回消息列表里的消息条目，不受免打扰影响
     * 即为原数据
     */
    public static Object getMessageBeanForOriginIndex(Object adapter, int index) {
        try {
            Object tMb = XposedHelpers.getObjectField(adapter, Method_Adapter_Get_Object_Step_1);

            Object hdB = XposedHelpers.getObjectField(tMb, Method_Adapter_Get_Object_Step_2);

            Object bean = XposedHelpers.callMethod(hdB, Method_Adapter_Get_Object_Step_3, index);

            return bean;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

                                for (Object o : arg)
                                    params = params + o.toString() + " ";
                            }
                        } catch (Exception e) {
                        }
                        XposedBridge.log("XposedLogi, desc = " + desc + ", value = " + value + ", params = " + params);

                        //无奈之举，只能使用拦截日志的做法来实现部分功能
                        Object arg = param.args[1];
                        if (arg instanceof String) {
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
                                    if (chatRoomViewPresenter != null) {
                                        chatRoomViewPresenter.setMessageRefresh(sendUsername);
                                    }
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

                        if (chatRoomViewPresenter == null) return;
                        Object adapter = chatRoomViewPresenter.getOriginAdapter();
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
