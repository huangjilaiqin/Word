package cn.lessask.word.word;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.tencent.tauth.Tencent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import cn.lessask.word.word.dialog.LoadingDialog;
import cn.lessask.word.word.model.ArrayListResponse;
import cn.lessask.word.word.model.User;
import cn.lessask.word.word.model.Word;
import cn.lessask.word.word.model.WordList;
import cn.lessask.word.word.net.GsonRequest;
import cn.lessask.word.word.net.VolleyHelper;
import cn.lessask.word.word.util.DbHelper;
import cn.lessask.word.word.util.GlobalInfo;

public class WordActivity extends AppCompatActivity {
    private String TAG = WordActivity.class.getSimpleName();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();

    View wordLearn,wordRevive,wordRecognize,wordInfo;

    private CircleProgressView timer,learnTimer;
    private LinearLayout meanings;
    private TextView learnMean1,learnMean2,learnMean3,learnMean4;
    private TextView learnWord;
    private View nextLayout,selectLayout;
    private Button next,learnNext;
    private Button know,unknow,recognizeUnknow;
    private TextView learnUkphone;
    private TextView answera,answerb,answerc,answerd;
    private View answeraItem,answerbItem,answercItem,answerdItem;
    private int thinkTimes = 3000;

    private ArrayList<Word> learnWords;
    private StringBuilder notSyncWords=new StringBuilder();
    private int learnIndex=0;
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

        User user = globalInfo.getUser();
        learnWords=getGroupOfWords(user.getUserid(),1);

