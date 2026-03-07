package com.quibbler.sevenmusic.adapter.mv;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.sevenvideoview.IVideoPlayerController;
import com.example.sevenvideoview.SevenVideoPlayer;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.found.RecyclerBaseAdapter;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;
import com.quibbler.sevenmusic.presenter.MvPresenter;
import com.quibbler.sevenmusic.utils.TextureVideoViewOutlineProvider;

import java.util.List;

public class RecommendMvAdapter extends RecyclerBaseAdapter<MvInfo, RecyclerView.ViewHolder> {

    private final static int RECOMMEND_TEXT_VIWE = 0;

    private final static int RECOMMEND_MV_VIWE = 1;

    private final static int MORE_TEXT_VIWE = 2;

    private final static int MORE_MV_VIWE = 3;

    private static final String TAG = "RecommendMvAdapter";

    private List<MvInfo> mMvInfoList = mSourceList;

    private TopMvClickListener mTopMvClickListener;

    private Context mAppCompatActivity;

//    TxVideoPlayerController controller;

    public RecommendMvAdapter(List<MvInfo> infoList, TopMvClickListener topMvClickListener) {
        super(infoList);
        mTopMvClickListener = topMvClickListener;

    }

    public void setAppCompatActivity(Context appCompatActivity) {
        mAppCompatActivity = appCompatActivity;

    }

    static class RecommendTextViewHolder extends RecyclerView.ViewHolder{
        TextView mTextView;

