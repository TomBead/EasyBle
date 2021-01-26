package com.tt.easyble.ble;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.orhanobut.logger.Logger;
import com.tt.easyble.sample.MyApplication;
import com.tt.easyble.sample.box.BleUUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author 78tao
 * <p>
 * 管理蓝牙的单例类
 * 1.打开关闭蓝牙
 * 2.连接蓝牙
 * 3.收发数据
 * <p>
 * //
 * 针对单个设备
 * 多个不同管道设备可以切换，
 */
public enum BleManger {

    INATAN;

    private Context context = MyApplication.getInstance();
    private static BluetoothAdapter mbluetoothAdapter;
    private static BluetoothGatt mBluetoothGatt;

    //
    private static BluetoothGattCharacteristic writeCharacteristic;

    //这个是用来标记看是不是已经存在了
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private BluetoothDevice device = null;
    private boolean isConnect = false;
    private String mac;
    //
    private boolean isScan = false;
    //是否需要重连，
    private static boolean needConnect = true;
    //重试次数
    public static int retryCount = 0;
    public static final int MAX_RETRY_COUNT = 3;
    //写超时，
    public static final int TIME_OUT = 5 * 1000;

    private Handler handler = new Handler(Looper.getMainLooper());

    //状态回调
    private BleConnectCallBackManger lockHandleManger;

    /**
     * applicttion 里面使用
     * 用build模式把参数设置进来，
     */
    public void init() {
        initBleAdapter();
        registBleListen();
        lockHandleManger = new BleConnectCallBackManger();
    }

