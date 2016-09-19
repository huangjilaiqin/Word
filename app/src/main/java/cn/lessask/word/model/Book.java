package cn.lessask.word.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by laiqin on 16/9/19.
 */
public class Book  implements Parcelable {
    String name;

    public Book(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
    public static final Parcelable.Creator<Book> CREATOR
             = new Parcelable.Creator<Book>() {
         public Book createFromParcel(Parcel in) {
             String name = in.readString();
             return new Book(name);
         }

         public Book[] newArray(int size) {
             return new Book[size];
         }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
