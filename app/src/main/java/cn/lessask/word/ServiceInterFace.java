package cn.lessask.word;

/**
 * Created by laiqin on 16/9/28.
 */
public interface ServiceInterFace {
    float getOfflineRate(int userid,int bookid);
    void startDownload(int userid,String token,int bookid);
    void stopDownload();
    boolean isDownloading();
    void downloadWordStatus(int userid,String token,int bookid);
    void checkSyncBook(int userid,String token,int bookid);
}
