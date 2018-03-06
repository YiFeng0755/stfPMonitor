package com.boyaa.stf.pmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.boyaa.stf.pmonitor.service.MonitorService;
import com.boyaa.stf.pmonitor.utils.Config;
import com.boyaa.stf.pmonitor.utils.FrameUtil;
import com.boyaa.stf.pmonitor.utils.ProcessUtils;

public class MainActivity extends AppCompatActivity {

    private static final int TIMEOUT = 20000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = getIntent();
        String pkg = i.getStringExtra("packageName");
        String filePath = i.getStringExtra("filePath");
        int delayTime = i.getIntExtra("delayTime",1000);
        Config.setPkgName(pkg);
        Config.setCsvFilePath(filePath);
        Config.setmContext(getApplicationContext());
        checkSu();
        if(waitForAppStart() != -1){
            //启动service
            Intent startIntent = new Intent(this, MonitorService.class);
            startIntent.putExtra("delayTime", delayTime);
            startService(startIntent);
        }
        else{
            Log.d(Config.LOG_TAG,"apk start fail");
        }

        finish();
    }

    private void checkSu() {
        Thread thread = new Thread(new CheckSuRunnable(), "CheckSu");
        thread.setDaemon(true);
        thread.start();
    }


    class CheckSuRunnable implements Runnable { // 无su，线程挂住
        @Override
        public void run() {
            Log.d("","checkSu CheckSuRunnable");
            FrameUtil.checkSu();
        }

    }


    /**
     * 等待被测应用启动完成
     * @return 被测应用pid
     */
    private int waitForAppStart(){
        Log.d(Config.LOG_TAG, "wait for app start");
        int pid = -1;
        long startTime = System.currentTimeMillis();
        String pName = Config.getPkgName();
        ProcessUtils.init();
        while (System.currentTimeMillis() < startTime + TIMEOUT) {
            pid = ProcessUtils.getProcessPID(pName);
            if (pid != -1) {
                Config.setPid(pid);
                break;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return pid;
    }
}
