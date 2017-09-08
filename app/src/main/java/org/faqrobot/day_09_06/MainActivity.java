package org.faqrobot.day_09_06;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// TODO: 2017/9/7
// TODO: 2017/9/7
// TODO: 2017/9/7
/**多线程下载*/
/**断点续传*/
/**进程间通信——让其他进程后台下载文件*/
public class MainActivity extends AppCompatActivity {

    ProgressBar pro_one;
    ProgressBar pro_two;
    ProgressBar pro_three;
    ProgressBar pro_four;
    private long filesize;
    /**图片的路径*/
//    public static final String THE_FILE_URL = "http://robotcdn.infinitus.com.cn/daping/chongqing/chongqing5.jpg";
    /**视频的路径*/
    public static final String THE_FILE_URL = "http://ouf0tawxw.bkt.clouddn.com/video.mp4";

    private  static String BASE_URL=null;
    /**预期使用的线程个数*/
    public static final int THREAD_SIZE=3;
    private static int true_thread_size;
    public static final String TAG="————Mainactivity————";
    long download_file_thread_size ;  //    每个线程下载文件的大小
    private List<DownLoadInfo> infos = new ArrayList<>();
    Timer timer_has_giveduty_ok;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "handler0调用一次————————————————————————————————————————" );
                            pro_one.setProgress((Integer)(msg.obj) );
                        }
                    });
                break;
                case 1:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "handler1调用一次————————————————————————————————————————");
                                pro_two.setProgress((Integer) (msg.obj));
                            }
                        });
                break;
                case 2:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "handler2调用一次————————————————————————————————————————");
                                pro_three.setProgress((Integer) (msg.obj));
                            }
                        });
                    break;
                case 3:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "handler3调用一次————————————————————————————————————————");
                                pro_four.setProgress((Integer) (msg.obj));
                            }
                        });
                break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取图片存储的路径
//        BASE_URL=getExternalCacheDirectory(this,Environment.DIRECTORY_PICTURES).getPath();
        //获取视频存储的路径
        BASE_URL=getExternalCacheDirectory(this,Environment.DIRECTORY_MOVIES).getPath();
        pro_one = (ProgressBar) findViewById(R.id.pro_one);
        pro_two = (ProgressBar) findViewById(R.id.pro_two);
        pro_three = (ProgressBar) findViewById(R.id.pro_three);
        pro_four = (ProgressBar) findViewById(R.id.pro_four);

        findViewById(R.id.bt_download_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //线程获取每个线程下载文件大小——并将线程的任务放入集合
                attain_file_size(THE_FILE_URL);
            }
        });
        //线程判断是否分配任务OK
        timer_has_giveduty_ok = new Timer();
        timer_has_giveduty_ok.schedule(new TimerTask() {
            @Override
            public void run() {
                if(infos.size()>0){
                    timer_has_giveduty_ok.cancel();
                    for (int i = 0; i < infos.size(); i++) {
                        DownLoadThread downloadThread = new DownLoadThread(
                                MainActivity.this,THE_FILE_URL,infos.get(i).getStartpos(),
                                infos.get(i).getEndpos(),infos.get(i).getDownpos(),BASE_URL+"/"+i+".jpg",
                                download_file_thread_size,infos.get(i).getThreadid(),handler
                                );
                        downloadThread.start();
                    }
                }
            }
        },100,100);
    }

    //获取所需文件大小_和每个线程下载文件的大小
    public  long  attain_file_size(final String file_uri){
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                HttpURLConnection http = null;
                try {
                    url = new URL(file_uri);
                    http = (HttpURLConnection) url.openConnection();
                    http.setConnectTimeout(10 * 1000);
                    http.setReadTimeout(10 * 1000);
                    http.setRequestMethod("GET");
                    if (http.getResponseCode() == 200) {
                        //文件大小
                        filesize = http.getContentLength();
                        Log.e(TAG, "文件的大小为："+filesize+"b");
                        //线程个数确定——获取每个线程下载文件的大小
                        download_file_thread_size =filesize%THREAD_SIZE==0?filesize/THREAD_SIZE:filesize/(THREAD_SIZE+1);
                        //真实的线程个数
                        true_thread_size=filesize%THREAD_SIZE==0?THREAD_SIZE:THREAD_SIZE+1;
                        Log.e(TAG,"真实线程个数为为："+ true_thread_size);
                        Log.e(TAG,"每个线程下载文件的大小为："+ download_file_thread_size);
                        //将任务分配给各线程
                        give_everythread_duty(file_uri);
                    } else {
                        filesize = -1;
                        Log.e(TAG, "文件大小获取失败" );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    filesize = -1;
                } finally {
                    http.disconnect();
                }
            }
        }).start();
        return filesize;
    }

    //给线程分配对应的下载任务
    public List<DownLoadInfo> give_everythread_duty(String file_uri){
        for (int i = 0; i < true_thread_size; i++) {
            DownLoadInfo info = new DownLoadInfo();
            long startpos = 0, endpos = 0;
            if (i == true_thread_size - 1) {
                startpos = i * download_file_thread_size;
                endpos = filesize - 1;  //为何减个1
            } else {
                startpos = i * download_file_thread_size;
                endpos = (i + 1) * download_file_thread_size - 1;//为何减个1
            }
            info.setBlock(download_file_thread_size);
            info.setDownpos(0);  //线程已下载的大小为0
            info.setStartpos(startpos);  //设置开始位置
            info.setEndpos(endpos); //设置结束位置
            info.setDownloadurl(file_uri);
            info.setThreadid(i);  //设置对应线程的id
            infos.add(info);
            info = null;
        }
        Log.e(TAG, "分配的线程个数为："+infos.size() );
        return infos;
    }


    public static File getExternalCacheDirectory(Context context,String type) {
        File appCacheDir = null;
        if( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (TextUtils.isEmpty(type)){
                appCacheDir = context.getExternalCacheDir();
            }else {
                appCacheDir = context.getExternalFilesDir(type);
            }
            if (appCacheDir == null){// 有些手机需要通过自定义目录
                appCacheDir = new File(Environment.getExternalStorageDirectory(),"Android/data/"+context.getPackageName()+"/cache/"+type);
            }
            if (appCacheDir == null){
                Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard unknown exception !");
            }else {
                if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
                    Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is make directory fail !");
                }
            }
        }else {
            Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard nonexistence or sdCard mount fail !");
        }
        return appCacheDir;
    }
}
