package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.zdy.project.wechat_chatroom_helper.HookLogic;
import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_Status_Bean;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_True_Content;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_True_Time;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_Bean_NickName;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_True_Content_Params;

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

public class ChatRoomRecyclerViewAdapter extends RecyclerView.Adapter<ChatRoomViewHolder> {

    private Context mContext;

    private Object originAdapter;

    private ArrayList<Integer> muteListInAdapterPositions = new ArrayList<>();

    private OnDialogItemClickListener onDialogItemClickListener;

    private ArrayList data = new ArrayList();

    public void setData(ArrayList data) {
        this.data = data;
    }

    public void setOnDialogItemClickListener(OnDialogItemClickListener onDialogItemClickListener) {
        this.onDialogItemClickListener = onDialogItemClickListener;
    }

    public void setMuteListInAdapterPositions(ArrayList<Integer> muteListInAdapterPositions) {
        this.muteListInAdapterPositions = muteListInAdapterPositions;
    }

    public ArrayList<Integer> getMuteListInAdapterPositions() {
        return muteListInAdapterPositions;
    }

    ChatRoomRecyclerViewAdapter(Context context, Object originAdapter) {
        mContext = context;
        this.originAdapter = originAdapter;
    }

    @Override
    public ChatRoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatRoomViewHolder(ChatRoomViewHelper.getItemView(mContext));
    }

    public Object getObject(int position) {
        return data.get(position);
    }

    public ArrayList getData() {
        return data;
    }

    @Override
    public void onBindViewHolder(ChatRoomViewHolder holder, final int position) {
//        Object item = HookLogic.getMessageBeanForOriginIndex(originAdapter, muteListInAdapterPositions.get(position));

        Object item = getObject(position);
        MessageEntity entity = new MessageEntity(item);

        try {
            Object j = XposedHelpers.callMethod(originAdapter, Method_Message_Status_Bean, item);

            CharSequence content = (CharSequence) XposedHelpers.callMethod(originAdapter, Method_Message_True_Content,
                    item, ScreenUtils.dip2px(mContext, 13), XposedHelpers.getBooleanField(j, Value_Message_True_Content_Params));

            CharSequence time = (CharSequence) XposedHelpers.callMethod(originAdapter, Method_Message_True_Time, item);

            holder.nickname.setText((CharSequence) XposedHelpers.getObjectField(j,
                    Value_Message_Bean_NickName));
            holder.content.setText(content == null ? entity.field_digest : content);
            holder.time.setText(time);

            XposedBridge.log("content =" + content + ", field_digest = " + entity.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HookLogic.setAvatar(holder.avatar, entity.field_username);


        if (entity.field_unReadCount > 0)
            holder.unread.setBackground(new ShapeDrawable(new Shape() {
                @Override
                public void draw(Canvas canvas, Paint paint) {
                    int size = canvas.getWidth();

                    paint.setAntiAlias(true);
                    paint.setColor(0xFFFF0000);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawCircle(size / 2, size / 2, size / 2, paint);
                }
            }));
        else
            holder.unread.setBackground(new BitmapDrawable(mContext.getResources()));

        holder.itemView.setBackground(ChatRoomViewHelper.getItemViewBackground());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDialogItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnDialogItemClickListener {
        void onItemClick(int relativePosition);
    }
}
