package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.activity.my.MyCollectionMVActivity.MVShowInfo
import com.quibbler.sevenmusic.bean.mv.MvInfo

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyCollectionMvAdapter
 * Description:    MV适配器 点击播放mv，预设一部《我和我的祖国》MV
 * Author:         zhaopeng
 * CreateDate:     2019/10/9 17:07
 */
class MyCollectionMvAdapter(context: Context, resource: Int, objects: MutableList<MVShowInfo?>) :
    ArrayAdapter<MVShowInfo?>(context, resource, objects) {
    private val mInfos: MutableList<MVShowInfo?>
    private val mContext: Context
    private val mResource: Int

    init {
        this.mContext = context
        this.mInfos = objects
        this.mResource = resource
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false)
            viewHolder = ViewHolder()
            viewHolder.cover = convertView.findViewById<ImageView>(R.id.my_collection_mv_cover)
            viewHolder.name = convertView.findViewById<TextView>(R.id.my_collection_mv_name)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
        }

        if ("我和我的祖国" == mInfos.get(position)!!.getName()) {
            Glide.with(mContext).load(R.drawable.my_mv_default)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .into(viewHolder!!.cover!!)
        } else {
            Glide.with(mContext).load(mInfos.get(position)!!.getPictureurl())
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .into(viewHolder!!.cover!!)
        }
        viewHolder.cover!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val mvInfo = MvInfo(
                    mInfos.get(position)!!.getId().toInt(),
                    mInfos.get(position)!!.getName(),
                    null,
                    0,
                    null,
                    mInfos.get(position)!!.getPictureurl()
                )
                ActivityStart.startMvPlayActivity(mContext, mvInfo)
            }
        })
        viewHolder.name!!.setText(mInfos.get(position)!!.getName())
        return convertView
    }

    fun updateData(infoList: MutableList<MVShowInfo?>) {
        this.mInfos.clear()
        this.mInfos.addAll(infoList)
        notifyDataSetChanged()
    }

    private class ViewHolder {
        var cover: ImageView? = null
        var name: TextView? = null
        var detail: TextView? = null
    }
}
