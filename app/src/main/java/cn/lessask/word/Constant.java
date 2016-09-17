package cn.lessask.word;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Created by laiqin on 16/9/11.
 */
public class Constant {

    private static Context context;
    public static void setContext(Context ctx){
        context=ctx;
        File file = new File(context.getExternalFilesDir(null),"phones");
        if (!file.exists()){
            file.mkdir();
        }
        phonePrefixPath=file.getPath();
        Log.e("phonePrefixPath:",phonePrefixPath);
        for(File f : file.listFiles()){
            Log.e("f:",f.getName());
        }
    }
    public static String phonePrefixPath;
}