    private void initBleAdapter() {
        if (mbluetoothAdapter == null) {
            mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

    /**
     * 是否支持蓝牙
     */
    public boolean isSupperBle() {
        return (getBluetoothAdapter() != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
    }

    public BluetoothAdapter getBluetoothAdapter() {
        if (mbluetoothAdapter == null) {
            mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mbluetoothAdapter;
    }

    /**
     * 打开蓝牙
     *
     * @return 是否打开蓝牙
     */
    public boolean openBle() {
        boolean openResult = mbluetoothAdapter.isEnabled();
        if (!openResult) {
            mbluetoothAdapter.enable();
        }
        return openResult;
    }

    /**
     * TODO 打开蓝牙不显示提示，
     */
    public boolean isOpenBle() {
        return mbluetoothAdapter.isEnabled();
    }

    /**
     * 是否有蓝牙连接，
     */
    public boolean isConnect() {
        return isConnect;
    }


    /**
     * 看看缓存里面有没有，，没有就要去扫描
     */
    public void connectDevice(final String address, BleConnectCallBack connectCallBack) {
        startTime = System.currentTimeMillis();
        mac = address;
        lockHandleManger.addBleConnectCallBack(connectCallBack);
        if (isDevExit(address)) {
            connect(mac);
        } else {
            scanDevice();
        }
    }

    /**
     *
     */
    public void addBleConnectCallBack(BleConnectCallBack connectCallBack) {
        lockHandleManger.addBleConnectCallBack(connectCallBack);
    }

    public void removeBleConnectCallBack(BleConnectCallBack connectCallBack) {
        lockHandleManger.removeBleConnectCallBack(connectCallBack);
    }

    /**
     * 其他搜索加入缓存
     */
    void addDeviceToCache(BluetoothDevice device) {
        if (!bluetoothDeviceList.contains(device)) {
            bluetoothDeviceList.add(device);
        }
    }

    private boolean isDevExit(String mac) {
        boolean isExit = false;
        for (int i = 0; i < bluetoothDeviceList.size(); i++) {
            if (bluetoothDeviceList.get(i).getAddress().equals(mac)) {
                isExit = true;
                break;
            }
        }
        return isExit;
    }

    /**
     * 蓝牙连接，暴露在外的就只有这个连接方法
     */
    public void connect(final String address) {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        if (mbluetoothAdapter == null || address == null) {
            log("==========不支持蓝牙");
            lockHandleManger.connectFail(BleError.BLE_NO_SUPPER);
            return;
        }

        if (isConnect && address.equals(mac)) {
            log("=========蓝牙已连接" + address);
            lockHandleManger.connectSuccess();
            return;
        }
        //如果设备不一致断开当前连接
        disConnect();
        //
        device = mbluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            log("=========device == null ");
            return;
        }
        mac = address;
        log("=========蓝牙开始连接" + address);
        retryCount = 0;
        needConnect = true;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            mBluetoothGatt = device.connectGatt(context, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        }
        //
        if (mBluetoothGatt == null) {
            log("=========mBluetoothGatt 为空" + address);
            lockHandleManger.connectFail(BleError.GATT_NULL);
        }
    }


    /**
     * 10s后就停止扫描
     */
    public void scanDevice() {
        if (isScan) {
            Logger.d("========已经在扫描了");
            return;
        }
        handler.removeCallbacks(scanRunnable);
        handler.postDelayed(scanRunnable, 10 * 1000);
        mbluetoothAdapter.startLeScan(mCallback);
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        handler.removeCallbacks(scanRunnable);
        mbluetoothAdapter.stopLeScan(mCallback);
        isScan = false;
    }

    public void stopScan(BluetoothAdapter.LeScanCallback mCallback) {
        mbluetoothAdapter.stopLeScan(mCallback);
    }

    /**
     * 停止扫描计时
     */
    Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            mbluetoothAdapter.stopLeScan(mCallback);
            //发送未找到设备通知，，
            isScan = false;
            lockHandleManger.connectFail(BleError.DEV_NO_SCAN);
        }
    };


    /**
     * 专门用来连接用的，不参与扫描，扫描用另外一个
     */
    public BluetoothAdapter.LeScanCallback mCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            log("====开始扫描" + device.getName() + "===" + device.getAddress());
            isScan = true;
            if (!bluetoothDeviceList.contains(device)) {
                bluetoothDeviceList.add(device);
            }
            //
            if (device.getAddress().equals(mac)) {
                log("====找到蓝牙设备了");
                stopScan();
                connect(mac);
            }
        }
    };


    /**
     *
     */
    private EasyLeScanCallback easyLeScanCallback;

    public boolean startScan(long time, EasyLeScanCallback easyLeScanCallback) {
        this.easyLeScanCallback = easyLeScanCallback;
        //
        if (isScan) {
            Logger.d("========已经在扫描了");
            return true;
        }
        handler.removeCallbacks(easyScanRunnable);
        handler.postDelayed(easyScanRunnable, time);
        return mbluetoothAdapter.startLeScan(easyLeScanCallback);
    }

    Runnable easyScanRunnable = new Runnable() {
        @Override
        public void run() {
            mbluetoothAdapter.stopLeScan(easyLeScanCallback);
            //发送未找到设备通知，，
            isScan = false;
            easyLeScanCallback.stopScan();
        }
    };

    /**
     * 断开蓝牙连接
     */
    public void disConnect() {
        startTime = 0;
        //
        isConnect = false;
        if (mBluetoothGatt == null) {
            log("====蓝牙未连接");
            return;
        }
        writeCharacteristic = null;
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        //
        lockHandleManger.connectFail(BleError.DIS_CONNECT);
        log("====断开连接");
        Logger.d("======disConnect thread" + Thread.currentThread().getName());
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override  //当连接上设备或者失去连接时会回调该函数
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            log("=======onConnectionStateChange==" + status + "  " + newState);
            //连接成功
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (isConnect) {
                    Logger.d("=======已经连接" + mac);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            lockHandleManger.connectSuccess();
                        }
                    });
                    return;
                }
                retryCount = 0;
                needConnect = false;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.discoverServices();
                        }
                        //在还没有连接成功就断开，可能会出现这种情况
                        else {
                            Logger.d("=======mBluetoothGatt == null？？");
                            lockHandleManger.connectFail(BleError.GATT_NULL);
                        }
                    }
                }, 100);
            }
            //连接失败，，
            // 要考虑自动断开的时候不重连
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Logger.d("======蓝牙断开onConnectionStateChange :status==" + status + " newState== " + newState);
                isConnect = false;
                //连接失败重连，，
                if (needConnect && (retryCount < MAX_RETRY_COUNT)) {
                    Logger.d("=======重连 retryCount==" + retryCount);
                    retryCount++;
                    //断开重连
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.disconnect();
                        mBluetoothGatt.close();
                    }
                    Logger.d("=======mBluetoothGatt 重新连接");
                    mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
                } else {
                    Logger.d("=======连接断开,不重连");
                    disConnect();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Logger.d("=======连接断开,lockHandleManger");
                            Logger.d("===== Thread ==" + Thread.currentThread().getName());
                            lockHandleManger.connectFail(BleError.DIS_CONNECT);
                        }
                    });
                }
            }
            //其他
            else {
                Logger.d("=======other status" + status + "newState===" + newState);
            }
        }

        @Override  //当设备是否找到服务时，会回调该函数
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic notifyCharacteristic = null;
                List<BluetoothGattService> services = mBluetoothGatt.getServices();
                //遍历所有服务，，
                for (BluetoothGattService bluetoothGattService : services) {
                    for (BleCharacteristic mCharacteristic : bleCharacteristics) {
                        if (bluetoothGattService.getUuid().toString().equals(mCharacteristic.getServiceUUID())) {
                            //找通知管道
                            notifyCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(mCharacteristic.getNotifyUUUID()));
                            //找写管道
                            writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(mCharacteristic.getWriteUUUID()));
                        }
                    }
                }

                if (notifyCharacteristic == null) {
                    disConnect();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            lockHandleManger.connectFail(BleError.SERVICE_NO_FIND);
                        }
                    });
                }
                if (writeCharacteristic == null) {
                    disConnect();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            lockHandleManger.connectFail(BleError.SERVICE_NO_FIND);
                        }
                    });
                }

                //打开通知，，
                boolean isenable = enableNotification(notifyCharacteristic, true);
                if (isenable) {
                    log("=====打开通知成功");
                    //这里要延时，，否则发送太快会失败，
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //连接成功，，交给当前的处理类处理
                            isConnect = true;
                            lockHandleManger.connectSuccess();
                        }
                    }, 10);
                }
            } else {
                log("==========搜索不到服务，onServicesDiscovered status：" + status);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lockHandleManger.connectFail(BleError.SERVICE_NO_FIND);
                    }
                });
            }
        }

        @Override //设备发出通知时会调用到该接口
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            if (gatt.getDevice().getAddress().equals(mac)) {
                String hexString = bytes2HexStr(characteristic.getValue());
                log("==========指令返回" + hexString);
                handler.removeCallbacks(timeOutRunnable);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lockHandleManger.handleMsg(hexString, characteristic.getValue());
                    }
                });
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            //当向Characteristic写数据时会回调该函数
            log("==========发送成功" + bytes2HexStr(characteristic.getValue()));
        }
    };

    /**
     * 打开通知
     */
    private boolean enableNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (mBluetoothGatt == null || characteristic == null) {
            return false;
        }
        if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
            return false;
        }
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(BleUUUID.descriptor));
        if (clientConfig == null) {
            return false;
        }
        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBluetoothGatt.writeDescriptor(clientConfig);
    }


    /**
     * byte数组要小于20字节
     */
    public void sendData(byte[] data) {
        if (!isConnect) {
            log("====蓝牙没连接");
            lockHandleManger.sendFail(BleError.DEV_NO_CONNECT);
            return;
        }
        if (mBluetoothGatt == null) {
            log("====蓝牙没连接");
            lockHandleManger.sendFail(BleError.GATT_NULL);
            return;
        }
        if (writeCharacteristic == null) {
            log("====写服务未发现");
            lockHandleManger.sendFail(BleError.SERVICE_NO_FIND);
            return;
        }
        writeCharacteristic.setValue(data);
        boolean isWriteSuccess = mBluetoothGatt.writeCharacteristic(writeCharacteristic);
        if (!isWriteSuccess) {
            Logger.d("======写数据失败");
            //提示失败
            lockHandleManger.sendFail(BleError.WRITR_FAIL);
        }
        //写成功，开始回复倒计时开始
        else {
            //如果发送到设备成功了没有回音，设置超时
            handler.removeCallbacks(timeOutRunnable);
            handler.postDelayed(timeOutRunnable, TIME_OUT);
        }
    }


    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    lockHandleManger.connectTimeOut();
                }
            });
        }
    };

    public static String bytes2HexStr(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            b.append(String.format("%02x", bytes[i] & 0xFF));
        }
        return b.toString();
    }

    /**
     * 蓝牙开关监听
     */
    private void registBleListen() {
        IntentFilter statusFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mStatusReceive, statusFilter);
    }

    private BroadcastReceiver mStatusReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            switch (blueState) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    //开启中
                    break;
                case BluetoothAdapter.STATE_ON:
                    Logger.d("=========蓝牙打开");
                    //开启
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    // 关闭中
                    break;
                case BluetoothAdapter.STATE_OFF:
                    // 关闭
                    Logger.d("=========蓝牙关闭");
                    isScan = false;
                    bluetoothDeviceList.clear();
                    break;
            }
        }
    };

    //
    static long startTime = 0;

    void log(String log) {
        long time = System.currentTimeMillis() - startTime;
        Logger.d(log + " time==" + time);
    }


    //============================================================
    List<BleCharacteristic> bleCharacteristics = new ArrayList<>();

    public void addBleCharacteristic(BleCharacteristic characteristic) {
        bleCharacteristics.add(characteristic);
    }
}
