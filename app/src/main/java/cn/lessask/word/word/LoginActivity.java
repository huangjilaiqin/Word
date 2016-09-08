package cn.lessask.word.word;

import android.content.Intent;
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

import cn.lessask.word.word.dialog.LoadingDialog;
import cn.lessask.word.word.model.LoginQQ;
import cn.lessask.word.word.model.Response;
import cn.lessask.word.word.model.User;
import cn.lessask.word.word.model.UserInfoQQ;
import cn.lessask.word.word.net.GsonRequest;
import cn.lessask.word.word.net.VolleyHelper;
import cn.lessask.word.word.util.GsonTool;

public class LoginActivity extends AppCompatActivity {
    private String TAG = LoginActivity.class.getSimpleName();
    private Tencent mTencent;
    private LoadingDialog loadingDialog;

    private IUiListener iUiListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            System.out.println(o.toString());
            LoginQQ loginQQ = GsonTool.getPerson(o.toString(),LoginQQ.class);
            Toast.makeText(LoginActivity.this, "openid:"+loginQQ.getOpenid(), Toast.LENGTH_SHORT).show();
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

    public void getUserInfo() {

        loadingDialog.show();
        UserInfo info = new UserInfo(LoginActivity.this,mTencent.getQQToken());
        info.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object o) {
                System.out.println(o.toString());

                UserInfoQQ userInfoQQ = GsonTool.getPerson(o.toString(),UserInfoQQ.class);
                //{"ret":0,"msg":"","is_lost":0,"nickname":"唐三炮","gender":"男","province":"","city":"","figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/30","figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/50","figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/100","figureurl_qq_1":"http:\/\/q.qlogo.cn\/qqapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/40","figureurl_qq_2":"http:\/\/q.qlogo.cn\/qqapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/100","is_yellow_vip":"0","vip":"0","yellow_vip_level":"0","level":"0","is_yellow_year_vip":"0"}
                User user = new User();
                user.setHeadimg(userInfoQQ.getFigureurl_2());
                user.setNickname(userInfoQQ.getNickname());
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
        //username qq_openid, wx_openid, wb_openid, mb_18682184215
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/login", User.class, new GsonRequest.PostGsonRequest<User>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(User user) {
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    loadingDialog.cancel();
                    Log.e(TAG, "loadUser onResponse error:" + user.getError() + ", " + user.getErrno());
                    Toast.makeText(LoginActivity.this, user.getError(), Toast.LENGTH_SHORT);
                }else {
                    loadingDialog.cancel();
                    Toast.makeText(LoginActivity.this,  "request ok", Toast.LENGTH_SHORT);
                    //新用户
                    if(user.getNickname().length()==0){
                        getUserInfo();
                    }else{

                    }
                    Log.e(TAG, "loadUser onResponse ok:");
                    /*
                    ContentValues values = new ContentValues();

                    values.put("userid", user.getUserid());
                    values.put("nickname", user.getNickname());
                    values.put("headimg", user.getHeadImg());
                    Cursor cursor = db.rawQuery("select 1 from t_user where userid=?", new String[]{"" + user.getUserid()});
                    if(cursor.getCount()==0){
                        db.insert("t_user", "", values);
                    }else {
                        db.update("t_user", values,"userid=?", new String[]{""+user.getUserid()});
                    }
                    globalInfos.setUser(user);
                    */
                }
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Toast.makeText(LoginActivity.this,  error.toString(), Toast.LENGTH_SHORT);
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("username", username);
            }

        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    private void uploadUserInfo(User user){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/test", Response.class, new GsonRequest.PostGsonRequest<Response>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(Response user) {
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    loadingDialog.cancel();
                    Toast.makeText(LoginActivity.this,  "uploadUserInfo error:"+user.getError(), Toast.LENGTH_SHORT);
                }else {
                    loadingDialog.cancel();
                    Toast.makeText(LoginActivity.this,  "uploadUserInfo success", Toast.LENGTH_SHORT);
                    /*
                    ContentValues values = new ContentValues();

                    values.put("userid", user.getUserid());
                    values.put("nickname", user.getNickname());
                    values.put("headimg", user.getHeadImg());
                    Cursor cursor = db.rawQuery("select 1 from t_user where userid=?", new String[]{"" + user.getUserid()});
                    if(cursor.getCount()==0){
                        db.insert("t_user", "", values);
                    }else {
                        db.update("t_user", values,"userid=?", new String[]{""+user.getUserid()});
                    }
                    globalInfos.setUser(user);
                    */
                }
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Toast.makeText(LoginActivity.this,  error.toString(), Toast.LENGTH_SHORT);
            }
            @Override
            public void setPostData(Map datas) {
                String username = ''
                datas.put("userid", "" + globalInfos.getUserId());
                //datas.put("token", globalInfos.getToken());
            }

        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode,resultCode,data,iUiListener);
    }

}
