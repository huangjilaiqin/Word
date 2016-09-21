package cn.lessask.word;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.recycleview.DividerItemDecoration;
import cn.lessask.word.recycleview.OnItemClickListener;
import cn.lessask.word.recycleview.RecyclerViewStatusSupport;
import cn.lessask.word.util.GlobalInfo;

public class SelectBookActivity extends AppCompatActivity {
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
        //设置点击事件, 编辑动作
        mRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                Toast.makeText(getBaseContext(), "onClick:"+position,Toast.LENGTH_SHORT).show();
            }
        });
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
}
