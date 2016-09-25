package cn.lessask.word;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;

import cn.lessask.word.model.User;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.util.GlobalInfo;

public class PersionalActivity extends AppCompatActivity {

    private Button download;
    private ImageView headImg;
    private GlobalInfo globalInfo=GlobalInfo.getInstance();
    private User user = globalInfo.getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persional);

        headImg=(ImageView)findViewById(R.id.head_img);


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
    }
    private void loadHeadImg(String url){
        ImageLoader.ImageListener headImgListener = ImageLoader.getImageListener(headImg, 0, 0);
        VolleyHelper.getInstance().getImageLoader().get(url, headImgListener, 100, 100);
    }
}
