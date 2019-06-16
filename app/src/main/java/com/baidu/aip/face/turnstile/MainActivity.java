/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face.turnstile;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.manager.DownloadManager;
import com.baidu.aip.face.turnstile.service.BackService;
import com.baidu.aip.face.turnstile.service.NetworkStateService;
import com.blankj.swipepanel.SwipePanel;
import com.example.HiJogging.R;
import com.example.HiJogging.Utils;
import com.example.HiJogging.fileUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import cn.davidsu.library.ShadowConfig;
import cn.davidsu.library.ShadowHelper;

public class MainActivity extends Activity {
    private Button bt;
    private View view;

    DownloadManager manager = DownloadManager.getInstance(this);
    private static HttpURLConnection httpURLConnection = null;
    private Handler handler = new Handler();

    //数据

    String code = null;
    int httpCode;
    String version ;
    String describe;
    String downUrl;
    String TAG = "Detect";

    private boolean run = true;
    private boolean RUN = true;


    private void startUpdate3() {
        /*
         * 整个库允许配置的内容
         * 非必选
         */
        UpdateConfiguration configuration = new UpdateConfiguration()
                //输出错误日志
                .setEnableLog(true)
                //设置自定义的下载
                //.setHttpManager()
                //下载完成自动跳动安装页面
                .setJumpInstallPage(true)
                //设置对话框背景图片 (图片规范参照demo中的示例图)
                //.setDialogImage(R.drawable.ic_dialog)
                //设置按钮的颜色
                //.setDialogButtonColor(Color.parseColor("#E743DA"))
                //设置按钮的文字颜色
                .setDialogButtonTextColor(Color.WHITE)
                //支持断点下载
                .setBreakpointDownload(true)
                //设置是否显示通知栏进度
                .setShowNotification(true)
                //设置是否提示后台下载toast
                .setShowBgdToast(true)
                //设置强制更新
                .setForcedUpgrade(true);
        //设置对话框按钮的点击监听
//                    .setButtonClickListener(this)
        //设置下载过程的监听
//                    .setOnDownloadListener(this);

        manager.setApkName("Face.apk")
                .setApkUrl(downUrl)
                .setSmallIcon(R.drawable.icon)
                .setShowNewerToast(true)
                .setConfiguration(configuration)
                .setDownloadPath(Environment.getExternalStorageDirectory() + "/AppUpdate")
                .setApkVersionCode(9999)
                .setApkVersionName(version)
                .setApkSize("14.57")
                .setAuthorities(getPackageName())
                .setApkDescription(describe)
                .download();
    }
    Handler Httphandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle data = msg.getData();
            JSONObject jsonObject;
            JSONObject jsons = new JSONObject();

