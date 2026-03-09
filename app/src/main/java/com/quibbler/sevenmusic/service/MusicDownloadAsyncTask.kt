package com.quibbler.sevenmusic.service;


import android.os.AsyncTask;

import com.google.gson.Gson;
import com.quibbler.sevenmusic.Constant;
import com.quibbler.sevenmusic.bean.MusicDownloadUrlJsonBean;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.listener.MusicDownloadListener;
import com.quibbler.sevenmusic.utils.CloseResourceUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.quibbler.sevenmusic.bean.MusicURL.API_MUSIC_DOWNLOAD_URL;

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      MusicPlayerService
 * Description:    下载音乐任务: MusicInfo:传入对象作为参数   Integer  :作为参数显示执行进度    Boolean  :反馈执行结果
 * Author:         zhaopeng
 * CreateDate:     2019/10/11 21:39
 */
public class MusicDownloadAsyncTask extends AsyncTask<MusicInfo, Integer, Boolean> {
    public static String SAVE_PATH = Constant.EXTERNAL + "/sevenMusic/music";

    private MusicDownloadListener mListener;

    public MusicDownloadAsyncTask(MusicDownloadListener listener) {
        this.mListener = listener;
    }

    /**
     * 该方法中所有代码在子线程中执行，耗时任务
     *
     * @param musicInfos 传入的参数类型就是定义的第一个类型
     * @return 返回值就是定义的Boolean类型
     */
    @Override
    protected Boolean doInBackground(MusicInfo[] musicInfos) {
        for (MusicInfo musicInfo : musicInfos) {
            MusicDownloadUrlJsonBean.Data musicOnlineData = null;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(API_MUSIC_DOWNLOAD_URL + musicInfo.getId() + "&br=128000").build();
                Response response = client.newCall(request).execute();
                if (response.code() != 200 || response.body() == null) {
                    return false;
                }
                String json = response.body().string();
                musicOnlineData = (new Gson().fromJson(json, MusicDownloadUrlJsonBean.class)).getData().get(0);
                if (musicOnlineData == null || musicOnlineData.getUrl() == null || "".equals(musicOnlineData.getUrl())) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(musicOnlineData.getUrl()).build();
            InputStream inputMusicStream = null;
            FileOutputStream musicFileOutputStream = null;
            try {
                Response response = client.newCall(request).execute();
                ResponseBody body = response.body();
                if (body == null) {
                    return false;
                }
                long total = body.contentLength();
                inputMusicStream = body.byteStream();

                File musicFile = new File(SAVE_PATH, musicInfo.getId() + ".mp3");
                musicFileOutputStream = new FileOutputStream(musicFile);

                byte[] buffer = new byte[1024 * 4];
                long sum = 0;
                int current = 0;
                while ((current = inputMusicStream.read(buffer)) != -1) {
                    musicFileOutputStream.write(buffer, 0, current);
                    sum += current;
                    mListener.onProgress((int) (sum * 100.00 / total), musicInfo.getMusicSongName());
                }
                musicFileOutputStream.flush();
            } catch (Exception e) {
                return false;
            } finally {
                CloseResourceUtil.closeInputAndOutput(inputMusicStream);
                CloseResourceUtil.closeInputAndOutput(musicFileOutputStream);
            }
        }
        return true;
    }

    /**
     * 在后台任务开始执行前调用
     * 检查储存下载歌曲文件的目录是否存在
     */
    @Override
    protected void onPreExecute() {
        File path = new File(SAVE_PATH);
        if (!path.exists()) {
            path.mkdir();
        }
    }

    /**
     * @param result
     */
    @Override
    protected void onPostExecute(Boolean result) {
        mListener.isSuccess(result);
    }

    /**
     * 后台任务调用publishProgress(Progress...)方法后，该方法会被调用
     * 不建议使用这个方法，更新进度，有延迟
     *
     * @param values
     */
    @Override
    protected void onProgressUpdate(Integer[] values) {
    }
}

