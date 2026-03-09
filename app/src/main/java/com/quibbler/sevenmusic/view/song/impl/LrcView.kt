package com.quibbler.sevenmusic.view.song.impl;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.bean.song.impl.LrcRow;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.view.song.ILrcView;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.view.song.impl
 * ClassName:      LrcView
 * Description:    自定义歌词view
 * Author:         lishijun
 * CreateDate:     2019/9/28 19:21
 */
public class LrcView extends View implements ILrcView {

    private final static String TAG = "LrcView";

    private List<LrcRow> mLrcRowList;

    private long mNextTime = 0l; // 保存下一句开始的时间

    private int mOffsetY;   // y上的偏移

    //歌词字体大小默认值
    private int mLrcFontSize = 47;

    //当没有歌词的时候展示的内容
    private String mLoadingLrcTip = "本歌曲暂无歌词";

    //当前高亮歌词的行数
    private int mHignlightRow = 0;

    //当前高亮歌词的字体颜色为白色
    private int mHignlightRowColor = Color.WHITE;

    //拖动高亮歌词的字体颜色为浅白色
    private int mSrollHignlightRowColor = Color.rgb(220, 220, 220);

    //不高亮歌词的字体颜色为灰色
    private int mNormalRowColor = Color.rgb(156, 156, 156);

    //歌词间距
    private int mPaddingY = 80;

    private int mMaxScroll = mLrcFontSize + mPaddingY; // 最大滑动距离=一行歌词高度+歌词间距

    private static final int SCROLL_TIME = 500;

    private Paint mPaint;

    //平滑滚动
    private Scroller mScroller;

    //是否允许用户滑动，默认允许，锁屏界面不允许
    private boolean mCanScroll = true;

    private boolean mIsScrolling = false;

    /**
     * 拖动歌词时，在当前高亮歌词下面的一条直线的字体颜色
     **/
    private int mSeekLineColor = Color.WHITE;

    /**
     * 拖动歌词时，展示当前高亮歌词的时间的字体颜色
     **/
    private int mSeekLineTextColor = Color.WHITE;
    /**
     * 拖动歌词时，展示当前高亮歌词的时间的字体大小默认值
     **/
    private int mSeekLineTextSize = 25;

    /**
     * 拖动歌词时，在当前高亮歌词下面的一条直线的起始位置
     **/
    private int mSeekLinePaddingX = 10;

    /**
     * 拖动歌词时的高亮行
     **/
    private int mScrollHignlightRow = 0;

    private float mLastMotionY;

    private float mLastMotionX;

    private int mLastHignlightRow;

    private boolean mIsFirstTouch = false;

    @Override
    public boolean isScrolling() {
        return mIsScrolling;
    }

