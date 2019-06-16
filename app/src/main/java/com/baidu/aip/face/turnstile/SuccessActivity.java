package com.baidu.aip.face.turnstile;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.baidu.aip.face.turnstile.DetectActivity;
import com.example.HiJogging.IBackService;
import com.example.HiJogging.R;

public class SuccessActivity extends AppCompatActivity {
    private Intent mServiceIntent;
    private IBackService iBackService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //实现页面跳转
//                startActivity(new Intent(getApplicationContext(), DetectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
//                return false;
                Intent intent=new Intent(SuccessActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return false;

            }
        }).sendEmptyMessageDelayed(0,2000);//表示延迟3秒发送任务
    }

}
