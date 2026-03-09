package com.quibbler.sevenmusic.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.search.GroupItemDecoration;
import com.quibbler.sevenmusic.adapter.search.SearchHistoryRecyclerAdapter;
import com.quibbler.sevenmusic.adapter.search.SearchHotAdapter;
import com.quibbler.sevenmusic.adapter.search.SearchResultAdapter;
import com.quibbler.sevenmusic.bean.search.HotSearchBean;
import com.quibbler.sevenmusic.bean.search.SearchAlbumBean;
import com.quibbler.sevenmusic.bean.search.SearchArtistsBean;
import com.quibbler.sevenmusic.bean.search.SearchBean;
import com.quibbler.sevenmusic.bean.search.SearchMvBean;
import com.quibbler.sevenmusic.bean.search.SearchPlayListBean;
import com.quibbler.sevenmusic.bean.search.SearchSongBean;
import com.quibbler.sevenmusic.bean.search.SearchSuggestionBean;
import com.quibbler.sevenmusic.utils.CheckTools;
import com.quibbler.sevenmusic.utils.CloseResourceUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static com.quibbler.sevenmusic.bean.MusicURL.SEARCH_HOT;
import static com.quibbler.sevenmusic.bean.MusicURL.SEARCH_MAIN_URL;
import static com.quibbler.sevenmusic.bean.MusicURL.SEARCH_SUGGESTION;

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
public class SearchMainActivity extends AppCompatActivity {
    private static final String TAG = "SearchMainActivity";

    private static final String PREFERENCE_SEARCH_HISTORY_KEY = "search_history";
    private StringBuilder mSearchHistoryContainer = new StringBuilder();

    private static final int SEARCH_LIMIT = 8;

    private static final int SEARCH_TYPE_SONG = 1;                  //单曲
    private static final int SEARCH_TYPE_ALBUM = 10;                //专辑
    private static final int SEARCH_TYPE_SINGER = 100;              //歌手
    private static final int SEARCH_TYPE_SONGLIST = 1000;           //歌单
    private static final int SEARCH_TYPE_MV = 1004;                 //MV

    private List<SearchSongBean.Song> mSearchSongBeanList;
    private List<SearchAlbumBean.Album> mSearchAlbumBeanList;
    private List<SearchArtistsBean.Artist> mSearchArtistBeanList;
    private List<SearchPlayListBean.PlayList> mSearchPlayListBeanList;
    private List<SearchMvBean.Mv> mSearchMvBeanList;

    private View mSearchWaitView;
    private ImageView mSearchWaitGif;

    private androidx.appcompat.widget.SearchView mSearchView;
    private SimpleCursorAdapter mSearchHintAdapter = null;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private RecyclerView mSearchResultRecycleView;
    private SearchResultAdapter mSearchResultAdapter;
    private List<SearchBean> mSearchResultLists = new ArrayList<>();
    private boolean mSearchStatusFlag = false;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    private LinearLayout mHistoryView;
    private ImageView mClearHistory;
    private RecyclerView mHistoryListView;
    private List<String> mSearchHistoryLists = new ArrayList<>();
    private SearchHistoryRecyclerAdapter mHistoryRecyclerAdapter;

    private LinearLayout mTopView;
    private ListView mHotSearchListView;
    private List<HotSearchBean.Data> mHotSearchLists = new ArrayList<>();
    private SearchHotAdapter mSearchHotAdapter;
    private SharedPreferences mSharedPreferences;

    private static final String TOP_SEARCH_DATA_CACHE = "HotSearchBean";
    private static final String TOP_SEARCH_DATA_CACHE_TIME_STAMP = "HotSearchBeanTimeStamp";
    private static final long MAX_UPDATE_TIME = 86400000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_search_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mSharedPreferences = getPreferences(MODE_PRIVATE);

        initRecommendList();

        initHotSearchData();

