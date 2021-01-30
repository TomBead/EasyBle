package com.tt.easyble.ble.work;


import com.orhanobut.logger.Logger;
import com.tt.easyble.ble.BleConnectCallBack;
import com.tt.easyble.ble.BleManger;

import java.util.HashMap;

/**
 * 处理回调和接受不在一个地方的问题，
 *
 *
 */
public enum SendManger {
    INATAN;
    //
    private static HashMap<String, MsgCallBack> msgCallBackHashMap = new HashMap<>();
    //列表，按顺序执行
    private static String currWork = "";
    //
    private static byte[] tempBytes;
    //
    private static String mAddress = "";

    public SendManger inti(String address) {
        mAddress = address;
        currWork = "";
        tempBytes = null;
        msgCallBackHashMap.clear();
        //
        BleManger.INATAN.addBleConnectCallBack(bleConnectCallBack);
        return this;
    }


    /**
     * 加入列表但是不发送
     */
    public SendManger listMsg(String work, MsgCallBack msgCallBack) {
        msgCallBackHashMap.put(work, msgCallBack);
        return this;
    }

    /**
     *
     */
    public void sendListData(String work, byte[] bytes) {
        currWork = work;
        BleManger.INATAN.sendData(bytes);
    }

    /**
     * 开始发送
     */
    public void startSend(String work, byte[] bytes) {
        currWork = work;
        tempBytes = bytes;
        if (BleManger.INATAN.isConnect()) {
            BleManger.INATAN.sendData(bytes);
        } else {
            BleManger.INATAN.connectDevice(mAddress, bleConnectCallBack);
        }
    }

    BleConnectCallBack bleConnectCallBack = new BleConnectCallBack("SendManger") {
        @Override
        public void connectSuccess() {
            super.connectSuccess();
            BleManger.INATAN.sendData(tempBytes);
        }

        @Override
        public void connectFail(String errorMsg) {
            super.connectFail(errorMsg);
        }

        @Override
        public void writeTimeOut() {
            super.writeTimeOut();
        }

        @Override
        public void sendSuccess() {
            super.sendSuccess();
        }

        @Override
        public void sendFail(String errorCode) {
            super.sendFail(errorCode);
        }

        @Override
        public void handleMsg(String hexString, byte[] bytes) {
            super.handleMsg(hexString, bytes);
            accept(bytes, hexString);
        }
    };


    /**
     * 把数据接收的管子套上
     */
    private void accept(byte[] data, String hexString) {
        if (msgCallBackHashMap.containsKey(currWork)) {
            MsgCallBack msgCallBack = msgCallBackHashMap.get(currWork);
            if (msgCallBack == null) {
                Logger.d("==========currWork " + currWork + "callback null");
                return;
            }
            msgCallBack.callback(currWork, data, hexString);
        } else {
            Logger.d("==========currWork " + currWork + "no key");
        }
    }


    public interface MsgCallBack {
        void callback(String work, byte[] data, String hexString);
    }

}
