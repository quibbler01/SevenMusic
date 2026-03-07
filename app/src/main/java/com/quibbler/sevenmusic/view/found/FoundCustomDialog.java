package com.quibbler.sevenmusic.view.found;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.found.FoundListDialogAdapter;
import com.quibbler.sevenmusic.bean.CustomMusicList;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.MySongListInfo;
import com.quibbler.sevenmusic.utils.MusicDatabaseUtils;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;

import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.SONGLIST_URL;
import static com.quibbler.sevenmusic.fragment.my.MyFragment.CREATOR_KEY;
import static com.quibbler.sevenmusic.utils.ResUtil.getString;

public class FoundCustomDialog extends Dialog {

    private Context mContext;
    private Thread mThread;
    private ArrayList<CustomMusicList> mCustomMusicLists;
    private RecyclerView mRecyclerView;
    private FoundListDialogAdapter mFoundListDialogAdapter;
    private MusicInfo mMusicInfo;
    private TextView mTvCreate;

    public FoundCustomDialog(Context context, MusicInfo musicInfo) {
        super(context);
        mContext = context;
        mMusicInfo = musicInfo;
    }

    @UiThread
    private void onObtainList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mFoundListDialogAdapter = new FoundListDialogAdapter(mCustomMusicLists, mMusicInfo, this);
        mRecyclerView.setAdapter(mFoundListDialogAdapter);

        mFoundListDialogAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.found_dialog_add_to_custom_playlist, null);
        mRecyclerView = view.findViewById(R.id.dialog_rv_custom_playlist);
        mTvCreate = view.findViewById(R.id.dialog_tv_create_new_custom_playlist);

        setContentView(view);

        mTvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSongListDialog();
            }
        });

        if (mThread != null) {
            mThread.interrupt();
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mCustomMusicLists = MusicDatabaseUtils.getCustomMusicList();

                if (mContext != null && mContext instanceof Activity) {
                    Activity activity = (Activity) mContext;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onObtainList();
                        }
                    });
                }

            }
        });
        mThread.start();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mThread.interrupt();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mThread != null) {
            mThread.interrupt();
        }
    }


    /*
    创建歌单Dialog
     */
    private void createSongListDialog() {
        EditText editText = new EditText(getContext());
        editText.setSingleLine();
        editText.setText("歌单名");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("新建歌单").setView(editText)
                .setPositiveButton("新建", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Cursor cursor = null;
                        cursor = MusicApplication.getContext().getContentResolver().query(SONGLIST_URL, null, "name = ?", new String[]{editText.getText().toString()}, null);
                        if (cursor != null && cursor.getCount() != 0) {
                            Toast.makeText(getContext(), "已存在重名歌单", Toast.LENGTH_SHORT).show();
                            cursor.close();
                            return;
                        }
                        MySongListInfo mySongListInfo = new MySongListInfo();
                        mySongListInfo.setListName(editText.getText().toString());
                        mySongListInfo.setCreator(Long.toString(System.currentTimeMillis()));

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

                                mCustomMusicLists = MusicDatabaseUtils.getCustomMusicList();
                                if (mContext != null && FoundCustomDialog.this != null && mFoundListDialogAdapter != null && mContext instanceof Activity) {
                                    Activity activity = (Activity) mContext;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFoundListDialogAdapter.updateData(mCustomMusicLists);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        editText.requestFocus();
        alertDialog.show();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}
