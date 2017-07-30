package com.zdy.project.wechat_chatroom_helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;
import com.zdy.project.wechat_chatroom_helper.ui.MuteConversationDialog;
import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.zdy.project.wechat_chatroom_helper.Constants.CLASS_SET_AVATAR;
import static com.zdy.project.wechat_chatroom_helper.Constants.CLASS_TENCENT_LOG;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_Adapter_OnItemClickListener_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_Parent_Name;
import static com.zdy.project.wechat_chatroom_helper.Constants.Class_Conversation_List_View_Adapter_SimpleName;
import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Chatroom_Avatar;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_1;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_2;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Adapter_Get_Object_Step_3;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_Status_Bean;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView_Adapter;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView_Adapter_ViewHolder_Avatar;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView_Adapter_ViewHolder_Content;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_ListView_Adapter_ViewHolder_Title;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Status_Is_Mute_1;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Status_Is_Mute_2;
import static com.zdy.project.wechat_chatroom_helper.Constants.WECHAT_PACKAGE_NAME;

/**
 * Created by zhudo on 2017/7/2.
 */

public class HookLogic implements IXposedHookLoadPackage {

    /**
     * com.tencent.mm.ui.e 应当是ListView的 adapter 的父类
     */

    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    //映射免打扰群组的数据位置和实际View位置
    private SparseIntArray newViewPositionWithDataPositionList = new SparseIntArray();
    private SparseIntArray unReadCountList = new SparseIntArray();

    //第一个免打扰群组的下标
    private int firstMutePosition = -1;

    //标记位，当点击Dialog内的免打扰群组时，防止onItemClick与getObject方法的position冲突
    private boolean clickChatRoomFlag = false;

    //标记位，用来标记刚刚关闭了免打扰群组
    private boolean closeMuteConversationFlag = false;

    private boolean notifyMuteList = true;

    private MuteConversationDialog muteConversationDialog;

