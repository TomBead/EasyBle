package com.tt.easyble.ble;


import com.orhanobut.logger.Logger;

import java.util.HashMap;


/**
 * 管理数据分发，
 * 可能多个界面用同一个返回的数据，所以要多个监听都能收到
 */
class BleConnectCallBackManger {


    private HashMap<String, BleConnectCallBack> BleConnectCallBackHashMap = new HashMap<>();

    BleConnectCallBackManger() {
    }

    void addBleConnectCallBack(BleConnectCallBack connectCallBack) {
        BleConnectCallBackHashMap.put(connectCallBack.getName(), connectCallBack);
    }

    void removeBleConnectCallBack(BleConnectCallBack connectCallBack) {
        BleConnectCallBackHashMap.remove(connectCallBack.getName());
    }

    void removeBleConnectCallBack(String callbackName) {
        BleConnectCallBackHashMap.remove(callbackName);
    }

    void connectSuccess() {
        for (String key : BleConnectCallBackHashMap.keySet()) {
            Logger.d("=====connectSuccess key==" + key);
            BleConnectCallBackHashMap.get(key).connectSuccess();
        }
    }

    void connectFail(String errorMsg) {
        for (String key : BleConnectCallBackHashMap.keySet()) {
            Logger.d("=====connectFail key==" + key + " " + errorMsg);
            BleConnectCallBackHashMap.get(key).connectFail(errorMsg);
        }
    }

    void connectTimeOut() {
        for (String key : BleConnectCallBackHashMap.keySet()) {
            BleConnectCallBackHashMap.get(key).writeTimeOut();
        }
    }

    void handleMsg(String hexString, byte[] value) {
        for (String key : BleConnectCallBackHashMap.keySet()) {
            BleConnectCallBackHashMap.get(key).handleMsg(hexString, value);
        }
    }


    //发送成功
    public void sendSuccess() {
        for (String key : BleConnectCallBackHashMap.keySet()) {
            BleConnectCallBackHashMap.get(key).sendSuccess();
        }
    }

    //发送失败
    public void sendFail(String errorCode) {
        for (String key : BleConnectCallBackHashMap.keySet()) {
            BleConnectCallBackHashMap.get(key).sendFail(errorCode);
        }
    }

}
