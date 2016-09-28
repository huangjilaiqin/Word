package cn.lessask.word;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.awt.font.TextAttribute;
import java.math.BigDecimal;

import cn.lessask.word.model.User;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.util.GlobalInfo;

public class PersionalActivity extends AppCompatActivity {
    private String TAG = PersionalActivity.class.getSimpleName();
    private Button download;
    private ImageView headImg;
    private TextView offlineRate;
    private GlobalInfo globalInfo=GlobalInfo.getInstance();
    private User user = globalInfo.getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persional);

        headImg=(ImageView)findViewById(R.id.head_img);
        offlineRate=(TextView)findViewById(R.id.offline_rate);

        findViewById(R.id.change_book).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersionalActivity.this, SelectBookActivity.class);
                startActivity(intent);

            }
        });
        findViewById(R.id.purch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        download=(Button)findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        loadHeadImg(user.getHeadimg());
        calOfflineRate(user.getUserid(),user.getBookid());
    }
    private void loadHeadImg(String url){
        ImageLoader.ImageListener headImgListener = ImageLoader.getImageListener(headImg, 0, 0);
        VolleyHelper.getInstance().getImageLoader().get(url, headImgListener, 100, 100);
    }

    private void calOfflineRate(int userid,int bookid){
        String allSql = "select count(id) as num from t_words where userid=? and bookid=?";
        SQLiteDatabase db = globalInfo.getDb(PersionalActivity.this);
        Cursor cursor = db.rawQuery(allSql, new String[]{"" + userid, "" + bookid});
        cursor.moveToNext();
        int allSize=cursor.getInt(0);
        if(allSize==0)
            return;
        String sql = "select count(id) as num from t_words where userid=? and bookid=? and mean=''";
        cursor = db.rawQuery(sql, new String[]{""+userid, ""+ bookid});
        cursor.moveToNext();
        int notOfflineSize=cursor.getInt(0);
        float rate=(allSize-notOfflineSize)/(allSize*1.0f);
        Log.e(TAG, allSize + ", " + notOfflineSize);
        String rateStr = new BigDecimal(rate).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        offlineRate.setText(rateStr + "%");
    }
}