    public LrcView(Context context, AttributeSet attr) {
        super(context, attr);
        mScroller = new Scroller(context, new LinearInterpolator());
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mLrcFontSize);
    }

    @Override
    public void setLrc(List<LrcRow> lrcRows) {
        mHignlightRow = 0;
        mLrcRowList = lrcRows;
        invalidate();
    }

    @Override
    public void seekLrcToTime(long time, boolean cb) {
        if (mLrcRowList == null || mLrcRowList.size() == 0) {
            return;
        }
        // 如果当前时间小于下一句开始的时间，且为用户手动调整歌曲播放位置
        if (mNextTime > time && cb) {
            // 每次进来都遍历存放的时间
            for (int i = 0; i < mLrcRowList.size(); i++) {
                LrcRow current = mLrcRowList.get(i);
                long currentTime = current.mTime;
                if (currentTime > time) {
                    mNextTime = currentTime;
                    mHignlightRow = i - 1;
                    postInvalidate();
                    return;
                }
            }
        } else if (mNextTime > time) {
            return;
        } else {
            // 每次进来都遍历存放的时间
            for (int i = 0; i < mLrcRowList.size(); i++) {
                // 发现这个时间大于传进来的时间
                // 那么现在就应该显示这个时间前面的对应的那一行
                // 每次都重新显示，是不是要判断：现在正在显示就不刷新了
                LrcRow current = mLrcRowList.get(i);
                long currentTime = current.mTime;
                if (currentTime > time) {
                    mNextTime = currentTime;
                    if(cb){
                        mHignlightRow = i - 1;
                    }else{
                        mScroller.abortAnimation();
                        mScroller.startScroll(i, 0, 0, mMaxScroll, SCROLL_TIME);
                    }
                    postInvalidate();
                    return;
                }else if(i == mLrcRowList.size() - 1){
                    //如果最后一个还不满足条件就显示最后一句
                    mNextTime = currentTime;
                    mHignlightRow = i;
                    postInvalidate();
                }
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mOffsetY = mScroller.getCurrY();
            if (mScroller.isFinished()) {
                mOffsetY = 0;
                int cur = mScroller.getCurrX();
                mHignlightRow = cur <= 1 ? 0 : cur - 1;
            }
            postInvalidate();
        }
    }

    /**
     * 设置要高亮的歌词为第几行歌词
     *
     * @param position 要高亮的歌词行数
     * @param cb       是否是手指拖动后要高亮的歌词
     */
    @Override
    public void seekLrc(int position, boolean cb) {
        if (mLrcRowList == null || position < 0 || position > mLrcRowList.size()) {
            return;
        }
        mHignlightRow = position;
        invalidate();
        if(cb){
            MusicPlayerService.setPlayProgress((int)mLrcRowList.get(position).mTime,
                    MusicPlayerService.isPlaying);
            seekLrcToTime((int)mLrcRowList.get(position).mTime, true);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int height = getHeight();
        final int width = getWidth();
        //当没有歌词的时候
        if (mLrcRowList == null || mLrcRowList.size() == 0) {
            if (mLoadingLrcTip != null) {
                mPaint.setColor(mHignlightRowColor);
                mPaint.setTextSize(mLrcFontSize);
                mPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(mLoadingLrcTip, width / 2, height / 2 - mLrcFontSize, mPaint);
            }
            return;
        }
        float rowY;
        final int rowX = width / 2;
        int rowNum;
        /**
         * 分以下三步来绘制歌词：
         *
         *  第1步：高亮地画出正在播放的那句歌词
         *  第2步：画出正在播放的那句歌词的上面可以展示出来的歌词
         *  第3步：画出正在播放的那句歌词的下面的可以展示出来的歌词
         */
        // 1、 高亮地画出正在要高亮的的那句歌词
        if(mHignlightRow < 0){
            mHignlightRow = 0;
        }
        String highlightText = mLrcRowList.get(mHignlightRow).mContent;
        float centerY = 2 * height / 5 - mLrcFontSize;
        float highlightRowY = centerY + mMicroOffsetY;
        mPaint.setColor(mHignlightRowColor);
        mPaint.setTextSize(mLrcFontSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(highlightText, rowX, highlightRowY - mOffsetY, mPaint);

        // 上下拖动歌词的时候 画出拖动第二高亮的那句歌词的时间和第二高亮的那句歌词下面的一条直线
        if (mIsScrolling) {
            // 画出高亮的那句歌词下面的一条直线
            mPaint.setColor(mSeekLineColor);
            float lineY = centerY + mPaddingY - mOffsetY;
            //该直线的x坐标从0到屏幕宽度
            canvas.drawLine(mSeekLinePaddingX, lineY, width - mSeekLinePaddingX, lineY, mPaint);
            //得到正在拖动的需要第二高亮的歌词行数和时间
            float tempY = 100000000f;
            String time = null;
            rowNum = mHignlightRow - 1;
            rowY = highlightRowY - mPaddingY - mLrcFontSize;
            while (rowY > -mLrcFontSize && rowNum >= 0) {
                String text = mLrcRowList.get(rowNum).mContent;
                canvas.drawText(text, rowX, rowY - mOffsetY, mPaint);
                if(rowNum >= 0){
                    if(tempY > Math.abs(rowY - lineY - (float)mLrcFontSize / 2)){
                        tempY = Math.abs(rowY - lineY - (float)mLrcFontSize / 2);
                        mScrollHignlightRow = rowNum;
                        time = mLrcRowList.get(rowNum).mString;
                    }
                }
                rowY -= (mPaddingY + mLrcFontSize);
                rowNum--;
            }
            rowNum = mHignlightRow + 1;
            rowY = highlightRowY + mPaddingY + mLrcFontSize;
            while (rowY < height && rowNum < mLrcRowList.size()) {
                String text = mLrcRowList.get(rowNum).mContent;
                canvas.drawText(text, rowX, rowY - mOffsetY, mPaint);
                if(rowNum < mLrcRowList.size()){
                    if(tempY > Math.abs(rowY - lineY - (float)mLrcFontSize / 2)){
                        tempY = Math.abs(rowY - lineY - (float)mLrcFontSize / 2);
                        mScrollHignlightRow = rowNum;
                        time = mLrcRowList.get(rowNum).mString;
                    }
                }
                rowY += (mPaddingY + mLrcFontSize);
                rowNum++;
            }
            if(time != null){
                // 画出高亮的那句歌词的时间
                mPaint.setColor(mSeekLineTextColor);
                mPaint.setTextSize(mSeekLineTextSize);
                mPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(time, mSeekLinePaddingX, centerY + mPaddingY - mOffsetY - 10, mPaint);
            }
        }
        // 2、画出正在播放的那句歌词的上面可以展示出来的歌词
        mPaint.setTextSize(mLrcFontSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        rowNum = mHignlightRow - 1;
        rowY = highlightRowY - mPaddingY - mLrcFontSize;
        //画出正在播放的那句歌词的上面所有的歌词
        while (rowY > -mLrcFontSize && rowNum >= 0) {
            String text = mLrcRowList.get(rowNum).mContent;
            if(mIsScrolling && rowNum == mScrollHignlightRow){
                mPaint.setColor(mSrollHignlightRowColor);
            }else{
                mPaint.setColor(mNormalRowColor);
            }
            canvas.drawText(text, rowX, rowY - mOffsetY, mPaint);
            rowY -= (mPaddingY + mLrcFontSize);
            rowNum--;
        }
        // 3、画出正在播放的那句歌词的下面的可以展示出来的歌词
        rowNum = mHignlightRow + 1;
        rowY = highlightRowY + mPaddingY + mLrcFontSize;
        //画出正在播放的那句歌词的所有下面的可以展示出来的歌词
        while (rowY < height && rowNum < mLrcRowList.size()) {
            String text = mLrcRowList.get(rowNum).mContent;
            if(mIsScrolling && rowNum == mScrollHignlightRow){
                mPaint.setColor(mSrollHignlightRowColor);
            }else{
                mPaint.setColor(mNormalRowColor);
            }
            canvas.drawText(text, rowX, rowY - mOffsetY, mPaint);
            rowY += (mPaddingY + mLrcFontSize);
            rowNum++;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mCanScroll) {
            return false;
        }
        if (mLrcRowList == null || mLrcRowList.size() == 0) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            //手指按下
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mLastHignlightRow = mHignlightRow;
                mIsFirstTouch = true;
                invalidate();
                break;
            //手指移动
            case MotionEvent.ACTION_MOVE:
                //第一次触摸，屏蔽微小移动
                float y = event.getY();//手指当前位置的y坐标
                float offsetY = y - mLastMotionY; //第一次按下的y坐标和目前移动手指位置的y坐标之差
                if(mIsFirstTouch && Math.abs(offsetY) < 20){
                    mIsScrolling = false;
                }else{
                    mIsScrolling = true;
                    mIsFirstTouch = false;
                }
                //如果一个手指按下，在屏幕上移动的话，拖动歌词上下
                doSeek(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                //手指抬起
            case MotionEvent.ACTION_UP:
                if(mIsScrolling){
                    updateHighLrc();
                    mIsScrolling = false;
                    mScrollHignlightRow = 0;
                }
                break;
        }
        return true;
    }

    private float mMicroOffsetY = 0;

    /**
     * 处理单指在屏幕移动时，歌词上下滚动
     */
    private void doSeek(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();//手指当前位置的y坐标
        float offsetY = y - mLastMotionY; //第一次按下的y坐标和目前移动手指位置的y坐标之差
        float offsetX = x - mLastMotionX;
        //屏蔽左右滑动造成的上下微滑动
        if((Math.abs(offsetY) / Math.abs(offsetX)) < 0.8){
            return;
        }
        //如果不需要高亮歌词，也要有偏移，保证滑动操作的平滑
        mMicroOffsetY = offsetY;
        invalidate();
    }

    private void updateHighLrc(){
        mMicroOffsetY = 0;
        mHignlightRow = mScrollHignlightRow;
        //如果高亮行发生了变化才设置歌曲位置
        if(mHignlightRow != mLastHignlightRow){
            seekLrc(mHignlightRow, true);
            invalidate();
            Intent intent = new Intent("com.quibbler.sevenmusic.UPDATE_PROGRESSBAR_BROADCAST");
            intent.setPackage(MusicApplication.getContext().getPackageName());
            MusicApplication.getContext().sendBroadcast(intent);
        }
    }

    public void setCanScroll(boolean canScroll) {
        mCanScroll = canScroll;
    }
}