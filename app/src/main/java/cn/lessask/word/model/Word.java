package cn.lessask.word.model;

import java.util.Date;

/**
 * Created by laiqin on 16/9/10.
 */
public class Word {
    private int id;
    private int bookid;
    private String word;
    private String usphone;
    private String ukphone;
    private String mean;
    private String sentence;

    private int status;
    private Date review;

    private int recognizeType;

    public Word() {
    }
    public Word(int id, int bookid, String word, String usphone, String ukphone, String mean, String sentence, int status) {
        this.id = id;
        this.bookid = bookid;
        this.word = word;
        this.usphone = usphone;
        this.ukphone = ukphone;
        this.mean = mean;
        this.sentence = sentence;
        this.status = status;
    }

    public Word(int id, int bookid, String word, String usphone, String ukphone, String mean, String sentence, int status, Date review) {
        this.id = id;
        this.bookid = bookid;
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

    public int getBookid() {
        return bookid;
    }

    public void setBookid(int bookid) {
        this.bookid = bookid;
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

    public int getRecognizeType() {
        return recognizeType;
    }

    public void setRecognizeType(int recognizeType) {
        this.recognizeType = recognizeType;
    }

    public void setReview(Date review) {
        this.review = review;
    }
}
