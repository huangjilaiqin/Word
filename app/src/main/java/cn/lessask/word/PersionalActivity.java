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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.lessask.word.model.Book;
import cn.lessask.word.model.User;
import cn.lessask.word.net.GsonRequest;
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
    private final int NOT_SELECT_BOOK=3;
    private final int GET_BOOK_NAME =4;

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
                        download.setVisibility(View.VISIBLE);
                        download.setText("已离线");
                        timer.cancel();
                    }else{
                        offlineRate.setVisibility(View.VISIBLE);
                        offlineRate.setText(rateStr + "%");
                        download.setEnabled(true);
                        download.setVisibility(View.VISIBLE);
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
                        download.setVisibility(View.VISIBLE);
                        download.setText("已离线");
                    }else{
                        offlineRate.setVisibility(View.VISIBLE);
                        offlineRate.setText(rateStr + "%");
                        download.setEnabled(true);
                        download.setVisibility(View.VISIBLE);
                        download.setText("离线");
                    }
                    break;
                case NOT_SELECT_BOOK:
                    offlineRate.setVisibility(View.INVISIBLE);
                    download.setVisibility(View.INVISIBLE);
                    break;
                case GET_BOOK_NAME:
                    String bookName=(String)msg.obj;
                    bookNameTv.setText(bookName);
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
            int bookid = user.getBookid();
            if(bookid==0){
                //还未选择词库
                offlineRate.setVisibility(View.INVISIBLE);
                download.setEnabled(false);
                download.setText("离线");
            }else{
                //根据bookid请求单词书信息
                queryBookInfo(user.getUserid(),user.getToken(),bookid);
            }
        }else{
            bookNameTv.setText(bookName);
        }

        bindService();

    }

    private void queryBookInfo(final int userid,final String token,final int bookid){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/bookinfo", Book.class, new GsonRequest.PostGsonRequest<Book>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(Book book) {
                if(book.getError()!=null && book.getError()!="" || book.getErrno()!=0){
                    Toast.makeText(PersionalActivity.this, "bookinfo error:" + book.getError(), Toast.LENGTH_SHORT).show();
                }else {
                    SharedPreferences sp = PersionalActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                    //存入数据
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("bookName",book.getName());
                    editor.commit();
                    Message message = new Message();
                    message.what = GET_BOOK_NAME;
                    message.obj=book.getName();
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(PersionalActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
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

    private void monitorDownload(){
        final TimerTask timerTask = new TimerTask() {
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
            int bookid=user.getBookid();
            if(bookid>0) {
                float rate = serviceInterFace.getOfflineRate(user.getUserid(), user.getBookid());
                Log.e(TAG, "service offlinerate:" + rate);
                Message message = new Message();
                message.what = DOWNLOAD_STOP_UPDATE;
                message.obj = rate * 100;
                handler.sendMessage(message);
            }else {
                Message message = new Message();
                message.what = NOT_SELECT_BOOK;
                handler.sendMessage(message);
            }
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
