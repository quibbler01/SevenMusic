package com.google.zxing.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.zxing.NetworkUtil
import com.google.zxing.R
import com.google.zxing.UiUtil
import com.google.zxing.camera.CameraManager

/**
 * 自定义组件实现,扫描功能
 */
class ViewfinderView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var cameraManager: CameraManager? = null
    private val mPaint: Paint
    private var resultBitmap: Bitmap? = null
    private val maskColor: Int
    private val resultColor: Int
    private var listener: netChangeListener? = null

    private var mFramingRect: Rect? = null
    private var mGridScanDrawable: Drawable? = null
    private var mGridScanBitmap: Bitmap? = null
    private var mGridScanLineBottom = 0f
    private var mScanLineTop = 0f
    private var mScanLineBitmap: Bitmap

    private val mMoveStepDistance: Int
    private var mAnimDelayTime = 0
    private val mAnimTime: Int

    interface netChangeListener {
        fun netChange(isAvailable: Boolean)
    }

    /**
     * 初始化内部框的大小
     * 
     * @param context
     * @param attrs
     */
    private fun initInnerRect(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView)

        // 扫描框距离顶部
        val innerMarginTop = ta.getDimension(R.styleable.ViewfinderView_inner_margintop, -1f)
        if (innerMarginTop != -1f) {
            CameraManager.Companion.FRAME_MARGINTOP = innerMarginTop.toInt()
        }

        // 扫描框的宽度
        CameraManager.Companion.FRAME_WIDTH = ta.getDimension(
            R.styleable.ViewfinderView_inner_width,
            (UiUtil.getScreenWidth(getContext()) * 2 / 3).toFloat()
        ).toInt()

        // 扫描框的高度
        CameraManager.Companion.FRAME_HEIGHT = ta.getDimension(
            R.styleable.ViewfinderView_inner_height,
            (UiUtil.getScreenWidth(getContext()) * 2 / 3).toFloat()
        ).toInt()

        // 扫描框边角颜色
        innercornercolor = ta.getColor(
            R.styleable.ViewfinderView_inner_corner_color,
            ContextCompat.getColor(getContext(), R.color.white)
        )
        // 扫描框边角长度
        innercornerlength = ta.getDimension(
            R.styleable.ViewfinderView_inner_corner_length,
            UiUtil.dp2px(getContext(), 17).toFloat()
        ).toInt()
        // 扫描框边角宽度
        innercornerwidth = ta.getDimension(
            R.styleable.ViewfinderView_inner_corner_width,
            UiUtil.dp2px(getContext(), 3).toFloat()
        ).toInt()

        //扫描网格图
        mGridScanDrawable = ta.getDrawable(R.styleable.ViewfinderView_inner_grid_scan_drawable)
        if (mGridScanDrawable != null) {
            mGridScanBitmap = (mGridScanDrawable as BitmapDrawable).getBitmap()
        }
        // 扫描线
        mScanLineBitmap = BitmapFactory.decodeResource(
            getResources(),
            ta.getResourceId(
                R.styleable.ViewfinderView_inner_scan_line_bitmap,
                R.drawable.scan_light
            )
        )

        ta.recycle()
    }

    public override fun onDraw(canvas: Canvas) {
        try {
            val frame: Rect? = CameraManager.Companion.get()?.framingRect
            mFramingRect = frame
            if (mFramingRect == null) {
                return
            }

            mAnimDelayTime =
                ((1.0f * mAnimTime * mMoveStepDistance) / mFramingRect!!.height()).toInt()
            if (resultBitmap != null) {
                // Draw the opaque result bitmap over the scanning rectangle
                mPaint.setAlpha(OPAQUE)
                canvas.drawBitmap(
                    resultBitmap!!,
                    mFramingRect!!.left.toFloat(),
                    mFramingRect!!.top.toFloat(),
                    mPaint
                )
            } else {
                // 画遮罩层
                drawMask(canvas)
                // 画四个直角的线
                drawCornerLine(canvas)

                if (NetworkUtil.isNetworkAvailable(getContext())) {
                    //                     画扫描线

                    drawScanLine(canvas)
                    //                     移动扫描线的位置
                    moveScanLine()

                    if (listener != null) {
                        listener!!.netChange(true)
                    }
                } else {
                    if (listener != null) {
                        listener!!.netChange(false)
                    }
                }
            }


            postInvalidateDelayed(
                mAnimDelayTime.toLong(), mFramingRect!!.left,
                mFramingRect!!.top,
                mFramingRect!!.right,
                mFramingRect!!.bottom
            )
        } catch (e: Exception) {
        }
    }

    /**
     * 画阴影遮罩层
     * 
     * @param canvas
     */
    private fun drawMask(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()

        // Draw the exterior darkened
        mPaint.setColor(if (resultBitmap != null) resultColor else maskColor)
        canvas.drawRect(0f, 0f, width.toFloat(), mFramingRect!!.top.toFloat(), mPaint)
        canvas.drawRect(
            0f,
            mFramingRect!!.top.toFloat(),
            mFramingRect!!.left.toFloat(),
            (mFramingRect!!.bottom + 1).toFloat(),
            mPaint
        )
        canvas.drawRect(
            (mFramingRect!!.right + 1).toFloat(),
            mFramingRect!!.top.toFloat(),
            width.toFloat(),
            (mFramingRect!!.bottom + 1).toFloat(),
            mPaint
        )
        canvas.drawRect(
            0f,
            (mFramingRect!!.bottom + 1).toFloat(),
            width.toFloat(),
            height.toFloat(),
            mPaint
        )
    }

    fun setCameraManager(cameraManager: CameraManager?) {
        this.cameraManager = cameraManager
    }

    /**
     * 绘制扫描线
     * 
     * @param canvas
     */
    private fun drawScanLine(canvas: Canvas) {
        //绘制网格形式的扫描图

        if (mGridScanBitmap != null) {
            if (mGridScanLineBottom == 0f) {
                mGridScanLineBottom = mFramingRect!!.top.toFloat()
            }
            val dstGridRectF = RectF(
                mFramingRect!!.left.toFloat(),
                mFramingRect!!.top.toFloat(),
                mFramingRect!!.right.toFloat(),
                mGridScanLineBottom
            )

            val srcGridRect = Rect(
                0,
                (mGridScanBitmap!!.getHeight() - dstGridRectF.height()).toInt(),
                mGridScanBitmap!!.getWidth(),
                mGridScanBitmap!!.getHeight()
            )

            canvas.drawBitmap(mGridScanBitmap!!, srcGridRect, dstGridRectF, mPaint)
        } else {
            //绘制线性形式的扫描图
            if (mScanLineTop == 0f) {
                mScanLineTop = mFramingRect!!.top.toFloat()
            }
            val lineRect = RectF(
                mFramingRect!!.left.toFloat(),
                mScanLineTop,
                mFramingRect!!.right.toFloat(),
                mScanLineTop + mScanLineBitmap.getHeight()
            )
            canvas.drawBitmap(mScanLineBitmap, null, lineRect, mPaint)
        }
    }

    /**
     * 移动扫描线的位置
     */
    private fun moveScanLine() {
        if (mGridScanBitmap != null) {
            //  处理网格扫描图片的情况
            mGridScanLineBottom += mMoveStepDistance.toFloat()
            if (mGridScanLineBottom > mFramingRect!!.bottom) {
                mGridScanLineBottom = mFramingRect!!.top.toFloat()
            }
        } else {
            mScanLineTop += mMoveStepDistance.toFloat()
            if (mScanLineTop + mScanLineBitmap.getHeight() > mFramingRect!!.bottom) {
                mScanLineTop = mFramingRect!!.top.toFloat()
            }
        }
    }

    // 扫描框边角颜色
    private var innercornercolor = 0

    // 扫描框边角长度
    private var innercornerlength = 0

    // 扫描框边角宽度
    private var innercornerwidth = 0

    init {
        mPaint = Paint()
        val resources = getResources()
        maskColor = resources.getColor(R.color.viewfinder_mask)
        resultColor = resources.getColor(R.color.result_view)
        mMoveStepDistance = UiUtil.dp2px(context, 2)
        mAnimTime = 1000
        mScanLineBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.scan_light
        )

        initInnerRect(context, attrs)
    }

    /**
     * 绘制四个角的折现边框
     * 
     * @param canvas
     */
    private fun drawCornerLine(canvas: Canvas) {
        mPaint.setColor(innercornercolor)
        mPaint.setStyle(Paint.Style.FILL)

        val corWidth = innercornerwidth
        val corLength = innercornerlength

        // 左上角
        canvas.drawRect(
            mFramingRect!!.left.toFloat(),
            mFramingRect!!.top.toFloat(),
            (mFramingRect!!.left + corWidth).toFloat(),
            (mFramingRect!!.top
                    + corLength).toFloat(),
            mPaint
        )
        canvas.drawRect(
            mFramingRect!!.left.toFloat(), mFramingRect!!.top.toFloat(), (mFramingRect!!.left
                    + corLength).toFloat(), (mFramingRect!!.top + corWidth).toFloat(), mPaint
        )
        // 右上角
        canvas.drawRect(
            (mFramingRect!!.right - corWidth).toFloat(),
            mFramingRect!!.top.toFloat(),
            mFramingRect!!.right.toFloat(),
            (mFramingRect!!.top + corLength).toFloat(),
            mPaint
        )
        canvas.drawRect(
            (mFramingRect!!.right - corLength).toFloat(), mFramingRect!!.top.toFloat(),
            mFramingRect!!.right.toFloat(), (mFramingRect!!.top + corWidth).toFloat(), mPaint
        )
        // 左下角
        canvas.drawRect(
            mFramingRect!!.left.toFloat(), (mFramingRect!!.bottom - corLength).toFloat(),
            (mFramingRect!!.left + corWidth).toFloat(), mFramingRect!!.bottom.toFloat(), mPaint
        )
        canvas.drawRect(
            mFramingRect!!.left.toFloat(),
            (mFramingRect!!.bottom - corWidth).toFloat(),
            (mFramingRect!!.left
                    + corLength).toFloat(),
            mFramingRect!!.bottom.toFloat(),
            mPaint
        )
        // 右下角
        canvas.drawRect(
            (mFramingRect!!.right - corWidth).toFloat(),
            (mFramingRect!!.bottom - corLength).toFloat(),
            mFramingRect!!.right.toFloat(),
            mFramingRect!!.bottom.toFloat(),
            mPaint
        )
        canvas.drawRect(
            (mFramingRect!!.right - corLength).toFloat(),
            (mFramingRect!!.bottom - corWidth).toFloat(),
            mFramingRect!!.right.toFloat(),
            mFramingRect!!.bottom.toFloat(),
            mPaint
        )
    }

    fun drawViewfinder() {
        resultBitmap = null
        invalidate()
    }

    fun setNetChangeListener(listener: netChangeListener?) {
        this.listener = listener
    }


    companion object {
        private val TAG: String = ViewfinderView::class.java.getSimpleName()
        private const val OPAQUE = 0xFF

        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.getResources().getDisplayMetrics().density
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}
