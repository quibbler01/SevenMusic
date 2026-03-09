package com.quibbler.sevenmusic.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.database.MatrixCursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.search.GroupItemDecoration
import com.quibbler.sevenmusic.adapter.search.GroupItemDecoration.TitleDecorationCallback
import com.quibbler.sevenmusic.adapter.search.SearchHistoryRecyclerAdapter
import com.quibbler.sevenmusic.adapter.search.SearchHotAdapter
import com.quibbler.sevenmusic.adapter.search.SearchResultAdapter
import com.quibbler.sevenmusic.bean.MusicURL
import com.quibbler.sevenmusic.bean.search.HotSearchBean
import com.quibbler.sevenmusic.bean.search.SearchAlbumBean
import com.quibbler.sevenmusic.bean.search.SearchAlbumBean.Album
import com.quibbler.sevenmusic.bean.search.SearchArtistsBean
import com.quibbler.sevenmusic.bean.search.SearchBean
import com.quibbler.sevenmusic.bean.search.SearchMvBean
import com.quibbler.sevenmusic.bean.search.SearchMvBean.Mv
import com.quibbler.sevenmusic.bean.search.SearchPlayListBean
import com.quibbler.sevenmusic.bean.search.SearchPlayListBean.PlayList
import com.quibbler.sevenmusic.bean.search.SearchSongBean
import com.quibbler.sevenmusic.bean.search.SearchSuggestionBean
import com.quibbler.sevenmusic.utils.CheckTools
import com.quibbler.sevenmusic.utils.CloseResourceUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Arrays
import java.util.Random
import java.util.concurrent.CountDownLatch

/**
 * Package:        com.quibbler.sevenmusic.activity
 * ClassName:      SearchMainActivity
 * Description:    在线综合搜索,热搜显示，搜索提示显示，综合搜索结果展示：歌曲，视频，歌手，专辑，歌单等
 * 20191009 优化搜索，搜索等待提示。优化热搜展示效果及获取逻辑。搜索逻辑优化，根据输入的关键字，展示热搜或者关键字提示
 * 20191010 搜索历史记录
 * 20191025 热搜缓存处理，无网仍然显示缓存的数据。搜索界面UI优化，内容更紧凑。
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 19:40
 */
class SearchMainActivity : AppCompatActivity() {
    private val mSearchHistoryContainer = StringBuilder()

    private var mSearchSongBeanList: MutableList<SearchSongBean.Song?>? = null
    private var mSearchAlbumBeanList: MutableList<Album?>? = null
    private var mSearchArtistBeanList: MutableList<SearchArtistsBean.Artist?>? = null
    private var mSearchPlayListBeanList: MutableList<PlayList?>? = null
    private var mSearchMvBeanList: MutableList<Mv?>? = null

    private var mSearchWaitView: View? = null
    private var mSearchWaitGif: ImageView? = null

    private var mSearchView: SearchView? = null
    private var mSearchHintAdapter: SimpleCursorAdapter? = null
    private var mSearchAutoComplete: SearchAutoComplete? = null
    private var mSearchResultRecycleView: RecyclerView? = null
    private var mSearchResultAdapter: SearchResultAdapter? = null
    private val mSearchResultLists: MutableList<SearchBean?> = ArrayList<SearchBean?>()
    private var mSearchStatusFlag = false

    private var mHistoryView: LinearLayout? = null
    private var mClearHistory: ImageView? = null
    private var mHistoryListView: RecyclerView? = null
    private val mSearchHistoryLists: MutableList<String?> = ArrayList<String?>()
    private var mHistoryRecyclerAdapter: SearchHistoryRecyclerAdapter? = null

