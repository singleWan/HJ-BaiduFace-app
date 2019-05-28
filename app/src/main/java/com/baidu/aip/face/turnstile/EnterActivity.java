package com.baidu.aip.face.turnstile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.HiJogging.R;

public class EnterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //实现页面跳转
                startActivity(new Intent(getApplicationContext(),DetectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                return false;
            }
        }).sendEmptyMessageDelayed(0,2000);//表示延迟3秒发送任务

    }
}
