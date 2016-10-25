package cn.lessask.word;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import cn.lessask.word.dialog.LoadingDialog;
import cn.lessask.word.model.ArrayListResponse;
import cn.lessask.word.model.Goods;
import cn.lessask.word.model.ResponseData;
import cn.lessask.word.model.User;
import cn.lessask.word.model.WordList;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.recycleview.DividerItemDecoration;
import cn.lessask.word.recycleview.OnItemClickListener;
import cn.lessask.word.recycleview.RecyclerViewStatusSupport;
import cn.lessask.word.util.GlobalInfo;
import cn.lessask.word.util.OnClickListener;

public class BuyActivity extends AppCompatActivity {
    private String TAG = BuyActivity.class.getSimpleName();
    private final int LOGIN=1;
    private RecyclerViewStatusSupport mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private GoodsAdapter mRecyclerViewAdapter;
    private VolleyHelper volleyHelper = VolleyHelper.getInstance();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();
    private User user;
    private boolean haveChange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user=globalInfo.getUser();
        setContentView(R.layout.buy);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("兑换元宝");
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

        mLinearLayoutManager = new LinearLayoutManager(BuyActivity.this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(BuyActivity.this, DividerItemDecoration.VERTICAL_LIST));
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

        mRecyclerViewAdapter = new GoodsAdapter(getBaseContext());
        mRecyclerViewAdapter.setOnSelectListener(new OnClickListener() {
            @Override
            public void onItemClick(Object object) {
                Goods goods = (Goods)object;

            }
        });
        //设置点击事件, 编辑动作
        //*
        mRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                Log.e(TAG, "good id:"+mRecyclerViewAdapter.getItem(position).getId());
                callAliPay2(mRecyclerViewAdapter.getItem(position).getId());
                //Toast.makeText(getBaseContext(), "onClick:"+position,Toast.LENGTH_SHORT).show();
            }
        });
        //*/
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        loadGoods();
    }

    private void onBack(){
        Intent intent = new Intent(BuyActivity.this, PersionalActivity.class);
        intent.putExtra("haveChange",haveChange);
        BuyActivity.this.setResult(1,intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        onBack();
    }

    private void callAliPay2(final int goodid){

        final LoadingDialog loadingDialog = new LoadingDialog(BuyActivity.this);
        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.POST, "http://www.word.gandafu.com/buy.php", ResponseData.class, new GsonRequest.PostGsonRequest<ResponseData>() {
            @Override
            public void onStart() {
                loadingDialog.show();
            }
            @Override
            public void onResponse(ResponseData user) {
                loadingDialog.cancel();
                if(user.getError()!=null && user.getError()!="" || user.getErrno()!=0){
                    if(user.getErrno()==601){
                        Intent intent = new Intent(BuyActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN);
                    }else {
                        Toast.makeText(BuyActivity.this, user.getError(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Log.e(TAG, "callAliPay2:"+user.getData());
                    final String orderInfo=user.getData();

                    Runnable payRunnable = new Runnable() {
                        @Override
                        public void run() {
                            PayTask alipay = new PayTask(BuyActivity.this);
                            String v = alipay.getVersion();
                            Log.e(TAG, "version:"+v);
                            Map<String,String> result = alipay.payV2(orderInfo,true);
                            //alipay.h5Pay(orderInfo,true);

                            Log.e(TAG, "pay cb");

                        }
                    };
                    // 必须异步调用
                    Thread payThread = new Thread(payRunnable);
                    payThread.start();

                }
            }

            @Override
            public void onError(VolleyError error) {
                loadingDialog.cancel();
                Toast.makeText(BuyActivity.this,  "网络异常,请检查网络", Toast.LENGTH_LONG).show();
            }
            @Override
            public void setPostData(Map datas) {
                datas.put("userid",""+globalInfo.getUser().getUserid());
                datas.put("goodid",""+goodid);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(gsonRequest);
    }

    private void loadGoods(){
        Type type = new TypeToken<ArrayListResponse<Goods>>() {}.getType();

        String url = GlobalInfo.host+"/word/goods";
        GsonRequest gsonRequest = new GsonRequest<ArrayListResponse<Goods>>(Request.Method.POST,url,type,new GsonRequest.PostGsonRequest<ArrayListResponse<Goods>>(){
            @Override
            public void onStart() {
                mRecyclerView.showLoadingView();
            }

            @Override
            public void onResponse(ArrayListResponse<Goods> response) {
                if(response.getError()!=null || response.getErrno()!=0){
                    Toast.makeText(BuyActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                }else {
                    List<Goods> goods = response.getDatas();
                    mRecyclerViewAdapter.appendToList(goods);
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
