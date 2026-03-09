package com.quibbler.sevenmusic.view.playbar

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.playbar.PlaybarMusicListAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.ResUtil

/**
 * Package:        com.quibbler.sevenmusic.view.playbar
 * ClassName:      PlayBarMusicListDialog
 * Description:    音乐播放条-播放列表对话框
 * Author:         11103876
 * CreateDate:     2019/10/12 11:18
 */
class PlaybarMusicListDialog(context: Context) :
    Dialog(context, R.style.play_bar_music_list_dialog_attr), View.OnClickListener,
    OnItemClickListener {
    private val mContext: Context?

    private var mPlaybarCollectAllMusicTv: TextView? = null
    private var mPlaybarDownloadAllMusicTv: TextView? = null
    private var mPlaybarClearPlayListTv: TextView? = null
    private var mPlaybarShowPlayListLv: ListView? = null

    /**
     * 播放列表适配器对象实例
     */
    private var mAdapter: PlaybarMusicListAdapter? = null

    /**
     * 最近音乐播放列表集合
     */
    private val mPlaybarMusicList: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    private var musicInfo: MusicInfo? = null

    /**
     * Intent对象实例，用于传递数据
     */
    private val intent = Intent(MusicBroadcastManager.MUSIC_GLOBAL_PLAY_BAR_UPDATE)

    init {
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_bar_music_list_dialog)
        initView()
    }

    /**
     * 描述：初始化组件
     */
    private fun initView() {
        mPlaybarCollectAllMusicTv = findViewById<TextView>(R.id.play_bar_tv_collect_all_music)
        mPlaybarDownloadAllMusicTv = findViewById<TextView>(R.id.play_bar_tv_download_all_music)
        mPlaybarClearPlayListTv = findViewById<TextView>(R.id.play_bar_tv_clear_play_list)
        mPlaybarShowPlayListLv = findViewById<ListView>(R.id.play_bar_lv_show_play_list)

        // 暂时隐藏”全部收藏“与”下载“选项
        mPlaybarCollectAllMusicTv!!.setVisibility(View.INVISIBLE)
        mPlaybarDownloadAllMusicTv!!.setVisibility(View.INVISIBLE)

        mPlaybarCollectAllMusicTv!!.setOnClickListener(this)
        mPlaybarDownloadAllMusicTv!!.setOnClickListener(this)
        mPlaybarClearPlayListTv!!.setOnClickListener(this)
        mPlaybarShowPlayListLv!!.setOnItemClickListener(this)

        if (MusicPlayerService.Companion.getPlayMusicLists().size != 0) {
            mAdapter = PlaybarMusicListAdapter(
                this@PlaybarMusicListDialog,
                MusicPlayerService.Companion.getPlayMusicLists()
            )
            mPlaybarShowPlayListLv!!.setAdapter(mAdapter)
        } else {
            Toast.makeText(
                MusicApplication.Companion.getContext(),
                ResUtil.getString(R.string.str_play_bar_play_list_dialog_tips),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun show() {
        super.show()
        val manager = MusicApplication.Companion.getContext()
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.getDefaultDisplay()
        val layoutParams = getWindow()!!.getAttributes() // 设置对话框属性
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.width = display.getWidth() // 对话框宽度与屏幕相同
        layoutParams.height = (display.getHeight() * 0.6).toInt() // 对话框高度为屏幕的0.6
        getWindow()!!.getDecorView().setPadding(0, 0, 0, 0)
        getWindow()!!.setAttributes(layoutParams)
    }


    override fun onClick(v: View) {
        if (v.getId() == R.id.play_bar_tv_collect_all_music) {
            Log.i(TAG, "收藏全部歌曲")
        } else if (v.getId() == R.id.play_bar_tv_download_all_music) {
            Log.i(TAG, "下载全部歌曲")
        } else if (v.getId() == R.id.play_bar_tv_clear_play_list) {
            Log.i(TAG, "清空播放歌曲")
            clearPlayList()
        } else if (v.getId() == R.id.play_bar_iv_play_list_single_delete) {
            deletePlaybarPlayListItem(v)
        }
    }

    /**
     * 描述：清空播放列表记录
     */
    private fun clearPlayList() {
        // 注意：Dialog的context不能传入getApplicationContext()，它要依赖于activity。
        AlertDialog.Builder(mContext)
            .setTitle(ResUtil.getString(R.string.str_play_bar_play_list_dialog_title))
            .setNegativeButton(
                ResUtil.getString(R.string.str_dialog_btn_cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    }
                }).setPositiveButton(
                ResUtil.getString(R.string.str_dialog_btn_clear),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        if (MusicPlayerService.Companion.getPlayMusicLists().size != 0) {
                            MusicPlayerService.Companion.clearPlayMusicList() // 清空播放列表记录
                            mAdapter!!.notifyDataSetChanged() // 刷新数据
                        }
                    }
                }).create().show()
    }

    /**
     * 描述：删除音乐播放条播放表中所选择的播放记录
     * 
     * @param view
     * @return
     */
    private fun deletePlaybarPlayListItem(view: View) {
        val position = view.getTag() as Int // 获取被点击控件所在item的位置
        MusicPlayerService.Companion.getPlayMusicLists().removeAt(position) // 删除当前点击的item
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mAdapter!!.setSelectedId(position) // 设置当前点击item的id
        mAdapter!!.notifyDataSetChanged() // 刷新数据

        musicInfo =
            MusicPlayerService.Companion.getPlayMusicLists().get(position) // 获取播放列表当前点击的歌曲对象，进行播放
        if (musicInfo != null) {
            MusicPlayerService.Companion.playMusic(musicInfo)
            //以下广播不用发送，使用赵鹏的全局广播即可
//            intent.putExtra("musicInfo", musicInfo);
//            intent.putExtra(SONGNAME, musicInfo.getMusicSongName());
//            intent.putExtra(SONGSINGER, musicInfo.getSinger());
//            intent.putExtra(SONGID, musicInfo.getId());
//            MusicBroadcastManager.sendBroadcast(intent);
//            Log.d(TAG, musicInfo.getMusicSongName());
        }
    }

    companion object {
        private const val TAG = "PlayBarMusicListDialog"

        /**
         * 保存广播发送选中歌曲的名称键值
         */
        const val SONGNAME: String = "song_name"

        /**
         * 保存广播发送选中歌曲的歌手键值
         */
        const val SONGSINGER: String = "song_singer"

        /**
         * 保存广播发送选中歌曲的id键值
         */
        const val SONGID: String = "song_id"
    }
}
