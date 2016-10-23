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

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cn.lessask.word.model.ArrayListResponse;
import cn.lessask.word.model.Book;
import cn.lessask.word.model.Response;
import cn.lessask.word.model.Word;
import cn.lessask.word.model.WordList;
import cn.lessask.word.model.WordStatus;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.NetworkFileHelper;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.util.GlobalInfo;
import cn.lessask.word.util.StringUtil;

/**
 * Created by laiqin on 16/9/16.
 */
public class ServiceCrack extends Service implements ServiceInterFace{
    private String TAG = ServiceCrack.class.getSimpleName();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();
    private int reviewSize=0;
    private ServiceBider serviceBider;
    private NetworkFileHelper networkFileHelper = NetworkFileHelper.getInstance();
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
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
        sp = this.getSharedPreferences("SP", MODE_PRIVATE);
        editor = sp.edit();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        userid = sp.getInt("userid", 0);
        bookid = sp.getInt("bookid", 0);
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
            String reviewSql = "select id from t_words where userid=? and bookid=? and status=? and review<strftime('%s','now', '"+timeDelta[i]+"')";
            int status=i+1;
            Cursor cursor = globalInfo.getDb(getApplicationContext()).rawQuery(reviewSql, new String[]{"" + userid, "" + bookid,""+status});
            currentReviewSize+=cursor.getCount();
            cursor.close();
        }
        editor.putInt("torevive",currentReviewSize);
        editor.commit();
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
        String sql = "select count(id) as num from t_words where userid=? and bookid=? and offline=2";
        cursor = db.rawQuery(sql, new String[]{""+userid, ""+ bookid});
        cursor.moveToNext();
        offlineWords=cursor.getInt(0);
        cursor.close();
        Log.e(TAG, "callOfflineRate totalWords:"+totalWords+", offlineWords:"+offlineWords);
        float rate=offlineWords/(totalWords*1.0f);
        return rate;
    }

    @Override
    public float getOfflineRate(int userid, int bookid) {
        if(totalWords==0){
            return calOfflineRate(userid,bookid);
        }
        synchronized (getBaseContext()) {
            Log.e(TAG, "getOfflineRate:" + offlineWords + "/" + totalWords);
            return offlineWords / (totalWords * 1.0f);
        }
        //return calOfflineRate(userid,bookid);
    }
    private void downloadWords(final int userid,final String token,final int bookid,final String wordsStr){
        Type type = new TypeToken<ArrayListResponse<Word>>() {}.getType();
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/downloadwords", type, new GsonRequest.PostGsonRequest<ArrayListResponse>() {
            @Override
            public void onStart() {
                changeDownloadStatus(1);
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
                    String updateSql = "update t_words set usphone=?,ukphone=?,mean=?,sentence=?,offline=offline+1 where userid=? and bookid=? and id=?";
                    for(int i=0,size=words.size();i<size;i++){
                        Word word = words.get(i);
                        Object[] args = new Object[]{word.getUsphone(),word.getUkphone(),word.getMean(),word.getSentence(),userid,bookid,word.getId()};
                        db.execSQL(updateSql,args);

                        checkOffline(userid, bookid, word.getId());
                    }
                    Log.e(TAG, "service download finish");
                }
                changeDownloadStatus(-1);
            }

            @Override
            public void onError(VolleyError error) {
                changeDownloadStatus(-1);
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
    private int downloading=0;
    private boolean downloadFinish=false;

    private ArrayList<Word> getNotDownloadWords(int wordSize){
        String newSql = "select id,word,mean from t_words where userid=? and bookid=? and offline!=2 order by id limit ?";
        Cursor cursor = globalInfo.getDb(getBaseContext()).rawQuery(newSql,new String[]{""+userid,""+bookid,""+wordSize});
        ArrayList<Word> words = new ArrayList<>();
        for(int i=0,count=cursor.getCount();i<count;i++) {
            cursor.moveToNext();
            Word word = new Word();
            word.setId(cursor.getInt(0));
            word.setWord(cursor.getString(1));
            word.setMean(cursor.getString(2));
            words.add(word);
        }
        cursor.close();
        return words;
    }

    @Override
    public void startDownload(final int userid,final String token,final int bookid) {
        this.userid=userid;
        this.bookid=bookid;

        Log.e(TAG, "startDownload bookid:"+bookid);
        new Thread(new Runnable() {
            @Override
            public void run() {
                canDownload=true;
                downloading=0;
                float rate = getOfflineRate(userid,bookid);
                if(rate==1)
                    downloadFinish=true;
                Log.e(TAG, "canDownload:"+canDownload+", downloadFinish:"+downloadFinish+", downloading:"+downloading);
                while (canDownload && !downloadFinish){
                    Log.e(TAG, "downloading:"+downloading);
                    if(downloading==0){
                        //查库调下载接口
                        ArrayList<Word> words = getNotDownloadWords(20);
                        if(words.size()==0){
                            canDownload=false;
                            downloadFinish=true;
                            break;
                        }
                        ArrayList<String> wordsStr = new ArrayList<String>();
                        boolean isoffline=true;
                        for(int i=0;i<words.size();i++){
                            isoffline=true;
                            Word word = words.get(i);
                            //Log.e(TAG, "word:"+word.getWord()+", "+word.get);
                            String mean = word.getMean();
                            String wordStr = word.getWord();
                            if(mean==null || mean.length()==0) {
                                wordsStr.add(wordStr);
                                isoffline=false;
                            }
                            //检查发音文件是否存在
                            String filename=wordStr+"_uk.mp3";
                            File phoneFile = new File(Constant.phonePrefixPath,filename);
                            if(!phoneFile.exists()){
                                isoffline=false;
                                downloadPhone(userid,word,"uk",phoneFile);
                            }else {
                                Log.e(TAG,"phone exists:"+phoneFile);
                            }
                            if(isoffline)
                                repaireOfflineStatus(userid,bookid,word.getId());
                        }
                        if(wordsStr.size()>0)
                            downloadWords(userid,token,bookid, StringUtil.join(wordsStr,","));
                    }
                    //休息
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e){

                    }
                }
            }
        }).start();
    }

    private void repaireOfflineStatus(int userid,int bookid,int wid){
        SQLiteDatabase db = globalInfo.getDb(getBaseContext());
        String sql = "update t_words set offline=2 where userid=? and bookid=? and id=?";
        db.execSQL(sql, new String[]{"" + userid, "" + bookid, "" + wid});

        synchronized (getBaseContext()) {
            offlineWords++;
            Log.e(TAG, "offline word repaire wid:" + wid);
            if(mySet.contains(wid))
                Log.e(TAG, "repeat 2 wid:"+wid);
            mySet.add(wid);
        }
    };

    private void incrOffline(int userid,int bookid,int wid){
        SQLiteDatabase db = globalInfo.getDb(getApplicationContext());
        String sql = "update t_words set offline=offline+1 where userid=? and bookid=? and id=?";
        db.execSQL(sql, new String[]{"" + userid, "" + bookid, "" + wid});

        checkOffline(userid,bookid,wid);
    }
    private Set<Integer> mySet=new HashSet<>();
    private void checkOffline(int userid,int bookid,int wid){
        SQLiteDatabase db = globalInfo.getDb(getApplicationContext());
        String sql = "select id from t_words where userid=? and bookid=? and id=? and offline=2";
        Cursor cursor=db.rawQuery(sql, new String[]{"" + userid, "" + bookid,""+wid});
        if(cursor.getCount()==1){
            synchronized (getBaseContext()) {
                offlineWords++;
                Log.e(TAG, "getOfflineRate:"+offlineWords);
                Log.e(TAG, "offline word:"+wid);
                if(mySet.contains(wid))
                    Log.e(TAG, "repeat 1 wid:"+wid);
                mySet.add(wid);
            }
        }
        cursor.close();
    }

    private void changeDownloadStatus(int step){
        synchronized (getBaseContext()){
            downloading+=step;
        }
    }

    private void downloadPhone(final int userid,final Word word, String type, File phoneFile){
        Log.e(TAG, "downloadPhone word:"+word.getWord());
        String url = "http://120.24.75.92:5006/word/downloadphone?word="+word.getWord()+"&type="+type;
        final String path = phoneFile.getPath();
        networkFileHelper.startGetFile(url, path, new NetworkFileHelper.GetFileRequest() {
            @Override
            public void onStart() {
                changeDownloadStatus(1);
            }

            @Override
            public void onResponse(String error) {
                if(error!=null && error.length()>0){
                    Log.e(TAG, "downloadPhone error:"+error);
                }else {
                    File file = new File(path);
                    if (file.exists()) {
                        //更新数据库
                        incrOffline(userid, word.getBookid(), word.getId());
                        Log.e(TAG, "download phone:" + word.getWord());
                    } else {
                        Log.e(TAG, "not exists:" + path);
                    }
                }
                changeDownloadStatus(-1);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, error);
                changeDownloadStatus(-1);
            }
        });
    }

    @Override
    public boolean isDownloading() {
        return canDownload;
    }

    @Override
    public void stopDownload() {
        canDownload=false;
        downloading=0;
        downloadFinish=false;
        totalWords=0;
        offlineWords=0;
    }

    public void downloadWordStatus(final int userid,final String token,final int bookid) {
        Type type = new TypeToken<ArrayListResponse<WordStatus>>(){}.getType();
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/downloadwordstatus", type, new GsonRequest.PostGsonRequest<ArrayListResponse>() {
            @Override
            public void onStart() {
            }

            @Override
            public void onResponse(ArrayListResponse resp) {
                if (resp.getError() != null && resp.getError() != "" || resp.getErrno() != 0) {
                    if (resp.getErrno() != 0 || resp.getError() != "") {
                        Log.e(TAG, "service downloadWordStatus error" + resp.getError());
                    }
                } else {
                    //本地存储
                    ArrayList<WordStatus> words = resp.getDatas();
                    SQLiteDatabase db = globalInfo.getDb(getBaseContext());
                    //当服务器单词的状态比本地高的时候才覆盖本地状态
                    String updateSql = "update t_words set status=?,review=? where userid=? and bookid=? and id=? and status<?";
                    for (int i = 0, size = words.size(); i < size; i++) {
                        WordStatus word = words.get(i);
                        Object[] args = new Object[]{word.getStatus(),word.getReview(),userid,bookid,word.getWid(),word.getStatus()};
                        db.execSQL(updateSql, args);
                    }
                    Log.e(TAG, "service downloadWordStatus finish");
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "service downloadWordStatus error:" + error.getMessage());
            }

            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
                datas.put("bookid", "" + bookid);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    private void syncBook(final int userid,final String token,final int bookid){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/changebook", WordList.class, new GsonRequest.PostGsonRequest<WordList>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(final WordList user) {
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    return;
                }
                //本地存储
                final String wordsStr = user.getWords();
                final String[] words = wordsStr.split(";");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase db = globalInfo.getDb(getBaseContext());
                        String[] where = new String[]{""+userid,""+bookid,"0"};
                        boolean isInsert=false;
                        for(int i=0,size=words.length;i<size;i++){
                            String[] info = words[i].split(":");
                            where[2]=info[0];
                            Cursor cursor = db.rawQuery("select id from t_words where userid=? and bookid=? and id=?", where);
                            if(cursor.getCount()==0) {
                                ContentValues values = new ContentValues();
                                values.put("id", info[0]);
                                values.put("word", info[1]);
                                values.put("userid", userid);
                                values.put("bookid", bookid);
                                globalInfo.getDb(getBaseContext()).insert("t_words", null, values);
                                //Log.e(TAG, "have word no:"+info[0]);
                                isInsert=true;
                            }else{
                                //Log.e(TAG, "have word:"+info[0]);
                            }
                            cursor.close();
                        }
                        Log.e(TAG, "insert done");
                        if(isInsert)
                            downloadWordStatus(userid,token,bookid);
                    }
                }).start();
                //设置bookid
                SharedPreferences sp = getBaseContext().getSharedPreferences("SP", MODE_PRIVATE);
                //存入数据
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("bookid", bookid);
                editor.commit();
            }

            @Override
            public void onError(VolleyError error) {
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
                //datas.put("id",""+id);
                datas.put("bookid",""+bookid);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    public void checkSyncBook(final int userid,final String token,final int bookid){
        Log.e(TAG, "checkSyncBook bookid:"+bookid);
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/bookinfo", Book.class, new GsonRequest.PostGsonRequest<Book>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(Book book) {
                if(book.getError()!=null && book.getError()!="" || book.getErrno()!=0){
                }else {
                    //查看本次词库大小跟线上词库大小是否相同
                    Cursor cursor = globalInfo.getDb(getBaseContext()).rawQuery("select count(id) as num from t_words",null);
                    if(cursor.moveToNext()){
                        int num=cursor.getInt(0);
                        if(num!=book.getNum()){
                            syncBook(userid,token,bookid);
                        }
                    }
                    cursor.close();
                }
            }

            @Override
            public void onError(VolleyError error) {
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
                datas.put("bookid",""+bookid);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }


    public void storageBook(String wordsStr){
        if(wordsStr==null){
            Log.e(TAG, "storageBook error: wordsStr is null");
            return;
        }
        if(wordsStr.length()>0) {
            final String[] words = wordsStr.split(";");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SQLiteDatabase db=globalInfo.getDb(getApplicationContext());
                    for (int i = 0, size = words.length; i < size; i++) {
                        String[] info = words[i].split(":");
                        ContentValues values = new ContentValues();
                        values.put("id", info[0]);
                        values.put("word", info[1]);
                        values.put("userid", userid);
                        values.put("bookid", bookid);
                        db.insert("t_words", null, values);
                        Log.e(TAG, "insert id:"+info[1]);
                    }
                    Log.e(TAG, "insert done");
                }
            }).start();
        }
    }

    public void syncWords(final int userid,final String token,final int bookid){
        final  SQLiteDatabase db = globalInfo.getDb(getBaseContext());
        String sql = "select id,status,review from t_words where userid=? and bookid=? and sync=0";
        Cursor cursor = db.rawQuery(sql, new String[]{"" + userid, "" + bookid});
        StringBuilder builder = new StringBuilder();
        StringBuilder idsBuilder = new StringBuilder();
        int count = cursor.getCount();
        if(count==0)
            return;
        int status=0,newnum=0,revivenum=0;
        for(int i=0;i<count;i++){
            cursor.moveToNext();
            int id = cursor.getInt(0);
            builder.append(id);
            idsBuilder.append(id);
            builder.append(",");
            status=cursor.getInt(1);
            builder.append(status);
            builder.append(",");
            builder.append(cursor.getInt(2));
            if(i+1<count){
                builder.append(";");
                idsBuilder.append(",");
            }
            if(status==1)
                newnum++;
            if(status>1)
                revivenum++;
        }
        Log.e(TAG, "syncWords newnum:"+newnum+", revivenum:"+revivenum);
        editor.putInt("newnum",sp.getInt("newnum",0)+newnum);
        editor.putInt("revivenum",sp.getInt("revivenum",0)+revivenum);
        editor.commit();

        final String syncDatas=builder.toString();
        final String syncIds=idsBuilder.toString();
        Log.e(TAG, "sync:"+syncDatas);
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/upwordstatus", Response.class, new GsonRequest.PostGsonRequest<Response>() {
            @Override
            public void onStart() {
            }
            @Override
            public void onResponse(Response resp) {
                if(resp.getError()!=null && resp.getError()!="" || resp.getErrno()!=0){
                    Log.e(TAG, "syncWords "+resp.getError());
                }else{
                    String updateSql = "update t_words set sync=1 where userid=? and bookid=? and id in ("+syncIds+")";
                    Cursor cursor=db.rawQuery(updateSql, new String[]{""+userid,""+bookid});
                    Log.e(TAG, "sync ok:"+cursor.getCount()+", "+syncIds);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "upwordstatus:" + error.toString());
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", ""+userid);
                datas.put("token", token);
                datas.put("bookid",""+bookid);
                datas.put("datas",syncDatas);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
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

        @Override
        public void downloadWordStatus(int userid, String token, int bookid) {
            ServiceCrack.this.downloadWordStatus(userid, token, bookid);
        }

        @Override
        public void checkSyncBook(int userid, String token, int bookid) {
            ServiceCrack.this.checkSyncBook(userid,token,bookid);
        }

        @Override
        public void storageBook(String wordsStr) {
            ServiceCrack.this.storageBook(wordsStr);
        }

        @Override
        public void syncWords(int userid, String token, int bookid) {
            ServiceCrack.this.syncWords(userid,token,bookid);
        }
    }
}
