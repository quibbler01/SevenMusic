package com.quibbler.sevenmusic.utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.quibbler.sevenmusic.bean.MusicURL.MUSIC_ALBUM_URI;


/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      DecodeMusicManager
 * Description:    获取音乐封面专辑根据传入的是专辑AlbumID还是封面url
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 9:38
 */
public class DecodeMusicManager {
    public static final int ROUND = 0;
    public static final int RECT = 1;
    public static final int CYCLE = 2;

    private Context mContext;
    private int mAlbumID = -1;
    private String mUrl = null;
    private Handler mMainHandler;
    private int mPlaceHolderSourceID;
    private int mRoundType = CYCLE;
    Bitmap bitmap = null;

    public DecodeMusicManager(Context mContext) {
        this.mContext = mContext;
        mMainHandler = new Handler(mContext.getMainLooper());
    }

    public DecodeMusicManager load(int albumID) {
        mAlbumID = albumID;
        return this;
    }

    public DecodeMusicManager load(String url) {
        mUrl = url;
        return this;
    }

    public DecodeMusicManager placeHolder(int source) {
        mPlaceHolderSourceID = source;
        return this;
    }

    public DecodeMusicManager radiusType(int type) {
        mRoundType = type;
        return this;
    }

    public void into(ImageView view) {
        view.setBackgroundResource(mPlaceHolderSourceID);
        if (mAlbumID > 0) {
            MusicThreadPool.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = null;
                    try {
                        Uri uri = Uri.parse(MUSIC_ALBUM_URI + "/" + mAlbumID);
                        cursor = mContext.getContentResolver().query(uri, new String[]{"album_art"}, null, null, null);
                        String albumPath = null;
                        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
                            cursor.moveToNext();
                            albumPath = cursor.getString(0);
                            if (albumPath != null) {
                                bitmap = BitmapFactory.decodeFile(albumPath);
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        view.setImageBitmap(bitmap);
                                    }
                                });
                            }
                        }
//                    下面这种获取音乐封面的方法似乎不能成功,对音频可能无效
//                    Log.e("DecodeMusicImageUtils", "cursor count\t" + mUrl);
//                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//                    mediaMetadataRetriever.setDataSource(mUrl);
//                    byte[] imageCover = mediaMetadataRetriever.getEmbeddedPicture();
//                    bitmap = BitmapFactory.decodeByteArray(imageCover, 0, imageCover.length);
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap != null) {
                                    view.setImageBitmap(bitmap);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            });
        } else if (mUrl != null) {
            HttpUtil.sendOkHttpRequest(mUrl, new Callback() {

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    InputStream inputStream = null;
                    inputStream = response.body().byteStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(Resources.getSystem(), bitmap);
                    switch (mRoundType) {
                        case ROUND:
                            roundedBitmapDrawable.setCornerRadius(0.2f * bitmap.getWidth());
                            break;
                        case RECT:
                            break;
                        default:
                            roundedBitmapDrawable.setCircular(true);
                            break;
                    }
                    inputStream.close();
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageDrawable(roundedBitmapDrawable);
                        }
                    });
                }
            });
        }

    }

}