            //从data中拿出存的数据
            String val = data.getString("value");
            //将数据进行显示到界面等操作
            try {
                jsonObject = new JSONObject(val);
                jsons = jsonObject.getJSONObject("data");
                httpCode = jsonObject.getInt("code");
            } catch (JSONException e) {
                DemoApplication.getLogger().e(TAG , e.toString());
                e.printStackTrace();
            }
            if (httpCode == 200){
                try {
                    version = jsons.getString("version");
                    describe = jsons.getString("describe");
                    downUrl = jsons.getString("url");
                    DemoApplication.getLogger().i(TAG , version + describe + downUrl);

                } catch (JSONException e) {
                    DemoApplication.getLogger().e(TAG , e.toString());
                    e.printStackTrace();
                }
                if (Utils.compareVersion(version , code) > 0){
                    startUpdate3();
                }
            }
        }
    };
    Thread syncTasks = new Thread(new Runnable() {
        public String getHtml(String path) throws Exception {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("access-token", "3BDoQeb-naUZrO49*Mk_CYBaToqzqVo$");
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();
                byte[] data = Utils.read(in);
                String html = new String(data, "UTF-8");
                return html;
            }
            return null;
        }
        @Override
        public void run(){
            while (true) {
                try {
                    if (!RUN) {
                        Thread.sleep(Long.MAX_VALUE);
                    }
                } catch (Exception e) {
                    DemoApplication.getLogger().e(TAG , e.toString());
                    e.printStackTrace();
                }
                Message msg = Message.obtain();
                String html = "";
                try {
                    html = getHtml("https://api.shikegongxiang.com/faceapp/V1/currentVersion");
                } catch (Exception e) {
                    e.printStackTrace();
                    DemoApplication.getLogger().e(TAG , e.toString());
                }
                Bundle data = new Bundle();
                data.putString("value", html);
                msg.setData(data);
                Httphandler.sendMessage(msg);
                RUN = false;
            }

        }
    });
    public boolean isServiceExisted(String className) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = am.getRunningServices(Integer.MAX_VALUE);
        int myUid = android.os.Process.myUid();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : serviceList) {
            if (runningServiceInfo.uid == myUid && runningServiceInfo.service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
    private void startService() {
        Intent startIntent = new Intent(this,BackService.class);
        startService(startIntent);
    }
    /**
     * 去除屏幕锁
     */
    public void clearKeyguardLock() {
        android.app.KeyguardManager keyguardManager = (android.app.KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        android.app.KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();
    }
    //广播接收器
    public class mBroadcastReceiver extends BroadcastReceiver {
        private String msg;
        //复写onReceive()方法
        // 接收到广播后，则自动调用该方法
        @Override
        public void onReceive(Context context, Intent intent) {
            //写入接收广播后的操作
            msg = intent.getStringExtra("msg");
//            if (msg.equals("isLine")){
//                new Handler(new Handler.Callback() {
//                    @Override
//                    public boolean handleMessage(Message msg) {
//                        //实现页面跳转
//                        Intent intent=new Intent(MainActivity.this,DetectActivity.class);
//                        startActivity(intent);
//                        return false;
//                    }
//                }).sendEmptyMessageDelayed(0,3000);//表示延迟0.5发送任务
//            }else
            if (msg.equals("isConfig")){
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(MainActivity.this,RegisterActivity.class);
                        startActivity(intent);
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,3000);//表示延迟0.5发送任务
            }else if (msg.equals("Update")){
                PackageManager manager = context.getPackageManager();
                PackageInfo info = null;
                try {
                    info = manager.getPackageInfo(context.getPackageName(), 0);
                    code = info.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    DemoApplication.getLogger().i(TAG , e.toString());
                }
                if (run){
                    syncTasks.start();
                    run = false;
                }else {
                    RUN = true;
                    syncTasks.interrupt();
                }
            }else if (msg.equals("notLine")){
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(MainActivity.this,NeterrorActivity.class);
                        startActivity(intent);
                        finish();
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,100);//表示延迟0.5秒发送任务
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int[] mColor = new int[]{Color.parseColor("#62bcde")};
        int[] mShadowColor = new int[]{Color.parseColor("#5B2089B0")};
        bt = (Button) findViewById(R.id.run);
        ShadowConfig.Builder config = new ShadowConfig.Builder()
                .setColor(mColor[0])//View颜色
                .setShadowColor(mShadowColor[0])//阴影颜色
//                .setGradientColorArray(mColor)//如果View是渐变色，则设置color数组
                .setRadius(999)//圆角
                .setOffsetX(6)//横向偏移
                .setOffsetY(6);//纵向偏移

        ShadowHelper.setShadowBgForView(bt, config);
        //1.匿名内部类
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceExisted("com.baidu.aip.face.turnstile.service.BackService")){
                    DemoApplication.getLogger().i("Main" , "有后台服务");
                    //实现页面跳转
                    Intent intent=new Intent(MainActivity.this,DetectActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    DemoApplication.getLogger().i("Main" , "无后台服务");
                    startService();
                    //实现页面跳转
                    Intent intent=new Intent(MainActivity.this,DetectActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        view = (View) findViewById(R.id.view2);
        //1.匿名内部类
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceExisted("com.baidu.aip.face.turnstile.service.BackService")){
                    DemoApplication.getLogger().i("Main" , "有后台服务");
                    //实现页面跳转
                    Intent intent=new Intent(MainActivity.this,DetectActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    DemoApplication.getLogger().i("Main" , "无后台服务");
                    startService();
                    //实现页面跳转
                    Intent intent=new Intent(MainActivity.this,DetectActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 1. 实例化BroadcastReceiver子类 &  IntentFilter
        mBroadcastReceiver mBroadcastReceiver = new mBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();

        // 2. 设置接收广播的类型
        intentFilter.addAction("com.HiJogging.ENTER");

        // 3. 动态注册：调用Context的registerReceiver（）方法
        registerReceiver(mBroadcastReceiver, intentFilter);



        if (!isServiceExisted("com.baidu.aip.face.turnstile.service.BackService")){
            DemoApplication.getLogger().i("Main" , "无后台服务");
            clearKeyguardLock();
            startService();
        }
        if (!isServiceExisted("com.baidu.aip.face.turnstile.service.NetworkStateService")){
            Intent i=new Intent(this, NetworkStateService.class);
            startService(i);
        }

        final SwipePanel swipePanel = new SwipePanel(this);
        swipePanel.setLeftEdgeSize(100);// 设置左侧触发阈值 100dp
        swipePanel.setLeftDrawable(R.drawable.base_back);// 设置左侧 icon
        swipePanel.wrapView(findViewById(R.id.main_bg));// 设置嵌套在 rootLayout 外层
        swipePanel.setOnFullSwipeListener(new SwipePanel.OnFullSwipeListener() {// 设置完全划开松手后的监听
            @Override
            public void onFullSwipe(int direction) {
                Intent intent=new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                swipePanel.close(direction);// 关闭
            }
        });

    }

}
