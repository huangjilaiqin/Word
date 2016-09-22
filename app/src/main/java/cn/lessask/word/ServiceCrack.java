package cn.lessask.word;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import cn.lessask.word.util.GlobalInfo;

/**
 * Created by laiqin on 16/9/16.
 */
public class ServiceCrack extends Service{
    private GlobalInfo globalInfo = GlobalInfo.getInstance();
    private int reviewSize=0;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        timer.schedule(timerTask,0,600000);
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
}
