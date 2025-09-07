package com.quibbler.sevenmusic.service;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.quibbler.sevenmusic.bean.search.SearchSingerJsonBean;
import com.quibbler.sevenmusic.utils.CloseResourceUtil;
import com.quibbler.sevenmusic.utils.MusicIconLoadUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_SINGER;
import static com.quibbler.sevenmusic.bean.MusicURL.SEARCH_SINGER;
import static com.quibbler.sevenmusic.utils.CloseResourceUtil.closeInputAndOutput;
import static com.quibbler.sevenmusic.utils.CloseResourceUtil.closeReader;

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      LoadSingerThumbnailAsyncTask
 * Description:    异步加载图片，缓存
 * Author:         zhaopeng
 * CreateDate:     2019/10/30 16:16
 */
public class LoadSingerThumbnailAsyncTask extends AsyncTask<Object, Void, Bitmap> {
    private String mName;
    private WeakReference<ImageView> mImageView;

    public LoadSingerThumbnailAsyncTask(ImageView imageView) {
        this.mImageView = new WeakReference<ImageView>(imageView);
    }

    public LoadSingerThumbnailAsyncTask() {

    }

    @Override
    protected void onPreExecute() {
        File path = new File(SEVEN_MUSIC_SINGER);
        if (!path.exists()) {
            path.mkdirs();
        }
    }

    @Override
    protected Bitmap doInBackground(Object[] args) {
        if (args.length <= 0) {
            return null;
        }
        mName = (String) args[0];
        if (new File(SEVEN_MUSIC_SINGER + "/" + mName).exists()) {
            return null;
        }

        Bitmap bitmap = null;
        bitmap = MusicIconLoadUtil.loadBitmapFromCache(SEVEN_MUSIC_SINGER + "/" + mName);
        if (bitmap != null) {
            return bitmap;
        }
        String url = getSingerCoverUrl(mName);
        bitmap = MusicIconLoadUtil.getBitmapFromServer(url);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (mImageView != null && mImageView.get() != null) {
            mImageView.get().setImageBitmap(bitmap);
        }
        MusicIconLoadUtil.saveBitmapToCache(bitmap, SEVEN_MUSIC_SINGER, mName);
    }

    public static String getSingerCoverUrl(String name) {
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(SEARCH_SINGER + name);
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
            return singerList.get(0).getImg1v1Url();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeInputAndOutput(inputStream);
            closeReader(reader);
            CloseResourceUtil.disconnect(httpURLConnection);
        }
        return null;
    }
}
