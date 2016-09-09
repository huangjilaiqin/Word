package cn.lessask.word.word.util;

import cn.lessask.word.word.model.User;

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

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
