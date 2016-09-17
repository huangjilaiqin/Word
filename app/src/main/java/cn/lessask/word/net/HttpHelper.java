package cn.lessask.word.net;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by JHuang on 2015/12/2.
 */
public class HttpHelper {
    private static String TAG = HttpHelper.class.getSimpleName();
    public static String httpDownload(String httpUrl,String saveFile){
        // 下载网络文件
        int bytesum = 0;
        int byteread = 0;
        URL url = null;
        Log.e(TAG, "httpDownload url:"+httpUrl+", file:"+saveFile);
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return e1.toString();
        }

        try {
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(saveFile);

            byte[] buffer = new byte[1204];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "FileNotFoundException url:"+httpUrl);
            return e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
       }
   }
}
