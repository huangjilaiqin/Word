package cn.lessask.word.net;

import android.util.Log;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by JHuang on 2015/10/18.
 */
public class PostSingle extends Thread{
    private String TAG = PostSingle.class.getSimpleName();
    private String host;
    private PostSingleEvent postSingleEvent;
    private PostResponse postResponse;


    public PostSingle(String host, PostSingleEvent postSingleEvent) {
        this.host = host;
        this.postSingleEvent = postSingleEvent;
    }

    @Override
    public void run() {
        postSingleEvent.onStart();
        Map<String, String> headers = postSingleEvent.getHeaders();
        Map<String, String> files = postSingleEvent.getFiles();
        Map<String, String> images = postSingleEvent.getImages();
        MultipartEntity multipartEntity = null;
        //Log.e(TAG, "postSingle url:"+host);
        try {
            multipartEntity = new MultipartEntity(host);
            if(headers!=null){
                Iterator iterator = headers.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    String name = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    Log.e(TAG, "headers: "+name+", "+value);
                    multipartEntity.addStringPart(name, value);
                }
            }
            if(files!=null){
                Iterator iterator = files.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    String name = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    //Log.e(TAG, "files: " + name + ", " + value);
                    multipartEntity.addFilePart(name, new File(value));
                }
            }
            if(images!=null){
                Iterator iterator = images.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry entry = (Map.Entry)iterator.next();
                    String name = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    Log.e(TAG, "images: "+name+", "+value);
                    multipartEntity.addOptimizeImagePart(name, new File(value));
                }
            }
            postResponse = multipartEntity.end();
        }catch (Exception e){
            Log.e(TAG, "MultipartEntity Exception:" + e.toString());
            postSingleEvent.onError(e.toString());
        }finally {
            if(postResponse!=null){
                int code = postResponse.getCode();
                if(postResponse.getCode()!=200){
                    postSingleEvent.onError("error code:"+code);
                }else {
                    postSingleEvent.onDone(postResponse);
                }
            }
            if(multipartEntity!=null)
                multipartEntity.close();
        }
    }
}
