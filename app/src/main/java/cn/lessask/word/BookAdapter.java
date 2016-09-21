package cn.lessask.word;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import cn.lessask.word.model.Book;
import cn.lessask.word.net.VolleyHelper;
import cn.lessask.word.recycleview.BaseRecyclerAdapter;
import cn.lessask.word.recycleview.OnItemClickListener;
import cn.lessask.word.recycleview.OnItemLongClickListener;

/**
 * Created by JHuang on 2015/11/24.
 */
public class BookAdapter extends BaseRecyclerAdapter<Book, BookAdapter.MyViewHolder> {

    private static final String TAG=BookAdapter.class.getSimpleName();
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    private Context context;

    public BookAdapter(Context context){
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
        return new MyViewHolder(view);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myHolder, final int position) {
        Book data = getItem(position);
        myHolder.name.setText(data.getName());
        if(data.getIscurrent()==1){
            //myHolder.iscurrent.setImageResource(context.getResources().getI(R.id.qq_login));
        }else{

        }

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(view, position);
            }
        });
        /*
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

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView name;
        ImageView iscurrent;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            name = (TextView)itemView.findViewById(R.id.name);
            iscurrent = (ImageView)itemView.findViewById(R.id.iscurrent);

        }
    }
}
