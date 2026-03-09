package com.quibbler.sevenmusic.view.playbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class MaqueeTextView extends TextView {
    /**
     * 字体行宽度
     */
    private int x;
    /**
     * 字体总高度
     */
    private int y;
    /**
     * 默认滚动速度
     */
    private int speed = 2;
    /**
     * 字幕滚动方向
     */
    public static final int FROM_RIGHT = 0;
    public static final int FROM_LEFT = 1;
    public static final int FROM_TOP = 2;
    public static final int FROM_BOTTOM = 3;
    /**
     * 默认文字滚动类型-从右往左
     */
    private int scrollType = FROM_RIGHT;
    /**
     * 文字滚动标识
     */
    public static final boolean START = true;
    /**
     * 文字停止标识
     */
    public static final boolean STOP = false;
    /**
     * 默认文字不滚动
     */
    private boolean scrollStatus = START;

    public MaqueeTextView(Context context) {
        super(context);
    }

    public MaqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        x = getTextWidth();
        y = getTextHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        scrollType(scrollType);
        super.onDraw(canvas);
    }

    /**
     * 文字滚动类型
     *
     * @param type
     */
    private void scrollType(int type) {
        if (scrollStatus) {
            switch (type) {
                case FROM_RIGHT:
                    // 右到左
                    if (x >= getTextWidth()) {
                        x = -getWidth();
                    }
                    scrollTo(x, 0);
                    x = x + speed;
                    postInvalidate();
                    break;
                case FROM_LEFT:
                    // 左到右
                    if (x <= -getWidth()) {
                        x = getTextWidth();
                    }
                    scrollTo(x, 0);
                    x = x - speed;
                    postInvalidate();
                    break;
                case FROM_TOP:
                    // 上到下
                    if (y <= -getHeight()) {
                        y = getTextHeight();
                    }
                    scrollTo(0, y);
                    y = y - speed;
                    postInvalidate();
                    break;
                case FROM_BOTTOM:
                    // 下到上
                    if (y >= getTextHeight()) {
                        y = -getHeight();
                    }
                    scrollTo(0, y);
                    y = y + speed;
                    postInvalidate();
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 获取字体行宽度
     *
     * @return
     */
    private int getTextWidth() {
        int mTextWidth;
        Paint mPaint = getPaint();
        if (getLineCount() > 1) {
            String[] lineContent = getText().toString().split("\n"); // 如果有多行文字，则获取最长的一行文字宽度
            int maxLine = 0;
            int maxLineNumber = 0;
            for (int i = 0; i < lineContent.length; i++) {
                if (lineContent[i].length() > maxLine) {
                    maxLine = lineContent[i].length();
                    maxLineNumber = i;
                }
            }
            mTextWidth = (int) mPaint.measureText(lineContent[maxLineNumber]);
        } else {
            mTextWidth = (int) mPaint.measureText(getText().toString());
        }
        return mTextWidth;
    }

    /**
     * 获取字体总高度
     *
     * @return
     */
    private int getTextHeight() {
        return getLineHeight() * getLineCount();
    }

    /**
     * 获取文字滚动方向类型
     *
     * @return
     */
    public int getScrollType() {
        return scrollType;
    }

    /**
     * 设置文字滚动方向类型
     *
     * @param scrollType 方向类型
     */
    public void setScrollType(int scrollType) {
        this.scrollType = scrollType;
        setScrollStatus(START);
    }

    /**
     * 获取文字滚动速度
     *
     * @return
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * 设置文字滚动速度
     *
     * @param speed
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * 获取当前文字是否是在滚动
     *
     * @return
     */
    public boolean isScrollStatus() {
        return scrollStatus;
    }

    /**
     * 设置滚动状态，开始or暂停
     *
     * @param scrollStatus
     */
    public void setScrollStatus(boolean scrollStatus) {
        this.scrollStatus = scrollStatus;
        postInvalidate();
    }
}
