package cn.lessask.word.word;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.open.utils.HttpUtils;
import com.tencent.tauth.IRequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import cn.lessask.word.word.dialog.LoadingDialog;
import cn.lessask.word.word.model.LoginQQ;
import cn.lessask.word.word.model.UserInfoQQ;
import cn.lessask.word.word.util.GsonTool;

public class LoginActivity extends AppCompatActivity {
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
            getUserInfo();
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

        UserInfo info = new UserInfo(LoginActivity.this,mTencent.getQQToken());
        info.getUserInfo(new IUiListener() {
        @Override
        public void onComplete(Object o) {
            System.out.println(o.toString());

            UserInfoQQ userInfoQQ = GsonTool.getPerson(o.toString(),UserInfoQQ.class);
            //{"ret":0,"msg":"","is_lost":0,"nickname":"唐三炮","gender":"男","province":"","city":"","figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/30","figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/50","figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/100","figureurl_qq_1":"http:\/\/q.qlogo.cn\/qqapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/40","figureurl_qq_2":"http:\/\/q.qlogo.cn\/qqapp\/1105464601\/F4327C81A7510540DCB6E9759010348F\/100","is_yellow_vip":"0","vip":"0","yellow_vip_level":"0","level":"0","is_yellow_year_vip":"0"}
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode,resultCode,data,iUiListener);
    }

}
