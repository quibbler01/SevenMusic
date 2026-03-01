package com.quibbler.sevenmusic.activity.my;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.my.MyCollectionViewsAdapter;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyCollectionsActivity
 * Description:    我的收藏:歌曲，歌手，专辑收藏,解决收藏不同步的问题
 * 20191010 歌曲、歌手、专辑收藏功能完善 取消收藏，撤销恢复；歌曲，歌手图片加载,断网显示默认图标
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:22
 */
public class MyCollectionsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MyCollectionsActivity";

    private ViewPager mCollectionViewPager;
    private MyCollectionViewsAdapter mAdapter;

    private TextView mSongTitle;
    private TextView mSingerTitle;
    private TextView mAlbumTitle;

    private View mSongLine;
    private View mSingerLine;
    private View mAlbumLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_clooections);

        init();
    }

    public void init() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.my_collection_music_text));

        mSongTitle = findViewById(R.id.my_collection_title_song);
        mSingerTitle = findViewById(R.id.my_collection_title_singer);
        mAlbumTitle = findViewById(R.id.my_collection_title_album);

        mSongTitle.setOnClickListener(this);
        mSingerTitle.setOnClickListener(this);
        mAlbumTitle.setOnClickListener(this);

        mSongLine = findViewById(R.id.my_collection_line_song);
        mSingerLine = findViewById(R.id.my_collection_line_singer);
        mAlbumLine = findViewById(R.id.my_collection_line_album);

        mAdapter = new MyCollectionViewsAdapter(getSupportFragmentManager());
        mCollectionViewPager = findViewById(R.id.my_collection_viewpager);
        mCollectionViewPager.setAdapter(mAdapter);
        mCollectionViewPager.setCurrentItem(0);
        mCollectionViewPager.setOffscreenPageLimit(1);

        mCollectionViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeSelectStats(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mCollectionViewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View view, float position) {
                float MIN_SCALE = 0.75f;
                int pageWidth = view.getWidth();

                if (position < -1) {
                    view.setAlpha(0);
                } else if (position <= 0) {
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setScaleX(1);
                    view.setScaleY(1);
                } else if (position <= 1) {
                    view.setAlpha(1 - position);
                    view.setTranslationX(pageWidth * -position);
                    float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);
                } else {
                    view.setAlpha(0);
                }
            }
        });
    }

    private void changeSelectStats(int position) {
        switch (position) {
            case 0:
                mSongTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                mSingerTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mAlbumTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mSongLine.setBackgroundColor(getColor(R.color.my_collection_line_select_color));
                mSingerLine.setBackgroundColor(getColor(R.color.my_collection_line_color));
                mAlbumLine.setBackgroundColor(getColor(R.color.my_collection_line_color));
                mCollectionViewPager.setCurrentItem(0);
                break;
            case 1:
                mSongTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mSingerTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                mAlbumTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mSongLine.setBackgroundColor(getColor(R.color.my_collection_line_color));
                mSingerLine.setBackgroundColor(getColor(R.color.my_collection_line_select_color));
                mAlbumLine.setBackgroundColor(getColor(R.color.my_collection_line_color));
                mCollectionViewPager.setCurrentItem(1);
                break;
            case 2:
                mSongTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mSingerTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mAlbumTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                mSongLine.setBackgroundColor(getColor(R.color.my_collection_line_color));
                mSingerLine.setBackgroundColor(getColor(R.color.my_collection_line_color));
                mAlbumLine.setBackgroundColor(getColor(R.color.my_collection_line_select_color));
                mCollectionViewPager.setCurrentItem(2);
                break;
            default:
                break;
        }
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
    public void onClick(View v) {
        if (v.getId() == R.id.my_collection_title_song) {
            changeSelectStats(0);
        } else if (v.getId() == R.id.my_collection_title_singer) {
            changeSelectStats(1);
        } else if (v.getId() == R.id.my_collection_title_album) {
            changeSelectStats(2);
        }
    }
}
