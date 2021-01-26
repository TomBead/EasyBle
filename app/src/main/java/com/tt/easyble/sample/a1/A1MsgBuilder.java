package com.tt.easyble.sample.a1;

import com.orhanobut.logger.Logger;

/**
 * A1锁指令
 */

public class A1MsgBuilder {

    public static StringBuffer sb;

    public static String bytes;

    public static String[] xors = new String[40];

    public static String sendType(String type, String data) {
        String dataLength = "";
        dataLength = Integer.toHexString(data.length() / 2);
        sb = new StringBuffer();
        sb.append("F1");
        sb.append(type);
        sb.append(buo(dataLength));
        sb.append(data);
        sb.append(buos(Integer.toHexString((xor(buo(dataLength), data)))));
        return sb.toString();
    }

    /**
     * 设置锁蓝牙广播名称
     *
     * @param name:广播名（1-15位字符串）
     * @return
     */
    public static String SetBleBroadCastName(String name) {
        return sendType("01", stringToHexstring(name));
    }

    /**
     * 查询锁蓝牙Mac地址
     *
     * @return
     */
    public static String GetBleMac() {
        return sendType("02", "01");
    }

    /**
     * APP添加门锁，下发管理员代号
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String AddMPermisson(String userId) {
        Logger.d("======APP添加门锁，下发管理员代号 " + userId);
//        return sendType("03","ffffffffffff"+getIntString(userId));
        return sendType("03", "ffffffffffff" + getIntString(userId));
//        return sendType("03",getIntString(userId)+getIntString(userId));
    }

    /**
     * 重置管理员代号成12个f
     */
    public static String ResetMPermisson(String userId) {
        Logger.d("======APP取消添加门锁，重置管理员代号 " + userId);
        return sendType("03", getIntString(userId) + "ffffffffffff");
    }


    /**
     * 比较管理员密码
     *
     * @param userId:管理员代号（6位字符串）
     * @return 读管理员密码，
     */
    public static String checkManagerPassword(String userId, String password) {
        Logger.d("======checkManagerPassword ");
        return sendType("19", getIntString(userId) + "01" + getRemainPsw(password));
    }


    /**
     * 修改管理员密码
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String changeManagerPassword(String userId, String oldPsw, String newPsw) {
        Logger.d("======修改管理员密码");
        return sendType("18", getIntString(userId) + getRemainPsw(oldPsw) + getRemainPsw(newPsw));
    }

    public static String getRemainPsw(String str) {
        return str + "ffffffffff".substring(str.length());
    }

    /**
     * 获取固件版本
     *
     * @return
     */
    public static String getFirmwareVersion() {
        Logger.d("======获取固件版本");
        return sendType("30", "00");
    }

    private static String getIntString(String userId) {
        return bytesToHexString(userId.getBytes());
    }

    private static String getIntString2(String userId) {
        String result = "";
        if (userId.length() < 10) {
            StringBuffer sb = new StringBuffer();
            int num = 10 - userId.length();
            for (int i = 0; i < num; i++) {
                sb.append("f");
            }
            result = userId + sb.toString();
        } else if (userId.length() == 10) {
            result = userId;
        } else {
            result = userId.substring(0, 10);
        }
        return result;
    }

    /**
     * APP开锁
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String OpenLock(String userId) {
        Logger.d("======APP开锁");
        return sendType("04", getIntString(userId));
    }

    /**
     * APP添加开锁密码(永久)
     *
     * @param userId:管理员代号（6位字符串）
     * @param psw:门锁密码（6-10位字符串）
     * @return
     */
    public static String SetLockPsw(String userId, String psw) {
        Logger.d("======APP添加开锁密码(永久)");
        return sendType("05", getIntString(userId) + "0b000000ffffff" + getIntString2(psw));
    }

    /**
     * APP添加开锁密码(限时)
     *
     * @param userId:管理员代号（6位字符串）
     * @param psw:门锁密码（6-10位字符串）
     * @return
     */
    public static String SetLockPsw(String userId, String psw, String time) {
        Logger.d("====== APP添加开锁密码(限时)");
        return sendType("05", getIntString(userId) + time + getIntString2(psw));
    }

    /**
     * APP删除开锁密码
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String CleanLockPsw(String index, String userId) {
        Logger.d("======APP删除开锁密码");
        return sendType("06", getIntString(userId) + index);
    }

    /**
     * APP添加指纹开锁
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String SetFingerLock(String userId) {
        Logger.d("======APP添加指纹开锁");
        return sendType("07", getIntString(userId) + "0b000000ffffff");
    }

    /**
     * APP添加指纹开锁（时效）
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String SetFingerLock(String userId, String time) {
        Logger.d("======APP添加指纹开锁（时效）");
        return sendType("07", getIntString(userId) + time);
    }

    /**
     * APP清除指纹开锁
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String CleanFingerLock(String num, String userId) {
        Logger.d("======APP清除指纹开锁，指纹编号：" + num);
        return sendType("08", getIntString(userId) + buo(num));
    }

    /**
     * 门锁常开功能设置
     *
     * @param userId:管理员代号（6位字符串）
     * @param set:功能码
     * @return
     */
    public static String LockUsualSet(String userId, String set) {
        Logger.d("======门锁常开功能设置" + set);
        return sendType("09", getIntString(userId) + buo(set));
    }

