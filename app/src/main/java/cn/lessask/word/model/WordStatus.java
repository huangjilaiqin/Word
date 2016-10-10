package cn.lessask.word.model;

/**
 * Created by huangji on 2016/10/10.
 */
public class WordStatus {
    private int wid;
    private int status;
    private int review;

    public WordStatus(int wid, int status, int review) {
        this.wid = wid;
        this.status = status;
        this.review = review;
    }

    public int getWid() {
        return wid;
    }

    public void setWid(int wid) {
        this.wid = wid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getReview() {
        return review;
    }

    public void setReview(int review) {
        this.review = review;
    }
}
