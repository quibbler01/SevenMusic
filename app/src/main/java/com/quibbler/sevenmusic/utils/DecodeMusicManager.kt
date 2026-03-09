package com.quibbler.sevenmusic.utils

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.quibbler.sevenmusic.bean.MusicURL
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.io.InputStream

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      DecodeMusicManager
 * Description:    获取音乐封面专辑根据传入的是专辑AlbumID还是封面url
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 9:38
 */
class DecodeMusicManager(mContext: Context) {
    private val mContext: Context
    private var mAlbumID = -1
    private var mUrl: String? = null
    private val mMainHandler: Handler
    private var mPlaceHolderSourceID = 0
    private var mRoundType: Int = CYCLE
    var bitmap: Bitmap? = null

    init {
        this.mContext = mContext
        mMainHandler = Handler(mContext.getMainLooper())
    }

    fun load(albumID: Int): DecodeMusicManager {
        mAlbumID = albumID
        return this
    }

    fun load(url: String?): DecodeMusicManager {
        mUrl = url
        return this
    }

    fun placeHolder(source: Int): DecodeMusicManager {
        mPlaceHolderSourceID = source
        return this
    }

    fun radiusType(type: Int): DecodeMusicManager {
        mRoundType = type
        return this
    }

    fun into(view: ImageView) {
        view.setBackgroundResource(mPlaceHolderSourceID)
        if (mAlbumID > 0) {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    var cursor: Cursor? = null
                    try {
                        val uri = Uri.parse(MusicURL.MUSIC_ALBUM_URI + "/" + mAlbumID)
                        cursor = mContext.getContentResolver()
                            .query(uri, arrayOf<String>("album_art"), null, null, null)
                        var albumPath: String? = null
                        if (cursor!!.getCount() > 0 && cursor.getColumnCount() > 0) {
                            cursor.moveToNext()
                            albumPath = cursor.getString(0)
                            if (albumPath != null) {
                                bitmap = BitmapFactory.decodeFile(albumPath)
                                mMainHandler.post(object : Runnable {
                                    override fun run() {
                                        view.setImageBitmap(bitmap)
                                    }
                                })
                            }
                        }
                        //                    下面这种获取音乐封面的方法似乎不能成功,对音频可能无效
//                    Log.e("DecodeMusicImageUtils", "cursor count\t" + mUrl);
//                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//                    mediaMetadataRetriever.setDataSource(mUrl);
//                    byte[] imageCover = mediaMetadataRetriever.getEmbeddedPicture();
//                    bitmap = BitmapFactory.decodeByteArray(imageCover, 0, imageCover.length);
                        mMainHandler.post(object : Runnable {
                            override fun run() {
                                if (bitmap != null) {
                                    view.setImageBitmap(bitmap)
                                }
                            }
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (cursor != null) {
                            cursor.close()
                        }
                    }
                }
            })
        } else if (mUrl != null) {
            HttpUtil.sendOkHttpRequest(mUrl, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    var inputStream: InputStream? = null
                    inputStream = response.body!!.byteStream()
                    bitmap = BitmapFactory.decodeStream(inputStream)
                    val roundedBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(Resources.getSystem(), bitmap)
                    when (mRoundType) {
                        ROUND -> roundedBitmapDrawable.setCornerRadius(0.2f * bitmap!!.getWidth())
                        RECT -> {}
                        else -> roundedBitmapDrawable.setCircular(true)
                    }
                    inputStream.close()
                    mMainHandler.post(object : Runnable {
                        override fun run() {
                            view.setImageDrawable(roundedBitmapDrawable)
                        }
                    })
                }
            })
        }
    }

    companion object {
        const val ROUND: Int = 0
        const val RECT: Int = 1
        const val CYCLE: Int = 2
    }
}
