package cn.lessask.word;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import cn.lessask.word.model.Goods;
import cn.lessask.word.recycleview.BaseRecyclerAdapter;
import cn.lessask.word.recycleview.OnItemClickListener;
import cn.lessask.word.recycleview.OnItemLongClickListener;
import cn.lessask.word.util.OnClickListener;

/**
 * Created by JHuang on 2015/11/24.
 */
public class GoodsAdapter extends BaseRecyclerAdapter<Goods, GoodsAdapter.MyViewHolder> {

    private static final String TAG=GoodsAdapter.class.getSimpleName();
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnClickListener onSelectListener;

    private Context context;

    public GoodsAdapter(Context context){
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goods_item, parent, false);
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
        Goods data = getItem(position);

        myHolder.amount.setText(""+data.getAmount());
        myHolder.name.setText(data.getName());

        float money = data.getMoney();
        DecimalFormat decimalFormat=new DecimalFormat(".00");
        myHolder.money.setText(decimalFormat.format(money));

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
        TextView amount;
        TextView name;
        TextView money;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            amount = (TextView)itemView.findViewById(R.id.amount);
            name = (TextView)itemView.findViewById(R.id.name);
            money = (TextView)itemView.findViewById(R.id.money);
        }
    }
}
