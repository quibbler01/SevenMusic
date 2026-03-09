package com.quibbler.sevenmusic.fragment.my

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.found.SingerActivity
import com.quibbler.sevenmusic.adapter.my.MyCollectionAdapter
import com.quibbler.sevenmusic.bean.MyCollectionsInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.listener.MyCollectionViewListener
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.fragment.my
 * ClassName:      MyCollectionSongFragment
 * Description:    收藏Fragment页面 功能细节完善
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 17:13
 */
class MyCollectionSongFragment(kind: Int) : Fragment() {
    private val mCollectionKind: Int
    private val myCollectionsInfos: MutableList<MyCollectionsInfo> = ArrayList<MyCollectionsInfo>()
    private var mAdapter: MyCollectionAdapter? = null

    private var mCollectionView: View? = null
    private var mListView: ListView? = null
    private var mTextView: TextView? = null

    private val myCollectionViewListener: MyCollectionViewListener =
        object : MyCollectionViewListener {
            override fun changeView() {
                if (mTextView!!.getVisibility() == View.VISIBLE) {
                    mTextView!!.setVisibility(View.GONE)
                } else {
                    mTextView!!.setVisibility(View.VISIBLE)
                }
            }

            override fun removeData(id: Int) {
                for (myCollectionsInfo in myCollectionsInfos) {
                    if (myCollectionsInfo.getId() == id) {
                        myCollectionsInfos.remove(myCollectionsInfo)
                        return
                    }
                }
            }
        }

    init {
        this.mCollectionKind = kind
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initData()
        mCollectionView = inflater.inflate(R.layout.my_collection_fragment, container, false)

        mAdapter = MyCollectionAdapter(
            getContext()!!,
            R.layout.my_collection_item,
            myCollectionsInfos,
            mCollectionKind,
            myCollectionViewListener
        )
        mListView = mCollectionView!!.findViewById<ListView>(R.id.my_collection_list_view)
        mListView!!.setDivider(null)
        mListView!!.setAdapter(mAdapter)
        mListView!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (mCollectionKind) {
                    1 -> {
                        val intent = Intent(getActivity(), SingerActivity::class.java)
                        intent.putExtra(
                            "id",
                            myCollectionsInfos.get(position).getId().toString() + ""
                        )
                        if (getActivity() != null) {
                            getActivity()!!.startActivity(intent)
                        }
                    }

                    else -> {}
                }
            }
        })

        mTextView = mCollectionView!!.findViewById<TextView>(R.id.my_collection_text_view)
        mTextView!!.setMovementMethod(LinkMovementMethod.getInstance())
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (getActivity() != null) {
                    getActivity()!!.setResult(MyFragment.Companion.RESULT_GO_TO_FOUND)
                    getActivity()!!.finish()
                }
            }
        }
        when (mCollectionKind) {
            0 -> {
                val songSpannableStringBuilder =
                    SpannableStringBuilder("没有收藏过音乐哦\n去发现页面寻找吧!")
                songSpannableStringBuilder.setSpan(
                    clickableSpan,
                    10,
                    14,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                mTextView!!.setText(songSpannableStringBuilder)
            }

            1 -> {
                val singerSpannableStringBuilder =
                    SpannableStringBuilder("没有心怡的歌手?\n去发现页面寻找吧!")
                singerSpannableStringBuilder.setSpan(
                    clickableSpan,
                    10,
                    14,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                mTextView!!.setText(singerSpannableStringBuilder)
            }

            2 -> {
                val albumSpannableStringBuilder =
                    SpannableStringBuilder("没有喜欢的专辑?\n去发现页面寻找吧!")
                albumSpannableStringBuilder.setSpan(
                    clickableSpan,
                    10,
                    14,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                mTextView!!.setText(albumSpannableStringBuilder)
            }
        }
        if (myCollectionsInfos.size != 0) {
            mTextView!!.setVisibility(View.GONE)
        }
        return mCollectionView
    }

    fun initData() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val tmp: MutableList<MyCollectionsInfo?> = ArrayList<MyCollectionsInfo?>()
                val uri =
                    Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/collection")
                val cursor: Cursor? = MusicApplication.Companion.getContext().getContentResolver()
                    .query(uri, null, null, null, "rowid desc")
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val myCollectionsInfo = MyCollectionsInfo()
                        myCollectionsInfo.setId(cursor.getInt(cursor.getColumnIndex("id")))
                        myCollectionsInfo.setTitle(cursor.getString(cursor.getColumnIndex("title")))
                        myCollectionsInfo.setDescription(cursor.getString(cursor.getColumnIndex("description")))
                        myCollectionsInfo.setKind(cursor.getInt(cursor.getColumnIndex("kind")))
                        if (myCollectionsInfo.getKind() == mCollectionKind) {
                            tmp.add(myCollectionsInfo)
                        }
                    }
                    cursor.close()
                }
                updateUI(tmp)
            }
        })
    }

    private fun updateUI(list: MutableList<MyCollectionsInfo?>?) {
        if (getActivity() != null) {
            getActivity()!!.runOnUiThread(object : Runnable {
                override fun run() {
                    if (list != null && list.size != 0) {
                        mTextView!!.setVisibility(View.GONE)
                        mAdapter!!.updateData(list)
                    }
                }
            })
        }
    }
}
