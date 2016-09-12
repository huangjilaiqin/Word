package cn.lessask.word.word;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.tencent.tauth.Tencent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import cn.lessask.word.word.dialog.LoadingDialog;
import cn.lessask.word.word.model.ArrayListResponse;
import cn.lessask.word.word.model.User;
import cn.lessask.word.word.model.Word;
import cn.lessask.word.word.model.WordList;
import cn.lessask.word.word.net.GsonRequest;
import cn.lessask.word.word.net.NetworkFileHelper;
import cn.lessask.word.word.net.VolleyHelper;
import cn.lessask.word.word.util.DbHelper;
import cn.lessask.word.word.util.GlobalInfo;

public class WordActivity extends AppCompatActivity {
    private String TAG = WordActivity.class.getSimpleName();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();

    View wordLearn,wordReviveLayout,wordRecognize,wordInfoLayout,wordGroupLayout ;

    private CircleProgressView timer,learnTimer;
    private LinearLayout meanings;
    private View nextLayout,selectLayout;
    private Button next;
    private Button know,unknow;

    //新单词
    private TextView learnWord,learnUkphone;
    private TextView learnMean1,learnMean2,learnMean3,learnMean4;
    private Button learnNext;

    //单词详情
    private TextView infoWord,infoUkphone;
    private TextView infoMean1,infoMean2,infoMean3,infoMean4;
    private ImageView infoVoice;

    //一组单词回顾
    private LinearLayout groupItem1,groupItem2,groupItem3,groupItem4,groupItem5,groupItem6,groupItem7;
    private TextView groupWord1,groupWord2,groupWord3,groupWord4,groupWord5,groupWord6,groupWord7;
    private TextView groupMean1,groupMean2,groupMean3,groupMean4,groupMean5,groupMean6,groupMean7;

    //辨认单词
    private TextView recognizeWord,recognizeUkphone;
    private TextView answera,answerb,answerc,answerd;
    private View answeraItem,answerbItem,answercItem,answerdItem;
    private int recognizeAnswer;

    private int thinkTimes = 3000;

