package com.quibbler.sevenmusic.adapter.found

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.found.SingerActivity
import com.quibbler.sevenmusic.adapter.found.FoundSingerLibRecommendAdapter.SingerTopViewHolder
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter

class FoundSingerLibRecommendAdapter(foundSingerInfoList: MutableList<FoundSingerInfo?>?) :
    RecyclerBaseAdapter<FoundSingerInfo?, SingerTopViewHolder?>(foundSingerInfoList) {
    //实际使用的数据源list
    private val mFoundSingerInfoList: MutableList<FoundSingerInfo> = mSourceList

    internal class SingerTopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mView: View
        var mImageView: ImageView
        var mTextView: TextView

        init {
            mView = view
            mImageView = view.findViewById<ImageView>(R.id.found_list_item_top_singer_iv)
            mTextView = view.findViewById<TextView>(R.id.found_list_item_singer_top_tv_name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingerTopViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.found_list_item_top_singer, parent, false)
        val viewHolder = SingerTopViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: SingerTopViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val foundSingerInfo = mFoundSingerInfoList.get(position)
        holder.mTextView.setText(foundSingerInfo.getName())

        ImageDownloadPresenter.Companion.getInstance().with(holder.mImageView.getContext())
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
}
