package com.tt.easyble.sample;

import android.app.Application;
import android.os.Handler;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tt.easyble.ble.BleCharacteristic;
import com.tt.easyble.ble.BleManger;
import com.tt.easyble.sample.box.BleUUUID;


/**
 *
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    Handler handler;

    public static String devMac = "";
    public static String devName = "";

    //单例模式中获取唯一的MyApplication实例
    public static MyApplication getInstance() {
        return instance;
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        intiLog();
        //主线程handle
        handler = new Handler(getMainLooper());

        //
        intiBle();
    }

    private void intiBle() {
        if (BleManger.INATAN.isSupperBle()) {
            BleManger.INATAN.init();
            //添加特征值
            BleCharacteristic characteristic = new BleCharacteristic();
            characteristic.setServiceUUID(BleUUUID.serviceUUUID);
            characteristic.setNotifyUUUID(BleUUUID.notifyUUUID);
            characteristic.setWriteUUUID(BleUUUID.writeUUUID);
            BleManger.INATAN.addBleCharacteristic(characteristic);
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
