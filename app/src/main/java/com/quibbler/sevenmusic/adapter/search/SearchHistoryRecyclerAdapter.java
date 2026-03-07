package com.quibbler.sevenmusic.adapter.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quibbler.sevenmusic.R;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.search
 * ClassName:      SearchHistoryRecyclerAdapter
 * Description:    搜索历史适配器
 * Author:         zhaopeng
 * CreateDate:     2019/10/10 9:54
 */
public class SearchHistoryRecyclerAdapter extends RecyclerView.Adapter<SearchHistoryRecyclerAdapter.SearchHistoryTextHolder> {
    private Context mContext;
    private int mResource;
    private List<String> mHistory;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    public SearchHistoryRecyclerAdapter(Context mContext, int mResource, List<String> history) {
        this.mContext = mContext;
        this.mResource = mResource;
        this.mHistory = history;
    }

    @NonNull
    @Override
    public SearchHistoryTextHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        return new SearchHistoryTextHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryTextHolder holder, int position) {
        holder.searchText.setText(mHistory.get(position));
        holder.searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(null, null, position, -1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mHistory.size();
    }

    public class SearchHistoryTextHolder extends RecyclerView.ViewHolder {
        public TextView searchText;

        public SearchHistoryTextHolder(@NonNull View itemView) {
            super(itemView);
            searchText = itemView.findViewById(R.id.search_history_text_record_item);
        }
    }

    public void updateSearchDataHistory(List<String> historys) {
        this.mHistory.clear();
        this.mHistory.addAll(historys);
        notifyDataSetChanged();
    }

    public void clearSearchData() {
        this.mHistory.clear();
        notifyDataSetChanged();
    }

    /*
    在SearchMainActivity中处理点击事件
     */
    public void addOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}
