//package com.tt.easyble.sample.box;
//
//
//import android.app.Activity;
//import android.content.Intent;
//
//import com.orhanobut.logger.Logger;
//import com.tt.easyble.ble.HexUtils;
//import com.tt.easyble.ble.work.SendManger;
//import com.tt.easyble.sample.a1.A1MsgBuilder;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * A1处理的数据
// */
//public class BoxLockHandler {
//
//    private static String currworkName = "";
//    //获取加密序号
//    public final static String WORK_GET_ENCRY_NUM = "WORK_GET_ENCRY_NUM";
//    //更新加密密钥
//    public final static String WORK_UPDATE_KEY = "WORK_UPDATE_KEY";
//    //初始化密钥
//    public final static String WORK_INTI_KEY = "WORK_INTI_KEY";
//    //查询密码列表
//    public final static String WORK_GET_LOCAL_PWD = "WORK_GET_LOCAL_PWD";
//    //查询指纹列表
//    public final static String WORK_GET_LOCAL_ZW = "WORK_GET_LOCAL_ZW";
//    //添加的时候，本地有密码，或者指纹，发这个去获取开锁密码
//    public final static String WORK_CHECK_ADD_DEVICE = "WORK_CHECK_ADD_DEVICE";
//    //下发管理员代号
//    public final static String WORK_UPDATE_MANGER = "WORK_UPDATE_MANGER";
//    //同步时间
//    public final static String WORK_UPDATE_TIME = "WORK_UPDATE_TIME";
//    //获取电量
//    public final static String WORK_GET_POWER = "WORK_GET_POWER";
//    //app开锁
//    public final static String WORK_OPEN_LOCK = "WORK_OPEN_LOCK";
//
//    //添加密码
//    public final static String WORK_ADD_PINGCODE = "WORK_ADD_PINGCODE";
//    //删除密码
//    public final static String WORK_DEL_PINGCODE = "WORK_DEL_PINGCODE";
//    //添加指纹
//    public final static String WORK_ADD_FINGER = "WORK_ADD_FINGER";
//    //删除指纹
//    public final static String WORK_DEL_FINGER = "WORK_DEL_FINGER";
//    //获取开锁记录条数
//    public final static String WORK_GET_RECORD_COUNT = "WORK_GET_RECORD_COUNT";
//    //获取开锁记录
//    public final static String WORK_GET_RECORD = "WORK_GET_RECORD";
//    //设置常开
//    public final static String WORK_SET_KEEP_OPEN = "WORK_SET_KEEP_OPEN";
//    //设置静音
//    public final static String WORK_SET_SILENT = "WORK_SET_SILENT";
//
//    //清除临时密码
//    public final static String WORK_CLEAN_SUB_PWD_FINGER_CARD = "WORK_CLEAN_SUB_PWD_FINGER_CARD";
//
//    //清除锁内开锁记录
//    public final static String WORK_CLEAN_OPEN_RECORD = "WORK_CLEAN_OPEN_RECORD";
//
//
//    //===================================================================================
//    //加密密钥。。4byte，8个字符串
//    private static String encryStr = "";
//    private static String mPassword = "";
//    private static String newPassword = "";
//
//    //要添加的密码
//    private static String addPwd;
//    //删除的密码id
//    private static String delPwdnumId;
//    //
//    private static String startTime, endTime;
//
//    //常开设置，静音设置
//    private static String openState;
//
//    //
//    //有多少条记录
//    private static int totalRecordCount;
//    private static int recordPage;
//    private static int pageCount;
//    private static int totalRecordPage;
//    private static List<String> uploadRecordList = new ArrayList<>();
//
//    //锁内密码数量
//    private static int devLocalPwdCount;
//    //锁内密码数量
//    private static int devLocalZWCount;
//    //===================================================================================
//    //
//    private static String devMac;
//
//    //
//    public BoxLockHandler(Activity activity) {
//
//    }
//
//
//    //===============================================
//    private static byte[] data = null;
//
//    public static void addLock() {
//        data = HexUtils.hexStr2Bytes(A1MsgBuilder.AddMPermisson(mPassword));
//        SendManger.INATAN.inti(devMac)
//                .listMsg("AddMPermisson", new SendManger.MsgCallBack() {
//                    @Override
//                    public void callback(String work, byte[] data, String hexString) {
//                        if (hexString.startsWith("f10301")) {
//                            String backString = hexString.substring(6, 8);
//                            if ("00".equals(backString)) {
//                                context.sendBroadcast(new Intent(addMember));
//                            } else {
//                                context.sendBroadcast(new Intent(addError));
//                            }
//                        }
//                    }
//                })
//                //
//                .listMsg("setkey", new SendManger.MsgCallBack() {
//                    @Override
//                    public void callback(String work, byte[] data, String hexString) {
//                        if (hexString.startsWith("f12401")) {
//                            String backString = hexString.substring(6, 8);
//                            if ("00".equals(backString)) {
//                                intent2.putExtra("setKey", "1");
//                                context.sendBroadcast(intent2);
//                            } else {
//                                intent2.putExtra("setKey", "error");
//                                context.sendBroadcast(intent2);
//                            }
//                        }
//                    }
//                })
//                .listMsg(WORK_INTI_KEY, new SendManger.MsgCallBack() {
//                    @Override
//                    public void callback(String work, byte[] data, String hexString) {
//                        if (data.length == 4 && hexString.substring(2, 4).equals("00")) {
//                            Logger.d(work + "========修改密码成功-->获取锁内密码");
//                            mPassword = newPassword;
//                            encryStr = "00000000";
//                            encryStr = encryStrAddOne(encryStr);
//                            devLocalPwdCount = 0;
//                            byte[] getPwdList = BoxCmdBuilder.getDevPwdList(encryStr, mPassword);
//                            SendManger.INATAN.sendListData(WORK_GET_LOCAL_PWD, getPwdList);
//                        } else {
//                            Logger.d(work + "========修改密码失败-->初始化密码");
//                            encryStr = encryStrAddOne(encryStr);
//                            byte[] intiPwd = BoxCmdBuilder.buildIntiPassword(encryStr, newPassword);
//                            SendManger.INATAN.sendListData(WORK_INTI_KEY, intiPwd);
//                        }
//
//                    }
//                })
//
//
//                //
//                .listMsg(WORK_GET_LOCAL_PWD, new SendManger.MsgCallBack() {
//                    @Override
//                    public void callback(String work, byte[] data, String hexString) {
//                        if (data.length == 4 && hexString.substring(2, 4).equals("00")) {
//                            Logger.d(work + "========修改密码成功-->获取锁内密码");
//                            mPassword = newPassword;
//                            encryStr = "00000000";
//                            encryStr = encryStrAddOne(encryStr);
//                            devLocalPwdCount = 0;
//                            byte[] getPwdList = BoxCmdBuilder.getDevPwdList(encryStr, mPassword);
//                            SendManger.INATAN.sendListData(WORK_GET_LOCAL_PWD, getPwdList);
//                        } else {
//                            Logger.d(work + "========修改密码失败-->初始化密码");
//                            encryStr = encryStrAddOne(encryStr);
//                            byte[] intiPwd = BoxCmdBuilder.buildIntiPassword(encryStr, newPassword);
//                            SendManger.INATAN.sendListData(WORK_INTI_KEY, intiPwd);
//                        }
//
//                    }
//                })
//
//
//                .listMsg(WORK_GET_LOCAL_ZW, new SendManger.MsgCallBack() {
//                    @Override
//                    public void callback(String work, byte[] data, String hexString) {
//                        if (data.length == 4 && hexString.substring(2, 4).equals("00")) {
//                            Logger.d(work + "========修改密码成功-->获取锁内密码");
//                            mPassword = newPassword;
//                            encryStr = "00000000";
//                            encryStr = encryStrAddOne(encryStr);
//                            devLocalPwdCount = 0;
//                            byte[] getPwdList = BoxCmdBuilder.getDevPwdList(encryStr, mPassword);
//                            SendManger.INATAN.sendListData(WORK_GET_LOCAL_PWD, getPwdList);
//                        } else {
//                            Logger.d(work + "========修改密码失败-->初始化密码");
//                            encryStr = encryStrAddOne(encryStr);
//                            byte[] intiPwd = BoxCmdBuilder.buildIntiPassword(encryStr, newPassword);
//                            SendManger.INATAN.sendListData(WORK_INTI_KEY, intiPwd);
//                        }
//
//                    }
//                })
//
//                .listMsg(WORK_ADD_PINGCODE, new SendManger.MsgCallBack() {
//                    @Override
//                    public void callback(String work, byte[] data, String hexString) {
//                        if (data.length == 4 && hexString.substring(2, 4).equals("00")) {
//                            Logger.d(work + "========修改密码成功-->获取锁内密码");
//                            mPassword = newPassword;
//                            encryStr = "00000000";
//                            encryStr = encryStrAddOne(encryStr);
//                            devLocalPwdCount = 0;
//                            byte[] getPwdList = BoxCmdBuilder.getDevPwdList(encryStr, mPassword);
//                            SendManger.INATAN.sendListData(WORK_GET_LOCAL_PWD, getPwdList);
//                        } else {
//                            Logger.d(work + "========修改密码失败-->初始化密码");
//                            encryStr = encryStrAddOne(encryStr);
//                            byte[] intiPwd = BoxCmdBuilder.buildIntiPassword(encryStr, newPassword);
//                            SendManger.INATAN.sendListData(WORK_INTI_KEY, intiPwd);
//                        }
//
//                    }
//                })
//                .listMsg(WORK_UPDATE_MANGER, new SendManger.MsgCallBack() {
//                    @Override
//                    public void callback(String work, byte[] data, String hexString) {
//                        if (data.length == 4 && hexString.substring(2, 4).equals("00")) {
//                            Logger.d(work + "========修改密码成功-->获取锁内密码");
//                            mPassword = newPassword;
//                            encryStr = "00000000";
//                            encryStr = encryStrAddOne(encryStr);
//                            devLocalPwdCount = 0;
//                            byte[] getPwdList = BoxCmdBuilder.getDevPwdList(encryStr, mPassword);
//                            SendManger.INATAN.sendListData(WORK_GET_LOCAL_PWD, getPwdList);
//                        } else {
//                            Logger.d(work + "========修改密码失败-->初始化密码");
//                            encryStr = encryStrAddOne(encryStr);
//                            byte[] intiPwd = BoxCmdBuilder.buildIntiPassword(encryStr, newPassword);
//                            SendManger.INATAN.sendListData(WORK_INTI_KEY, intiPwd);
//                        }
//
//                    }
//                })
//                .listMsg(WORK_UPDATE_TIME, new SendManger.MsgCallBack() {
//                    @Override
//                    public void callback(String work, byte[] data, String hexString) {
//                        if (data.length == 4 && hexString.substring(2, 4).equals("00")) {
//                            Logger.d(work + "========修改密码成功-->获取锁内密码");
//                            mPassword = newPassword;
//                            encryStr = "00000000";
//                            encryStr = encryStrAddOne(encryStr);
//                            devLocalPwdCount = 0;
//                            byte[] getPwdList = BoxCmdBuilder.getDevPwdList(encryStr, mPassword);
//                            SendManger.INATAN.sendListData(WORK_GET_LOCAL_PWD, getPwdList);
//                        } else {
//                            Logger.d(work + "========修改密码失败-->初始化密码");
//                            encryStr = encryStrAddOne(encryStr);
//                            byte[] intiPwd = BoxCmdBuilder.buildIntiPassword(encryStr, newPassword);
//                            SendManger.INATAN.sendListData(WORK_INTI_KEY, intiPwd);
//                        }
//
//                    }
//                })
//                //
//                //最后发送
//                .startSend(WORK_GET_ENCRY_NUM, data);
//    }
//
//
//    /**
//     *
//     */
//    public interface SendCallBack {
//
//        void openSuccess();
//    }
//
//}
