package com.tt.easyble.ble;


import android.Manifest;
import android.app.Application;
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
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private Application context;
    private static BluetoothAdapter mbluetoothAdapter;
    private static BluetoothGatt mBluetoothGatt;
    //
    private static BluetoothGattCharacteristic writeCharacteristic;
    //标记mac地址，如果已经扫描到可以直连
    //如果长时间没有搜索到就做一个移除
    private static final int INTI_CACHE_COUNT = 200;
    //要一个线程安全的，
    private HashMap<String, Integer> cacheDeviceMap = new HashMap<>();
    //
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
    private BleConnectCallBackManger callBackManger;

    /**
     * applicttion 里面使用
     * 用build模式把参数设置进来，
     */
    public void init(Application application) {
        context = application;
        initBleAdapter();
        registBleListen();
        callBackManger = new BleConnectCallBackManger();
    }

    private void initBleAdapter() {
        if (mbluetoothAdapter == null) {
            mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

    /**
     * 是否支持蓝牙
     */
    public boolean isSupperBle(Context context) {
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
        callBackManger.addBleConnectCallBack(connectCallBack);
        if (cacheDeviceMap.containsKey(address)) {
            connect(address);
        } else {
            scanDevice();
        }
    }


    /**
     * 蓝牙连接，暴露在外的就只有这个连接方法
     */
    private void connect(final String address) {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        if (mbluetoothAdapter == null) {
            log("==========不支持蓝牙");
            callBackManger.connectFail(BleError.BLE_NO_SUPPER);
            return;
        }
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            log("==========mac地址错误");
            callBackManger.connectFail(BleError.BLE_MAC_ERROR);
            return;
        }
        if (address == null) {
            callBackManger.connectFail(BleError.DEV_MAC_NULL);
            return;
        }
        if (isConnect && address.equals(mac)) {
            log("=========蓝牙已连接" + address);
            callBackManger.connectSuccess();
            return;
        }
        //如果设备不一致断开当前连接
        disConnect();
        //
        device = mbluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            log("=========device == null ");
            callBackManger.connectFail(BleError.DEV_NULL);
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
            callBackManger.connectFail(BleError.GATT_NULL);
        }
    }


    /**
     * 10s后就停止扫描
     */
    private void scanDevice() {
        if (isScan) {
            log("========已经在扫描了");
            return;
        }
        handler.removeCallbacks(scanRunnable);
        handler.postDelayed(scanRunnable, 10 * 1000);
        mbluetoothAdapter.startLeScan(mCallback);
    }

    /**
     * 停止扫描计时
     */
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            mbluetoothAdapter.stopLeScan(mCallback);
            //发送未找到设备通知，，
            isScan = false;
            callBackManger.connectFail(BleError.DEV_NO_SCAN);
        }
    };

    /**
     * 停止扫描
     */
    private void stopScan() {
        log("========stopSca 停止扫描");
        handler.removeCallbacks(scanRunnable);
        mbluetoothAdapter.stopLeScan(mCallback);
        isScan = false;
    }


    /**
     * 专门用来连接用的，不参与扫描，扫描用另外一个
     */
    private BluetoothAdapter.LeScanCallback mCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() == null) {
                return;
            }
            log("====开始扫描" + device.getName() + "===" + device.getAddress());
            //这个有问题，怎么突然变true
            isScan = true;
            addDeviceToCache(device);
            //
            if (device.getAddress().equals(mac)) {
                log("====找到蓝牙设备了");
                stopScan();
                connect(mac);
            }
        }
    };


    public void stopScan(BluetoothAdapter.LeScanCallback mCallback) {
        mbluetoothAdapter.stopLeScan(mCallback);
    }

    /**
     * 断开蓝牙连接
     */
    private void disConnect() {
        startTime = 0;
        isConnect = false;
        if (mBluetoothGatt == null) {
            log("====蓝牙未连接");
            return;
        }
        writeCharacteristic = null;
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        //？？？解绑，否则有概率出现22错误，好像没用
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.setAccessible(true);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            log("======removeBond " + e.getMessage());
        }
        log("======断开连接   isScan===" + isScan);
        log("======disConnect thread" + Thread.currentThread().getName());
        log("======mac  count" + cacheDeviceMap.get(mac));
    }


    /**
     * 和上面不同的是发一个回调
     * 主动断开
     */
    public void disConnectByCode() {
        disConnect();
        callBackManger.connectFail(BleError.DIS_CONNECT);
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override  //当连接上设备或者失去连接时会回调该函数
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            log("=======onConnectionStateChange==" + status + "  " + newState);
            //返回22的话，连接成功会断开
            //连接成功，发送成功，然后突然返回22，断开，，然后重试又连接成功，会重复发两次
            //status=22且newState==0，后面还会断开，所以这个要拿出来处理
            if (status == 22 && newState == 0) {
                log("======status == 22 && newState == 0,,不处理");
                return;
            }

            //连接成功
            if (newState == BluetoothProfile.STATE_CONNECTED) {
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
                            log("=======mBluetoothGatt == null？？");
                            callBackManger.connectFail(BleError.GATT_NULL);
                        }
                    }
                }, 100);
            }
            //连接失败，，
            // 要考虑自动断开的时候不重连
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                log("======蓝牙断开onConnectionStateChange :status==" + status + " newState== " + newState);
                log("======retryCount==" + retryCount + "  needConnect  " + needConnect);
                isConnect = false;
                if (!needConnect) {
                    log("=======已经连接过不需要重连" + retryCount);
                    disConnect();
                    connectFail(BleError.CONNECT_ERROR);
                    return;
                }
                if (retryCount >= MAX_RETRY_COUNT) {
                    log("=======已重试过最大次数" + retryCount);
                    disConnect();
                    connectFail(BleError.CONNECT_ERROR);
                    return;
                }
                //连接失败重连，，
                log("=======重连 retryCount==" + retryCount);
                retryCount++;
                //断开重连
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
                log("=======mBluetoothGatt 重新连接");
                mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
            }
            //其他
            else {
                log("=======other status" + status + "newState===" + newState);
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
                if (notifyCharacteristic == null || writeCharacteristic == null) {
                    disConnect();
                    connectFail(BleError.SERVICE_NO_FIND);
                    return;
                }
                //打开通知
                enableNotification(mBluetoothGatt, notifyCharacteristic, true);
            }
            //
            else {
                log("==========搜索不到服务，onServicesDiscovered status：" + status);
                disConnect();
                connectFail(BleError.SERVICE_NO_FIND);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (gatt == null || gatt.getDevice() == null) return;
            UUID uuid = descriptor.getCharacteristic().getUuid();
            log("==========write descriptor uuid:" + uuid);
            //写值失败
            if (status != BluetoothGatt.GATT_SUCCESS) {
                log("==========写特征值失败");
                return;
            }
            //写值成功
            //打开通知成功
            if (Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    || Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                log("==========通知打开成功");
                connectSuccess();
            }
            //通知改变
            else if (Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                log("==========通知关闭");
            }
        }


        @Override //手机接收到数据回调此接口
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (gatt.getDevice().getAddress().equals(mac)) {
                final String hexString = bytes2HexStr(characteristic.getValue());
                log("==========指令返回" + hexString);
                handler.removeCallbacks(timeOutRunnable);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBackManger.handleMsg(hexString, characteristic.getValue());
                    }
                });
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //当向Characteristic写数据时会回调该函数
            log("==========发送成功" + bytes2HexStr(characteristic.getValue()));
        }
    };


    private void connectSuccess() {
        //这里要延时，，否则发送太快会失败，
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //连接成功，，交给当前的处理类处理
                isConnect = true;
                callBackManger.connectSuccess();
            }
        }, 100);
    }

    private void connectFail(String error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callBackManger.connectFail(error);
            }
        });
    }


    /**
     * 打开通知
     */
    private void enableNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (gatt == null) {
            log("==========mBluetoothGatt null");
            connectFail(BleError.GATT_NULL);
            return;
        }
        gatt.setCharacteristicNotification(characteristic, enabled);
        if (characteristic.getDescriptors().size() == 0) {
            return;
        }
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            if (descriptor != null) {
                //两个都是通知的意思，notify和indication的区别在于，notify只是将你要发的数据发送给手机，没有确认机制，
                //不会保证数据发送是否到达。而indication的方式在手机收到数据时会主动回一个ack回来。即有确认机制，只有收
                //到这个ack你才能继续发送下一个数据。这保证了数据的正确到达，也起到了流控的作用。所以在打开通知的时候，需要设置一下。
                //Write the description value
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                    descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                    descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                gatt.writeDescriptor(descriptor);
            }
        }
    }


    /***
     *直接发送，可在没链接的情况下调用
     * 1.先去连接，然后发送
     * 2.发送完成取消掉连接
     */
    public void postData(final String address, final byte[] data) {
        if (mbluetoothAdapter == null) {
            log("==========不支持蓝牙");
            callBackManger.connectFail(BleError.BLE_NO_SUPPER);
            return;
        }
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            log("==========mac地址错误");
            callBackManger.connectFail(BleError.BLE_MAC_ERROR);
            return;
        }
        if (isConnect && address.equals(mac)) {
            log("=========蓝牙已连接" + address);
            sendData(data);
            return;
        }

        connectDevice(address, new BleConnectCallBack(address) {
            @Override
            public void connectSuccess() {
                super.connectSuccess();
                sendData(data);
            }

            @Override
            public void sendSuccess() {
                super.connectSuccess();
                callBackManger.removeBleConnectCallBack(address);
            }
        });
    }


    /**
     * 连接之后再调用
     * byte数组要小于20字节
     */
    public void sendData(byte[] data) {
        if (!isConnect) {
            log("====蓝牙没连接");
            callBackManger.sendFail(BleError.DEV_NO_CONNECT);
            return;
        }
        if (mBluetoothGatt == null) {
            log("====蓝牙没连接");
            callBackManger.sendFail(BleError.GATT_NULL);
            return;
        }
        if (writeCharacteristic == null) {
            log("====写服务未发现");
            callBackManger.sendFail(BleError.SERVICE_NO_FIND);
            return;
        }
        writeCharacteristic.setValue(data);
        boolean isWriteSuccess = mBluetoothGatt.writeCharacteristic(writeCharacteristic);
        if (!isWriteSuccess) {
            log("====写数据失败");
            //提示失败
            callBackManger.sendFail(BleError.WRITR_FAIL);
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
                    callBackManger.writeTimeOut();
                }
            });
        }
    };

    public static String bytes2HexStr(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for (byte aByte : bytes) {
            b.append(String.format("%02x", aByte & 0xFF));
        }
        return b.toString();
    }
