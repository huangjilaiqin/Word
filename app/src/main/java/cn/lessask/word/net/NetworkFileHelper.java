package cn.lessask.word.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JHuang on 2015/12/9.
 */
public class NetworkFileHelper {
    private final String TAG = NetworkFileHelper.class.getSimpleName();
    private final int REQUEST_START=1;
    private final int REQUEST_DONE=2;
    private final int REQUEST_ERROR=3;
    private Gson gson = new Gson();
    private Map<Integer, PostFileRequest> postFileRequests = new HashMap<>();
    private Map<Integer, GetFileRequest> getFileRequests = new HashMap<>();
    private int POST_FILE = 0;
    private int GET_FILE = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==POST_FILE) {
                int tag = msg.arg2;
                PostFileRequest postFileRequest = postFileRequests.get(tag);
                switch (msg.what) {
                    case REQUEST_START:
                        postFileRequest.onStart();
                        break;
                    case REQUEST_DONE:
                        postFileRequest.onResponse(msg.obj);
                        postFileRequests.remove(tag);
                        break;
                    case REQUEST_ERROR:
                        postFileRequest.onError((String) msg.obj);
                        postFileRequests.remove(tag);
                        break;
                }
            }else if(msg.arg1==GET_FILE){
                int tag = msg.arg2;
                GetFileRequest getFileRequest = getFileRequests.get(tag);
                switch (msg.what) {
                    case REQUEST_START:
                        getFileRequest.onStart();
                        break;
                    case REQUEST_DONE:
                        getFileRequest.onResponse((String)msg.obj);
                        getFileRequests.remove(tag);
                        break;
                    case REQUEST_ERROR:
                        getFileRequest.onError((String) msg.obj);
                        getFileRequests.remove(tag);
                        break;
                }
            }
        }
    };
    private NetworkFileHelper(){

    }
    public static NetworkFileHelper getInstance(){
        return LazyHolder.INSTANCE;
    }
    private static class LazyHolder {
        private static final NetworkFileHelper INSTANCE = new NetworkFileHelper();
    }

    /*
    * 自定义post接口
    * 支持post字段
    * 文件
    * 图片(压缩过的图片)
    * */
    public interface PostFileRequest{
        void onStart();
        void onResponse(Object response);
        void onError(String error);
        Map<String, String> getHeaders();
        Map<String, String> getFiles();
        Map<String, String> getImages();
    }
    public interface GetFileRequest{
        void onStart();
        void onResponse(String error);
        void onError(String error);
    }

    public void startPost(String url, final Class responseClass, final PostFileRequest postFile){
        final int tag = postFileRequests.size();
        postFileRequests.put(tag, postFile);
        PostSingleEvent event = new PostSingleEvent() {
            @Override
            public void onStart() {
                Message msg = new Message();
                msg.what = REQUEST_START;
                msg.arg1 = POST_FILE;
                msg.arg2 = tag;
                handler.sendMessage(msg);
            }

            @Override
            public void onDone(PostResponse postResponse) {
                Message msg = new Message();
                msg.what = REQUEST_DONE;
                msg.arg1 = POST_FILE;
                msg.arg2 = tag;
                String body = postResponse.getBody();
                msg.obj  = gson.fromJson(body, responseClass);
                handler.sendMessage(msg);
            }

            @Override
            public void onError(String err) {
                Message msg = new Message();
                msg.what = REQUEST_ERROR;
                msg.arg1 = POST_FILE;
                msg.arg2 = tag;
                msg.obj = err;
                handler.sendMessage(msg);
            }

            @Override
            public Map<String, String> getFiles() {
                return postFile.getFiles();
            }

            @Override
            public Map<String, String> getHeaders() {
                return postFile.getHeaders();
            }

            @Override
            public Map<String, String> getImages() {
                return postFile.getImages();
            }
        };
        PostSingle postSingle = new PostSingle(url, event);
        postSingle.start();
    }

    public void startGetFile(final String url, final String path, GetFileRequest getFileRequest){
        //加入文件请求队列
        final int tag = getFileRequests.size();
        getFileRequests.put(tag, getFileRequest);
        new Thread(new Runnable() {
            Message msg = new Message();
            @Override
            public void run() {
                msg.arg1 = GET_FILE;
                msg.arg2 = tag;
                msg.what = REQUEST_START;
                handler.sendMessage(msg);

                String error = HttpHelper.httpDownload(url, path);
                Log.e(TAG, "startGetFile result:"+error);
                if(error==null) {
                    Message msg = new Message();
                    msg.arg1 = GET_FILE;
                    msg.arg2 = tag;
                    msg.obj = error;
                    msg.what = REQUEST_DONE;
                    handler.sendMessage(msg);
                }else {
                    Message msg = new Message();
                    msg.arg1 = GET_FILE;
                    msg.arg2 = tag;
                    msg.obj = error;
                    msg.what = REQUEST_ERROR;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
}
