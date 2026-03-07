package com.quibbler.sevenmusic.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.utils.BitmapUtils;
import com.quibbler.sevenmusic.utils.HttpUtil;
import com.quibbler.sevenmusic.utils.HttpsUrlConverter;
import com.quibbler.sevenmusic.utils.IRequestCallback;
import com.quibbler.sevenmusic.utils.MD5Util;
import com.quibbler.sevenmusic.utils.MusicThreadPool;
import com.quibbler.sevenmusic.utils.ThreadDispatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.Call;

public class ImageDealer {
    private static final String TAG = "ImageDealer";
    public static final String IMG_CACHE_PATH = MusicApplication.getContext().getResources().getString(R.string.image_cache_folder);

    //除了网络请求用url，其他地方都用Md5Str
    private String mUrl;
    private String mMd5Str;
    private Context mContext;
    private ImageView mImageView;

    //本地需要设置内存上限
    private static long sMaxMemory = Runtime.getRuntime().maxMemory() / 16;

    //占位图片
    private boolean mUsePlaceHolder = false;

    private static final int STYLE_ORIGIN = 0; //原始图片
    private static final int STYLE_ROUND = 1; //圆角图片
    private static final int STYLE_CIRCLE = 2; // 圆形图片
    //默认是原始图片
    private int mStyle = STYLE_ORIGIN;

    //key为图片的完整url，value为对应的bitmap
    private static LruCache<String, Bitmap> sLruCache = new LruCache<String, Bitmap>((int) sMaxMemory) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public static int getPicLruMemory() {
        if (sLruCache == null) {
            return 0;
        }
        return sLruCache.size();
    }

    /**
     * 获取上下文
     *
     * @param context
     * @return
     */
    public ImageDealer with(Context context) {
        mContext = context;
        return this;
    }

    /**
     * 获取图片下载地址
     *
     * @param url
     * @return
     */
    public ImageDealer load(String url) {
        mUrl = url;
        mMd5Str = MD5Util.encodeStr2MD5(url);
        return this;
    }

    /**
     * @param style
     * @return
     */
    public ImageDealer imageStyle(int style) {
        mStyle = style;
        return this;
    }

    /**
     * 是否使用占位图片
     */
    public ImageDealer placeholder(boolean b) {
        mUsePlaceHolder = b;
        return this;
    }

    /**
     * 获取到bitmap对象后，对imageView进行设置
     *
     * @param imageView
     * @param bitmap
     */
    @UiThread
    private void setImgSrcInUiThread(ImageView imageView, Bitmap bitmap) {
        //先比较tag，否则由于RecyclerView重用，会导致加载错误
        Bitmap originBitmap;
        if (!mMd5Str.equals((String) imageView.getTag())) {
            originBitmap = getBitmapFromMap((String) imageView.getTag());
        } else {
            originBitmap = bitmap;
        }
        if (originBitmap == null) {
            return;
        }

        switch (mStyle) {
            case STYLE_ORIGIN:
                mImageView.setImageBitmap(originBitmap);
                break;

            case STYLE_ROUND:
                //展示圆角图片
                //如果圆角半径设为固定数字，由于每张图片的原始实际大小不一样，每张图的圆角半径与图片长宽比例不同，把图片放入同样大小的imageV后，最终显示出的图片圆角大小不同。
                int height = originBitmap.getHeight();
                int width = originBitmap.getWidth();
                final int minDrawDimen = Math.min(width, height);
                //根据图片比例设置圆角半径
                float radius = 0.1f * minDrawDimen / 2;

                final RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(Resources.getSystem(), originBitmap);
                roundedBitmapDrawable.setCornerRadius(radius);

                mImageView.setImageDrawable(roundedBitmapDrawable);
                break;

            case STYLE_CIRCLE:
                //展示圆形图片
                mImageView.setImageBitmap(BitmapUtils.makeRoundCorner(originBitmap));
                break;

            default:
                break;
        }
    }

