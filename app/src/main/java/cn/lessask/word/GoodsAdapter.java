package cn.lessask.word;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

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
        /*
        myHolder.name.setText(data.getName());
        if(data.getIscurrent()==1){
            //myHolder.iscurrent.setImageResource(context.getResources().getI(R.id.qq_login));
            myHolder.iscurrent.setChecked(true);
        }else{
            myHolder.iscurrent.setChecked(false);
        }

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
        */
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView name;
        RadioButton iscurrent;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            name = (TextView)itemView.findViewById(R.id.name);
            iscurrent = (RadioButton)itemView.findViewById(R.id.iscurrent);

            iscurrent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isSelected=iscurrent.isSelected();
                    List<Goods> goodss=GoodsAdapter.this.getList();

                    Goods goods;
                    /*
                    for(int i=0,size=goodss.size();i<size;i++){
                        goods = goodss.get(i);
                        if(goods.getIscurrent()==1){
                            goods.setIscurrent(0);
                            GoodsAdapter.this.notifyItemChanged(i);
                        }else {
                            goods.setIscurrent(0);
                        }
                    }
                    if(!isSelected) {
                        int position = getLayoutPosition();
                        goods = goodss.get(position);
                        goods.setIscurrent(1);
                        GoodsAdapter.this.notifyItemChanged(position);
                        if(GoodsAdapter.this.onSelectListener!=null){
                            GoodsAdapter.this.onSelectListener.onItemClick(goods);
                        }
                    }
                    */
                }
            });
        }
    }
}
