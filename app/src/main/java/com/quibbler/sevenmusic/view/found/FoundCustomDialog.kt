package com.quibbler.sevenmusic.view.found

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.found.FoundListDialogAdapter
import com.quibbler.sevenmusic.bean.CustomMusicList
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.MySongListInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.fragment.my.MyFragment
import com.quibbler.sevenmusic.utils.MusicDatabaseUtils
import com.quibbler.sevenmusic.utils.MusicThreadPool

class FoundCustomDialog(context: Context, musicInfo: MusicInfo?) : Dialog(context) {
    private val mContext: Context?
    private var mThread: Thread? = null
    private var mCustomMusicLists: ArrayList<CustomMusicList?>? = null
    private var mRecyclerView: RecyclerView? = null
    private var mFoundListDialogAdapter: FoundListDialogAdapter? = null
    private val mMusicInfo: MusicInfo?
    private var mTvCreate: TextView? = null

    init {
        mContext = context
        mMusicInfo = musicInfo
    }

    @UiThread
    private fun onObtainList() {
        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        mRecyclerView!!.setLayoutManager(linearLayoutManager)

        mFoundListDialogAdapter = FoundListDialogAdapter(mCustomMusicLists, mMusicInfo, this)
        mRecyclerView!!.setAdapter(mFoundListDialogAdapter)

        mFoundListDialogAdapter!!.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.found_dialog_add_to_custom_playlist, null)
        mRecyclerView = view.findViewById<RecyclerView>(R.id.dialog_rv_custom_playlist)
        mTvCreate = view.findViewById<TextView>(R.id.dialog_tv_create_new_custom_playlist)

        setContentView(view)

        mTvCreate!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                createSongListDialog()
            }
        })

        if (mThread != null) {
            mThread!!.interrupt()
        }
        mThread = Thread(object : Runnable {
            override fun run() {
                mCustomMusicLists = MusicDatabaseUtils.getCustomMusicList()

                if (mContext != null && mContext is Activity) {
                    val activity = mContext
                    activity.runOnUiThread(object : Runnable {
                        override fun run() {
                            onObtainList()
                        }
                    })
                }
            }
        })
        mThread!!.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mThread!!.interrupt()
    }

    override fun dismiss() {
        super.dismiss()
        if (mThread != null) {
            mThread!!.interrupt()
        }
    }


    /*
    创建歌单Dialog
     */
    private fun createSongListDialog() {
        val editText = EditText(getContext())
        editText.setSingleLine()
        editText.setText("歌单名")

        val builder = AlertDialog.Builder(getContext())
            .setTitle("新建歌单").setView(editText)
            .setPositiveButton("新建", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    var cursor: Cursor? = null
                    cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                        MusicContentProvider.Companion.SONGLIST_URL,
                        null,
                        "name = ?",
                        arrayOf<String>(editText.getText().toString()),
                        null
                    )
                    if (cursor != null && cursor.getCount() != 0) {
                        Toast.makeText(getContext(), "已存在重名歌单", Toast.LENGTH_SHORT).show()
                        cursor.close()
                        return
                    }
                    val mySongListInfo = MySongListInfo()
                    mySongListInfo.setListName(editText.getText().toString())
                    mySongListInfo.setCreator(System.currentTimeMillis().toString())

                    MusicThreadPool.postRunnable(object : Runnable {
                        override fun run() {
                            val values = ContentValues()
                            values.put("name", mySongListInfo.getListName())
                            values.put("type", 0)
                            values.put("songs", "")
                            values.put("number", 0)
                            values.put("id", -1)
                            values.put(
                                MyFragment.Companion.CREATOR_KEY,
                                mySongListInfo.getCreator()
                            )
                            MusicApplication.Companion.getContext().getContentResolver()
                                .insert(MusicContentProvider.Companion.SONGLIST_URL, values)

                            mCustomMusicLists = MusicDatabaseUtils.getCustomMusicList()
                            if (mContext != null && this@FoundCustomDialog != null && mFoundListDialogAdapter != null && mContext is Activity) {
                                val activity = mContext
                                activity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        mFoundListDialogAdapter!!.updateData(mCustomMusicLists)
                                    }
                                })
                            }
                        }
                    })
                }
            }).setNegativeButton("取消", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                }
            })
        val alertDialog = builder.create()
        editText.requestFocus()
        alertDialog.show()
        alertDialog.getWindow()!!
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}
