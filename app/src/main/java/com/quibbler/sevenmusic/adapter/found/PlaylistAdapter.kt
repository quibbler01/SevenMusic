package com.quibbler.sevenmusic.adapter.found;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.SingerInfo;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.service.MusicDownloaderService;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.BeanConverter;
import com.quibbler.sevenmusic.utils.MusicThreadPool;
import com.quibbler.sevenmusic.view.found.FoundCustomDialog;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.COLLECTION_URL;

public class PlaylistAdapter extends RecyclerBaseAdapter<MusicInfo, PlaylistAdapter.PlaylistViewHolder> {
    private static final String TAG = "PlaylistAdapter";
    //kind 0歌曲 1歌手 2专辑
    private static final Integer MUSIC_KIND = 0;

    //显示歌手个数
//    private static final Integer SINGER_NUM = 3;

    //当前正在播放的歌曲的位置，默认为-1
    private int mPlayingPosition = -1;

    //是否处于初始化
    private boolean mIsInInit = true;
    private Thread mQueryThread;

    //歌单的歌曲数据源list
    private List<MusicInfo> mMusicInfoList = mSourceList;
    private final Context mContext;

    //选择将要下载的歌曲id的list
    private List<Integer> mSelectedPositionList = new ArrayList<>();

    //是否在多选模式
    private boolean mIsInSelectMode = false;

    //弹出的下载收藏等popView
    private PopupWindow mDownloadPopWindow;

    public PlaylistAdapter(List<MusicInfo> musicInfoList, Context context) {
        super(musicInfoList);
        mContext = context;

        if (mQueryThread != null) {
            mQueryThread.interrupt();
        }
        mQueryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (MusicInfo musicInfo : musicInfoList) {
                    musicInfo.setCollected(queryIdInThread(context, musicInfo.getId()));
                }
            }
        });
        mQueryThread.start();
    }


    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mTvName;
        private TextView mTvSinger;
        private TextView mTvNum;
        private ImageButton mBtnPlayOrPause;
        private ImageButton mBtnPop;
        private CheckBox mCbSelect;

        PlaylistViewHolder(View view) {
            super(view);
            mView = view;
            mTvName = view.findViewById(R.id.playlist_list_item_tv_name);
            mTvSinger = view.findViewById(R.id.playlist_list_item_tv_singer_or_album);
            mTvNum = view.findViewById(R.id.playlist_list_item_tv_num);
            mBtnPlayOrPause = view.findViewById(R.id.playlist_list_item_btn_play_pause);
            mBtnPop = view.findViewById(R.id.playlist_list_item_btn_pop);
            mCbSelect = view.findViewById(R.id.playlist_list_item_cb_download);
        }
    }

    /**
     * 选择全部歌曲的checkbox
     */
    public void selectAll() {
        mSelectedPositionList.clear();
        for (int i = 0; i < mMusicInfoList.size(); i++) {
            mSelectedPositionList.add(i);
        }
        notifyDataSetChanged();
    }

    /**
     * 清除全部歌曲的checkbox
     */
    public void unselectAll() {
        mSelectedPositionList.clear();
        notifyDataSetChanged();
    }

    /**
     * 开始下载歌曲
     */
    public void startDownload() {
//        StringBuffer logStr = new StringBuffer();
        ArrayList<MusicInfo> musicInfoList = new ArrayList<>();
        for (Integer i : mSelectedPositionList) {
//            logStr.append(String.valueOf(i) + ", ");
            MusicInfo musicInfo = mMusicInfoList.get(i);
            musicInfo.setSinger(musicInfo.getArName());
            musicInfoList.add(musicInfo);
            Log.d(TAG, "singer: " + musicInfo.getSinger());
        }
        if (musicInfoList.size() != 0) {
            Intent intent = new Intent(mContext, MusicDownloaderService.class);
            intent.putExtra("musics", musicInfoList);

            MusicApplication.getContext().startService(intent);
        }
//        Log.d(TAG, "开始下载：" + logStr.toString());

        mSelectedPositionList.clear();
//        changeSelectMode(false);
    }

    public void changeSelectMode(boolean mode) {
        mIsInSelectMode = mode;
        notifyDataSetChanged();
    }

    @Override
    public void updateData(List<MusicInfo> list) {
        super.updateData(list);
        if (mQueryThread != null) {
            mQueryThread.interrupt();
        }
        mQueryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (MusicInfo musicInfo : list) {
                    musicInfo.setCollected(queryIdInThread(mContext, musicInfo.getId()));
                }
            }
        });
        mQueryThread.start();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_list_item_tracks, parent, false);
        PlaylistViewHolder viewHolder = new PlaylistViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        StringBuffer logStr = new StringBuffer();
        for (Integer i : mSelectedPositionList) {
            logStr.append(String.valueOf(i) + ", ");
        }
        Log.d(TAG, logStr.toString());

        //popView状态
        holder.mBtnPop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popView;
                if (mDownloadPopWindow != null) {
                    popView = mDownloadPopWindow.getContentView();
                    mDownloadPopWindow.setFocusable(true);
                    if (mDownloadPopWindow.isShowing()) {
                        mDownloadPopWindow.dismiss();
                    } else {
                        mDownloadPopWindow.showAtLocation(holder.mView, Gravity.BOTTOM | Gravity.CENTER, 0, 0);
                        darkenBackground(0.5f);
                    }
                } else {
                    popView = LayoutInflater.from(holder.mView.getContext()).inflate(R.layout.playlist_pop_view, null);
                    mDownloadPopWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, 600, false);
                    mDownloadPopWindow.setOutsideTouchable(true); // 点击外部关闭
                    mDownloadPopWindow.setFocusable(true);
                    mDownloadPopWindow.setAnimationStyle(android.R.style.Animation_Dialog);
                    mDownloadPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    mDownloadPopWindow.showAtLocation(holder.mView, Gravity.BOTTOM | Gravity.CENTER, 0, 0);
                    darkenBackground(0.5f);
                    mDownloadPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            darkenBackground(1.0f);
                        }
                    });
                }

                //具体操作监听
                MusicInfo musicInfo = mMusicInfoList.get(position);
                //收藏歌曲操作
                LinearLayout llCollect = popView.findViewById(R.id.playlist_pop_ll_collect);
                ImageView ivCollect = popView.findViewById(R.id.playlist_pop_iv_collect);
                TextView tvCollect = popView.findViewById(R.id.playlist_pop_tv_collect);
                if (musicInfo.isCollected()) {
                    ivCollect.setImageResource(R.drawable.playlist_btn_music_collected);
                    tvCollect.setText(R.string.playlist_pop_collected);
                } else {
                    ivCollect.setImageResource(R.drawable.playlist_btn_music_not_collected);
                    tvCollect.setText(R.string.playlist_pop_not_collected);
                }
                llCollect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (musicInfo.isCollected()) {
                            //已收藏，点击后取消收藏
                            ivCollect.setImageResource(R.drawable.playlist_btn_music_not_collected);
                            tvCollect.setText(R.string.playlist_pop_not_collected);
                            musicInfo.setCollected(false);
                            MusicThreadPool.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    updateMusicCollectInThread(holder.mView.getContext(), musicInfo, false);
                                }
                            });
                        } else {
                            //未收藏，点击后收藏
                            ivCollect.setImageResource(R.drawable.playlist_btn_music_collected);
                            tvCollect.setText(R.string.playlist_pop_collected);
                            musicInfo.setCollected(true);
                            MusicThreadPool.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    updateMusicCollectInThread(holder.mView.getContext(), musicInfo, true);
                                }
                            });
                        }
                    }
                });

                //添加到歌单
                LinearLayout llAddToCustomList = popView.findViewById(R.id.playlist_pop_ll_add_to_custom_list);
                llAddToCustomList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FoundCustomDialog dialog = new FoundCustomDialog(mContext, musicInfo);
                        dialog.show();
