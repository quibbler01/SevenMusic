package com.quibbler.sevenmusic.view.found;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quibbler.sevenmusic.R;

/**
 * Package:        com.quibbler.sevenmusic.view
 * ClassName:      FoundCustomButton
 * Description:    自定义button，用于“发现”页面的歌单库、歌手库按钮。上方是图片、下方是文字，点击图片和文字都能响应点击事件。
 * Author:         yanwuyang
 * CreateDate:     2019/9/17 11:02
 */
public class FoundCustomButton extends LinearLayout {
    private ImageView mImageView;
    private TextView mTextView;

    public FoundCustomButton(Context context) {
        super(context, null);
    }

    public FoundCustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        //将button布局映射到当前的Activity
        LayoutInflater.from(context).inflate(R.layout.found_custom_button, this, true);
        mImageView = findViewById(R.id.found_custom_btn_iv);
        mTextView = findViewById(R.id.found_custom_btn_tv);
        setClickable(true);
        setFocusable(true);
    }

    /**
     * 设置imageView的src
     *
     * @param resourceID src
     */
    public void setImgResource(int resourceID) {
        mImageView.setImageResource(resourceID);
    }

    /**
     * 设置imageView的大小
     *
     * @param width 指定图片宽度
     * @param height 指定图片高度
     */
    public void setImgSize(int width, int height) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        mImageView.setLayoutParams(params);
    }

    /**
     * 设置TextView的文本
     *
     * @param str
     */
    public void setText(String str) {
        mTextView.setText(str);
    }

    /**
     * 文本颜色
     *
     * @param color
     */
    public void setTextColor(int color) {
        mTextView.setTextColor(color);
    }

    /**
     * 文本字体大小
     *
     * @param size
     */
    public void setTextSize(float size) {
        mTextView.setTextSize(size);
    }

}
