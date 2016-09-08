package cn.lessask.word.word;

import android.app.Application;


import cn.lessask.word.word.net.VolleyHelper;

/**
 * Created by huangji on 2015/8/12.
 */
public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        VolleyHelper.setmCtx(getApplicationContext());
    }
}
