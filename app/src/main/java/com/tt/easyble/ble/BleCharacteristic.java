package com.tt.easyble.ble;


/**
 * 蓝牙初始化用到
 */
public class BleCharacteristic {

    private String serviceUUID;
    private String notifyUUUID;
    private String writeUUUID;


    public String getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public String getNotifyUUUID() {
        return notifyUUUID;
    }

    public void setNotifyUUUID(String notifyUUUID) {
        this.notifyUUUID = notifyUUUID;
    }

    public String getWriteUUUID() {
        return writeUUUID;
    }

    public void setWriteUUUID(String writeUUUID) {
        this.writeUUUID = writeUUUID;
    }
}
