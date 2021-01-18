package com.tt.easyble.sample.box;


import android.text.TextUtils;

import com.tt.easyble.sample.TimeUtils;
import com.orhanobut.logger.Logger;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author tt
 * box的指令更新为加密逻辑，现在的逻辑要改
 */
public class BoxCmdBuilder {


    //=======================明文开始=======================
    //读固件版本号，加密序号
    public static byte[] getFirmwareVersion() {
        String data = buildData("00", "30", "");
        return hexStr2Bytes(data);
    }

    //获取电池电量
    public static byte[] getDevPower() {
        String data = buildData("00", "31", "");
        return hexStr2Bytes(data);
    }

    //========================明文结束=============================


    //=======================加密密文开始==========================

    /**
     * 下发管理员代号
     */
    public static byte[] delLocalPwd(String encryNum, String password) {
        //指令名称
        String cmdByte = "03";
        //16个ff
        byte[] intiByte = new byte[16];
        Arrays.fill(intiByte, (byte) 0xff);
        String data = bytes2HexStr(intiByte);
        //加密密钥
        String intiKey = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, intiKey, data);
    }

    /**
     * app开锁
     */
    public static byte[] openLock(String encryNum, String password) {
        //指令名称
        String cmdByte = "04";
        //16个ff
        byte[] intiByte = new byte[16];
        Arrays.fill(intiByte, (byte) 0xff);
        String data = bytes2HexStr(intiByte);
        //加密密钥
        String intiKey = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, intiKey, data);
    }

    /**
     * app添加开锁密码
     */
    public static byte[] addPwd(String encryNum, String password, String addPwd, String startTime, String stopTime) {
        //指令名称
        String cmdByte = "05";
        String data = "";
        //没有时效,永久7byte+开锁密码,4-6位
        if (TextUtils.isEmpty(startTime)) {
            data = "0b000000ffffff" + addPwd;
        }
        //有时效,
        else {
//            data = Util.getTimeS(startTime, stopTime) + addPwd;
        }
        //填满16位
        data = buildCmdData(encryNum, data);
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }


    /**
     * app清除开锁密码
     */
    public static byte[] delPwd(String encryNum, String password, String pwdNo) {
        //指令名称
        String cmdByte = "06";
        //删除的id
        String data = buildCmdData(encryNum, pwdNo);
        //加密密钥
        String intiKey = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, intiKey, data);
    }

    /**
     * app添加指纹，，
     */
    public static byte[] addFinger(String encryNum, String password, String startTime, String endTime) {
        //指令名称
        String cmdByte = "07";
        String data = "";
        //没有时效,永久7byte+开锁密码,4-6位
        if (TextUtils.isEmpty(startTime)) {
            data = "0b000000ffffff";
        }
        //有时效,
        else {
//            data = Util.getTimeS(startTime, endTime);
        }
        //填满16位
        data = buildCmdData(encryNum, data);
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }

    /**
     * app删除指纹
     */
    public static byte[] delFinger(String encryNum, String password, String pwdNo) {
        //指令名称
        String cmdByte = "08";
        //pwdNo
        if (pwdNo.length() == 1) {
            pwdNo = "0" + pwdNo.toUpperCase();
        }
        //删除的id
        String data = buildCmdData(encryNum, pwdNo);
        //加密密钥
        String intiKey = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, intiKey, data);
    }

    /**
     * 门锁常开功能，
     * keepOpen： 00H:关闭，01H:打开
     */
    public static byte[] keepOpen(String encryNum, String password, String keepOpen) {
        //指令名称
        String cmdByte = "09";
        //
        String data = buildCmdData(encryNum, keepOpen);
        //加密密钥
        String intiKey = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, intiKey, data);
    }

    /**
     * 门锁静音功能，不响
     * 00H:关闭，01H:打开
     */
    public static byte[] setSilence(String encryNum, String password, String silenceType) {
        //指令名称
        String cmdByte = "0A";
        //
        String data = buildCmdData(encryNum, silenceType);
        //加密密钥
        String intiKey = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, intiKey, data);
    }


    /**
     * app删除指纹
     */
    public static byte[] getRecordCount(String encryNum, String password) {
        //指令名称
        String cmdByte = "0D";
        //
        String data = buildCmdData(encryNum, "");
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }


    /**
     * 获取
     */
    public static byte[] getRecordPage(String encryNum, String password, int recordPage) {
        //指令名称
        String cmdByte = "0E";
        //pwdNo
        String pageStr = "";
        if (recordPage < 10) {
            pageStr = "0" + recordPage;
        } else {
            pageStr = "" + recordPage;
        }
        //页数
        String data = buildCmdData(encryNum, pageStr);
        //加密密钥
        String intiKey = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, intiKey, data);
    }

    /**
     * 同步时间
     */
    public static byte[] syncTime(String encryNum, String password) {
        //指令名称
        String cmdByte = "0f";
        //8个ff
        //当前时间
        String cmdData = TimeUtils.getSyTime();
        //填充明文数据
        String data = buildCmdData(encryNum, cmdData);
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }

    /**
     * 查询指纹列表
     */
    public static byte[] getDevZWList(String encryNum, String password) {
        //指令名称
        String cmdByte = "10";
        //凑成16个byte，其他的不用是因为已经够16个byte了，
        String data = buildCmdData(encryNum, "");
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //加密序号
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }

    /**
     * 查询密码列表
     */
    public static byte[] getDevPwdList(String encryNum, String password) {
        //指令名称
        String cmdByte = "11";
        //凑成16个byte，其他的不用是因为已经够16个byte了，
        String data = buildCmdData(encryNum, "");
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //加密序号
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }


    /**
     * 清空锁内记录数量，
     * 00是清空数量，
     */
    public static byte[] cleanRecord(String encryNum, String password) {
        //指令名称
        String cmdByte = "1d";
        //凑成16个byte，其他的不用是因为已经够16个byte了，
        String data = buildCmdData(encryNum, "0000");
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //加密序号
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }

    /**
     * 加密序号
     * devNum 加密序号，4byte
     * key 加密密钥
     * oldPassword 8个ff
     * newPassword 新密码，自己生成的，
     */
    public static byte[] buildIntiPassword(String encryNum, String newPassword) {
        //指令名称
        String cmdByte = "24";
        //8个ff
        String data = "ffffffffffffffff" + keyAddZero(newPassword);
        //加密密钥
        String intiKey = getIntiEncryKey(encryNum);
        //
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, intiKey, data);
    }


    /**
     * 加密序号
     * devNum 加密序号，4byte
     * key 加密密钥
     * oldPassword 服务器返回的密码
     * newPassword 新密码，自己生成的，
     */
    public static byte[] changePassword(String encryNum, String oldPassword, String newPassword) {
        //指令名称
        String cmdByte = "24";
        //8个ff
        String data = keyAddZero(oldPassword) + keyAddZero(newPassword);
        //加密密钥
        String key = createEncryKey(oldPassword, encryNum);
        //加密序号
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }


    /**
     * 0x25
     * 验证开锁，
     * 数据是16个ff，发这个后锁就会返回来开锁指令
     */
    public static byte[] checkOpenLock(String encryNum, String password) {
        //指令名称
        String cmdByte = "25";
        //16个ff
        byte[] intiByte = new byte[16];
        Arrays.fill(intiByte, (byte) 0xff);
        String data = bytes2HexStr(intiByte);
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //加密序号
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }

    /**
     * 26
     * A1H：清除所有临时密码
     * B2H：清除所有临时密码和用户登记过的指纹/密码/卡片,管理员密码除外.
     * C3H：恢复出厂设置（等效于长按设置按键3秒）.
     */
    public static byte[] cleanSubPwdFingerCard(String encryNum, String password) {
        //指令名称
        String cmdByte = "26";
        //
        //凑成16个byte，其他的不用是因为已经够16个byte了，,b2指令删除除了管理员之外的用户设置的密码
        String data = buildCmdData(encryNum, "b2");
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //加密序号
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }

    /**
     * 0x26，，b2
     * A1H：清除所有临时密码
     * B2H：清除所有临时密码和用户登记过的指纹/密码/卡片,管理员密码除外.
     * C3H：恢复出厂设置（等效于长按设置按键3秒）.
     */
    public static byte[] resetDevice(String encryNum, String password) {
        //指令名称
        String cmdByte = "26";
        //
        //凑成16个byte，其他的不用是因为已经够16个byte了，
        String data = buildCmdData(encryNum, "c3");
        //加密密钥
        String key = createEncryKey(password, encryNum);
        //加密序号
        String headNum = getHeadNum(encryNum);
        //
        return buildEncryData(headNum, cmdByte, key, data);
    }
    //=======================加密密文结束=======================

    /**
     * 获取最后两位，做通讯序号
     * 0000000c-->0c
     */
    static String getHeadNum(String encryStr) {
        return encryStr.substring(encryStr.length() - 2);
    }

    /***
     * 16byte，填满ff，
     * 然后替换后面4byte，成通讯序号
     *
     */
    public static String buildCmdData(String encryStr, String data) {
        //16g
        StringBuilder ffStr = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            ffStr.append("ff");
        }
        //替换后面的
        ffStr.replace(32 - encryStr.length(), 32, encryStr);
        ffStr.replace(0, data.length(), data);
        return ffStr.toString();
    }


    /**
     * 加密序号
     */
    public static byte[] buildEncryData(String headNum, String cmd, String key, String data) {
        Logger.d("加密 key===" + key);
        //计算校验码,
        String mingwen = buildData(headNum, cmd, data);
        Logger.d("明文数据===" + mingwen);
        //加密数据,
        String encryData = encryData(key, data);
        Logger.d("加密后数据===" + encryData);
        //替换数据
        String result = replaceData(mingwen, encryData);
        Logger.d("最后结果===" + result);
        return hexStr2Bytes(result);
    }

    /**
     *
     */
    public static String replaceData(String toRe, String wanRe) {
        StringBuilder sb = new StringBuilder(toRe);
        sb.replace(4, 36, wanRe);
        return sb.toString();
    }


    /**
     * 12345678-->0102030405060708
     */
    public static String keyAddZero(String key) {
        String newKey = "";
        for (int i = 0; i < key.length(); i++) {
            newKey += "0" + key.substring(i, i + 1);
        }
        return newKey;
    }


    /**
     * encryNum 加密序号
     * key 加密key
     * 例子：
     * 04 03 02 01//加密序号
     * +
     * 00 01 00 02 00 03 00 04 00 05 00 06 00 07 00 08//加密密钥
     * =
     * 04 04 02 03 00 03 00 04 00 05 00 06 00 07 00 08
     */
    public static String createEncryKey(String key, String encryNum) {
        byte[] keyByte = fillTo16Byte(key);
        byte[] encryNumByte = hexStr2Bytes(encryNum);
        //反转数字
        encryNumByte = reverse(encryNumByte);
        for (int i = 0; i < encryNumByte.length; i++) {
            keyByte[i] = (byte) (keyByte[i] + encryNumByte[i]);
        }
        return bytes2HexStr(keyByte);
    }

    /**
     * 返回数据的解密不用加上序号
     */
    public static String createBackEncryKey(String key) {
        byte[] keyByte = fillTo16Byte(key);
        return bytes2HexStr(keyByte);
    }


    /**
     * key的长度是8，补0成16个
     * 1,2,3,4,5,6,7,8,-->00,01,00,02,00,03,00,04,00,05,00,06,00,07,00,08
     */
    public static byte[] fillTo16Byte(String key) {
        byte[] keyByte = new byte[16];
        byte[] oldPwdByte = hexStr2TowBytes(key);
        for (int i = 0; i < oldPwdByte.length; i++) {
            keyByte[(i * 2) + 1] = oldPwdByte[i];
        }
        return keyByte;
    }

    /**
     * 获取初始的加密密码，
     */
    public static String getIntiEncryKey(String encryNum) {
        //16个0xf
        byte[] intiByte = new byte[16];
        Arrays.fill(intiByte, (byte) 0x0f);

        byte[] encryNumByte = hexStr2Bytes(encryNum);
        //反转数字
        encryNumByte = reverse(encryNumByte);
        for (int i = 0; i < encryNumByte.length; i++) {
            intiByte[i] = (byte) (intiByte[i] + encryNumByte[i]);
        }
        return bytes2HexStr(intiByte);
    }


    /**
     * 实现数组元素的翻转
     */
    public static byte[] reverse(byte[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            byte temp = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = arr[i];
            arr[i] = temp;
        }
        return arr;
    }

    /**
     * 加密序号
     * key 加密密钥 16位
     * data 16byte的数据
     */
    public static String encryData(String key, String data) {
        String encryData = "";
        try {
            encryData = bytes2HexStr(encrypt(hexStr2Bytes(data), hexStr2Bytes(key)));
        } catch (Exception e) {
            Logger.d("==============" + e.getLocalizedMessage());
        }
        return encryData;
    }


    public static String dncryData(String key, String data) {
        String encryData = "";
        try {
            encryData = bytes2HexStr(Decrypt(hexStr2Bytes(data), hexStr2Bytes(key)));
        } catch (Exception e) {
            Logger.d("==============" + e.getLocalizedMessage());
        }
        return encryData;
    }

    /**
     * 加密
     * content 需要加密的内容
     * key 加密秘钥
     * return 返回加密后的内容
     * https://www.cnblogs.com/chen-lhx/p/5817161.html
     * 补码方式
     */
    public static byte[] encrypt(byte[] src, byte[] sKey) throws Exception {
        if (sKey == null) {
            Logger.d("========" + "Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length != 16) {
            Logger.d("========" + "Key长度不是16位");
            return null;
        }
        SecretKeySpec skeySpec = new SecretKeySpec(sKey, "AES");
        //"算法/模式/补码方式"
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(src);

        Logger.d("加密长度==" + encrypted.length);
        //加密的转化成16进制字符串，
        return encrypted;
    }

    // 解密
    public static byte[] Decrypt(byte[] src, byte[] sKey) throws Exception {
        // 判断Key是否正确
        if (sKey == null) {
            Logger.d("========" + "Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length != 16) {
            Logger.d("========" + "Key长度不是16位");
            return null;
        }
        SecretKeySpec skeySpec = new SecretKeySpec(sKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(src);
        return original;
    }

    //============================================================

    /**
     * 生成指令
     */
    public static String buildData(String head, String cmd, String data) {
        //校验码1
        String xor = xor(head, cmd, data);
        //校验码2
        String lrc = lrc(head, cmd, data);
        //
        StringBuilder sb = new StringBuilder();
        sb.append(head);
        sb.append(cmd);
        sb.append(data);
        sb.append(xor);
        sb.append(lrc);
        return sb.toString();
    }

    /**
     * 加起来，，转化成
     */
    private static String xor(String f1, String c1, String d1) {
        String xorstr = f1 + c1 + d1;
        byte b = 0;
        byte[] bytes = hexStr2Bytes(xorstr);
        //异或校验
        b = bytes[0];
        for (int i = 0; i < bytes.length - 1; i++) {
            b = (byte) (b ^ bytes[i + 1]);
        }
        return byteToHex(b);
    }


    private static String lrc(String f1, String c1, String d1) {
        String str = f1 + c1 + d1;
        byte b = 0;
        byte[] bytes = hexStr2Bytes(str);
        //和校验
        b = bytes[0];
        for (int i = 0; i < bytes.length - 1; i++) {
            b = (byte) (b + bytes[i + 1]);
        }
        return byteToHex(b);
    }

    /**
     * 字节转十六进制
     *
     * @param b 需要进行转换的byte字节
     * @return 转换后的Hex字符串
     * https://blog.csdn.net/qq_34763699/java/article/details/78650272
     */
    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }


    /**
     * 将字节数组转换为16进制字符串
     *
     * @param bytes
     * @return 01FE0835F1000000000000000000000000000000
     */
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
     * 2个字符转化成1个byte
     * 1112-》11，12
     */
    public static byte[] hexStr2Bytes(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    /**
     * 1个字符转化成一个byte
     * 1112-》01 01 01 02
     */
    public static byte[] hexStr2TowBytes(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length()];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(i, i + 1);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }


    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }


}
