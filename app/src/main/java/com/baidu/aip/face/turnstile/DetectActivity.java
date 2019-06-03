/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face.turnstile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.manager.DownloadManager;
import com.baidu.aip.ImageFrame;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.FaceFilter;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.face.camera.PermissionCallback;
import com.baidu.aip.face.turnstile.service.BackService;
import com.example.HiJogging.R;
import com.baidu.aip.face.turnstile.exception.FaceError;
import com.baidu.aip.face.turnstile.model.RegResult;
import com.baidu.aip.face.turnstile.utils.ImageUtil;
import com.baidu.aip.face.turnstile.utils.OnResultListener;
import com.baidu.idl.facesdk.FaceInfo;

import com.blankj.swipepanel.SwipePanel;
import com.example.HiJogging.Utils;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DetectActivity extends AppCompatActivity {
    int i=0;
    private TextView nameTextView;
    // 预览View;
    private PreviewView previewView;
    // textureView用于绘制人脸框等。
    private TextureView textureView;
    // 用于检测人脸。
    private FaceDetectManager faceDetectManager;

    DownloadManager manager = DownloadManager.getInstance(this);
    private static HttpURLConnection httpURLConnection = null;
    // 为了方便调式。
//    private ImageView testView;
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
    //广播接收器
    public class mBroadcastReceiver extends BroadcastReceiver {
        private String msg;
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
                        faceDetectManager.stop();
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

        //复写onReceive()方法
        // 接收到广播后，则自动调用该方法
        @Override
        public void onReceive(Context context, Intent intent) {
            //写入接收广播后的操作
            msg = intent.getStringExtra("msg");
            if (msg.equals("noTime")){
                faceDetectManager.stop();
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(DetectActivity.this,NotimeActivity.class);
                        startActivity(intent);
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,100);//表示延迟0.5发送任务
            }else if (msg.equals("InSuccess")){
                faceDetectManager.stop();
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(DetectActivity.this,SuccessActivity.class);
                        startActivity(intent);
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,100);//表示延迟0.5秒发送任务
            }else if (msg.equals("OutSuccess")){
                faceDetectManager.stop();
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(DetectActivity.this,OutSuccess.class);
                        startActivity(intent);
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,100);//表示延迟0.5秒发送任务
            }else if (msg.equals("Card")){
                faceDetectManager.stop();
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(DetectActivity.this,NotPayActivity.class);
                        startActivity(intent);
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,100);//表示延迟0.5秒发送任务
            }else if (msg.equals("Ctsm")){
                faceDetectManager.stop();
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(DetectActivity.this,ContactManagerActivity.class);
                        startActivity(intent);
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,100);//表示延迟0.5秒发送任务
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
                faceDetectManager.stop();
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        //实现页面跳转
                        Intent intent=new Intent(DetectActivity.this,NeterrorActivity.class);
                        startActivity(intent);
                        return false;
                    }
                }).sendEmptyMessageDelayed(0,100);//表示延迟0.5秒发送任务
            }
        }
    }
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detected);

        faceDetectManager = new FaceDetectManager(getApplicationContext());
//        testView = (ImageView) findViewById(R.id.test_view);
        nameTextView = (TextView) findViewById(R.id.name_text_view);
        previewView = (PreviewView) findViewById(R.id.preview_view);
        textureView = (TextureView) findViewById(R.id.texture_view);

        // 从系统相机获取图片帧。
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        cameraImageSource.getCameraControl().setPreferredPreviewSize(1280, 720);

        // 设置预览
        cameraImageSource.setPreviewView(previewView);
        // 设置图片源
        faceDetectManager.setImageSource(cameraImageSource);
        // 设置人脸过滤角度，角度越小，人脸越正，比对时分数越高
        faceDetectManager.getFaceFilter().setAngle(20);
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, FaceInfo[] infos, ImageFrame frame) {
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断
                final Bitmap bitmap =
                        Bitmap.createBitmap(frame.getArgb(), frame.getWidth(), frame.getHeight(), Bitmap.Config
                                .ARGB_8888);
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        testView.setImageBitmap(bitmap);
//                    }
//                });
                if (infos == null) {
                    // null表示，没有人脸。
                    showFrame(null);
                    shouldUpload = true;
                }
            }
        });
        // 人脸追踪回调。没有人脸时不会回调。
        faceDetectManager.setOnTrackListener(new FaceFilter.OnTrackListener() {
            @Override
            public void onTrack(FaceFilter.TrackedModel trackedModel) {
                showFrame(trackedModel);
                if (trackedModel.meetCriteria()) {
                        upload(trackedModel);
                }
            }
        });

        // 安卓6.0+ 运行时，权限回调。
        cameraImageSource.getCameraControl().setPermissionCallback(new PermissionCallback() {
            @Override
            public boolean onRequestPermission() {
                ActivityCompat.requestPermissions(DetectActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
                return true;
            }
        });

        textureView.setOpaque(false);

        // 不需要屏幕自动变黑。
        textureView.setKeepScreenOn(true);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            // previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            // previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机横屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }


        final SwipePanel swipePanel = new SwipePanel(this);
        swipePanel.setLeftEdgeSize(100);// 设置左侧触发阈值 100dp
        swipePanel.setLeftDrawable(R.drawable.base_back);// 设置左侧 icon
        swipePanel.wrapView(findViewById(R.id.camera_layout));// 设置嵌套在 rootLayout 外层
        swipePanel.setOnFullSwipeListener(new SwipePanel.OnFullSwipeListener() {// 设置完全划开松手后的监听
            @Override
            public void onFullSwipe(int direction) {
                Intent intent=new Intent(DetectActivity.this, RegisterActivity.class);
                startActivity(intent);
                swipePanel.close(direction);// 关闭
            }
        });

        setCameraType(cameraImageSource);
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        // TODO 选择使用前置摄像头
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);

        // TODO 选择使用后置摄像头
