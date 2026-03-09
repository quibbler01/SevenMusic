package com.quibbler.sevenmusic

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.PageTransformer
import com.quibbler.sevenmusic.adapter.splash.SplashPagerAdapter
import kotlin.math.floor
import kotlin.math.min

/**
 * Package:        com.quibbler.sevenmusic
 * ClassName:      SplashActivity
 * Description:    引导页，显示不同的广告画面
 * Author:         11103876
 * CreateDate:     2019/10/7 16:10
 */
class SplashActivity : Activity() {
    /**
     * 存储引导页不同图片资源布局的集合实例
     */
    private var mViews: MutableList<View?>? = null

    /**
     * 引导页ViewPager实例
     */
    private var mSplashVp: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = getWindow().getDecorView()
            //设置让应用主题内容占据状态栏和导航栏
            val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.setSystemUiVisibility(option)
            //设置状态栏和导航栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT)
            getWindow().setNavigationBarColor(Color.TRANSPARENT)
        }
        setContentView(R.layout.splash_main_activity)
        initView()
    }

    /**
     * 描述：初始化组件
     */
    private fun initView() {
        mSplashVp = findViewById<ViewPager>(R.id.splash_vp_guide)

        mViews = ArrayList<View?>()
        val splashView1 =
            LayoutInflater.from(this).inflate(R.layout.splash_view_img_01, null, false)
        val splashView2 =
            LayoutInflater.from(this).inflate(R.layout.splash_view_img_02, null, false)
        val splashView3 =
            LayoutInflater.from(this).inflate(R.layout.splash_view_img_03, null, false)
        val splashView4 =
            LayoutInflater.from(this).inflate(R.layout.splash_view_img_04, null, false)
        mViews!!.add(splashView1)
        mViews!!.add(splashView2)
        mViews!!.add(splashView3)
        mViews!!.add(splashView4)

        val adapter = SplashPagerAdapter(mViews, this@SplashActivity)
        mSplashVp!!.setAdapter(adapter) // 设置导航页的适配器
        mSplashVp!!.setCurrentItem(0) // 默认显示第一个页卡
        mSplashVp!!.setOffscreenPageLimit(0)
        mSplashVp!!.setPageTransformer(true, object : PageTransformer {
            override fun transformPage(page: View, position: Float) {
                val CENTER_PAGE_SCALE = 1.0f // 控制屏幕图片占据屏幕的大小
                val pagerWidth = mSplashVp!!.getWidth()
                val offscreenPageLimit = mSplashVp!!.getOffscreenPageLimit()
                val horizontalOffsetBase =
                    (pagerWidth - pagerWidth * CENTER_PAGE_SCALE) / 2 / offscreenPageLimit + 15
                if (position >= offscreenPageLimit || position <= -1) {
                    page.setVisibility(View.GONE)
                } else {
                    page.setVisibility(View.VISIBLE)
                }

                if (position >= 0) {
                    val translationX = (horizontalOffsetBase - page.getWidth()) * position
                    page.setTranslationX(translationX)
                }
                if (position > -1 && position < 0) {
                    val rotation = position * 30
                    page.setRotation(rotation)
                    page.setAlpha((position * position * position + 1))
                } else if (position > offscreenPageLimit - 1) {
                    page.setAlpha((1 - position + floor(position.toDouble())).toFloat())
                } else {
                    page.setRotation(0f)
                    page.setAlpha(1f)
                }
                if (position == 0f) {
                    page.setScaleX(CENTER_PAGE_SCALE)
                    page.setScaleY(CENTER_PAGE_SCALE)
                } else {
                    val scaleFactor = min(CENTER_PAGE_SCALE - position * 0.1f, CENTER_PAGE_SCALE)
                    page.setScaleX(scaleFactor)
                    page.setScaleY(scaleFactor)
                }
                ViewCompat.setElevation(page, (offscreenPageLimit - position) * 5)
            }
        })
    }
}
