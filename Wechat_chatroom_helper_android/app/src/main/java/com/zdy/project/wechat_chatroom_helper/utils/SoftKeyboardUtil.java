package com.zdy.project.wechat_chatroom_helper.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SoftKeyboardUtil {
    public static void observeSoftKeyboard(final Activity activity, final OnSoftKeyboardChangeListener listener) {

        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int previousKeyboardHeight = -1;
            int priorHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHeight = rect.bottom - rect.top;
                int height = decorView.getHeight();
                int keyboardHeight = height - displayHeight;
                if (priorHeight == keyboardHeight) {
                    return;
                }
                priorHeight = keyboardHeight;
                if (previousKeyboardHeight != keyboardHeight) {
                    boolean hide = (double) displayHeight / height > 0.8;
                    keyboardHeight = keyboardHeight - ScreenUtils.getStatusHeight(activity);
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
                        keyboardHeight = keyboardHeight - ScreenUtils.getBottomStatusHeight(activity);
                    listener.onSoftKeyBoardChange(keyboardHeight, !hide);
                }
                previousKeyboardHeight = height;
            }
        });
    }

    public interface OnSoftKeyboardChangeListener {
        void onSoftKeyBoardChange(int softKeyboardHeight, boolean visible);
    }

    public static void openKeyboard(EditText mEditText, Context mContext) {
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mEditText, InputMethodManager.RESULT_UNCHANGED_SHOWN);

    }

    public static void closeKeyboard(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    public static boolean getKeyboardStatus(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isAcceptingText();
    }

    public static void switchKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.RESULT_HIDDEN);
    }

    public static void closeKeyboardForce(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}