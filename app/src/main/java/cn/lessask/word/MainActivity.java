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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.Map;

import cn.lessask.word.alipay.OrderInfoUtil2_0;
import cn.lessask.word.model.Response;
import cn.lessask.word.model.ResponseData;
import cn.lessask.word.model.User;
import cn.lessask.word.model.WordList;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.util.GlobalInfo;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private final int LOGIN=1;
    private CircleImageView headImg;

    private GlobalInfo globalInfo=GlobalInfo.getInstance();

    private ServiceInterFace serviceInterFace;
    private Intent serviceIntent;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceInterFace=(ServiceInterFace)service;
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

        /*
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
        Log.e(TAG, "orderParam "+orderParam);
        String sign = OrderInfoUtil2_0.getSign(params, RSA_PRIVATE);
        Log.e(TAG, "sign "+sign);
        final String orderInfo = orderParam + "&" + sign;

        Log.e(TAG, "orderInfo: "+orderInfo);

        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(MainActivity.this);
                String v = alipay.getVersion();
                Log.e(TAG, "version:"+v);
                Map<String,String> result = alipay.payV2(orderInfo,true);
                //alipay.h5Pay(orderInfo,true);


                Log.e(TAG, "pay cb");
                //Message msg = new Message();
                //msg.what = SDK_PAY_FLAG;
                //msg.obj = result;
                //mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent(this, ServiceCrack.class);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);
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

        if(userid==0 || token==""){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, LOGIN);
        }else{
            if(nickname==""||headimg==""){
                //加载用户信息
                loadUserInfo(userid,token);
            }else{
                User user = new User(userid,"",nickname,token,headimg,gender,bookid);
                globalInfo.setUser(user);
                loadHeadImg(user.getHeadimg());
            }
        }
    }

    private void changeList(final int userid,final String token,final int id,final int bookid){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/changebook", WordList.class, new GsonRequest.PostGsonRequest<WordList>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(WordList user) {
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    if(user.getErrno()==601){
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN);
                    }else {
                        Toast.makeText(MainActivity.this, "changeList error:" + user.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //本地存储
                    final String wordsStr = user.getWords();
                    final String[] words = wordsStr.split(";");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i=0,size=words.length;i<size;i++){
                                String[] info = words[i].split(":");
                                ContentValues values = new ContentValues();
                                values.put("id",info[0]);
                                values.put("word",info[1]);
                                values.put("userid",userid);
                                values.put("bookid",bookid);
                                globalInfo.getDb(MainActivity.this).insert("t_words",null,values);
                            }
                            Log.e(TAG, "insert done");
                        }
                    }).start();
                }
                Cursor cursor = globalInfo.getDb(MainActivity.this).rawQuery("select count(id) as num from t_words",null);
                while (cursor.moveToNext()){
                    int num=cursor.getInt(0);
                    Log.e(TAG, "size:"+num);
                }
                cursor.close();
                //设置bookid
                SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                //存入数据
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("bookid", bookid);
                editor.commit();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(MainActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
                datas.put("id",""+id);
                datas.put("bookid",""+bookid);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
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
                    storageUser(user);
                    globalInfo.setUser(user);
                    loadHeadImg(user.getHeadimg());
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
        values.put("userid",user.getUserid());
        values.put("token", user.getToken());
        values.put("nickname", user.getNickname());
        values.put("headimg", user.getHeadimg());
        values.put("gender",user.getGender());
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
        db.execSQL("create table t_words(`id` INTEGER primary key,`userid` INTEGER not null,`bookid` integer not null,`word` text not null,`usphone` text default '',`ukphone` text default '',mean text default '',sentence text default '',`review` TIMESTAMP,`status` tinyint not null default 0,`sync` tinyint not null default 1,offline tinyint not null default 0)");

        Log.e(TAG, "create db end");

        editor.putBoolean("initDb", true);
        editor.commit();
        Toast.makeText(MainActivity.this,"initDb",Toast.LENGTH_SHORT).show();

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

                    int bookid = user.getBookid();
                    if(bookid>0){
                        //检查t_words是否下载了相应的词库
                        String[] where = new String[]{""+user.getUserid(),""+bookid};
                        Cursor cursor = globalInfo.getDb(MainActivity.this).rawQuery("select count(id) as num from t_words where userid=? and bookid=?",where);
                        if(cursor.getCount()==0)
                            changeList(user.getUserid(),user.getToken(),0,bookid);
                    }

                    break;
            }
        }
    }

}
