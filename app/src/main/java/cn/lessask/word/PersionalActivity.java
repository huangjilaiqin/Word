package cn.lessask.word;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.awt.font.TextAttribute;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

import cn.lessask.word.model.User;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.test.ExampleTest;
import cn.lessask.word.util.GlobalInfo;

public class PersionalActivity extends AppCompatActivity {
    private String TAG = PersionalActivity.class.getSimpleName();
    private Button download;
    private ImageView headImg;
    private TextView offlineRate;
    private GlobalInfo globalInfo=GlobalInfo.getInstance();
    private User user = globalInfo.getUser();
    private boolean downloading=false;
    private Thread downloadMonitor;
    private final int DOWNLOAD_UPDATE=1;
    private final int DOWNLOAD_FINISH=2;

    private ServiceInterFace serviceInterFace;
    private Intent serviceIntent;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DOWNLOAD_UPDATE:
                    float rate  =(float)msg.obj;
                    String rateStr = new BigDecimal(rate).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                    offlineRate.setVisibility(View.VISIBLE);
                    offlineRate.setText(rateStr + "%");
                    Log.e(TAG, "download updte");
                    break;
                case DOWNLOAD_FINISH:
                    offlineRate.setVisibility(View.INVISIBLE);
                    download.setEnabled(false);
                    download.setText("已离线");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persional);
        serviceIntent = new Intent(this, ServiceCrack.class);

        headImg=(ImageView)findViewById(R.id.head_img);
        offlineRate=(TextView)findViewById(R.id.offline_rate);

        findViewById(R.id.change_book).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersionalActivity.this, SelectBookActivity.class);
                startActivity(intent);

            }
        });
        findViewById(R.id.purch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        download=(Button)findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用服务进行离线

            }
        });
        loadHeadImg(user.getHeadimg());

        SharedPreferences sp = PersionalActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
        if(!sp.getBoolean("offline",false)){
            float rate = calOfflineRate(user.getUserid(),user.getBookid());
            if(rate==1){
                SharedPreferences.Editor editor=sp.edit();
                editor.putBoolean("offline",true);
                editor.commit();
                offlineRate.setVisibility(View.INVISIBLE);
                download.setEnabled(false);
                download.setText("已离线");
            }else {
                String rateStr = new BigDecimal(rate).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                offlineRate.setVisibility(View.VISIBLE);
                offlineRate.setText(rateStr + "%");
                download.setEnabled(true);
                download.setText("离线");
            }
        }else {
            offlineRate.setVisibility(View.INVISIBLE);
            download.setEnabled(false);
            download.setText("已离线");
        }

        /*
        downloading=true;
        if(downloading){
            final Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    //从服务中获取离线进度
                    float rate=1f;
                    if(rate==1){
                        Message message = new Message();
                        message.what = DOWNLOAD_FINISH;
                        handler.sendMessage(message);
                        timer.cancel();
                    }else {
                        Message message = new Message();
                        message.what = DOWNLOAD_UPDATE;
                        message.obj=rate;
                        handler.sendMessage(message);
                    }
                }
            };
            timer.schedule(task,0,1000);
        }
        */
        bindService();
    }
    private void loadHeadImg(String url){
        ImageLoader.ImageListener headImgListener = ImageLoader.getImageListener(headImg, 0, 0);
        VolleyHelper.getInstance().getImageLoader().get(url, headImgListener, 100, 100);
    }

    private float calOfflineRate(int userid,int bookid){
        String allSql = "select count(id) as num from t_words where userid=? and bookid=?";
        SQLiteDatabase db = globalInfo.getDb(PersionalActivity.this);
        Cursor cursor = db.rawQuery(allSql, new String[]{"" + userid, "" + bookid});
        cursor.moveToNext();
        int allSize=cursor.getInt(0);
        if(allSize==0)
            return -1;
        String sql = "select count(id) as num from t_words where userid=? and bookid=? and mean=''";
        cursor = db.rawQuery(sql, new String[]{""+userid, ""+ bookid});
        cursor.moveToNext();
        int notOfflineSize=cursor.getInt(0);
        float rate=(allSize-notOfflineSize)/(allSize*1.0f);
        return rate;

    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceInterFace=(ServiceInterFace)service;
            float rate=serviceInterFace.getOfflineRate(user.getUserid(),user.getBookid());
            Log.e(TAG, "service offlinerate:"+rate);
            Message message = new Message();
            message.what = DOWNLOAD_UPDATE;
            message.obj=rate*100;
            handler.sendMessage(message);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private void bindService(){
        bindService(serviceIntent, serviceConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(serviceIntent); // Myservice需要在清单文件中配置
    };

    @Override
    protected void onStop() {
        super.onStop();
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //接触绑定
        unbindService(serviceConnection);
    }
}