//         cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
//         previewView.setMirrored(false);

        // TODO 选择使用usb摄像头 如果不设置，人脸框会镜像，显示不准
        //  cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
        //  previewView.setMirrored(false);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开始检测
        faceDetectManager.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        // 结束检测。
        faceDetectManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceDetectManager.stop();
    }
    @Override
    protected void onPause() {
        super.onPause();

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

    // 屏幕上显示用户信息。
    private void showUserInfo(String userInfo, float score) {
        // 把userInfo和分数显示在屏幕上
        String text = String.format(Locale.ENGLISH, "%s  %.2f", userInfo, score);
        nameTextView.setText(text);
    }


    private boolean shouldUpload = true;

    // 上传一帧至服务器进行，人脸识别。
    private void upload(FaceFilter.TrackedModel model) {
        if (model.getEvent() != FaceFilter.Event.OnLeave) {
            if (!shouldUpload) {
                return;
            }
            shouldUpload = false;
            final Bitmap face = model.cropFace();
            try {
                final File file = File.createTempFile(UUID.randomUUID().toString() + "", ".jpg");
                // 人脸识别不需要整张图片。可以对人脸区别进行裁剪。减少流量消耗和，网络传输占用的时间消耗。
                ImageUtil.resize(face, file, 200, 200);
                APIService.getInstance().identify(new OnResultListener<RegResult>() {
                    @Override
                    public void onResult(RegResult result) {
                        if (file != null && file.exists()) {
                            file.delete();
                        }
                        if (result == null) {
                            return;
                        }

                        String res = result.getJsonRes();
                        double maxScore = 0;
                        String userId = "";
                        if (TextUtils.isEmpty(res)) {
                            return;
                        }
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(res);
                            JSONObject resObj = obj.optJSONObject("result");
                            if (resObj != null) {
                                JSONArray resArray = resObj.optJSONArray("user_list");
                                int size = resArray.length();
                                for (int i = 0; i < size; i++) {
                                    JSONObject s = (JSONObject) resArray.get(i);
                                    if (s != null) {
                                        double score = s.getDouble("score");
                                        if (score > maxScore) {
                                            maxScore = score;
                                            userId = s.getString("user_id");
                                            DemoApplication.getLogger().e(TAG , s.toString());
                                        }

                                    }
                                }

                            }

                        } catch (JSONException e) {
                            DemoApplication.getLogger().e(TAG , e.toString());
                            e.printStackTrace();
                        }

                        // 识别分数小于80，也可能是角度不好。可以选择重试。
                        if (maxScore < 80) {

                            showUserInfo("相似度： "  , (float)maxScore);
                            if (maxScore < 70) {
                                faceDetectManager.stop();
                                Intent intent = new Intent(DetectActivity.this, NoFaceIdActivity.class);
                                startActivity(intent);
                            }else {
                                shouldUpload = true;
                            }
                        }else{
                            if (isServiceExisted("com.baidu.aip.face.turnstile.service.BackService")){
                                DemoApplication.getLogger().i(TAG , "有后台服务");
                                showUserInfo("相似度： "  , (float)maxScore);
                                Intent intent = new Intent();
                                intent.setAction("com.service.Receive");
                                intent.putExtra("userId", userId);
                                sendBroadcast(intent);
                            }else{
                                DemoApplication.getLogger().i(TAG , "无后台服务");
                                startService();
                            }
                        }
                    }

                    @Override
                    public void onError(FaceError error) {
                        error.printStackTrace();
                        shouldUpload = true;
                        if (file != null && file.exists()) {
                            file.delete();
                        }
                    }
                }, file);
            } catch (IOException e) {
                DemoApplication.getLogger().e(TAG , e.toString());
                e.printStackTrace();
            }
        } else {
            shouldUpload = true;
        }
    }

    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(10);
    }

    RectF rectF = new RectF();

    /**
     * 绘制人脸框。
     *
     * @param model 追踪到的人脸
     */
    private void showFrame(FaceFilter.TrackedModel model) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            return;
        }
        // 清空canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (model != null) {
            model.getImageFrame().retain();
            rectF.set(model.getFaceRect());

            // 检测图片的坐标和显示的坐标不一样，需要转换。
            previewView.mapFromOriginalRect(rectF);
            if (model.meetCriteria()) {
                // 符合检测要求，绘制绿框
                paint.setColor(Color.GREEN);
            } else {
                // 不符合要求，绘制黄框
                paint.setColor(Color.YELLOW);

                String text = "请正视屏幕";
                float width = paint.measureText(text) + 50;
                float x = rectF.centerX() - width / 2;
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(text, x + 25, rectF.top - 20, paint);
                paint.setColor(Color.YELLOW);
            }
            paint.setStyle(Paint.Style.STROKE);
            // 绘制框
            canvas.drawRect(rectF, paint);
        }
        textureView.unlockCanvasAndPost(canvas);
    }
    private void Jump(String state) {
        if (state=="1")
        {

        }
        else{

        }
    }

}
