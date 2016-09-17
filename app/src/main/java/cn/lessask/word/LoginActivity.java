package cn.lessask.word;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
;
import java.util.Map;

import cn.lessask.word.dialog.LoadingDialog;
import cn.lessask.word.model.LoginQQ;
import cn.lessask.word.model.Response;
import cn.lessask.word.model.User;
import cn.lessask.word.model.UserInfoQQ;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.util.GsonTool;

public class LoginActivity extends AppCompatActivity {
    private String TAG = LoginActivity.class.getSimpleName();
    private Tencent mTencent;
    private LoadingDialog loadingDialog;

    private IUiListener iUiListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            System.out.println(o.toString());
            LoginQQ loginQQ = GsonTool.getPerson(o.toString(),LoginQQ.class);
            //Toast.makeText(LoginActivity.this, "openid:"+loginQQ.getOpenid(), Toast.LENGTH_SHORT).show();
            //登录成功后mTencent中的token和openid还是null的,需要自己设置否者后面的请求会出现 client request's parameters are invalid, invalid openid错误
            mTencent.setAccessToken(loginQQ.getAccess_token(),""+loginQQ.getExpires_in());
            mTencent.setOpenId(loginQQ.getOpenid());

            String username = "qq_"+loginQQ.getOpenid();
            login(username);
        }

        @Override
        public void onError(UiError uiError) {
            loadingDialog.cancel();
            Toast.makeText(LoginActivity.this, uiError.toString(), Toast.LENGTH_SHORT).show();
            System.out.println("onError");
        }

        @Override
        public void onCancel() {
            loadingDialog.cancel();
            System.out.println("onCancel");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.qq_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qqLogin();
            }
        });
    }

    public void qqLogin() {
        mTencent = Tencent.createInstance("1105464601", this.getApplicationContext());
        if (!mTencent.isSessionValid())
        {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.show();
            mTencent.login(this, "get_simple_userinfo,add_topic", iUiListener);
        }
    }

    public void getUserInfo(final User user) {

        loadingDialog.show();
        UserInfo info = new UserInfo(LoginActivity.this,mTencent.getQQToken());
        info.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object o) {
                System.out.println(o.toString());

                UserInfoQQ userInfoQQ = GsonTool.getPerson(o.toString(), UserInfoQQ.class);
                //{"ret":0,"msg":"","is_lost":0,"nickname":"唐三炮","gender":"男","province":"","city":"","figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/30","figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/50","figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/100","figureurl_qq_1":"http:\/\/q.qlogo.cn\/qqapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/40","figureurl_qq_2":"http:\/\/q.qlogo.cn\/qqapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/100","is_yellow_vip":"0","vip":"0","yellow_vip_level":"0","level":"0","is_yellow_year_vip":"0"}
                user.setNickname(userInfoQQ.getNickname());
                user.setHeadimg(userInfoQQ.getFigureurl_qq_2());
                user.setGender(userInfoQQ.getGender());
                uploadUserInfo(user);
            }

            @Override
            public void onError(UiError uiError) {
                loadingDialog.cancel();
                Toast.makeText(LoginActivity.this, uiError.toString(), Toast.LENGTH_SHORT).show();
                System.out.println("onError");
            }

            @Override
            public void onCancel() {
                loadingDialog.cancel();
                System.out.println("onCancel");
            }
        });
        //info.getOpenId(iUiListener);
    }

    private void login(final String username){
        final String channel = ((MyApplication)getApplication()).getChannel();

        //username qq_openid, wx_openid, wb_openid, mb_18682184215
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/login", User.class, new GsonRequest.PostGsonRequest<User>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(User user) {
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    loadingDialog.cancel();
                    Log.e(TAG, "loadUser onResponse error:" + user.getError() + ", " + user.getErrno());
                    Toast.makeText(LoginActivity.this, user.getError(), Toast.LENGTH_SHORT).show();
                }else {
                    loadingDialog.cancel();
                    //Toast.makeText(LoginActivity.this,  "login ok:"+user.getNickname(), Toast.LENGTH_SHORT).show();
                    //新用户
                    if(user.getNickname()==null || user.getNickname().length()==0){
                        getUserInfo(user);
                    }else{
                        //存储 userid,token,nickname,headimg,gender
                        storageUser(user);
                        //返回主界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("user",user);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        LoginActivity.this.setResult(1,intent);
                        finish();
                    }
                }
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Toast.makeText(LoginActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("username", username);
                datas.put("comefrom", channel);
            }

        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    private void uploadUserInfo(final User user){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/uploaduserinfo", Response.class, new GsonRequest.PostGsonRequest<Response>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(Response resp) {
                if(resp.getError()!=null && resp.getError()!="" || resp.getErrno()!=0){
                    loadingDialog.cancel();
                    Toast.makeText(LoginActivity.this,  "uploadUserInfo error:"+user.getError(), Toast.LENGTH_SHORT).show();
                }else {
                    loadingDialog.cancel();
                    //本地存储
                    storageUser(user);
                    //回到主界面
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("user",user);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    LoginActivity.this.setResult(1,intent);
                    finish();

                }
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Toast.makeText(LoginActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + user.getUserid());
                datas.put("token", user.getToken());
                datas.put("nickname", user.getNickname());
                datas.put("headimg", user.getHeadimg());
                datas.put("gender", user.getGender());
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode,resultCode,data,iUiListener);
    }

}