    private ArrayList<Word> learnWords;
    private StringBuilder notSyncWords=new StringBuilder();
    private int learnIndex=0;

    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_word);
        LayoutInflater inflater = LayoutInflater.from(this);
        //以上两行功能一样
        wordLearn = inflater.inflate(R.layout.word_learn,null);
        initWordLearn();
        wordReviveLayout = inflater.inflate(R.layout.word_revive, null);
        initWordRevive();
        wordInfoLayout = inflater.inflate(R.layout.word_info, null);
        initWordInfo();
        wordRecognize = inflater.inflate(R.layout.word_recognize, null);
        initWordRecognize();


        wordGroupLayout = inflater.inflate(R.layout.word_group, null);
        initWordGroup();

        user = globalInfo.getUser();
        gotoLean();
    }

    private void gotoLean(){
        learnWords=getGroupOfWords(user.getUserid(),1);

        Log.e(TAG, "notSyncWords :"+notSyncWords.toString());
        if(notSyncWords.length()>0){
            downloadWords(user.getUserid(),user.getToken(),1,notSyncWords.toString());
            notSyncWords.delete(0,notSyncWords.length());
        }else {
            showWord();
            Log.e(TAG, "gotoLearn");
        }
    }

    private boolean showWord(){
        if(learnIndex<learnWords.size()){
            Word word = learnWords.get(learnIndex);
            learnIndex++;
            int status = word.getStatus();
            switch (status){
                case 0:
                    setWordLearnLayout(word);
                    break;
                case 1:
                    setWordRecognizeLayout(word);
                    break;
            }
            return true;
        }else{
            //回顾当前批次的单词
            setWordGroupLayout(learnWords);
            learnIndex=0;
            return false;
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
                Log.e(TAG, "downloadWords");
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



    private ArrayList<Word> getGroupOfWords(int userid,int wtype){
        Log.e(TAG,"getGroupOfWords");
        ArrayList<Word> words = new ArrayList<>();
        //String reviewSql = "select id,word,usphone,ukphone,mean,sentence,status,review,date('now') from t_words where userid=? and wtype=? and review>date('now') order by review desc limit 7";
        String reviewSql = "select id,word,usphone,ukphone,mean,sentence,status,review,strftime('%s','now','localtime') from t_words where userid=? and wtype=? and status<2 order by review desc limit 7";
        Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery(reviewSql,new String[]{""+userid,""+wtype});
        while (cursor.moveToNext()){
            int id =cursor.getInt(0);
            String wordStr =cursor.getString(1);
            String usphone =cursor.getString(2);
            String ukphone =cursor.getString(3);
            String mean =cursor.getString(4);
            String sentence =cursor.getString(5);
            int status =cursor.getInt(6);
            Date review = new Date(cursor.getLong(7)*1000);
            //Log.e(TAG, wordStr+", review:"+review+", now:"+cursor.getLong(8));

            Log.e(TAG, wordStr+", us:"+usphone+", uk:"+ukphone+", mean:"+mean);
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
        /*
        for(int i=0;i<words.size();i++){
            Log.e(TAG, "word:"+words.get(i).getWord()+", "+words.get(i).getReview());
        }
        */
        return words;
    }

    private void initWordRecognize(){
        recognizeWord=(TextView)wordRecognize.findViewById(R.id.word);
        recognizeUkphone=(TextView)wordRecognize.findViewById(R.id.ukphone);
        answera = (TextView)wordRecognize.findViewById(R.id.answera);
        answerb = (TextView)wordRecognize.findViewById(R.id.answerb);
        answerc = (TextView)wordRecognize.findViewById(R.id.answerc);
        answerd = (TextView)wordRecognize.findViewById(R.id.answerd);

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
                setWordStatus(user.getUserid(),learnWords.get(learnIndex-1),-1);
                setWordInfoLayout(learnWords.get(learnIndex-1));
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
            Word word=learnWords.get(learnIndex-1);
            if(isRight){
                answerItem.setTextColor(getResources().getColor(R.color.hublue));
                setWordStatus(user.getUserid(),word,1);
                showWord();
            }else{
                //错了
                answerItem.setTextColor(getResources().getColor(R.color.red));
                setWordStatus(user.getUserid(),word,-1);
                setWordInfoLayout(word);
            }
        }
    };

    private void setWordRecognizeLayout(Word word){
        recognizeWord.setText(word.getWord());
        recognizeUkphone.setText("/"+word.getUkphone()+"/");
        TextView[] answerItems = new TextView[]{answera,answerb,answerc,answerd};
        //获取用于混淆的三个单词的意思
        recognizeAnswer=(int)(Math.random()*4);
        Log.e(TAG, "recognizeAnswer:"+recognizeAnswer);
        ArrayList<String> errorAnswers = getErrorAnswers(word.getId());

        TextView answerItem=null;
        for(int i=0,errorI=0;i<4;i++){
            answerItem=answerItems[i];
            answerItem.setTextColor(getResources().getColor(R.color.black));
            if(i==recognizeAnswer){
                answerItem.setText(getFirstMean(word.getMean()));
            }else {
                answerItem.setText(errorAnswers.get(errorI++));
            }
        }

        setContentView(wordRecognize);

    }

    private ArrayList<String> getErrorAnswers(int id){
        ArrayList<String> answers = new ArrayList<>();

        int minId = id-15;
        Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery("select mean from t_words where id<? and id>? order by random()",new String[]{""+id,""+minId});
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

    private String getFirstMean(String mean){
        int lastIndex = mean.indexOf("#");
        if(lastIndex==-1)
            lastIndex=mean.length();
        return mean.substring(0,lastIndex);
    }

    private void initWordInfo(){
        infoWord=(TextView)wordInfoLayout.findViewById(R.id.word);
        infoUkphone=(TextView)wordInfoLayout.findViewById(R.id.ukphone);
        wordInfoLayout.findViewById(R.id.voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Word word = learnWords.get(learnIndex-1);
                playPhoneFile(word.getWord(),"uk");
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
        setContentView(wordInfoLayout);
    }

    private void initWordLearn(){
        learnWord=(TextView) wordLearn.findViewById(R.id.word);
        nextLayout=wordLearn.findViewById(R.id.next_layout);
        learnNext = (Button)wordLearn.findViewById(R.id.next);
        learnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(learnIndex<=learnWords.size())
                    setWordStatus(user.getUserid(), learnWords.get(learnIndex-1), 1);
                showWord();
            }
        });
        learnUkphone = (TextView)wordLearn.findViewById(R.id.ukphone);
        learnMean1=(TextView)wordLearn.findViewById(R.id.mean1);
        learnMean2=(TextView)wordLearn.findViewById(R.id.mean2);
        learnMean3=(TextView)wordLearn.findViewById(R.id.mean3);
        learnMean4=(TextView)wordLearn.findViewById(R.id.mean4);
        //learnTimer = (CircleProgressView) wordLearn.findViewById(R.id.timer);
        //learnTimer.setValueAnimated(100, thinkTimes);
        //learnTimer.setSeekModeEnabled(false);
        //learnTimer.setTextMode(TextMode.TEXT);
        //learnTimer.setText("");
        //learnTimer.setSpinSpeed(1);
    }

    private void setWordLearnLayout(Word word){
        //新单词必须停留一定时间才能跳过
        //learnTimer.clearAnimation();
        //learnTimer.setValueAnimated(0,100, thinkTimes);
        //learnNext.setEnabled(false);
        //new android.os.Handler().postDelayed(
        //    new Runnable() {
        //        public void run() {
        //            learnNext.setEnabled(true);
        //        }
        //    }, thinkTimes + 200);

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
        setContentView(wordLearn);
    }


    private void initWordRevive(){
        selectLayout=wordReviveLayout.findViewById(R.id.select_layout);
        know = (Button)wordReviveLayout.findViewById(R.id.know);
        know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setWordInfoLayout();
            }
        });
        unknow = (Button)wordReviveLayout.findViewById(R.id.unknow);
        unknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setWordInfoLayout();
            }
        });

        meanings = (LinearLayout)wordReviveLayout.findViewById(R.id.meanings);
        timer = (CircleProgressView) wordReviveLayout.findViewById(R.id.timer);
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
        setContentView(wordReviveLayout);
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
                means[i].setText(w.getMean().replace("#", "\n"));
            }else {
                items[i].setVisibility(View.INVISIBLE);
            }
        }

        setContentView(wordGroupLayout);
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

    private void setWordStatus(int userid,Word word,int step){
        int id = word.getId();
        int status = word.getStatus();
        status+=step;
        Date now = new Date();
        Date review = new Date(now.getTime()+1*60*1000);

        ContentValues values = new ContentValues();
        values.put("status",status);
        values.put("review",review.getTime()/1000);
        String where = "userid=? and id=?";
        String[] whereArgs = new String[]{""+userid,""+word.getId()};
        globalInfo.getDb(WordActivity.this).update("t_words",values,where,whereArgs);
        Log.e(TAG, "setWordStatus update:"+word.getWord());

        /*
        Cursor cursor = globalInfo.getDb(WordActivity.this).rawQuery("select review,status,word from t_words where review is not null",null);
        while (cursor.moveToNext()){
            Log.e(TAG, "setWordStatus:"+cursor.getString(0)+","+cursor.getString(1)+","+cursor.getString(2));
        }
        */
    }

    //type: 1英式发音，2美式发音
    private void playPhoneFile(String word,String type){
        String url = "http://120.24.75.92:5006/word/downloadphone?word="+word+"&type="+type;
        String filename=word+"_"+type+".mp3";
        File phoneFile = new File(Constant.phonePrefixPath,filename);
        if(phoneFile.exists()){
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
        }catch (IOException e){
            Log.e(TAG, "playMp3 error:"+e);
        }
    }
}







