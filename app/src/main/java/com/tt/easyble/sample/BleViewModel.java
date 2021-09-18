package com.tt.easyble.sample;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.tt.easyble.ble.BleConnectCallBack;
import com.tt.easyble.ble.BleManger;
import com.tt.easyble.ble.HexUtils;

import java.util.HashMap;


/**
 * 一个通用的法则是，你的 ViewModel 中没有导入像 android.*这样的包（像 android.arch.* 这样的除外)。
 * 这个经验也同样适用于 MVP 模式中的 Presenter 。
 * <p>
 * ❌ 避免在 ViewModel 里持有视图层的引用
 * =============================================
 */
public class BleViewModel extends ViewModel {
    public static final String TAG = "BleViewModel";
    //连接状态
    private MutableLiveData<String> connectLiveData;
    //加载
    private MutableLiveData<Boolean> loadingLiveData;
    //
    private MutableLiveData<String> powerLiveData;

    public BleViewModel() {
        connectLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
        powerLiveData = new MutableLiveData<>();

        //
        //数据返回，，逻辑
        BleConnectCallBack bleConnectCallBack = new BleConnectCallBack(TAG) {
            @Override
            public void connectSuccess() {
                loadingLiveData.setValue(false);
            }

            @Override
            public void connectFail(String errorMsg) {
                connectLiveData.setValue(errorMsg);
            }

            @Override
            public void sendFail(String errorCode) {
                connectLiveData.setValue(errorCode);
            }

            @Override
            public void writeTimeOut() {
                connectLiveData.setValue("writeTimeOut");
            }

            @Override
            public void handleMsg(String hexString, byte[] bytes) {
                //数据返回，，逻辑
                log("=====handleMsg" + hexString);
                handleGateWay(hexString, bytes);
            }
        };
        BleManger.INATAN.addBleConnectCallBack(bleConnectCallBack);
    }


    /**
     * 退出东西哦
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        log("=====onCleared");
        connectLiveData = null;
        loadingLiveData = null;
        powerLiveData = null;
        //UserRepository清除callback
        BleManger.INATAN.removeBleConnectCallBack(TAG);
    }

    private void log(String msg) {
        Log.d(TAG, "=========" + msg);
    }

    //======================get-set====================================
    public LiveData<String> getConnectLiveData() {
        return connectLiveData;
    }

    public LiveData<String> getPowerLiveData() {
        return powerLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }
    //====================================================================


    /***
     *数据接受
     */
    private static String currworkName = "";
    //任务列表，
    private static HashMap<String, DataCallback> workActionList = new HashMap<>();

    private static void sendData(String work, byte[] data) {
        currworkName = work;
        BleManger.INATAN.sendData(data);
    }

    private static void handleGateWay(String hexString, byte[] bytes) {
        Logger.d("handleGateWay 指令返回：" + currworkName + "========" + hexString);
        DataCallback callback = workActionList.get(currworkName);
        if (callback == null) {
            Logger.d("GateWayMsgHandle=====currworkName 为空");
            return;
        }
        callback.callback(currworkName, hexString, bytes);
    }


    public void checkOta(String code) {
        workActionList.clear();
        workActionList.put(WorkName.WORK_GET_GATEWATY_VERSION, new DataCallback() {
            @Override
            public void callback(String work, String hexString, byte[] data) {
                Logger.d("==========获取网关版本返回：" + hexString);
                //fa0001f1c1a4
                boolean isSuccess = hexString.startsWith("fa13") && hexString.substring(6, 8).equals("00");
                //成功
                //FAH+13H+数据长度（1byte）+返回码（1byte）+版本（5byte）+ MAC地址（6byte）+ IP地址（4byte）+端口（2byte）+crc
                //fa13 12 00 0031303031 00ccdeba7856 276c4d99 21db dbf7
                if (isSuccess) {
//                    softVersion = hexString.substring(8, 18);
                    //直接打开ota
//                    sendData(WORK_ENABLE_OTA, P01GateWayCmdMaker.p01GatewayEnableOta(gateWayCode));
                }
                //失败,fa0001f1c1a4
                else {

                }
            }
        });

        //
        //如果有升级--》打开ota
        workActionList.put(WorkName.WORK_ENABLE_OTA, new DataCallback() {
            @Override
            public void callback(String work, String hexString, byte[] data) {
                boolean isSuccess = hexString.startsWith("fa14") && hexString.substring(6, 8).equals("00");
                if (isSuccess) {
                    Logger.d("==========打开ota成功");
                    powerLiveData.setValue("成功");
                } else {
                    Logger.d("==========打开ota失败");
                }
            }
        });
    }

    /**
     * 进行逻辑，然后刷新界面，
     */
    public void startWork(String str) {
        log("=========startWork");
        if (!BleManger.INATAN.isConnect()) {
            loadingLiveData.setValue(true);
        }
        //
        byte[] data = HexUtils.hexStr2Bytes(str);
        BleManger.INATAN.postData(MyApplication.devMac, data);

    }
}