        if(notSyncWords.length()>0){
            downloadWords(user.getUserid(),user.getToken(),1,notSyncWords.toString());
            notSyncWords.delete(0,notSyncWords.length());
            Log.e(TAG, "notSyncWords size:"+notSyncWords.length());
        }else {
            showWord();
        }
    }

    private void downloadWords(final int userid,final String token,final int wtype,final String wordsStr){
        final LoadingDialog loadingDialog = new LoadingDialog(WordActivity.this);
        //loadingDialog
        Log.e(TAG, "downloadWrods:"+wordsStr);
        Type type = new TypeToken<ArrayListResponse<Word>>() {}.getType();
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/downloadwords", type, new GsonRequest.PostGsonRequest<ArrayListResponse>() {
            @Override
            public void onStart() {
                loadingDialog.show();
            }
            @Override
            public void onResponse(ArrayListResponse resp) {
                loadingDialog.cancel();
                if(resp.getError()!=null && resp.getError()!="" || resp.getErrno()!=0){
                    if(resp.getErrno()==601){
                        Intent intent = new Intent(WordActivity.this, LoginActivity.class);
                        startActivityForResult(intent, 1);
                    }else {
                        Toast.makeText(WordActivity.this, "changeList error:" + resp.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //本地存储
                    ArrayList<Word> words = resp.getDatas();
                    for(int i=0,size=words.size();i<size;i++){
                        Word word = words.get(i);
                        ContentValues values = new ContentValues();
                        values.put("usphone",word.getUsphone());
                        values.put("ukphone",word.getUkphone());
                        values.put("mean",word.getMean());
                        values.put("sentence",word.getSentence());
                        String where = "userid=? and id=?";
                        String[] whereArgs = new String[]{""+userid,""+word.getId()};
                        GlobalInfo.getInstance().getDb(WordActivity.this).update("t_words",values,where,whereArgs);

                        for(int j=0;j<learnWords.size();j++){
                            Word w = learnWords.get(i);
                            if(w.getId()==word.getId()){
                                w.setUsphone(word.getUsphone());
                                w.setUkphone(word.getUkphone());
                                w.setMean(word.getMean());
                                w.setSentence(word.getSentence());
                            }
                        }
                    }
                }
                showWord();
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Toast.makeText(WordActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", ""+userid);
                datas.put("token", token);
                datas.put("wtype",""+wtype);
                datas.put("words", wordsStr);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);

    }

    private void showWord(){
        if(learnIndex<learnWords.size()){
            Word word = learnWords.get(learnIndex);
            learnIndex++;
            int status = word.getStatus();
            switch (status){
                case 0:
                    setWordLearnLayout(word);
                    break;
            }
        }else{
            //回顾当前批次的单词
        }
    }

    private ArrayList<Word> getGroupOfWords(int userid,int wtype){
        ArrayList<Word> words = new ArrayList<>();
        String reviewSql = "select id,word,usphone,ukphone,mean,sentence,status from t_words where userid=? and wtype=? and review>date('now') order by review desc limit 7";
        Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery(reviewSql,new String[]{""+userid,""+wtype});
        while (cursor.moveToNext()){
            int id =cursor.getInt(0);
            String wordStr =cursor.getString(1);
            String usphone =cursor.getString(2);
            String ukphone =cursor.getString(3);
            String mean =cursor.getString(4);
            String sentence =cursor.getString(5);
            int status =cursor.getInt(6);

            if(usphone.length()==0 && ukphone.length()==0 && mean.length()==0) {
                if(notSyncWords.length()>0)
                    notSyncWords.append(","+wordStr);
                else
                    notSyncWords.append(wordStr);
            }
            Word word=new Word(id,wtype,wordStr,usphone,ukphone,mean,sentence,status);
            words.add(word);
        }
        Log.e(TAG,"review size:"+words.size());
        int remain = 7-words.size();
        if(remain>0){
            String newSql = "select id,word,usphone,ukphone,mean,sentence,status from t_words where userid=? and wtype=? and review is null order by id limit ?";
            cursor = globalInfo.getDb(WordActivity.this).rawQuery(newSql,new String[]{""+userid,""+wtype,""+remain});
            while (cursor.moveToNext()){
                int id =cursor.getInt(0);
                String wordStr =cursor.getString(1);
                String usphone =cursor.getString(2);
                String ukphone =cursor.getString(3);
                String mean =cursor.getString(4);
                String sentence =cursor.getString(5);
                int status =cursor.getInt(6);

                if(usphone==null && ukphone==null && mean==null) {
                    if(notSyncWords.length()>0)
                        notSyncWords.append(","+wordStr);
                    else
                        notSyncWords.append(wordStr);
                }

                Word word=new Word(id,wtype,wordStr,usphone,ukphone,mean,sentence,status);
                words.add(word);
            }
        }
        for(int i=0;i<words.size();i++){
            Log.e(TAG, words.get(i).getWord());
        }
        return words;
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
        learnWord=(TextView) wordLearn.findViewById(R.id.word);
        nextLayout=wordLearn.findViewById(R.id.next_layout);
        learnNext = (Button)wordLearn.findViewById(R.id.next);
        learnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWord();
            }
        });
        learnUkphone = (TextView)wordLearn.findViewById(R.id.ukphone);
        learnMean1=(TextView)wordLearn.findViewById(R.id.mean1);
        learnMean2=(TextView)wordLearn.findViewById(R.id.mean2);
        learnMean3=(TextView)wordLearn.findViewById(R.id.mean3);
        learnMean4=(TextView)wordLearn.findViewById(R.id.mean4);
        learnTimer = (CircleProgressView) wordLearn.findViewById(R.id.timer);
        learnTimer.setValueAnimated(100, thinkTimes);
        learnTimer.setSeekModeEnabled(false);
        learnTimer.setTextMode(TextMode.TEXT);
        learnTimer.setText("");
        learnTimer.setSpinSpeed(1);
    }

    private void setWordLearnLayout(Word word){
        //新单词必须停留一定时间才能跳过
        learnTimer.clearAnimation();
        learnTimer.setValueAnimated(0,100, thinkTimes);
        learnNext.setEnabled(false);
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    learnNext.setEnabled(true);
                }
            }, thinkTimes + 200);
        setContentView(wordLearn);
        //设置数据
        learnWord.setText(word.getWord());

        learnUkphone.setText("/"+word.getUkphone()+"/");

        String[] means = word.getMean().split("#");
        int size = means.length;
        switch (size){
            case 1:
                learnMean1.setText(means[0]);
                learnMean2.setText("");
                learnMean3.setText("");
                learnMean4.setText("");
                break;
            case 2:
                learnMean1.setText(means[0]);
                learnMean2.setText(means[1]);
                learnMean3.setText("");
                learnMean4.setText("");
                break;
            case 3:
                learnMean1.setText(means[0]);
                learnMean2.setText(means[1]);
                learnMean3.setText(means[2]);
                learnMean4.setText("");
                break;
            case 4:
                learnMean1.setText(means[0]);
                learnMean2.setText(means[1]);
                learnMean3.setText(means[2]);
                learnMean4.setText(means[3]);
                break;
        }
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







