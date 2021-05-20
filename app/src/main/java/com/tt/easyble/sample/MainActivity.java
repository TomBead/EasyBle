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
import com.tt.easyble.ble.work.SendManger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


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
    @BindView(R.id.main_disconnect)
    Button mainDisconnect;

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
        setRv();

        String msg = "f1040631333534373705";
        mainEd.setText(msg);
        //
        MyApplication.devMac = "C7:EA:AF:BE:2F:77";
        MyApplication.devName = "蓝牙设备AA";
        mainDevName.setText(MyApplication.devName);
        mainDevMac.setText(MyApplication.devMac);
        if (BleManger.INATAN.isConnect()) {
            mainConnectState.setText("连接");
        } else {
            mainConnectState.setText("断开");
        }
    }


    //点击监听
    @OnClick({R.id.main_clean_back, R.id.main_send, R.id.main_disconnect})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.main_clean_back:
                cleanLog();
                break;
            case R.id.main_send:
                sendMsg();
                break;
            case R.id.main_disconnect:
                BleManger.INATAN.disConnectByCode();
                break;
            default:
                break;
        }
    }

    void setRv() {
        logAdapter = new LogAdapter(this, stringList);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mainLogRv.setLayoutManager(manager);
        mainLogRv.setAdapter(logAdapter);
    }


    void sendMsg() {
        String str = mainEd.getText().toString().trim();
        byte[] data = HexUtils.hexStr2Bytes(str);
        BleManger.INATAN.postData(MyApplication.devMac, data);
    }

    void setBle() {
        bleConnectCallBack = new BleConnectCallBack("main") {
            @Override
            public void connectSuccess() {
                super.connectSuccess();
                mainConnectState.setText("连接");
            }

            @Override
            public void connectFail(String errorMsg) {
                mainConnectState.setText(errorMsg);
            }

            @Override
            public void writeTimeOut() {
                addLog("发送超时");
            }

            @Override
            public void handleMsg(String hexString, byte[] bytes) {
                addLog(hexString);
            }

            @Override
            public void sendFail(String errorCode) {
                addLog(errorCode);
            }
        };
        BleManger.INATAN.addBleConnectCallBack(bleConnectCallBack);
    }

    void cleanLog() {
        stringList.clear();
        logAdapter.notifyDataSetChanged();
    }

    void addLog(String log) {
        stringList.add(log);
        logAdapter.setNewData(stringList);
    }
    //==================================================

    /**
     * 多条指令串起来可以这么写
     * 一条发完需要结果再发下一条
     */
    public static void startWork() {
        String msg1 = "AABBCCDD";
        byte[] data = HexUtils.hexStr2Bytes(msg1);
        //
        SendManger.INATAN.inti(MyApplication.devMac)
                //
                .listMsg("msg1", new SendManger.MsgCallBack() {
                    @Override
                    public void callback(String work, byte[] data, String hexString) {
                        //模拟逻辑判断
                        boolean isSuccess = hexString.length() % 2 == 0;
                        if (isSuccess) {
                            Logger.d("=======" + work + "成功");
                            //发送下一条指令。和下面的名字对上
                            String msg2 = "AABBCCDDEEFF";
                            byte[] getPwdList = HexUtils.hexStr2Bytes(msg2);
                            SendManger.INATAN.sendListData("msg2", getPwdList);
                        } else {
                            Logger.d("=======" + work + "失败");
                        }
                    }
                })
                //
                .listMsg("msg2", new SendManger.MsgCallBack() {
                    @Override
                    public void callback(String work, byte[] data, String hexString) {
                        //模拟逻辑判断
                        boolean isSuccess = hexString.length() % 2 == 0;
                        if (isSuccess) {
                            Logger.d("=======" + work + "成功");
                        } else {
                            Logger.d("=======" + work + "失败");
                        }
                    }
                })
                //...
                //最后从第一条开始发送
                .startSend("msg1", data);
    }

}
