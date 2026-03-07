package com.quibbler.sevenmusic.fragment.my;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.found.SingerActivity;
import com.quibbler.sevenmusic.adapter.my.MyCollectionAdapter;
import com.quibbler.sevenmusic.bean.MyCollectionsInfo;
import com.quibbler.sevenmusic.listener.MyCollectionViewListener;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.MUSIC_AUTHORITY;
import static com.quibbler.sevenmusic.fragment.my.MyFragment.RESULT_GO_TO_FOUND;

/**
 * Package:        com.quibbler.sevenmusic.fragment.my
 * ClassName:      MyCollectionSongFragment
 * Description:    收藏Fragment页面 功能细节完善
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 17:13
 */
public class MyCollectionSongFragment extends Fragment {
    private int mCollectionKind;
    private List<MyCollectionsInfo> myCollectionsInfos = new ArrayList<>();
    private MyCollectionAdapter mAdapter;

    private View mCollectionView;
    private ListView mListView;
    private TextView mTextView;

    private MyCollectionViewListener myCollectionViewListener = new MyCollectionViewListener() {
        @Override
        public void changeView() {
            if (mTextView.getVisibility() == View.VISIBLE) {
                mTextView.setVisibility(View.GONE);
            } else {
                mTextView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void removeData(int id) {
            for (MyCollectionsInfo myCollectionsInfo : myCollectionsInfos) {
                if (myCollectionsInfo.getId() == id) {
                    myCollectionsInfos.remove(myCollectionsInfo);
                    return;
                }
            }
        }
    };

    public MyCollectionSongFragment(int kind) {
        this.mCollectionKind = kind;
    }

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initData();
        mCollectionView = inflater.inflate(R.layout.my_collection_fragment, container, false);

        mAdapter = new MyCollectionAdapter(getContext(), R.layout.my_collection_item, myCollectionsInfos, mCollectionKind, myCollectionViewListener);
        mListView = mCollectionView.findViewById(R.id.my_collection_list_view);
        mListView.setDivider(null);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mCollectionKind) {
                    case 1:
                        Intent intent = new Intent(getActivity(), SingerActivity.class);
                        intent.putExtra("id", myCollectionsInfos.get(position).getId() + "");
                        if (getActivity() != null) {
                            getActivity().startActivity(intent);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        mTextView = mCollectionView.findViewById(R.id.my_collection_text_view);
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (getActivity() != null) {
                    getActivity().setResult(RESULT_GO_TO_FOUND);
                    getActivity().finish();
                }
            }
        };
        switch (mCollectionKind) {
            case 0:
                SpannableStringBuilder songSpannableStringBuilder = new SpannableStringBuilder("没有收藏过音乐哦\n去发现页面寻找吧!");
                songSpannableStringBuilder.setSpan(clickableSpan, 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTextView.setText(songSpannableStringBuilder);
                break;
            case 1:
                SpannableStringBuilder singerSpannableStringBuilder = new SpannableStringBuilder("没有心怡的歌手?\n去发现页面寻找吧!");
                singerSpannableStringBuilder.setSpan(clickableSpan, 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTextView.setText(singerSpannableStringBuilder);
                break;
            case 2:
                SpannableStringBuilder albumSpannableStringBuilder = new SpannableStringBuilder("没有喜欢的专辑?\n去发现页面寻找吧!");
                albumSpannableStringBuilder.setSpan(clickableSpan, 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTextView.setText(albumSpannableStringBuilder);
                break;
        }
        if (myCollectionsInfos.size() != 0) {
            mTextView.setVisibility(View.GONE);
        }
        return mCollectionView;
    }

    public void initData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                List<MyCollectionsInfo> tmp = new ArrayList<>();
                Uri uri = Uri.parse("content://" + MUSIC_AUTHORITY + "/collection");
                Cursor cursor = MusicApplication.getContext().getContentResolver().query(uri, null, null, null, "rowid desc");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MyCollectionsInfo myCollectionsInfo = new MyCollectionsInfo();
                        myCollectionsInfo.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        myCollectionsInfo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                        myCollectionsInfo.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                        myCollectionsInfo.setKind(cursor.getInt(cursor.getColumnIndex("kind")));
                        if (myCollectionsInfo.getKind() == mCollectionKind) {
                            tmp.add(myCollectionsInfo);
                        }
                    }
                    cursor.close();
                }
                updateUI(tmp);
            }
        });
    }

    private void updateUI(List<MyCollectionsInfo> list) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (list != null && list.size() != 0) {
                        mTextView.setVisibility(View.GONE);
                        mAdapter.updateData(list);
                    }
                }
            });
        }
    }
}
