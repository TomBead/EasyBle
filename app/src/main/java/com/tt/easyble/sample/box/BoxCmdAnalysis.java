package com.tt.easyble.sample.box;


import com.tt.easyble.sample.TimeUtils;
import com.orhanobut.logger.Logger;

/**
 * 蓝牙指令解析
 */
public class BoxCmdAnalysis {


    /**
     * 获取加密序号
     * 00 + 00 + 硬件版本号（1byte）+ 固件版本号（3byte）+ 当前加密序号（4byte）+ 电池电量（3byte）+ 硬件配置（1byte）+ xor + lrc
     * 0000003130300000000002ac5000cf8f
     * 0000 00 313030 00000000 02ac50 00 cf8f
     */
    public static String getEncryNumStr(byte[] data) {
        byte[] nums = new byte[4];
        System.arraycopy(data, 6, nums, 0, 4);
        return bytes2HexStr(nums);
    }

    /**
     * 获取电量
     * 0000 0249 28 6373
     * 02ac50 ->50-> 转化成16进制
     */
    public static int getPower(byte[] data) {
        String b = byteToHex(data[4]);
        return Integer.parseInt(b, 16);
    }


    /**
     * 获取锁内的密码数量
     * 0100821a4d31d5a9e77040178ad0278926b50a28
     * ↓
     * 821a4d31d5a9e77040178ad0278926b5
     * ↓
     * 000b000000ffffff1111ffffffffffff
     * <p>
     * 00 0b000000ffffff   1111  ffffffffffff
     * 00 序号
     * 0b000000ffffff 时效
     * 1111 密码
     * ======================================
     * 指纹，
     */
    public static String[] getDevLocalPwd(String cmd, String password) {
        String data = cmd.substring(4, 36);
        Logger.d("======getDevLocalPwd: " + data);
        String key = BoxCmdBuilder.createBackEncryKey(password);
        String dncryData = BoxCmdBuilder.dncryData(key, data);
        Logger.d("======getDevLocalPwd dncry: " + dncryData);
        //
        String[] pwdData = new String[2];
        //密码序号
        pwdData[0] = dncryData.substring(0, 2);
        //6位密码，如果有f替换成空
        pwdData[1] = dncryData.substring(16, 22).replace("f", "");
        return pwdData;
    }


    /**
     * 1.解密
     * 2.解密后，前面两个 byte 是记录数量
     */
    public static int getRecordCount(String cmd, String password) {
        String data = cmd.substring(4, 36);
        Logger.d("======getRecordCount: " + data);
        String key = BoxCmdBuilder.createBackEncryKey(password);
        String dncryData = BoxCmdBuilder.dncryData(key, data);
        //
        Logger.d("======getRecordCount: " + dncryData);
        int count = Integer.parseInt(dncryData.substring(0, 4), 16);
        Logger.d("======getRecordCount: " + count);
        return count;
    }

    /**
     * 解析返回的记录，，
     * 操作方式（1byte）+ 开锁时间（6byte）+ 开锁 ID（1byte）
     * ========================================
     * 01200805180709ff00ffffffffffffff
     * 01 200805180709 ff 00ffffffffffffff
     * ======
     * 0320080517454600006938de8e6143ce
     * 03 200805174546 00 006938de8e6143ce
     * ======
     * 032008051745300100d964243f2180ef
     * 03 200805174530 01 00d964243f2180ef
     * <p>
     * 03 700101000314 00 005aa362d85b6005
     * =================================
     * 结束后还是有东西传过来，，不过是6个ff
     * ffffffffffffffff0027f1e72a362304
     */
//    public static UploadRecord getRecordData(String cmd, String password) {
//        String data = cmd.substring(4, 36);
//        Logger.d("======getRecordData: " + data);
//        String key = BoxCmdBuilder.createBackEncryKey(password);
//        String dncryData = BoxCmdBuilder.dncryData(key, data);
//        Logger.d("======dncry record : " + dncryData);
//
//        //01:APP开锁，02:密码开始，03：指纹开锁，04:临时密码开锁，05：撬门
//        String openType = dncryData.substring(1, 2);
//        //开锁时间
//        String time = dncryData.substring(2, 14);
//        //APP、临时密码、撬门开锁返回0，密码、指纹开锁返回ID。
//        String openid = dncryData.substring(14, 16);
//
//        Logger.d("======开锁类型:" + openType);
//        Logger.d("======开锁时间:" + time);
//        Logger.d("======开锁ID:" + openid);
//        //
//        UploadRecord uploadRecord = new UploadRecord();
//        uploadRecord.setDopentype(Integer.parseInt(openType, 16));
//        uploadRecord.setDopentime(time);
//        uploadRecord.setDopenid(openid);
//        return uploadRecord;
//    }

