package com.boyaa.stf.pmonitor.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 格式类
 * Created by JessicZeng on 2017/2/22.
 */
public class FormatUtils {

    /**
     * 获得系统时间
     *
     * @return
     */
    private static SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    public static String getSystemTime() {
        Date date = new Date();
        return simpleTimeFormat.format(date);
    }

    public static String getSystemTime(long date) {
        return simpleTimeFormat.format(new Date(date));
    }

    // 获取系统短日期时间
    private static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);

    public static String getSystemDateTime() {
        Date date = new Date();
        return simpleDateTimeFormat.format(date);
    }

    public static String getSystemDateTime(long date) {
        return simpleDateTimeFormat.format(new Date(date));
    }

    // GPS使用的日期格式
    private static SimpleDateFormat gpsDataFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static String getGpsSaveTime() {
        Date date = new Date();
        return gpsDataFormatter.format(date);
    }

    public static String getGpsSaveTime(long data) {
        return gpsDataFormatter.format(new Date(data));
    }

    public static String getGpsSaveTime(Date date) {
        return gpsDataFormatter.format(date);
    }

    // 供外部模块做保存操作时引用的日期格式转换器
    private static SimpleDateFormat saveFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public static String getSaveTime() {
        Date date = new Date();
        return saveFormatter.format(date);
    }

    public static String getSaveTime(long data) {
        return saveFormatter.format(new Date(data));
    }

    // 日期，到ms
    private static SimpleDateFormat saveDateMsFormatter = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);

    public static String getSaveDateMs() {
        Date date = new Date();
        return saveDateMsFormatter.format(date);
    }

    public static String getSaveDateMs(long data) {
        return saveDateMsFormatter.format(new Date(data));
    }

    // 日期，到s
    private static SimpleDateFormat saveDateFormatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);

    public static String getSaveDate() {
        Date date = new Date();
        return saveDateFormatter.format(date);
    }

    public static String getSaveDate(long data) {
        return saveDateFormatter.format(new Date(data));
    }

    // 日期，到日
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String getDate() {
        Date date = new Date();
        return dateFormatter.format(date);
    }

    public static String getDate(long data) {
        return dateFormatter.format(new Date(data));
    }

    /**
     * 判断是否是数字，支持负数
     * @param s
     * @return
     */
    public static boolean isNumeric(String s) {
        if (s == null || s.length() == 0)
        {
            return false;
        }
        int numStartPos = 0;
        if (s.charAt(0) == '-')
        {
            numStartPos = 1;
        }

        for (int i = s.length(); --i >= numStartPos;) {
            int chr = s.charAt(i);
            if ((chr < 48 || chr > 57) && chr != '.')
                return false;
        }
        return true;
    }

    /**
     * double 除法
     *
     * @param d1
     * @param d2
     * @param scale
     *            四舍五入 小数点位数
     * @return
     */
    public static double div(double d1, double d2, int scale) {
        // 当然在此之前，你要判断分母是否为0，
        // 为0你可以根据实际需求做相应的处理

        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        // return bd1.divide(bd2, scale,
        // BigDecimal.ROUND_HALF_UP).doubleValue();
        // 直接向下取整，保持和UI展示一致
        try {
            return bd1.divide(bd2, scale, BigDecimal.ROUND_DOWN).doubleValue();
        } catch (Exception e) {
            // 根据bugly观测，在进入GTOpMulPerfActivity页时有极小概率crash，故加上异常保护
            // @see http://bugly.qq.com/detail?app=900010910&pid=1&ii=46#stack
            e.printStackTrace();
            return 0;
        }

    }
}
