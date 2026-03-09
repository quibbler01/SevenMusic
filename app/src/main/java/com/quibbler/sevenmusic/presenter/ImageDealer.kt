package com.quibbler.sevenmusic.presenter

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.utils.BitmapUtils
import com.quibbler.sevenmusic.utils.HttpUtil
import com.quibbler.sevenmusic.utils.HttpsUrlConverter
import com.quibbler.sevenmusic.utils.IRequestCallback
import com.quibbler.sevenmusic.utils.MD5Util
import com.quibbler.sevenmusic.utils.MusicThreadPool
import com.quibbler.sevenmusic.utils.ThreadDispatcher
import okhttp3.Call
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import kotlin.math.min

class ImageDealer {
    //除了网络请求用url，其他地方都用Md5Str
    private var mUrl: String? = null
    private var mMd5Str: String? = null
    private var mContext: Context? = null
    private var mImageView: ImageView? = null

    //占位图片
    private var mUsePlaceHolder = false

    //默认是原始图片
    private var mStyle: Int = STYLE_ORIGIN

    /**
     * 获取上下文
     * 
     * @param context
     * @return
     */
    fun with(context: Context): ImageDealer {
        mContext = context
        return this
    }

    /**
     * 获取图片下载地址
     * 
     * @param url
     * @return
     */
    fun load(url: String?): ImageDealer {
        mUrl = url
        mMd5Str = MD5Util.encodeStr2MD5(url)
        return this
    }

    /**
     * @param style
     * @return
     */
    fun imageStyle(style: Int): ImageDealer {
        mStyle = style
        return this
    }

    /**
     * 是否使用占位图片
     */
    fun placeholder(b: Boolean): ImageDealer {
        mUsePlaceHolder = b
        return this
    }

    /**
     * 获取到bitmap对象后，对imageView进行设置
     * 
     * @param imageView
     * @param bitmap
     */
    @UiThread
    private fun setImgSrcInUiThread(imageView: ImageView, bitmap: Bitmap?) {
        //先比较tag，否则由于RecyclerView重用，会导致加载错误
        val originBitmap: Bitmap?
        if (mMd5Str != imageView.getTag() as String?) {
            originBitmap = getBitmapFromMap(imageView.getTag() as String?)
        } else {
            originBitmap = bitmap
        }
        if (originBitmap == null) {
            return
        }

        when (mStyle) {
            STYLE_ORIGIN -> mImageView!!.setImageBitmap(originBitmap)
            STYLE_ROUND -> {
                //展示圆角图片
                //如果圆角半径设为固定数字，由于每张图片的原始实际大小不一样，每张图的圆角半径与图片长宽比例不同，把图片放入同样大小的imageV后，最终显示出的图片圆角大小不同。
                val height = originBitmap.getHeight()
                val width = originBitmap.getWidth()
                val minDrawDimen = min(width, height)
                //根据图片比例设置圆角半径
                val radius = 0.1f * minDrawDimen / 2

                val roundedBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(Resources.getSystem(), originBitmap)
                roundedBitmapDrawable.setCornerRadius(radius)

                mImageView!!.setImageDrawable(roundedBitmapDrawable)
            }

            STYLE_CIRCLE ->                 //展示圆形图片
                mImageView!!.setImageBitmap(BitmapUtils.makeRoundCorner(originBitmap))

            else -> {}
        }
    }

