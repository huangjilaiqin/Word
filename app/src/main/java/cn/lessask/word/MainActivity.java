package cn.lessask.word;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.IBinder;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.facebook.stetho.common.Util;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.lessask.word.alipay.OrderInfoUtil2_0;
import cn.lessask.word.dialog.LoadingDialog;
import cn.lessask.word.model.ArrayListResponse;
import cn.lessask.word.model.Response;
import cn.lessask.word.model.ResponseData;
import cn.lessask.word.model.Sign;
import cn.lessask.word.model.User;
import cn.lessask.word.model.WordList;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.recycleview.DividerItemDecoration;
import cn.lessask.word.recycleview.RecyclerViewStatusSupport;
import cn.lessask.word.util.GlobalInfo;
import cn.lessask.word.util.TimeHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private final int LOGIN=1;
    private VolleyHelper volleyHelper = VolleyHelper.getInstance();
    private CircleImageView headImg;
    private RecyclerViewStatusSupport mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SignAdapter mRecyclerViewAdapter;

    private GlobalInfo globalInfo=GlobalInfo.getInstance();

    private ServiceInterFace serviceInterFace;
    private Intent serviceIntent;
    private Button start;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceInterFace=(ServiceInterFace)service;
            User user=globalInfo.getUser();
            if(user.getBookid()!=0) {
                serviceInterFace.checkSyncBook(user.getUserid(), user.getToken(), user.getBookid());
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void callAliPay2(){

        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://www.word.gandafu.com/buy.php", ResponseData.class, new GsonRequest.PostGsonRequest<ResponseData>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(ResponseData user) {
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    if(user.getErrno()==601){
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN);
                    }else {
                        Toast.makeText(MainActivity.this, user.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Log.e(TAG, "callAliPay2:"+user.getData());
                    final String orderInfo=user.getData();

                    Runnable payRunnable = new Runnable() {
                        @Override
                        public void run() {
                            PayTask alipay = new PayTask(MainActivity.this);
                            String v = alipay.getVersion();
                            Log.e(TAG, "version:"+v);
                            Map<String,String> result = alipay.payV2(orderInfo,true);
                            //alipay.h5Pay(orderInfo,true);

                            Log.e(TAG, "pay cb");

                        }
                    };
                    // 必须异步调用
                    Thread payThread = new Thread(payRunnable);
                    payThread.start();

                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(MainActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid",""+globalInfo.getUser().getUserid());
                datas.put("goodid","0");
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serviceIntent = new Intent(this, ServiceCrack.class);

        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerViewStatusSupport)findViewById(R.id.list);
        mRecyclerView.setStatusViews(findViewById(R.id.loading_view), findViewById(R.id.empty_view), findViewById(R.id.error_view));

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.setClickable(true);

        mRecyclerViewAdapter = new SignAdapter(getBaseContext());
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buy();
            }
        });

        findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(globalInfo.getUser().getBookid()>0) {
                    Intent intent = new Intent(MainActivity.this, WordActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(MainActivity.this, SelectBookActivity.class);
                    startActivity(intent);
                }
            }
        });

        headImg = (CircleImageView)findViewById(R.id.head_img);
        headImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PersionalActivity.class);
                startActivity(intent);
                //callAliPay2();
            }
        });

        WaveView mWaveView = (WaveView)findViewById(R.id.waveview);
        /*
        mWaveView.setDuration(5000);
        mWaveView.setSpeed(1000);
        mWaveView.setInitialRadius(150);
        mWaveView.setMaxRadius(500);
        mWaveView.setColor(Color.parseColor("#ff0000"));
        mWaveView.setInterpolator(new LinearOutSlowInInterpolator());
        //mWaveView.setInterpolator(new AccelerateInterpolator(1.2f));
        mWaveView.start();
        */

        mWaveView.setDuration(5000);
        mWaveView.setSpeed(1000);
        mWaveView.setStyle(Paint.Style.FILL_AND_STROKE);
        mWaveView.setColor(getResources().getColor(R.color.hublue));
        //mWaveView.setColor(Color.RED);
        mWaveView.setInterpolator(new LinearOutSlowInInterpolator());
        mWaveView.start();

        //从本地加载
        SharedPreferences sp = this.getSharedPreferences("SP", MODE_PRIVATE);
        //判断是否是第一次启动
        if(!sp.getBoolean("initDb", false)){
            initDb(sp);
        }

        //为审核伪造的用户信息

        //User tuser = new User(1,"qq_F4327C81A7510540DCB6E9759010348F","唐三炮","272233d9580dab2c9e432992a0659b88","http://q.qlogo.cn/qqapp/1105464601/F4327C81A7510540DCB6E9759010348F/100","男");
        //storageUser(tuser);

        int userid = sp.getInt("userid", 0);
        String token = sp.getString("token", "");
        String nickname = sp.getString("nickname", "");
        String headimg = sp.getString("headimg","");
        String gender = sp.getString("gender", "");
        int bookid = sp.getInt("bookid",0);
        Log.e(TAG, "local bookid:"+bookid);

        if(userid==0 || token==""){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, LOGIN);
        }else{
            if(nickname==""||headimg==""||bookid==0){
                //加载用户信息
                loadUserInfo(userid,token);
            }else{
                User user = new User(userid,"",nickname,token,headimg,gender,bookid);
                globalInfo.setUser(user);
                loadHeadImg(user.getHeadimg());
                bindService(serviceIntent, connection, BIND_AUTO_CREATE);
                atferLoadUser();
            }
        }
    }

    private void atferLoadUser(){
        loadSign();
    }

    private void loadUserInfo(final int userid,final String token){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/userinfo", User.class, new GsonRequest.PostGsonRequest<User>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(User user) {
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    if(user.getErrno()==601){
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN);
                    }else {
                        Toast.makeText(MainActivity.this, "uploadUserInfo error:" + user.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //本地存储
                    Log.e(TAG,"bookid test:"+user.getBookid());
                    storageUser(user);
                    globalInfo.setUser(user);
                    loadHeadImg(user.getHeadimg());
                    bindService(serviceIntent, connection, BIND_AUTO_CREATE);
                    atferLoadUser();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(MainActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    private void loadHeadImg(String url){
        ImageLoader.ImageListener headImgListener = ImageLoader.getImageListener(headImg, 0, 0);
        VolleyHelper.getInstance().getImageLoader().get(url, headImgListener, 100, 100);
    }

    private void storageUser(User user){
        SharedPreferences sp = this.getSharedPreferences("SP", MODE_PRIVATE);
        //存入数据
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("userid", user.getUserid());
        editor.putString("token", user.getToken());
        editor.putString("nickname", user.getNickname());
        editor.putString("headimg", user.getHeadimg());
        editor.putString("gender", user.getGender());
        editor.commit();
    }
    private void storageUser2Db(User user){
        ContentValues values = new ContentValues();
        values.put("userid", user.getUserid());
        values.put("token", user.getToken());
        values.put("nickname", user.getNickname());
        values.put("headimg", user.getHeadimg());
        values.put("gender", user.getGender());
        globalInfo.getDb(MainActivity.this).insert("t_user", null, values);

    }

    private void initDb(SharedPreferences baseInfo){
        SharedPreferences.Editor editor = baseInfo.edit();
        //初始化数据库
        SQLiteDatabase db = globalInfo.getDb(getApplicationContext());

        Log.e(TAG, "create db begin");
        //db.execSQL("drop table t_words");
        //db.execSQL("create table t_user(userid INTEGER primary key,token text,nickname text,headimg text,gender text)");
        //db.execSQL("create table t_books(userid integer,bookid integer,completeness real,current integer)");
        db.execSQL("create table t_words(`id` INTEGER not null,`userid` INTEGER not null,`bookid` integer not null,`word` text not null,`usphone` text default '',`ukphone` text default '',mean text default '',sentence text default '',`review` TIMESTAMP,`status` tinyint not null default 0,`sync` tinyint not null default 1,offline tinyint not null default 0)");
        db.execSQL("create index iduidbid on t_words(id,userid,bookid)");

        Log.e(TAG, "create db end");

        editor.putBoolean("initDb", true);
        editor.commit();
        Toast.makeText(MainActivity.this, "initDb", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult userid:" + requestCode + "," + resultCode);
        if(resultCode==RESULT_OK || resultCode==RESULT_FIRST_USER) {
            switch (requestCode) {
                case LOGIN:
                    User user = data.getParcelableExtra("user");
                    Log.e(TAG, "onActivityResult userid:" + user.getUserid() + ", nickname:" + user.getNickname()+", headimg:"+user.getHeadimg());

                    storageUser(user);
                    //storageUser2Db(user);
                    globalInfo.setUser(user);
                    loadHeadImg(user.getHeadimg());
                    bindService(serviceIntent, connection, BIND_AUTO_CREATE);

                    atferLoadUser();
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(serviceIntent); // Myservice需要在清单文件中配置
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(serviceIntent);
    }

    private void loadSign(){
        Type type = new TypeToken<ArrayListResponse<Sign>>() {}.getType();

        String url = "http://120.24.75.92:5006/word/sign";

        GsonRequest gsonRequest = new GsonRequest<ArrayListResponse<Sign>>(Request.Method.POST,url,type,new GsonRequest.PostGsonRequest<ArrayListResponse<Sign>>(){
            @Override
            public void onStart() {
                mRecyclerView.showLoadingView();
            }

            @Override
            public void onResponse(ArrayListResponse<Sign> response) {
                if(response.getError()!=null || response.getErrno()!=0){
                    mRecyclerView.showErrorView(response.getError());
                    Toast.makeText(MainActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                }else {
                    List<Sign> signs = response.getDatas();
                    mRecyclerViewAdapter.appendToList(signs);
                    mRecyclerViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(VolleyError error) {
                mRecyclerView.showErrorView(error.toString());
            }

            @Override
            public void setPostData(Map datas) {
                User user=globalInfo.getUser();
                datas.put("userid", "" + user.getUserid());
                datas.put("token", "" + user.getToken());
            }

        });
        //gsonRequest.setGson(TimeHelper.gsonWithDate());
        gsonRequest.setGson(TimeHelper.gsonWithNodeDate());
        volleyHelper.addToRequestQueue(gsonRequest);
    }

    private void buy(){
        final LoadingDialog loadingDialog = new LoadingDialog(this);
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://www.word.gandafu.com/buy.php", ResponseData.class, new GsonRequest.PostGsonRequest<ResponseData>() {
            @Override
            public void onStart() {
                loadingDialog.show();
            }
            @Override
            public void onResponse(ResponseData user) {
                loadingDialog.cancel();
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    if(user.getErrno()==601){
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN);
                    }else {
                        Toast.makeText(MainActivity.this, user.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Log.e(TAG, "callAliPay2:"+user.getData());
                    final String orderInfo=user.getData();

                    Runnable payRunnable = new Runnable() {
                        @Override
                        public void run() {
                            PayTask alipay = new PayTask(MainActivity.this);
                            String v = alipay.getVersion();
                            Log.e(TAG, "version:"+v);
                            Map<String,String> result = alipay.payV2(orderInfo,true);
                            //alipay.h5Pay(orderInfo,true);

                            Log.e(TAG, "pay cb");

                        }
                    };
                    // 必须异步调用
                    Thread payThread = new Thread(payRunnable);
                    payThread.start();

                }
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Toast.makeText(MainActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid",""+globalInfo.getUser().getUserid());
                datas.put("contractid","0");
                datas.put("wordnum","20");
                datas.put("tradetype","1");
                datas.put("tradeplat","1");
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }
}


