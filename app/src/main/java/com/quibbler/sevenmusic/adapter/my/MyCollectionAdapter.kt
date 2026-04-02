package com.quibbler.sevenmusic.adapter.my

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MyCollectionsInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.listener.MyCollectionViewListener

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyCollectionAdapter
 * Description:    我的收藏适配器:三用，同时兼备歌曲，歌手，专辑三种类型数据的适配器
 * Author:         11103905
 * CreateDate:     2019/9/20 10:31
 */
class MyCollectionAdapter(
    context: Context,
    resource: Int,
    myCollectionsInfoList: MutableList<MyCollectionsInfo>,
    kind: Int,
    listener: MyCollectionViewListener
) : ArrayAdapter<MyCollectionsInfo?>(context, resource, myCollectionsInfoList) {
    private val mCollectionKind: Int
    private val mResourceID: Int
    private val mContext: Context
    private val myCollectionsInfoList: MutableList<MyCollectionsInfo>
    private val myCollectionViewListener: MyCollectionViewListener

    init {
        this.mContext = context
        this.mCollectionKind = kind
        this.mResourceID = resource
        this.myCollectionsInfoList = myCollectionsInfoList
        this.myCollectionViewListener = listener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val myCollectionsInfo = myCollectionsInfoList.get(position)
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResourceID, parent, false)
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById<ImageView>(R.id.my_collection_head_icon)
            viewHolder.title = convertView.findViewById<TextView>(R.id.my_collection_title_text)
            viewHolder.description =
                convertView.findViewById<TextView>(R.id.my_collection_description_text)
            viewHolder.start = convertView.findViewById<ImageView>(R.id.my_collection_button_icon)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
        }

        when (mCollectionKind) {
            0 -> //                File songCache = new File(SEVEN_MUSIC_IMAGE + "/" + myCollectionsInfo.getTitle().hashCode());
//                if (songCache.exists()) {
                Glide.with(mContext)
                    .load(Constant.SEVEN_MUSIC_IMAGE + "/" + myCollectionsInfo.id)
                    .placeholder(R.drawable.music_item_icon).into(viewHolder!!.icon!!)

            1 -> //                File singleCache = new File(SEVEN_MUSIC_SINGER + "/" + myCollectionsInfo.getTitle());
//                if (singleCache.exists()) {
                Glide.with(mContext)
                    .load(Constant.SEVEN_MUSIC_SINGER + "/" + myCollectionsInfo.title)
                    .placeholder(R.drawable.my_collection_singer_icon_item)
                    .into(viewHolder!!.icon!!)

            2 -> viewHolder!!.icon!!.setImageResource(R.drawable.my_collection_album_icon_item)
        }

        viewHolder!!.title!!.setText(myCollectionsInfo.title)

        viewHolder.description!!.setText(myCollectionsInfo.description)

        val star = viewHolder.start
        viewHolder.start!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                star!!.setImageResource(R.drawable.my_collection_un_star)
                val collectionUrl =
                    Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/collection/" + myCollectionsInfo.id)
                MusicApplication.Companion.context.getContentResolver()
                    .delete(collectionUrl, null, null)
                myCollectionsInfoList.remove(myCollectionsInfo)
                myCollectionViewListener.removeData(myCollectionsInfo.id)
                notifyDataSetChanged()
                if (myCollectionsInfoList.size == 0) {
                    myCollectionViewListener.changeView()
                }
                Snackbar.make(v, "移除收藏", Snackbar.LENGTH_SHORT)
                    .setAction("撤销", object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            val values = ContentValues()
                            values.put("id", myCollectionsInfo.id)
                            values.put("title", myCollectionsInfo.title)
                            values.put("description", myCollectionsInfo.description)
                            values.put("kind", myCollectionsInfo.kind)
                            MusicApplication.Companion.context.getContentResolver()
                                .insert(collectionUrl, values)
                            if (myCollectionsInfoList.size == 0) {
                                myCollectionViewListener.changeView()
                            }
                            myCollectionsInfoList.add(myCollectionsInfo)
                            notifyDataSetChanged()
                        }
                    }).show()
            }
        })
        return convertView
    }

    fun updateData(list: MutableList<MyCollectionsInfo?>) {
        myCollectionsInfoList.clear()
        myCollectionsInfoList.addAll(list)
        notifyDataSetChanged()
    }

    private class ViewHolder {
        var icon: ImageView? = null
        var title: TextView? = null
        var description: TextView? = null
        var start: ImageView? = null
    }
}
