package cn.lessask.word;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import cn.lessask.word.dialog.LoadingDialog;
import cn.lessask.word.model.ArrayListResponse;
import cn.lessask.word.model.Response;
import cn.lessask.word.model.User;
import cn.lessask.word.model.Word;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.NetworkFileHelper;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.util.GlobalInfo;

public class WordActivity extends AppCompatActivity {
    private String TAG = WordActivity.class.getSimpleName();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();

    View wordLearn,wordReviveLayout,wordRecognize,wordInfoLayout,wordGroupLayout ;
    SharedPreferences sp;
    SharedPreferences.Editor editor;


    //新单词
    private TextView learnWord,learnUkphone;
    private TextView learnMean1,learnMean2,learnMean3,learnMean4;
    private Button learnNext;

    //单词详情
    RatingBar infoStatusRating;
    private TextView infoWord,infoUkphone;
    private TextView infoMean1,infoMean2,infoMean3,infoMean4;
    private ImageView infoVoice;

    //一组单词回顾
    private LinearLayout groupItem1,groupItem2,groupItem3,groupItem4,groupItem5,groupItem6,groupItem7;
    private TextView groupWord1,groupWord2,groupWord3,groupWord4,groupWord5,groupWord6,groupWord7;
    private TextView groupMean1,groupMean2,groupMean3,groupMean4,groupMean5,groupMean6,groupMean7;

    //辨认单词
    RatingBar recognizeStatusRating;
    private TextView recognizeWord,recognizeWordZh,recognizeUkphone;
    private SiriView recognizeSiriView;
    private TextView answera,answerb,answerc,answerd;
    private View answeraItem,answerbItem,answercItem,answerdItem;
    private int recognizeAnswer;
    private View recognizeType1,recognizeType2,recognizeType3;

    RatingBar reviveStatusRating;
    private TextView reviveWord,reviveUkPhone;
    private TextView reviveMean1,reviveMean2,reviveMean3,reviveMean4;
    private Button reviveKnow,reviveUnknow;
    private CircleProgressView reviveCircle;
    private LinearLayout reviveMeanings;

    private int thinkTimes = 4000;

    private ArrayList<Word> learnWords;
    private StringBuilder notSyncWords=new StringBuilder();
    private int learnIndex=0;