    /**
     * yyyy-MM-dd HH:mm:ss
     * 200805174530-->2020-08-05 17:45:30
     */
    public static String formatRecordTime(String timeStr) {
        String year = timeStr.substring(0, 2);
        String mouth = timeStr.substring(2, 4);
        String day = timeStr.substring(4, 6);
        String hour = timeStr.substring(6, 8);
        String min = timeStr.substring(8, 10);
        String second = timeStr.substring(10, 12);

        return TimeUtils.getCurrYear() + year + "-"
                + mouth + "-"
                + day + " "
                + hour + ":"
                + min + ":"
                + second;

    }


    /**
     * 58 00 36b79628c5d13d4aede367a9cf7c64e4 5b49
     * 开门类型(1byte) + 开门ID(1byte) + 用户ID(1byte) + 开门时间(5byte)
     * 03 00 02 2005669354 00adcb36f992ee75
     * <p>
     * 03 00 02 20 85 66 10 17 00 84363b39bce097
     * <p>
     * 返回的格式：如下
     * //
     * 开门类型+开门ID+用户ID+开门时间
     * 03+00+0+2020-06-18 16:32:39
     */
//    public static String getRecordData(String cmd, String key) {
//        String data = cmd.substring(4, 36);
//        data = BleCmdMaker.dncryData(key, data);
//        Logger.d("====getRecordData：  " + data);
//        String openType = data.substring(0, 2);
//        String openId = data.substring(2, 4);
//        //用户id转化成 0-256 的int
//        String userId = data.substring(4, 6);
//        //
//        String year = data.substring(6, 8);
//        String min = data.substring(14, 16);
//
//        //
//        byte[] decry = hexStr2Bytes(data);
//        String mouthBit = byteToBit(decry[4]);
//        String dayBit = byteToBit(decry[5]);
//        String hourBit = byteToBit(decry[6]);
//
//        String secBit = mouthBit.substring(0, 2)
//                + dayBit.substring(0, 2)
//                + hourBit.substring(0, 2);
//
//        byte miaoByte = bit2byte(secBit);
//        byte mouthByte = bit2byte("00" + mouthBit.substring(2));
//        byte dayByte = bit2byte("00" + dayBit.substring(2));
//        byte hourByte = bit2byte("00" + hourBit.substring(2));
//
//        String mouth = byteToHex(mouthByte);
//
//        String day = byteToHex(dayByte);
//        String hour = byteToHex(hourByte);
//        String miao = byteToHex(miaoByte);
//
//        StringBuilder sb = new StringBuilder();
//        String yearStr = TimeUtils.getCurrYear() + year;
//        int userNumId = Integer.parseInt(userId, 16);
//        sb.append(openType).append("+").append(openId).append("+").append(userNumId)
//                .append("+")
//                .append(yearStr)
//                .append("-")
//                .append(mouth).append("-")
//                .append(day)
//                .append(" ")
//                .append(hour).append(":")
//                .append(min).append(":")
//                .append(miao);
//        return sb.toString();
//    }


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
     * 把byte转为字符串的bit
     */
    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1)
                + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1)
                + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1)
                + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1)
                + (byte) ((b >> 0) & 0x1);
    }


    /**
     *
     */
    public static byte bit2byte(String bString) {
        byte result = 0;
        for (int i = bString.length() - 1, j = 0; i >= 0; i--, j++) {
            result += (Byte.parseByte(bString.charAt(i) + "") * Math.pow(2, j));
        }
        return result;
    }


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
}