//==============================================================

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
                    log("=========蓝牙打开");
                    if (bleStateListen != null) {
                        bleStateListen.onOpen();
                    }
                    //开启
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    // 关闭中
                    break;
                case BluetoothAdapter.STATE_OFF:
                    // 关闭
                    log("=========蓝牙关闭");
                    if (bleStateListen != null) {
                        bleStateListen.onClose();
                    }
                    isBgScan = false;
                    isScan = false;
                    cacheDeviceMap.clear();
                    break;
            }
        }
    };
    /**
     * 蓝牙状态监听，打开关闭
     */
    private BleStateListen bleStateListen;

    public void setBleStateListen(BleStateListen bleStateListen) {
        this.bleStateListen = bleStateListen;
    }


    public interface BleStateListen {
        void onOpen();

        void onClose();

    }

    //==============================================================
    /**
     * 后台搜索蓝牙，可以让连接蓝牙的速度快一点，
     */
    private boolean isScanBackstage = false;
    private boolean isBgScan = false;

    //是否后台扫描
    public void setScanBackstage(boolean b) {
        isScanBackstage = b;

        //不开启后台扫描
        if (!isScanBackstage) {
            mbluetoothAdapter.stopLeScan(bgCallback);
            return;
        }
        if (mbluetoothAdapter != null) {
            mbluetoothAdapter.startLeScan(bgCallback);
        }
        //第一次进来老子还特么作延时检查，等符合条件就开始搜索蓝牙
        handler.postDelayed(checkBgRunnable, 5 * 1000);
    }


    private Runnable checkBgRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isScanBackstage) {
                log("======设置停止后台扫描");
                mbluetoothAdapter.stopLeScan(bgCallback);
                handler.postDelayed(this, 5 * 1000);
                return;
            }
            if (isBgScan) {
                log("======后台扫描已经启动，不重复启动");
            } else {
                log("======后台扫描没有启动，开始启动");
                if (mbluetoothAdapter != null) {
                    mbluetoothAdapter.stopLeScan(bgCallback);
                    mbluetoothAdapter.startLeScan(bgCallback);
                }
            }
            handler.postDelayed(this, 5 * 1000);
        }
    };

    BluetoothAdapter.LeScanCallback bgCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() == null) {
                return;
            }