    /**
     * 从内存中查找缓存
     * 
     * @param url
     * @return
     */
    private fun getBitmapFromMap(url: String?): Bitmap? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        return sLruCache!!.get(url)
    }

    private fun setBitmapIntoMap(url: String?, bitmap: Bitmap?) {
        if (url == null || bitmap == null) {
            return
        }
        sLruCache!!.put(url, bitmap)
    }

    /**
     * 从本地文件读取bitmap
     * 
     * @param fileName
     * @return
     */
    @WorkerThread
    private fun getBitmapFromFileInThread(fileName: String): Bitmap? {
        var fileInputStream: FileInputStream? = null
        try {
            val file: File = File(IMG_CACHE_PATH, fileName)
            if (file.exists()) {
                fileInputStream = FileInputStream(file)
                val bitmap = BitmapFactory.decodeStream(fileInputStream)
                return bitmap
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "get file not found! file name is :" + fileName)
            e.printStackTrace()
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close()
                }
            } catch (e: IOException) {
                Log.e(TAG, "exception happened when close fileInputStream")
                e.printStackTrace()
            }
        }
        return null
    }

    //异步执行即可
    @WorkerThread
    private fun setBitmapIntoFile(fileName: String?, bitmap: Bitmap) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var fileOutputStream: FileOutputStream? = null
                try {
                    val dir: File = File(IMG_CACHE_PATH)
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file = File(IMG_CACHE_PATH + fileName)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    fileOutputStream = FileOutputStream(file)
                    //对图片进行压缩后，再保存到本地。压缩成原品质的quality%。
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, fileOutputStream)
                } catch (e: FileNotFoundException) {
                    Log.e(TAG, "set file not found! file name is:" + fileName)
                    e.printStackTrace()
                } catch (e: IOException) {
                    Log.e(TAG, "write file failed! file name is:" + fileName)
                    e.printStackTrace()
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close()
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "exception happened when close fileInputStream")
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    fun into(imageView: ImageView) {
        mImageView = imageView
        if (TextUtils.isEmpty(mUrl)) {
            Log.d(TAG, "url is empty!")
            return
        }
        Log.d(TAG, "url is:" + mUrl)

        //给imageView设置tag，下载完成后比较tag，一致再加载图片，否则由于RecyclerView重用，可能出现图片错乱
        mImageView!!.setTag(mMd5Str)

        //占位图片
        if (mUsePlaceHolder && imageView.getDrawable() != null) {
            imageView.setBackgroundColor(
                mContext!!.getResources().getColor(R.color.image_placeholder_color)
            )
        }

        //三级缓存
        //先在内存中查找，若内存中已有
        if (sLruCache!!.get(mMd5Str) != null) {
            setImgSrcInUiThread(imageView, getBitmapFromMap(mMd5Str))
            return
        }
        //内存中没有，再去本地查找，若本地已有，需要写入内存
        ThreadDispatcher.Companion.getInstance().runOnWorkerThread(object : Runnable {
            override fun run() {
                val bitmap = getBitmapFromFileInThread(mMd5Str!!)
                if (bitmap != null) {
                    ThreadDispatcher.Companion.getInstance().runOnUiThread(object : Runnable {
                        override fun run() {
                            setImgSrcInUiThread(imageView, bitmap)
                        }
                    })
                    setBitmapIntoMap(mMd5Str, bitmap)
                    return
                }
            }
        })

        //若内存和本地文件都没有，则进行网络请求，请求完毕后写入内存和本地文件
        val httpsUrl = HttpsUrlConverter.httpToHttps(mUrl)
        HttpUtil.sendOkHttpRequest(httpsUrl, mContext, object : IRequestCallback {
            override fun onResponse(bitmap: Bitmap?) {
                if (bitmap == null) {
                    Log.d(TAG, "decodeStream to bitmap failed! url is:" + httpsUrl)
                    return
                }
                ThreadDispatcher.Companion.getInstance().runOnUiThread(object : Runnable {
                    override fun run() {
                        setImgSrcInUiThread(imageView, bitmap)
                    }
                })
                setBitmapIntoMap(mMd5Str, bitmap)
                setBitmapIntoFile(mMd5Str, bitmap)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d(TAG, "request fail! url is :" + mUrl)
            }
        })
    }

    fun into(imageView: ImageView, resourceCallback: ImageDownloadPresenter.ResourceCallback<*>) {
        mImageView = imageView
        if (TextUtils.isEmpty(mUrl)) {
            Log.d(TAG, "url is empty!")
            return
        }
        Log.d(TAG, "url is:" + mUrl)

        //给imageView设置tag，下载完成后比较tag，一致再加载图片，否则由于RecyclerView重用，可能出现图片错乱
        mImageView!!.setTag(mMd5Str)

        //占位图片
        if (mUsePlaceHolder && imageView.getDrawable() != null) {
            imageView.setBackgroundColor(
                mContext!!.getResources().getColor(R.color.image_placeholder_color)
            )
        }

        //三级缓存
        //先在内存中查找，若内存中已有
        if (sLruCache!!.get(mMd5Str) != null) {
            setImgSrcInUiThread(imageView, getBitmapFromMap(mMd5Str))
            resourceCallback.onResourceReady(getBitmapFromMap(mMd5Str))
            return
        }
        //内存中没有，再去本地查找，若本地已有，需要写入内存
        ThreadDispatcher.Companion.getInstance().runOnWorkerThread(object : Runnable {
            override fun run() {
                val bitmap = getBitmapFromFileInThread(mMd5Str!!)
                if (bitmap != null) {
                    ThreadDispatcher.Companion.getInstance().runOnUiThread(object : Runnable {
                        override fun run() {
                            setImgSrcInUiThread(imageView, bitmap)
                            resourceCallback.onResourceReady(bitmap)
                        }
                    })
                    setBitmapIntoMap(mMd5Str, bitmap)
                    return
                } else {
                    //若内存和本地文件都没有，则进行网络请求，请求完毕后写入内存和本地文件
                    val httpsUrl = HttpsUrlConverter.httpToHttps(mUrl)
                    HttpUtil.sendOkHttpRequest(httpsUrl, mContext, object : IRequestCallback {
                        override fun onResponse(bitmap: Bitmap?) {
                            if (bitmap == null) {
                                Log.d(TAG, "decodeStream to bitmap failed! url is:" + httpsUrl)
                                return
                            }
                            ThreadDispatcher.Companion.getInstance()
                                .runOnUiThread(object : Runnable {
                                    override fun run() {
                                        setImgSrcInUiThread(imageView, bitmap)
                                        resourceCallback.onResourceReady(bitmap)
                                    }
                                })
                            setBitmapIntoMap(mMd5Str, bitmap)
                            setBitmapIntoFile(mMd5Str, bitmap)
                        }

                        override fun onFailure(call: Call?, e: IOException?) {
                            Log.d(TAG, "request fail! url is :" + mUrl)
                        }
                    })
                }
            }
        })
    }


    companion object {
        private const val TAG = "ImageDealer"
        val IMG_CACHE_PATH: String = MusicApplication.Companion.getContext().getResources()
            .getString(R.string.image_cache_folder)

        //本地需要设置内存上限
        private val sMaxMemory = Runtime.getRuntime().maxMemory() / 16

        private const val STYLE_ORIGIN = 0 //原始图片
        private const val STYLE_ROUND = 1 //圆角图片
        private const val STYLE_CIRCLE = 2 // 圆形图片

        //key为图片的完整url，value为对应的bitmap
        private val sLruCache: LruCache<String?, Bitmap?>? =
            object : LruCache<String?, Bitmap?>(sMaxMemory.toInt()) {
                override fun sizeOf(key: String?, value: Bitmap): Int {
                    return value.getByteCount()
                }
            }

        val picLruMemory: Int
            get() {
                if (sLruCache == null) {
                    return 0
                }
                return sLruCache.size()
            }

        val pictureLruCache: String
            /**
             * 获取图片缓存，单位M
             * @return
             */
            get() {
                if (sLruCache == null) {
                    return "0"
                }
                val df = DecimalFormat("#.00")
                val pictureCache =
                    df.format(sLruCache.size().toDouble() / 1048576) + "M"
                return pictureCache
            }
    }
}