package cn.lessask.word.word.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by huangji on 2015/12/9.
 */
public class VolleyHelper {
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;
    private String TAG = VolleyHelper.class.getSimpleName();
    private MyImageCache myImageCache;

    private VolleyHelper() {
        if(mCtx==null)
            throw new NullPointerException();
        mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        myImageCache = new MyImageCache();
        mImageLoader = new ImageLoader(mRequestQueue, myImageCache);
    }

    public static void setmCtx(Context mCtx) {
        VolleyHelper.mCtx = mCtx.getApplicationContext();
    }

    public static VolleyHelper getInstance() {
        return LazyHolder.INSTANCE;
    }
    private static class LazyHolder {
        private static final VolleyHelper INSTANCE = new VolleyHelper();
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
    public void removeCache(String url){
        mRequestQueue.getCache().remove(url);
        if(mRequestQueue.getCache().get(url)==null)
            Log.e(TAG, "remove cache:"+url);
        myImageCache.remove(new StringBuilder(url.length() + 12).append("#W").append(168)
                .append("#H").append(168).append(url).toString());
        Log.e(TAG, "remove isCache:"+mImageLoader.isCached(url, 168, 168));
    }

    class MyImageCache implements ImageLoader.ImageCache{
        private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);
        @Override
        public Bitmap getBitmap(String url) {
            return cache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            Log.e(TAG, "bitmap size:"+bitmap.getByteCount()/1024);
            cache.put(url, bitmap);
        }
        public Bitmap remove(String url){
            return cache.remove(url);
        }
    }
}
