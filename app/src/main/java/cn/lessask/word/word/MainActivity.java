package cn.lessask.word.word;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

import cn.lessask.word.word.model.Response;
import cn.lessask.word.word.model.User;
import cn.lessask.word.word.model.WordList;
import cn.lessask.word.word.net.GsonRequest;
import cn.lessask.word.word.net.VolleyHelper;
import cn.lessask.word.word.util.GlobalInfo;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private final int LOGIN=1;
    private CircleImageView headImg;

    private GlobalInfo globalInfo=GlobalInfo.getInstance();
    private boolean isInitDb=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WordActivity.class);
                startActivity(intent);
            }
        });

        headImg = (CircleImageView)findViewById(R.id.head_img);

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

        int userid = sp.getInt("userid", 0);
        String token = sp.getString("token", "");
        String nickname = sp.getString("nickname", "");
        String headimg = sp.getString("headimg","");
        String gender = sp.getString("gender","");
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
