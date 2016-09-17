package cn.lessask.word.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

/**
 * Created by JHuang on 2015/8/19.
 */
public class DownImageAsync extends AsyncTask<String, Void, Bitmap>{
    private static final String TAG = DownImageAsync.class.getName();
    private String url;
    private ImageView imageView;
    private Bitmap bitmap;

    public DownImageAsync(String url, ImageView imageView){
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                bitmap = BitmapFactory.decodeStream(is);
            }
            Log.e(TAG, "url:"+url+", statusCode:"+response.getStatusLine().getStatusCode());
            return bitmap;
        }catch (Exception e){
            Log.e(TAG, ""+e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Log.e(TAG, "onPostExecute");
        if(bitmap!=null) {
            imageView.setImageBitmap(bitmap);
            //将bitmap存储在user中
            Log.e(TAG, "async set headImg:"+url);
        }
    }
}
