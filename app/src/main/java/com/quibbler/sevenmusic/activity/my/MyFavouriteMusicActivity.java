package com.quibbler.sevenmusic.activity.my;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.my.FavouriteMusicAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.fragment.my.MyFragment.RESULT_GO_TO_FOUND;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyFavouriteMusicActivity
 * Description:    我喜欢的歌曲
 * Author:         zhaopeng
 * CreateDate:     2019/09/26 09:21
 */
public class MyFavouriteMusicActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MyFavouriteMusicActivity";

    private List<MusicInfo> mFavouriteMusicLists = new ArrayList<>();
    private FavouriteMusicAdapter mAdapter;
    private ListView mFavouriteMusicListView;
    private TextView mNoneMusicTextView;

    private View mButtonView;
    private TextView mSelectAll;
    private TextView mCancel;
    private TextView mDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_favourite_music);

        init();
        initData();

        initManagerFunction();
    }

    public void init() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.my_favourite_music_title);
        mNoneMusicTextView = findViewById(R.id.my_favourite_no_music);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.my_favourite_no_music));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                setResult(RESULT_GO_TO_FOUND);
                finish();
            }
        };
        spannableStringBuilder.setSpan(clickableSpan, 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mNoneMusicTextView.setMovementMethod(LinkMovementMethod.getInstance());
        mNoneMusicTextView.setText(spannableStringBuilder);

        mFavouriteMusicListView = findViewById(R.id.my_favourite_music_list_view);
        mFavouriteMusicListView.setDivider(null);
        mAdapter = new FavouriteMusicAdapter(this, mFavouriteMusicLists);
        mFavouriteMusicListView.setAdapter(mAdapter);
//        mFavouriteMusicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
    }

    private void initManagerFunction() {
        mButtonView = findViewById(R.id.my_music_manager_button_layout);

        mSelectAll = findViewById(R.id.my_music_manager_menu_text_select_all);
        mCancel = findViewById(R.id.my_music_manager_menu_text_cancel);
        mDelete = findViewById(R.id.my_music_manager_menu_text_delete);

        mSelectAll.setOnClickListener(this::onClick);
        mCancel.setOnClickListener(this::onClick);
        mDelete.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.my_music_manager_menu_text_select_all) {
            mAdapter.selectAll();
        } else if (v.getId() == R.id.my_music_manager_menu_text_cancel) {
            mAdapter.changeManagerState();
            mButtonView.setVisibility(View.GONE);
            mAdapter.unSelectAndReset();
        } else if (v.getId() == R.id.my_music_manager_menu_text_delete) {
            int select = mAdapter.getSelectCount();
            if (select == 0) {
                Toast.makeText(this, R.string.my_favourite_music_manager_toast, Toast.LENGTH_SHORT).show();
            } else {
                if (select == mFavouriteMusicLists.size()) {
                    mNoneMusicTextView.setVisibility(View.VISIBLE);
                }
                mAdapter.removeFavourite();
                mAdapter.changeManagerState();
                mButtonView.setVisibility(View.GONE);

            }
        }
    }

    public void initData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                List<MusicInfo> tempList = new ArrayList<>();
                Uri uri = Uri.parse("content://" + MusicContentProvider.MUSIC_AUTHORITY + "/favourite");
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(uri, null, null, null, "rowid desc");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            MusicInfo musicInfo = new MusicInfo();
                            musicInfo.setId(cursor.getString(cursor.getColumnIndex("id")));
                            musicInfo.setMusicSongName(cursor.getString(cursor.getColumnIndex("name")));
                            musicInfo.setSinger(cursor.getString(cursor.getColumnIndex("singer")));
                            musicInfo.setMusicFilePath(cursor.getString(cursor.getColumnIndex("path")));
                            tempList.add(musicInfo);
                        }
                        cursor.close();
                    }
                    updateUI(tempList);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }
        });
    }

    private void updateUI(List<MusicInfo> tempList) {
        if (tempList.size() == 0) {
            mNoneMusicTextView.setVisibility(View.VISIBLE);
            return;
        }
        mNoneMusicTextView.setVisibility(View.GONE);
        mAdapter.update(tempList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.my_music_manager_menu_text) {
            if (mFavouriteMusicLists.size() == 0) {
                Toast.makeText(this, R.string.my_favourite_music_manager_edit, Toast.LENGTH_SHORT).show();
                return true;
            }
            mAdapter.changeManagerState();
            if (mButtonView.getVisibility() == View.VISIBLE) {
                mButtonView.setVisibility(View.GONE);
                mAdapter.unSelectAndReset();
            } else {
                mButtonView.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_music_favourite_manager, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mButtonView.getVisibility() == View.VISIBLE) {
            mAdapter.changeManagerState();
            mButtonView.setVisibility(View.GONE);
            mAdapter.unSelectAndReset();
        } else {
            super.onBackPressed();
        }
    }
}
