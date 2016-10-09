package cn.lessask.word.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JHuang on 2015/12/9.
 */
public class NetworkFileHelper {
    private final String TAG = NetworkFileHelper.class.getSimpleName();
    private final int REQUEST_START=1;
    private final int REQUEST_DONE=2;
    private final int REQUEST_ERROR=3;
    private Gson gson = new Gson();
    private Map<String, PostFileRequest> postFileRequests = new ConcurrentHashMap<>();
    private Map<String, GetFileRequest> getFileRequests = new ConcurrentHashMap<>();
    //private Map<Long, PostFileRequest> postFileRequests = new HashMap<>();
    //private Map<Long, GetFileRequest> getFileRequests = new HashMap<>();
    private int POST_FILE = 0;
    private int GET_FILE = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==POST_FILE) {
                FileResponse fileResponse = (FileResponse)msg.obj;
                String tag = fileResponse.getTag();
                String error = fileResponse.getError();
                PostFileRequest postFileRequest = postFileRequests.get(tag);
                switch (msg.what) {
                    case REQUEST_START:
                        postFileRequest.onStart();
                        break;
                    case REQUEST_DONE:
                        postFileRequest.onResponse(error);
                        postFileRequests.remove(tag);
                        break;
                    case REQUEST_ERROR:
                        postFileRequest.onError(error);
                        postFileRequests.remove(tag);
                        break;
                }
            }else if(msg.arg1==GET_FILE){
                FileResponse fileResponse = (FileResponse)msg.obj;
                String tag = fileResponse.getTag();
                String error = fileResponse.getError();
                GetFileRequest getFileRequest = getFileRequests.get(tag);
                switch (msg.what) {
                    case REQUEST_START:
                        getFileRequest.onStart();
                        break;
                    case REQUEST_DONE:
                        getFileRequest.onResponse(error);
                        getFileRequests.remove(tag);
                        break;
                    case REQUEST_ERROR:
                        getFileRequest.onError(error);
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

    private int randomInt(){
        return (int)(Math.random()*1000);
    }

    public void startGetFile(final String url, final String path, GetFileRequest getFileRequest){
        //加入文件请求队列
        int rand = randomInt();
        final String tag = ""+System.currentTimeMillis()+rand;
        Log.e(TAG, "tag before:" + tag + ", " + rand);
        if(getFileRequests.get(tag)!=null)
            Log.e(TAG, "same tag:"+tag);
        getFileRequests.put(tag, getFileRequest);

        final FileResponse fileResponse=new FileResponse();
        fileResponse.setTag(tag);
        new Thread(new Runnable() {
            Message msg = new Message();
            @Override
            public void run() {
                msg.arg1 = GET_FILE;
                msg.obj= fileResponse;
                msg.what = REQUEST_START;
                handler.sendMessage(msg);

                String error = HttpHelper.httpDownload(url, path);
                fileResponse.setError(error);

                if(error==null) {
                    Message msg = new Message();
                    msg.arg1 = GET_FILE;
                    msg.obj= fileResponse;
                    msg.what = REQUEST_DONE;
                    handler.sendMessage(msg);
                    Log.e(TAG, "startGetFile ok:"+path);
                }else {
                    Message msg = new Message();
                    msg.arg1 = GET_FILE;
                    msg.obj= fileResponse;
                    msg.what = REQUEST_ERROR;
                    handler.sendMessage(msg);
                    Log.e(TAG, "startGetFile error:"+error+", "+path);
                }
            }
        }).start();
    }

    class FileResponse{
        private String tag;
        private String error;

        public FileResponse() {
        }

        public FileResponse(String tag, String error) {
            this.tag = tag;
            this.error = error;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
