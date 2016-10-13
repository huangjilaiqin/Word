package cn.lessask.word;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.lessask.word.model.Contract;

public class SignContractActivity extends AppCompatActivity {
    private String TAG=SignContractActivity.class.getSimpleName();
    private Contract contract;
    private TextView days,golden;
    private EditText words;
    private Button sign;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_contract);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("签订契约");
        mToolbar.setTitleTextColor(getResources().getColor(R.color.background_white));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        days=(TextView)findViewById(R.id.days);
        golden=(TextView)findViewById(R.id.golden);
        words=(EditText)findViewById(R.id.words);
        sign=(Button)findViewById(R.id.sign);

        Intent intent=getIntent();
        contract=intent.getParcelableExtra("contract");
        Log.e(TAG, "contract id:"+contract.getId());
        days.setText("" + contract.getDays());
        golden.setText(""+contract.getGolden());
        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num=20;
                try {
                    num=Integer.parseInt(words.getText().toString().trim());
                }catch (Exception e){

                }
                Toast.makeText(SignContractActivity.this,"num:"+num,Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void onBack(){
        //Intent intent = new Intent(SignContractActivity.this, PersionalActivity.class);
        //intent.putExtra("haveChange", haveChange);
        //SignContractActivity.this.setResult(1, intent);
        finish();
    }

    private void sign(){

    }
}
