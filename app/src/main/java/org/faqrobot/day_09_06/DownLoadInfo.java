package org.faqrobot.day_09_06;

/**
 * 既然要对各个线程分配对应的下载大小，我们就有必要知道各个线程对应的信息，那么我么先来定义 bean 类来表示这些信息
 */

public class DownLoadInfo {

    private int threadid;//线程id
    private long startpos;//下载的起始位置
    private long endpos;//下载的结束位置
    private long block;//每条下载的大小
    private long downpos;//该条线程已经下载的大小
    private String downloadurl;//下载地址

    public int getThreadid() {
        return threadid;
    }

    public void setThreadid(int threadid) {
        this.threadid = threadid;
    }

    public long getStartpos() {
        return startpos;
    }

    public void setStartpos(long startpos) {
        this.startpos = startpos;
    }

    public long getEndpos() {
        return endpos;
    }

    public void setEndpos(long endpos) {
        this.endpos = endpos;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public long getDownpos() {
        return downpos;
    }

    public void setDownpos(long downpos) {
        this.downpos = downpos;
    }

    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }
}
