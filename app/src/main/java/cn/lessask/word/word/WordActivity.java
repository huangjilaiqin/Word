package cn.lessask.word.word;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

public class WordActivity extends AppCompatActivity {

    View wordLearn,wordRevive,wordRecognize,wordInfo;

    private CircleProgressView timer,learnTimer;
    private LinearLayout meanings,learnMeanings;
    private View nextLayout,selectLayout;
    private Button next,learnNext;
    private Button know,unknow,recognizeUnknow;
    private TextView answera,answerb,answerc,answerd;
    private View answeraItem,answerbItem,answercItem,answerdItem;
    private int thinkTimes = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_word);
        LayoutInflater inflater = LayoutInflater.from(this);
        //以上两行功能一样
        wordLearn = inflater.inflate(R.layout.word_learn,null);
        initWordLearn();
        wordRevive = inflater.inflate(R.layout.word_revive, null);
        initWordRevive();
        wordInfo = inflater.inflate(R.layout.word_info, null);
        initWordInfo();
        wordRecognize = inflater.inflate(R.layout.word_recognize, null);
        initWordRecognize();


        //setWordLearnLayout();
        //setWordReviveLayout();
        setWordRecognizeLayout();
    }

    private void initWordRecognize(){
        answera = (TextView)wordRecognize.findViewById(R.id.answera);
        answerb = (TextView)wordRecognize.findViewById(R.id.answerb);
        answerc = (TextView)wordRecognize.findViewById(R.id.answerc);
        answerd = (TextView)wordRecognize.findViewById(R.id.answerd);

        answeraItem = wordRecognize.findViewById(R.id.answera_item);
        answerbItem  = wordRecognize.findViewById(R.id.answerb_item);
        answercItem  = wordRecognize.findViewById(R.id.answerc_item);
        answerdItem  = wordRecognize.findViewById(R.id.answerd_item);

        wordRecognize.findViewById(R.id.answera_item).setOnClickListener(selectAnswer);
        wordRecognize.findViewById(R.id.unknow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordInfoLayout();
            }
        });
    }

    private View.OnClickListener selectAnswer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v==answeraItem){
                if(false){
                    answera.setTextColor(getResources().getColor(R.color.red));
                    setWordInfoLayout();
                }else{
                    //对了
                    answera.setTextColor(getResources().getColor(R.color.red));
                }
            }else if(v==answerbItem){

            }else if(v==answercItem){

            }else if(v==answerdItem){

            }
        }
    };

    private void initWordInfo(){
        nextLayout=wordInfo.findViewById(R.id.next_layout);
        next = (Button)wordInfo.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setWordInfoLayout();
            }
        });
    }

    private void initWordLearn(){
        nextLayout=wordLearn.findViewById(R.id.next_layout);
        learnNext = (Button)wordLearn.findViewById(R.id.next);
        learnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setWordInfoLayout();
            }
        });

        learnMeanings = (LinearLayout)wordLearn.findViewById(R.id.meanings);
        learnTimer = (CircleProgressView) wordLearn.findViewById(R.id.timer);
        learnTimer.setValueAnimated(100, thinkTimes);
        learnTimer.setSeekModeEnabled(false);
        learnTimer.setTextMode(TextMode.TEXT);
        learnTimer.setText("");
        learnTimer.setSpinSpeed(1);
    }


    private void initWordRevive(){
        selectLayout=wordRevive.findViewById(R.id.select_layout);
        know = (Button)wordRevive.findViewById(R.id.know);
        know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordInfoLayout();
            }
        });
        unknow = (Button)wordRevive.findViewById(R.id.unknow);
        unknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordInfoLayout();
            }
        });

        meanings = (LinearLayout)wordRevive.findViewById(R.id.meanings);
        timer = (CircleProgressView) wordRevive.findViewById(R.id.timer);
        timer.setValueAnimated(100, thinkTimes);
        timer.setSeekModeEnabled(false);
        timer.setTextMode(TextMode.TEXT);
        timer.setText("");
        timer.setSpinSpeed(1);
    }

    private void setWordLearnLayout(){
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    learnNext.setEnabled(true);
                }
            }, thinkTimes + 200);
        setContentView(wordLearn);
    }

    private void setWordReviveLayout(){
        timer.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        timer.setVisibility(View.INVISIBLE);
                        meanings.setVisibility(View.VISIBLE);
                    }
                }, thinkTimes + 200);
        setContentView(wordRevive);
    }

    private void setWordInfoLayout(){
        //这是单词信息
        setContentView(wordInfo);
    }

    private void setWordRecognizeLayout(){

        setContentView(wordRecognize);
    }
}







