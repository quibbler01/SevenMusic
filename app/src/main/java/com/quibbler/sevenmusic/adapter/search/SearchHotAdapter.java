package com.quibbler.sevenmusic.adapter.search;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.search.HotSearchBean;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.search
 * ClassName:      SearchHotAdapter
 * Description:    热搜适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 20:39
 */
public class SearchHotAdapter extends ArrayAdapter<HotSearchBean.Data> {
    private List<HotSearchBean.Data> mLists;
    private int mResource;
    private Context mContext;

    public SearchHotAdapter(@NonNull Context context, int resource, @NonNull List<HotSearchBean.Data> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mLists = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.positionTextView = convertView.findViewById(R.id.search_hot_position);
            viewHolder.titleTextView = convertView.findViewById(R.id.search_hot_title);
            viewHolder.detailTextView = convertView.findViewById(R.id.search_hot_details);
            viewHolder.hotImage = convertView.findViewById(R.id.search_top_hot_file_icon);
            viewHolder.numberTextView = convertView.findViewById(R.id.search_hot_number);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position < 3) {
            viewHolder.positionTextView.setText(Integer.toString(position + 1));
            viewHolder.positionTextView.setTextColor(Color.RED);
            viewHolder.titleTextView.setText(mLists.get(position).getSearchWord());
            viewHolder.titleTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else {
            viewHolder.positionTextView.setText(Integer.toString(position + 1));
            viewHolder.titleTextView.setText(mLists.get(position).getSearchWord());
            viewHolder.hotImage.setVisibility(View.INVISIBLE);
        }
        viewHolder.detailTextView.setText(mLists.get(position).getContent());
        viewHolder.numberTextView.setText(mLists.get(position).getScore());

        return convertView;
    }

    private static class ViewHolder {
        TextView positionTextView;
        TextView titleTextView;
        TextView detailTextView;
        ImageView hotImage;
        TextView numberTextView;
    }
}
