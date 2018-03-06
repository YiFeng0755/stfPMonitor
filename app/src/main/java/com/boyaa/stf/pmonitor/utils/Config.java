package com.boyaa.stf.pmonitor.utils;

import android.content.Context;
import android.util.Log;

/**
 * Created by JessicZeng on 2017/8/21.
 * 各项参数配置文件
 */
public class Config {

    public static final int API = android.os.Build.VERSION.SDK_INT;

    //场景结束标志
    public static final String TAG_STOP = "stop";

    //场景开始标志
    public static final String TAG_START = "start";

    //logTag
    public static final String LOG_TAG = "performaceMonitor-";

    //场景名
    private static String tagName = "";

    //待测应用package
    private static String packageName = "com.boyaa.enginedlqp.maindevelop";

    //待测应用结果文件全路径
    private static String csvFilePath = "/mnt/sdcard/monitorResult.csv";

    //被测应用pid
    private static int testAPK_Pid = -1;

    private static Context mContext = null;

    public static void setTagName(String tname){
        tagName = tname;
    }

    public static String getTagName(){
        return tagName;
    }

    public static void setPkgName(String name){
        Log.d(Config.LOG_TAG,"设置的packageName："+name);
        if(name != null && name.equals("")) {
            packageName = name;
        }
    }

    public static String getPkgName(){
        return packageName;
    }

    public static void setCsvFilePath(String filePath){
        Log.d(Config.LOG_TAG,"设置的filePath："+filePath);
        if(filePath != null && !filePath.equals("")) {
            csvFilePath = filePath;
        }
    }

    public static String getCsvFilePath(){
        return csvFilePath;
    }

    public static void setmContext(Context con){
        mContext = con;
    }

    public static Context getmContext(){
        return mContext;
    }

    public static int getPid(){
        return testAPK_Pid;
    }

    public static void setPid(int pid){
        testAPK_Pid = pid;
    }

    /**
     * 启动命令：adb shell am start --user 0 -n com.boyaa.stf.pmonitor/.MainActivity --es 'packageName' com.boyaa.enginedlqp.maindevelop --es 'filePath' /mnt/sdcard/result.csv
     * 发送场景广播命令：adb shell am broadcast -a com.boyaa.stf.UIScript --es 'tagName' login --es 'tagType' stop
     * 停止服务命令：adb shell am broadcast -a com.boyaa.stf.stopService
     */

}
