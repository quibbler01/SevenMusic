
package com.google.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.zxing.NetworkUtil;
import com.google.zxing.R;
import com.google.zxing.UiUtil;
import com.google.zxing.camera.CameraManager;

/**
 * 自定义组件实现,扫描功能
 */
public final class ViewfinderView extends View {

    private static final String TAG = ViewfinderView.class.getSimpleName();
    private static final int OPAQUE = 0xFF;

    private CameraManager cameraManager;
    private final Paint mPaint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private netChangeListener listener;

    private Rect mFramingRect;
    private Drawable mGridScanDrawable;
    private Bitmap mGridScanBitmap;
    private float mGridScanLineBottom;
    private float mScanLineTop;
    private Bitmap mScanLineBitmap;

    private int mMoveStepDistance;
    private int mAnimDelayTime;
    private int mAnimTime;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        mMoveStepDistance = UiUtil.dp2px(context, 2);
        mAnimTime = 1000;
        mScanLineBitmap = BitmapFactory.decodeResource(resources,
                R.drawable.scan_light);

        initInnerRect(context, attrs);
    }

    public interface netChangeListener {
        void netChange(boolean isAvailable);
    }

    /**
     * 初始化内部框的大小
     *
     * @param context
     * @param attrs
     */
    private void initInnerRect(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView);

        // 扫描框距离顶部
        float innerMarginTop = ta.getDimension(R.styleable.ViewfinderView_inner_margintop, -1);
        if (innerMarginTop != -1) {
            CameraManager.FRAME_MARGINTOP = (int) innerMarginTop;
        }

        // 扫描框的宽度
        CameraManager.FRAME_WIDTH = (int) ta.getDimension(R.styleable.ViewfinderView_inner_width, UiUtil.getScreenWidth(getContext()) * 2 / 3);

        // 扫描框的高度
        CameraManager.FRAME_HEIGHT = (int) ta.getDimension(R.styleable.ViewfinderView_inner_height, UiUtil.getScreenWidth(getContext()) * 2 / 3);

        // 扫描框边角颜色
        innercornercolor = ta.getColor(R.styleable.ViewfinderView_inner_corner_color, ContextCompat.getColor(getContext(), R.color.white));
        // 扫描框边角长度
        innercornerlength = (int) ta.getDimension(R.styleable.ViewfinderView_inner_corner_length, UiUtil.dp2px(getContext(), 17));
        // 扫描框边角宽度
        innercornerwidth = (int) ta.getDimension(R.styleable.ViewfinderView_inner_corner_width, UiUtil.dp2px(getContext(), 3));

        //扫描网格图
        mGridScanDrawable = ta.getDrawable(R.styleable.ViewfinderView_inner_grid_scan_drawable);
        if (mGridScanDrawable != null) {
            mGridScanBitmap = ((BitmapDrawable) mGridScanDrawable).getBitmap();
        }
        // 扫描线
        mScanLineBitmap = BitmapFactory.decodeResource(getResources(), ta.getResourceId(R.styleable.ViewfinderView_inner_scan_line_bitmap, R.drawable.scan_light));

        ta.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        try {
            Rect frame = CameraManager.get().getFramingRect();
            mFramingRect = frame;
            if (mFramingRect == null) {
                return;
            }

            mAnimDelayTime = (int) ((1.0f * mAnimTime * mMoveStepDistance) / mFramingRect.height());
            if (resultBitmap != null) {
                // Draw the opaque result bitmap over the scanning rectangle
                mPaint.setAlpha(OPAQUE);
                canvas.drawBitmap(resultBitmap, mFramingRect.left, mFramingRect.top, mPaint);
            } else {
                // 画遮罩层
                drawMask(canvas);
                // 画四个直角的线
                drawCornerLine(canvas);

                if (NetworkUtil.isNetworkAvailable(getContext())) {

//                     画扫描线
                    drawScanLine(canvas);
//                     移动扫描线的位置
                    moveScanLine();

                    if (listener != null) {
                        listener.netChange(true);
                    }
                } else {
                    if (listener != null) {
                        listener.netChange(false);
                    }
                }
            }


            postInvalidateDelayed(mAnimDelayTime, mFramingRect.left,
                    mFramingRect.top,
                    mFramingRect.right,
                    mFramingRect.bottom);
        } catch (Exception e) {

        }
    }

    /**
     * 画阴影遮罩层
     *
     * @param canvas
     */
    private void drawMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior darkened
        mPaint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, mFramingRect.top, mPaint);
        canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, mPaint);
        canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width, mFramingRect.bottom + 1, mPaint);
        canvas.drawRect(0, mFramingRect.bottom + 1, width, height, mPaint);

    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    /**
     * 绘制扫描线
     *
     * @param canvas
     */
    private void drawScanLine(Canvas canvas) {

        //绘制网格形式的扫描图
        if (mGridScanBitmap != null) {
            if (mGridScanLineBottom == 0) {
                mGridScanLineBottom = mFramingRect.top;
            }
            RectF dstGridRectF = new RectF(mFramingRect.left,
                    mFramingRect.top,
                    mFramingRect.right,
                    mGridScanLineBottom);

            Rect srcGridRect = new Rect(0,
                    (int) (mGridScanBitmap.getHeight() - dstGridRectF.height()),
                    mGridScanBitmap.getWidth(),
                    mGridScanBitmap.getHeight());

            canvas.drawBitmap(mGridScanBitmap, srcGridRect, dstGridRectF, mPaint);
        } else {
            //绘制线性形式的扫描图
            if (mScanLineTop == 0) {
                mScanLineTop = mFramingRect.top;
            }
            RectF lineRect = new RectF(mFramingRect.left,
                    mScanLineTop,
                    mFramingRect.right,
                    mScanLineTop + mScanLineBitmap.getHeight());
            canvas.drawBitmap(mScanLineBitmap, null, lineRect, mPaint);

        }
    }

    /**
     * 移动扫描线的位置
     */
    private void moveScanLine() {

        if (mGridScanBitmap != null) {
            //  处理网格扫描图片的情况
            mGridScanLineBottom += mMoveStepDistance;
            if (mGridScanLineBottom > mFramingRect.bottom) {
                mGridScanLineBottom = mFramingRect.top;
            }
        } else {
            mScanLineTop += mMoveStepDistance;
            if (mScanLineTop + mScanLineBitmap.getHeight() > mFramingRect.bottom) {
                mScanLineTop = mFramingRect.top;
            }
        }
    }

    // 扫描框边角颜色
    private int innercornercolor;
    // 扫描框边角长度
    private int innercornerlength;
    // 扫描框边角宽度
    private int innercornerwidth;

    /**
     * 绘制四个角的折现边框
     *
     * @param canvas
     */
    private void drawCornerLine(Canvas canvas) {

        mPaint.setColor(innercornercolor);
        mPaint.setStyle(Paint.Style.FILL);

        int corWidth = innercornerwidth;
        int corLength = innercornerlength;

        // 左上角
        canvas.drawRect(mFramingRect.left, mFramingRect.top, mFramingRect.left + corWidth, mFramingRect.top
                + corLength, mPaint);
        canvas.drawRect(mFramingRect.left, mFramingRect.top, mFramingRect.left
                + corLength, mFramingRect.top + corWidth, mPaint);
        // 右上角
        canvas.drawRect(mFramingRect.right - corWidth, mFramingRect.top, mFramingRect.right,
                mFramingRect.top + corLength, mPaint);
        canvas.drawRect(mFramingRect.right - corLength, mFramingRect.top,
                mFramingRect.right, mFramingRect.top + corWidth, mPaint);
        // 左下角
        canvas.drawRect(mFramingRect.left, mFramingRect.bottom - corLength,
                mFramingRect.left + corWidth, mFramingRect.bottom, mPaint);
        canvas.drawRect(mFramingRect.left, mFramingRect.bottom - corWidth, mFramingRect.left
                + corLength, mFramingRect.bottom, mPaint);
        // 右下角
        canvas.drawRect(mFramingRect.right - corWidth, mFramingRect.bottom - corLength,
                mFramingRect.right, mFramingRect.bottom, mPaint);
        canvas.drawRect(mFramingRect.right - corLength, mFramingRect.bottom - corWidth,
                mFramingRect.right, mFramingRect.bottom, mPaint);
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setNetChangeListener(netChangeListener listener) {
        this.listener = listener;
    }


}
