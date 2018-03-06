package com.boyaa.stf.pmonitor.utils;

import android.net.TrafficStats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by JessicZeng on 2017/2/20.
 */
public class NetUtils {
    // 采集应用流量的方案
    private static final int TYPE_CASE1 = 1;
    private static final int TYPE_CASE2 = 2;
    private static final int TYPE_CASE3 = 3;
    private static int netCase = TYPE_CASE1;
    private static final float B2K = 1024.00f;

    private long p_t_cur = 0;
    private long p_r_cur = 0;
    private long p_t_base = 0;
    private long p_r_base = 0;

    private double p_t_add = 0.0;
    private double p_r_add = 0.0;
    private static String pName = "";

    public NetUtils() {
        pName = Config.getPkgName();
        initProcessNetValue();
    }

    public void initProcessNetValue() {

        p_t_base = getOutOctets();
        p_r_base = getInOctets();

        p_t_add = 0;
        p_r_add = 0;
    }

    /**
     * 根据进程id获取网络发送流量
     *
     * @return 字节数
     */
    public static long getOutOctets() {
        int uid = ProcessUtils.getProcessUID(pName);
        String netPath = "/proc/uid_stat/" + uid + "/tcp_snd";

        switch (netCase) {
            case TYPE_CASE1:
                File f = new File(netPath);
                if (!f.exists()) {
                    // 转方案2
                    netCase = TYPE_CASE2;
                } else {
                    String ret = "0";
                    try {
                        FileReader fr = new FileReader(netPath);
                        BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
                        ret = localBufferedReader.readLine();
                        FileUtil.closeReader(localBufferedReader);
                        return Long.parseLong(ret);
                    } catch (Exception e) {
                        netCase = TYPE_CASE2;
                    }

                    // 最后一个尝试
                    if ((ret == null || ret.equals("0"))
                            && (TrafficStats.getUidTxBytes(uid) > 0 || TrafficStats.getUidRxBytes(uid) > 0)) {
                        netCase = TYPE_CASE2;
                    }
                }

                // 如果方案1判断不支持，不需要break直接跳方案2
                // break;

            case TYPE_CASE2:
                long s = TrafficStats.getUidTxBytes(uid);
                if (s >= 0) {
                    return s;
                }
                netCase = TYPE_CASE3;

            case TYPE_CASE3:
            default:
                break;
        }
        return 0;
    }

    /**
     * 根据进程id获取网络接收流量
     *
     * @return 字节数
     */
    public static long getInOctets() {
        int uid = ProcessUtils.getProcessUID(pName);
        String netPath = "/proc/uid_stat/" + uid + "/tcp_rcv";

        switch (netCase) {
            case TYPE_CASE1:
                File f = new File(netPath);
                if (!f.exists()) {
                    // 转方案2
                    netCase = TYPE_CASE2;
                } else {
                    String ret = "0";
                    try {
                        FileReader fr = new FileReader(netPath);
                        BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
                        ret = localBufferedReader.readLine();
                        FileUtil.closeReader(localBufferedReader);
                        return Long.parseLong(ret);
                    } catch (Exception e) {
                        netCase = TYPE_CASE2;
                    }

                    // 最后一个尝试
                    if ((ret == null || ret.equals("0"))
                            && (TrafficStats.getUidTxBytes(uid) > 0 || TrafficStats.getUidRxBytes(uid) > 0)) {
                        netCase = TYPE_CASE2;
                    }
                }

                // 如果方案1判断不支持，不需要break直接跳方案2
                // break;

            case TYPE_CASE2:
                long r = TrafficStats.getUidRxBytes(uid);
                if (r >= 0) {
                    return r;
                }
                netCase = TYPE_CASE3;

            case TYPE_CASE3:
            default:
                break;
        }
        return 0;
    }

    public double[] getProcessNetValue() {
        double[] value = new double[3] ;

        p_t_cur = getOutOctets();
        p_r_cur = getInOctets();
        p_t_add = (p_t_cur - p_t_base) / B2K;
        p_r_add = (p_r_cur - p_r_base) / B2K;
        p_t_base = p_t_cur;
        p_r_base = p_r_cur;

        value[0] = p_t_add + p_r_add;
        value[1]= p_r_add;
        value[2]= p_t_add;

        return value;
    }

    public double getP_t_add() {
        return p_t_add;
    }

    public double getP_r_add() {
        return p_r_add;
    }


}