    private static ClassLoader mClassLoader;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) return;

        mClassLoader = loadPackageParam.classLoader;

        if (!PreferencesUtils.initVariableName()) return;//判断是否获取了配置

        XposedHelpers.findAndHookMethod("android.widget.BaseAdapter", loadPackageParam.classLoader,
                "notifyDataSetChanged", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        String clazzName = param.thisObject.getClass().getSimpleName();

                        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
                            return;//是否为正确的Adapter

                        XposedBridge.log("Class_Conversation_List_View_Adapter_Parent_Name notifyDataSetChanged = ");

                        notifyMuteList = true;
                    }
                });

        /**
         * 消息列表数量
         */
        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name, loadPackageParam
                .classLoader, "getCount", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!PreferencesUtils.open()) return;//开关

                int result = (int) param.getResult();//原有会话数量


                String clazzName = param.thisObject.getClass().getSimpleName();

                if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
                    return;//是否为正确的Adapter


                XposedBridge.log("Class_Conversation_List_View_Adapter_Parent_Name getCount = " + result);

                if (result == 0) return;
                if (!notifyMuteList) return;
                notifyMuteList = false;


                XposedBridge.log("Class_Conversation_List_View_Adapter_Parent_Name_after getCount = " + result);

                newViewPositionWithDataPositionList.clear();
                muteListInAdapterPositions.clear();
                int muteCount = 0;//免打扰消息群组的的數量
                firstMutePosition = -1;


                //逐一判断是否为免打扰群组
                for (int i = 0; i < result; i++) {
                    Object value = getMessageBeanForOriginIndex(param.thisObject, i);

                    Object messageStatus = XposedHelpers.callMethod(param.thisObject, Method_Message_Status_Bean,
                            value);

                    boolean uyI = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_1);
                    boolean uXX = XposedHelpers.getBooleanField(messageStatus, Value_Message_Status_Is_Mute_2);

                    if (uyI && uXX) {
                        if (firstMutePosition == -1)
                            firstMutePosition = i;

                        muteCount++;
                        muteListInAdapterPositions.add(i);

                        MessageEntity entity = new MessageEntity(value);

                        unReadCountList.put(i, entity.field_unReadCount);
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
        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Parent_Name,
                loadPackageParam.classLoader, Method_Adapter_Get_Object, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;//开关

                        int index = (int) param.args[0];//要取的数据下标

                        XposedBridge.log("XposedBridge, ev dataPosition = " + param.args[0]);

                        String clazzName = param.thisObject.getClass().getSimpleName();

                        if (!clazzName.equals(Class_Conversation_List_View_Adapter_SimpleName))
                            return;

                        if (newViewPositionWithDataPositionList.size() != 0)
                            index = newViewPositionWithDataPositionList.get(index, index);

                        //如果剛剛點擊了Dialog中的item，則下一次getObject方法，不再修改數據和View的位置
                        if (clickChatRoomFlag) {
                            index = (int) param.args[0];//重置数据位置
                            clickChatRoomFlag = false;
                            closeMuteConversationFlag = true;
                        }

                        Object bean = getMessageBeanForOriginIndex(param.thisObject, index);

                        XposedBridge.log("XposedBridge, ev position = " + index);

                        param.setResult(bean);
                    }
                });

        XposedHelpers.findAndHookMethod(Class_Conversation_List_View_Adapter_Name, loadPackageParam
                        .classLoader, "getView",
                int.class, View.class, ViewGroup.class, new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;

                        int position = (int) param.args[0];
                        View itemView = (View) param.args[1];

                        if (itemView == null) return;
                        if (itemView.getTag() == null) return;

                        //修改群消息助手入口itemView
                        Object viewHolder = itemView.getTag();

                        //將第一個免打擾的itemView更改為群消息助手入口，更新其UI
                        if (position == firstMutePosition) {

                            Object title = XposedHelpers.getObjectField(viewHolder,
                                    Value_ListView_Adapter_ViewHolder_Title);
                            final Object avatar = XposedHelpers.getObjectField(viewHolder,
                                    Value_ListView_Adapter_ViewHolder_Avatar);
                            final Object content = XposedHelpers.getObjectField(viewHolder,
                                    Value_ListView_Adapter_ViewHolder_Content);


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

                                if (itemValue > 0)
                                    newMessageCount++;
                            }

                            if (newMessageCount > 0) {
                                XposedHelpers.callMethod(content, "setText", "[" + newMessageCount + "个群有新消息]");
                                XposedHelpers.callMethod(content, "setTextColor", Color.rgb(242, 140, 72));
                            }

                        }
                    }
                });


        XposedHelpers.findAndHookMethod(Class_Conversation_List_Adapter_OnItemClickListener_Name,
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

                        //移除頭部View的position
                        Object uWH = XposedHelpers.getObjectField(param.thisObject, Value_ListView);
                        final int headerViewsCount = (int) XposedHelpers.callMethod(uWH, "getHeaderViewsCount");

                        position = position - headerViewsCount;

                        XposedBridge.log("XposedBridge, onItemClick, getHeaderViewsCount =" + headerViewsCount);

                        XposedBridge.log("XposedBridge, onItemClick, position =" + position);

                        if (position == firstMutePosition && !clickChatRoomFlag) {

                            XposedBridge.log("XposedBridge, onItemClick, firstMutePosition");

                            Context context = view.getContext();

                            Object uXk = XposedHelpers.getObjectField(param.thisObject, Value_ListView_Adapter);
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
                    }
                });


        hookLog(loadPackageParam);
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
            XposedHelpers.callStaticMethod(Class.forName(CLASS_SET_AVATAR, false, mClassLoader),
                    "h", avatar, field_username);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
//
//
//    public static String getNickName(Object adapter, String wechat_id) {
//        Object vJp = XposedHelpers.getObjectField(adapter, "vJp");
//        Object localx = XposedHelpers.callMethod(vJp, "bWH");
//
//        Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.s.n", mClassLoader);
//        return (String) XposedHelpers.callStaticMethod(clazz, "a", localx, wechat_id, false);
//    }
//

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
        XposedHelpers.findAndHookMethod(CLASS_TENCENT_LOG,
                loadPackageParam.classLoader, "i", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) return;

                        XposedBridge.log("XposedLog, params0 = " + param.args[0] + " params1 = " + param.args[1]);

                        //无奈之举，只能使用拦截日志的做法来实现部分功能
                        Object arg = param.args[1];
                        if (arg instanceof String) {
                            //关闭聊天窗口
                            if (((String) arg).contains("closeChatting")) {
                                if (closeMuteConversationFlag && !PreferencesUtils.auto_close()) {
                                    muteConversationDialog.show();
                                    closeMuteConversationFlag = false;
                                }
                           //     notifyMuteList = true;
                            }

                            //收到新消息
                            if (((String) arg).contains("newcursor cursor update Memory key")) {
                                if (muteConversationDialog != null && muteConversationDialog.isShowing())
                                    muteConversationDialog.requestLayout();
                            }
                        }
                    }
                });


    }

}
