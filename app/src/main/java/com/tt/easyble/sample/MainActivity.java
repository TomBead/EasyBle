package com.tt.easyble.sample;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tt.easyble.R;
import com.tt.easyble.ble.BleManger;

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
    List<String> stringList = new ArrayList<>();
    LogAdapter logAdapter;
    BleViewModel bleViewModel;

    @Override
    public int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    public void inti() {
        super.inti();
        initViewModel();
        observeLivaData();
        setRv();

        String msg = "f1040631333534373705";
        mainEd.setText(msg);
        //
        MyApplication.devMac = "C7:EA:AF:BE:2F:77";
        MyApplication.devName = "eLock-AAB455D0";
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

    private void initViewModel() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory());
        bleViewModel = viewModelProvider.get(BleViewModel.class);
    }

    //观察ViewModel的数据，且此数据 是 View 直接需要的，不需要再做逻辑处理
    private void observeLivaData() {
        bleViewModel.getConnectLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                addLog(error);
            }
        });

        bleViewModel.getLoadingLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                //显示/隐藏加载进度条
                if (aBoolean) {
                    showLoadingDialog("");
                } else {
                    stopLoading();
                }
            }
        });
    }

    void sendMsg() {
        String str = mainEd.getText().toString().trim();
        bleViewModel.startWork(str);
    }


    void cleanLog() {
        stringList.clear();
        logAdapter.notifyDataSetChanged();
    }

    void addLog(String log) {
        stringList.add(log);
        logAdapter.setNewData(stringList);
    }


}
