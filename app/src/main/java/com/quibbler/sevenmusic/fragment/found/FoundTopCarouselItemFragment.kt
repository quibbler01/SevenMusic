package com.quibbler.sevenmusic.fragment.found;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.SingerInfo;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.BeanConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.fragment.found
 * ClassName:      FoundTopCarouselItemFragment
 * Description:    轮播图item的fragment
 * Author:         yanwuyang
 * CreateDate:     2019/10/10 20:06
 */
public class FoundTopCarouselItemFragment extends Fragment {

    private String mImageUrl;
    private MusicInfo mMusicInfo;
    private ImageView mImageView;

    public FoundTopCarouselItemFragment() {
    }

    public FoundTopCarouselItemFragment(String url, MusicInfo musicInfo) {
        mImageUrl = url;
        mMusicInfo = musicInfo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_found_top_carousel_item, container, false);
        mImageView = view.findViewById(R.id.found_fragment_iv_carousel);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        ImageDownloadPresenter.getInstance().with(getContext())
//                .load(mImageUrl)
//                .imageStyle(ImageDownloadPresenter.STYLE_ROUND)
//                .into(mImageView);

        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(20);
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(600, 400);

        Glide.with(getContext())
                .load(mImageUrl)
//                    .placeholder(R.drawable.carousel_placeholder)
                .apply(options)
                .into(mImageView);


//        mImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mMusicInfo == null) {
//                    return;
//                }
//                //先播放
//                MusicPlayerService.playMusic(mMusicInfo);
//                int id = Integer.valueOf(mMusicInfo.getId());
//                String name = mMusicInfo.getMusicSongName();
//                String url = mMusicInfo.getAlbumPicUrl();
//                List<Artist> artistList = new ArrayList<>();
//                for (SingerInfo singerInfo : mMusicInfo.getAr()) {
//                    artistList.add(BeanConverter.convertSingerInfo2Artist(singerInfo));
//                }
//                MvMusicInfo mvMusicInfo = new MvMusicInfo(id, name, url, artistList);
//                ActivityStart.startMusicPlayActivity(getContext(), mvMusicInfo);
//            }
//        });
    }
}
