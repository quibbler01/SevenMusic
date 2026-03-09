package com.quibbler.sevenmusic.adapter.found

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.found.SingerActivity
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter
import java.util.Locale

class FoundSingerLibSelectedAdapter(list: MutableList<FoundSingerInfo?>?) :
    RecyclerBaseAdapter<FoundSingerInfo?, FoundSingerLibSelectedAdapter.SingerViewHolder?>(list) {
    //实际使用的数据源list
    private val mFoundSingerInfoList: MutableList<FoundSingerInfo> = mSourceList

    internal class SingerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mView: View
        var mImageView: ImageView?
        var mTvName: TextView

        init {
            mView = view
            mImageView = view.findViewById<ImageView?>(R.id.found_list_item_singer_iv)
            mTvName = view.findViewById<TextView>(R.id.found_list_item_singer_tv_name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingerViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.found_list_item_selected_singer, parent, false)
        val viewHolder = SingerViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: SingerViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val foundSingerInfo = mFoundSingerInfoList.get(position)
        //歌手名
        holder.mTvName.setText(foundSingerInfo.getName())

        //下载歌手封面并显示
        //最好在这里给imageView一个占位图片，否则textView可能错乱
        ImageDownloadPresenter.Companion.getInstance().with(MusicApplication.Companion.getContext())
            .load(foundSingerInfo.getPicUrl())
            .imageStyle(ImageDownloadPresenter.Companion.STYLE_CIRCLE)
            .into(holder.mImageView)

        holder.mView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(holder.mView.getContext(), SingerActivity::class.java)
                intent.putExtra("id", foundSingerInfo.getId())
                holder.mView.getContext().startActivity(intent)
            }
        })
    }

    fun getPositionForSelection(selection: Char): Int {
        for (i in mFoundSingerInfoList.indices) {
            val firstPinyin = mFoundSingerInfoList.get(i).getFirstPinyin()
            val first: Char = firstPinyin.uppercase(Locale.getDefault()).get(0)
            if (first == selection) {
                return i
            }
        }
        return -1
    }
}
