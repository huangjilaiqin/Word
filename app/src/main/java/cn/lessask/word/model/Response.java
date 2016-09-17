package cn.lessask.word.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huangji on 2015/8/12.
 */
public class Response implements Parcelable {
    private String error;
    private int errno;

    public Response(){
    }
    public Response(int errno, String error){
        this.errno = errno;
        this.error = error;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(error);
        dest.writeInt(errno);
    }

    public static final Creator<Response> CREATOR
             = new Creator<Response>() {
         public Response createFromParcel(Parcel in) {
             String error = in.readString();
             int errno = in.readInt();

             return new Response(errno,error);
         }

         public Response[] newArray(int size) {
             return new Response[size];
         }
    };

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }
}
