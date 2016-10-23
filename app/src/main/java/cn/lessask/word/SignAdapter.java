package cn.lessask.word;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.lessask.word.model.Sign;
import cn.lessask.word.recycleview.BaseRecyclerAdapter;
import cn.lessask.word.recycleview.OnItemClickListener;
import cn.lessask.word.recycleview.OnItemLongClickListener;
import cn.lessask.word.util.OnClickListener;
import cn.lessask.word.util.TimeHelper;

/**
 * Created by JHuang on 2015/11/24.
 */
public class SignAdapter extends BaseRecyclerAdapter<Sign, SignAdapter.MyViewHolder> {

    private static final String TAG=SignAdapter.class.getSimpleName();
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnClickListener onSelectListener;

    private Context context;

    public SignAdapter(Context context){
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=null;
        if(viewType==0)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sign_item, parent, false);
        else if(viewType==1)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sign_item1, parent, false);
        else if(viewType==2)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sign_item2, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return getList().get(position).getStatus();
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
        Sign data = getItem(position);

        myHolder.day.setText(TimeHelper.date2Sign(data.getTime()));
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

        if(data.getSignid()!=0){
            myHolder.mark.setVisibility(View.VISIBLE);
        }else {
            myHolder.mark.setVisibility(View.INVISIBLE);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView mark;
        TextView day;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            mark=(TextView)itemView.findViewById(R.id.mark);
            day=(TextView)itemView.findViewById(R.id.day);
        }
    }
}
