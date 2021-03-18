package com.tt.easyble.sample;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tt.easyble.ble.BleCharacteristic;
import com.tt.easyble.ble.BleManger;
import com.tt.easyble.ble.BleUUUID;


/**
 *
 */
public class MyApplication extends Application {

    private static MyApplication instance;


    public static String devMac = "";
    public static String devName = "";

    public static MyApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        intiLog();
        intiBle();
    }

    private void intiBle() {
        if (BleManger.INATAN.isSupperBle(this)) {
            BleManger.INATAN.init(this);
            //添加特征值
            BleCharacteristic characteristic = new BleCharacteristic();
            characteristic.setServiceUUID(BleUUUID.serviceUUUID);
            characteristic.setNotifyUUUID(BleUUUID.notifyUUUID);
            characteristic.setWriteUUUID(BleUUUID.writeUUUID);
            BleManger.INATAN.addBleCharacteristic(characteristic);
            //设置后台扫描蓝牙
            BleManger.INATAN.setScanBackstage(true);
        } else {
            Logger.d("=========手机不支持蓝牙");
        }
    }


    /**
     * 日记
     */
    private void intiLog() {
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return true;
            }
        });
        //保存到磁盘
//        Logger.addLogAdapter(new DiskLogAdapter());
    }


}
