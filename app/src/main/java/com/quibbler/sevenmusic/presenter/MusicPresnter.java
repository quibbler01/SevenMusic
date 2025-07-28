package com.quibbler.sevenmusic.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.utils.HttpUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
  *
  * Package:        com.quibbler.sevenmusic.presenter
  * ClassName:      MusicPresnter
  * Description:    关于音乐的presenter
  * Author:         lishijun
  * CreateDate:     2019/9/27 11:27
 */
public class MusicPresnter {

    private static final String MUSIC_CANUSE_URL = "/check/music?id=";

    private static final String SERVER = "http://114.116.128.229:3000";

    private static final String MUSIC_LYRIC_URL = "/lyric?id=";

    private static final String MUSIC_DETAIL_URL = "/song/detail?ids=";

    public static void getMusicCanUse(List<MvMusicInfo> mvMusicInfo){
        getMusicCanUseFromId(0, mvMusicInfo);
    }

    //填充歌词
    public static void getMusicLyric(MvMusicInfo mvMusicInfo){
        HttpUtil.sendOkHttpRequest(SERVER + MUSIC_LYRIC_URL + mvMusicInfo.getId(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("url", "出错");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.body() != null){
                        String lyric = new JSONObject(response.body().string()).getJSONObject("lrc").getString("lyric");
                        mvMusicInfo.setLyric(lyric);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //填充歌曲图片，带callback
    public static void getMusicPicture(MvMusicInfo mvMusicInfo, MusicCallBack musicCallBack){
        HttpUtil.sendOkHttpRequest(SERVER + MUSIC_DETAIL_URL + mvMusicInfo.getId(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("url", "出错");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.body() != null){
                        JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("songs");
                        //如果没有，直接返回
                        if(jsonArray.length() == 0){
                            return;
                        }
                        JSONObject songObject = jsonArray.getJSONObject(0);
                        String pictureUrl = songObject.getJSONObject("al").getString("picUrl");
                        mvMusicInfo.setPictureUrl(pictureUrl);
                        musicCallBack.onMusicInfoCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //填充歌词，带callback
    public static void getMusicLyric(MvMusicInfo mvMusicInfo, MusicCallBack musicCallBack){
        HttpUtil.sendOkHttpRequest(SERVER + MUSIC_LYRIC_URL + mvMusicInfo.getId(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("url", "出错");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.body() != null){
                        String lyric = new JSONObject(response.body().string()).getJSONObject("lrc").getString("lyric");
                        mvMusicInfo.setLyric(lyric);
                        musicCallBack.onMusicInfoCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //填充歌曲的版权信息
    private static void getMusicCanUseFromId(int i, List<MvMusicInfo> mvMusicInfoList){
        HttpUtil.sendOkHttpRequest(SERVER + MUSIC_CANUSE_URL + mvMusicInfoList.get(i).getId(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("url", "出错");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.body() != null){
                        String success = new JSONObject(response.body().string()).getString("success");
                        if(TextUtils.equals(success, "true")){
                            mvMusicInfoList.get(i).setCanUse(true);
                        }
                        //获取下一个视频的url
                        if(i + 1 < mvMusicInfoList.size()){
                            getMusicCanUseFromId(i + 1, mvMusicInfoList);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
