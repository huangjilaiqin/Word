package cn.lessask.word.word;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Tencent mTencent;
    private IUiListener listener;
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

        mTencent = Tencent.createInstance("1105464601", this.getApplicationContext());
        listener = new BaseUiListener() {
            @Override
            protected void doComplete(JSONObject values) {
                //updateLoginButton();
            }
        };
        findViewById(R.id.qqlogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTencent.isSessionValid())
                {
                    mTencent.login(MainActivity.this, "all", listener);
                }
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
    }
    private class BaseUiListener implements IUiListener {
        @Override
        public void onComplete(Object response) {
            //mBaseMessageText.setText("onComplete:");
            //mMessageText.setText(response.toString());
            //doComplete(response);
        }
        protected void doComplete(JSONObject values) {
        }
        @Override
        public void onError(UiError e) {
            //showResult("onError:", "code:" + e.errorCode + ", msg:"+ e.errorMessage + ", detail:" + e.errorDetail);
        }
        @Override
        public void onCancel() {
            //showResult("onCancel", "");
        }
    }
}