    /**
     * 从内存中查找缓存
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapFromMap(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return sLruCache.get(url);
    }

    private void setBitmapIntoMap(String url, Bitmap bitmap) {
        if (url == null || bitmap == null) {
            return;
        }
        sLruCache.put(url, bitmap);
    }

    /**
     * 从本地文件读取bitmap
     *
     * @param fileName
     * @return
     */
    @WorkerThread
    private Bitmap getBitmapFromFileInThread(String fileName) {
        FileInputStream fileInputStream = null;
        try {
            File file = new File(IMG_CACHE_PATH, fileName);
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                return bitmap;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "get file not found! file name is :" + fileName);
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "exception happened when close fileInputStream");
                e.printStackTrace();
            }
        }
        return null;
    }

    //异步执行即可
    @WorkerThread
    private void setBitmapIntoFile(String fileName, Bitmap bitmap) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fileOutputStream = null;
                try {
                    File dir = new File(IMG_CACHE_PATH);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(IMG_CACHE_PATH + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    fileOutputStream = new FileOutputStream(file);
                    //对图片进行压缩后，再保存到本地。压缩成原品质的quality%。
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, fileOutputStream);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "set file not found! file name is:" + fileName);
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "write file failed! file name is:" + fileName);
                    e.printStackTrace();
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "exception happened when close fileInputStream");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void into(ImageView imageView) {
        mImageView = imageView;
        if (TextUtils.isEmpty(mUrl)) {
            Log.d(TAG, "url is empty!");
            return;
        }
        Log.d(TAG, "url is:" + mUrl);

        //给imageView设置tag，下载完成后比较tag，一致再加载图片，否则由于RecyclerView重用，可能出现图片错乱
        mImageView.setTag(mMd5Str);

        //占位图片
        if (mUsePlaceHolder && imageView.getDrawable() != null) {
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.image_placeholder_color));
        }

        //三级缓存
        //先在内存中查找，若内存中已有
        if (sLruCache.get(mMd5Str) != null) {
            setImgSrcInUiThread(imageView, getBitmapFromMap(mMd5Str));
            return;
        }
        //内存中没有，再去本地查找，若本地已有，需要写入内存
        ThreadDispatcher.getInstance().runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getBitmapFromFileInThread(mMd5Str);
                if (bitmap != null) {
                    ThreadDispatcher.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setImgSrcInUiThread(imageView, bitmap);
                        }
                    });
                    setBitmapIntoMap(mMd5Str, bitmap);
                    return;
                }
            }
        });

        //若内存和本地文件都没有，则进行网络请求，请求完毕后写入内存和本地文件
        String httpsUrl = HttpsUrlConverter.httpToHttps(mUrl);
        HttpUtil.sendOkHttpRequest(httpsUrl, mContext, new IRequestCallback() {
            @Override
            public void onResponse(Bitmap bitmap) {
                if (bitmap == null) {
                    Log.d(TAG, "decodeStream to bitmap failed! url is:" + httpsUrl);
                    return;
                }
                ThreadDispatcher.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setImgSrcInUiThread(imageView, bitmap);
                    }
                });
                setBitmapIntoMap(mMd5Str, bitmap);
                setBitmapIntoFile(mMd5Str, bitmap);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "request fail! url is :" + mUrl);
            }
        });
    }

    public void into(ImageView imageView, ImageDownloadPresenter.ResourceCallback resourceCallback) {
        mImageView = imageView;
        if (TextUtils.isEmpty(mUrl)) {
            Log.d(TAG, "url is empty!");
            return;
        }
        Log.d(TAG, "url is:" + mUrl);

        //给imageView设置tag，下载完成后比较tag，一致再加载图片，否则由于RecyclerView重用，可能出现图片错乱
        mImageView.setTag(mMd5Str);

        //占位图片
        if (mUsePlaceHolder && imageView.getDrawable() != null) {
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.image_placeholder_color));
        }

        //三级缓存
        //先在内存中查找，若内存中已有
        if (sLruCache.get(mMd5Str) != null) {
            setImgSrcInUiThread(imageView, getBitmapFromMap(mMd5Str));
            resourceCallback.onResourceReady(getBitmapFromMap(mMd5Str));
            return;
        }
        //内存中没有，再去本地查找，若本地已有，需要写入内存
        ThreadDispatcher.getInstance().runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getBitmapFromFileInThread(mMd5Str);
                if (bitmap != null) {
                    ThreadDispatcher.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setImgSrcInUiThread(imageView, bitmap);
                            resourceCallback.onResourceReady(bitmap);
                        }
                    });
                    setBitmapIntoMap(mMd5Str, bitmap);
                    return;
                } else {
                    //若内存和本地文件都没有，则进行网络请求，请求完毕后写入内存和本地文件
                    String httpsUrl = HttpsUrlConverter.httpToHttps(mUrl);
                    HttpUtil.sendOkHttpRequest(httpsUrl, mContext, new IRequestCallback() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            if (bitmap == null) {
                                Log.d(TAG, "decodeStream to bitmap failed! url is:" + httpsUrl);
                                return;
                            }
                            ThreadDispatcher.getInstance().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setImgSrcInUiThread(imageView, bitmap);
                                    resourceCallback.onResourceReady(bitmap);
                                }
                            });
                            setBitmapIntoMap(mMd5Str, bitmap);
                            setBitmapIntoFile(mMd5Str, bitmap);
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d(TAG, "request fail! url is :" + mUrl);
                        }
                    });
                }
            }
        });
    }


    /**
     * 获取图片缓存，单位M
     * @return
     */
    public static String getPictureLruCache() {
        if (sLruCache == null) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String pictureCache = df.format((double) sLruCache.size() / 1048576) + "M";
        return pictureCache;
    }
}