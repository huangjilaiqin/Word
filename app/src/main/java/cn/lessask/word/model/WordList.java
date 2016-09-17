package cn.lessask.word.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by laiqin on 16/9/10.
 */
public class WordList extends Response{
    private String words;
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(words);
    }

    public static final Parcelable.Creator<WordList> CREATOR
             = new Parcelable.Creator<WordList>() {
         public WordList createFromParcel(Parcel in) {
             String words = in.readString();
             return new WordList(words);
         }

         public WordList[] newArray(int size) {
             return new WordList[size];
         }
    };

    public WordList(String words) {
        this.words = words;
    }

    public WordList(int errno, String error, String words) {
        super(errno, error);
        this.words = words;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }
}
