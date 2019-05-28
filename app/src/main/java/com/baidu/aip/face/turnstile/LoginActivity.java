package com.baidu.aip.face.turnstile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.HiJogging.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginActivity extends AppCompatActivity {
    private Socket socket = null;
    private EditText ediMsg = null;
    private PrintWriter out = null;
    private Button btnSend = null;
    private Button btnConnect=null;
    private OutputStream output=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    //最好每次建立一个连接之后，不使用时关闭,这里并没有关闭，只是连接和发送数据
                    //用花生壳域名测试：结果：成功
                    //152u48.iok.la:43948
                    //socket = new Socket("152u48.iok.la", 43948);
                    socket = new Socket("106.15.198.115", 22);
                    output = socket.getOutputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String s=reader.readLine();
                } catch (UnknownHostException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }).start();
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                senduserInfo();
                return false;
            }
        }).sendEmptyMessageDelayed(0,1000);//表示延迟3秒发送任务
    }
    public static String byte2HexStr(byte[] b)
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

    private void    senduserInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                out = new PrintWriter(output);
                out.write("Nihao");
                out.flush();
            }
        }).start();




    }

}
