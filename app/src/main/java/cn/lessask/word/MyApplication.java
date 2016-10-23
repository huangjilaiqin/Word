package cn.lessask.word;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
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
    private int versionCode;
    private String versionName;
    @Override
    public void onCreate() {
        super.onCreate();
        VolleyHelper.setmCtx(getApplicationContext());

        try {
            PackageManager packageManager = this.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            channel=appInfo.metaData.getString("CHANNEL");

            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            versionCode=packageInfo.versionCode;
            versionName=packageInfo.versionName;
        }catch (PackageManager.NameNotFoundException e){
            Log.e(TAG, "getChannel Error:" + e);
        }
        Constant.setContext(getApplicationContext());

        /*
        Stetho.initializeWithDefaults(this);
        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
                */

        Stetho.initialize(Stetho
                .newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(
                        Stetho.defaultInspectorModulesProvider(this)).build());
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getChannel(){
        return channel;
    }
}