    private User user;
    private Word currentWord;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.e(TAG, "handler showWord");
                    showWord();
                    break;
                //显示单词完整解释
                case 2:
                    setWordInfoLayout(currentWord);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_word);
        LayoutInflater inflater = LayoutInflater.from(this);
        sp = this.getSharedPreferences("SP", MODE_PRIVATE);
        editor = sp.edit();

        //以上两行功能一样
        wordLearn = inflater.inflate(R.layout.word_learn,null);
        initWordLearn();
        //回想, 1颗星
        wordReviveLayout = inflater.inflate(R.layout.word_revive, null);
        initWordRevive();
        //错误的时候在展示单词详情
        wordInfoLayout = inflater.inflate(R.layout.word_info, null);
        initWordInfo();
        //再认(英选中，中选英，音选英)
        wordRecognize = inflater.inflate(R.layout.word_recognize, null);
        initWordRecognize();

        wordGroupLayout = inflater.inflate(R.layout.word_group, null);
        initWordGroup();

        user = globalInfo.getUser();
        gotoLean();
    }

    private void gotoLean(){
        int bookid = user.getBookid();
        learnWords=getGroupOfWords(user.getUserid(),bookid);

        Log.e(TAG, "notSyncWords :" + notSyncWords.toString());
        if(notSyncWords.length()>0){
            downloadWords(user.getUserid(),user.getToken(),bookid,notSyncWords.toString());
            notSyncWords.delete(0,notSyncWords.length());
        }else {
            showWord();
            Log.e(TAG, "gotoLearn");
        }
    }

    private void delay(final int time, final int what){
        Log.e(TAG,"delay begin, what:"+what);
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                Message message = new Message();
                message.what = what;
                handler.sendMessage(message);
                Log.e(TAG,"delay end");
            }
        };
        timer.schedule(task,time);
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void onBack(){
        syncWords(user.getUserid(),user.getBookid());
        WordActivity.this.setResult(RESULT_OK,getIntent());
        finish();
    }

    private void showWord(){
        if(learnIndex<learnWords.size()){
            currentWord = learnWords.get(learnIndex);
            learnIndex++;
            int status = currentWord.getStatus();
            switch (status){
                //新单词
                case 0:
                    setWordLearnLayout(currentWord);
                    break;
                //英->中辨认
                case 1:
                    currentWord.setRecognizeType(1);
                    setWordRecognizeLayout(currentWord);
                    break;
                //回想
                case 2:
                    setWordReviveLayout(currentWord);
                    break;
                //中->英辨认
                case 3:
                    currentWord.setRecognizeType(2);
                    setWordRecognizeLayout(currentWord);
                    break;
                //音->中辨认
                case 4:
                    currentWord.setRecognizeType(3);
                    setWordRecognizeLayout(currentWord);
                    break;
            }
            //return true;
        }else{
            //回顾当前批次的单词
            setWordGroupLayout(learnWords);
            learnIndex=0;
            //return false;
            syncWords(user.getUserid(),user.getBookid());
        }
    }

    private void downloadWords(final int userid,final String token,final int bookid,final String wordsStr){
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
                        globalInfo.getDb(WordActivity.this).update("t_words",values,where,whereArgs);

                        for(int j=0;j<learnWords.size();j++){
                            Word w = learnWords.get(j);
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
                datas.put("bookid",""+bookid);
                datas.put("words", wordsStr);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);

    }



    private ArrayList<Word> getGroupOfWords(int userid,int bookid){
        Log.e(TAG,"getGroupOfWords userid:"+userid+", bookid:"+bookid);
        ArrayList<Word> words = new ArrayList<>();
        //String [] timeDelta = new String[]{"-5 minute","-30 minute","-1 hour","-12 hour","-1 day"};
        String [] timeDelta = new String[]{"-5 minute","-30 minute","-480 minute","-720 minute"};
        //i对应的就是status的值
        for(int i=0,size=timeDelta.length;i<size;i++) {
            String reviewSql = "select id,word,usphone,ukphone,mean,sentence,review from t_words where userid=? and bookid=? and status=? and review<strftime('%s','now', '"+timeDelta[i]+"') order by review limit ?";
            int limit=7-words.size();
            int status=i+1;
            Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery(reviewSql, new String[]{"" + userid, "" + bookid,""+status,""+limit});
            Log.e(TAG, "review status:"+status+", size:"+cursor.getCount());
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String wordStr = cursor.getString(1);
                String usphone = cursor.getString(2);
                String ukphone = cursor.getString(3);
                String mean = cursor.getString(4);
                String sentence = cursor.getString(5);
                Date review = new Date(cursor.getLong(6) * 1000);

                Log.e(TAG, wordStr + ", status:" + status + ", review:" + review + ", mean:" + mean);
                if (usphone.length() == 0 && ukphone.length() == 0 && mean.length() == 0) {
                    if (notSyncWords.length() > 0)
                        notSyncWords.append("," + wordStr);
                    else
                        notSyncWords.append(wordStr);
                }
                Word word = new Word(id, bookid, wordStr, usphone, ukphone, mean, sentence, status);
                words.add(word);
            }
            if(words.size()>=7)
                break;
        }
        Log.e(TAG,"review size:"+words.size());
        int remain = 7-words.size();
        if(remain>0){
            int status=0;
            String newSql = "select id,word,usphone,ukphone,mean,sentence from t_words where userid=? and bookid=? and review is null order by id limit ?";
            Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery(newSql,new String[]{""+userid,""+bookid,""+remain});
            while (cursor.moveToNext()){
                int id =cursor.getInt(0);
                String wordStr =cursor.getString(1);
                String usphone =cursor.getString(2);
                String ukphone =cursor.getString(3);
                String mean =cursor.getString(4);
                String sentence =cursor.getString(5);

                Log.e(TAG,"usphone:"+usphone+", ukphone:"+ukphone+",mean:"+mean);
                if(usphone.length()==0 && ukphone.length()==0 && mean.length()==0) {
                    if(notSyncWords.length()>0)
                        notSyncWords.append(","+wordStr);
                    else
                        notSyncWords.append(wordStr);
                }

                Word word=new Word(id,bookid,wordStr,usphone,ukphone,mean,sentence,status);
                words.add(word);
            }
        }
        Collections.sort(words, randomComparator);
        return words;
    }

    private Comparator randomComparator = new Comparator() {
        @Override
        public int compare(Object lhs, Object rhs) {
            double r=Math.random();
            if(r>0.5)
                return 1;
            else if(r<0.5)
                return -1;
            else
                return 0;
        }
    };

    private void initWordRecognize(){

        recognizeStatusRating=(RatingBar) wordRecognize.findViewById(R.id.status);
        recognizeWord=(TextView)wordRecognize.findViewById(R.id.word);
        recognizeWordZh=(TextView)wordRecognize.findViewById(R.id.word_zh);
        recognizeSiriView=(SiriView)wordRecognize.findViewById(R.id.siriView);
        // 设置曲线高度，height的取值是0f~1f
        recognizeSiriView.setWaveHeight(0f);
        // 设置曲线的粗细，width的取值大于0f
        recognizeSiriView.setWaveWidth(5f);
        // 设置曲线颜色
        recognizeSiriView.setWaveColor(Color.rgb(39, 188, 136));
        // 设置曲线在X轴上的偏移量，默认值为0f
        recognizeSiriView.setWaveOffsetX(0f);
        // 设置曲线的数量，默认是4
        recognizeSiriView.setWaveAmount(4);
        // 设置曲线的速度，默认是0.1f
        recognizeSiriView.setWaveSpeed(0.1f);

        recognizeSiriView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recognizeSiriView.isActive())
                    recognizeSiriView.stop();
                else {
                    recognizeSiriView.start();
                    playPhoneFile(currentWord.getWord(),"uk");
                }
            }
        });
        recognizeUkphone=(TextView)wordRecognize.findViewById(R.id.ukphone);
        answera = (TextView)wordRecognize.findViewById(R.id.answera);
        answerb = (TextView)wordRecognize.findViewById(R.id.answerb);
        answerc = (TextView)wordRecognize.findViewById(R.id.answerc);
        answerd = (TextView)wordRecognize.findViewById(R.id.answerd);

        recognizeType1=wordRecognize.findViewById(R.id.recognize_type1);
        recognizeType2=wordRecognize.findViewById(R.id.recognize_type2);
        recognizeType3=wordRecognize.findViewById(R.id.recognize_type3);

        answeraItem = wordRecognize.findViewById(R.id.answera_item);
        answerbItem  = wordRecognize.findViewById(R.id.answerb_item);
        answercItem  = wordRecognize.findViewById(R.id.answerc_item);
        answerdItem  = wordRecognize.findViewById(R.id.answerd_item);

        answeraItem.setOnClickListener(selectAnswer);
        answerbItem.setOnClickListener(selectAnswer);
        answercItem.setOnClickListener(selectAnswer);
        answerdItem.setOnClickListener(selectAnswer);
        //点击不知道
        wordRecognize.findViewById(R.id.unknow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setWordInfoLayout();
                setWordStatus(user.getUserid(),currentWord,-1);
                setWordInfoLayout(currentWord);
            }
        });
    }

    private View.OnClickListener selectAnswer = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            TextView answerItem=null;
            boolean isRight=false;
            if(v==answeraItem){
                answerItem=answera;
                if(recognizeAnswer==0)
                    isRight=true;
            }else if(v==answerbItem){
                answerItem=answerb;
                if(recognizeAnswer==1)
                    isRight=true;
            }else if(v==answercItem){
                answerItem=answerc;
                if(recognizeAnswer==2)
                    isRight=true;
            }else if(v==answerdItem){
                answerItem=answerd;
                if(recognizeAnswer==3)
                    isRight=true;
            }
            if(isRight){
                answerItem.setTextColor(getResources().getColor(R.color.hublue));
                setWordStatus(user.getUserid(),currentWord,1);

                //音选中文
                if(currentWord.getRecognizeType()==3) {
                    answerItem.setText(currentWord.getWord());
                    answerItem.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.word_recognize_mean_en_size));
                    delay(1000,1);
                }else {
                    Log.e(TAG, "showWord");
                    delay(500,1);
                }
            }else{
                //错了
                answerItem.setTextColor(getResources().getColor(R.color.red));
                setWordStatus(user.getUserid(),currentWord,-1);
                delay(500,2);
            }
        }
    };

    private void setWordRecognizeLayout(Word word){
        int type=word.getRecognizeType();
        recognizeStatusRating.setRating(word.getStatus());
        if(type==1) {
            recognizeType1.setVisibility(View.VISIBLE);
            recognizeType2.setVisibility(View.INVISIBLE);
            recognizeType3.setVisibility(View.INVISIBLE);
            recognizeWord.setText(word.getWord());
            recognizeUkphone.setText("/" + word.getUkphone() + "/");
            TextView[] answerItems = new TextView[]{answera, answerb, answerc, answerd};
            //获取用于混淆的三个单词的意思
            recognizeAnswer = (int) (Math.random() * 4);
            Log.e(TAG, "recognizeAnswer:" + recognizeAnswer);
            ArrayList<String> errorAnswers = getErrorAnswers(word.getId());

            TextView answerItem = null;
            for (int i = 0, errorI = 0; i < 4; i++) {
                answerItem = answerItems[i];
                answerItem.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.word_recognize_mean_size));
                answerItem.setTextColor(getResources().getColor(R.color.black));
                if (i == recognizeAnswer) {
                    answerItem.setText(getFirstMean(word.getMean()));
                } else {
                    answerItem.setText(errorAnswers.get(errorI++));
                }
            }
            playPhoneFile(word.getWord(),"uk");
        }else if(type==2){
            //中->英
            recognizeType1.setVisibility(View.INVISIBLE);
            recognizeType2.setVisibility(View.VISIBLE);
            recognizeType3.setVisibility(View.INVISIBLE);

            recognizeWordZh.setText(getFirstMean(word.getMean()));
            TextView[] answerItems = new TextView[]{answera, answerb, answerc, answerd};
            //获取用于混淆的三个单词的意思
            recognizeAnswer = (int) (Math.random() * 4);
            Log.e(TAG, "recognizeAnswer:" + recognizeAnswer);
            ArrayList<String> errorAnswers = getErrorAnswersWords(word.getId());

            TextView answerItem = null;
            for (int i = 0, errorI = 0; i < 4; i++) {
                answerItem = answerItems[i];
                answerItem.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.word_recognize_mean_en_size));
                answerItem.setTextColor(getResources().getColor(R.color.black));
                if (i == recognizeAnswer) {
                    answerItem.setText(word.getWord());
                } else {
                    answerItem.setText(errorAnswers.get(errorI++));
                }
            }
        }else{
            recognizeType1.setVisibility(View.INVISIBLE);
            recognizeType2.setVisibility(View.INVISIBLE);
            recognizeType3.setVisibility(View.VISIBLE);

            TextView[] answerItems = new TextView[]{answera, answerb, answerc, answerd};
            //获取用于混淆的三个单词的意思
            recognizeAnswer = (int) (Math.random() * 4);
            Log.e(TAG, "recognizeAnswer:" + recognizeAnswer);
            ArrayList<String> errorAnswers = getErrorAnswers(word.getId());

            TextView answerItem = null;
            for (int i = 0, errorI = 0; i < 4; i++) {
                answerItem = answerItems[i];
                answerItem.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.word_recognize_mean_size));
                answerItem.setTextColor(getResources().getColor(R.color.black));
                if (i == recognizeAnswer) {
                    answerItem.setText(getFirstMean(word.getMean()));
                } else {
                    answerItem.setText(errorAnswers.get(errorI++));
                }
            }
            recognizeSiriView.start();
            playPhoneFile(word.getWord(),"uk");
        }
        setContentView(wordRecognize);
    }

    private ArrayList<String> getErrorAnswers(int id){
        ArrayList<String> answers = new ArrayList<>();

        int minId = id-15;
        Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery("select mean from t_words where id<? and id>? and mean!='' order by random()",new String[]{""+id,""+minId});
        int count = cursor.getCount();
        if(count>=3){
            for(int i=0;i<3;i++){
                cursor.moveToNext();
                answers.add(getFirstMean(cursor.getString(0)));
            }
        }else{
            while(cursor.moveToNext())
                answers.add(getFirstMean(cursor.getString(0)));
            if(count==0){
                answers.add("adj. 外部的");
                answers.add("n. 建筑物，大楼");
                answers.add("vi. 聚焦");
            }else if(count==1){
                answers.add("n. 悬崖,峭壁");
                answers.add("n. 邮费，邮资");
            }else if(count==2){
                answers.add("vt. 陈列，展览");
            }
        }
        return answers;
    }
    private ArrayList<String> getErrorAnswersWords(int id){
        ArrayList<String> answers = new ArrayList<>();

        int minId = id-15;
        Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery("select word from t_words where id<? and id>? order by random()",new String[]{""+id,""+minId});
        int count = cursor.getCount();
        if(count>=3){
            for(int i=0;i<3;i++){
                cursor.moveToNext();
                answers.add(cursor.getString(0));
            }
        }else{
            while(cursor.moveToNext())
                answers.add(cursor.getString(0));
            if(count==0){
                answers.add("broadcast");
                answers.add("gather");
                answers.add("cultivate");
            }else if(count==1){
                answers.add("zero");
                answers.add("distinct");
            }else if(count==2){
                answers.add("connect");
            }
        }
        return answers;
    }

    private String getFirstMean(String mean){
        int lastIndex = mean.indexOf("#");
        if(lastIndex==-1)
            lastIndex=mean.length();
        return mean.substring(0,lastIndex);
    }

    private void initWordInfo(){
        infoStatusRating=(RatingBar)wordInfoLayout.findViewById(R.id.status);
        infoWord=(TextView)wordInfoLayout.findViewById(R.id.word);
        infoUkphone=(TextView)wordInfoLayout.findViewById(R.id.ukphone);
        wordInfoLayout.findViewById(R.id.voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPhoneFile(currentWord.getWord(),"uk");
            }
        });
        infoMean1=(TextView)wordInfoLayout.findViewById(R.id.mean1);
        infoMean2=(TextView)wordInfoLayout.findViewById(R.id.mean2);
        infoMean3=(TextView)wordInfoLayout.findViewById(R.id.mean3);
        infoMean4=(TextView)wordInfoLayout.findViewById(R.id.mean4);

        wordInfoLayout.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWord();
            }
        });
    }

    private void setWordInfoLayout(Word word){
        Log.e(TAG, "setWordInfoLayout:"+word.getStatus());
        infoStatusRating.setRating(word.getStatus());
        infoWord.setText(word.getWord());
        infoUkphone.setText("/"+word.getUkphone()+"/");

        String[] means = word.getMean().split("#");
        int size = means.length;
        switch (size){
            case 1:
                infoMean1.setText(means[0]);
                infoMean2.setText("");
                infoMean3.setText("");
                infoMean4.setText("");
                break;
            case 2:
                infoMean1.setText(means[0]);
                infoMean2.setText(means[1]);
                infoMean3.setText("");
                infoMean4.setText("");
                break;
            case 3:
                infoMean1.setText(means[0]);
                infoMean2.setText(means[1]);
                infoMean3.setText(means[2]);
                infoMean4.setText("");
                break;
            case 4:
                infoMean1.setText(means[0]);
                infoMean2.setText(means[1]);
                infoMean3.setText(means[2]);
                infoMean4.setText(means[3]);
                break;
        }
        playPhoneFile(word.getWord(),"uk");
        setContentView(wordInfoLayout);
    }

    private void initWordLearn(){
        learnWord=(TextView) wordLearn.findViewById(R.id.word);
        learnNext = (Button)wordLearn.findViewById(R.id.next);
        learnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(learnIndex<=learnWords.size())
                    setWordStatus(user.getUserid(), currentWord, 1);
                showWord();
            }
        });
        learnUkphone = (TextView)wordLearn.findViewById(R.id.ukphone);
        learnMean1=(TextView)wordLearn.findViewById(R.id.mean1);
        learnMean2=(TextView)wordLearn.findViewById(R.id.mean2);
        learnMean3=(TextView)wordLearn.findViewById(R.id.mean3);
        learnMean4=(TextView)wordLearn.findViewById(R.id.mean4);
    }

    private void setWordLearnLayout(Word word){
        infoStatusRating.setRating(word.getStatus());
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
        playPhoneFile(word.getWord(),"uk");
        setContentView(wordLearn);
    }


    private void initWordRevive(){
        reviveStatusRating=(RatingBar)wordReviveLayout.findViewById(R.id.status);
        reviveWord=(TextView)wordReviveLayout.findViewById(R.id.word);
        reviveKnow = (Button)wordReviveLayout.findViewById(R.id.know);
        reviveKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordStatus(user.getUserid(),currentWord,1);
                setWordInfoLayout(currentWord);
                reviveCircle.setValue(0);
            }
        });
        reviveUnknow= (Button)wordReviveLayout.findViewById(R.id.unknow);
        reviveUnknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordStatus(user.getUserid(),currentWord,-1);
                setWordInfoLayout(currentWord);
                reviveCircle.setValue(0);
            }
        });
        reviveMeanings=(LinearLayout)wordReviveLayout.findViewById(R.id.meanings);
        reviveUkPhone = (TextView)wordReviveLayout.findViewById(R.id.ukphone);
        reviveMean1=(TextView)wordReviveLayout.findViewById(R.id.mean1);
        reviveMean2=(TextView)wordReviveLayout.findViewById(R.id.mean2);
        reviveMean3=(TextView)wordReviveLayout.findViewById(R.id.mean3);
        reviveMean4=(TextView)wordReviveLayout.findViewById(R.id.mean4);

        reviveCircle = (CircleProgressView) wordReviveLayout.findViewById(R.id.timer);
        reviveCircle.setValue(0);
        //reviveCircle.setValueAnimated(100, thinkTimes);
        reviveCircle.setSeekModeEnabled(false);
        reviveCircle.setTextMode(TextMode.TEXT);
        reviveCircle.setText("");
        reviveCircle.setSpinSpeed(1);
    }



    private void setWordReviveLayout(Word word){
        reviveStatusRating.setRating(word.getStatus());
        reviveMeanings.setVisibility(View.INVISIBLE);
        reviveCircle.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        reviveCircle.setVisibility(View.INVISIBLE);
                        reviveMeanings.setVisibility(View.VISIBLE);
                    }
                }, thinkTimes + 200);
        reviveCircle.setValueAnimated(0, 100, thinkTimes);
        reviveWord.setText(word.getWord());
        reviveUkPhone.setText("/"+word.getUkphone()+"/");
        playPhoneFile(word.getWord(), "uk");
        String[] means = word.getMean().split("#");
        int size = means.length;
        switch (size){
            case 1:
                reviveMean1.setText(means[0]);
                reviveMean2.setText("");
                reviveMean3.setText("");
                reviveMean4.setText("");
                break;
            case 2:
                reviveMean1.setText(means[0]);
                reviveMean2.setText(means[1]);
                reviveMean3.setText("");
                reviveMean4.setText("");
                break;
            case 3:
                reviveMean1.setText(means[0]);
                reviveMean2.setText(means[1]);
                reviveMean3.setText(means[2]);
                reviveMean4.setText("");
                break;
            case 4:
                reviveMean1.setText(means[0]);
                reviveMean2.setText(means[1]);
                reviveMean3.setText(means[2]);
                reviveMean4.setText(means[3]);
                break;
        }
        setContentView(wordReviveLayout);
    }



    private  void initWordGroup(){
        wordGroupLayout.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLean();
            }
        });
        groupItem1=(LinearLayout)wordGroupLayout.findViewById(R.id.item1);
        groupItem2=(LinearLayout)wordGroupLayout.findViewById(R.id.item2);
        groupItem3=(LinearLayout)wordGroupLayout.findViewById(R.id.item3);
        groupItem4=(LinearLayout)wordGroupLayout.findViewById(R.id.item4);
        groupItem5=(LinearLayout)wordGroupLayout.findViewById(R.id.item5);
        groupItem6=(LinearLayout)wordGroupLayout.findViewById(R.id.item6);
        groupItem7=(LinearLayout)wordGroupLayout.findViewById(R.id.item7);

        groupWord1=(TextView)wordGroupLayout.findViewById(R.id.word1);
        groupWord2=(TextView)wordGroupLayout.findViewById(R.id.word2);
        groupWord3=(TextView)wordGroupLayout.findViewById(R.id.word3);
        groupWord4=(TextView)wordGroupLayout.findViewById(R.id.word4);
        groupWord5=(TextView)wordGroupLayout.findViewById(R.id.word5);
        groupWord6=(TextView)wordGroupLayout.findViewById(R.id.word6);
        groupWord7=(TextView)wordGroupLayout.findViewById(R.id.word7);

        groupMean1=(TextView)wordGroupLayout.findViewById(R.id.mean1);
        groupMean2=(TextView)wordGroupLayout.findViewById(R.id.mean2);
        groupMean3=(TextView)wordGroupLayout.findViewById(R.id.mean3);
        groupMean4=(TextView)wordGroupLayout.findViewById(R.id.mean4);
        groupMean5=(TextView)wordGroupLayout.findViewById(R.id.mean5);
        groupMean6=(TextView)wordGroupLayout.findViewById(R.id.mean6);
        groupMean7=(TextView)wordGroupLayout.findViewById(R.id.mean7);

    }

    private void setWordGroupLayout(ArrayList<Word> learnWords){
        int size=learnWords.size();
        LinearLayout[] items = new LinearLayout[]{groupItem1,groupItem2,groupItem3,groupItem4,groupItem5,groupItem6,groupItem7};
        TextView[] words = new TextView[]{groupWord1,groupWord2,groupWord3,groupWord4,groupWord5,groupWord6,groupWord7};
        TextView[] means = new TextView[]{groupMean1,groupMean2,groupMean3,groupMean4,groupMean5,groupMean6,groupMean7};
        for(int i=0,wordsSize=learnWords.size(),length=items.length;i<length;i++){
            if(i<wordsSize){
                items[i].setVisibility(View.VISIBLE);
                Word w = learnWords.get(i);
                words[i].setText(w.getWord());
                //means[i].setText(w.getMean().replace("#", "\n"));
                means[i].setText(getFirstMean(w.getMean()));
            }else {
                items[i].setVisibility(View.INVISIBLE);
            }
        }

        setContentView(wordGroupLayout);
    }

    private void setWordStatus(int userid,Word word,int step){
        int id = word.getId();
        int status = word.getStatus();
        status+=step;
        word.setStatus(status);
        Date now = new Date();
        //review表示最近一次复习时间
        Date review = new Date(now.getTime());

        ContentValues values = new ContentValues();
        values.put("status",status);
        values.put("sync",0);
        values.put("review",review.getTime()/1000);
        String where = "userid=? and id=?";
        String[] whereArgs = new String[]{""+userid,""+word.getId()};
        globalInfo.getDb(WordActivity.this).update("t_words",values,where,whereArgs);
        Log.e(TAG, "setWordStatus update:"+word.getWord()+", status:"+word.getStatus());

        Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery("select status from t_words where id=?",new String[]{""+word.getId()});
        while (cursor.moveToNext()){
            Log.e(TAG, "new WordStatus:"+cursor.getInt(0));
        }
    }

    //type: 1英式发音，2美式发音
    private void playPhoneFile(String word,String type){
        String url = "http://120.24.75.92:5006/word/downloadphone?word="+word+"&type="+type;
        String filename=word+"_"+type+".mp3";
        File phoneFile = new File(Constant.phonePrefixPath,filename);
        if(phoneFile.exists()){
            Log.e(TAG, "playPhoneFile local:"+phoneFile.getPath() );
            playMp3(phoneFile.getPath());
        }else {
            final String path = phoneFile.getPath();
            NetworkFileHelper.getInstance().startGetFile(url, path, new NetworkFileHelper.GetFileRequest() {
                @Override
                public void onStart() {

                }

                @Override
                public void onResponse(String error) {
                    File file = new File(path);
                    if (file.exists()) {
                        Log.e(TAG, "exists:" + path);
                    } else {
                        Log.e(TAG, "not exists:" + path);
                    }
                    playMp3(path);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(WordActivity.this, "getphone error" + error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                }
            });
        }
    }
    private void playMp3(String path){

        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    if(currentWord.getStatus()==4)
                        recognizeSiriView.stop();
                }
            });
        }catch (IOException e){
            Log.e(TAG, "playMp3 error:"+e);
        }
    }

    private void syncWords(final int userid, final int bookid){
        String sql = "select id,status,review from t_words where userid=? and bookid=? and sync=0";
        Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery(sql, new String[]{"" + userid, "" + bookid});
        StringBuilder builder = new StringBuilder();
        StringBuilder idsBuilder = new StringBuilder();
        int count = cursor.getCount();
        if(count==0)
            return;
        int status=0,newnum=0,revivenum=0;
        for(int i=0;i<count;i++){
            cursor.moveToNext();
            int id = cursor.getInt(0);
            builder.append(id);
            idsBuilder.append(id);
            builder.append(",");
            status=cursor.getInt(1);
            builder.append(status);
            builder.append(",");
            builder.append(cursor.getInt(2));
            if(i+1<count){
                builder.append(";");
                idsBuilder.append(",");
            }
            if(status==3)
                newnum++;
            if(status>1)
                revivenum++;
        }
        Log.e(TAG, "syncWords newnum:"+newnum+", revivenum:"+revivenum);
        editor.putInt("newnum",sp.getInt("newnum",0)+newnum);
        editor.putInt("revivenum",sp.getInt("revivenum",0)+revivenum);
        editor.commit();

        final String syncDatas=builder.toString();
        final String syncIds=idsBuilder.toString();
        Log.e(TAG, "sync:"+syncDatas);
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/upwordstatus", Response.class, new GsonRequest.PostGsonRequest<Response>() {
            final LoadingDialog loadingDialog = new LoadingDialog(WordActivity.this);
            @Override
            public void onStart() {
                loadingDialog.show();
            }
            @Override
            public void onResponse(Response resp) {
                loadingDialog.cancel();
                if(resp.getError()!=null && resp.getError()!="" || resp.getErrno()!=0){
                    if(resp.getErrno()==601){
                        Intent intent = new Intent(WordActivity.this, LoginActivity.class);
                        startActivityForResult(intent, 1);
                    }else {
                        Toast.makeText(WordActivity.this, "changeList error:" + resp.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    String updateSql = "update t_words set sync=1 where userid=? and bookid=? and id in ("+syncIds+")";
                    Cursor cursor=globalInfo.getDb(WordActivity.this).rawQuery(updateSql, new String[]{""+userid,""+bookid});
                    Log.e(TAG, "sync ok:"+cursor.getCount()+", "+syncIds);
                }
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Log.e(TAG, "upwordstatus:"+error.toString());
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", ""+user.getUserid());
                datas.put("token", user.getToken());
                datas.put("bookid",""+user.getBookid());
                datas.put("datas",syncDatas);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }
}







