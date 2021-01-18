package com.tt.easyble.ble;


/**
 * 任务bean，，用来装操作指令
 */
public class WorkAction {

    //名称
    private String workName;
    private byte[] data;


    public WorkAction(String workName, byte[] data) {
        this.workName = workName;
        this.data = data;
    }


    /**
     * 数据回调,重写这个方法
     */
    public void workCallback(String work, String data, byte[] bytes) {
    }


    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
