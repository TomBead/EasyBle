package com.tt.easyble.ble;


/**
 * 蓝牙初始化用到
 */
public class BleCharacteristic {

    private String serviceUUID;
    private String notifyUUUID;
    private String writeUUUID;
    private String descriptor;


    String getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    String getNotifyUUUID() {
        return notifyUUUID;
    }

    public void setNotifyUUUID(String notifyUUUID) {
        this.notifyUUUID = notifyUUUID;
    }

    String getWriteUUUID() {
        return writeUUUID;
    }

    public void setWriteUUUID(String writeUUUID) {
        this.writeUUUID = writeUUUID;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
}
