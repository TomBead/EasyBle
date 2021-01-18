package com.tt.easyble.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


/**
 * 搜索回调，主要是加一个东西加到缓存
 */
public abstract class EasyLeScanCallback implements BluetoothAdapter.LeScanCallback {

    /**
     * 扫描回调
     */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //加入到缓存
        BleManger.INATAN.addDeviceToCache(device);

        //
        onBleScan(device, rssi, scanRecord);
    }

    /**
     * 扫描回调
     */
    public abstract void onBleScan(BluetoothDevice device, int rssi, byte[] scanRecord);

    /**
     * 停止扫描
     */
    public abstract void stopScan();
}
