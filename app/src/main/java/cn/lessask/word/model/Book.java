package cn.lessask.word.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by laiqin on 16/9/19.
 */
public class Book  implements Parcelable {
    private int bookid;
    private String name;
    private float completeness;
    private int iscurrent;

    public Book(int bookid,String name,float completeness,int iscurrent) {
        this.bookid=bookid;
        this.name = name;
        this.completeness=completeness;
        this.iscurrent=iscurrent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookid);
        dest.writeString(name);
        dest.writeFloat(completeness);
        dest.writeInt(iscurrent);
    }
    public static final Parcelable.Creator<Book> CREATOR
             = new Parcelable.Creator<Book>() {
         public Book createFromParcel(Parcel in) {
             int bookid=in.readInt();
             String name = in.readString();
             float completeness = in.readFloat();
             int iscurrent = in.readInt();
             return new Book(bookid,name,completeness,iscurrent);
         }

         public Book[] newArray(int size) {
             return new Book[size];
         }
    };

    public float getCompleteness() {
        return completeness;
    }

    public void setCompleteness(float completeness) {
        this.completeness = completeness;
    }

    public int getIscurrent() {
        return iscurrent;
    }

    public void setIscurrent(int iscurrent) {
        this.iscurrent = iscurrent;
    }

    public int getBookid() {
        return bookid;
    }

    public void setBookid(int bookid) {
        this.bookid = bookid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
