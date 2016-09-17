package cn.lessask.word;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by laiqin on 16/9/16.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context,ServiceCrack.class);
        context.startService(service);
        Log.d("TAG1","开机自启动服务");

    }
}
