package com.tt.easyble.sample;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Ken on 2017/8/9.
 */

public class ToastUtil {
    public static Toast mToast;
    public static Context context;

    public static void showToast(Context context, String text) {
        if (context != null) {
            mToast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
            mToast.setText(text);
            mToast.show();
        }
    }

    public static void showToastLong(Context context, String text) {
        if (context != null) {
            mToast = Toast.makeText(context, null, Toast.LENGTH_LONG);
            mToast.setText(text);
            mToast.show();
        }
    }


    public static void showToastNew(Context context, String text) {
        if (context != null) {
            mToast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
            mToast.setText(text);
            mToast.show();
        }
    }


    public static void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}
