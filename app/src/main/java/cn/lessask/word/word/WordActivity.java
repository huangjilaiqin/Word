package cn.lessask.word.word;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import at.grabner.circleprogress.AnimationState;
import at.grabner.circleprogress.AnimationStateChangedListener;
import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

public class WordActivity extends AppCompatActivity {

    View wordFirst;

    private CircleProgressView timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_word);
        LayoutInflater inflater = LayoutInflater.from(this);
        //以上两行功能一样
        wordFirst = inflater.inflate(R.layout.word_first, null);
        setContentView(wordFirst);

        timer = (CircleProgressView) wordFirst.findViewById(R.id.timer);
        timer.setValueAnimated(100,2000);
        timer.setSeekModeEnabled(false);
        timer.setTextMode(TextMode.TEXT);
        timer.setText("");

        timer.setOnAnimationStateChangedListener(new AnimationStateChangedListener() {

            @Override
            public void onAnimationStateChanged(AnimationState _animationState) {
                System.out.println(_animationState);
                switch (_animationState){
                    case START_ANIMATING_AFTER_SPINNING:
                        System.out.println("START_ANIMATING_AFTER_SPINNING");
                        break;
                    case SPINNING:
                        System.out.println("SPINNING");
                        break;
                    case END_SPINNING:
                        System.out.println("END_SPINNING");
                        break;
                    case END_SPINNING_START_ANIMATING:
                        System.out.println("END_SPINNING_START_ANIMATING");
                        break;

                }
            }
        });

    }
}
