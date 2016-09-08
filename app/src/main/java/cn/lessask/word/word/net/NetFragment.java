package cn.lessask.word.word.net;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laiqin on 15/12/8.
 */
public abstract class NetFragment extends Fragment {
    private final String TAG = NetFragment.class.getSimpleName();
    private final int REQUEST_START=1;
    private final int REQUEST_DONE=2;
    private final int REQUEST_ERROR=3;
    private Gson gson = new Gson();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int requestCode = msg.arg1;
            switch (msg.what){
                case REQUEST_START:
                    onStart(requestCode);
                    break;
                case REQUEST_DONE:
                    onDone(requestCode, msg.obj);
                    break;
                case REQUEST_ERROR:
                    onError(requestCode, (String)msg.obj);
                    break;
            }

        }
    };

    public abstract void onStart(int requestCode);
    public abstract void onDone( int requestCode ,Object response);
    public abstract void onError(int requestCode, String error);

    public abstract void postData(int requestCode, Map headers, Map files);

    public void startPost(String url, final int requestCode, final Class responseClass){
        PostSingleEvent event = new PostSingleEvent() {
            @Override
            public void onStart() {
                Message msg = new Message();
                msg.what = REQUEST_START;
                msg.arg1 = requestCode;
                handler.sendMessage(msg);
            }

            @Override
            public void onDone(PostResponse postResponse) {
                Message msg = new Message();
                msg.what = REQUEST_DONE;
                msg.arg1 = requestCode;
                String body = postResponse.getBody();
                msg.obj  = gson.fromJson(body, responseClass);
                handler.sendMessage(msg);
            }

            @Override
            public void onError(String err) {
                Message msg = new Message();
                msg.what = REQUEST_ERROR;
                msg.arg1 = requestCode;
                msg.obj = err;
                handler.sendMessage(msg);
            }

            @Override
            public HashMap<String, String> getHeaders() {
                return null;
            }

            @Override
            public HashMap<String, String> getFiles() {
                return null;
            }

            @Override
            public HashMap<String, String> getImages() {
                return null;
            }
        };
        PostSingle postSingle = new PostSingle(url, event);

        HashMap<String, String> headers = new HashMap<>();
        HashMap<String, String> files = new HashMap<>();
        postData(requestCode, headers, files);

        postSingle.start();

    }
    public void startGetFile(final String url, final int requestCode, final String path){
        new Thread(new Runnable() {
            Message msg = new Message();
            @Override
            public void run() {
                msg.arg1 = requestCode;
                msg.what = REQUEST_START;
                handler.sendMessage(msg);
                Log.e(TAG, "download video start");

                String error = HttpHelper.httpDownload(url, path);
                if(error==null) {
                    Log.e(TAG, "download success");
                    msg.what = REQUEST_DONE;
                    handler.sendMessage(msg);
                }else {
                    Log.e(TAG, "download failed,"+error);
                    msg.what = REQUEST_ERROR;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
}
