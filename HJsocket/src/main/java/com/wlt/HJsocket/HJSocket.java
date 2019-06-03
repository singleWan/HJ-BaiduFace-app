package com.wlt.HJsocket;

import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HJSocket {

    private Builder mBuilder;

    //设置IP
    private String ip;
    //设置端口
    private int port;
    private Callback callback;


    private Socket mSocket;
    private InputStream is = null;
    private InputStreamReader isr = null;
    private BufferedReader br = null;
    private OutputStream os = null;

    public Boolean isConnected = false;
    private Thread mThread;
    private byte[] buffer = new byte[1024];

    private boolean isLine = false;

    private Thread watchThread = null;

    private Boolean isAutoConnect = true;


    //是否开启心跳监测。开启心跳功能后，每隔一段间隔发送心跳包
    private static Boolean needHeart = true;
    //心跳包发送间隔
    private static long heartInterval = 10000;
    //设置门店ID
    private static int DoorId = 7;
    //设置进出门
    private static boolean ifIn = true;
    //最长等待服务器回应时间
    private long maxHeartTime = 5000;

    //最后的发送时间
    private long last_send_time = 0;

    //最后的接收时间
    private long last_rec_time = 0;

    public static byte[] generateMsg (int instruction) {
        byte[] ins = new byte[4];
        byte[] variable = new byte[4];
        switch (instruction){
            case 1:
                ins = ByteBuffer.allocate(4).putInt(instruction).array();
                if (ifIn) variable = ByteBuffer.allocate(4).putInt(1).array();
                else variable = ByteBuffer.allocate(4).putInt(2).array();
                break;
            case 5:
                ins = ByteBuffer.allocate(4).putInt(instruction).array();
                variable = ByteBuffer.allocate(4).putInt(0).array();
                break;
            case 6:
                ins = ByteBuffer.allocate(4).putInt(instruction).array();
                variable = ByteBuffer.allocate(4).putInt(0).array();
                break;
            default:
                if (ifIn){
                    ins = ByteBuffer.allocate(4).putInt(3).array();
                    variable = ByteBuffer.allocate(4).putInt(instruction).array();
                }else{
                    ins = ByteBuffer.allocate(4).putInt(4).array();
                    variable = ByteBuffer.allocate(4).putInt(instruction).array();
                }
                break;
        }
        //指令集
        byte[] msgS = new byte[8];
        //fe 02 00 03 01 00 01 fe
        byte[] msgDoorId = ByteBuffer.allocate(4).putInt(DoorId).array();
        msgS[0] = (byte) 0xFE;
        msgS[1] = (byte) 02;
        msgS[2] = msgDoorId[2];
        msgS[3] = msgDoorId[3];
        msgS[4] = ins[3];
        msgS[5] = variable[2];
        msgS[6] = variable[3];
        msgS[7] = (byte) 0xFE;
        return msgS;
    }
    public static byte[] downMsg (int instruction , int variable) {
        byte[] ins = new byte[4];
        byte[] var = new byte[4];
        ins = ByteBuffer.allocate(4).putInt(instruction).array();
        var = ByteBuffer.allocate(4).putInt(variable).array();
        //指令集
        byte[] msgS = new byte[8];
        //fe 02 00 03 01 00 01 fe
        byte[] msgDoorId = ByteBuffer.allocate(4).putInt(DoorId).array();
        msgS[0] = (byte) 0xFC;
        msgS[1] = (byte) 02;
        msgS[2] = msgDoorId[2];
        msgS[3] = msgDoorId[3];
        msgS[4] = ins[3];
        msgS[5] = var[2];
        msgS[6] = var[3];
        msgS[7] = (byte) 0xFC;
        return msgS;
    }

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    public HJSocket(Builder builder) {
        this.mBuilder = builder;
        this.ip = builder.ip;
        this.port = builder.port;
        this.callback = builder.callback;
    }



    public void connect() {
        disconnectSocketIfNecessary();

        if (Thread.currentThread()==Looper.getMainLooper().getThread()) {

            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    realconnect();
                }
            });

        }else {
            realconnect();
        }

        //连接了socket之后，才创建监听进程。
        openWatchThread();

    }

    private void realconnect() {
        try {

            mSocket = new Socket(ip,port);

            Boolean isConnect = mSocket.isConnected();

            if (isConnect) {

                is = mSocket.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);

                os = mSocket.getOutputStream();

                isConnected = true;
                isLine = true;
                callback.onConnected();
                //创建监听线程
                openThread();



            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            callback.onError("创建连接错误");
        }


    }


    private void disconnectSocketIfNecessary() {
        try {
            if (mSocket!=null) {
                isConnected = false;
                isLine = false;
                closeThread();

                if (!mSocket.isClosed()) {
                    if(!mSocket.isInputShutdown()){
                        mSocket.shutdownInput();
                    }
                    if (!mSocket.isOutputShutdown()) {
                        mSocket.shutdownOutput();
                    }

                    if (br!=null) {
                        br.close();
                        br=null;
                    }
                    if (isr!=null) {
                        isr.close();
                        isr=null;
                    }
                    if (is!=null) {
                        is.close();
                        is=null;
                    }
                    if (os!=null) {
                        os.close();
                        os=null;
                    }

                    mSocket.close();
                }
                mSocket = null;

                callback.onDisconnected();
//                Log.e(TAG, "onDisconnected");
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            callback.onError("断开连接异常");
        }
    }


    public void disconnect(){
        disconnectSocketIfNecessary();
        closeWatchThread();
    }

    private void closeThread()
    {
        if (mThread!=null) {
            isConnected = false;
            isLine = false;
            mThread.interrupt();
            mThread = null;
//            Log.e(TAG, "close thread");
        }
    }

    private void closeWatchThread()
    {
        if (watchThread!=null) {
            isAutoConnect = false;
            watchThread.interrupt();
            watchThread = null;
//            Log.e(TAG, "close watchThread");
        }
    }

    private void openThread()
    {
        closeThread();
        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isConnected) {
                    try {
                        int readLen=0;

                        readLen = is.read(buffer);
                        if (readLen>0) {
                            byte[] data = new byte[readLen];
                            System.arraycopy(buffer, 0, data, 0, readLen);

                            callback.onReceived(byte2HexStr(data));

                            if (needHeart){
                                last_rec_time = System.currentTimeMillis();
                            }
                        }

                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                        callback.onError("notLine");
                    }

                }
            }
        });
        mThread.start();
    }

    private void openWatchThread()
    {
        //closeWatchThread();
        if (watchThread!=null) {
            return;
        }
        watchThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isAutoConnect) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                    try {


                        if (needHeart) {
                            if (!isLine){
                                Thread.sleep(10000);
                            }
                            if (System.currentTimeMillis() - last_send_time > heartInterval){
                                realsend(HJSocket.generateMsg(1) , "heart");
                            }


                            //若当前发送的时间比上次接收的时间新，而且间隔maxHearTime没有收到应答
                            if(last_send_time > last_rec_time && System.currentTimeMillis() - last_send_time>maxHeartTime){
                                isConnected = false;
                                isLine = false;
                            }
                        }


                        if (isConnected) {

                        }else {
                            //未连接的情况下，重新连接服务器
                            callback.onReconnected();
                            disconnectSocketIfNecessary();
                            realconnect();
                        }



                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                        callback.onError("发送心跳错误");
                    }

                }
            }
        });
        watchThread.start();
    }


    /**
     * 发送命令
     * @param msg  信息
     */
    public void send(final byte[] msg)
    {

        if (Thread.currentThread()==Looper.getMainLooper().getThread()) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    realsend(msg,"send");
                }
            }).start();
        }else {
            realsend(msg,"send");
        }

    }

    public static   String byte2HexStr(byte[] b)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
        }
        return sb.toString().toUpperCase().trim().substring(0,16);
    }

    private void realsend(byte[] msg , String type) {

        try {
            os.write(msg);
            os.flush();

            if (needHeart) {
                last_send_time = System.currentTimeMillis();
            }

            if (mSocket.isInputShutdown()||mSocket.isOutputShutdown()) {
                isConnected = false;
                isLine = false;
            }

            if (type.equals("send")){
                callback.onSend();
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            if (type.equals("send")){
                callback.onError("发送失败");
            }else if (type.equals("heart")){
                isConnected = false;
                isLine = false;
                callback.onError("心跳发送失败");
            }
        }
    }




    /**
     * 配置构造器
     */
    public static class Builder{

        private String ip;
        private int port;
        private Callback callback;

        private long maxHeartTime = 5000;
        private int DoorId = 7 ;
        private Boolean needHeart = false;

        public String getIp() {
            return ip;
        }

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public int getPort() {
            return port;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Callback getCallback() {
            return callback;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public long getMaxHeartTime() {
            return maxHeartTime;
        }

        public Builder setMaxHeartTime(long maxHeartTime) {
            this.maxHeartTime = maxHeartTime;
            return this;
        }

        public Boolean getNeedHeart() {
            return needHeart;
        }

        public Builder setNeedHeart(Boolean needHeart) {
            HJSocket.needHeart = needHeart;
            return this;
        }
        //设置心跳间隔时间
        public Builder setHeartInterval(int heartInterval) {
            HJSocket.heartInterval = heartInterval;
            return this;
        }
        //设置门店ID
        public Builder setDoorId(int Id) {
            HJSocket.DoorId = Id;
            return this;
        }
        //设置进出们
        public Builder setIfIn(int ifIn){
            if (ifIn == 0){
                HJSocket.ifIn = true;
            }else {
                HJSocket.ifIn = false;
            }

            return this;
        }

        public HJSocket build()
        {

            if (this.ip == null) {
                throw new IllegalStateException("ip == null");
            } else if (this.port == 0) {
                throw new IllegalStateException("port == 0");
            } else {
                return new HJSocket(this);
            }
        }
    }






}
