package com.tt.easyble.sample;

import android.app.Application;
import android.os.Handler;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tt.easyble.ble.BleManger;


/**
 *
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    Handler handler;

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
