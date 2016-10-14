package cn.lessask.word.model;

import java.util.Date;

/**
 * Created by huangji on 2016/10/14.
 */
public class Sign {
    private int id;
    private Date time;
    private int revivenum;
    private int newnum;
    private int status;

    public Sign(int id, Date time, int revivenum, int newnum, int status) {
        this.id = id;
        this.time = time;
        this.revivenum = revivenum;
        this.newnum = newnum;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getRevivenum() {
        return revivenum;
    }

    public void setRevivenum(int revivenum) {
        this.revivenum = revivenum;
    }

    public int getNewnum() {
        return newnum;
    }

    public void setNewnum(int newnum) {
        this.newnum = newnum;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
