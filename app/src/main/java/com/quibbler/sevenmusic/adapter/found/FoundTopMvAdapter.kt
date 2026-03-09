package com.quibbler.sevenmusic.adapter.found

import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.mv.MvPlayActivity
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.utils.BeanConverter
import com.quibbler.sevenmusic.utils.GlideRoundTransform
import com.quibbler.sevenmusic.utils.HttpUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class FoundTopMvAdapter(MvInfoList: MutableList<MvInfo?>?) :
    RecyclerBaseAdapter<MvInfo?, FoundTopMvAdapter.ViewHolder?>(MvInfoList) {
    //实际使用的数据源
    private val mMvInfoList: MutableList<MvInfo> = mSourceList

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mView: View
        var mImageView: ImageView
        var mTextView: TextView

        init {
            mView = view
            mImageView = view.findViewById<ImageView>(R.id.found_list_item_top_mv_iv)
            mTextView = view.findViewById<TextView>(R.id.found_list_item_top_mv_tv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.found_list_item_top_mv, parent, false)
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val MvInfo = mMvInfoList.get(position)

        //显示歌单名
        holder.mTextView.setText(MvInfo.getName())
        Log.d(TAG, "MvInfo.getCoverImgUrl() is: " + MvInfo.getPictureUrl())

        //下载歌单封面并显示
        //最好在这里给imageView一个占位图片，否则textView可能错乱
//        ImageDownloadPresenter.getInstance().with(holder.mImageView.getContext())
//                .load(MvInfo.getPictureUrl())
//                .imageStyle(ImageDownloadPresenter.STYLE_ROUND)
//                .into(holder.mImageView);
        val options = RequestOptions().transform(
            GlideRoundTransform(
                MusicApplication.Companion.getContext(),
                30
            )
        )

        Glide.with(holder.mImageView.getContext())
            .load(MvInfo.getPictureUrl())
            .apply(options)
            .into(holder.mImageView)

        HttpUtil.sendOkHttpRequest(MV_URL_AUTHORITY + MvInfo.getId(), object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                try {
                    val jsonObject = JSONObject(response.body!!.string()).getJSONObject("data")

                    MvInfo.setUrl(jsonObject.getJSONObject("brs").getString("480"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
            }
        })

        holder.mView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (TextUtils.isEmpty(MvInfo.getUrl())) {
                    Toast.makeText(
                        holder.mView.getContext(),
                        "视频不存在或已下架！",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                } else {
                    val intent =
                        Intent(MusicApplication.Companion.getContext(), MvPlayActivity::class.java)
                    intent.putExtra("mvInfo", BeanConverter.convertMvInfo2MvInfo(MvInfo))
                    holder.mView.getContext().startActivity(intent)
                }
            }
        })
    }

    companion object {
        private const val TAG = "FoundTopMvAdapter"
        private const val MV_URL_AUTHORITY = "http://114.116.128.229:3000/mv/detail?mvid="
    }
}
