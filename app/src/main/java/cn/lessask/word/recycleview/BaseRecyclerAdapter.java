package cn.lessask.word.recycleview;

import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by JHuang on 2015/11/30.
 */
public abstract class BaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>  {
        private final List<T> mList = new LinkedList<T>();
        public List<T> getList() {
        return mList;
    }

    //子类操作列表必须使用getList, 操作原来的数据无效，因为这是复制过去的
    public void appendToList(List<T> list) {
        if (list == null) {
            return;
        }
        mList.addAll(list);
    }


    public void append(T t) {
        if (t == null) {
            return;
        }
        mList.add(t);
    }


    public void appendToTop(T item) {
        if (item == null) {
            return;
        }
        mList.add(0, item);
    }

    public void appendToTopList(List<T> list) {
        if (list == null) {
            return;
        }
        mList.addAll(0, list);
    }


    public void remove(int position) {
        if (position < mList.size() && position >= 0) {
            mList.remove(position);
        }
    }

    public void update(int position,T obj){
        remove(position);
        notifyItemRemoved(position);
        mList.add(position,obj);
        notifyItemInserted(position);
    }
    public void notifyItemUpdate(int position){
        if (position < mList.size() && position >= 0)
            notifyItemInserted(position);
    }

    public void clear() {
        mList.clear();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public T getItem(int position) {
        if (position > mList.size() - 1) {
            return null;
        }
        return mList.get(position);
    }

}
