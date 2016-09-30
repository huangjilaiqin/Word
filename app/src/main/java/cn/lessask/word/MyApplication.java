package cn.lessask.word;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;


import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import cn.lessask.word.net.VolleyHelper;
import okhttp3.OkHttpClient;

/**
 * Created by huangji on 2015/8/12.
 */
public class MyApplication extends Application{
    private String TAG = MyApplication.class.getSimpleName();
    private String channel = "unknow";
    @Override
    public void onCreate() {
        super.onCreate();
        VolleyHelper.setmCtx(getApplicationContext());

        try {
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            channel=appInfo.metaData.getString("CHANNEL");
        }catch (PackageManager.NameNotFoundException e){
            Log.e(TAG, "getChannel Error:" + e);
        }
        Constant.setContext(getApplicationContext());

        Stetho.initializeWithDefaults(this);
        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }

    public String getChannel(){
        return channel;
    }
}
