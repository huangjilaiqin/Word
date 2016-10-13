package cn.lessask.word;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import cn.lessask.word.model.Contract;
import cn.lessask.word.recycleview.BaseRecyclerAdapter;
import cn.lessask.word.recycleview.OnItemClickListener;
import cn.lessask.word.recycleview.OnItemLongClickListener;
import cn.lessask.word.util.OnClickListener;

/**
 * Created by JHuang on 2015/11/24.
 */
public class ContractAdapter extends BaseRecyclerAdapter<Contract, ContractAdapter.MyViewHolder> {

    private static final String TAG=ContractAdapter.class.getSimpleName();
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnClickListener onSelectListener;

    private Context context;

    public ContractAdapter(Context context){
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contract_item, parent, false);
        return new MyViewHolder(view);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnSelectListener(OnClickListener onSelectListener){
        this.onSelectListener=onSelectListener;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myHolder, final int position) {
        Contract data = getItem(position);


        myHolder.days.setText(""+data.getDays());
        myHolder.golden.setText(""+data.getGolden());

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener!=null)
                    onItemClickListener.onItemClick(view, position);
            }
        });
        myHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (onItemLongClickListener != null) {
                    Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                    vib.vibrate(10);
                    onItemLongClickListener.onItemLongClick(view, position);
                }
                return false;
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView days;
        TextView golden;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            days = (TextView)itemView.findViewById(R.id.days);
            golden = (TextView)itemView.findViewById(R.id.golden);
        }
    }
}
