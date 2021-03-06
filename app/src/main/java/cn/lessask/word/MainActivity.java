package cn.lessask.word;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.facebook.stetho.common.Util;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.grabner.circleprogress.AnimationState;
import at.grabner.circleprogress.AnimationStateChangedListener;
import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import cn.lessask.word.alipay.OrderInfoUtil2_0;
import cn.lessask.word.dialog.LoadingDialog;
import cn.lessask.word.model.ArrayListResponse;
import cn.lessask.word.model.MainInfo;
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

    private VolleyHelper volleyHelper = VolleyHelper.getInstance();
    private CircleImageView headImg;
    private RecyclerViewStatusSupport mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SignAdapter mRecyclerViewAdapter;
    private Button signReload;

    private GlobalInfo globalInfo=GlobalInfo.getInstance();

    private ServiceInterFace serviceInterFace;
    private Intent serviceIntent;

    private CircleProgressView toLearnProgress;
    private CircleProgressView toReviveProgress;
    private CircleProgressView depositProgress;
    private TextView signInfoMsg;
    private Button signStart;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private final int LOGIN=1;
    private final int GET_MININFO=1;
    private final int LEARN_WORD=2;

    private MyApplication myApplication;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
            switch (msg.what){
                case GET_MININFO:
                    MainInfo info=(MainInfo)msg.obj;
                    int newnum=0,revivenum=0,wordnum=0;
                    if(info!=null){
                        newnum=info.getNewnum();
                        revivenum=info.getRevivenum();
                        wordnum=info.getWordnum();
                    }else{
                        newnum=sp.getInt("newnum",0);
                        revivenum=sp.getInt("revivenum",0);
                        wordnum=sp.getInt("wordnum",20);
                    }
                    toLearnProgress.setMaxValue(wordnum);
                    //toLearnProgress.setValue(newnum);
                    toLearnProgress.setValueAnimated(0, newnum, 1000);
                    toLearnProgress.setText(newnum + "/" + wordnum);

                    //获取需要复习的个数
                    User user=globalInfo.getUser();
                    int torevive=queryReviveSized(user.getUserid(),user.getBookid());
                    Log.e(TAG, "torevive:"+torevive);
                    int total=torevive+revivenum;
                    toReviveProgress.setMaxValue(total);
                    toReviveProgress.setValueAnimated(0, revivenum, 1000);
                    toReviveProgress.setText(revivenum+"/"+total);

                    depositProgress.setMaxValue(user.getDeposit());
                    depositProgress.setValueAnimated(0, user.getGetback(), 1000);
                    depositProgress.setText(user.getGetback()+"/"+user.getDeposit());
                    break;
                case LEARN_WORD:
                    newnum=sp.getInt("newnum",0);
                    revivenum=sp.getInt("revivenum",0);
                    wordnum=sp.getInt("wordnum",20);

                    toLearnProgress.setMaxValue(wordnum);
                    //toLearnProgress.setValue(newnum);
                    toLearnProgress.setValueAnimated(0, newnum, 1000);
                    toLearnProgress.setText(newnum + "/" + wordnum);

                    //获取需要复习的个数
                    user=globalInfo.getUser();
                    torevive=queryReviveSized(user.getUserid(),user.getBookid());
                    total=torevive+revivenum;
                    toReviveProgress.setMaxValue(total);
                    toReviveProgress.setValueAnimated(0, revivenum, 1000);
                    toReviveProgress.setText(revivenum+"/"+total);

                    break;
            }
        }
    };

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
        //没网的情况下服务未绑定，解绑会报错
        try {
            unbindService(connection);
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serviceIntent = new Intent(this, ServiceCrack.class);
        myApplication=(MyApplication) getApplication();

        setContentView(R.layout.activity_main);

        sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
        editor=sp.edit();

        mRecyclerView = (RecyclerViewStatusSupport)findViewById(R.id.list);
        mRecyclerView.setStatusViews(findViewById(R.id.loading_view), findViewById(R.id.empty_view), findViewById(R.id.error_view));
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loadSign();
                mRecyclerView.showLoadingView();
                refreshMainInfo();
            }
        });

        findViewById(R.id.sign_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SignInfoActivity.class));
            }
        });

        /*
        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignInfoActivity.class));
            }
        });
        */

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.setClickable(true);

        mRecyclerViewAdapter = new SignAdapter(getBaseContext());
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        toLearnProgress=(CircleProgressView)findViewById(R.id.tolearn);
        toLearnProgress.setTextMode(TextMode.TEXT);
        toReviveProgress=(CircleProgressView)findViewById(R.id.torevive);
        toReviveProgress.setTextMode(TextMode.TEXT);
        depositProgress=(CircleProgressView)findViewById(R.id.deposit);
        depositProgress.setTextMode(TextMode.TEXT);
        signInfoMsg=(TextView)findViewById(R.id.sign_info_msg);
        signStart=(Button)findViewById(R.id.start);

        signStart.setOnClickListener(new View.OnClickListener() {
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
                    startActivityForResult(intent, LEARN_WORD);
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

        refreshMainInfo();
        querySql("select id,word from t_words where bookid=2 order by id",new String[]{});
    }

    private void refreshMainInfo(){
        int userid = sp.getInt("userid", 0);
        String token = sp.getString("token", "");
        String nickname = sp.getString("nickname", "");
        String headimg = sp.getString("headimg","");
        String gender = sp.getString("gender", "");
        int bookid = sp.getInt("bookid",0);
        Log.e(TAG, "local bookid:"+bookid);

        User user=new User();
        user.setUserid(userid);
        user.setToken(token);
        user.setBookid(bookid);
        //防止在没网或者loadUserinfo未返回前进入学习单词界面获取不到user对象
        globalInfo.setUser(user);

        if(userid==0 || token==""){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, LOGIN);
        }else{
            loadUserInfo(userid,token);
        }
    }

    private void afterLoadUser(User user){

        storageUser(user);
        globalInfo.setUser(user);
        loadHeadImg(user.getHeadimg());
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
        loadSign();
        loadMainInfo(user.getUserid(),user.getToken());
        if(user.getSignid()==0){
            signInfoMsg.setVisibility(View.VISIBLE);
            signStart.setVisibility(View.VISIBLE);
        }else{
            signInfoMsg.setVisibility(View.INVISIBLE);
            signStart.setVisibility(View.INVISIBLE);
        }
    }

    private void loadMainInfo(final int userid,final String token){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, GlobalInfo.host+"/word/maininfo", MainInfo.class, new GsonRequest.PostGsonRequest<MainInfo>() {
            @Override
            public void onStart() {
                Log.e(TAG, "loadMainInfo");
            }
            @Override
            public void onResponse(MainInfo data) {
                Message msg=new Message();
                msg.what=GET_MININFO;
                if(data.getError()!=null && data.getError()!="" || data.getErrno()!=0){
                    if(data.getErrno()==601){
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN);
                    }else {
                        Toast.makeText(MainActivity.this, "uploadUserInfo error:" + data.getError(), Toast.LENGTH_SHORT).show();
                    }
                    msg.obj=null;
                }else {
                    //本地存储
                    SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                    //存入数据
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("newnum", data.getNewnum());
                    editor.putInt("revivenum", data.getRevivenum());
                    editor.putInt("wordnum", data.getWordnum());
                    editor.commit();

                    msg.obj=data;
                }
                Log.e(TAG, "maininfo sendmsg");
                msg.what=GET_MININFO;
                msg.obj=data;
                handler.sendMessage(msg);
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "maininfo error:"+error.getMessage());
                //Toast.makeText(MainActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
                Message msg=new Message();
                msg.what=GET_MININFO;
                msg.obj=null;
                handler.sendMessage(msg);
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    private void loadUserInfo(final int userid,final String token){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, GlobalInfo.host+"/word/userinfo", User.class, new GsonRequest.PostGsonRequest<User>() {
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
                    Log.e(TAG, "bookid test:" + user.getBookid());
                    afterLoadUser(user);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, error.toString());
                mRecyclerView.showErrorView("网络错误，请检查网络");
                //Toast.makeText(MainActivity.this,  "网络错误，请检查网络", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
                datas.put("os","Android");
                datas.put("versionCode",""+myApplication.getVersionCode());
                datas.put("versionName",myApplication.getVersionName());
                datas.put("comefrom",myApplication.getChannel());
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
        db.execSQL("create table t_words(`id` INTEGER not null,`userid` INTEGER not null,`bookid` integer not null,`word` text not null,`usphone` text default '',`ukphone` text default '',mean text default '',sentence text default '',`review` TIMESTAMP,`status` tinyint not null default 0,`step` tinyint not null default 0,`sync` tinyint not null default 1,offline tinyint not null default 0)");
        db.execSQL("create UNIQUE index iduidbid on t_words(id,userid,bookid)");

        Log.e(TAG, "create db end");

        editor.putBoolean("initDb", true);
        editor.commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult userid:" + requestCode + "," + resultCode);
        User user;
        if(resultCode==RESULT_OK || resultCode==RESULT_FIRST_USER) {
            switch (requestCode) {
                case LOGIN:
                    user = data.getParcelableExtra("user");
                    Log.e(TAG, "onActivityResult userid:" + user.getUserid() + ", nickname:" + user.getNickname()+", headimg:"+user.getHeadimg());
                    loadUserInfo(user.getUserid(), user.getToken());
                    //afterLoadUser(user);
                    break;
                case LEARN_WORD:
                    user=globalInfo.getUser();
                    Log.e(TAG, "learn_word");
                    if(sp.getInt("isComplete",0)==1)
                        loadUserInfo(user.getUserid(), user.getToken());
                    else
                        handler.sendEmptyMessage(LEARN_WORD);
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

        String url = GlobalInfo.host+"/word/sign";

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

                    int currentDateIndex=signs.size();
                    for(int i=0,size=signs.size();i<size;i++){
                        Date d = signs.get(i).getTime();
                        if(TimeHelper.isCurrentDay(d)){
                            currentDateIndex=i;
                            break;
                        }
                    }
                    mRecyclerView.scrollToPosition(currentDateIndex);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG,"loadSign error:"+error.toString());
                mRecyclerView.showErrorView("请检查网络！");
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
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, GlobalInfo.host+"/buy.php", ResponseData.class, new GsonRequest.PostGsonRequest<ResponseData>() {
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
                    Log.e(TAG, "buy:"+user.getData());
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
                Toast.makeText(MainActivity.this,  "网络错误，请检查网络", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid",""+globalInfo.getUser().getUserid());
                datas.put("contractid","2");
                datas.put("wordnum","20");
                datas.put("tradetype","1");
                datas.put("tradeplat","1");
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }
    private int queryReviveSized(int userid,int bookid){
        String [] timeDelta = new String[]{"-5 minute","-30 minute","-480 minute","-720 minute"};
        //i对应的就是status的值
        int toReviveSize=0;
        for(int i=0,size=timeDelta.length;i<size;i++) {
            String reviewSql = "select id from t_words where userid=? and bookid=? and status=? and review<strftime('%s','now', '"+timeDelta[i]+"')";
            int status=i+1;
            Cursor cursor = globalInfo.getDb(getApplicationContext()).rawQuery(reviewSql, new String[]{"" + userid, "" + bookid,""+status});
            toReviveSize+=cursor.getCount();
            cursor.close();
        }
        return toReviveSize;
    }

    private void querySql(String sql,String[] values){
        Cursor cursor = globalInfo.getDb(getApplicationContext()).rawQuery(sql, values);
        int columnSize=cursor.getColumnCount();
        Log.e(TAG, "querySql "+cursor.getCount()+", "+columnSize);
        while (cursor.moveToNext()){
            StringBuilder builder=new StringBuilder();
            for(int i=0;i<columnSize;i++){
                builder.append(cursor.getString(i));
                builder.append(",");
            }
            Log.e(TAG, builder.toString());
            //Log.e(TAG, builder.substring(0,builder.length()-1));
        }
    }
}


