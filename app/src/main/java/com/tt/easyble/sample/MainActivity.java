package com.tt.easyble.sample;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.tt.easyble.R;
import com.tt.easyble.ble.BleConnectCallBack;
import com.tt.easyble.ble.BleManger;
import com.tt.easyble.ble.HexUtils;
import com.tt.easyble.sample.a1.A1LockHandler;
import com.tt.easyble.sample.a1.A1MsgBuilder;
import com.tt.easyble.sample.view.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * https://blog.csdn.net/laoguanhua/article/details/81385270
 */
public class MainActivity extends BaseActivity {


    @BindView(R.id.main_dev_name)
    TextView mainDevName;
    @BindView(R.id.main_dev_mac)
    TextView mainDevMac;
    @BindView(R.id.main_connect_state)
    TextView mainConnectState;
    @BindView(R.id.main_log_rv)
    RecyclerView mainLogRv;
    @BindView(R.id.main_clean_back)
    Button mainCleanBack;
    @BindView(R.id.main_ed)
    EditText mainEd;
    @BindView(R.id.main_send)
    Button mainSend;


    //
    BleConnectCallBack bleConnectCallBack;
    List<String> stringList = new ArrayList<>();

    LogAdapter logAdapter;

    @Override
    public int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    public void inti() {
        super.inti();
        setBle();
        MyApplication.devMac = "DE:5F:50:E6:FE:D0";
        mainDevName.setText(MyApplication.devName);
        mainDevMac.setText(MyApplication.devMac);
        if (BleManger.INATAN.isConnect()) {
            mainConnectState.setText("连接");
        } else {
            mainConnectState.setText("断开");
        }
        setRv();
    }

    //点击监听
    @OnClick({R.id.main_clean_back, R.id.main_send, R.id.main_connect_state})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.main_clean_back:
                cleanLog();
                break;
            case R.id.main_send:
//                sendMsg();
                A1LockHandler.setMac(MyApplication.devMac);
                A1LockHandler.addLock();
                break;
            case R.id.main_connect_state:
                connectDev();
                break;
            default:
                break;
        }
    }

    private void connectDev() {
        BleManger.INATAN.connectDevice(MyApplication.devMac, bleConnectCallBack);
    }


    void setRv() {
        logAdapter = new LogAdapter(this, stringList);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mainLogRv.setLayoutManager(manager);
        mainLogRv.addItemDecoration(new SpacesItemDecoration(10));
        mainLogRv.setAdapter(logAdapter);
    }


    void sendMsg() {
        String str = mainEd.getText().toString().trim();
        byte[] data = HexUtils.hexStr2Bytes(str);
        String cmd = A1MsgBuilder.OpenLock("148410");
        data = HexUtils.hexStr2Bytes(cmd);
        BleManger.INATAN.sendData(data);
    }

    void cleanLog() {
        stringList.clear();
        logAdapter.notifyDataSetChanged();
    }

    void addLog(String log) {
        stringList.add(log);
        Logger.d("=====" + stringList);
        logAdapter.setNewData(stringList);
    }


    void setBle() {
        bleConnectCallBack = new BleConnectCallBack(MyApplication.devMac) {
            @Override
            public void connectSuccess() {
                super.connectSuccess();
                mainConnectState.setText("连接");
            }

            @Override
            public void connectFail(String errorMsg) {
                Logger.d("============= Thread ==" + Thread.currentThread().getName());
                mainConnectState.setText("断开");
            }

            @Override
            public void writeTimeOut() {
                Logger.d("============= Thread ==" + Thread.currentThread().getName());
                addLog("发送超时");
            }

            @Override
            public void handleMsg(String hexString, byte[] bytes) {
                addLog(hexString);
                //
            }

            @Override
            public void sendFail(String errorCode) {
                addLog(errorCode);
            }
        };
        BleManger.INATAN.addBleConnectCallBack(bleConnectCallBack);
    }


    void setSend() {

    }

}