    private var mTopView: LinearLayout? = null
    private var mHotSearchListView: ListView? = null
    private val mHotSearchLists: MutableList<HotSearchBean.Data?> = ArrayList<HotSearchBean.Data?>()
    private var mSearchHotAdapter: SearchHotAdapter? = null
    private var mSharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_search_main)
        if (getSupportActionBar() != null) {
            getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        }
        mSharedPreferences = getPreferences(MODE_PRIVATE)

        initRecommendList()

        initHotSearchData()

        initSearchHistory()
    }

    private fun initSearchFunction() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL)

        mSearchWaitView = findViewById<View>(R.id.search_online_wait_layout)
        mSearchWaitGif = findViewById<ImageView>(R.id.search_online_wait_hint)
        Glide.with(this).load(R.drawable.search_online_wait_gif).into(mSearchWaitGif!!)

        mSearchResultRecycleView = findViewById<RecyclerView>(R.id.search_result_recycler_view)
        mSearchResultRecycleView!!.setLayoutManager(linearLayoutManager)


        mSearchResultAdapter = SearchResultAdapter(this, mSearchResultLists)
        mSearchResultRecycleView!!.setAdapter(mSearchResultAdapter)
    }

    private fun getSearchData(word: String?) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                saveSearchHistory(word)
                val temp: MutableList<SearchBean?> = ArrayList<SearchBean?>()
                val indexs: MutableList<Int?> = ArrayList<Int?>()
                val gson = Gson()
                try {
                    val countDownLatch = CountDownLatch(5)
                    MusicThreadPool.postRunnable(object : Runnable {
                        override fun run() {
                            try {
                                val result: String? = getJsonDataFromServer(
                                    String.format(
                                        MusicURL.SEARCH_MAIN_URL,
                                        SEARCH_TYPE_SONG,
                                        SEARCH_LIMIT,
                                        word
                                    )
                                )
                                mSearchSongBeanList = gson.fromJson<SearchSongBean?>(
                                    result,
                                    SearchSongBean::class.java
                                ).getResult().getSongs()
                            } catch (e: Exception) {
                                Log.e(TAG, e.toString())
                            } finally {
                                countDownLatch.countDown()
                            }
                        }
                    })

                    MusicThreadPool.postRunnable(object : Runnable {
                        override fun run() {
                            try {
                                val result: String? = getJsonDataFromServer(
                                    String.format(
                                        MusicURL.SEARCH_MAIN_URL,
                                        SEARCH_TYPE_ALBUM,
                                        SEARCH_LIMIT,
                                        word
                                    )
                                )
                                mSearchAlbumBeanList = gson.fromJson<SearchAlbumBean?>(
                                    result,
                                    SearchAlbumBean::class.java
                                ).getResult().getAlbums()
                            } catch (e: Exception) {
                                Log.e(TAG, e.toString())
                            } finally {
                                countDownLatch.countDown()
                            }
                        }
                    })

                    MusicThreadPool.postRunnable(object : Runnable {
                        override fun run() {
                            try {
                                val result: String? = getJsonDataFromServer(
                                    String.format(
                                        MusicURL.SEARCH_MAIN_URL,
                                        SEARCH_TYPE_SINGER,
                                        SEARCH_LIMIT,
                                        word
                                    )
                                )
                                mSearchArtistBeanList = gson.fromJson<SearchArtistsBean?>(
                                    result,
                                    SearchArtistsBean::class.java
                                ).getResult().getArtists()
                            } catch (e: Exception) {
                                Log.e(TAG, e.toString())
                            } finally {
                                countDownLatch.countDown()
                            }
                        }
                    })

                    MusicThreadPool.postRunnable(object : Runnable {
                        override fun run() {
                            try {
                                val result: String? = getJsonDataFromServer(
                                    String.format(
                                        MusicURL.SEARCH_MAIN_URL,
                                        SEARCH_TYPE_SONGLIST,
                                        SEARCH_LIMIT,
                                        word
                                    )
                                )
                                mSearchPlayListBeanList = gson.fromJson<SearchPlayListBean?>(
                                    result,
                                    SearchPlayListBean::class.java
                                ).getResult().getPlaylists()
                            } catch (e: Exception) {
                                Log.e(TAG, e.toString())
                            } finally {
                                countDownLatch.countDown()
                            }
                        }
                    })

                    MusicThreadPool.postRunnable(object : Runnable {
                        override fun run() {
                            try {
                                val result: String? = getJsonDataFromServer(
                                    String.format(
                                        MusicURL.SEARCH_MAIN_URL,
                                        SEARCH_TYPE_MV,
                                        SEARCH_LIMIT,
                                        word
                                    )
                                )
                                mSearchMvBeanList =
                                    gson.fromJson<SearchMvBean?>(result, SearchMvBean::class.java)
                                        .getResult().getMvs()
                            } catch (e: Exception) {
                                Log.e(TAG, e.toString())
                            } finally {
                                countDownLatch.countDown()
                            }
                        }
                    })
                    countDownLatch.await()

                    temp.addAll(mSearchSongBeanList!!)
                    indexs.add(mSearchSongBeanList!!.size)

                    temp.addAll(mSearchPlayListBeanList!!)
                    indexs.add(mSearchPlayListBeanList!!.size)

                    temp.addAll(mSearchArtistBeanList!!)
                    indexs.add(mSearchArtistBeanList!!.size)

                    temp.addAll(mSearchAlbumBeanList!!)
                    indexs.add(mSearchAlbumBeanList!!.size)

                    temp.addAll(mSearchMvBeanList!!)
                    indexs.add(mSearchMvBeanList!!.size)

                    if (temp.size != 0) {
                        Log.e(TAG, "Result size:" + temp.size)
                        mHandler.post(object : Runnable {
                            override fun run() {
                                mSearchWaitView!!.setVisibility(View.GONE)
                                mSearchResultAdapter!!.updateDataSet(temp, indexs)
                                mSearchResultRecycleView!!.addItemDecoration(
                                    GroupItemDecoration(
                                        getApplicationContext(),
                                        object : TitleDecorationCallback {
                                            override fun getGroupId(position: Int): Long {
                                                if (indexs.size == 0) {
                                                    return -1
                                                }
                                                if (position >= 0 && position < indexs.get(0)!!) {
                                                    return SearchResultAdapter.Companion.TYPE_SONG.toLong()
                                                } else if (position < indexs.get(0)!! + indexs.get(1)!!) {
                                                    return SearchResultAdapter.Companion.TYPE_PLAY_LIST.toLong()
                                                } else if (position < indexs.get(0)!! + indexs.get(1)!! + indexs.get(
                                                        2
                                                    )!!
                                                ) {
                                                    return SearchResultAdapter.Companion.TYPE_SINGER.toLong()
                                                } else if (position < indexs.get(0)!! + indexs.get(1)!! + indexs.get(
                                                        2
                                                    )!! + indexs.get(3)!!
                                                ) {
                                                    return SearchResultAdapter.Companion.TYPE_ALBUM.toLong()
                                                } else if (position < indexs.get(0)!! + indexs.get(1)!! + indexs.get(
                                                        2
                                                    )!! + indexs.get(3)!! + indexs.get(4)!!
                                                ) {
                                                    return SearchResultAdapter.Companion.TYPE_MV.toLong()
                                                } else {
                                                    return -1
                                                }
                                            }

                                            override fun getGroupName(position: Int): String {
                                                if (indexs.size == 0) {
                                                    return " 歌曲"
                                                }
                                                if (position >= 0 && position < indexs.get(0)!!) {
                                                    return " 歌曲"
                                                } else if (position < indexs.get(0)!! + indexs.get(1)!!) {
                                                    return "歌单"
                                                } else if (position < indexs.get(0)!! + indexs.get(1)!! + indexs.get(
                                                        2
                                                    )!!
                                                ) {
                                                    return " 歌手"
                                                } else if (position < indexs.get(0)!! + indexs.get(1)!! + indexs.get(
                                                        2
                                                    )!! + indexs.get(3)!!
                                                ) {
                                                    return "专辑"
                                                } else if (position < indexs.get(0)!! + indexs.get(1)!! + indexs.get(
                                                        2
                                                    )!! + indexs.get(3)!! + indexs.get(4)!!
                                                ) {
                                                    return " 视频"
                                                } else {
                                                    return " 歌曲"
                                                }
                                            }
                                        })
                                )
                            }
                        })
                    } else {
                        onRequestDataError()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error while get data from Server " + e.toString())
                    onRequestDataError()
                }
            }
        })
    }

    private fun onRequestDataError() {
        mHandler.post(object : Runnable {
            override fun run() {
                mSearchWaitView!!.findViewById<View?>(R.id.search_online_wait_hint).setVisibility(
                    View.GONE
                )
                mSearchWaitView!!.findViewById<View?>(R.id.search_online_wait_error).setVisibility(
                    View.VISIBLE
                )
            }
        })
    }

    private fun initSearchHistory() {
        mHistoryView = findViewById<LinearLayout>(R.id.search_history_layout)
        mClearHistory = findViewById<ImageView>(R.id.search_history_clear_icon)
        mHistoryListView = findViewById<RecyclerView>(R.id.search_history_list_record)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL)
        mHistoryListView!!.setLayoutManager(layoutManager)

        mHistoryRecyclerAdapter = SearchHistoryRecyclerAdapter(
            this,
            R.layout.search_history_text_item,
            mSearchHistoryLists
        )
        mHistoryListView!!.setAdapter(mHistoryRecyclerAdapter)
        if (mSearchHistoryLists.size == 0) {
            mHistoryView!!.setVisibility(View.GONE)
        }
        /*
         *通过回调接口触发点击事件
         */
        mHistoryRecyclerAdapter!!.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (!CheckTools.isNetWordAvailable(this@SearchMainActivity)) {
                    Toast.makeText(
                        this@SearchMainActivity,
                        getString(R.string.network_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (mSearchView != null) {
                    mSearchView!!.setQuery(mSearchHistoryLists.get(position), true)
                }
            }
        })

        mClearHistory!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Toast.makeText(v.getContext(), R.string.search_history_clear, Toast.LENGTH_SHORT)
                    .show()
                clearSearchHistory()
            }
        })

        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                this.searchHistory
            }
        })
    }

    private fun initRecommendList() {
        mTopView = findViewById<LinearLayout?>(R.id.search_top_layout)
        mHotSearchListView = findViewById<ListView>(R.id.search_top_list_view)
        mSearchHotAdapter =
            SearchHotAdapter(this, R.layout.search_hot_search_item_list, mHotSearchLists)
        mHotSearchListView!!.setAdapter(mSearchHotAdapter)
        mHotSearchListView!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (mSearchView != null) {
                    mSearchView!!.setQuery(mHotSearchLists.get(position)!!.getSearchWord(), true)
                }
            }
        })
    }

    @MainThread
    private fun initHotSearchData() {
        if (mHotSearchLists.size != 0) {
            return
        }
        if (!CheckTools.isNetWordAvailable(this)) {
            this.hotSearchCache
            return
        }
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val timeStamp = mSharedPreferences!!.getLong(TOP_SEARCH_DATA_CACHE_TIME_STAMP, 0L)
                if (timeStamp + MAX_UPDATE_TIME < System.currentTimeMillis()) {
                    var result: MutableList<HotSearchBean.Data?> = ArrayList<HotSearchBean.Data?>()
                    try {
                        val jsonData: String? = getJsonDataFromServer(MusicURL.SEARCH_HOT)
                        if (jsonData == null) {
                            return
                        }
                        result =
                            Gson().fromJson<HotSearchBean?>(jsonData, HotSearchBean::class.java)
                                .getData()
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    }
                    if (result.size == 0) {
                        return
                    }
                    updateHotSearchView(result)
                    storeHotSearchDataCache(result)
                } else {
                    this.hotSearchCache
                }
            }
        })
    }

    @UiThread
    private fun updateHotSearchView(result: MutableList<HotSearchBean.Data?>?) {
        if (result == null || result.size == 0) {
            return
        }
        runOnUiThread(object : Runnable {
            override fun run() {
                mSearchHotAdapter!!.clear()
                mSearchHotAdapter!!.addAll(result)
                mSearchHotAdapter!!.notifyDataSetChanged()
                if (mSearchView != null) {
                    mSearchView!!.setQueryHint(
                        result.get(
                            (Random(System.currentTimeMillis())).nextInt(
                                result.size - 1
                            )
                        )!!.getSearchWord()
                    )
                }
            }
        })
    }

    private fun storeHotSearchDataCache(result: MutableList<HotSearchBean.Data?>?) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val editor = mSharedPreferences!!.edit()
                val gson = Gson()
                editor.putString(TOP_SEARCH_DATA_CACHE, gson.toJson(result))
                editor.putLong(TOP_SEARCH_DATA_CACHE_TIME_STAMP, System.currentTimeMillis())
                editor.apply()
            }
        })
    }

    private val hotSearchCache: Unit
        get() {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    val jsonData = mSharedPreferences!!.getString(
                        TOP_SEARCH_DATA_CACHE,
                        ""
                    )
                    if ("" == jsonData) {
                        return
                    }
                    val gson = Gson()
                    val temp =
                        gson.fromJson<MutableList<HotSearchBean.Data?>?>(
                            jsonData,
                            object :
                                TypeToken<MutableList<HotSearchBean.Data?>?>() {
                            }.getType()
                        )
                    updateHotSearchView(temp)
                }
            })
        }


    @MainThread
    private fun getSearchSuggest(keyword: String?) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val jsonData: String? = getJsonDataFromServer(MusicURL.SEARCH_SUGGESTION + keyword)
                if (jsonData == null) {
                    return
                }
                try {
                    val list = Gson().fromJson<SearchSuggestionBean?>(
                        jsonData,
                        SearchSuggestionBean::class.java
                    ).getResult().getAllMatch()
                    if (list != null) {
                        val cursor = MatrixCursor(arrayOf<String>("_id", "keyword"))
                        for (i in list.indices) {
                            cursor.addRow(
                                arrayOf<String?>(
                                    i.toString(),
                                    list.get(i)!!.getKeyword()
                                )
                            )
                        }
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                mSearchHotAdapter!!.notifyDataSetChanged()
                                mSearchView!!.getSuggestionsAdapter().changeCursor(cursor)
                            }
                        })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error while get Suggestion" + e.toString())
                } finally {
                    //
                }
            }
        })
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.music_search_menu, menu)
        mSearchView = menu.findItem(R.id.music_search_view_icon).getActionView() as SearchView?
        val text_id = mSearchView!!.getContext().getResources()
            .getIdentifier("android:id/search_src_text", null, null)
        mSearchAutoComplete = mSearchView!!.findViewById<View?>(text_id) as SearchAutoComplete?
        if (mSearchAutoComplete != null) {
            mSearchAutoComplete!!.setThreshold(1)
        }

        mSearchHintAdapter = SimpleCursorAdapter(
            this@SearchMainActivity,
            R.layout.search_hint_keyword_item,
            MatrixCursor(arrayOf<String>("_id", "keyword")),
            arrayOf<String>("keyword"),
            intArrayOf(R.id.search_keyword_hint_item),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        mSearchView!!.setSuggestionsAdapter(mSearchHintAdapter)

        //      设置搜索框直接展开显示。左侧有放大镜(在搜索框中) 右侧有叉叉 可以关闭搜索框
//      mSearchView.setIconified(false);

//      设置搜索框直接展开显示。左侧有放大镜(在搜索框外) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
//      mSearchView.setIconifiedByDefault(false);

//      设置搜索框直接展开显示。左侧有无放大镜(在搜索框中) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
        mSearchView!!.onActionViewExpanded()

        if (mHotSearchLists.size != 0) {
            mSearchView!!.setQueryHint(
                mHotSearchLists.get(
                    (Random(System.currentTimeMillis())).nextInt(
                        mHotSearchLists.size - 1
                    )
                )!!.getSearchWord()
            )
        }

        mSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!CheckTools.isNetWordAvailable(this@SearchMainActivity)) {
                    Toast.makeText(
                        this@SearchMainActivity,
                        getString(R.string.network_available),
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
                if ("" == query) {
                    setContentView(R.layout.activity_search_main)
                    initRecommendList()
                    initHotSearchData()
                    initSearchHistory()
                    mSearchStatusFlag = false
                    return false
                }
                mSearchView!!.clearFocus()
                setContentView(R.layout.search_result_layout)
                initSearchFunction()
                if (mSearchResultAdapter != null) {
                    mSearchResultAdapter!!.clearAll()
                }
                if (mSearchStatusFlag) {
                    mSearchWaitView!!.setVisibility(View.VISIBLE)
                    getSearchData(query)
                    return true
                }
                getSearchData(query)
                mSearchStatusFlag = true
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if ("" == newText) {
                    setContentView(R.layout.activity_search_main)
                    initRecommendList()
                    initHotSearchData()
                    initSearchHistory()
                    mSearchStatusFlag = false
                    return false
                }
                getSearchSuggest(newText)
                return false
            }
        })
        mSearchView!!.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                if (mSearchAutoComplete != null) {
                    mSearchAutoComplete!!.setText(
                        mSearchView!!.getSuggestionsAdapter().getCursor().getString(1)
                    )
                }
                mSearchView!!.setQuery(
                    mSearchView!!.getSuggestionsAdapter().getCursor().getString(1), true
                )
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> finish()
            else -> {}
        }
        return true
    }

    override fun onBackPressed() {
        if (mSearchStatusFlag) {
            if (mSearchView != null) {
                mSearchView!!.setQuery("", true)
                initSearchHistory()
            }
        } else {
            super.onBackPressed()
        }
    }

    @WorkerThread
    private fun saveSearchHistory(word: String?) {
        if (word != null && (word != "") && !mSearchHistoryContainer.toString().contains(word)) {
            mSearchHistoryContainer.append(word).append("####")
            val sharedPreferences = getPreferences(MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_SEARCH_HISTORY_KEY, mSearchHistoryContainer.toString())
            editor.apply()
        }
    }

    @get:WorkerThread
    private val searchHistory: Boolean
        get() {
            val sharedPreferences =
                getPreferences(MODE_PRIVATE)
            mSearchHistoryContainer.delete(0, mSearchHistoryContainer.length)
            mSearchHistoryContainer.append(
                sharedPreferences.getString(
                    PREFERENCE_SEARCH_HISTORY_KEY,
                    ""
                )
            )
            if (mSearchHistoryContainer.toString() == "") {
                return false
            } else {
                val temp =
                    Arrays.asList<String?>(
                        *mSearchHistoryContainer.toString().split("####".toRegex())
                            .dropLastWhile { it.isEmpty() }.toTypedArray()
                    )
                runOnUiThread(object : Runnable {
                    override fun run() {
                        if (temp == null || temp.size == 0) {
                            mHistoryView!!.setVisibility(View.GONE)
                        } else {
                            mHistoryView!!.setVisibility(View.VISIBLE)
                            mHistoryRecyclerAdapter!!.updateSearchDataHistory(temp)
                        }
                    }
                })
                return true
            }
        }

    private fun clearSearchHistory() {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(PREFERENCE_SEARCH_HISTORY_KEY, "")
        editor.apply()
        mSearchHistoryContainer.delete(0, mSearchHistoryContainer.length)
        mHistoryRecyclerAdapter!!.clearSearchData()
        mHistoryView!!.setVisibility(View.GONE)
    }

    companion object {
        private const val TAG = "SearchMainActivity"

        private const val PREFERENCE_SEARCH_HISTORY_KEY = "search_history"
        private const val SEARCH_LIMIT = 8

        private const val SEARCH_TYPE_SONG = 1 //单曲
        private const val SEARCH_TYPE_ALBUM = 10 //专辑
        private const val SEARCH_TYPE_SINGER = 100 //歌手
        private const val SEARCH_TYPE_SONGLIST = 1000 //歌单
        private const val SEARCH_TYPE_MV = 1004 //MV

        private val mHandler = Handler(Looper.getMainLooper())

        private const val TOP_SEARCH_DATA_CACHE = "HotSearchBean"
        private const val TOP_SEARCH_DATA_CACHE_TIME_STAMP = "HotSearchBeanTimeStamp"
        private const val MAX_UPDATE_TIME: Long = 86400000

        @WorkerThread
        fun getJsonDataFromServer(urlPath: String?): String? {
            var result: String? = null
            var httpURLConnection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            var reader: BufferedReader? = null
            try {
                val url = URL(urlPath)
                httpURLConnection = url.openConnection() as HttpURLConnection?
                httpURLConnection!!.setRequestMethod("GET")
                httpURLConnection.setConnectTimeout(8000)
                httpURLConnection.setReadTimeout(8000)
                inputStream = httpURLConnection.getInputStream()
                reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while ((reader.readLine().also { line = it }) != null) {
                    stringBuilder.append(line)
                }
                result = stringBuilder.toString()
            } catch (e: Exception) {
                return null
            } finally {
                CloseResourceUtil.closeInputAndOutput(inputStream)
                CloseResourceUtil.closeReader(reader)
                CloseResourceUtil.disconnect(httpURLConnection)
            }
            return result
        }
    }
}


