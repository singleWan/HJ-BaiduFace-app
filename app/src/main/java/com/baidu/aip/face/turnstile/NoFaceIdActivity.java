package com.baidu.aip.face.turnstile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.baidu.aip.face.turnstile.DetectActivity;
import com.example.HiJogging.IBackService;
import com.example.HiJogging.R;

public class NoFaceIdActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_no_face_id_activity);
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //实现页面跳转
                Intent intent=new Intent(NoFaceIdActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        }).sendEmptyMessageDelayed(0,5000);//表示延迟3秒发送任务
    }
}
