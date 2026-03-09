package com.quibbler.sevenmusic.utils;

import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.SingerInfo;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      BeanConverter
 * Description:    Bean之间互相转换的工具类
 * Author:         yanwuyang
 * CreateDate:     2019/9/25 16:48
 */
public class BeanConverter {

    public static MvInfo convertMvInfo2MvInfo(MvInfo MvInfo) {
        if (MvInfo == null) {
            return null;
        }

        MvInfo mvInfo = new MvInfo(Integer.valueOf(MvInfo.getId()), MvInfo.getName(), MvInfo.getArtists(), MvInfo.getPlayCount(), MvInfo.getCopyWriter(), MvInfo.getPictureUrl());
        mvInfo.setUrl(MvInfo.getUrl());
        return mvInfo;
    }

    public static Artist convertSingerInfo2Artist(SingerInfo singerInfo) {
        if (singerInfo == null) {
            return null;
        }

        Artist artist = new Artist(Integer.valueOf(singerInfo.getId()), singerInfo.getName());
        return artist;
    }

    public static MvMusicInfo convertMusicInfo2MvMusicInfo(MusicInfo musicInfo) {
        int id = Integer.valueOf(musicInfo.getId());
        String name = musicInfo.getMusicSongName();
        String picUrl = musicInfo.getAlbumPicUrl();
        List<Artist> artists = new ArrayList<>();
        if (musicInfo.getAr() != null) {
            for (SingerInfo singerInfo : musicInfo.getAr()) {
                artists.add(convertSingerInfo2Artist(singerInfo));
            }
        }
        return new MvMusicInfo(id, name, picUrl, artists);
    }
}
