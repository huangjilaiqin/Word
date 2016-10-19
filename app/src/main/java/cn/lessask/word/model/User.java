package cn.lessask.word.model;

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
    private String gender;
    private int bookid;
    private int signid;
    private int wordnum;
    private float money;    //帐号余额
    private float deposit;  //押金
    private float getback;  //取回
    private int reachnum;   //达成任务天数

    public User() {
    }

    public User(int userid, String username, String nickname, String token,String headimg,String gender,int bookid) {
        this.userid = userid;
        this.username = username;
        this.nickname = nickname;
        this.token = token;
        this.headimg=headimg;
        this.gender=gender;
        this.bookid=bookid;
    }

    public User(int userid, String username, String nickname, String token,String headimg,String gender,int bookid,int signid,int wordnum,float money,float deposit,float getback,int reachnum) {
        this.userid = userid;
        this.username = username;
        this.nickname = nickname;
        this.token = token;
        this.headimg=headimg;
        this.gender=gender;
        this.bookid=bookid;
        this.signid=signid;
        this.wordnum=wordnum;
        this.money=money;
        this.deposit=deposit;
        this.getback=getback;
        this.reachnum=reachnum;
    }


    public User(int errno, String error, int userid, String username, String nickname, String token,String headimg,String gender,int bookid,int signid,int wordnum,float money,float deposit,float getback,int reachnum) {
        super(errno, error);
        this.userid = userid;
        this.username = username;
        this.nickname = nickname;
        this.token = token;
        this.headimg=headimg;
        this.gender=gender;
        this.bookid=bookid;
        this.signid=signid;
        this.wordnum=wordnum;
        this.money=money;
        this.deposit=deposit;
        this.getback=getback;
        this.reachnum=reachnum;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userid);
        dest.writeString(username);
        dest.writeString(nickname);
        dest.writeString(token);
        dest.writeString(headimg);
        dest.writeString(gender);
        dest.writeInt(bookid);
        dest.writeInt(signid);
        dest.writeInt(wordnum);
        dest.writeFloat(money);
        dest.writeFloat(deposit);
        dest.writeFloat(getback);
        dest.writeInt(reachnum);
    }

    public static final Parcelable.Creator<User> CREATOR
             = new Parcelable.Creator<User>() {
         public User createFromParcel(Parcel in) {
             int userid = in.readInt();
             String username = in.readString();
             String nickname = in.readString();
             String token = in.readString();
             String headimg = in.readString();
             String gender = in.readString();
             int bookid=in.readInt();
             int signid=in.readInt();
             int wordnum=in.readInt();
             float money=in.readFloat();
             float deposit=in.readFloat();
             float getback=in.readFloat();
             int reachnum=in.readInt();

             return new User(userid,username,nickname,token,headimg,gender,bookid,signid,wordnum,money,deposit,getback,reachnum);
         }

         public User[] newArray(int size) {
             return new User[size];
         }
    };

    public int getBookid() {
        return bookid;
    }

    public void setBookid(int bookid) {
        this.bookid = bookid;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

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
