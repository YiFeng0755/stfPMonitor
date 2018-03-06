package com.boyaa.stf.pmonitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.boyaa.stf.pmonitor.utils.Config;

/**
 * Created by JessicZeng on 2017/8/21.
 * 接收UI自动化脚本的广播信息
 */
public class UIScriptReceiver extends BroadcastReceiver {
    //Once boot completed,start server
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        String tagName = intent.getStringExtra("tagName");
        String type = intent.getStringExtra("tagType");
        if(type.equals(Config.TAG_START)){
            Config.setTagName(tagName);
        }
        else if(type.equals(Config.TAG_STOP)){
            Config.setTagName("");
        }
        System.out.println("测试广播收到更新场景命令:"+tagName + " " + type);
    }
}
