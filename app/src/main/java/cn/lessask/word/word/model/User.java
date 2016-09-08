package cn.lessask.word.word.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by laiqin on 16/9/8.
 */
public class User extends Response{
    private int userid;
    private String username;
    private String nickname;
    private String token;
    private String headimg;

    public User() {
    }

    public User(int userid, String username, String nickname, String token,String headimg) {
        this.userid = userid;
        this.username = username;
        this.nickname = nickname;
        this.token = token;
        this.headimg=headimg;
    }

    public User(int errno, String error, int userid, String username, String nickname, String token,String headimg) {
        super(errno, error);
        this.userid = userid;
        this.username = username;
        this.nickname = nickname;
        this.token = token;
        this.headimg=headimg;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userid);
        dest.writeString(username);
        dest.writeString(nickname);
        dest.writeString(token);
        dest.writeString(headimg);
    }

    public static final Parcelable.Creator<User> CREATOR
             = new Parcelable.Creator<User>() {
         public User createFromParcel(Parcel in) {
             int userid = in.readInt();
             String username = in.readString();
             String nickname = in.readString();
             String token = in.readString();
             String headimg = in.readString();

             return new User(userid,username,nickname,token,headimg);
         }

         public User[] newArray(int size) {
             return new User[size];
         }
    };

    public String getHeadimg() {
        return headimg;
    }

    public void setHeadimg(String headimg) {
        this.headimg = headimg;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