    /**
     * 门锁静音功能设置
     *
     * @param userId:管理员代号（6位字符串）
     * @param set:功能码
     * @return
     */
    public static String LockSetSilent(String userId, String set) {
        Logger.d("======门锁静音功能设置" + set);
        return sendType("0A", getIntString(userId) + buo(set));
    }

    /**
     * 门锁防拆功能设置
     *
     * @param userId:管理员代号（6位字符串）
     * @param set:功能码
     * @return
     */
    public static String LockDismantleSet(String userId, String set) {
        return sendType("0B", getIntString(userId) + buo(set));
    }

    /**
     * 门锁电子锁芯（波动开关）功能设置
     *
     * @param userId:管理员代号（6位字符串）
     * @param set:功能码
     * @return
     */
    public static String EleLockCoreSet(String userId, String set) {
        return sendType("0C", getIntString(userId) + buo(set));
    }

    /**
     * 查询开锁记录总数量
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String GetLockLogPage(String userId) {
        Logger.d("======查询开锁记录总数量");
        return sendType("0D", getIntString(userId));
    }

    /**
     * 查询开锁记录
     *
     * @param userId:管理员代号（6位字符串）
     * @param page:页码
     * @return
     */
    public static String GetLockLog(String userId, String page) {
        Logger.d("======查询开锁记录  userId" + userId + "  page" + page);
        return sendType("0E", getIntString(userId) + buo(page));
    }

    /**
     * 同步时间
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String SetTime(String userId, String time) {
        Logger.d("=====同步时间");
        return sendType("0F", getIntString(userId) + timeTypeChange(time));
    }

    /**
     * 添加门卡
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String AddCards(String userId) {
        Logger.d("=====添加门卡");
        return sendType("12", getIntString(userId) + "0b000000ffffff");
    }

    /**
     * 添加门卡(添加时效)
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String AddCards(String userId, String time) {
        Logger.d("=====添加门卡(添加时效)");
        return sendType("12", getIntString(userId) + time);
    }

    /**
     * 删除门卡
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String DelCards(String userId, String num) {
        Logger.d("=====删除门卡");
        return sendType("13", getIntString(userId) + buo(num));
    }

    /**
     * 查询门锁电池电量
     */
    public static String getBattry() {
        Logger.d("======获取电量");
        return sendType("31", "0100");
    }

    /**
     * 设置临时密码的加密密钥
     */
    public static String setKey(String userId, String key) {
        Logger.d("=====设置临时密码的加密密钥" + key);
        return sendType("24", getIntString(userId) + key + "00");
    }

    /**
     * 查询门卡记录
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String GetCards(String userId) {
        return sendType("14", getIntString(userId));
    }

    /**
     * 更新门锁数量
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String updateLogNum(String userId) {
        return sendType("1d", getIntString(userId) + "0000");
    }

    /**
     * 清除权限（清除用户权限）
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String CleanQuan(String userId) {
        Logger.d("=====清除权限（清除用户权限）");
        return sendType("26", getIntString(userId) + "b2");
    }

    /**
     * 清除权限（清除所有临时密码权限）
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String CleanLin(String userId) {
        Logger.d("=====除权限（清除所有临时密码权限）");
        return sendType("26", getIntString(userId) + "a1");
    }

    /**
     * 重置门锁（清除用户权限）
     *
     * @param userId:管理员代号（6位字符串）
     * @return
     */
    public static String ResetLock(String userId) {
        Logger.d("=====重置门锁（清除用户权限）");
        return sendType("26", getIntString(userId) + "c3");
    }

    public static String buo(String s) {
        if (s.length() == 1) {
            s = s.toUpperCase();
            return "0" + s;
        } else {
            return s;
        }
    }

    public static String buos(String s) {
        if (s.length() == 1) {
            s = s.toUpperCase();
            return "0" + s;
        } else if (s.length() == 2) {
            return s;
        } else {
            s = s.substring(s.length() - 2, s.length());
            return s;
        }
    }


    public static String timeTypeChange(String date) {
        String newdate = date.replace("-", "").replace(" ", "").replace(":", "");
        return newdate.substring(2, newdate.length());
    }

    public static String stringToHexstring(String s) {
        return bytesToHexString(s.getBytes());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static int xor(String data1, String data2) {
        bytes = "";
        String X3 = data1 + data2;
        int length = X3.length() / 2;
        for (int i = 0, j = 0; i < X3.length(); i += 2, j++) {
            xors[j] = X3.substring(i, i + 2);
        }
        for (int i = 0; i < length - 1; i++) {
            if (i == 0) {
//                Log.e("anjiss", "xor:" + Integer.parseInt(xors[i], 16) + "^" + Integer.parseInt(xors[i + 1], 16));
                bytes = String.valueOf(Integer.parseInt(xors[i], 16) ^ Integer.parseInt(xors[i + 1], 16));
//                Log.e("anjiss", "等於" + Integer.parseInt(bytes));
            } else {
//                Log.e("anjiss", "xor:" + Integer.parseInt(bytes) + "^" + Integer.parseInt(xors[i + 1], 16));
                bytes = String.valueOf(Integer.parseInt(bytes) ^ Integer.parseInt(xors[i + 1], 16));
//                Log.e("anjiss", "等於" + Integer.parseInt(bytes));
            }
        }
//        Log.e("anjiss", "x2:" + (Integer.parseInt(bytes) % 256));
        return Integer.parseInt(bytes);
    }

}
