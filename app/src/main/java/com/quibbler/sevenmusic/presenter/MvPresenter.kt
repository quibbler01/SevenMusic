package com.quibbler.sevenmusic.presenter;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.callback.MvCollectCallback;
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider;
import com.quibbler.sevenmusic.utils.HttpUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
  *
  * Package:        com.quibbler.sevenmusic.presenter
  * ClassName:      MvPresenter
  * Description:    关于mv的presenter
  * Author:         lishijun
  * CreateDate:     2019/9/27 10:04
 */
public class MvPresenter {

    private static final String SERVER = "http://114.116.128.229:3000";

    private static final String MV_DETAIL_URL = "/mv/detail?mvid=";

    private static final String MV_URL_URL = "/mv/url?id=";

    //获取mv的详细信息
    public static void getMvInfo(MvInfo mvInfo, MusicCallBack musicCallBack){
        HttpUtil.sendOkHttpRequest(SERVER + MV_DETAIL_URL + mvInfo.getId(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if(response.body() != null){
                    try {
                        String jsonString = new JSONObject(response.body().string()).getJSONObject("data").toString();
                        Log.d("QUIBBLER_TAG", "" + jsonString);
                        MvInfo tempMvInfo = new Gson().fromJson(jsonString, MvInfo.class);
                        tempMvInfo.setUrl((tempMvInfo.getMvUrl()).getUrl());
                        mvInfo.copy(tempMvInfo);
                        musicCallBack.onMusicInfoCompleted();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("url", "出错");
            }
        });
    }

    public static void getMvUrlList(List<MvInfo> mvInfoList){
        getMvUrlListFromDetail(0, mvInfoList);
    }

    //通过detail递归获取mv的url
    private static void getMvUrlListFromDetail(int i, List<MvInfo> mvInfoList){
        HttpUtil.sendOkHttpRequest(SERVER + MV_DETAIL_URL + mvInfoList.get(i).getId(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if(response.body() != null){
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("data");
                        String mvUrl = jsonObject.getJSONObject("brs").optString("480");
                        if(TextUtils.equals(mvUrl, "")){
                            mvUrl = jsonObject.getJSONObject("brs").optString("240");
                        }
                        mvInfoList.get(i).setUrl(mvUrl);
                        //获取下一个视频的url
                        if(i + 1 < mvInfoList.size()){
                            getMvUrlListFromDetail(i + 1, mvInfoList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("url", "出错");
            }
        });
    }

    //通过url递归获取mv的url
    private static void getMvUrlListFromUrl(int i, List<MvInfo> mvInfoList){
        HttpUtil.sendOkHttpRequest(SERVER + MV_URL_URL + mvInfoList.get(i).getId(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("data");
                    mvInfoList.get(i).setUrl(jsonObject.getString("url"));
                    //获取下一个视频的url
                    if(i + 1 < mvInfoList.size()){
                        getMvUrlListFromUrl(i + 1, mvInfoList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("url", "出错");
            }
        });
    }

    public static void isMvCollected(String mvId, MvCollectCallback mvCollectCallback){
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                try {
                    cursor = MusicApplication.getContext().getContentResolver().query(MusicContentProvider.MV_URL,
                            null, "id = ?", new String[]{mvId}, null);
                    if(cursor == null || cursor.getCount() == 0){
                        mvCollectCallback.notCollected();
                    }else{
                        mvCollectCallback.isCollected();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(cursor != null){
                        cursor.close();
                    }
                }
            }
        });
    }

    public static void collectMv(MvInfo mvInfo, MvCollectCallback mvCollectCallback){
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                try{
                    int id = mvInfo.getId();
                    cursor = MusicApplication.getContext().getContentResolver().query(MusicContentProvider.MV_URL,
                            null, "id = ?", new String[]{String.valueOf(id)}, null);
                    if(cursor == null || cursor.getCount() == 0){
                        ContentValues values = new ContentValues();
                        values.put("id", id);
                        values.put("name", mvInfo.getName());
                        values.put("pictureurl", mvInfo.getPictureUrl());
                        MusicApplication.getContext().getContentResolver().insert(MusicContentProvider.MV_URL, values);
                        mvCollectCallback.isCollected();
                    }else{
                        MusicApplication.getContext().getContentResolver().delete(MusicContentProvider.MV_URL,
                                "id = ?", new String[]{String.valueOf(mvInfo.getId())});
                        mvCollectCallback.notCollected();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(cursor != null){
                        cursor.close();
                    }
                }
            }
        });
    }
}
