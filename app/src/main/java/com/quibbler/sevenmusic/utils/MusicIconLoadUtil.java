package com.quibbler.sevenmusic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.quibbler.sevenmusic.bean.MusicCoverJsonBean;
import com.quibbler.sevenmusic.bean.search.SearchSingerJsonBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_IMAGE;
import static com.quibbler.sevenmusic.bean.MusicURL.API_GET_SONG_DETAIL_AND_IMAGE;
import static com.quibbler.sevenmusic.bean.MusicURL.SEARCH_SINGER;
import static com.quibbler.sevenmusic.utils.CloseResourceUtil.closeInputAndOutput;
import static com.quibbler.sevenmusic.utils.CloseResourceUtil.closeReader;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      MusicIconLoadUtil
 * Description:    封面icon获取
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 17:18
 */
public class MusicIconLoadUtil {
    private Context mContext = null;
    private Handler mHandler = null;
    private String mSingerName;
    private String mID = null;
    private int mResource;

    private Bitmap mBitmap = null;

    public MusicIconLoadUtil(Context mContext) {
        this.mContext = mContext;
        mHandler = new Handler(mContext.getMainLooper());
    }

    public MusicIconLoadUtil found(String singerName) {
        mSingerName = singerName;
        return this;
    }

    public MusicIconLoadUtil setID(String id) {
        this.mID = id;
        return this;
    }

    public MusicIconLoadUtil placeHolder(int resource) {
        this.mResource = resource;
        return this;
    }

    @MainThread
    public void into(ImageView view) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                view.setImageResource(mResource);
            }
        });
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection = null;
                InputStream inputStream = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(SEARCH_SINGER + mSingerName);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(8000);
                    httpURLConnection.setReadTimeout(8000);
                    inputStream = httpURLConnection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder jsonData = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonData.append(line);
                    }
                    Gson gson = new Gson();
                    List<SearchSingerJsonBean.SearchSingerArtists> singerList = gson.fromJson(jsonData.toString(), SearchSingerJsonBean.class).getResult().getArtists();
                    if (singerList.size() != 0) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(mContext).load(singerList.get(0).getImg1v1Url()).placeholder(mResource).into(view);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeInputAndOutput(inputStream);
                    closeReader(reader);
                    CloseResourceUtil.disconnect(httpURLConnection);
                }
            }
        });
    }

    public void icon(ImageView view) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                view.setImageResource(mResource);
            }
        });
        if (mID == null) {
            return;
        }
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                BufferedReader bufferedReader = null;
                try {
                    URL musicDetailUrl = new URL(API_GET_SONG_DETAIL_AND_IMAGE + mID);
                    connection = (HttpURLConnection) musicDetailUrl.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestMethod("GET");
                    inputStream = connection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }
                    String jsonData = builder.toString();
                    MusicCoverJsonBean musicCoverJsonBean = new Gson().fromJson(jsonData, MusicCoverJsonBean.class);
                    String imageUrl = musicCoverJsonBean.getSongs().get(0).getAl().getPicUrl();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mContext).load(imageUrl).placeholder(mResource).into(view);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeInputAndOutput(inputStream);
                    closeReader(bufferedReader);
                    CloseResourceUtil.disconnect(connection);
                }
            }
        });
    }

    @WorkerThread
    public static Bitmap getBitmapFromServer(String uri) {
        Bitmap bitmap = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(uri);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setRequestMethod("GET");
            if (httpURLConnection.getResponseCode() != 200) {
                return null;
            }
            inputStream = httpURLConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (Exception e) {

        } finally {
            CloseResourceUtil.closeInputAndOutput(inputStream);
            CloseResourceUtil.disconnect(httpURLConnection);
        }

        return null;
    }

    @WorkerThread
    public static Bitmap loadBitmapFromCache(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }

    public static void saveBitmapToCache(Bitmap bitmap, String dirPath, String name) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dirPath + "/" + name);
                if (file.exists()) {
                    return;
                }
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, fileOutputStream);
                    fileOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    CloseResourceUtil.closeInputAndOutput(fileOutputStream);
                }
            }
        });
    }

    public static void getMusicIcon(String musicID) {
        if (musicID == null || "".equals(musicID)) {
            return;
        }
        File file = new File(SEVEN_MUSIC_IMAGE + "/" + musicID);
        if (file.exists()) {
            return;
        }

        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                //先解析歌曲详细详细
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                BufferedReader bufferedReader = null;
                try {
                    URL musicDetailUrl = new URL(API_GET_SONG_DETAIL_AND_IMAGE + musicID);
                    connection = (HttpURLConnection) musicDetailUrl.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestMethod("GET");
                    if (connection.getResponseCode() != 200) {
                        return;
                    }
                    inputStream = connection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }
                    //拿到歌曲封面地址，去获取歌曲封面
                    String jsonData = builder.toString();
                    MusicCoverJsonBean musicCoverJsonBean = new Gson().fromJson(jsonData, MusicCoverJsonBean.class);
                    if (musicCoverJsonBean.getSongs().size() == 0) {
                        return;
                    }
                    String imageUrl = musicCoverJsonBean.getSongs().get(0).getAl().getPicUrl();
                    if ("".equals(imageUrl) || imageUrl == null) {
                        return;
                    }
                    connection.disconnect();
                    inputStream.close();
                    URL musicImageURL = new URL(imageUrl);
                    connection = (HttpURLConnection) musicImageURL.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestMethod("GET");
                    if (connection.getResponseCode() != 200) {
                        return;
                    }
                    inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    saveBitmapToCache(bitmap, SEVEN_MUSIC_IMAGE, musicID);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    CloseResourceUtil.closeInputAndOutput(inputStream);
                    CloseResourceUtil.disconnect(connection);
                }
            }
        });
    }

}