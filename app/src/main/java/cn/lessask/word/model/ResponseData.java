package cn.lessask.word.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by laiqin on 16/10/7.
 */
public class ResponseData extends Response{
    private String data;

    public ResponseData(String data) {
        this.data = data;
    }

    public ResponseData(int errno, String error, String data) {
        super(errno, error);
        this.data = data;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
    }

    public static final Parcelable.Creator<ResponseData> CREATOR
             = new Parcelable.Creator<ResponseData>() {
         public ResponseData createFromParcel(Parcel in) {
             String data = in.readString();
             return new ResponseData(data);
         }

         public ResponseData[] newArray(int size) {
             return new ResponseData[size];
         }
    };

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
