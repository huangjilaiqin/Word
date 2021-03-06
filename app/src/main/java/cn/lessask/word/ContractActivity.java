package cn.lessask.word;

import android.content.Intent;
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
import cn.lessask.word.model.Contract;
import cn.lessask.word.model.ResponseData;
import cn.lessask.word.model.User;
import cn.lessask.word.net.GsonRequest;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.recycleview.DividerItemDecoration;
import cn.lessask.word.recycleview.OnItemClickListener;
import cn.lessask.word.recycleview.RecyclerViewStatusSupport;
import cn.lessask.word.util.GlobalInfo;
import cn.lessask.word.util.OnClickListener;

public class ContractActivity extends AppCompatActivity {
    private String TAG = ContractActivity.class.getSimpleName();
    private final int LOGIN=1;
    private RecyclerViewStatusSupport mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ContractAdapter mRecyclerViewAdapter;
    private VolleyHelper volleyHelper = VolleyHelper.getInstance();
    private GlobalInfo globalInfo = GlobalInfo.getInstance();
    private User user;
    private boolean haveChange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user=globalInfo.getUser();
        setContentView(R.layout.contract);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("选择契约");
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

        mLinearLayoutManager = new LinearLayoutManager(ContractActivity.this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ContractActivity.this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setClickable(true);

        mRecyclerViewAdapter = new ContractAdapter(getBaseContext());
        mRecyclerViewAdapter.setOnSelectListener(new OnClickListener() {
            @Override
            public void onItemClick(Object object) {

            }
        });
        //*
        mRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                Contract contract = mRecyclerViewAdapter.getItem(position);
                Intent intent = new Intent(ContractActivity.this,SignContractActivity.class);
                intent.putExtra("contract",contract);
                startActivity(intent);
            }
        });
        //*/
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        loadContract();
    }

    private void onBack(){
        Intent intent = new Intent(ContractActivity.this, PersionalActivity.class);
        intent.putExtra("haveChange",haveChange);
        ContractActivity.this.setResult(1,intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        onBack();
    }

    private void loadContract(){
        Type type = new TypeToken<ArrayListResponse<Contract>>() {}.getType();

        String url = GlobalInfo.host+"/word/contract";
        GsonRequest gsonRequest = new GsonRequest<ArrayListResponse<Contract>>(Request.Method.POST,url,type,new GsonRequest.PostGsonRequest<ArrayListResponse<Contract>>(){
            @Override
            public void onStart() {
                mRecyclerView.showLoadingView();
            }

            @Override
            public void onResponse(ArrayListResponse<Contract> response) {
                if(response.getError()!=null || response.getErrno()!=0){
                    Toast.makeText(ContractActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                }else {
                    List<Contract> goods = response.getDatas();
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
