package cn.lessask.word.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huangji on 2016/10/17.
 */
public class MainInfo extends Response{
    private int newnum;
    private int revivenum;
    private int wordnum;
    private int isComplete;

    public MainInfo(int newnum, int revivenum,int wordnum) {
        this.newnum = newnum;
        this.revivenum = revivenum;
        this.wordnum=wordnum;
    }

    public MainInfo(int newnum, int revivenum,int wordnum,int isComplete) {
        this.newnum = newnum;
        this.revivenum = revivenum;
        this.wordnum=wordnum;
        this.isComplete=isComplete;
    }

    public MainInfo(int errno, String error, int newnum, int revivenum,int wordnum) {
        super(errno, error);
        this.newnum = newnum;
        this.revivenum = revivenum;
        this.wordnum=wordnum;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(newnum);
        dest.writeInt(revivenum);
        dest.writeInt(wordnum);
        dest.writeInt(isComplete);
    }

    public static final Parcelable.Creator<MainInfo> CREATOR
             = new Parcelable.Creator<MainInfo>() {
         public MainInfo createFromParcel(Parcel in) {
             int newnum = in.readInt();
             int revivenum = in.readInt();
             int wordnum = in.readInt();
             int isComplete = in.readInt();
             return new MainInfo(newnum,revivenum,wordnum,isComplete);
         }

         public MainInfo[] newArray(int size) {
             return new MainInfo[size];
         }
    };

    public int getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(int isComplete) {
        this.isComplete = isComplete;
    }

    public int getWordnum() {
        return wordnum;
    }

    public void setWordnum(int wordnum) {
        this.wordnum = wordnum;
    }

    public int getNewnum() {
        return newnum;
    }

    public void setNewnum(int newnum) {
        this.newnum = newnum;
    }

    public int getRevivenum() {
        return revivenum;
    }

    public void setRevivenum(int revivenum) {
        this.revivenum = revivenum;
    }
}
