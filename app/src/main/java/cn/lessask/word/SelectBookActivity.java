package cn.lessask.word;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
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
import java.util.List;
import java.util.Map;

import cn.lessask.word.dialog.LoadingDialog;
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
    private final int LOGIN=1;
    private RecyclerViewStatusSupport mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private BookAdapter mRecyclerViewAdapter;
    private VolleyHelper volleyHelper = VolleyHelper.getInstance();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();
    private User user;
    private boolean haveChange;

    private ServiceInterFace serviceInterFace;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user=globalInfo.getUser();
        setContentView(R.layout.activity_select_book);

        serviceIntent = new Intent(this, ServiceCrack.class);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("选择词库");
        mToolbar.setTitleTextColor(getResources().getColor(R.color.background_white));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
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
                if(book.getBookid()==user.getBookid())
                    return;

                //切换词库
                String[] where=new String[]{""+user.getUserid(),""+book.getBookid()};
                Log.e(TAG, ""+user.getUserid()+", "+book.getBookid());
                Cursor cursor = globalInfo.getDb(SelectBookActivity.this).rawQuery("select id from t_words where userid=? and bookid=? order by id desc limit 1",where);
                int id=0;
                if(cursor.getCount()>0) {
                    cursor.moveToNext();
                    id=cursor.getInt(0);
                }

                changeBook(user.getUserid(),user.getToken(),book.getBookid());

                SharedPreferences sp = SelectBookActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                SharedPreferences.Editor editor=sp.edit();
                editor.putInt("bookid",book.getBookid());
                editor.putString("bookName",book.getName());
                editor.commit();
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
        bindService();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceInterFace=(ServiceInterFace)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private void bindService(){
        bindService(serviceIntent, serviceConnection,BIND_AUTO_CREATE);
    }

    private void onBack(){
        Intent intent = new Intent(SelectBookActivity.this, PersionalActivity.class);
        intent.putExtra("haveChange",haveChange);
        SelectBookActivity.this.setResult(1,intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        onBack();
    }

    private void loadBooks(){
        Type type = new TypeToken<ArrayListResponse<Book>>() {}.getType();

        String url = GlobalInfo.host+"/word/books";
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
                    List<Book> books = response.getDatas();
                    int bookid = user.getBookid();
                    if(bookid>0) {
                        for (int i=0,size=books.size();i<size;i++){
                            Book book = books.get(i);
                            if(book.getBookid()==bookid){
                                book.setIscurrent(1);
                                break;
                            }
                        }
                    }
                    mRecyclerViewAdapter.appendToList(books);
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

    private void changeBook(final int userid,final String token,final int bookid){
        final LoadingDialog loadingDialog = new LoadingDialog(SelectBookActivity.this);
        //切换词库,获取该词库本地最大的id
        String[] where=new String[]{""+userid,""+bookid};
        Cursor cursor = globalInfo.getDb(SelectBookActivity.this).rawQuery("select id from t_words where userid=? and bookid=? order by id desc limit 1",where);
        int id=0;
        if(cursor.getCount()>0) {
            cursor.moveToNext();
            id=cursor.getInt(0);
        }

        final int wid=id;

        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, GlobalInfo.host+"/word/changebook", WordList.class, new GsonRequest.PostGsonRequest<WordList>() {
            @Override
            public void onStart() {
                loadingDialog.show();
            }
            @Override
            public void onResponse(WordList wordlist) {
                loadingDialog.cancel();
                if(wordlist.getError()!=null && wordlist.getError()!="" || wordlist.getErrno()!=0){
                    if(wordlist.getErrno()==601){
                        Intent intent = new Intent(SelectBookActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN);
                    }else {
                        Toast.makeText(SelectBookActivity.this, "changeBook error:" + wordlist.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //设置bookid
                    SharedPreferences sp = SelectBookActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                    //存入数据
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("bookid", bookid);
                    editor.commit();
                    user.setBookid(bookid);
                    haveChange=true;

                    //本地存储
                    final String wordsStr = wordlist.getWords();
                    if (serviceInterFace != null && wordsStr!=null && wordsStr.length()>0) {
                        serviceInterFace.storageBook(userid,token,bookid,wordsStr);
                    }

                    querySql("select bookid,count(id) as num from t_words group by bookid",new String[]{});
                }
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Log.e(TAG, "changeBook error:"+error.getMessage());
                Toast.makeText(SelectBookActivity.this,  "网络错误，请检查网络", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid", "" + userid);
                datas.put("token", token);
                datas.put("id",""+wid);
                datas.put("bookid",""+bookid);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(serviceIntent); // Myservice需要在清单文件中配置
    };

    @Override
    protected void onStop() {
        super.onStop();
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //接触绑定
        unbindService(serviceConnection);
    }

    private void querySql(String sql,String[] values){
        Cursor cursor = globalInfo.getDb(getApplicationContext()).rawQuery(sql, values);
        int columnSize=cursor.getColumnCount();
        Log.e(TAG, "querySql "+cursor.getCount()+", "+columnSize);
        while (cursor.moveToNext()){
            StringBuilder builder=new StringBuilder();
            for(int i=0;i<columnSize;i++){
                builder.append(cursor.getString(i));
                builder.append(",");
            }
            Log.e(TAG, builder.toString());
            //Log.e(TAG, builder.substring(0,builder.length()-1));
        }
    }
}