        initSearchHistory();
    }

    private void initSearchFunction() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mSearchWaitView = findViewById(R.id.search_online_wait_layout);
        mSearchWaitGif = findViewById(R.id.search_online_wait_hint);
        Glide.with(this).load(R.drawable.search_online_wait_gif).into(mSearchWaitGif);

        mSearchResultRecycleView = findViewById(R.id.search_result_recycler_view);
        mSearchResultRecycleView.setLayoutManager(linearLayoutManager);


        mSearchResultAdapter = new SearchResultAdapter(this, mSearchResultLists);
        mSearchResultRecycleView.setAdapter(mSearchResultAdapter);
    }

    private void getSearchData(String word) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                saveSearchHistory(word);
                List<SearchBean> temp = new ArrayList<>();
                List<Integer> indexs = new ArrayList<>();
                Gson gson = new Gson();
                try {
                    final CountDownLatch countDownLatch = new CountDownLatch(5);
                    MusicThreadPool.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = getJsonDataFromServer(String.format(SEARCH_MAIN_URL, SEARCH_TYPE_SONG, SEARCH_LIMIT, word));
                                mSearchSongBeanList = gson.fromJson(result, SearchSongBean.class).getResult().getSongs();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    });

                    MusicThreadPool.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = getJsonDataFromServer(String.format(SEARCH_MAIN_URL, SEARCH_TYPE_ALBUM, SEARCH_LIMIT, word));
                                mSearchAlbumBeanList = gson.fromJson(result, SearchAlbumBean.class).getResult().getAlbums();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    });

                    MusicThreadPool.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = getJsonDataFromServer(String.format(SEARCH_MAIN_URL, SEARCH_TYPE_SINGER, SEARCH_LIMIT, word));
                                mSearchArtistBeanList = gson.fromJson(result, SearchArtistsBean.class).getResult().getArtists();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    });

                    MusicThreadPool.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = getJsonDataFromServer(String.format(SEARCH_MAIN_URL, SEARCH_TYPE_SONGLIST, SEARCH_LIMIT, word));
                                mSearchPlayListBeanList = gson.fromJson(result, SearchPlayListBean.class).getResult().getPlaylists();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    });

                    MusicThreadPool.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = getJsonDataFromServer(String.format(SEARCH_MAIN_URL, SEARCH_TYPE_MV, SEARCH_LIMIT, word));
                                mSearchMvBeanList = gson.fromJson(result, SearchMvBean.class).getResult().getMvs();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    });
                    countDownLatch.await();

                    temp.addAll(mSearchSongBeanList);
                    indexs.add(mSearchSongBeanList.size());

                    temp.addAll(mSearchPlayListBeanList);
                    indexs.add(mSearchPlayListBeanList.size());

                    temp.addAll(mSearchArtistBeanList);
                    indexs.add(mSearchArtistBeanList.size());

                    temp.addAll(mSearchAlbumBeanList);
                    indexs.add(mSearchAlbumBeanList.size());

                    temp.addAll(mSearchMvBeanList);
                    indexs.add(mSearchMvBeanList.size());

                    if (temp.size() != 0) {
                        Log.e(TAG, "Result size:" + temp.size());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mSearchWaitView.setVisibility(View.GONE);
                                mSearchResultAdapter.updateDataSet(temp, indexs);
                                mSearchResultRecycleView.addItemDecoration(new GroupItemDecoration(getApplicationContext(), new GroupItemDecoration.TitleDecorationCallback() {
                                    @Override
                                    public long getGroupId(int position) {
                                        if (indexs.size() == 0) {
                                            return -1;
                                        }
                                        if (position >= 0 && position < indexs.get(0)) {
                                            return SearchResultAdapter.TYPE_SONG;
                                        } else if (position < indexs.get(0) + indexs.get(1)) {
                                            return SearchResultAdapter.TYPE_PLAY_LIST;
                                        } else if (position < indexs.get(0) + indexs.get(1) + indexs.get(2)) {
                                            return SearchResultAdapter.TYPE_SINGER;
                                        } else if (position < indexs.get(0) + indexs.get(1) + indexs.get(2) + indexs.get(3)) {
                                            return SearchResultAdapter.TYPE_ALBUM;
                                        } else if (position < indexs.get(0) + indexs.get(1) + indexs.get(2) + indexs.get(3) + indexs.get(4)) {
                                            return SearchResultAdapter.TYPE_MV;
                                        } else {
                                            return -1;
                                        }
                                    }

                                    @Override
                                    public String getGroupName(int position) {
                                        if (indexs.size() == 0) {
                                            return " 歌曲";
                                        }
                                        if (position >= 0 && position < indexs.get(0)) {
                                            return " 歌曲";
                                        } else if (position < indexs.get(0) + indexs.get(1)) {
                                            return "歌单";
                                        } else if (position < indexs.get(0) + indexs.get(1) + indexs.get(2)) {
                                            return " 歌手";
                                        } else if (position < indexs.get(0) + indexs.get(1) + indexs.get(2) + indexs.get(3)) {
                                            return "专辑";
                                        } else if (position < indexs.get(0) + indexs.get(1) + indexs.get(2) + indexs.get(3) + indexs.get(4)) {
                                            return " 视频";
                                        } else {
                                            return " 歌曲";
                                        }
                                    }
                                }));
                            }
                        });
                    } else {
                        onRequestDataError();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error while get data from Server " + e.toString());
                    onRequestDataError();
                }
            }
        });
    }

    private void onRequestDataError() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSearchWaitView.findViewById(R.id.search_online_wait_hint).setVisibility(View.GONE);
                mSearchWaitView.findViewById(R.id.search_online_wait_error).setVisibility(View.VISIBLE);
            }
        });
    }

    private void initSearchHistory() {
        mHistoryView = findViewById(R.id.search_history_layout);
        mClearHistory = findViewById(R.id.search_history_clear_icon);
        mHistoryListView = findViewById(R.id.search_history_list_record);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mHistoryListView.setLayoutManager(layoutManager);

        mHistoryRecyclerAdapter = new SearchHistoryRecyclerAdapter(this, R.layout.search_history_text_item, mSearchHistoryLists);
        mHistoryListView.setAdapter(mHistoryRecyclerAdapter);
        if (mSearchHistoryLists.size() == 0) {
            mHistoryView.setVisibility(View.GONE);
        }
        /*
         *通过回调接口触发点击事件
         */
        mHistoryRecyclerAdapter.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!CheckTools.isNetWordAvailable(SearchMainActivity.this)) {
                    Toast.makeText(SearchMainActivity.this, getString(R.string.network_available), Toast.LENGTH_SHORT).show();
                }
                if (mSearchView != null) {
                    mSearchView.setQuery(mSearchHistoryLists.get(position), true);
                }
            }
        });

        mClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), R.string.search_history_clear, Toast.LENGTH_SHORT).show();
                clearSearchHistory();
            }
        });

        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                getSearchHistory();
            }
        });
    }

    private void initRecommendList() {
        mTopView = findViewById(R.id.search_top_layout);
        mHotSearchListView = findViewById(R.id.search_top_list_view);
        mSearchHotAdapter = new SearchHotAdapter(this, R.layout.search_hot_search_item_list, mHotSearchLists);
        mHotSearchListView.setAdapter(mSearchHotAdapter);
        mHotSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSearchView != null) {
                    mSearchView.setQuery(mHotSearchLists.get(position).getSearchWord(), true);
                }
            }
        });
    }

    @MainThread
    private void initHotSearchData() {
        if (mHotSearchLists.size() != 0) {
            return;
        }
        if (!CheckTools.isNetWordAvailable(this)) {
            getHotSearchCache();
            return;
        }
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                long timeStamp = mSharedPreferences.getLong(TOP_SEARCH_DATA_CACHE_TIME_STAMP, 0L);
                if (timeStamp + MAX_UPDATE_TIME < System.currentTimeMillis()) {
                    List<HotSearchBean.Data> result = new ArrayList<>();
                    try {
                        String jsonData = getJsonDataFromServer(SEARCH_HOT);
                        if (jsonData == null) {
                            return;
                        }
                        result = new Gson().fromJson(jsonData, HotSearchBean.class).getData();
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (result.size() == 0) {
                        return;
                    }
                    updateHotSearchView(result);
                    storeHotSearchDataCache(result);
                } else {
                    getHotSearchCache();
                }
            }
        });
    }

    @UiThread
    private void updateHotSearchView(List<HotSearchBean.Data> result) {
        if (result == null || result.size() == 0) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSearchHotAdapter.clear();
                mSearchHotAdapter.addAll(result);
                mSearchHotAdapter.notifyDataSetChanged();
                if (mSearchView != null) {
                    mSearchView.setQueryHint(result.get((new Random(System.currentTimeMillis())).nextInt(result.size() - 1)).getSearchWord());
                }
            }
        });
    }

    private void storeHotSearchDataCache(List<HotSearchBean.Data> result) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                Gson gson = new Gson();
                editor.putString(TOP_SEARCH_DATA_CACHE, gson.toJson(result));
                editor.putLong(TOP_SEARCH_DATA_CACHE_TIME_STAMP, System.currentTimeMillis());
                editor.apply();
            }
        });
    }

    private void getHotSearchCache() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                String jsonData = mSharedPreferences.getString(TOP_SEARCH_DATA_CACHE, "");
                if ("".equals(jsonData)) {
                    return;
                }
                Gson gson = new Gson();
                List<HotSearchBean.Data> temp = gson.fromJson(jsonData, new TypeToken<List<HotSearchBean.Data>>() {
                }.getType());
                updateHotSearchView(temp);
            }
        });
    }


    @MainThread
    private void getSearchSuggest(String keyword) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                String jsonData = getJsonDataFromServer(SEARCH_SUGGESTION + keyword);
                if (jsonData == null) {
                    return;
                }
                try {
                    List<SearchSuggestionBean.SearchSuggestion> list = new Gson().fromJson(jsonData, SearchSuggestionBean.class).getResult().getAllMatch();
                    if (list != null) {
                        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "keyword"});
                        for (int i = 0; i < list.size(); ++i) {
                            cursor.addRow(new String[]{Integer.toString(i), list.get(i).getKeyword()});
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSearchHotAdapter.notifyDataSetChanged();
                                mSearchView.getSuggestionsAdapter().changeCursor(cursor);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error while get Suggestion" + e.toString());
                } finally {
                    //
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_search_menu, menu);
        mSearchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.music_search_view_icon).getActionView();
        int text_id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        mSearchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(text_id);
        if (mSearchAutoComplete != null) {
            mSearchAutoComplete.setThreshold(1);
        }

        mSearchHintAdapter = new SimpleCursorAdapter(SearchMainActivity.this, R.layout.search_hint_keyword_item,
                new MatrixCursor(new String[]{"_id", "keyword"}), new String[]{"keyword"}, new int[]{R.id.search_keyword_hint_item}, FLAG_REGISTER_CONTENT_OBSERVER);
        mSearchView.setSuggestionsAdapter(mSearchHintAdapter);

