package cn.lessask.word.word.model;

import java.util.Date;

/**
 * Created by laiqin on 16/9/10.
 */
public class Word {
    private int id;
    private int wtype;
    private String word;
    private String usphone;
    private String ukphone;
    private String mean;
    private String sentence;

    private int status;
    private Date review;

    public Word() {
    }
    public Word(int id, int wtype, String word, String usphone, String ukphone, String mean, String sentence, int status) {
        this.id = id;
        this.wtype = wtype;
        this.word = word;
        this.usphone = usphone;
        this.ukphone = ukphone;
        this.mean = mean;
        this.sentence = sentence;
        this.status = status;
    }

    public Word(int id, int wtype, String word, String usphone, String ukphone, String mean, String sentence, int status, Date review) {
        this.id = id;
        this.wtype = wtype;
        this.word = word;
        this.usphone = usphone;
        this.ukphone = ukphone;
        this.mean = mean;
        this.sentence = sentence;
        this.status = status;
        this.review = review;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWtype() {
        return wtype;
    }

    public void setWtype(int wtype) {
        this.wtype = wtype;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getUsphone() {
        return usphone;
    }

    public void setUsphone(String usphone) {
        this.usphone = usphone;
    }

    public String getUkphone() {
        return ukphone;
    }

    public void setUkphone(String ukphone) {
        this.ukphone = ukphone;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getReview() {
        return review;
    }

    public void setReview(Date review) {
        this.review = review;
    }
}
