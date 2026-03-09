package com.quibbler.sevenmusic.fragment.my;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.found.PlaylistActivity;
import com.quibbler.sevenmusic.activity.my.MyCollectionMVActivity;
import com.quibbler.sevenmusic.activity.my.MyCollectionsActivity;
import com.quibbler.sevenmusic.activity.my.MyDownloadMusicActivity;
import com.quibbler.sevenmusic.activity.my.MyFavouriteMusicActivity;
import com.quibbler.sevenmusic.activity.my.MyLocalMusicActivity;
import com.quibbler.sevenmusic.activity.my.MyRecentlyPlayedMusicActivity;
import com.quibbler.sevenmusic.activity.my.MySongListDetailActivity;
import com.quibbler.sevenmusic.activity.my.MySongListManagerActivity;
import com.quibbler.sevenmusic.adapter.my.MySongListAdapter;
import com.quibbler.sevenmusic.bean.MusicURL;
import com.quibbler.sevenmusic.bean.MyRecommendSongListJSonBean;
import com.quibbler.sevenmusic.bean.MySongListInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastReceiver;
import com.quibbler.sevenmusic.listener.BroadcastDatabaseListener;
import com.quibbler.sevenmusic.utils.CheckTools;
import com.quibbler.sevenmusic.utils.CloseResourceUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;
import com.quibbler.sevenmusic.view.MyListView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.SYSTEM_BROADCAST_NETWORK_CHANGE;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.COLLECTION_URL;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.DOWNLOAD_URL;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.FAVOURITE_URL;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.MV_URL;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.PLAYED_URL;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.SONGLIST_URL;


/**
 * Package:        com.quibbler.sevenmusic.fragment.my
 * ClassName:      MyFragment
 * Description:    我的页面
 * 20191009 界面优化;推荐歌单圆角显示，增加为6个推荐歌单;6大子界面图标美化;点击手势范围更改，方便点击进入子界面
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 23:50
 */
