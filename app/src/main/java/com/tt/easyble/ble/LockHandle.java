package com.tt.easyble.ble;


/**
 * 多个连接对象
 */
public interface LockHandle {


    String getName();

    //连接成功
    void connectSuccess();

    //连接失败
    void connectFail();

    //数据发送超时等
    void writeTimeOut();

    //处理接收的消息
    void handleMsg(String hexString, byte[] bytes);

}
