package cn.lessask.word.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huangji on 2016/10/13.
 */
public class Contract extends Response{
    private int id;
    private int days;
    private int golden;

    public Contract(int id, int days, int golden) {
        this.id = id;
        this.days = days;
        this.golden = golden;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(days);
        dest.writeInt(golden);
    }
    public static final Parcelable.Creator<Contract> CREATOR
             = new Parcelable.Creator<Contract>() {
         public Contract createFromParcel(Parcel in) {
             int id=in.readInt();
             int days=in.readInt();
             int golden= in.readInt();
             return new Contract(id,days,golden);
         }

         public Contract[] newArray(int size) {
             return new Contract[size];
         }
    };


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getGolden() {
        return golden;
    }

    public void setGolden(int golden) {
        this.golden = golden;
    }
}
