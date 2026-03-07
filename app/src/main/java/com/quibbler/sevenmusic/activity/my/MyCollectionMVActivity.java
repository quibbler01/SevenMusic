package com.quibbler.sevenmusic.activity.my;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.adapter.my.MyCollectionMvAdapter;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.utils.CloseResourceUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.MV_URL;
import static com.quibbler.sevenmusic.fragment.my.MyFragment.RESULT_GO_TO_FOUND;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyCollectionMVActivity
 * Description:    收藏MV 长按编辑、播放 。mv收藏展示
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 15:22
 */
public class MyCollectionMVActivity extends AppCompatActivity {
    private static final String TAG = "MyCollectionMVActivity";

    private TextView mNoMvCollectionHint;
    private ListView mCollectionMV;
    private MyCollectionMvAdapter mAdapter;
    private List<MVShowInfo> mLists = new ArrayList<>();

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_bought_music);
        init();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerForContextMenu(mCollectionMV);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterForContextMenu(mCollectionMV);
    }

    @MainThread
    public void init() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.my_collection_mv));

        mNoMvCollectionHint = findViewById(R.id.my_bought_no_music_found);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.my_bought_none_music_text));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                setResult(RESULT_GO_TO_FOUND);
                finish();
            }
        };
        spannableStringBuilder.setSpan(clickableSpan, 11, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mNoMvCollectionHint.setMovementMethod(LinkMovementMethod.getInstance());
        mNoMvCollectionHint.setText(spannableStringBuilder);

        mCollectionMV = findViewById(R.id.my_collection_mv);
        mCollectionMV.setDivider(null);

        mAdapter = new MyCollectionMvAdapter(this, R.layout.my_collection_mv_item, mLists);
        mCollectionMV.setAdapter(mAdapter);
    }

    private void initData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                List<MVShowInfo> temp = new ArrayList<>();
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(MV_URL, null, null, null, "rowid desc");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            MVShowInfo mvInfo = new MVShowInfo();
                            mvInfo.setId(cursor.getString(cursor.getColumnIndex("id")));
                            mvInfo.setName(cursor.getString(cursor.getColumnIndex("name")));
                            mvInfo.setPictureurl(cursor.getString(cursor.getColumnIndex("pictureurl")));
                            temp.add(mvInfo);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (temp.size() == 0) {
                                    mNoMvCollectionHint.setVisibility(View.VISIBLE);
                                } else {
                                    mNoMvCollectionHint.setVisibility(View.GONE);
                                }
                                mAdapter.updateData(temp);
                                popTips();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    CloseResourceUtil.closeInputAndOutput(cursor);
                }
            }
        });
    }

    private void popTips() {
        if (mLists.size() != 0) {
            MusicThreadPool.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(mNoMvCollectionHint, getString(R.string.my_collection_mv_onlong_click), Snackbar.LENGTH_LONG).setAction(getString(R.string.my_collection_mv_onlong_know), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //
                                    }
                                }).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.my_collection_mv_manager));
        menu.add(0, 1, 1, getString(R.string.my_collection_mv_manager_play));
        menu.add(0, 2, 2, getString(R.string.my_collection_mv_manager_delete));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = (int) mCollectionMV.getAdapter().getItemId(menuInfo.position);
        switch (item.getItemId()) {
            case 1:
                MvInfo mvInfo = new MvInfo(Integer.valueOf(mLists.get(pos).getId()), mLists.get(pos).getName(), null, 0, null, mLists.get(pos).getPictureurl());
                ActivityStart.startMvPlayActivity(MyCollectionMVActivity.this, mvInfo);
                break;
            case 2:
                getContentResolver().delete(MV_URL, "id = ?", new String[]{mLists.get(pos).getId()});
                mLists.remove(pos);
                mAdapter.notifyDataSetChanged();
                if (mLists.size() == 0) {
                    mNoMvCollectionHint.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
        return true;
    }

    public class MVShowInfo {
        private String id = "";
        private String name = "";
        private String pictureurl = "";

        public MVShowInfo() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPictureurl() {
            return pictureurl;
        }

        public void setPictureurl(String pictureurl) {
            this.pictureurl = pictureurl;
        }
    }
}
