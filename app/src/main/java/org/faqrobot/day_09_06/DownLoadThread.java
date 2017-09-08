package org.faqrobot.day_09_06;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.faqrobot.day_09_06.MainActivity.TAG;

/**
 * 这里大家得了解下 http 中这个 Range 这个字段的含义：用户请求头中，指定第一个字节的位置和最后一个字节的位置，
 * 如（Range：200-300）! 这样就可以指定下载文件的位置了呢！到了这里核心的部分已经说完了！
 */
public class DownLoadThread extends Thread {
    private String apkurl;//下载地址
    private long startpos;//起始地址
    private long endpos;//结束地址
    private long downpos;//已经下载的大小
    private String apkpath;//保存地址
    private long block;//每块大小
    private int threadid;//线程ID
    private boolean finish = false; // 是否已经下载完成
    private Context context;
    private Handler handler;
    HttpURLConnection http;

    public DownLoadThread(Context context, String apkurl, long startpos, long endpos,
                          long   downpos, String apkpath, long block, int threadid,Handler handler) {
        this.context = context;
        this.apkurl = apkurl;
        this.startpos = startpos;
        this.endpos = endpos;
        this.downpos = downpos;
        this.apkpath = apkpath;
        this.block = block;
        this.threadid = threadid;
        this.handler = handler;
    }

    public DownLoadThread() {
    }

    @Override
    public void run() {
        File file=null;
        FileOutputStream fout=null;
        InputStream in = null;
        if (downpos < block&&finish==false) {
            try {
                URL url = new URL(apkurl);
                http = (HttpURLConnection) url
                        .openConnection();
                http.setConnectTimeout(5 * 1000);
                http.setReadTimeout(5 * 1000);
                http.setRequestMethod("GET");
                http.setRequestProperty(
                        "Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg," +
                                " application/x-shockwave-flash, application/xaml+xml," +
                                " application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                                "  application/x-ms-application, application/vnd.ms-excel, " +
                                "application/vnd.ms-powerpoint,  application/msword, */*");
                http.setRequestProperty("Accept-Language", "zh-CN");
                http.setRequestProperty("Referer", url.toString());
                http.setRequestProperty("Charset", "UTF-8");
                http.setRequestProperty("Connection", "Keep-Alive");
                //起始进度的变化
                long startPos = startpos + downpos;
                // 设置获取实体数据的范围
                http.setRequestProperty("Range", "bytes=" + startPos + "-");
                long endPos = endpos;
                file=new File(apkpath);
                if (file.length()>0){
                    fout=new FileOutputStream(file,true);
                }else{
                    fout=new FileOutputStream(file);
                }
                byte[] bytes = new byte[2048];
                int len = 0;
                in = http.getInputStream();
                Log.e(TAG, "线程"+threadid+"开始下载" );
                while ((len = in.read(bytes, 0, bytes.length)) != -1) {
                    fout.write(bytes,0,len);
                    downpos += len;//已下载的大小
                    Log.e(TAG, "第"+threadid+"个线程已下载了进度为："+downpos);

                    //将进度更新到ui
                    Message m = handler.obtainMessage();
                    m.what = threadid;
                    m.obj =Integer.parseInt(String.valueOf(downpos/block*100));
                    m.sendToTarget();
                    if (downpos-block>0){
                        finish = true;
                        Log.e(threadid+"", "第"+threadid+"个线程下载数据完毕" );
                        Log.e(threadid+"", "第"+threadid+"个文件的大小为"+file.length());
                        http.disconnect();
                    }
                }
            } catch (Exception e) {
            } finally {
                closeIO(in,fout);
            }
        }
    }
//得到每条线程已经下载的大小
    public long getDownpos() {
        return downpos;
    }


    //是否下载完成
    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }


    public void setDownpos(long downpos) {
        this.downpos = downpos;
    }

    //关闭流
    public static void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
