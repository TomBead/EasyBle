package com.tt.easyble.sample;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static String getTimes(Date date) {
        //        Log.e(TAG,"当前时区:"+Util.getCurrentTimeZone().substring(0,Util.getCurrentTimeZone().length()-3));
//        format.setTimeZone(TimeZone.getTimeZone(Util.getCurrentTimeZone().substring(0,Util.getCurrentTimeZone().length()-3)));
//        Log.e("getTime()", "data: " + format.format(date));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static String getSelTimes(Date date, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return format.format(date);
    }

    public static String getSelTimes(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
        return format.format(date) + ":00:00";
    }

    public static String getemptyRoomTimes(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH时");
        return format.format(date);
    }

    /**
     * 获取当前年的前两位
     */
    public static String getCurrYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return String.valueOf(year).substring(0, 2);
    }


    public static String getSyTime() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
        String dateStr = format.format(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        //一周的第几天
        String[] weekDays = {"00", "01", "02", "03", "04", "05", "06"};
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return dateStr + weekDays[w];
    }


    /**
     * 服务器的时间->年月日时分 写入蓝牙
     * 2020-06-01 17:46:06->2006011746
     */
    public static String getTimeToBle(String time) {
        String str = time.replace("-", "").replace(":", "").replace(" ", "");
        str = str.substring(2, 12);
        return str;
    }

    public static Date timesToDate(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(time);
        } catch (ParseException e) {
            return new Date();
        }
    }

    /**
     *
     */
    public static Date getDateByStr(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.parse(time);
        } catch (ParseException e) {
            return new Date();
        }
    }


    /**
     *
     */
    public static boolean isAfterCurrTime(String time) {
        boolean isAfter;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(time);
            Date curr = new Date();
            isAfter = curr.before(date);
        } catch (ParseException e) {
            //无法解析的日期格式，
            isAfter = true;
        }
        return isAfter;
    }


    /**
     * formach 的值，类似这种"yyyy-MM-dd HH:mm:ss"
     */
    public static boolean isOverdue(String time) {
        boolean isAfter;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(time);
            Date curr = new Date();
            //当前时间比传入时间靠后，，过期
            isAfter = curr.after(date);
        } catch (ParseException e) {
            isAfter = true;
        }
        return isAfter;
    }


}
