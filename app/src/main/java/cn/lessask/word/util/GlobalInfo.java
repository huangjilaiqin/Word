package cn.lessask.word.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import cn.lessask.word.model.User;

/**
 * Created by laiqin on 16/4/3.
 */
public class GlobalInfo {

    private GlobalInfo(){}
    public static final GlobalInfo getInstance(){
        return LazyHolder.INSTANCE;
    }
    private static class LazyHolder {
        private static final GlobalInfo INSTANCE = new GlobalInfo();
    }

    //支付宝沙箱appid 2016091600523406

    private User user;

    public User getUser() {
        if(user==null){
            /*
            SharedPreferences sp = .getSharedPreferences("SP", MODE_PRIVATE);
            //为审核伪造的用户信息

            //User tuser = new User(1,"qq_F4327C81A7510540DCB6E9759010348F","唐三炮","272233d9580dab2c9e432992a0659b88","http://q.qlogo.cn/qqapp/1105464601/F4327C81A7510540DCB6E9759010348F/100","男");
            //storageUser(tuser);

            int userid = sp.getInt("userid", 0);
            String token = sp.getString("token", "");
            String nickname = sp.getString("nickname", "");
            String headimg = sp.getString("headimg","");
            String gender = sp.getString("gender","");
            */
        }
        return user;
    }

    public static String host="https://www.91less.com";

    public void setUser(User user) {
        this.user = user;
    }
    public SQLiteDatabase getDb(Context context){
        return DbHelper.getInstance(context).getDb();
    }
}
