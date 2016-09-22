package cn.lessask.word;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import cn.lessask.word.model.ArrayListResponse;
import cn.lessask.word.model.Book;
import cn.lessask.word.model.User;
import cn.lessask.word.model.WordList;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.recycleview.DividerItemDecoration;
import cn.lessask.word.recycleview.OnItemClickListener;
import cn.lessask.word.recycleview.RecyclerViewStatusSupport;
import cn.lessask.word.util.GlobalInfo;
import cn.lessask.word.util.OnClickListener;

public class SelectBookActivity extends AppCompatActivity {
    private String TAG = SelectBookActivity.class.getSimpleName();
    private RecyclerViewStatusSupport mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private BookAdapter mRecyclerViewAdapter;
    private VolleyHelper volleyHelper = VolleyHelper.getInstance();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user=globalInfo.getUser();
        setContentView(R.layout.activity_select_book);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("选择词库");
        mToolbar.setTitleTextColor(getResources().getColor(R.color.background_white));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mRecyclerView = (RecyclerViewStatusSupport)findViewById(R.id.list);
        mRecyclerView.setStatusViews(findViewById(R.id.loading_view), findViewById(R.id.empty_view), findViewById(R.id.error_view));

        mLinearLayoutManager = new LinearLayoutManager(SelectBookActivity.this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(SelectBookActivity.this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setClickable(true);
        /*
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recycleViewScrollListener != null) {
                    recycleViewScrollListener.onRecycleViewScroll(recyclerView, dx, dy);
                }
            }
        });
        */

        mRecyclerViewAdapter = new BookAdapter(getBaseContext());
        mRecyclerViewAdapter.setOnSelectListener(new OnClickListener() {
            @Override
            public void onItemClick(Object object) {
                Book book = (Book)object;
                //更新数据库
                SharedPreferences sp = SelectBookActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("bookid", book.getBookid());
                editor.commit();
                //切换词库
                    //检查词库是否存在
                    String[] where=new String[]{""+user.getUserid(),""+book.getBookid()};
                    Cursor cursor = globalInfo.getDb(SelectBookActivity.this).rawQuery("select count(id) as num from t_words where userid=? and wtype=?",where);
                    if(cursor.getCount()==0)
                        //changeList(user.getUserid(),user.getToken(),0,bookid);
                    //下载词库
            }
        });
        //设置点击事件, 编辑动作
        /*
        mRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                Toast.makeText(getBaseContext(), "onClick:"+position,Toast.LENGTH_SHORT).show();
            }
        });
        */
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        loadBooks();
    }

    private void loadBooks(){
        Type type = new TypeToken<ArrayListResponse<Book>>() {}.getType();

        String url = "http://120.24.75.92:5006/word/books";
        GsonRequest gsonRequest = new GsonRequest<ArrayListResponse<Book>>(Request.Method.POST,url,type,new GsonRequest.PostGsonRequest<ArrayListResponse<Book>>(){
            @Override
            public void onStart() {
                mRecyclerView.showLoadingView();
            }

            @Override
            public void onResponse(ArrayListResponse<Book> response) {
                if(response.getError()!=null || response.getErrno()!=0){
                    Toast.makeText(SelectBookActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                }else {
                    mRecyclerViewAdapter.appendToList(response.getDatas());
                    mRecyclerViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(VolleyError error) {
                mRecyclerView.showErrorView(error.toString());
            }

            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + user.getUserid());
                datas.put("token", "" + user.getToken());
            }

        });
        volleyHelper.addToRequestQueue(gsonRequest);
    }

    private void changeList(final int userid,final String token,final int id,final int wtype){
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://120.24.75.92:5006/word/changelist", WordList.class, new GsonRequest.PostGsonRequest<WordList>() {
            @Override
            public void onStart() {}
            @Override
            public void onResponse(WordList user) {
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    if(user.getErrno()==601){
                        Intent intent = new Intent(SelectBookActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN);
                    }else {
                        Toast.makeText(SelectBookActivity.this, "changeList error:" + user.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //本地存储
                    final String wordsStr = user.getWords();
                    final String[] words = wordsStr.split(";");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i=0,size=words.length;i<size;i++){
                                String[] info = words[i].split(":");
                                ContentValues values = new ContentValues();
                                values.put("id",info[0]);
                                values.put("word",info[1]);
                                values.put("userid",userid);
                                values.put("wtype",wtype);
                                globalInfo.getDb(SelectBookActivity.this).insert("t_words",null,values);
                            }
                            Log.e(TAG, "insert done");
                        }
                    }).start();
                }
                Cursor cursor = globalInfo.getDb(SelectBookActivity.this).rawQuery("select count(id) as num from t_words",null);
                while (cursor.moveToNext()){
                    int num=cursor.getInt(0);
                    Log.e(TAG, "size:"+num);
                }
                cursor.close();
                //设置wtype
                SharedPreferences sp = SelectBookActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                //存入数据
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("wtype", wtype);
                editor.commit();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(SelectBookActivity.this,  error.toString(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
                datas.put("id",""+id);
                datas.put("wtype",""+wtype);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }
}
