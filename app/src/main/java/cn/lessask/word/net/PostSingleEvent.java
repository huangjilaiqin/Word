package cn.lessask.word.net;

import java.util.Map;

/**
 * Created by JHuang on 2015/10/18.
 */
public interface PostSingleEvent {
    void onStart();
    void onDone(PostResponse response);
    void onError(String err);
    Map<String, String> getHeaders();
    Map<String, String> getFiles();
    Map<String, String> getImages();
}
