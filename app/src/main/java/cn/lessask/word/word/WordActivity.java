package cn.lessask.word.word;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import at.grabner.circleprogress.AnimationState;
import at.grabner.circleprogress.AnimationStateChangedListener;
import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

public class WordActivity extends AppCompatActivity {

    View wordFirst;

    private CircleProgressView timer;
    private LinearLayout meanings;
    private View nextLayout,selectLayout;
    private Button know,unknow,next;
    private int thinkTimes = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_word);
        LayoutInflater inflater = LayoutInflater.from(this);
        //以上两行功能一样
        wordFirst = inflater.inflate(R.layout.word_first, null);

        nextLayout=wordFirst.findViewById(R.id.next_layout);
        selectLayout=wordFirst.findViewById(R.id.select_layout);
        know = (Button)wordFirst.findViewById(R.id.know);
        know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLayout.setVisibility(View.INVISIBLE);
                nextLayout.setVisibility(View.VISIBLE);
                timer.setVisibility(View.INVISIBLE);
                meanings.setVisibility(View.VISIBLE);
            }
        });
        unknow = (Button)wordFirst.findViewById(R.id.unknow);
        unknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLayout.setVisibility(View.INVISIBLE);
                nextLayout.setVisibility(View.VISIBLE);
                timer.setVisibility(View.INVISIBLE);
                meanings.setVisibility(View.VISIBLE);
            }
        });
        next = (Button)wordFirst.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        meanings = (LinearLayout)wordFirst.findViewById(R.id.meanings);
        timer = (CircleProgressView) wordFirst.findViewById(R.id.timer);
        timer.setValueAnimated(100, thinkTimes);
        timer.setSeekModeEnabled(false);
        timer.setTextMode(TextMode.TEXT);
        timer.setText("");
        timer.setSpinSpeed(1);

        setWordFirstLayout();


    }

    private void setWordFirstLayout(){
        setContentView(wordFirst);
        timer.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        timer.setVisibility(View.INVISIBLE);
                        meanings.setVisibility(View.VISIBLE);
                    }
                }, thinkTimes + 200);
    }
}
