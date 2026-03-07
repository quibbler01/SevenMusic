package com.quibbler.sevenmusic.adapter.found;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.found
 * ClassName:      RecyclerBaseAdapter
 * Description:    RecyclerView显示list内容的adapter基类。T是显示的具体内容的Bean类，VH是viewHolder类。
 * Author:         yanwuyang
 * CreateDate:     2019/9/28 11:16
 */
public abstract class RecyclerBaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    //实际使用的数据源
    protected List<T> mSourceList = new ArrayList<>();
    //是否停止更新RecyclerView
    protected boolean mShouldStop = false;

    /**
     * 当数据源list改变时，通知recyclerView更新
     *
     * @param list 新的数据源
     */
    public void updateData(List<T> list) {
        mShouldStop = false;
        if (list == null) {
            return;
        }
        //如果传入的就是数据源list，则直接更新
        if (mSourceList == list) {
            notifyDataSetChanged();
            return;
        }
        mSourceList.clear();
        mSourceList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 当页面切换时，停止更新RecyclerView的操作
     */
    public void stopUpdateData() {
        mShouldStop = true;
    }

    public RecyclerBaseAdapter(List<T> infoList) {
        mSourceList.clear();
        mSourceList.addAll(infoList);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (mShouldStop){
            return;
        }
    }

    @Override
    public int getItemCount() {
        return mSourceList.size();
    }

}
