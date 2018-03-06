package com.boyaa.stf.pmonitor.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JessicZeng on 2017/8/21.
 */
public class FrameUtil {
    private static long startTime = 0L;
    private static int lastFrameNum = 0;
    private static long testCount = 0;
    public static boolean hasSu = false;
    private static int pid = 0;

    private static Process fpsProcess;
    private static DataOutputStream os;
    private static BufferedReader ir;

    public FrameUtil(){
    }

    public static synchronized int getFrame(){
        //未root，直接返回避免无效的空判断
        if(!isHasSu()) return 0;
        long end = 0L;
        float realCostTime = 0.0F;
        int fpsResult = 0;
        end = System.nanoTime();
        if (testCount != 0) {
            realCostTime = (float) (end - startTime) / 1000000.0F;
        }

        startTime = System.nanoTime();
        if (testCount == 0) {
            try {
                lastFrameNum = getCurFrameNum();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int currentFrameNum = 0;
        try {
            currentFrameNum = getCurFrameNum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int FPS = currentFrameNum - lastFrameNum;
        if (realCostTime > 0.0F) {
            fpsResult = (int) (FPS * 1000 / realCostTime);
        }
        lastFrameNum = currentFrameNum;

        testCount += 1;
        return fpsResult;
    }


    public static synchronized int getCurFrameNum() throws IOException {
        String frameNumString = "";
        String getFps40 = "service call SurfaceFlinger 1013";
        int frameNum = 0;
        try{
            if (fpsProcess == null) {
                fpsProcess = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(fpsProcess.getOutputStream());
                ir = new BufferedReader(new InputStreamReader(fpsProcess.getInputStream()));
            }

            os.writeBytes(getFps40 + "\n");
            os.flush();

            String str = "";
            int index1 = 0;
            int index2 = 0;
            while ((str = ir.readLine()) != null) {
                if (str.indexOf("(") != -1) {
                    index1 = str.indexOf("(");
                    index2 = str.indexOf("  ");

                    frameNumString = str.substring(index1 + 1, index2);
                    break;
                }
            }
            if (!frameNumString.equals("")) {
                frameNum = Integer.parseInt(frameNumString, 16);
            } else {
                frameNum = 0;
            }
        }catch (IOException e){
            Log.d(Config.LOG_TAG,"get SurfaceFlinger 1013 fail，the return is 0");
        }finally {
            return frameNum;
        }


    }

    public static boolean isHasSu() {
        return hasSu;
    }

    public static void setHasSu(boolean hasSu) {
        FrameUtil.hasSu = hasSu;
    }

    public static void checkSu() {
        Log.d(Config.LOG_TAG,"FrameUtil setPid");
        setHasSu(false);
        try {
            ProcessBuilder execBuilder = null;
            if (pid == 0) {
                execBuilder = new ProcessBuilder("su", "-c", "ps");

                execBuilder.redirectErrorStream(true);

                Process exec = null;
                exec = execBuilder.start();
                InputStream is = exec.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    if (line.contains("surfaceflinger")) {
                        String regEx = "\\s[0-9][0-9]*\\s";
                        Pattern pat = Pattern.compile(regEx);
                        Matcher mat = pat.matcher(line);
                        if (mat.find()) {
                            String temp = mat.group();
                            temp = temp.replaceAll("\\s", "");
                            pid = Integer.parseInt(temp);
                        }
                        break;
                    }
                }
            }

            if (pid == 0) {
                if (ProcessUtils.getProcessPID("system_server") != -1) {
                    pid = ProcessUtils.getProcessPID("system_server");
                } else {
                    pid = ProcessUtils.getProcessPID("system");
                }

            }
            Log.d(Config.LOG_TAG,"FrameUtil get root success");
            setHasSu(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Config.LOG_TAG, "FrameUtil get root fail:"+e.getMessage());
            setHasSu(false);
        }

        Log.d(Config.LOG_TAG,"hasSu:"+hasSu);
    }

    /**
     * upgrade app to get root permission
     *
     * @return is root successfully
     */
    public static boolean upgradeRootPermission(String cmd) {
        Log.d(Config.LOG_TAG,"begin upgradeRootPermission");
        Process process = null;
        DataOutputStream os = null;
        try {
            //String cmd = "chmod 777 " + Config.getmContext().getPackageCodePath();
            process = Runtime.getRuntime().exec("su -c "); // 切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int existValue = process.waitFor();
            if (existValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.w("", "upgradeRootPermission exception=" + e.getMessage());
            Log.w("", "upgradeRootPermission exception=" + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
    }

}