//      设置搜索框直接展开显示。左侧有放大镜(在搜索框中) 右侧有叉叉 可以关闭搜索框
//      mSearchView.setIconified(false);

//      设置搜索框直接展开显示。左侧有放大镜(在搜索框外) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
//      mSearchView.setIconifiedByDefault(false);

//      设置搜索框直接展开显示。左侧有无放大镜(在搜索框中) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
        mSearchView.onActionViewExpanded();

        if (mHotSearchLists.size() != 0) {
            mSearchView.setQueryHint(mHotSearchLists.get((new Random(System.currentTimeMillis())).nextInt(mHotSearchLists.size() - 1)).getSearchWord());
        }

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!CheckTools.isNetWordAvailable(SearchMainActivity.this)) {
                    Toast.makeText(SearchMainActivity.this, getString(R.string.network_available), Toast.LENGTH_SHORT).show();
                    return false;
                }
                if ("".equals(query)) {
                    setContentView(R.layout.activity_search_main);
                    initRecommendList();
                    initHotSearchData();
                    initSearchHistory();
                    mSearchStatusFlag = false;
                    return false;
                }
                mSearchView.clearFocus();
                setContentView(R.layout.search_result_layout);
                initSearchFunction();
                if (mSearchResultAdapter != null) {
                    mSearchResultAdapter.clearAll();
                }
                if (mSearchStatusFlag) {
                    mSearchWaitView.setVisibility(View.VISIBLE);
                    getSearchData(query);
                    return true;
                }
                getSearchData(query);
                mSearchStatusFlag = true;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if ("".equals(newText)) {
                    setContentView(R.layout.activity_search_main);
                    initRecommendList();
                    initHotSearchData();
                    initSearchHistory();
                    mSearchStatusFlag = false;
                    return false;
                }
                getSearchSuggest(newText);
                return false;
            }
        });
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                if (mSearchAutoComplete != null) {
                    mSearchAutoComplete.setText(mSearchView.getSuggestionsAdapter().getCursor().getString(1));
                }
                mSearchView.setQuery(mSearchView.getSuggestionsAdapter().getCursor().getString(1), true);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mSearchStatusFlag) {
            if (mSearchView != null) {
                mSearchView.setQuery("", true);
                initSearchHistory();
            }
        } else {
            super.onBackPressed();
        }
    }

    @WorkerThread
    public static String getJsonDataFromServer(String urlPath) {
        String result = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlPath);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setReadTimeout(8000);
            inputStream = httpURLConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            result = stringBuilder.toString();
        } catch (Exception e) {
            return null;
        } finally {
            CloseResourceUtil.closeInputAndOutput(inputStream);
            CloseResourceUtil.closeReader(reader);
            CloseResourceUtil.disconnect(httpURLConnection);
        }
        return result;
    }

    @WorkerThread
    private void saveSearchHistory(String word) {
        if (word != null && !word.equals("") && !mSearchHistoryContainer.toString().contains(word)) {
            mSearchHistoryContainer.append(word).append("####");
            SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREFERENCE_SEARCH_HISTORY_KEY, mSearchHistoryContainer.toString());
            editor.apply();
        }
    }

    @WorkerThread
    private boolean getSearchHistory() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        mSearchHistoryContainer.delete(0, mSearchHistoryContainer.length());
        mSearchHistoryContainer.append(sharedPreferences.getString(PREFERENCE_SEARCH_HISTORY_KEY, ""));
        if (mSearchHistoryContainer.toString().equals("")) {
            return false;
        } else {
            List<String> temp = Arrays.asList(mSearchHistoryContainer.toString().split("####"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (temp == null || temp.size() == 0) {
                        mHistoryView.setVisibility(View.GONE);
                    } else {
                        mHistoryView.setVisibility(View.VISIBLE);
                        mHistoryRecyclerAdapter.updateSearchDataHistory(temp);
                    }
                }
            });
            return true;
        }
    }

    private void clearSearchHistory() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENCE_SEARCH_HISTORY_KEY, "");
        editor.apply();
        mSearchHistoryContainer.delete(0, mSearchHistoryContainer.length());
        mHistoryRecyclerAdapter.clearSearchData();
        mHistoryView.setVisibility(View.GONE);
    }
}


