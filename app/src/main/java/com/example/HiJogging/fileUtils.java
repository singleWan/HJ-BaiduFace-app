package com.example.HiJogging;

import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class fileUtils {
    private static String filePath = Environment.getExternalStorageDirectory().getPath();
    private static String fileName = "config.txt";
    public static void saveConfig (String config) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            //如果文件存在则删除文件
            file = new File(filePath, fileName);
            if(file.exists()){
                file.delete();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            //把需要保存的文件保存到SD卡中
            bos.write(config.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
    public static String getConfig() throws IOException {
        StringBuilder sb = new StringBuilder("");
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String filename = filePath + "/" + fileName;
            //打开文件输入流
            FileInputStream input = new FileInputStream(filename);
            byte[] temp = new byte[1024];

            int len = 0;
            //读取文件内容:
            while ((len = input.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
            //关闭输入流
            input.close();
        }
        return sb.toString();
    }
}
