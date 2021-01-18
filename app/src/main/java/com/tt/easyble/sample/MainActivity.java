package com.tt.easyble.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.tt.easyble.R;
import com.tt.easyble.ble.BleManger;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * https://blog.csdn.net/laoguanhua/article/details/81385270
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_clean_log)
    Button mainCleanLog;
    @BindView(R.id.main_log_tv)
    TextView mainLogTv;
    @BindView(R.id.main_connect)
    Button mainConnect;
    @BindView(R.id.main_scan)
    Button mainScan;

    StringBuilder builder = new StringBuilder();
//    String mac = "01:02:03:04:05:07";

    String mac = "D7:69:E4:87:F4:FD";
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        requestPermission();
    }


    //点击监听
    @OnClick({R.id.main_connect, R.id.main_scan, R.id.main_clean_log})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.main_clean_log:
                cleanLog();
                break;
            case R.id.main_connect:
                connectMac();
                break;
            case R.id.main_scan:
                connectAndScan();
                break;
            default:
                break;
        }
    }


    void connectMac() {
        BleManger.INATAN.disConnect();
        mainConnect.setEnabled(false);
        mainScan.setEnabled(false);
        BleManger.INATAN.connect(mac);
        handler.removeCallbacks(connectTimeOutRunnable);
        handler.postDelayed(connectTimeOutRunnable, 10 * 1000);
    }

    void connectAndScan() {
        BleManger.INATAN.disConnect();
        mainConnect.setEnabled(false);
        mainScan.setEnabled(false);
        handler.removeCallbacks(connectTimeOutRunnable);
        handler.postDelayed(connectTimeOutRunnable, 10 * 1000);
    }

    Runnable connectTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            mainConnect.setEnabled(true);
            mainScan.setEnabled(true);
        }
    };

    void resetButton() {
        handler.removeCallbacks(connectTimeOutRunnable);
        mainConnect.setEnabled(true);
        mainScan.setEnabled(true);
    }


    public void addLog(String log) {
        mainLogTv.post(new Runnable() {
            @Override
            public void run() {
                builder.append(log).append("\n");
                mainLogTv.setText(builder.toString());
            }
        });
    }

    void cleanLog() {
        builder.setLength(0);
        mainLogTv.setText("");
    }

    /**
     * 定位权限是蓝牙ble扫描需要
     * Android10 以上不给申请读写权限了
     * 低版本的还是需要
     */
    private void requestPermission() {
        String[] permission = new String[]{Permission.ACCESS_FINE_LOCATION};

        AndPermission.with(this)
                .runtime()
                .permission(permission)
                .onGranted(permissions -> {
                    BleManger.INATAN.openBle();
                })
                .onDenied(permissions -> {

                })
                .start();
        Logger.d("==========请求权限");
    }
}
