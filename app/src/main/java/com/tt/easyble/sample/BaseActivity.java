package com.tt.easyble.sample;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.tt.easyble.ble.BleManger;
import com.tt.easyble.ble.GpsUtil;
import com.yanzhenjie.permission.runtime.Permission;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 基础activity
 */
public abstract class BaseActivity extends AppCompatActivity {

    private Unbinder mUnbinder;
    //
    private AlertDialog.Builder mBuilder;
    //
    private ProgressDialog progressDialog;
    //是不是在前台
    boolean isForeground = false;

    protected BaseActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());
        mUnbinder = ButterKnife.bind(this);
        inti();
    }


    public void inti() {
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeground = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * 判断是否有权限，返回true就是没有权限
     */
    protected boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED;
    }

    protected boolean checkBle() {
        if (!BleManger.INATAN.isOpenBle()) {
            showTipsDialog("蓝牙已关闭，请先打开蓝牙");
            return false;
        }
        /**
         * Android 10以上要加这个判断，，
         */
        if (lacksPermission(Permission.ACCESS_FINE_LOCATION)) {
            showTipsDialog("app没有定位权限，无法使用蓝牙ble功能");
            return false;
        }
        /**
         * 没有定位权限，，
         */
        if (lacksPermission(Permission.ACCESS_COARSE_LOCATION)) {
            showTipsDialog("app没有定位权限，无法使用蓝牙ble功能");
            return false;
        }

        //某些手机要开启gps才能搜索到蓝牙
        if (!GpsUtil.isOPen(this)) {
            GpsUtil.showGpsDialog(this);
            return false;
        }
        return true;
    }


    /**
     * 强制隐藏键盘
     */
    public void hideSoftInput(EditText view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /**
     * 显示提示dialog
     */
    protected void showTipsDialog(String text) {
        if (mBuilder == null) {
            mBuilder = new AlertDialog.Builder(this);
            mBuilder.setCancelable(true);
            mBuilder.setTitle("提示");
            mBuilder.setMessage(text);
            mBuilder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        mBuilder.setMessage(text);
        mBuilder.create().show();
    }

    protected void showTipsDialog(String text, DialogInterface.OnClickListener onClickListener) {
        if (mBuilder == null) {
            mBuilder = new AlertDialog.Builder(this);
            mBuilder.setCancelable(true);
            mBuilder.setTitle("提示");
            mBuilder.setMessage(text);
            mBuilder.setPositiveButton("好的", onClickListener);
        }
        mBuilder.setCancelable(false);
        mBuilder.setMessage(text);
        mBuilder.create().show();
    }


    /**
     * 普通加载弹窗
     */
    protected void showLoadingDialog(String text) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage(text);
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    protected void stopLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void showToast(String message) {
        ToastUtil.showToast(this, message);
    }

    /**
     * 打开activity
     */
    public void openActivity(Class<?> cls) {
        stopLoading();
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    public void openActivity(Intent intent) {
        stopLoading();
        startActivity(intent);
    }

    public void finishWithAnim() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public abstract int getLayoutResID();


}