public class MyFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MyFragment";

    private static final int READ_EXTERNAL_STORAGE = 1;
    private static final int WRITE_EXTERNAL_STORAGE = 2;

    public static final int REQUEST_CODE_OPEN_LOCAL = 0;
    public static final int REQUEST_CODE_OPEN_DOWNLOAD = 1;
    public static final int REQUEST_CODE_OPEN_PLAYED = 2;
    public static final int REQUEST_CODE_OPEN_FAVOURITE = 3;
    public static final int REQUEST_CODE_OPEN_COLLECTION = 4;
    public static final int REQUEST_CODE_OPEN_BOUGHT = 5;

    public static final int RESULT_OK = 0;
    public static final int RESULT_GO_TO_FOUND = 1;

    public static final String TITLE_KEY = "title";
    public static final String CREATOR_KEY = "creator";
    public static final String TYPE_KEY = "type";

    private View view;

    private View mLocalMusicView;
    private View mDownloadMusicView;
    private View mRecentlyPlayedMusicView;
    private View mFavouriteMusicView;
    private View mCollectionMusicView;
    private View mMvMusicView;

    private TextView mLocalMusicNumber;
    private TextView mDownloadsMusicNumber;
    private TextView mRecentlyPlayedMusicNumber;
    private TextView mMyFavouritesNumber;
    private TextView mMyCollectionsNumber;
    private TextView mMvMusicNumber;

    private ImageView mMySongListShowButton;
    private TextView mMySongListShowTextView;
    private TextView mMySongListNumberTextView;
    private ImageView mBuildMySongListButton;
    private TextView mBuildMySongListTextView;
    private ImageView mEditMySongListButton;
    private TextView mEditMySongListText;
    private TextView mNoneSongListToShow;
    private MyListView mSongListView;

    private ImageView mCollectionListShowButton;
    private TextView mCollectionListShowTextView;
    private TextView mCollectionSongListNumberTextView;
    private ImageView mEditCollectionSongListButton;
    private TextView mEditMyCollectionListText;
    private TextView mNoneCollectionListToShow;
    private MyListView mCollectionListView;

    private List<MySongListInfo> mMySongLists = new ArrayList<>();
    private List<MySongListInfo> mCollectionLists = new ArrayList<>();

    private MySongListAdapter mSongListAdapter;
    private MySongListAdapter mCollectionAdapter;

    private List<MyRecommendSongListJSonBean.MyRecommendSongList> mRecommendSongLists = new ArrayList<>();
    private ImageView mRecommendSongListIconOne;
    private ImageView mRecommendSongListIconTwo;
    private ImageView mRecommendSongListIconThree;
    private ImageView mRecommendSongListIconFour;
    private ImageView mRecommendSongListIconFive;
    private ImageView mRecommendSongListIconSix;
    private TextView mRecommendSongListNameOne;
    private TextView mRecommendSongListNameTwo;
    private TextView mRecommendSongListNameThree;
    private TextView mRecommendSongListNameFour;
    private TextView mRecommendSongListNameFive;
    private TextView mRecommendSongListNameSix;
    private SharedPreferences mSharedPreferences;
    private static final String HAS_LOCAL_RECOMMEND_CACHE = "hasMyRecommendSongListJSonBean";
    private static final String LOCAL_RECOMMEND_CACHE = "MyRecommendSongListJSonBean";

    private AlertDialog.Builder builder;

    private NetWordStateChangeReceiver mNetWordStateChangeReceiver;

    private MusicBroadcastReceiver mReceiver;

    public MyFragment() {
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.my_fragment_layout, container, false);
        mSharedPreferences = MusicApplication.getContext().getSharedPreferences("cache_my_fragment", Context.MODE_PRIVATE);

        checkPermission();

        initBaseFunction();

        initSongListFunction();

        addListener();

        initNumber();

        initSongList();

        initRecommend();

        initBroadcastReceiver();

        return view;
    }

    public void checkPermission() {
        if (getActivity() != null) {
            if (MusicApplication.getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            }
            if (MusicApplication.getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void initBaseFunction() {
        mLocalMusicView = view.findViewById(R.id.my_fragment_local_music);
        mDownloadMusicView = view.findViewById(R.id.my_fragment_download_music);
        mRecentlyPlayedMusicView = view.findViewById(R.id.my_fragment_recently_played_music);
        mFavouriteMusicView = view.findViewById(R.id.my_fragment_favourite_music);
        mCollectionMusicView = view.findViewById(R.id.my_fragment_collection_music);
        mMvMusicView = view.findViewById(R.id.my_fragment_mv);

        mLocalMusicNumber = view.findViewById(R.id.my_local_music_count);
        mDownloadsMusicNumber = view.findViewById(R.id.my_download_music_count);
        mRecentlyPlayedMusicNumber = view.findViewById(R.id.my_recently_music_count);
        mMyFavouritesNumber = view.findViewById(R.id.my_favourite_music_count);
        mMyCollectionsNumber = view.findViewById(R.id.my_collection_music_count);
        mMvMusicNumber = view.findViewById(R.id.my_buy_music_count);

        mRecommendSongListIconOne = view.findViewById(R.id.my_recommend_song_list_icon_one);
        mRecommendSongListIconTwo = view.findViewById(R.id.my_recommend_song_list_icon_two);
        mRecommendSongListIconThree = view.findViewById(R.id.my_recommend_song_list_icon_three);
        mRecommendSongListIconFour = view.findViewById(R.id.my_recommend_song_list_icon_four);
        mRecommendSongListIconFive = view.findViewById(R.id.my_recommend_song_list_icon_five);
        mRecommendSongListIconSix = view.findViewById(R.id.my_recommend_song_list_icon_six);
        mRecommendSongListNameOne = view.findViewById(R.id.my_recommend_song_list_text_one);
        mRecommendSongListNameTwo = view.findViewById(R.id.my_recommend_song_list_text_two);
        mRecommendSongListNameThree = view.findViewById(R.id.my_recommend_song_list_text_three);
        mRecommendSongListNameFour = view.findViewById(R.id.my_recommend_song_list_text_four);
        mRecommendSongListNameFive = view.findViewById(R.id.my_recommend_song_list_text_five);
        mRecommendSongListNameSix = view.findViewById(R.id.my_recommend_song_list_text_six);
    }

    private void initSongListFunction() {
        mMySongListShowButton = view.findViewById(R.id.my_song_list_show_detail);
        mMySongListShowTextView = view.findViewById(R.id.my_song_list_self_text);
        mMySongListNumberTextView = view.findViewById(R.id.my_song_list_number);
        mBuildMySongListButton = view.findViewById(R.id.my_song_list_add);
        mBuildMySongListTextView = view.findViewById(R.id.my_song_lists_new);
        mEditMySongListButton = view.findViewById(R.id.my_song_list_edit_icon);
        mEditMySongListText = view.findViewById(R.id.my_song_lists_edit);

        mCollectionListShowButton = view.findViewById(R.id.my_collection_song_list_show_detail);
        mCollectionListShowTextView = view.findViewById(R.id.my_collection_song_list_self_text);
        mCollectionSongListNumberTextView = view.findViewById(R.id.my_collection_song_list_number);
        mEditCollectionSongListButton = view.findViewById(R.id.my_collection_song_list_edit_icon);
        mEditMyCollectionListText = view.findViewById(R.id.my_collection_song_lists_edit);

        mSongListAdapter = new MySongListAdapter(MusicApplication.getContext(), R.layout.my_song_list_item, mMySongLists);
        mCollectionAdapter = new MySongListAdapter(MusicApplication.getContext(), R.layout.my_song_list_item, mCollectionLists);

        mNoneSongListToShow = view.findViewById(R.id.my_song_list_none_text_view);
        mSongListView = view.findViewById(R.id.my_song_list_view);
        mSongListView.setDivider(null);
        mCollectionListView = view.findViewById(R.id.my_collection_song_list_view);
        mCollectionListView.setDivider(null);

        mNoneCollectionListToShow = view.findViewById(R.id.my_collection_list_none_text_view);
        mSongListView.setAdapter(mSongListAdapter);
        mCollectionListView.setAdapter(mCollectionAdapter);

        //注册ContextMenu
        registerForContextMenu(mSongListView);
        //注册点击事件
        mSongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), MySongListDetailActivity.class);
                intent.putExtra(TITLE_KEY, mMySongLists.get(position).getListName());
                intent.putExtra(CREATOR_KEY, mMySongLists.get(position).getCreator());
                startActivity(intent);
            }
        });
        mCollectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), PlaylistActivity.class);
                intent.putExtra(getString(R.string.playlist_id), mCollectionLists.get(position).getId());
                startActivity(intent);
            }
        });

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.my_song_list_none_text_view));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                createSongListDialog();
            }
        };
        spannableStringBuilder.setSpan(clickableSpan, 8, 12, SPAN_EXCLUSIVE_EXCLUSIVE);
        mNoneSongListToShow.setMovementMethod(LinkMovementMethod.getInstance());
        mNoneSongListToShow.setText(spannableStringBuilder);

        SpannableStringBuilder spannableStringBuilderCollection = new SpannableStringBuilder(getString(R.string.my_collection_list_none_text_view));
        ClickableSpan clickableSpanCollection = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                MusicBroadcastManager.sendBroadcast(MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO);
            }
        };
        spannableStringBuilderCollection.setSpan(clickableSpanCollection, 10, 14, SPAN_EXCLUSIVE_EXCLUSIVE);
        mNoneCollectionListToShow.setMovementMethod(LinkMovementMethod.getInstance());
        mNoneCollectionListToShow.setText(spannableStringBuilderCollection);
    }

    private void addListener() {
        mLocalMusicView.setOnClickListener(this);
        mDownloadMusicView.setOnClickListener(this);
        mRecentlyPlayedMusicView.setOnClickListener(this);
        mFavouriteMusicView.setOnClickListener(this);
        mCollectionMusicView.setOnClickListener(this);
        mMvMusicView.setOnClickListener(this);

        mMySongListShowButton.setOnClickListener(this);
        mMySongListShowTextView.setOnClickListener(this);
        mBuildMySongListButton.setOnClickListener(this);
        mBuildMySongListTextView.setOnClickListener(this);
        mEditMySongListButton.setOnClickListener(this);
        mEditMySongListText.setOnClickListener(this);
        mCollectionListShowButton.setOnClickListener(this);
        mCollectionListShowTextView.setOnClickListener(this);
        mEditCollectionSongListButton.setOnClickListener(this);
        mEditMyCollectionListText.setOnClickListener(this);

        mRecommendSongListIconOne.setOnClickListener(this);
        mRecommendSongListIconTwo.setOnClickListener(this);
        mRecommendSongListIconThree.setOnClickListener(this);
        mRecommendSongListIconFour.setOnClickListener(this);
        mRecommendSongListIconFive.setOnClickListener(this);
        mRecommendSongListIconSix.setOnClickListener(this);
    }

    public void initBroadcastReceiver() {
        //数据库广播
        mReceiver = new MusicBroadcastReceiver(new BroadcastDatabaseListener() {
            @Override
            public void onDatabaseChanged() {
                initNumber();
                initSongList();
            }
        });
        MusicBroadcastManager.registerMusicBroadcastReceiver(mReceiver, MUSIC_GLOBAL_DATABASE_UPDATE);
        //网络状态广播
        mNetWordStateChangeReceiver = new NetWordStateChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SYSTEM_BROADCAST_NETWORK_CHANGE);
        if (getActivity() != null) {
            getActivity().registerReceiver(mNetWordStateChangeReceiver, intentFilter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(mSongListView);
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mReceiver);
        if (getActivity() != null) {
            getActivity().unregisterReceiver(mNetWordStateChangeReceiver);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        initNumber();
        initSongList();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 查询歌曲数量
     * 显示在各个分类的下方
     */
    @UiThread
    private void initNumber() {
        if (CheckTools.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MusicApplication.getContext())) {
            MusicThreadPool.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        ContentResolver localMusicResolver = MusicApplication.getContext().getContentResolver();
                        Cursor localMusicCursor = localMusicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                        int localMusicNumber = localMusicCursor.getCount();
                        CloseResourceUtil.closeInputAndOutput(localMusicCursor);

                        Cursor downloadMusicCursor = localMusicResolver.query(DOWNLOAD_URL, null, "is_download = ?", new String[]{"0"}, null);
                        int downloadMusicNumber = downloadMusicCursor.getCount();
                        CloseResourceUtil.closeInputAndOutput(downloadMusicCursor);

                        Cursor recentlyPlayedMusicCursor = localMusicResolver.query(PLAYED_URL, null, null, null, null);
                        int recentlyNumber = recentlyPlayedMusicCursor.getCount();
                        CloseResourceUtil.closeInputAndOutput(recentlyPlayedMusicCursor);

                        Cursor favouritePlayedMusicCursor = localMusicResolver.query(FAVOURITE_URL, null, null, null, null);
                        int favouriteNumber = favouritePlayedMusicCursor.getCount();
                        CloseResourceUtil.closeInputAndOutput(favouritePlayedMusicCursor);

                        Cursor collectionCursor = localMusicResolver.query(COLLECTION_URL, null, "kind = ?", new String[]{"0"}, null);
                        int collectionNumber = collectionCursor.getCount();
                        CloseResourceUtil.closeInputAndOutput(collectionCursor);

                        Cursor cmvCursor = localMusicResolver.query(MV_URL, null, null, null, null);
                        int mvNumber = cmvCursor.getCount();
                        CloseResourceUtil.closeInputAndOutput(cmvCursor);

                        updateSongNumber(localMusicNumber, downloadMusicNumber, recentlyNumber, favouriteNumber, collectionNumber, mvNumber);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
        } else {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), getString(R.string.my_local_music_scan_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @UiThread
    private void updateSongNumber(int... numbers) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLocalMusicNumber.setText(numbers[0] + "首");
                    mDownloadsMusicNumber.setText(numbers[1] + "首");
                    mRecentlyPlayedMusicNumber.setText(numbers[2] + "首");
                    mMyFavouritesNumber.setText(numbers[3] + "首");
                    mMyCollectionsNumber.setText(String.format("%d首", numbers[4]));
                    mMvMusicNumber.setText(String.format("%d部", numbers[5]));

                }
            });
        }
    }

    @WorkerThread
    public void initSongList() {
        MusicThreadPool.postRunnable(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                List<MySongListInfo> songLists = new ArrayList<>();
                List<MySongListInfo> collectionLists = new ArrayList<>();
                Cursor songListCursor = null;
                try {
                    songListCursor = MusicApplication.getContext().getContentResolver().query(SONGLIST_URL, null, null, null, null);
                    if (songListCursor != null) {
                        while (songListCursor.moveToNext()) {
                            MySongListInfo mySongListInfo = new MySongListInfo();
                            mySongListInfo.setListName(songListCursor.getString(songListCursor.getColumnIndex("name")));
                            mySongListInfo.setDescription(songListCursor.getString(songListCursor.getColumnIndex("description")));
                            mySongListInfo.setType(songListCursor.getInt(songListCursor.getColumnIndex("type")));
                            mySongListInfo.setNumber(songListCursor.getInt(songListCursor.getColumnIndex("number")));
                            mySongListInfo.setId(songListCursor.getString(songListCursor.getColumnIndex("id")));
                            mySongListInfo.setCreator(songListCursor.getString(songListCursor.getColumnIndex("creator")));
                            mySongListInfo.setSongsJsonData(songListCursor.getString(songListCursor.getColumnIndex("songs")));
                            mySongListInfo.setImageUrl(songListCursor.getString(songListCursor.getColumnIndex("coverimgurl")));
                            switch (mySongListInfo.getType()) {
                                case 0:
                                    songLists.add(mySongListInfo);
                                    break;
                                case 1:
                                    collectionLists.add(mySongListInfo);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (songListCursor != null) {
                        songListCursor.close();
                    }
                }
                updateCollectionSongListUI(songLists, collectionLists);
            }
        });
    }

    @MainThread
    private void updateCollectionSongListUI(List<MySongListInfo> songLists, List<MySongListInfo> collectionLists) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMySongLists.clear();
                    mMySongLists.addAll(songLists);
                    mSongListAdapter.notifyDataSetChanged();

                    mCollectionLists.clear();
                    mCollectionLists.addAll(collectionLists);
                    mCollectionAdapter.notifyDataSetChanged();

                    mMySongListNumberTextView.setText("(" + mMySongLists.size() + ")");
                    mCollectionSongListNumberTextView.setText("(" + mCollectionLists.size() + ")");

                    if (mMySongLists.size() != 0) {
                        mNoneSongListToShow.setVisibility(View.GONE);
                    } else {
                        mNoneSongListToShow.setVisibility(View.VISIBLE);
                    }

                    if (mCollectionLists.size() != 0) {
                        mNoneCollectionListToShow.setVisibility(View.GONE);
                    } else {
                        mNoneCollectionListToShow.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @WorkerThread
    public void initRecommend() {
        if (!CheckTools.isNetWordAvailable(getContext())) {
            loadRecommendDataFromCache();
            return;
        }
        loadRecommendDataFromCache();
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(MusicURL.API_HIGHQUALITY_SONGLIST);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);

                    InputStream inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Gson gson = new Gson();
                    mRecommendSongLists = gson.fromJson(response.toString(), MyRecommendSongListJSonBean.class).getPlaylists();
                    if (mRecommendSongLists != null && mRecommendSongLists.size() != 0) {
                        updateRecommendSongListOnUI(mRecommendSongLists);
                        storeRecommendDataCache();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }

    @UiThread
    private void updateRecommendSongListOnUI(List<MyRecommendSongListJSonBean.MyRecommendSongList> result) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result != null && result.size() != 0) {
                        for (int i = 0; i < result.size(); ++i) {
                            switch (i) {
                                case 0:
                                    mRecommendSongListNameOne.setText(result.get(i).getName());
                                    if (getContext() != null) {
                                        Glide.with(getContext()).load(result.get(i).getCoverImgUrl()).placeholder(R.drawable.my_wait_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(mRecommendSongListIconOne);
                                    }
                                    break;
                                case 1:
                                    mRecommendSongListNameTwo.setText(result.get(i).getName());
                                    if (getContext() != null) {
                                        Glide.with(getContext()).load(result.get(i).getCoverImgUrl()).placeholder(R.drawable.my_wait_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(mRecommendSongListIconTwo);
                                    }
                                    break;
                                case 2:
                                    mRecommendSongListNameThree.setText(result.get(i).getName());
                                    if (getContext() != null) {
                                        Glide.with(getContext()).load(result.get(i).getCoverImgUrl()).placeholder(R.drawable.my_wait_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(mRecommendSongListIconThree);
                                    }
                                    break;
                                case 3:
                                    mRecommendSongListNameFour.setText(result.get(i).getName());
                                    if (getContext() != null) {
                                        Glide.with(getContext()).load(result.get(i).getCoverImgUrl()).placeholder(R.drawable.my_wait_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(mRecommendSongListIconFour);
                                    }
//                                自写加载图片工具类
//                                DecodeMusicImageUtils.with(getContext()).placeholder(R.drawable.my_song_list_item_icon).load(mRecommendSongLists.get(i).getCoverImgUrl()).radiusType(DecodeMusicManager.CYCLE).into(mRecommendSongListIconFour);
                                    break;
                                case 4:
                                    mRecommendSongListNameFive.setText(result.get(i).getName());
                                    if (getContext() != null) {
                                        Glide.with(getContext()).load(result.get(i).getCoverImgUrl()).placeholder(R.drawable.my_wait_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(mRecommendSongListIconFive);
                                    }
                                    break;
                                case 5:
                                    mRecommendSongListNameSix.setText(result.get(i).getName());
                                    if (getContext() != null) {
                                        Glide.with(getContext()).load(result.get(i).getCoverImgUrl()).placeholder(R.drawable.my_wait_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(mRecommendSongListIconSix);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d(TAG, "on click id = " + id);
        if (id == R.id.my_fragment_local_music) {
            Intent openLocalMusicIntent = new Intent(v.getContext(), MyLocalMusicActivity.class);
            startActivityForResult(openLocalMusicIntent, REQUEST_CODE_OPEN_LOCAL);
        } else if (id == R.id.my_fragment_download_music) {
            Intent openDownloadMusicIntent = new Intent(v.getContext(), MyDownloadMusicActivity.class);
            startActivityForResult(openDownloadMusicIntent, REQUEST_CODE_OPEN_DOWNLOAD);
        } else if (id == R.id.my_fragment_recently_played_music) {
            Intent openPlayedIntent = new Intent(v.getContext(), MyRecentlyPlayedMusicActivity.class);
            startActivityForResult(openPlayedIntent, REQUEST_CODE_OPEN_PLAYED);
        } else if (id == R.id.my_fragment_favourite_music) {
            Intent openFavouriteIntent = new Intent(v.getContext(), MyFavouriteMusicActivity.class);
            startActivityForResult(openFavouriteIntent, REQUEST_CODE_OPEN_FAVOURITE);
        } else if (id == R.id.my_fragment_collection_music) {
            Intent openCollectionIntent = new Intent(v.getContext(), MyCollectionsActivity.class);
            startActivityForResult(openCollectionIntent, REQUEST_CODE_OPEN_COLLECTION);
        } else if (id == R.id.my_fragment_mv) {
            Intent openBoughtMusicIntent = new Intent(v.getContext(), MyCollectionMVActivity.class);
            startActivityForResult(openBoughtMusicIntent, REQUEST_CODE_OPEN_BOUGHT);
        } else if (id == R.id.my_song_list_show_detail || id == R.id.my_song_list_self_text) {
            if (mMySongLists.size() == 0) {
                if (mNoneSongListToShow.getVisibility() == View.VISIBLE) {
                    mMySongListShowButton.animate().rotation(-180);
                    mNoneSongListToShow.setVisibility(View.GONE);
                } else {
                    mMySongListShowButton.animate().rotation(0);
                    mNoneSongListToShow.setVisibility(View.VISIBLE);
                }
                return;
            }

            if (mSongListView.getVisibility() == View.GONE) {
                mSongListView.setVisibility(View.VISIBLE);
                mMySongListShowButton.animate().rotation(0);
            } else {
                mMySongListShowButton.animate().rotation(-180);
                mSongListView.setVisibility(View.GONE);
            }
        } else if (id == R.id.my_song_lists_new || id == R.id.my_song_list_add) {
            createSongListDialog();
        } else if (id == R.id.my_song_list_edit_icon || id == R.id.my_song_lists_edit) {
            Intent editMySongListIntent = new Intent(MusicApplication.getContext(), MySongListManagerActivity.class);
            editMySongListIntent.putExtra(TITLE_KEY, "我的歌单");
//                editMySongListIntent.putExtra("data", (Serializable) mMySongLists);
            editMySongListIntent.putExtra(TYPE_KEY, 0);
            startActivity(editMySongListIntent);
        } else if (id == R.id.my_collection_song_list_show_detail || id == R.id.my_collection_song_list_self_text) {
            if (mCollectionLists.size() == 0) {
                if (mNoneCollectionListToShow.getVisibility() == View.VISIBLE) {
                    mCollectionListShowButton.animate().rotation(-180);
                    mNoneCollectionListToShow.setVisibility(View.GONE);
                } else {
                    mCollectionListShowButton.animate().rotation(0);
                    mNoneCollectionListToShow.setVisibility(View.VISIBLE);
                }
                return;
            }

            if (mCollectionListView.getVisibility() == View.GONE) {
                mCollectionListView.setVisibility(View.VISIBLE);
                mCollectionListShowButton.animate().rotation(0);
            } else {
                mCollectionListShowButton.animate().rotation(-180);
                mCollectionListView.setVisibility(View.GONE);
            }
        } else if (id == R.id.my_collection_song_list_edit_icon || id == R.id.my_collection_song_lists_edit) {
            Intent intentCollectionListManagerActivity = new Intent(MusicApplication.getContext(), MySongListManagerActivity.class);
            intentCollectionListManagerActivity.putExtra(TITLE_KEY, "收藏歌单");
//                intentCollectionListManagerActivity.putExtra("data", (Serializable) mCollectionLists);
            intentCollectionListManagerActivity.putExtra(TYPE_KEY, 1);
            startActivity(intentCollectionListManagerActivity);
        } else if (id == R.id.my_recommend_song_list_icon_one) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.my_network_un_avaiable), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent1 = new Intent(MusicApplication.getContext(), PlaylistActivity.class);
            intent1.putExtra(getContext().getString(R.string.playlist_id), mRecommendSongLists.get(0).getId());
            startActivity(intent1);
        } else if (id == R.id.my_recommend_song_list_icon_two) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.my_network_un_avaiable), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent2 = new Intent(MusicApplication.getContext(), PlaylistActivity.class);
            intent2.putExtra(getContext().getString(R.string.playlist_id), mRecommendSongLists.get(1).getId());
            startActivity(intent2);
        } else if (id == R.id.my_recommend_song_list_icon_three) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.my_network_un_avaiable), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent3 = new Intent(MusicApplication.getContext(), PlaylistActivity.class);
            intent3.putExtra(getContext().getString(R.string.playlist_id), mRecommendSongLists.get(2).getId());
            startActivity(intent3);
        } else if (id == R.id.my_recommend_song_list_icon_four) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.my_network_un_avaiable), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent4 = new Intent(MusicApplication.getContext(), PlaylistActivity.class);
            intent4.putExtra(getContext().getString(R.string.playlist_id), mRecommendSongLists.get(3).getId());
            startActivity(intent4);
        } else if (id == R.id.my_recommend_song_list_icon_five) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.my_network_un_avaiable), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent5 = new Intent(MusicApplication.getContext(), PlaylistActivity.class);
            intent5.putExtra(getContext().getString(R.string.playlist_id), mRecommendSongLists.get(4).getId());
            startActivity(intent5);
        } else if (id == R.id.my_recommend_song_list_icon_six) {
            if (!CheckTools.isNetWordAvailable(getContext())) {
                Toast.makeText(getContext(), getString(R.string.my_network_un_avaiable), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent6 = new Intent(MusicApplication.getContext(), PlaylistActivity.class);
            intent6.putExtra(getContext().getString(R.string.playlist_id), mRecommendSongLists.get(5).getId());
            startActivity(intent6);
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        if (getActivity() != null) {
            getActivity().getMenuInflater().inflate(R.menu.my_song_list_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = (int) mSongListView.getAdapter().getItemId(menuInfo.position);
        if (mMySongLists.size() == 0) {
            return false;
        }
        if (item.getItemId() == R.id.my_song_list_context_menu_delete) {
            MusicApplication.getContext().getContentResolver().delete(SONGLIST_URL, "name = ?", new String[]{mMySongLists.get(pos).getListName()});
            mMySongLists.remove(pos);
            mSongListAdapter.notifyDataSetChanged();
            mMySongListNumberTextView.setText("(" + mMySongLists.size() + ")");
            if (mMySongLists.size() == 0) {
                mNoneSongListToShow.setVisibility(View.VISIBLE);
            }
        } else if (item.getItemId() == R.id.my_song_list_context_menu_edit) {
            Intent intent = new Intent(getContext(), MySongListDetailActivity.class);
            intent.putExtra(CREATOR_KEY, mMySongLists.get(pos).getCreator());
            intent.putExtra(TITLE_KEY, mMySongLists.get(pos).getListName());
            startActivity(intent);
        } else if (item.getItemId() == R.id.my_song_list_context_menu_add_to_play) {
            //
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case READ_EXTERNAL_STORAGE:
                    initNumber();
                    initSongList();
                    break;
                default:
                    break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Deprecated
    public void showCreateSongListPopupWindow(View v) {
        View view = View.inflate(getContext(), R.layout.my_create_song_list_pop_menu, null);
        PopupWindow createSongListPopupWindow = new PopupWindow(view, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.
                LayoutParams.WRAP_CONTENT, true);
        createSongListPopupWindow.setOutsideTouchable(true);
        createSongListPopupWindow.setAnimationStyle(R.style.my_create_song_list_dialog_animation);

        EditText editText = view.findViewById(R.id.my_create_text_list_name);
        editText.setText("我的歌单 " + (mMySongLists.size() + 1));

        Button cancel = view.findViewById(R.id.my_create_dialog_cancel);
        Button create = view.findViewById(R.id.my_create_dialog_confirm);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSongListPopupWindow.dismiss();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSongListPopupWindow.dismiss();
                MySongListInfo mySongListInfo = new MySongListInfo();
                mySongListInfo.setListName(editText.getText().toString());
                mMySongLists.add(mySongListInfo);
                mSongListAdapter.notifyDataSetChanged();
                mMySongListNumberTextView.setText("(" + mMySongLists.size() + ")");
                ContentValues values = new ContentValues();
                values.put("name", mySongListInfo.getListName());
                values.put("type", 0);
                values.put("songs", "");
                values.put("number", 0);
                values.put("id", -1);
                MusicApplication.getContext().getContentResolver().insert(SONGLIST_URL, values);
                mNoneSongListToShow.setVisibility(View.GONE);
            }
        });
        createSongListPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
    }

    /*
    创建歌单Dialog
     */
    private void createSongListDialog() {
        EditText editText = new EditText(getContext());
        editText.setSingleLine();
        editText.setText(getString(R.string.my_song_list_create_name_list) + (mMySongLists.size() + 1));

        builder = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.my_song_lists_new_song_list)).setView(editText)
                .setPositiveButton(getString(R.string.my_song_lists_new), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Cursor cursor = null;
                        cursor = MusicApplication.getContext().getContentResolver().query(SONGLIST_URL, null, "name = ?", new String[]{editText.getText().toString()}, null);
                        if (cursor != null && cursor.getCount() != 0) {
                            Toast.makeText(getContext(), getString(R.string.my_song_lists_new_conflict), Toast.LENGTH_SHORT).show();
                            cursor.close();
                            return;
                        }
                        MySongListInfo mySongListInfo = new MySongListInfo();
                        mySongListInfo.setListName(editText.getText().toString());
                        mySongListInfo.setCreator(Long.toString(System.currentTimeMillis()));
                        mMySongLists.add(mySongListInfo);
                        mSongListAdapter.notifyDataSetChanged();
                        mMySongListNumberTextView.setText("(" + mMySongLists.size() + ")");
                        mNoneSongListToShow.setVisibility(View.GONE);
                        MusicThreadPool.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                ContentValues values = new ContentValues();
                                values.put("name", mySongListInfo.getListName());
                                values.put("type", 0);
                                values.put("songs", "");
                                values.put("number", 0);
                                values.put("id", -1);
                                values.put(CREATOR_KEY, mySongListInfo.getCreator());
                                MusicApplication.getContext().getContentResolver().insert(SONGLIST_URL, values);
                            }
                        });
                    }
                }).setNegativeButton(getString(R.string.my_song_lists_new_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        editText.requestFocus();
        alertDialog.show();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /*
       处理各个Activity返回结果,跳转请求
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPEN_PLAYED:
            case REQUEST_CODE_OPEN_FAVOURITE:
            case REQUEST_CODE_OPEN_COLLECTION:
                switch (resultCode) {
                    case RESULT_GO_TO_FOUND:
                        MusicBroadcastManager.sendBroadcast(MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO);
                    default:
                        break;
                }
                break;
            case REQUEST_CODE_OPEN_BOUGHT:
                switch (resultCode) {
                    case RESULT_GO_TO_FOUND:
                        MusicBroadcastManager.sendBroadcast(MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE);
                    default:
                        break;
                }
                break;
            case REQUEST_CODE_OPEN_LOCAL:
            case REQUEST_CODE_OPEN_DOWNLOAD:
            default:
                break;
        }
    }

    private class NetWordStateChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case SYSTEM_BROADCAST_NETWORK_CHANGE:
                        if (CheckTools.isNetWordAvailable(getContext())) {
                            if (mRecommendSongLists.size() == 0) {
                                initRecommend();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

    }

    private boolean hasLocalCache() {
        return mSharedPreferences.getBoolean(HAS_LOCAL_RECOMMEND_CACHE, false);
    }

    private void storeRecommendDataCache() {
        Gson gson = new Gson();
        String string = gson.toJson(mRecommendSongLists);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(HAS_LOCAL_RECOMMEND_CACHE, true);
        editor.putString(LOCAL_RECOMMEND_CACHE, string);
        editor.apply();
    }

    private void loadRecommendDataFromCache() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (hasLocalCache()) {
                    Gson gson = new Gson();
                    String jsonData = mSharedPreferences.getString(LOCAL_RECOMMEND_CACHE, "");
                    if (!"".equals(jsonData)) {
                        List<MyRecommendSongListJSonBean.MyRecommendSongList> recommendSongLists = gson.fromJson(jsonData, new TypeToken<List<MyRecommendSongListJSonBean.MyRecommendSongList>>() {
                        }.getType());
                        if (recommendSongLists != null && recommendSongLists.size() == 6) {
                            mRecommendSongLists = recommendSongLists;
                            updateRecommendSongListOnUI(mRecommendSongLists);
                        }
                    }
                }
            }
        });
    }
}
