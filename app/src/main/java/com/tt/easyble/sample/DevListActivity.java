package com.tt.easyble.sample;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.orhanobut.logger.Logger;
import com.tt.easyble.R;
import com.tt.easyble.ble.BleConnectCallBack;
import com.tt.easyble.ble.BleManger;
import com.tt.easyble.ble.EasyLeScanCallback;
import com.tt.easyble.sample.view.SpacesItemDecoration;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * https://blog.csdn.net/laoguanhua/article/details/81385270
 */
public class DevListActivity extends BaseActivity {

    Handler handler = new Handler();
    @BindView(R.id.dev_list_rv)
    RecyclerView devListRv;
    @BindView(R.id.dev_list_refresh)
    SwipeRefreshLayout devListRefresh;

    BleDeviceAdapter bleDeviceAdapter;
    List<BluetoothDevice> deviceList = new ArrayList<>();


    @Override
    public void inti() {
        super.inti();
        requestPermission();
        //
        setRv();
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceList.clear();
        bleDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public int getLayoutResID() {
        return R.layout.activity_dev_list;
    }

    /**
     *
     */
    void setRv() {
        bleDeviceAdapter = new BleDeviceAdapter(this, null);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        devListRv.setLayoutManager(manager);
        devListRv.addItemDecoration(new SpacesItemDecoration(10));
        devListRv.setAdapter(bleDeviceAdapter);


        devListRv.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                showLoadingDialog("连接中...");
                String mac = deviceList.get(position).getAddress();
                MyApplication.devMac = mac;
                MyApplication.devName = deviceList.get(position).getName();
                BleManger.INATAN.connectDevice(mac, new BleConnectCallBack(mac) {
                    @Override
                    public void connectSuccess() {
                        stopLoading();
                        BleManger.INATAN.stopScan(easyLeScanCallback);
                        startActivity(new Intent(DevListActivity.this, MainActivity.class));
                    }

                    @Override
                    public void connectFail(String errorMsg) {
                        stopLoading();
                    }
                });
            }
        });


        devListRefresh.setColorSchemeResources(R.color.colorAccent);
        devListRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                deviceList.clear();
                startScan();
                devListRefresh.setRefreshing(false);
            }
        });
    }


    /**
     * 解决30s只能扫6次的问题，自动重启扫描
     */
    private boolean isBleScan = false;

    void startScan() {
        if (!isBleScan) {
            boolean result = BleManger.INATAN.getBluetoothAdapter().startLeScan(easyLeScanCallback);
            Logger.d("======开始搜索" + result);
            handler.removeCallbacks(checkScanRunnable);
            handler.postDelayed(checkScanRunnable, 2000);
        } else {
            Logger.d("======已经在搜索了");
        }
    }

    private EasyLeScanCallback easyLeScanCallback = new EasyLeScanCallback() {
        @Override
        public void onBleScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            isBleScan = true;
            if (device.getName() == null) {
                return;
            }
            if (!deviceList.contains(device)) {
                deviceList.add(device);
                bleDeviceAdapter.setNewData(deviceList);
            }
        }

        @Override
        public void stopScan() {
            Logger.d("======搜索停止？");
        }
    };


    private Runnable checkScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isBleScan) {
                boolean result = BleManger.INATAN.getBluetoothAdapter().startLeScan(easyLeScanCallback);
                Logger.d("======checkScanRunnable 再次搜索" + result);
                if (!result) {
                    BleManger.INATAN.getBluetoothAdapter().stopLeScan(easyLeScanCallback);
                    BleManger.INATAN.getBluetoothAdapter().startLeScan(easyLeScanCallback);
                }
                handler.postDelayed(this, 5000);
            } else {
                Logger.d("======checkScanRunnable 已搜索不需要搜素");
                handler.removeCallbacks(this);
            }
        }
    };


    /**
     * 定位权限是蓝牙ble扫描需要
     */
    private void requestPermission() {
        String[] permission = new String[]{Permission.ACCESS_FINE_LOCATION};

        AndPermission.with(this)
                .runtime()
                .permission(permission)
                .onGranted(permissions -> {
                    Logger.d("==========获取权限成功");
                    BleManger.INATAN.openBle();
                    startScan();
                })
                .onDenied(permissions -> {
                    Logger.d("==========获取权限失败");
                })
                .start();
        Logger.d("==========请求权限");
    }

}
