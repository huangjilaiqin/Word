package cn.lessask.word;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.lessask.word.model.ArrayListResponse;
import cn.lessask.word.model.Word;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.util.GlobalInfo;

/**
 * Created by laiqin on 16/9/16.
 */
public class ServiceCrack extends Service implements ServiceInterFace{
    private String TAG = ServiceCrack.class.getSimpleName();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();
    private int reviewSize=0;
    private ServiceBider serviceBider;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(serviceBider==null)
            serviceBider=new ServiceBider();
        return serviceBider;
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            int currentReviewSize=queryReviewSized();
            Log.e("ServiceCrack ", "currentReviewSize:"+currentReviewSize+", "+reviewSize);
            if(reviewSize!=currentReviewSize) {
                reviewSize=currentReviewSize;
                mynotify();
            }else if(currentReviewSize==0){
                cancleNotify(1);
            }
        }
    };
    private Timer timer=new Timer();
    //从本地加载
    private int userid,bookid;
    private NotificationManager manager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TAG2","test service");
        SharedPreferences sp = this.getSharedPreferences("SP", MODE_PRIVATE);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        userid = sp.getInt("userid", 0);
        bookid = sp.getInt("bookid", 1);
        timer.schedule(timerTask, 0, 600000);
    }

    private void mynotify(){

        PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 0,new Intent(this, MainActivity.class), 0);
        // 通过Notification.Builder来创建通知，注意API Level
        // API11之后才支持
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.qq_login) // 设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap
                // icon)
                .setTicker("TickerText:" + "您有新短消息，请注意查收！")// 设置在status
                // bar上显示的提示文字
                .setContentTitle("契约单词")// 设置在下拉status
                // bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
                .setContentText("您有"+reviewSize+"个单词需要复习")// TextView中显示的详细内容
                .setContentIntent(pendingIntent2) // 关联PendingIntent
                .setNumber(reviewSize); // 在TextView的右方显示的数字，可放大图片看，在最右侧。这个number同时也起到一个序列号的左右，如果多个触发多个通知（同一ID），可以指定显示哪一个。
                //.getNotification(); // 需要注意build()是在API level
        // 16及之后增加的，在API11中可以使用getNotificatin()来代替

        Notification notify2 = builder.getNotification();
        notify2.defaults |= Notification.DEFAULT_VIBRATE;
        notify2.defaults |= Notification.DEFAULT_SOUND;
        notify2.defaults |= Notification.DEFAULT_LIGHTS;
        notify2.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(1, notify2);
    }

    private void cancleNotify(int id){
        manager.cancel(id);
    }

    private int queryReviewSized(){
        //String [] timeDelta = new String[]{"-5 minute","-30 minute","-1 hour","-12 hour","-1 day"};
        //String [] timeDelta = new String[]{"-1 minute","-1 minute","-1 minute","-1 minute"};
        String [] timeDelta = new String[]{"-5 minute","-30 minute","-480 minute","-720 minute"};
        //i对应的就是status的值
        int currentReviewSize=0;
        for(int i=0,size=timeDelta.length;i<size;i++) {
            String reviewSql = "select id,word,usphone,ukphone,mean,sentence,review from t_words where userid=? and bookid=? and status=? and review<strftime('%s','now', '"+timeDelta[i]+"')";
            int status=i+1;
            Cursor cursor = globalInfo.getDb(getApplicationContext()).rawQuery(reviewSql, new String[]{"" + userid, "" + bookid,""+status});
            currentReviewSize+=cursor.getCount();
        }
        return currentReviewSize;
    }

    private int totalWords,offlineWords;
    private float calOfflineRate(int userid,int bookid){
        String allSql = "select count(id) as num from t_words where userid=? and bookid=?";
        SQLiteDatabase db = globalInfo.getDb(getBaseContext());
        Cursor cursor = db.rawQuery(allSql, new String[]{"" + userid, "" + bookid});
        cursor.moveToNext();
        totalWords=cursor.getInt(0);
        if(totalWords==0)
            return -1;
        String sql = "select count(id) as num from t_words where userid=? and bookid=? and mean!=''";
        cursor = db.rawQuery(sql, new String[]{""+userid, ""+ bookid});
        cursor.moveToNext();
        offlineWords=cursor.getInt(0);
        float rate=offlineWords/(totalWords*1.0f);
        return rate;
    }

    @Override
    public float getOfflineRate(int userid, int bookid) {
        if(totalWords==0){
            return calOfflineRate(userid,bookid);
        }
        return offlineWords/(totalWords*1.0f);
    }
    private void downloadWords(final int userid,final String token,final int bookid,final String wordsStr){
        Type type = new TypeToken<ArrayListResponse<Word>>() {}.getType();
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/downloadwords", type, new GsonRequest.PostGsonRequest<ArrayListResponse>() {
            @Override
            public void onStart() {
                Log.e(TAG, "service stat download words");
            }
            @Override
            public void onResponse(ArrayListResponse resp) {
                if(resp.getError()!=null && resp.getError()!="" || resp.getErrno()!=0){
                    if(resp.getErrno()!=0 || resp.getError()!=""){
                        Log.e(TAG, "service downloadWords error"+resp.getError());
                    }
                }else {
                    //本地存储
                    ArrayList<Word> words = resp.getDatas();
                    SQLiteDatabase db=globalInfo.getDb(getBaseContext());
                    for(int i=0,size=words.size();i<size;i++){
                        Word word = words.get(i);
                        ContentValues values = new ContentValues();
                        values.put("usphone",word.getUsphone());
                        values.put("ukphone",word.getUkphone());
                        values.put("mean",word.getMean());
                        values.put("sentence",word.getSentence());
                        String where = "userid=? and id=?";
                        String[] whereArgs = new String[]{""+userid,""+word.getId()};
                        db.update("t_words", values, where, whereArgs);
                    }
                    offlineWords+=words.size();
                    Log.e(TAG, "service download finish");
                }
                downloading=false;
            }

            @Override
            public void onError(VolleyError error) {
                downloading=false;
                Log.e(TAG, "service downloadWords error:"+error.getMessage());
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", ""+userid);
                datas.put("token", token);
                datas.put("bookid",""+bookid);
                datas.put("words", wordsStr);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    private boolean canDownload=false;
    private boolean downloading=false;
    private boolean downloadFinish=false;

    private String getNotDownloadWords(int wordSize){
        String newSql = "select id,word,usphone,ukphone,mean,sentence from t_words where userid=? and bookid=? and mean='' order by id limit ?";
        Cursor cursor = globalInfo.getDb(getBaseContext()).rawQuery(newSql,new String[]{""+userid,""+bookid,""+wordSize});
        StringBuilder builder = new StringBuilder();
        for(int i=0,count=cursor.getCount();i<count;i++) {
            cursor.moveToNext();
            int id = cursor.getInt(0);
            String wordStr = cursor.getString(1);
            builder.append(wordStr);
            if (i + 1 < count)
                builder.append(",");
        }
        return builder.toString();
    }

    @Override
    public void startDownload(final int userid,final String token,final int bookid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                canDownload=true;
                float rate = getOfflineRate(userid,bookid);
                if(rate==1)
                    downloadFinish=true;
                while (canDownload && !downloadFinish){
                    if(!downloading){
                        //查库调下载接口
                        String words = getNotDownloadWords(20);
                        if(words.length()==0){
                            canDownload=false;
                            downloadFinish=true;
                            break;
                        }
                        Log.e(TAG, "words:"+words);
                        downloadWords(userid,token,bookid,words);
                        //休息
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){

                        }
                    }
                }

            }
        }).start();

    }

    @Override
    public boolean isDownloading() {
        return canDownload;
    }

    @Override
    public void stopDownload() {
        canDownload=false;
        downloading=false;
        downloadFinish=false;
        totalWords=0;
        offlineWords=0;
    }

    class ServiceBider extends Binder implements ServiceInterFace{
        @Override
        public float getOfflineRate(int userid, int bookid) {
            return ServiceCrack.this.getOfflineRate(userid,bookid);
        }

        @Override
        public void startDownload(int userid, String token, int bookid) {
            ServiceCrack.this.startDownload(userid,token,bookid);
        }

        @Override
        public void stopDownload() {
            ServiceCrack.this.stopDownload();
        }

        @Override
        public boolean isDownloading() {
            return ServiceCrack.this.isDownloading();
        }
    }
}
