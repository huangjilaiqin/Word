package cn.lessask.word.test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import cn.lessask.word.util.GlobalInfo;

/**
 * Created by huangji on 2016/9/12.
 */
public class ExampleTest extends AndroidTestCase{
    private String TAG = ExampleTest.class.getSimpleName();
    public void testFoo(){

        Log.e(TAG,"testFoo");
        SQLiteDatabase db = GlobalInfo.getInstance().getDb(getContext());
        Cursor cursor=db.rawQuery("select word,review from t_words", null);
        Log.e(TAG,"count:"+cursor.getCount());
        while(cursor.moveToNext()){
            Log.e(TAG,cursor.getString(0)+","+cursor.getString(1));
        }
    }
}
