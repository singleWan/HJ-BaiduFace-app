package com.baidu.aip.face.turnstile.service;

import com.example.HiJogging.fileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.content.IntentFilter;

import android.util.Log;

import com.wlt.HJsocket.Callback;
import com.wlt.HJsocket.HJSocket;

import java.io.File;
import java.io.IOException;

public class BackService extends Service {
    HJSocket socket;
    public static final String TAG = "MyService";
    private AutoExceptMsgReceiver autoExcepMstReceiver;
    private static String filePath = Environment.getExternalStorageDirectory().getPath();
    private static String fileName = "config.txt";


    String IP;
    int Port;
    int doorId;
    int InOrOut;
    int heartInterval;


    private void setConfig() throws IOException, JSONException {
        String config = "";
        File f = null;
        JSONObject jsonObj = null;
        try{
            f=new File(filePath , fileName);
            if(!f.exists()){
                Intent intent = new Intent();
                intent.setAction("com.HiJogging.ENTER");
                intent.putExtra("msg", "isConfig");
                sendBroadcast(intent);
            }else{
                config = fileUtils.getConfig();
                jsonObj = new JSONObject(config);
                IP = jsonObj.getString("IP");
                Port = jsonObj.getInt("Port");
                doorId = jsonObj.getInt("doorId");
                InOrOut = jsonObj.getInt("InOrOut");
                heartInterval = jsonObj.getInt("heartInterval");

                HJSocket.Builder builder = new HJSocket.Builder();
                socket = builder.setIp(IP)
                        .setPort(Port)
                        .setNeedHeart(true)    //是否心跳
                        .setDoorId(doorId)          //设置门店ID
                        .setIfIn(InOrOut)         //设置进出门 （0进门 1出门）
                        .setMaxHeartTime(5000) //默认为5000
                        .setHeartInterval(heartInterval) //设置心跳间隔
                        .setCallback(new Callback() {
                            @Override
                            public void onConnected() {
                                Intent intent = new Intent();
                                intent.setAction("com.HiJogging.ENTER");
                                intent.putExtra("msg", "isLine");
                                sendBroadcast(intent);
                            }

                            @Override
                            public void onDisconnected() {
//                        socket.connect();
                            }

                            @Override
                            public void onReconnected() {

                            }

                            @Override
                            public void onSend() {

                            }

                            @Override
                            public void onReceived(String msg) {
                                Intent intent = new Intent();
                                intent.setAction("com.HiJogging.ENTER");
                                if (msg.equals(HJSocket.byte2HexStr(HJSocket.downMsg(1,0)))){
                                    //发送广播的数据
                                    intent.putExtra("msg", "心跳");
                                }else if (msg.equals(HJSocket.byte2HexStr(HJSocket.downMsg(3,2)))){
                                    intent.putExtra("msg", "noTime");
                                }else if (msg.equals(HJSocket.byte2HexStr(HJSocket.downMsg(4,0)))){
                                    if (InOrOut == 0){
                                        intent.putExtra("msg", "InSuccess");
                                    }else{
                                        intent.putExtra("msg", "OutSuccess");
                                    }
                                }else if (msg.equals(HJSocket.byte2HexStr(HJSocket.downMsg(3,1)))){
                                    intent.putExtra("msg", "Card");
                                }else if (msg.equals(HJSocket.byte2HexStr(HJSocket.downMsg(3,3)))){
                                    intent.putExtra("msg", "Ctsm");
                                }else if (msg.equals(HJSocket.byte2HexStr(HJSocket.downMsg(5,0)))){
                                    intent.putExtra("msg", "Update");
                                }else{
                                    intent.putExtra("msg", msg);
                                }
                                sendBroadcast(intent);

                            }

                            @Override
                            public void onError(String msg) {
                                Log.e("Socket" , msg);
                                if (msg.equals("notLine")){
                                    Intent intent = new Intent();
                                    intent.setAction("com.HiJogging.ENTER");
                                    intent.putExtra("msg", "notLine");
                                    sendBroadcast(intent);
                                }
                            }

                        }).build();

                //注册接收异常广播消息
                autoExcepMstReceiver = new AutoExceptMsgReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction("com.service.Receive");

                registerReceiver(autoExcepMstReceiver, filter);

                socket.connect();
            }
        }catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction("com.HiJogging.ENTER");
            intent.putExtra("msg", "isConfig");
            sendBroadcast(intent);
        }
    }
    //广播接收异常消息
    private class AutoExceptMsgReceiver extends BroadcastReceiver {
        private int UserId;
        @Override
        public void onReceive(Context context, Intent intent) {
            UserId = Integer.parseInt(intent.getStringExtra("userId"));
            if (intent != null) {
                socket.send(HJSocket.generateMsg(UserId));
            }
        }
    }

    //创建服务时调用
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            setConfig();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    //服务执行的操作
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    //销毁服务时调用
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
