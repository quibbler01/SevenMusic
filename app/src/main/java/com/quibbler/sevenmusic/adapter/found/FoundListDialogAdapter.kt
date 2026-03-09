package com.quibbler.sevenmusic.adapter.found

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.found.FoundListDialogAdapter.DialogViewHolder
import com.quibbler.sevenmusic.bean.CustomMusicList
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.utils.MusicDatabaseUtils
import com.quibbler.sevenmusic.utils.MusicThreadPool
import com.quibbler.sevenmusic.view.found.FoundCustomDialog

/**
 * Package:        com.quibbler.sevenmusic.adapter.found
 * ClassName:      FoundListDialogAdapter
 * Description:
 * Author:         yanwuyang
 * CreateDate:     2019/11/13 17:24
 */
class FoundListDialogAdapter(
    list: MutableList<CustomMusicList?>?,
    musicInfo: MusicInfo?,
    foundCustomDialog: FoundCustomDialog?
) : RecyclerBaseAdapter<CustomMusicList?, DialogViewHolder?>(list), View.OnClickListener {
    private val mCustomMusicLists: MutableList<CustomMusicList> = mSourceList
    private val mMusicInfo: MusicInfo?
    private val mFoundCustomDialog: FoundCustomDialog?

    internal class DialogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mView: View
        var mIvCover: ImageView
        var mTvName: TextView

        init {
            mView = view
            mIvCover = view.findViewById<ImageView>(R.id.found_list_item_iv_dialog_cover)
            mTvName = view.findViewById<TextView>(R.id.found_list_item_tv_dialog_name)
        }
    }

    init {
        mMusicInfo = musicInfo
        mFoundCustomDialog = foundCustomDialog
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.found_list_item_costum_dialog, parent, false)
        val viewHolder = DialogViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val customMusicList = mCustomMusicLists.get(position)
        if (customMusicList.getCoverImgUrl() != null) {
            holder.mIvCover.setImageResource(R.drawable.search_online_play_list_item)
        } else {
            Glide.with(holder.mView.getContext())
                .load(customMusicList.getCoverImgUrl())
                .placeholder(R.drawable.search_online_play_list_item)
                .into(holder.mIvCover)
        }
        holder.mTvName.setText(customMusicList.getName())

        holder.mView.setOnClickListener(this)
        holder.mView.setTag(customMusicList.getName())
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.found_list_item_ll) {
            val name = v.getTag() as String?
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    MusicDatabaseUtils.addToMusicList(name, mMusicInfo)
                }
            })
            Toast.makeText(v.getContext(), "添加成功", Toast.LENGTH_SHORT).show()

            if (mFoundCustomDialog != null) {
                mFoundCustomDialog.dismiss()
            }
        }
    }
}
