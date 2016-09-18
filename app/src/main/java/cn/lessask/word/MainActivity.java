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

    private final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAK18h0X4GvsC1vTD26CTS/O+/HvjfQ74EDo74gS2ALGm/UD4adjqVHuvXLwtJzm3/ghKmybbO5h7PPLfKoF+fi5SmzDMpqDiv+7t+KartgmqEbtG7f6UuTgMPw1t8wtotv+l6TyOZ8PQlv8gUxbRtrKrRGaussGdk8CSNdk2ajQzAgMBAAECgYA2ig6QzZXV0ae4HRafnY7kGuePHw5CtXOMiyTb7Ee9kczOLwo/mjNjCtcxhVRujcw72RB7n7JVlnCrvcLEIPsTmk3FBzP3QbGeX3CSCWhVcwWdXckc8RdW2ECzWv8gIbgvyvyJi39VxqMToYmGSYgD2eYZqoqPvCduktM5KC9e4QJBAN4wXpfdWCvPCuSCvwiHnm+gJxBW2yRamkLCrUi+dtbh9sXgHsNGWqdFLYY1Y0VNPWzGaj1ZJDbTLPQcMW5IwVECQQDH4uVURbGQbIKPJVhfhUL3NYwR1IXurrYL5Iepavbxeo8kPy9czp37ZbIu27ZeTDEEd1s6XW0Oynuog+R/3txDAkAt0BlhBNGuTsV3MoJDNvtzFrmXQ+FxkIDoLQ3fxu3oBrWEPV76cqI0hS4K0y1B19hHem3jcmLmLwrA1qNWkwfRAkEAk5dm7BXV6aUlthrGKSnV64FfXp8FEdtxUlC5FxCDLITpgsMg7q8lmcZajhzviKtLmlejRfJMH5rFOgXBR+W/zwJAAzX1ZbiMUqxCe2ixgb3mvx/Sobjp9UrS9oGp8fzumkes23nueHtFOqZW1/fLaqoQijw3oCfIp+cvf7+wBu2c/Q==";

    private GlobalInfo globalInfo=GlobalInfo.getInstance();
    private boolean isInitDb=false;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //myBinder = (MyService.MyBinder) service;
            //myBinder.startDownload();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void callAliPay(){
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap("2016091600523406");
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
        String sign = OrderInfoUtil2_0.getSign(params, RSA_PRIVATE);
        final String orderInfo = orderParam + "&" + sign;

        Runnable payRunnable = new Runnable() {




            @Override
            public void run() {
                PayTask alipay = new PayTask(MainActivity.this);
                Map<String,String> result = alipay.payV2(orderInfo,true);

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent bindIntent = new Intent(this, ServiceCrack.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);
        findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WordActivity.class);
                startActivity(intent);
            }
        });

        headImg = (CircleImageView)findViewById(R.id.head_img);
        headImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callAliPay();
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
        String gender = sp.getString("gender","");
        Log.e(TAG, "userid "+userid);
        Log.e(TAG, "token "+token);
        Log.e(TAG, "token "+nickname);
        Log.e(TAG, "token "+headimg);
        Log.e(TAG, "token "+gender);
        if(userid==0 || token==""){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, LOGIN);
        }else{
            if(nickname==""||headimg==""){
                //加载用户信息
                loadUserInfo(userid,token);
            }else{
                User user = new User(userid,"",nickname,token,headimg,gender);
                globalInfo.setUser(user);
                loadHeadImg(user.getHeadimg());

                if(isInitDb) {
                    Toast.makeText(MainActivity.this,"changeList",Toast.LENGTH_SHORT).show();
                    changeList(userid,token,0,1);
                    isInitDb=false;
                }
            }
        }
    }

    private void changeList(final int userid,final String token,final int id,final int wtype){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/changelist", WordList.class, new GsonRequest.PostGsonRequest<WordList>() {
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
                                values.put("wtype",wtype);
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
                //设置wtype
                SharedPreferences sp = MainActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                //存入数据
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("wtype", wtype);
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
                datas.put("wtype",""+wtype);
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

    private void initDb(SharedPreferences baseInfo){
        SharedPreferences.Editor editor = baseInfo.edit();
        //初始化数据库
        SQLiteDatabase db = globalInfo.getDb(getApplicationContext());

        Log.e(TAG, "create db begin");
        //db.execSQL("drop table t_words");
        db.execSQL("create table t_words(`id` INTEGER primary key,`userid` INTEGER not null,`wtype` integer not null,`word` text not null,`usphone` text default '',`ukphone` text default '',mean text default '',sentence text default '',`review` TIMESTAMP,`status` tinyint not null default 0,`sync` tinyint not null default 1)");

        Log.e(TAG, "create db end");

        editor.putBoolean("initDb", true);
        editor.commit();
        isInitDb=true;
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
                    Toast.makeText(MainActivity.this,user.getNickname(),Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onActivityResult userid:" + user.getUserid() + ", nickname:" + user.getNickname());

                    storageUser(user);
                    globalInfo.setUser(user);
                    loadHeadImg(user.getHeadimg());
                    if(isInitDb) {
                        Toast.makeText(MainActivity.this,"changeList",Toast.LENGTH_SHORT).show();
                        changeList(user.getUserid(),user.getToken(),0,1);
                        isInitDb=false;
                    }
                    break;
            }
        }
    }
}