        public RecommendTextViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.mv_tv_text);
        }
    }

    static class MoreTextViewHolder extends RecyclerView.ViewHolder{
        TextView mTextView;

        public MoreTextViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.mv_tv_text);
        }
    }

    static class SmallMvViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView mVideoImageView;
        TextView mVideoNameView;
        TextView mVideoArtistView;
        ImageButton mMoreButton;

        public SmallMvViewHolder(View view) {
            super(view);
            mView = view;
            mVideoImageView = view.findViewById(R.id.mv_video);
            mVideoNameView = view.findViewById(R.id.mv_tv_name);
            mVideoArtistView = view.findViewById(R.id.mv_tv_artist);
            mMoreButton = view.findViewById(R.id.mv_ib_download);
        }
    }

    static class BigMvViewHolder extends RecyclerView.ViewHolder{
        View mView;
        SevenVideoPlayer mVideoView;
        TextView mVideoNameView;
        TextView mVideoArtistView;
        ImageButton mMoreButton;
        ImageView mArtistHeadView;

        public BigMvViewHolder(View view) {
            super(view);
            mView = view;
            mVideoView = view.findViewById(R.id.mv_video);
            mVideoNameView = view.findViewById(R.id.mv_tv_name);
            mVideoArtistView = view.findViewById(R.id.mv_tv_artist);
            mMoreButton = view.findViewById(R.id.mv_ib_download);
            mArtistHeadView = view.findViewById(R.id.mv_iv_artist_head);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == RECOMMEND_TEXT_VIWE){
            Log.d(TAG,"RECOMMEND_TEXT_VIWE");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mv_textview_item,
                    parent, false);
            return new RecommendTextViewHolder(view);
        }else if(viewType == RECOMMEND_MV_VIWE){
            Log.d(TAG,"RECOMMEND_MV_VIWE");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.small_mv_item,
                    parent, false);
            return new SmallMvViewHolder(view);
        }else if(viewType == MORE_TEXT_VIWE){
            Log.d(TAG,"MORE_TEXT_VIWE");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mv_textview_item,
                    parent, false);
            return new MoreTextViewHolder(view);
        }else if(viewType == MORE_MV_VIWE){
            Log.d(TAG,"MORE_MV_VIWE");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mv_item,
                    parent, false);
            return new BigMvViewHolder(view);
        }else {
    //        controller = new TxVideoPlayerController(MusicApplication.getContext());
        return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Log.d(TAG,"onBindViewHolder");
        if(holder instanceof RecommendTextViewHolder){
            ((RecommendTextViewHolder) holder).mTextView.setText(MusicApplication.getContext()
                    .getString(R.string.mv_choosen));
        }else if(holder instanceof SmallMvViewHolder){
            SmallMvViewHolder viewHolder = (SmallMvViewHolder) holder;
            MvInfo mvInfo = mMvInfoList.get(position - 1);
            Log.d(TAG, "recommend mvs: " + mvInfo.getName());
            viewHolder.mVideoNameView.setText(mvInfo.getName());
            StringBuffer artistString = new StringBuffer();
            List<Artist> mvArtistList = mvInfo.getArtists();
            if (mvArtistList != null) {
                for (int i = 0; i < mvArtistList.size(); i++) {
                    if (i == mvArtistList.size() - 1) {
                        artistString.append(mvArtistList.get(i).getName());
                    } else {
                        artistString.append(mvArtistList.get(i).getName() + "/");
                    }
                }
                viewHolder.mVideoArtistView.setText(artistString);
            }
            ImageDownloadPresenter.getInstance().with(MusicApplication.getContext())
                    .load(mvInfo.getPictureUrl())
                    .imageStyle(ImageDownloadPresenter.STYLE_ROUND)
                    .into(viewHolder.mVideoImageView);
            //设置图片圆角角度
//            RoundedCorners roundedCorners = new RoundedCorners(20);
//            RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
//            Glide.with(MusicApplication.getContext())
//                    .load(mvInfo.getPictureUrl())
//                    .apply(options)
//                    .placeholder(R.drawable.default_mv_cover)
//                    .into(viewHolder.mVideoImageView);
            viewHolder.mVideoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转进入播放activity
                    if(mTopMvClickListener != null){
                        mTopMvClickListener.onStartMvPlayActivity(mvInfo);
                    }
                }
            });
            viewHolder.mMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTopMvClickListener.onClickMoreButton(mvInfo);
                }
            });
        }else if(holder instanceof MoreTextViewHolder){
            ((MoreTextViewHolder) holder).mTextView.setText(MusicApplication.getContext()
                    .getString(R.string.mv_more_choosen));
        } else if(holder instanceof BigMvViewHolder){
            MvInfo mvInfo = mMvInfoList.get(position - 2);
            Log.d(TAG, "more mvs: " + mvInfo.getName());
            BigMvViewHolder viewHolder = (BigMvViewHolder) holder;
            viewHolder.mVideoNameView.setText(mvInfo.getName());
            StringBuffer artistString = new StringBuffer();
            List<Artist> mvArtistList = mvInfo.getArtists();
            if (mvArtistList != null) {
                for (int i = 0; i < mvArtistList.size(); i++) {
                    if (i == mvArtistList.size() - 1) {
                        artistString.append(mvArtistList.get(i).getName());
                    } else {
                        artistString.append(mvArtistList.get(i).getName() + "/");
                    }
                }
                viewHolder.mVideoArtistView.setText(artistString);
            }
            viewHolder.mVideoView.setContext(mAppCompatActivity);
            //设置圆角
            viewHolder.mVideoView.setOutlineProvider(new TextureVideoViewOutlineProvider(20));
            viewHolder.mVideoView.setClipToOutline(true);
            MvPresenter.getMvInfo(mvInfo, new MusicCallBack() {
                @Override
                public void onMusicInfoCompleted() {
                    viewHolder.mVideoView.setUp(mvInfo.getUrl(), null);

                }
            });
            IVideoPlayerController controller = new IVideoPlayerController(mAppCompatActivity);
            controller.setTitle(String.valueOf(mvInfo.getPlayCount()));
            viewHolder.mVideoView.setController(controller);
            Glide.with(mAppCompatActivity)
                    .load(mvInfo.getPictureUrl())
                    .placeholder(R.drawable.default_mv_cover)
                    .into(controller.imageView());
            //先用glide加载
            Glide.with(mAppCompatActivity)
                    .load(mvInfo.getPictureUrl())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(viewHolder.mArtistHeadView);
            viewHolder.mVideoNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转进入播放activity
                    if(mTopMvClickListener != null){
                        mTopMvClickListener.onStartMvPlayActivity(mvInfo);
                    }
                }
            });
            viewHolder.mMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTopMvClickListener.onClickMoreButton(mvInfo);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return RECOMMEND_TEXT_VIWE;
        }else if(position >= 1 && position <= 4){
            return RECOMMEND_MV_VIWE;
        }else if(position == 5){
            return MORE_TEXT_VIWE;
        }else if(position > 5){
            return MORE_MV_VIWE;
        }else{
            return super.getItemViewType(position);
        }
    }

    @Override
    public int getItemCount() {
        if(mMvInfoList.size() <= 0){
            return 0;
        }else if(mMvInfoList.size() <= 4){
            return mMvInfoList.size() + 1;
        }else{
            return mMvInfoList.size() + 2;
        }

    }

}

