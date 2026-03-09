package com.quibbler.sevenmusic.view.found

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.quibbler.sevenmusic.R

/**
 * Package:        com.quibbler.sevenmusic.view
 * ClassName:      FoundCustomButton
 * Description:    自定义button，用于“发现”页面的歌单库、歌手库按钮。上方是图片、下方是文字，点击图片和文字都能响应点击事件。
 * Author:         yanwuyang
 * CreateDate:     2019/9/17 11:02
 */
class FoundCustomButton : LinearLayout {
    private var mImageView: ImageView? = null
    private var mTextView: TextView? = null

    constructor(context: Context?) : super(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        //将button布局映射到当前的Activity
        LayoutInflater.from(context).inflate(R.layout.found_custom_button, this, true)
        mImageView = findViewById<ImageView>(R.id.found_custom_btn_iv)
        mTextView = findViewById<TextView>(R.id.found_custom_btn_tv)
        setClickable(true)
        setFocusable(true)
    }

    /**
     * 设置imageView的src
     * 
     * @param resourceID src
     */
    fun setImgResource(resourceID: Int) {
        mImageView!!.setImageResource(resourceID)
    }

    /**
     * 设置imageView的大小
     * 
     * @param width 指定图片宽度
     * @param height 指定图片高度
     */
    fun setImgSize(width: Int, height: Int) {
        val params = LayoutParams(width, height)
        mImageView!!.setLayoutParams(params)
    }

    /**
     * 设置TextView的文本
     * 
     * @param str
     */
    fun setText(str: String?) {
        mTextView!!.setText(str)
    }

    /**
     * 文本颜色
     * 
     * @param color
     */
    fun setTextColor(color: Int) {
        mTextView!!.setTextColor(color)
    }

    /**
     * 文本字体大小
     * 
     * @param size
     */
    fun setTextSize(size: Float) {
        mTextView!!.setTextSize(size)
    }
}
