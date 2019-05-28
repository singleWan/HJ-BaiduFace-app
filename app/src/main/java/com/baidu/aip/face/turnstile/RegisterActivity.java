/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face.turnstile;

import com.example.HiJogging.R;
import com.example.HiJogging.fileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.support.v7.app.AlertDialog;
import android.app.PendingIntent;
import android.app.AlarmManager;

import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    JSONObject object =new JSONObject();
    private String pwds = "haijiao666";

    private EditText IPEditText;
    private EditText PortEditText;
    private EditText doorIdEditText;
    private EditText inOrOutEditText;
    private EditText heartIntervalEditText;
    private EditText pwdEditText;

    private Button submitButton;
    private Context context;
    private Properties properties;


    private void setInput() throws IOException, JSONException {
        String config = "";
        JSONObject jsonObj = null;
        IPEditText = (EditText) findViewById(R.id.IP);
        PortEditText = (EditText) findViewById(R.id.Port);
        doorIdEditText = (EditText) findViewById(R.id.doorId);
        inOrOutEditText = (EditText) findViewById(R.id.inOrOut);
        heartIntervalEditText = (EditText) findViewById(R.id.heartInterval);

        config = fileUtils.getConfig();
        jsonObj = new JSONObject(config);

        IPEditText.setText(jsonObj.getString("IP"));
        PortEditText.setText(jsonObj.getString("Port"));
        doorIdEditText.setText(jsonObj.getString("doorId"));
        inOrOutEditText.setText(jsonObj.getString("InOrOut"));
        heartIntervalEditText.setText(jsonObj.getString("heartInterval"));
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        IPEditText = (EditText) findViewById(R.id.IP);
        PortEditText = (EditText) findViewById(R.id.Port);
        doorIdEditText = (EditText) findViewById(R.id.doorId);
        inOrOutEditText = (EditText) findViewById(R.id.inOrOut);
        heartIntervalEditText = (EditText) findViewById(R.id.heartInterval);
        pwdEditText = (EditText) findViewById(R.id.pwd);
        submitButton = (Button) findViewById(R.id.submit_btn);


        submitButton.setOnClickListener(this);

        try {
            setInput();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private  void tip(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改成功")
                .setMessage("配置修改成功，请自行重启APP.")
                .setPositiveButton("了解", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {

                    }
                })
                .show();
    }
    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (v == submitButton) {
            String IP = IPEditText.getText().toString();
            String Port = PortEditText.getText().toString();
            String doorId = doorIdEditText.getText().toString();
            String inOrOut = inOrOutEditText.getText().toString();
            String heartInterval = heartIntervalEditText.getText().toString();
            String pwd = pwdEditText.getText().toString();
            if (pwds.equals(pwd)){
                try {
                    object.put("IP",IP);
                    object.put("Port",Port);
                    object.put("doorId",doorId);
                    object.put("InOrOut",inOrOut);
                    object.put("heartInterval" , heartInterval);
                }catch (Exception e){

                }


                builder.setTitle("修改配置")
                        .setMessage("修改配置将重启APP，是否确认修改.")
                        .setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
                                fileUtils.saveConfig(object.toString());
                                tip();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {

                            }
                        })
                        .show();
            }else{
                builder.setTitle("密码错误")
                        .setMessage("请检查配置密码是否正确.")
                        .setPositiveButton("了解", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {

                            }
                        })
                        .show();
            }

        }
    }



    private Handler handler = new Handler(Looper.getMainLooper());
}
