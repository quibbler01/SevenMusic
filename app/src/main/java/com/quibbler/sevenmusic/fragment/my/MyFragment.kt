package com.quibbler.sevenmusic.fragment.my

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.found.PlaylistActivity
import com.quibbler.sevenmusic.activity.my.MyCollectionMVActivity
import com.quibbler.sevenmusic.activity.my.MyCollectionsActivity
import com.quibbler.sevenmusic.activity.my.MyDownloadMusicActivity
import com.quibbler.sevenmusic.activity.my.MyFavouriteMusicActivity
import com.quibbler.sevenmusic.activity.my.MyLocalMusicActivity
import com.quibbler.sevenmusic.activity.my.MyRecentlyPlayedMusicActivity
import com.quibbler.sevenmusic.activity.my.MySongListDetailActivity
import com.quibbler.sevenmusic.activity.my.MySongListManagerActivity
import com.quibbler.sevenmusic.adapter.my.MySongListAdapter
import com.quibbler.sevenmusic.bean.MusicURL
import com.quibbler.sevenmusic.bean.MyRecommendSongListJSonBean
import com.quibbler.sevenmusic.bean.MyRecommendSongListJSonBean.MyRecommendSongList
import com.quibbler.sevenmusic.bean.MySongListInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.broadcast.MusicBroadcastReceiver
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.listener.BroadcastDatabaseListener
import com.quibbler.sevenmusic.utils.CheckTools
import com.quibbler.sevenmusic.utils.CloseResourceUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool
import com.quibbler.sevenmusic.view.MyListView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Package:        com.quibbler.sevenmusic.fragment.my
 * ClassName:      MyFragment
 * Description:    我的页面
 * 20191009 界面优化;推荐歌单圆角显示，增加为6个推荐歌单;6大子界面图标美化;点击手势范围更改，方便点击进入子界面
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 23:50
 */
class MyFragment : Fragment(), View.OnClickListener {
    private var view: View? = null

    private var mLocalMusicView: View? = null
    private var mDownloadMusicView: View? = null
    private var mRecentlyPlayedMusicView: View? = null
    private var mFavouriteMusicView: View? = null
    private var mCollectionMusicView: View? = null
    private var mMvMusicView: View? = null

    private var mLocalMusicNumber: TextView? = null
    private var mDownloadsMusicNumber: TextView? = null
    private var mRecentlyPlayedMusicNumber: TextView? = null
    private var mMyFavouritesNumber: TextView? = null
    private var mMyCollectionsNumber: TextView? = null
    private var mMvMusicNumber: TextView? = null

    private var mMySongListShowButton: ImageView? = null
    private var mMySongListShowTextView: TextView? = null
    private var mMySongListNumberTextView: TextView? = null
    private var mBuildMySongListButton: ImageView? = null
    private var mBuildMySongListTextView: TextView? = null
    private var mEditMySongListButton: ImageView? = null
    private var mEditMySongListText: TextView? = null
    private var mNoneSongListToShow: TextView? = null
    private var mSongListView: MyListView? = null

    private var mCollectionListShowButton: ImageView? = null
    private var mCollectionListShowTextView: TextView? = null
    private var mCollectionSongListNumberTextView: TextView? = null
    private var mEditCollectionSongListButton: ImageView? = null
    private var mEditMyCollectionListText: TextView? = null
    private var mNoneCollectionListToShow: TextView? = null
    private var mCollectionListView: MyListView? = null

    private val mMySongLists: MutableList<MySongListInfo?> = ArrayList<MySongListInfo?>()
    private val mCollectionLists: MutableList<MySongListInfo?> = ArrayList<MySongListInfo?>()

    private var mSongListAdapter: MySongListAdapter? = null
    private var mCollectionAdapter: MySongListAdapter? = null

    private var mRecommendSongLists: MutableList<MyRecommendSongList?>? =
        ArrayList<MyRecommendSongList?>()
    private var mRecommendSongListIconOne: ImageView? = null
    private var mRecommendSongListIconTwo: ImageView? = null
    private var mRecommendSongListIconThree: ImageView? = null
    private var mRecommendSongListIconFour: ImageView? = null
    private var mRecommendSongListIconFive: ImageView? = null
    private var mRecommendSongListIconSix: ImageView? = null
    private var mRecommendSongListNameOne: TextView? = null
    private var mRecommendSongListNameTwo: TextView? = null
    private var mRecommendSongListNameThree: TextView? = null
    private var mRecommendSongListNameFour: TextView? = null
    private var mRecommendSongListNameFive: TextView? = null
    private var mRecommendSongListNameSix: TextView? = null
    private var mSharedPreferences: SharedPreferences? = null
    private var builder: AlertDialog.Builder? = null

    private var mNetWordStateChangeReceiver: NetWordStateChangeReceiver? = null

