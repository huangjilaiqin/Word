package cn.lessask.word.word;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

public class WordActivity extends AppCompatActivity {

    View wordFirst;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_word);
        LayoutInflater inflater = LayoutInflater.from(this);
        //以上两行功能一样
        wordFirst = inflater.inflate(R.layout.word_first, null);
        setContentView(wordFirst);
    }
}
