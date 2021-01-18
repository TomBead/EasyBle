package com.tt.easyble.sample.box;


import android.app.Activity;

import com.orhanobut.logger.Logger;
import com.tt.easyble.ble.BleManger;
import com.tt.easyble.ble.LockHandle;
import com.tt.easyble.ble.WorkAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A1处理的数据
 */
public class BoxLockHandler implements LockHandle {

    private static String currworkName = "";
    //获取加密序号
    public final static String WORK_GET_ENCRY_NUM = "WORK_GET_ENCRY_NUM";
    //更新加密密钥
    public final static String WORK_UPDATE_KEY = "WORK_UPDATE_KEY";
    //初始化密钥
    public final static String WORK_INTI_KEY = "WORK_INTI_KEY";
    //查询密码列表
    public final static String WORK_GET_LOCAL_PWD = "WORK_GET_LOCAL_PWD";
    //查询指纹列表
    public final static String WORK_GET_LOCAL_ZW = "WORK_GET_LOCAL_ZW";
    //添加的时候，本地有密码，或者指纹，发这个去获取开锁密码
    public final static String WORK_CHECK_ADD_DEVICE = "WORK_CHECK_ADD_DEVICE";
    //下发管理员代号
    public final static String WORK_UPDATE_MANGER = "WORK_UPDATE_MANGER";
    //同步时间
    public final static String WORK_UPDATE_TIME = "WORK_UPDATE_TIME";
    //获取电量
    public final static String WORK_GET_POWER = "WORK_GET_POWER";
    //app开锁
    public final static String WORK_OPEN_LOCK = "WORK_OPEN_LOCK";

    //添加密码
    public final static String WORK_ADD_PINGCODE = "WORK_ADD_PINGCODE";
    //删除密码
    public final static String WORK_DEL_PINGCODE = "WORK_DEL_PINGCODE";
    //添加指纹
    public final static String WORK_ADD_FINGER = "WORK_ADD_FINGER";
    //删除指纹
    public final static String WORK_DEL_FINGER = "WORK_DEL_FINGER";
    //获取开锁记录条数
    public final static String WORK_GET_RECORD_COUNT = "WORK_GET_RECORD_COUNT";
    //获取开锁记录
    public final static String WORK_GET_RECORD = "WORK_GET_RECORD";
    //设置常开
    public final static String WORK_SET_KEEP_OPEN = "WORK_SET_KEEP_OPEN";
    //设置静音
    public final static String WORK_SET_SILENT = "WORK_SET_SILENT";

    //清除临时密码
    public final static String WORK_CLEAN_SUB_PWD_FINGER_CARD = "WORK_CLEAN_SUB_PWD_FINGER_CARD";

    //清除锁内开锁记录
    public final static String WORK_CLEAN_OPEN_RECORD = "WORK_CLEAN_OPEN_RECORD";

    //任务列表，
    private static HashMap<String, WorkAction> workActionList = new HashMap<>();


    //===================================================================================
    //加密密钥。。4byte，8个字符串
    private static String encryStr = "";
    private static String mPassword = "";
    private static String newPassword = "";

    //要添加的密码
    private static String addPwd;
    //删除的密码id
    private static String delPwdnumId;
    //
    private static String startTime, endTime;

    //常开设置，静音设置
    private static String openState;

    //
    //有多少条记录
    private static int totalRecordCount;
    private static int recordPage;
    private static int pageCount;
    private static int totalRecordPage;
    private static List<String> uploadRecordList = new ArrayList<>();

    //锁内密码数量
    private static int devLocalPwdCount;
    //锁内密码数量
    private static int devLocalZWCount;
    //===================================================================================

    //
    public BoxLockHandler(Activity activity) {

    }

    @Override
    public String getName() {
        return "box";
    }

    @Override
    public void connectSuccess() {
        WorkAction workAction = workActionList.get(currworkName);
        if (workAction != null) {
            sendData(WORK_OPEN_LOCK, workAction.getData());
        } else {
            Logger.d("找不到指令 currworkName = " + currworkName);
        }
    }

    @Override
    public void connectFail() {

    }

    @Override
    public void writeTimeOut() {

    }

    @Override
    public void handleMsg(String hexString, byte[] bytes) {
        Logger.d("指令返回：" + currworkName + "========" + hexString);
        //处理返回数据，，
        WorkAction workAction = workActionList.get(currworkName);
        if (workAction != null) {
            workAction.workCallback(workAction.getWorkName(), hexString, bytes);
        }
    }


    public void opeLock(String mac, String password) {
        mPassword = password;
        workActionList.clear();
        //==================================================================================
        //1获取加密序号，这个跟锁的逻辑有关
        byte[] data = com.tt.easyble.sample.box.BoxCmdBuilder.getFirmwareVersion();
        workActionList.put(WORK_GET_ENCRY_NUM, new WorkAction(WORK_GET_ENCRY_NUM, data) {
            @Override
            public void workCallback(String work, String hexString, byte[] data) {
                if (data.length == 16) {
                    encryStr = com.tt.easyble.sample.box.BoxCmdAnalysis.getEncryNumStr(data);
                    Logger.d("========encryStr:" + encryStr);
                    //下一步
                    encryStr = encryStrAddOne(encryStr);
                    byte[] openlockdata = com.tt.easyble.sample.box.BoxCmdBuilder.openLock(encryStr, mPassword);
                    sendData(WORK_OPEN_LOCK, openlockdata);
                } else {
                    Logger.d("========获取加密序号错误");
                }
            }
        });

        //==============================================
        workActionList.put(WORK_OPEN_LOCK, new WorkAction(WORK_OPEN_LOCK, null) {
            @Override
            public void workCallback(String work, String hexString, byte[] data) {

            }
        });

        //连接开始,设置连接成功开始地点，
        currworkName = WORK_GET_ENCRY_NUM;
        BleManger.INATAN.connect(mac);
    }


    private void sendData(String work, byte[] data) {
        currworkName = work;
        BleManger.INATAN.sendData(data);
    }

    /**
     * 序号+1
     * 0000000c-->0000000c+1=0000000d
     * 0000001c-->0000001c+1=0000001d
     */
    private static String encryStrAddOne(String encryStr) {
        int num = Integer.parseInt(encryStr, 16);
        encryStr = Integer.toHexString(num + 1);
        //原来是6位，，
        int len = 8 - encryStr.length();
        for (int i = 0; i < len; i++) {
            encryStr = "0" + encryStr;
        }
        return encryStr;
    }
}