    private var mReceiver: MusicBroadcastReceiver? = null

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.my_fragment_layout, container, false)
        mSharedPreferences = MusicApplication.Companion.getContext()
            .getSharedPreferences("cache_my_fragment", Context.MODE_PRIVATE)

        checkPermission()

        initBaseFunction()

        initSongListFunction()

        addListener()

        initNumber()

        initSongList()

        initRecommend()

        initBroadcastReceiver()

        return view!!
    }

    fun checkPermission() {
        if (getActivity() != null) {
            if (MusicApplication.Companion.getContext()
                    .checkSelfPermission(permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    getActivity()!!,
                    arrayOf<String>(permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE
                )
            }
            if (MusicApplication.Companion.getContext()
                    .checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    getActivity()!!,
                    arrayOf<String>(permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun initBaseFunction() {
        mLocalMusicView = view!!.findViewById<View>(R.id.my_fragment_local_music)
        mDownloadMusicView = view!!.findViewById<View>(R.id.my_fragment_download_music)
        mRecentlyPlayedMusicView = view!!.findViewById<View>(R.id.my_fragment_recently_played_music)
        mFavouriteMusicView = view!!.findViewById<View>(R.id.my_fragment_favourite_music)
        mCollectionMusicView = view!!.findViewById<View>(R.id.my_fragment_collection_music)
        mMvMusicView = view!!.findViewById<View>(R.id.my_fragment_mv)

        mLocalMusicNumber = view!!.findViewById<TextView>(R.id.my_local_music_count)
        mDownloadsMusicNumber = view!!.findViewById<TextView>(R.id.my_download_music_count)
        mRecentlyPlayedMusicNumber = view!!.findViewById<TextView>(R.id.my_recently_music_count)
        mMyFavouritesNumber = view!!.findViewById<TextView>(R.id.my_favourite_music_count)
        mMyCollectionsNumber = view!!.findViewById<TextView>(R.id.my_collection_music_count)
        mMvMusicNumber = view!!.findViewById<TextView>(R.id.my_buy_music_count)

        mRecommendSongListIconOne =
            view!!.findViewById<ImageView>(R.id.my_recommend_song_list_icon_one)
        mRecommendSongListIconTwo =
            view!!.findViewById<ImageView>(R.id.my_recommend_song_list_icon_two)
        mRecommendSongListIconThree =
            view!!.findViewById<ImageView>(R.id.my_recommend_song_list_icon_three)
        mRecommendSongListIconFour =
            view!!.findViewById<ImageView>(R.id.my_recommend_song_list_icon_four)
        mRecommendSongListIconFive =
            view!!.findViewById<ImageView>(R.id.my_recommend_song_list_icon_five)
        mRecommendSongListIconSix =
            view!!.findViewById<ImageView>(R.id.my_recommend_song_list_icon_six)
        mRecommendSongListNameOne =
            view!!.findViewById<TextView>(R.id.my_recommend_song_list_text_one)
        mRecommendSongListNameTwo =
            view!!.findViewById<TextView>(R.id.my_recommend_song_list_text_two)
        mRecommendSongListNameThree =
            view!!.findViewById<TextView>(R.id.my_recommend_song_list_text_three)
        mRecommendSongListNameFour =
            view!!.findViewById<TextView>(R.id.my_recommend_song_list_text_four)
        mRecommendSongListNameFive =
            view!!.findViewById<TextView>(R.id.my_recommend_song_list_text_five)
        mRecommendSongListNameSix =
            view!!.findViewById<TextView>(R.id.my_recommend_song_list_text_six)
    }

    private fun initSongListFunction() {
        mMySongListShowButton = view!!.findViewById<ImageView>(R.id.my_song_list_show_detail)
        mMySongListShowTextView = view!!.findViewById<TextView>(R.id.my_song_list_self_text)
        mMySongListNumberTextView = view!!.findViewById<TextView>(R.id.my_song_list_number)
        mBuildMySongListButton = view!!.findViewById<ImageView>(R.id.my_song_list_add)
        mBuildMySongListTextView = view!!.findViewById<TextView>(R.id.my_song_lists_new)
        mEditMySongListButton = view!!.findViewById<ImageView>(R.id.my_song_list_edit_icon)
        mEditMySongListText = view!!.findViewById<TextView>(R.id.my_song_lists_edit)

        mCollectionListShowButton =
            view!!.findViewById<ImageView>(R.id.my_collection_song_list_show_detail)
        mCollectionListShowTextView =
            view!!.findViewById<TextView>(R.id.my_collection_song_list_self_text)
        mCollectionSongListNumberTextView =
            view!!.findViewById<TextView>(R.id.my_collection_song_list_number)
        mEditCollectionSongListButton =
            view!!.findViewById<ImageView>(R.id.my_collection_song_list_edit_icon)
        mEditMyCollectionListText =
            view!!.findViewById<TextView>(R.id.my_collection_song_lists_edit)

        mSongListAdapter = MySongListAdapter(
            MusicApplication.Companion.getContext(),
            R.layout.my_song_list_item,
            mMySongLists
        )
        mCollectionAdapter = MySongListAdapter(
            MusicApplication.Companion.getContext(),
            R.layout.my_song_list_item,
            mCollectionLists
        )

        mNoneSongListToShow = view!!.findViewById<TextView>(R.id.my_song_list_none_text_view)
        mSongListView = view!!.findViewById<MyListView>(R.id.my_song_list_view)
        mSongListView!!.setDivider(null)
        mCollectionListView = view!!.findViewById<MyListView>(R.id.my_collection_song_list_view)
        mCollectionListView!!.setDivider(null)

        mNoneCollectionListToShow =
            view!!.findViewById<TextView>(R.id.my_collection_list_none_text_view)
        mSongListView!!.setAdapter(mSongListAdapter)
        mCollectionListView!!.setAdapter(mCollectionAdapter)

        //注册ContextMenu
        registerForContextMenu(mSongListView!!)
        //注册点击事件
        mSongListView!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val intent = Intent(getContext(), MySongListDetailActivity::class.java)
                intent.putExtra(TITLE_KEY, mMySongLists.get(position)!!.getListName())
                intent.putExtra(CREATOR_KEY, mMySongLists.get(position)!!.getCreator())
                startActivity(intent)
            }
        })
        mCollectionListView!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val intent = Intent(getContext(), PlaylistActivity::class.java)
                intent.putExtra(
                    getString(R.string.playlist_id),
                    mCollectionLists.get(position)!!.getId()
                )
                startActivity(intent)
            }
        })

        val spannableStringBuilder =
            SpannableStringBuilder(getString(R.string.my_song_list_none_text_view))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                createSongListDialog()
            }
        }
        spannableStringBuilder.setSpan(clickableSpan, 8, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mNoneSongListToShow!!.setMovementMethod(LinkMovementMethod.getInstance())
        mNoneSongListToShow!!.setText(spannableStringBuilder)

        val spannableStringBuilderCollection =
            SpannableStringBuilder(getString(R.string.my_collection_list_none_text_view))
        val clickableSpanCollection: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO)
            }
        }
        spannableStringBuilderCollection.setSpan(
            clickableSpanCollection,
            10,
            14,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        mNoneCollectionListToShow!!.setMovementMethod(LinkMovementMethod.getInstance())
        mNoneCollectionListToShow!!.setText(spannableStringBuilderCollection)
    }

    private fun addListener() {
        mLocalMusicView!!.setOnClickListener(this)
        mDownloadMusicView!!.setOnClickListener(this)
        mRecentlyPlayedMusicView!!.setOnClickListener(this)
        mFavouriteMusicView!!.setOnClickListener(this)
        mCollectionMusicView!!.setOnClickListener(this)
        mMvMusicView!!.setOnClickListener(this)

        mMySongListShowButton!!.setOnClickListener(this)
        mMySongListShowTextView!!.setOnClickListener(this)
        mBuildMySongListButton!!.setOnClickListener(this)
        mBuildMySongListTextView!!.setOnClickListener(this)
        mEditMySongListButton!!.setOnClickListener(this)
        mEditMySongListText!!.setOnClickListener(this)
        mCollectionListShowButton!!.setOnClickListener(this)
        mCollectionListShowTextView!!.setOnClickListener(this)
        mEditCollectionSongListButton!!.setOnClickListener(this)
        mEditMyCollectionListText!!.setOnClickListener(this)

        mRecommendSongListIconOne!!.setOnClickListener(this)
        mRecommendSongListIconTwo!!.setOnClickListener(this)
        mRecommendSongListIconThree!!.setOnClickListener(this)
        mRecommendSongListIconFour!!.setOnClickListener(this)
        mRecommendSongListIconFive!!.setOnClickListener(this)
        mRecommendSongListIconSix!!.setOnClickListener(this)
    }

    fun initBroadcastReceiver() {
        //数据库广播
        mReceiver = MusicBroadcastReceiver(object : BroadcastDatabaseListener {
            override fun onDatabaseChanged() {
                initNumber()
                initSongList()
            }
        })
        MusicBroadcastManager.registerMusicBroadcastReceiver(
            mReceiver,
            MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE
        )
        //网络状态广播
        mNetWordStateChangeReceiver = NetWordStateChangeReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(MusicBroadcastManager.SYSTEM_BROADCAST_NETWORK_CHANGE)
        if (getActivity() != null) {
            getActivity()!!.registerReceiver(mNetWordStateChangeReceiver, intentFilter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterForContextMenu(mSongListView!!)
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mReceiver)
        if (getActivity() != null) {
            getActivity()!!.unregisterReceiver(mNetWordStateChangeReceiver)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        initNumber()
        initSongList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
    }

    /**
     * 查询歌曲数量
     * 显示在各个分类的下方
     */
    @UiThread
    private fun initNumber() {
        if (CheckTools.hasPermission(
                permission.READ_EXTERNAL_STORAGE,
                MusicApplication.Companion.getContext()
            )
        ) {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    try {
                        val localMusicResolver: ContentResolver =
                            MusicApplication.Companion.getContext().getContentResolver()
                        val localMusicCursor = localMusicResolver.query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            null,
                            null,
                            null,
                            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
                        )
                        val localMusicNumber = localMusicCursor!!.getCount()
                        CloseResourceUtil.closeInputAndOutput(localMusicCursor)

                        val downloadMusicCursor = localMusicResolver.query(
                            MusicContentProvider.Companion.DOWNLOAD_URL,
                            null,
                            "is_download = ?",
                            arrayOf<String>("0"),
                            null
                        )
                        val downloadMusicNumber = downloadMusicCursor!!.getCount()
                        CloseResourceUtil.closeInputAndOutput(downloadMusicCursor)

                        val recentlyPlayedMusicCursor = localMusicResolver.query(
                            MusicContentProvider.Companion.PLAYED_URL,
                            null,
                            null,
                            null,
                            null
                        )
                        val recentlyNumber = recentlyPlayedMusicCursor!!.getCount()
                        CloseResourceUtil.closeInputAndOutput(recentlyPlayedMusicCursor)

                        val favouritePlayedMusicCursor = localMusicResolver.query(
                            MusicContentProvider.Companion.FAVOURITE_URL,
                            null,
                            null,
                            null,
                            null
                        )
                        val favouriteNumber = favouritePlayedMusicCursor!!.getCount()
                        CloseResourceUtil.closeInputAndOutput(favouritePlayedMusicCursor)

                        val collectionCursor = localMusicResolver.query(
                            MusicContentProvider.Companion.COLLECTION_URL,
                            null,
                            "kind = ?",
                            arrayOf<String>("0"),
                            null
                        )
                        val collectionNumber = collectionCursor!!.getCount()
                        CloseResourceUtil.closeInputAndOutput(collectionCursor)

                        val cmvCursor = localMusicResolver.query(
                            MusicContentProvider.Companion.MV_URL,
                            null,
                            null,
                            null,
                            null
                        )
                        val mvNumber = cmvCursor!!.getCount()
                        CloseResourceUtil.closeInputAndOutput(cmvCursor)

                        updateSongNumber(
                            localMusicNumber,
                            downloadMusicNumber,
                            recentlyNumber,
                            favouriteNumber,
                            collectionNumber,
                            mvNumber
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    }
                }
            })
        } else {
            if (getActivity() != null) {
                Toast.makeText(
                    getActivity(),
                    getString(R.string.my_local_music_scan_permission),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @UiThread
    private fun updateSongNumber(vararg numbers: Int) {
        if (getActivity() != null) {
            getActivity()!!.runOnUiThread(object : Runnable {
                override fun run() {
                    mLocalMusicNumber!!.setText(numbers[0].toString() + "首")
                    mDownloadsMusicNumber!!.setText(numbers[1].toString() + "首")
                    mRecentlyPlayedMusicNumber!!.setText(numbers[2].toString() + "首")
                    mMyFavouritesNumber!!.setText(numbers[3].toString() + "首")
                    mMyCollectionsNumber!!.setText(String.format("%d首", numbers[4]))
                    mMvMusicNumber!!.setText(String.format("%d部", numbers[5]))
                }
            })
        }
    }

    @WorkerThread
    fun initSongList() {
        MusicThreadPool.postRunnable(object : Runnable {
            @SuppressLint("Range")
            override fun run() {
                val songLists: MutableList<MySongListInfo?> = ArrayList<MySongListInfo?>()
                val collectionLists: MutableList<MySongListInfo?> = ArrayList<MySongListInfo?>()
                var songListCursor: Cursor? = null
                try {
                    songListCursor = MusicApplication.Companion.getContext().getContentResolver()
                        .query(MusicContentProvider.Companion.SONGLIST_URL, null, null, null, null)
                    if (songListCursor != null) {
                        while (songListCursor.moveToNext()) {
                            val mySongListInfo = MySongListInfo()
                            mySongListInfo.setListName(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "name"
                                    )
                                )
                            )
                            mySongListInfo.setDescription(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "description"
                                    )
                                )
                            )
                            mySongListInfo.setType(
                                songListCursor.getInt(
                                    songListCursor.getColumnIndex(
                                        "type"
                                    )
                                )
                            )
                            mySongListInfo.setNumber(
                                songListCursor.getInt(
                                    songListCursor.getColumnIndex(
                                        "number"
                                    )
                                )
                            )
                            mySongListInfo.setId(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "id"
                                    )
                                )
                            )
                            mySongListInfo.setCreator(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "creator"
                                    )
                                )
                            )
                            mySongListInfo.setSongsJsonData(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "songs"
                                    )
                                )
                            )
                            mySongListInfo.setImageUrl(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "coverimgurl"
                                    )
                                )
                            )
                            when (mySongListInfo.getType()) {
                                0 -> songLists.add(mySongListInfo)
                                1 -> collectionLists.add(mySongListInfo)
                                else -> {}
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (songListCursor != null) {
                        songListCursor.close()
                    }
                }
                updateCollectionSongListUI(songLists, collectionLists)
            }
        })
    }

    @MainThread
    private fun updateCollectionSongListUI(
        songLists: MutableList<MySongListInfo?>,
        collectionLists: MutableList<MySongListInfo?>
    ) {
        if (getActivity() != null) {
            getActivity()!!.runOnUiThread(object : Runnable {
                override fun run() {
                    mMySongLists.clear()
                    mMySongLists.addAll(songLists)
                    mSongListAdapter!!.notifyDataSetChanged()

                    mCollectionLists.clear()
                    mCollectionLists.addAll(collectionLists)
                    mCollectionAdapter!!.notifyDataSetChanged()

                    mMySongListNumberTextView!!.setText("(" + mMySongLists.size + ")")
                    mCollectionSongListNumberTextView!!.setText("(" + mCollectionLists.size + ")")

                    if (mMySongLists.size != 0) {
                        mNoneSongListToShow!!.setVisibility(View.GONE)
                    } else {
                        mNoneSongListToShow!!.setVisibility(View.VISIBLE)
                    }

                    if (mCollectionLists.size != 0) {
                        mNoneCollectionListToShow!!.setVisibility(View.GONE)
                    } else {
                        mNoneCollectionListToShow!!.setVisibility(View.VISIBLE)
                    }
                }
            })
        }
    }

    @WorkerThread
    fun initRecommend() {
        if (!CheckTools.isNetWordAvailable(getContext())) {
            loadRecommendDataFromCache()
            return
        }
        loadRecommendDataFromCache()
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var connection: HttpURLConnection? = null
                var reader: BufferedReader? = null
                try {
                    val url = URL(MusicURL.API_HIGHQUALITY_SONGLIST)

                    connection = url.openConnection() as HttpURLConnection?
                    connection!!.setRequestMethod("GET")
                    connection.setConnectTimeout(8000)
                    connection.setReadTimeout(8000)

                    val inputStream = connection.getInputStream()
                    reader = BufferedReader(InputStreamReader(inputStream))

                    val response = StringBuilder()
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        response.append(line)
                    }
                    val gson = Gson()
                    mRecommendSongLists = gson.fromJson<MyRecommendSongListJSonBean?>(
                        response.toString(),
                        MyRecommendSongListJSonBean::class.java
                    ).getPlaylists()
                    if (mRecommendSongLists != null && mRecommendSongLists!!.size != 0) {
                        updateRecommendSongListOnUI(mRecommendSongLists)
                        storeRecommendDataCache()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (connection != null) {
                        connection.disconnect()
                    }
                }
            }
        })
    }

    @UiThread
    private fun updateRecommendSongListOnUI(result: MutableList<MyRecommendSongList?>?) {
        if (getActivity() != null) {
            getActivity()!!.runOnUiThread(object : Runnable {
                override fun run() {
                    if (result != null && result.size != 0) {
                        for (i in result.indices) {
                            when (i) {
                                0 -> {
                                    mRecommendSongListNameOne!!.setText(result.get(i)!!.getName())
                                    if (getContext() != null) {
                                        Glide.with(getContext()!!)
                                            .load(result.get(i)!!.getCoverImgUrl())
                                            .placeholder(R.drawable.my_wait_icon)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                                            .into(mRecommendSongListIconOne!!)
                                    }
                                }

                                1 -> {
                                    mRecommendSongListNameTwo!!.setText(result.get(i)!!.getName())
                                    if (getContext() != null) {
                                        Glide.with(getContext()!!)
                                            .load(result.get(i)!!.getCoverImgUrl())
                                            .placeholder(R.drawable.my_wait_icon)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                                            .into(mRecommendSongListIconTwo!!)
                                    }
                                }

                                2 -> {
                                    mRecommendSongListNameThree!!.setText(result.get(i)!!.getName())
                                    if (getContext() != null) {
                                        Glide.with(getContext()!!)
                                            .load(result.get(i)!!.getCoverImgUrl())
                                            .placeholder(R.drawable.my_wait_icon)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                                            .into(mRecommendSongListIconThree!!)
                                    }
                                }

                                3 -> {
                                    mRecommendSongListNameFour!!.setText(result.get(i)!!.getName())
                                    if (getContext() != null) {
                                        Glide.with(getContext()!!)
                                            .load(result.get(i)!!.getCoverImgUrl())
                                            .placeholder(R.drawable.my_wait_icon)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                                            .into(mRecommendSongListIconFour!!)
                                    }
                                }

                                4 -> {
                                    mRecommendSongListNameFive!!.setText(result.get(i)!!.getName())
                                    if (getContext() != null) {
                                        Glide.with(getContext()!!)
                                            .load(result.get(i)!!.getCoverImgUrl())
                                            .placeholder(R.drawable.my_wait_icon)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                                            .into(mRecommendSongListIconFive!!)
                                    }
                                }

                                5 -> {
                                    mRecommendSongListNameSix!!.setText(result.get(i)!!.getName())
                                    if (getContext() != null) {
                                        Glide.with(getContext()!!)
                                            .load(result.get(i)!!.getCoverImgUrl())
                                            .placeholder(R.drawable.my_wait_icon)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                                            .into(mRecommendSongListIconSix!!)
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onClick(v: View) {
        val id = v.getId()
        Log.d(TAG, "on click id = " + id)
        if (id == R.id.my_fragment_local_music) {
            val openLocalMusicIntent = Intent(v.getContext(), MyLocalMusicActivity::class.java)
            startActivityForResult(openLocalMusicIntent, REQUEST_CODE_OPEN_LOCAL)
        } else if (id == R.id.my_fragment_download_music) {
            val openDownloadMusicIntent =
                Intent(v.getContext(), MyDownloadMusicActivity::class.java)
            startActivityForResult(openDownloadMusicIntent, REQUEST_CODE_OPEN_DOWNLOAD)
        } else if (id == R.id.my_fragment_recently_played_music) {
            val openPlayedIntent = Intent(v.getContext(), MyRecentlyPlayedMusicActivity::class.java)
            startActivityForResult(openPlayedIntent, REQUEST_CODE_OPEN_PLAYED)
        } else if (id == R.id.my_fragment_favourite_music) {
            val openFavouriteIntent = Intent(v.getContext(), MyFavouriteMusicActivity::class.java)
            startActivityForResult(openFavouriteIntent, REQUEST_CODE_OPEN_FAVOURITE)
        } else if (id == R.id.my_fragment_collection_music) {
            val openCollectionIntent = Intent(v.getContext(), MyCollectionsActivity::class.java)
            startActivityForResult(openCollectionIntent, REQUEST_CODE_OPEN_COLLECTION)
        } else if (id == R.id.my_fragment_mv) {
            val openBoughtMusicIntent = Intent(v.getContext(), MyCollectionMVActivity::class.java)
            startActivityForResult(openBoughtMusicIntent, REQUEST_CODE_OPEN_BOUGHT)
        } else if (id == R.id.my_song_list_show_detail || id == R.id.my_song_list_self_text) {
            if (mMySongLists.size == 0) {
                if (mNoneSongListToShow!!.getVisibility() == View.VISIBLE) {
                    mMySongListShowButton!!.animate().rotation(-180f)
                    mNoneSongListToShow!!.setVisibility(View.GONE)
                } else {
                    mMySongListShowButton!!.animate().rotation(0f)
                    mNoneSongListToShow!!.setVisibility(View.VISIBLE)
                }
                return
            }

            if (mSongListView!!.getVisibility() == View.GONE) {
                mSongListView!!.setVisibility(View.VISIBLE)
                mMySongListShowButton!!.animate().rotation(0f)
            } else {
                mMySongListShowButton!!.animate().rotation(-180f)
                mSongListView!!.setVisibility(View.GONE)
            }
        } else if (id == R.id.my_song_lists_new || id == R.id.my_song_list_add) {
            createSongListDialog()
        } else if (id == R.id.my_song_list_edit_icon || id == R.id.my_song_lists_edit) {
            val editMySongListIntent = Intent(
                MusicApplication.Companion.getContext(),
                MySongListManagerActivity::class.java
            )
            editMySongListIntent.putExtra(TITLE_KEY, "我的歌单")
            //                editMySongListIntent.putExtra("data", (Serializable) mMySongLists);
            editMySongListIntent.putExtra(TYPE_KEY, 0)
            startActivity(editMySongListIntent)
        } else if (id == R.id.my_collection_song_list_show_detail || id == R.id.my_collection_song_list_self_text) {
            if (mCollectionLists.size == 0) {
                if (mNoneCollectionListToShow!!.getVisibility() == View.VISIBLE) {
                    mCollectionListShowButton!!.animate().rotation(-180f)
                    mNoneCollectionListToShow!!.setVisibility(View.GONE)
                } else {
                    mCollectionListShowButton!!.animate().rotation(0f)
                    mNoneCollectionListToShow!!.setVisibility(View.VISIBLE)
                }
                return
            }

            if (mCollectionListView!!.getVisibility() == View.GONE) {
                mCollectionListView!!.setVisibility(View.VISIBLE)
                mCollectionListShowButton!!.animate().rotation(0f)
            } else {
                mCollectionListShowButton!!.animate().rotation(-180f)
                mCollectionListView!!.setVisibility(View.GONE)
            }
        } else if (id == R.id.my_collection_song_list_edit_icon || id == R.id.my_collection_song_lists_edit) {
            val intentCollectionListManagerActivity = Intent(
                MusicApplication.Companion.getContext(),
                MySongListManagerActivity::class.java
            )
            intentCollectionListManagerActivity.putExtra(TITLE_KEY, "收藏歌单")
            //                intentCollectionListManagerActivity.putExtra("data", (Serializable) mCollectionLists);
            intentCollectionListManagerActivity.putExtra(TYPE_KEY, 1)
            startActivity(intentCollectionListManagerActivity)
        } else if (id == R.id.my_recommend_song_list_icon_one) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(
                    getContext(),
                    getString(R.string.my_network_un_avaiable),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent1 =
                Intent(MusicApplication.Companion.getContext(), PlaylistActivity::class.java)
            intent1.putExtra(
                getContext()!!.getString(R.string.playlist_id),
                mRecommendSongLists!!.get(0)!!.getId()
            )
            startActivity(intent1)
        } else if (id == R.id.my_recommend_song_list_icon_two) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(
                    getContext(),
                    getString(R.string.my_network_un_avaiable),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent2 =
                Intent(MusicApplication.Companion.getContext(), PlaylistActivity::class.java)
            intent2.putExtra(
                getContext()!!.getString(R.string.playlist_id),
                mRecommendSongLists!!.get(1)!!.getId()
            )
            startActivity(intent2)
        } else if (id == R.id.my_recommend_song_list_icon_three) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(
                    getContext(),
                    getString(R.string.my_network_un_avaiable),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent3 =
                Intent(MusicApplication.Companion.getContext(), PlaylistActivity::class.java)
            intent3.putExtra(
                getContext()!!.getString(R.string.playlist_id),
                mRecommendSongLists!!.get(2)!!.getId()
            )
            startActivity(intent3)
        } else if (id == R.id.my_recommend_song_list_icon_four) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(
                    getContext(),
                    getString(R.string.my_network_un_avaiable),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent4 =
                Intent(MusicApplication.Companion.getContext(), PlaylistActivity::class.java)
            intent4.putExtra(
                getContext()!!.getString(R.string.playlist_id),
                mRecommendSongLists!!.get(3)!!.getId()
            )
            startActivity(intent4)
        } else if (id == R.id.my_recommend_song_list_icon_five) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(
                    getContext(),
                    getString(R.string.my_network_un_avaiable),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent5 =
                Intent(MusicApplication.Companion.getContext(), PlaylistActivity::class.java)
            intent5.putExtra(
                getContext()!!.getString(R.string.playlist_id),
                mRecommendSongLists!!.get(4)!!.getId()
            )
            startActivity(intent5)
        } else if (id == R.id.my_recommend_song_list_icon_six) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(
                    getContext(),
                    getString(R.string.my_network_un_avaiable),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent6 =
                Intent(MusicApplication.Companion.getContext(), PlaylistActivity::class.java)
            intent6.putExtra(
                getContext()!!.getString(R.string.playlist_id),
                mRecommendSongLists!!.get(5)!!.getId()
            )
            startActivity(intent6)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (getActivity() != null) {
            getActivity()!!.getMenuInflater().inflate(R.menu.my_song_list_context_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.getMenuInfo() as AdapterContextMenuInfo?
        val pos = mSongListView!!.getAdapter().getItemId(menuInfo!!.position).toInt()
        if (mMySongLists.size == 0) {
            return false
        }
        if (item.getItemId() == R.id.my_song_list_context_menu_delete) {
            MusicApplication.Companion.getContext().getContentResolver().delete(
                MusicContentProvider.Companion.SONGLIST_URL,
                "name = ?",
                arrayOf<String?>(mMySongLists.get(pos)!!.getListName())
            )
            mMySongLists.removeAt(pos)
            mSongListAdapter!!.notifyDataSetChanged()
            mMySongListNumberTextView!!.setText("(" + mMySongLists.size + ")")
            if (mMySongLists.size == 0) {
                mNoneSongListToShow!!.setVisibility(View.VISIBLE)
            }
        } else if (item.getItemId() == R.id.my_song_list_context_menu_edit) {
            val intent = Intent(getContext(), MySongListDetailActivity::class.java)
            intent.putExtra(CREATOR_KEY, mMySongLists.get(pos)!!.getCreator())
            intent.putExtra(TITLE_KEY, mMySongLists.get(pos)!!.getListName())
            startActivity(intent)
        } else if (item.getItemId() == R.id.my_song_list_context_menu_add_to_play) {
            //
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                READ_EXTERNAL_STORAGE -> {
                    initNumber()
                    initSongList()
                }

                else -> {}
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Deprecated("")
    fun showCreateSongListPopupWindow(v: View?) {
        val view = View.inflate(getContext(), R.layout.my_create_song_list_pop_menu, null)
        val createSongListPopupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        createSongListPopupWindow.setOutsideTouchable(true)
        createSongListPopupWindow.setAnimationStyle(R.style.my_create_song_list_dialog_animation)

        val editText = view.findViewById<EditText>(R.id.my_create_text_list_name)
        editText.setText("我的歌单 " + (mMySongLists.size + 1))

        val cancel = view.findViewById<Button>(R.id.my_create_dialog_cancel)
        val create = view.findViewById<Button>(R.id.my_create_dialog_confirm)
        cancel.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                createSongListPopupWindow.dismiss()
            }
        })
        create.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                createSongListPopupWindow.dismiss()
                val mySongListInfo = MySongListInfo()
                mySongListInfo.setListName(editText.getText().toString())
                mMySongLists.add(mySongListInfo)
                mSongListAdapter!!.notifyDataSetChanged()
                mMySongListNumberTextView!!.setText("(" + mMySongLists.size + ")")
                val values = ContentValues()
                values.put("name", mySongListInfo.getListName())
                values.put("type", 0)
                values.put("songs", "")
                values.put("number", 0)
                values.put("id", -1)
                MusicApplication.Companion.getContext().getContentResolver()
                    .insert(MusicContentProvider.Companion.SONGLIST_URL, values)
                mNoneSongListToShow!!.setVisibility(View.GONE)
            }
        })
        createSongListPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0)
    }

    /*
    创建歌单Dialog
     */
    private fun createSongListDialog() {
        val editText = EditText(getContext())
        editText.setSingleLine()
        editText.setText(getString(R.string.my_song_list_create_name_list) + (mMySongLists.size + 1))

        builder = AlertDialog.Builder(getContext())
            .setTitle(getString(R.string.my_song_lists_new_song_list)).setView(editText)
            .setPositiveButton(
                getString(R.string.my_song_lists_new),
                object : DialogInterface.OnClickListener {
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
                            Toast.makeText(
                                getContext(),
                                getString(R.string.my_song_lists_new_conflict),
                                Toast.LENGTH_SHORT
                            ).show()
                            cursor.close()
                            return
                        }
                        val mySongListInfo = MySongListInfo()
                        mySongListInfo.setListName(editText.getText().toString())
                        mySongListInfo.setCreator(System.currentTimeMillis().toString())
                        mMySongLists.add(mySongListInfo)
                        mSongListAdapter!!.notifyDataSetChanged()
                        mMySongListNumberTextView!!.setText("(" + mMySongLists.size + ")")
                        mNoneSongListToShow!!.setVisibility(View.GONE)
                        MusicThreadPool.postRunnable(object : Runnable {
                            override fun run() {
                                val values = ContentValues()
                                values.put("name", mySongListInfo.getListName())
                                values.put("type", 0)
                                values.put("songs", "")
                                values.put("number", 0)
                                values.put("id", -1)
                                values.put(CREATOR_KEY, mySongListInfo.getCreator())
                                MusicApplication.Companion.getContext().getContentResolver()
                                    .insert(MusicContentProvider.Companion.SONGLIST_URL, values)
                            }
                        })
                    }
                }).setNegativeButton(
                getString(R.string.my_song_lists_new_cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }
                })
        val alertDialog = builder!!.create()
        editText.requestFocus()
        alertDialog.show()
        alertDialog.getWindow()!!
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /*
       处理各个Activity返回结果,跳转请求
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_OPEN_PLAYED, REQUEST_CODE_OPEN_FAVOURITE, REQUEST_CODE_OPEN_COLLECTION -> when (resultCode) {
                RESULT_GO_TO_FOUND -> MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO)
                else -> {}
            }

            REQUEST_CODE_OPEN_BOUGHT -> when (resultCode) {
                RESULT_GO_TO_FOUND -> MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE)
                else -> {}
            }

            REQUEST_CODE_OPEN_LOCAL, REQUEST_CODE_OPEN_DOWNLOAD -> {}
            else -> {}
        }
    }

    private inner class NetWordStateChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.getAction()
            if (action != null) {
                when (action) {
                    MusicBroadcastManager.SYSTEM_BROADCAST_NETWORK_CHANGE -> if (CheckTools.isNetWordAvailable(
                            getContext()
                        )
                    ) {
                        if (mRecommendSongLists!!.size == 0) {
                            initRecommend()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun hasLocalCache(): Boolean {
        return mSharedPreferences!!.getBoolean(HAS_LOCAL_RECOMMEND_CACHE, false)
    }

    private fun storeRecommendDataCache() {
        val gson = Gson()
        val string = gson.toJson(mRecommendSongLists)
        val editor = mSharedPreferences!!.edit()
        editor.putBoolean(HAS_LOCAL_RECOMMEND_CACHE, true)
        editor.putString(LOCAL_RECOMMEND_CACHE, string)
        editor.apply()
    }

    private fun loadRecommendDataFromCache() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                if (hasLocalCache()) {
                    val gson = Gson()
                    val jsonData = mSharedPreferences!!.getString(LOCAL_RECOMMEND_CACHE, "")
                    if ("" != jsonData) {
                        val recommendSongLists = gson.fromJson<MutableList<MyRecommendSongList?>?>(
                            jsonData,
                            object : TypeToken<MutableList<MyRecommendSongList?>?>() {
                            }.getType()
                        )
                        if (recommendSongLists != null && recommendSongLists.size == 6) {
                            mRecommendSongLists = recommendSongLists
                            updateRecommendSongListOnUI(mRecommendSongLists)
                        }
                    }
                }
            }
        })
    }

    companion object {
        private const val TAG = "MyFragment"

        private const val READ_EXTERNAL_STORAGE = 1
        private const val WRITE_EXTERNAL_STORAGE = 2

        const val REQUEST_CODE_OPEN_LOCAL: Int = 0
        const val REQUEST_CODE_OPEN_DOWNLOAD: Int = 1
        const val REQUEST_CODE_OPEN_PLAYED: Int = 2
        const val REQUEST_CODE_OPEN_FAVOURITE: Int = 3
        const val REQUEST_CODE_OPEN_COLLECTION: Int = 4
        const val REQUEST_CODE_OPEN_BOUGHT: Int = 5

        const val RESULT_OK: Int = 0
        const val RESULT_GO_TO_FOUND: Int = 1

        const val TITLE_KEY: String = "title"
        const val CREATOR_KEY: String = "creator"
        const val TYPE_KEY: String = "type"

        private const val HAS_LOCAL_RECOMMEND_CACHE = "hasMyRecommendSongListJSonBean"
        private const val LOCAL_RECOMMEND_CACHE = "MyRecommendSongListJSonBean"
    }
}
