package com.tt.easyble.ble;


/**
 * 连接蓝牙不成功返回的各种错误，
 */
public class BleError {
    //设备没有搜索到
    public static final String BLE_NO_SUPPER = "BLE_NO_SUPPER";
    //mac地址有错误
    public static final String BLE_MAC_ERROR = "BLE_MAC_ERROR";

    //mac地址有错误
    public static final String DEV_MAC_NULL = "DEV_MAC_NULL";

    //设备没有搜索到
    public static final String DEV_NO_SCAN = "DEV_NO_SCAN";

    //设备没有连接上
    public static final String DEV_NO_CONNECT = "DEV_NO_CONNECT";

    //连接超时
    public static final String DEV_CONNECT_TIMEOUT = "DEV_CONNECT_TIMEOUT";

    //
    public static final String DEV_NULL = "DEV_NULL";

    //
    public static final String GATT_NULL = "GATT_NULL";

    //服务没搜索到
    public static final String SERVICE_NO_FIND = "SERVICE_NO_FIND";

    //主动断开
    public static final String DIS_CONNECT = "DIS_CONNECT";
    //主动断开
    public static final String CONNECT_ERROR = "CONNECT_ERROR";
    //
    public static final String WRITR_FAIL = "WRITR_FAIL";

}
