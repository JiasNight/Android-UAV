package com.jias.uav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final Intent it = new Intent();
        //设置跳转另一页面不再返回
        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        it.setClass(this,MainActivity.class);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                startActivity(it); //执行
            }
        };
        timer.schedule(task, 1000 * 3); //3秒后
    }
}
