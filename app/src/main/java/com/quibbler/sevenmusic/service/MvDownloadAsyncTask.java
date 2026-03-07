package com.quibbler.sevenmusic.service;

import android.os.AsyncTask;
import android.os.Environment;

import com.quibbler.sevenmusic.listener.MvDownloadListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
  *
  * Package:        com.quibbler.sevenmusic.service
  * ClassName:      MvDownloadAsyncTask
  * Description:    mv下载异步任务
  * Author:         lishijun
  * CreateDate:     2019/9/24 10:45
 */
public class MvDownloadAsyncTask extends AsyncTask<String, Integer, Integer> {

    public static final String SAVE_PATH = "/storage/emulated/0/sevenMusic/mv";

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED= 2;
    public static final int TYPE_CANCELED = 3;

    private boolean mIsPaused = false;
    private boolean mIsCanceled = false;
    private int mLastProgress;

    private MvDownloadListener mMvDownloadListener;

    public MvDownloadAsyncTask(MvDownloadListener mMvDownloadListener) {
        this.mMvDownloadListener = mMvDownloadListener;
    }

    public void setMvDownloadListener(MvDownloadListener mvDownloadListener) {
        this.mMvDownloadListener = mvDownloadListener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        File file = null;
        Response response = null;
        try {
            long downLoadLength = 0;//记录已下载的文件长度
            String downLoadUrl = strings[0];
            //获取文件名称
            String fileName = strings[1] + ".mp4";
            //mv保存目录
            File newFile = new File(SAVE_PATH);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            file = new File(SAVE_PATH, fileName);
            if (file.exists()) {
                downLoadLength = file.length();
            }
            long contentLength = getContentLength(downLoadUrl);
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downLoadLength) {
                //如果已下载的字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downLoadLength + "-") //断点继续下载
                    .url(downLoadUrl)
                    .build();
            response = client.newCall(request).execute();
            if (response.body() != null) {
                inputStream = response.body().byteStream();
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(downLoadLength);//跳过已下载的字节
                byte[] bytes = new byte[1024];
                int total = 0;
                int len;
                //inputStream.read(bytes)--读取多个字节写到bytes中S
                while ((len = inputStream.read(bytes)) != -1) {
                    if (mIsCanceled) {
                        return TYPE_CANCELED;
                    } else if (mIsPaused) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        randomAccessFile.write(bytes, 0, len);
                        int progress = (int)((total + downLoadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (mIsCanceled && file != null) {
                    file.delete();
                }
                if(response != null){
                    response.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_SUCCESS;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > mLastProgress) {
            mMvDownloadListener.onProgress(progress);
            mLastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer) {
            case TYPE_SUCCESS: {
                mMvDownloadListener.onSuccess();
                break;
            }
            case TYPE_FAILED: {
                mMvDownloadListener.onFailed();
                break;
            }
            case TYPE_PAUSED: {
                mMvDownloadListener.onPaused();
                break;
            }
            case TYPE_CANCELED: {
                mMvDownloadListener.onCanceled();
                break;
            }
            default:break;
        }
    }

    /**
     * 暂停下载
     */
    public void pauseDownLoad() {
        this.mIsPaused = true;
    }

    /**
     * 取消下载
     */
    public void cancelDownLoad() {
        this.mIsCanceled = true;
    }

    /**
     * 获取文件长度
     *
     * @param downLoadUrl
     * @return
     */
    private long getContentLength(String downLoadUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downLoadUrl).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                try {
                    long contentLength = response.body().contentLength();
                    return contentLength;
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    response.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
