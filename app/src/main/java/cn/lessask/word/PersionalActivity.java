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
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

import cn.lessask.word.model.User;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.util.GlobalInfo;

public class PersionalActivity extends AppCompatActivity {
    private String TAG = PersionalActivity.class.getSimpleName();
    private Button download;
    private ImageView headImg;
    private TextView offlineRate;
    private TextView bookNameTv;
    private GlobalInfo globalInfo=GlobalInfo.getInstance();
    private User user = globalInfo.getUser();
    private SharedPreferences sp;

    private final int DOWNLOAD_UPDATE=1;
    private final int DOWNLOAD_STOP_UPDATE=2;

    private final int CHANGE_BOOK=1;

    private ServiceInterFace serviceInterFace;
    private Intent serviceIntent;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //离线中
                case DOWNLOAD_UPDATE:
                    float rate  =(float)msg.obj;
                    String rateStr = new BigDecimal(rate).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                    if(rateStr.equals("100.00")){
                        offlineRate.setVisibility(View.INVISIBLE);
                        download.setEnabled(false);
                        download.setText("已离线");
                    }else{
                        offlineRate.setVisibility(View.VISIBLE);
                        offlineRate.setText(rateStr + "%");
                        download.setEnabled(true);
                        download.setText("暂停离线");
                    }
                    break;
                //不是离线中
                case DOWNLOAD_STOP_UPDATE:
                    rate  =(float)msg.obj;
                    rateStr = new BigDecimal(rate).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                    if(rateStr.equals("100.00")){
                        offlineRate.setVisibility(View.INVISIBLE);
                        download.setEnabled(false);
                        download.setText("已离线");
                    }else{
                        offlineRate.setVisibility(View.VISIBLE);
                        offlineRate.setText(rateStr + "%");
                        download.setEnabled(true);
                        download.setText("离线");
                    }
                    break;
            }
        }
    };

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persional);
        serviceIntent = new Intent(this, ServiceCrack.class);
        sp = PersionalActivity.this.getSharedPreferences("SP", MODE_PRIVATE);

        headImg=(ImageView)findViewById(R.id.head_img);
        offlineRate=(TextView)findViewById(R.id.offline_rate);
        bookNameTv=(TextView)findViewById(R.id.bookname);

        findViewById(R.id.change_book).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersionalActivity.this, SelectBookActivity.class);
                startActivityForResult(intent, CHANGE_BOOK);
            }
        });
        findViewById(R.id.purch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        loadHeadImg(user.getHeadimg());

        download=(Button)findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用服务进行离线
                if (download.getText().equals("离线")) {
                    serviceInterFace.startDownload(user.getUserid(), user.getToken(), user.getBookid());
                    download.setText("暂停离线");
                    monitorDownload();
                } else if (download.getText().equals("暂停离线")) {
                    serviceInterFace.stopDownload();
                    download.setText("离线");
                    timer.cancel();
                }
            }
        });

        String bookName = sp.getString("bookName","");
        if(bookName.length()==0){
            //根据bookid请求单词书信息
        }else{
            bookNameTv.setText(bookName);
        }

        bindService();
    }

    private void monitorDownload(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                float rate = serviceInterFace.getOfflineRate(user.getUserid(), user.getBookid());
                Message message = new Message();
                message.what = DOWNLOAD_UPDATE;
                message.obj = rate * 100;
                handler.sendMessage(message);
            }
        };

        timer=new Timer();
        timer.schedule(timerTask, 0, 1000);
    }
    private void loadHeadImg(String url){
        ImageLoader.ImageListener headImgListener = ImageLoader.getImageListener(headImg, 0, 0);
        VolleyHelper.getInstance().getImageLoader().get(url, headImgListener, 100, 100);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceInterFace=(ServiceInterFace)service;
            checkDownloadService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private void bindService(){
        bindService(serviceIntent, serviceConnection,BIND_AUTO_CREATE);
    }

    private void checkDownloadService(){
        if(serviceInterFace.isDownloading()){
            Log.e(TAG, "isDownloading");
            monitorDownload();
        }else {
            float rate = serviceInterFace.getOfflineRate(user.getUserid(), user.getBookid());
            Log.e(TAG, "service offlinerate:" + rate);
            Message message = new Message();
            message.what = DOWNLOAD_STOP_UPDATE;
            message.obj = rate * 100;
            handler.sendMessage(message);
        }
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
        if(timer!=null)
            timer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //接触绑定
        unbindService(serviceConnection);

        if(timer!=null)
            timer.cancel();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK || resultCode==RESULT_FIRST_USER) {
            switch (requestCode) {
                case CHANGE_BOOK:
                    if(data.getBooleanExtra("haveChange",false)) {
                        Log.e(TAG, "haveChange book");
                        bookNameTv.setText(sp.getString("bookName", "单词书"));
                        if (serviceInterFace != null) {
                            serviceInterFace.stopDownload();
                            checkDownloadService();
                        } else {
                            bindService();
                        }
                    }
                    break;
            }
        }
    }
}
