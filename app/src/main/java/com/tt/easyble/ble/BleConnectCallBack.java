package com.tt.easyble.ble;


/**
 * ble连接回调
 */
public abstract class BleConnectCallBack {

    private String name;

    public BleConnectCallBack(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    //====================================
    //连接成功
    public void connectSuccess() {
    }

    //连接失败
    public void connectFail(String errorMsg) {
    }

    //数据发送超时等
    public void writeTimeOut() {
    }

    //发送成功
    public void sendSuccess() {
    }

    //发送失败
    public void sendFail(String errorCode) {
    }

    //处理接收的消息
    public void handleMsg(String hexString, byte[] bytes) {
    }

}
