package cn.lessask.word.word.model;

import java.util.ArrayList;

/**
 * Created by JHuang on 2016/1/10.
 */
public class ArrayListResponse<T> extends Response{
    private ArrayList<T> datas;

    public ArrayListResponse(ArrayList<T> datas) {
        this.datas = datas;
    }

    public ArrayListResponse(int errno, String error, ArrayList<T> datas) {
        super(errno, error);
        this.datas = datas;
    }

    public ArrayList<T> getDatas() {
        return datas;
    }

    public void setDatas(ArrayList<T> datas) {
        this.datas = datas;
    }
}
