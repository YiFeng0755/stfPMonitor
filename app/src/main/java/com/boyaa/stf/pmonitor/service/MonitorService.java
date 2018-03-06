package com.boyaa.stf.pmonitor.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.boyaa.stf.pmonitor.receiver.UIScriptReceiver;
import com.boyaa.stf.pmonitor.utils.Config;
import com.boyaa.stf.pmonitor.utils.CpuUtils;
import com.boyaa.stf.pmonitor.utils.FileUtil;
import com.boyaa.stf.pmonitor.utils.FrameUtil;
import com.boyaa.stf.pmonitor.utils.MemUtils;
import com.boyaa.stf.pmonitor.utils.NetUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by JessicZeng on 2017/8/21.
 * 性能采集后台服务
 */
public class MonitorService extends Service {

    public static BufferedWriter bw;
    public static FileOutputStream out;
    public static OutputStreamWriter osw;

    private CpuUtils cpuUtils;
    private NetUtils netUtils;
    private Thread thread;
    private UIScriptReceiver scriptReceiver;
    private Handler handler;
    private int pid = -1;
    private Boolean isServiceStop = false;
    //采集数据间隔
    private int delaytime = 1000;
    private final static String LOG_TAG = "monitor-" + MonitorService.class.getSimpleName();
    private DecimalFormat dataFormat;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "MonitorService onCreate");
        super.onCreate();
        dataFormat = new java.text.DecimalFormat("#.##");
        handler = new Handler();
        pid = Config.getPid();
        cpuUtils = new CpuUtils();
        netUtils = new NetUtils();
        checkSu();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "MonitorService onStartCommand");
        delaytime = intent.getIntExtra("delayTime", 1000);
        registBoradReceiver();
        createResultCsv();
        handler.postDelayed(task, delaytime);
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        Log.i(LOG_TAG, "MonitorService onDestroy");
        isServiceStop = true;
        handler.removeCallbacks(task);
        closeOpenedStream();
        unregisterReceiver(scriptReceiver);
        unregisterReceiver(mBR);
        super.onDestroy();
    }



    private void checkSu() {
        thread = new Thread(new CheckSuRunnable(), "CheckSu");
        thread.setDaemon(true);
        thread.start();
    }

    class CheckSuRunnable implements Runnable { // 无su，线程挂住
        @Override
        public void run() {
            FrameUtil.checkSu();
        }

    }


    private void closeOpenedStream() {
        try {
            if (bw != null) {
                bw.close();
            }
            if (osw != null) {
                osw.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建性能结果文件
     */
    private void createResultCsv() {
        File resutlFile = new File(Config.getCsvFilePath());
        FileUtil.deleteFile(resutlFile);
        try {
            resutlFile.createNewFile();
            out = new FileOutputStream(resutlFile);
            osw = new OutputStreamWriter(out, "UTF-8");
            bw = new BufferedWriter(osw);

            StringBuffer sb = new StringBuffer();
            //应用描述
            sb.append("性能测试结果");
            sb.append("\r\n");
            sb.append("packageName,");
            sb.append(Config.getPkgName());
            sb.append("\r\n");
            sb.append("begin Time:,");
            sb.append("\r\n");
            sb.append("end Time:,");
            sb.append("\r\n");
            sb.append("app start Time:,");
            sb.append("\r\n");
            sb.append("\r\n");
            sb.append("\r\n");
            sb.append("\r\n");

            //数据表头
            sb.append("time,tagName,pss(KB),cpu(%),FPS,net_total(KB),net_in(KB),net_out(KB)");
            sb.append("\r\n");

            //写入文件
            bw.write(sb.toString());

        } catch (IOException e) {
            Log.e("createResultCsv", e.toString());
            System.out.print(e);
        }
    }

    private Runnable task = new Runnable() {

        public void run() {
        if (!isServiceStop) {
            dataRefresh();
            handler.postDelayed(this, delaytime);
        } else {
            stopSelf();
        }
        }
    };

    /**
     * 刷新数据
     */
    private void dataRefresh() {
        try {
            String performanceData = getPerformanceData();
            if (performanceData != null && bw != null) {
                bw.write(performanceData + "\r\n");
                bw.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPerformanceData() {
        StringBuffer performanceDate = new StringBuffer();
        long totalPss = MemUtils.getTotalPss(Config.getmContext(), pid);
        double cpuUsage = cpuUtils.getProcessCpuUsage();
        int fps = FrameUtil.getFrame();
        double[] net_total = netUtils.getProcessNetValue();
        performanceDate.append(getCurrentTime() + ",");
        performanceDate.append(Config.getTagName() + ",");
        performanceDate.append(totalPss + ",");
        performanceDate.append(cpuUsage + ",");
        performanceDate.append(fps + ",");
        performanceDate.append(dataFormat.format(net_total[0]) + ",");
        performanceDate.append(dataFormat.format(net_total[1]) + ",");
        performanceDate.append(dataFormat.format(net_total[2]));
        Log.d("performaceMonitor- ","data:"+performanceDate.toString());
        return performanceDate.toString();
    }

    /**
     * 注册接收广播
     */
    private void registBoradReceiver() {
        scriptReceiver = new UIScriptReceiver();
        registerReceiver(scriptReceiver, new IntentFilter("com.boyaa.stf.UIScript"));

        IntentFilter myInterntFilter = new IntentFilter();
        myInterntFilter.addAction("com.boyaa.stf.stopService");
        registerReceiver(mBR, myInterntFilter);
    }

    private String getCurrentTime() {
        String time;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (Build.MODEL.equals("sdk") || Build.MODEL.equals("google_sdk")) {
            time = sformat.format(cal.getTime().getTime() + 8 * 60 * 60 * 1000);
        } else {
            time = sformat.format(cal.getTime().getTime());
        }
        return time;
    }

    private BroadcastReceiver mBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if ("com.boyaa.stf.stopService".equals(action)) {
                Log.d(Config.LOG_TAG,"get the stopService broadcast");
                isServiceStop = true;
                stopSelf();

            }
        }
    };
}
