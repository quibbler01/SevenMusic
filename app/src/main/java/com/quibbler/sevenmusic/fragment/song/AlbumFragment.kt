package com.quibbler.sevenmusic.fragment.song

import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.song.MusicPlayActivity
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.presenter.MusicPresnter
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.BlurTransformation

/**
 * 
 * Package:        com.quibbler.sevenmusic.fragment.song
 * ClassName:      AlbumFragment
 * Description:    播放界面专辑页面fragment
 * Author:         lishijun
 * CreateDate:     2019/9/27 19:48
 */
class AlbumFragment : Fragment() {
    private var mView: View? = null

    //歌曲信息
    private var mMvMusicInfo: MvMusicInfo? = null

    private var mAlbumAnimator: ObjectAnimator? = null

    private var mPlayAnimator: ObjectAnimator? = null

    private var mPauseAnimator: ObjectAnimator? = null

    private var mMusicPictureView: ImageView? = null

    //留声机view
    private var mGramoView: ImageView? = null

    private var mIsPlayingAlbumAnim = false

    private var mGramoViewStoped = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.fragment_album, container, false)
        initView()
        initPicture()
        initAnimator()
        if (getArguments()!!.getBoolean("state")) {
            startPlayAnimator()
            startAlbumAnimator()
        }
        return mView!!
    }

    fun initPicture() {
        mMvMusicInfo = (getActivity() as MusicPlayActivity).getMvMusicInfo()
        if (TextUtils.isEmpty(mMvMusicInfo!!.getPictureUrl())) {
            MusicPresnter.getMusicPicture(mMvMusicInfo, object : MusicCallBack {
                override fun onMusicInfoCompleted() {
                    if (TextUtils.isEmpty(mMvMusicInfo!!.getPictureUrl())) {
                        return
                    }
                    if (getActivity() != null) {
                        getActivity()!!.runOnUiThread(object : Runnable {
                            override fun run() {
                                initPictureByGlide()
                            }
                        })
                    }
                }
            })
        } else {
            initPictureByGlide()
        }
    }

    private fun initPictureByGlide() {
        //先用glide加载
        Glide.with(this@AlbumFragment)
            .load(mMvMusicInfo!!.getPictureUrl())
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(mMusicPictureView!!)
        Glide.with(this@AlbumFragment)
            .load(mMvMusicInfo!!.getPictureUrl())
            .apply(RequestOptions.bitmapTransform(BlurTransformation(22, 35)))
            .into(object : SimpleTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable?,
                    transition: Transition<in Drawable?>?
                ) {
                    if (getActivity() != null) {
                        val linearLayout = (getActivity() as MusicPlayActivity).getMainLayout()
                        linearLayout.setBackground(resource)
                    }
                }
            })
    }

    private fun initView() {
        mMusicPictureView = mView!!.findViewById<ImageView>(R.id.music_iv_picture)
        mGramoView = mView!!.findViewById<ImageView>(R.id.music_iv_gramo)
    }

    //初始化旋转动画
    private fun initAnimator() {
        //专辑旋转动画
        val frameLayout = mView!!.findViewById<FrameLayout?>(R.id.music_fl_album)
        mAlbumAnimator = ObjectAnimator.ofFloat(frameLayout, "rotation", 0.0f, 360.0f)
        mAlbumAnimator!!.setDuration(50000) //设定转一圈的时间
        mAlbumAnimator!!.setRepeatCount(Animation.INFINITE) //设定无限循环
        mAlbumAnimator!!.setRepeatMode(ObjectAnimator.RESTART) // 循环模式
        mAlbumAnimator!!.setInterpolator(LinearInterpolator()) // 匀速

        //留声机开始动画
        mPlayAnimator = ObjectAnimator.ofFloat(mGramoView, "rotation", 0.0f, 20.0f)
        mPlayAnimator!!.setDuration(400) //设定转一圈的时间
        mPlayAnimator!!.setInterpolator(AccelerateInterpolator()) // 加速

        //留声机结束动画
        mPauseAnimator = ObjectAnimator.ofFloat(mGramoView, "rotation", 20.0f, 0f)
        mPauseAnimator!!.setDuration(400) //设定转一圈的时间
        mPauseAnimator!!.setInterpolator(AccelerateInterpolator()) // 加速
        mGramoView!!.setPivotX(55f)
        mGramoView!!.setPivotY(90f)
        mGramoView!!.bringToFront()
    }

    fun startAlbumAnimator() {
        mIsPlayingAlbumAnim = true
        if (mAlbumAnimator!!.getCurrentPlayTime() != 0L) {
            mAlbumAnimator!!.resume()
        } else {
            mAlbumAnimator!!.start()
        }
    }

    fun stopAlbumAnimator() {
        mIsPlayingAlbumAnim = false
        mAlbumAnimator!!.cancel()
        mAlbumAnimator!!.setCurrentPlayTime(0)
    }

    fun resumeAlbumAnimator() {
        mAlbumAnimator!!.resume()
    }

    fun pauseAlbumAnimator() {
        mAlbumAnimator!!.pause()
    }

    fun startPlayAnimator() {
        val playBeginTime = mPauseAnimator!!.getCurrentPlayTime()
        mPlayAnimator!!.setCurrentPlayTime(playBeginTime)
        mPlayAnimator!!.start()
        mGramoViewStoped = false
    }

    val isPlayAnimatorStoped: Boolean
        get() = mPlayAnimator!!.getDuration() == mPlayAnimator!!.getCurrentPlayTime()

    fun stopPlayAnimator() {
        mPlayAnimator!!.cancel()
        mPlayAnimator!!.setCurrentPlayTime(0)
    }

    fun startPauseAnimator() {
        val playBeginTime = mPlayAnimator!!.getCurrentPlayTime()
        mPauseAnimator!!.setCurrentPlayTime(playBeginTime)
        mPauseAnimator!!.start()
        mGramoViewStoped = true
    }

    fun stopPauseAnimator() {
        mPauseAnimator!!.cancel()
        mPauseAnimator!!.setCurrentPlayTime(0)
    }

    override fun onPause() {
        super.onPause()
        //需要停止循环动画
        if (mIsPlayingAlbumAnim) {
            stopAlbumAnimator()
        }
    }

    override fun onResume() {
        super.onResume()
        //开启循环动画
        if (!mIsPlayingAlbumAnim && MusicPlayerService.Companion.isPlaying) {
            startAlbumAnimator()
            if (mGramoViewStoped) {
                startPlayAnimator()
            }
        }
    }

    companion object {
        fun newInstance(isPlaying: Boolean): Fragment {
            val fragment = AlbumFragment()
            val bundle = Bundle()
            bundle.putBoolean("state", isPlaying)
            fragment.setArguments(bundle)
            return fragment
        }
    }
}
