package cn.lessask.word.word.net;

/**
 * Created by JHuang on 2015/10/18.
 */
public class PostResponse {
    private int code;
    private String body;

    public PostResponse(int code, String body) {
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

}