//                        MusicDatabaseUtils.addToMusicList(String listName,MusicInfo musicInfo);
                    }
                });
            }
        });

        //checkbox状态
        //先清空checkbox的监听器，防止ViewHolder复用时监听错乱
        holder.mCbSelect.setOnCheckedChangeListener(null);
        if (mIsInSelectMode) {
            holder.mCbSelect.setVisibility(View.VISIBLE);
            holder.mCbSelect.setChecked(mSelectedPositionList.contains((Integer) position));
        } else {
            holder.mCbSelect.setVisibility(View.GONE);
//            holder.mCbSelect.setChecked(mSelectedPositionList.contains((Integer) position));
        }
        holder.mCbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSelectedPositionList.add((Integer) position);
                } else {
                    mSelectedPositionList.remove((Integer) position);
                }
            }
        });

        MusicInfo musicInfo = mMusicInfoList.get(position);
        holder.mTvName.setText(musicInfo.getMusicSongName());
        holder.mTvSinger.setText(musicInfo.getAllArName());
        holder.mTvNum.setText(String.valueOf(position + 1));


        if (mIsInInit) {
            if (MusicPlayerService.isPlaying && musicInfo.getId().equals(MusicPlayerService.sMusicID)) {
                //如果该歌曲正在播放，则显示暂停按钮
                holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_pause_red);
                mPlayingPosition = position;
            } else {
                //如果该歌曲未播放，则显示播放按钮
                holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_play);
            }

            if (position == mMusicInfoList.size() - 1) {
                mIsInInit = false;
            }
        } else {
            if (mPlayingPosition == position) {
                //如果该歌曲正在播放，则显示暂停按钮
                holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_pause_red);
            } else {
                //如果该歌曲未播放，则显示播放按钮
                holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_play);
            }
        }

        //页面点击事件的反馈比service控制歌曲要快，所以无法用service的参数进行状态判断，而要在当前页面中自行控制
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPlayingPosition == -1) {
                    //当前歌单没有歌曲在播放
                    //更新mPlayingPosition，开启播放服务
                    mPlayingPosition = position;

                    holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_pause_red);

                    musicInfo.setSinger(musicInfo.getFirstArName());
                    MusicPlayerService.playMusic(musicInfo);
                } else if (mPlayingPosition == position) {
                    //当前歌单有歌曲正在播放，且是当前点击的歌曲
                    //开启播放activity
                    int id = Integer.valueOf(musicInfo.getId());
                    String name = musicInfo.getMusicSongName();
                    String url = musicInfo.getAlbumPicUrl();
                    List<Artist> artistList = new ArrayList<>();
                    for (SingerInfo singerInfo : musicInfo.getAr()) {
                        artistList.add(BeanConverter.convertSingerInfo2Artist(singerInfo));
                    }
                    MvMusicInfo mvMusicInfo = new MvMusicInfo(id, name, url, artistList);
                    if (mContext instanceof Activity) {
                        Activity activity = (Activity) mContext;
                        ActivityStart.startMusicPlayActivity(activity, mvMusicInfo);
                    }
                } else {
                    //当前歌单有歌曲正在播放，但不是当前点击的歌曲
                    //更新mPlayingPosition，更新播放服务
                    mPlayingPosition = position;

                    notifyDataSetChanged();

                    musicInfo.setSinger(musicInfo.getFirstArName());
                    MusicPlayerService.playMusic(musicInfo);
                }
            }
        });

        holder.mBtnPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayingPosition == -1) {
                    //当前歌单没有歌曲在播放
                    //更新mPlayingPosition，开启播放服务
                    mPlayingPosition = position;

                    holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_pause_red);

                    musicInfo.setSinger(musicInfo.getFirstArName());
                    MusicPlayerService.playMusic(musicInfo);
                } else if (mPlayingPosition == position) {
                    //当前歌曲正在播放，点击后暂停播放
                    mPlayingPosition = -1;

                    holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_play);

                    MusicPlayerService.pauseMusic();
                } else {
                    //当前歌单有歌曲正在播放，但不是当前点击的歌曲
                    //更新mPlayingPosition，更新播放服务
                    mPlayingPosition = position;

                    notifyDataSetChanged();

                    musicInfo.setSinger(musicInfo.getFirstArName());
                    MusicPlayerService.playMusic(musicInfo);
                }
            }
        });
    }

    /**
     * 刷新页面信息，不包括checkbox的状态
     */
    public void refresh() {
        mIsInInit = true;
        mPlayingPosition = -1;
        queryAllIfCollect((Activity) mContext);
//        notifyDataSetChanged();
    }

    /**
     * 刷新页面信息，包括checkbox
     */
    public void refreshCompletely() {

    }

    /**
     * 查询某一首歌是否被用户收藏
     *
     * @param context 上下文
     * @param id      歌曲id
     * @return
     */
    private boolean queryIdInThread(Context context, String id) {
        boolean result = false;
        Uri uri = COLLECTION_URL;
        Cursor cursor = context.getContentResolver().query(uri, null, "id = ?", new String[]{id}, null);
        if (cursor != null && cursor.moveToFirst()) {
            //本地收藏数据库有记录
            result = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    private void queryAllIfCollect(Activity activity) {
        if (mQueryThread != null) {
            mQueryThread.interrupt();
        }
        mQueryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (MusicInfo musicInfo : mMusicInfoList) {
                    musicInfo.setCollected(queryIdInThread(mContext, musicInfo.getId()));
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
        mQueryThread.start();
    }

    /**
     * 更新歌曲收藏数据库
     *
     * @param context
     * @param musicInfo
     * @param collected
     */
    private void updateMusicCollectInThread(Context context, MusicInfo musicInfo, boolean collected) {
        Uri authorityUri = COLLECTION_URL;
        if (collected) {
            //增加该数据
            ContentValues values = new ContentValues();
            values.put("id", musicInfo.getId());
            values.put("title", musicInfo.getMusicSongName());
            values.put("kind", MUSIC_KIND);
//            values.put("description", "");
            context.getContentResolver().insert(authorityUri, values);
        } else {
            //删除该数据
            Uri collectionUrl = Uri.parse(authorityUri + "/" + musicInfo.getId());
            context.getContentResolver().delete(collectionUrl, null, null);
        }

    }

    public void setPlayingPosition(int playingPosition) {
        mPlayingPosition = playingPosition;
    }

    //设置屏幕透明度,bgcolor:0-1
    private void darkenBackground(float bgcolor) {
        if (mContext != null) {
            Activity activity = (Activity) mContext;
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.alpha = bgcolor;
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            activity.getWindow().setAttributes(lp);
        }
    }
}