//            log("======后台扫描中" + device.getName());
            isBgScan = true;
            addDeviceToCache(device);
        }
    };

    /**
     * 其他搜索加入缓存
     * 把复位，其他的
     */
    void addDeviceToCache(BluetoothDevice device) {
        cacheDeviceMap.put(device.getAddress(), INTI_CACHE_COUNT);
        for (Iterator<Map.Entry<String, Integer>> it = cacheDeviceMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> item = it.next();
            //如果是这个mac，不减少
            if (item.getKey().equals(device.getAddress())) {
                continue;
            }
            //连接中的设备也扫不到，但是不能减
            if (item.getKey().equals(mac)) {
//                log("=====mac 相等  isConnect==" + isConnect);
            }
            //连接中的设备也扫不到，但是不能减
            if (isConnect && item.getKey().equals(mac)) {
//                log("=====已经连接，不减少");
                cacheDeviceMap.put(item.getKey(), INTI_CACHE_COUNT);
                continue;
            }
            Integer count = cacheDeviceMap.get(item.getKey());
            if (count == null) {
                continue;
            }
            //不是这个mac，每次减1，等减到0就移除
            if (count > 0) {
//                log("======" + item.getKey() + " " + (count - 1));
                cacheDeviceMap.put(item.getKey(), count - 1);
            } else {
//                log("====== 移除" + item.getKey());
                it.remove();
                cacheDeviceMap.remove(item.getKey());

                //连接中的设备也扫不到，但是不能减
                if (item.getKey().equals(mac)) {
                    log("=====mac 相等  移除" + isConnect);
                }
            }
        }
    }


    /**
     * 判断是否有蓝牙权限
     */
    protected boolean checkBle() {
        //蓝牙打开
        if (!BleManger.INATAN.isOpenBle()) {
            return false;
        }
        //定位权限
        if (lacksPermission()) {
            return false;
        }
        //某些手机要开启gps才能搜索到蓝牙
        if (!GpsUtil.isOPen(context)) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否有权限，返回true就是有权限
     */
    private boolean lacksPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }

    /**
     * 回调加入或移除，
     */
    public void addBleConnectCallBack(BleConnectCallBack connectCallBack) {
        callBackManger.addBleConnectCallBack(connectCallBack);
    }

    public void removeBleConnectCallBack(BleConnectCallBack connectCallBack) {
        callBackManger.removeBleConnectCallBack(connectCallBack);
    }

    public void removeBleConnectCallBack(String connectCallBack) {
        callBackManger.removeBleConnectCallBack(connectCallBack);
    }


    //============================uuuid，可添加多个================================
    List<BleCharacteristic> bleCharacteristics = new ArrayList<>();

    public void addBleCharacteristic(BleCharacteristic characteristic) {
        bleCharacteristics.add(characteristic);
    }

    //==============================================
    private static long startTime = 0;

    void log(String log) {
        long time = System.currentTimeMillis() - startTime;
        Log.d("BleManger", " time==" + time + log);
    }

}
