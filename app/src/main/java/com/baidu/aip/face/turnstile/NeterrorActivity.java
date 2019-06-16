/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face.turnstile;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.HiJogging.R;


public class NeterrorActivity extends AppCompatActivity {

    private IntentFilter intentFilter;
    //广播接收器
    public class mBroadcastReceiver extends BroadcastReceiver {
        private String msg;
        //复写onReceive()方法
        // 接收到广播后，则自动调用该方法
        @Override
        public void onReceive(Context context, Intent intent) {
            //写入接收广播后的操作
            msg = intent.getStringExtra("msg");
            if (msg.equals("isLine")){
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(NeterrorActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,100);//表示延迟0.5发送任务
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neterror);
    }
    //动态注册广播
    @Override
    protected void onResume(){
        super.onResume();

        // 1. 实例化BroadcastReceiver子类 &  IntentFilter
        mBroadcastReceiver mBroadcastReceiver = new mBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();

        // 2. 设置接收广播的类型
        intentFilter.addAction("com.HiJogging.ENTER");

        // 3. 动态注册：调用Context的registerReceiver（）方法
        registerReceiver(mBroadcastReceiver, intentFilter);
    }
}
