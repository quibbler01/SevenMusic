package com.quibbler.sevenmusic.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Package:        com.quibbler.sevenmusic.util
 * ClassName:      HttpUtil
 * Description:    使用okttp访问网络
 * Author:         lishijun
 * CreateDate:     2019/9/17 17:15
 */
public class HttpUtil {
    private static final String TAG = "HttpUtil";

    private static final OkHttpClient client = new OkHttpClient();

    //调用自带的callback,回调时需要切换回主线程
    public static void sendOkHttpRequest(String address, Callback callback) {
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    //调用自己封装的callback，回调时已经在主线程
    public static void sendOkHttpRequest(String address, final Activity context, final ICallback callback) {
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (context == null)
                    return;
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (context == null)
                    return;
                final String responseText = response.body().string();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(responseText);
                    }
                });
            }
        });
    }

    //使用原生的httpconnection
    public static void sendHttpRequest(String address, ICallback callback) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        callback.onResponse(response.toString());
                    } else {
                        callback.onFailure();
                    }
                } catch (IOException e) {
                    callback.onFailure();
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }

    //调用自己封装的callback，回调时已经在主线程。context类型不限于Activity
    public static void sendOkHttpRequest(String address, Context context, final ICallback callback) {
        Handler handler = new Handler(context.getMainLooper());
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (context == null)
                    return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (context == null)
                    return;
                final String responseText = response.body().string();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(responseText);
                    }
                });
            }
        });
    }

    //调用自己封装的callback，回调时已经在主线程。context类型不限于Activity。返回的是inputStream。
    public static void sendOkHttpRequest(String address, Context context, final IRequestCallback callback) {
        Handler handler = new Handler(context.getMainLooper());
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (context == null)
                    return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(call, e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (context == null)
                    return;
                final InputStream inputStream = response.body().byteStream();
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Bitmap bitmap = BitmapUtils.decodeLowQualityBitmap(inputStream, 400, 200);
                response.close();
                if (bitmap == null) {
                    Log.d(TAG, "bitmap is null!");
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(bitmap);
                    }
                });
            }
        });
    }
}
