package com.quibbler.sevenmusic.presenter;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Package:        com.quibbler.sevenmusic.presenter
 * ClassName:      ImageDownloadPresenter
 * Description:    根据图片的url下载该图片，并传入需要展现的imageView中。模仿glide写法。三级缓存。
 * Author:         yanwuyang
 * CreateDate:     2019/10/17 11:09
 */
public class ImageDownloadPresenter {
    private static final String TAG = "ImageDownloadPresenter";

    private static volatile ImageDownloadPresenter sInstance;

    public static final int STYLE_ORIGIN = 0; //原始图片
    public static final int STYLE_ROUND = 1; //圆角图片
    public static final int STYLE_CIRCLE = 2; // 圆形图片

    private ImageDownloadPresenter() {

    }

    public static ImageDownloadPresenter getInstance() {
        if (sInstance == null) {
            synchronized (ImageDownloadPresenter.class) {
                if (sInstance == null) {
                    sInstance = new ImageDownloadPresenter();
                }
            }
        }
        return sInstance;
    }

    public ImageDealer with(Context context) {
        return new ImageDealer().with(context);
    }

    public interface ResourceCallback<T>{
        void onResourceReady(T t);
    }

}
