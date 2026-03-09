package com.quibbler.sevenmusic

import android.Manifest.permission
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.sevenvideoview.SevenVideoPlayerManager
import com.quibbler.sevenmusic.activity.ActivityCollector
import com.quibbler.sevenmusic.activity.SearchMainActivity
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.activity.sidebar.AboutAppActivity
import com.quibbler.sevenmusic.activity.sidebar.MusicAlarmActivity
import com.quibbler.sevenmusic.activity.sidebar.MusicRecognitionActivity
import com.quibbler.sevenmusic.activity.sidebar.ScanCaptureActivity
import com.quibbler.sevenmusic.activity.sidebar.SettingActivity
import com.quibbler.sevenmusic.adapter.MainAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.fragment.found.FoundFragment
import com.quibbler.sevenmusic.fragment.mv.MvFragment
import com.quibbler.sevenmusic.fragment.my.MyFragment
import com.quibbler.sevenmusic.presenter.MusicPresnter
import com.quibbler.sevenmusic.service.AsyncService
import com.quibbler.sevenmusic.service.LockScreenService
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.service.MusicPlayerService.MusicBinder
import com.quibbler.sevenmusic.utils.CacheUtil
import com.quibbler.sevenmusic.utils.ResUtil
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils
import com.quibbler.sevenmusic.utils.ThreadDispatcher
import com.quibbler.sevenmusic.view.playbar.PlaybarMusicListDialog
import com.quibbler.sevenmusic.view.sidebar.TimingStopPlayDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Package:        com.quibbler.sevenmusic
 * ClassName:      MainActivity
 * Description:    主activity类
 * Author:         guojinliang & zhaopeng & yanwuyang
 * CreateDate:     2019/9/16 17:31
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    /**
     * DrawerLayout布局组件实例
     */
    private var mDrawerLayout: DrawerLayout? = null

    /**
     * ViewPager组件实例
     */
    private var mMainViewPager: ViewPager? = null

    /**
     * 主页面中“我的”tab 标识实例
     */
    private var mMy: ImageView? = null

    /**
     * 主页面中“发现”tab 标识实例
     */
    private var mFound: ImageView? = null

    /**
     * 主页面中“Mv”tab 标识实例
     */
    private var mMv: ImageView? = null

    /**
     * 搜索实例
     */
    private var mSearch: ImageView? = null

    /**
     * 侧边栏按钮实例
     */
    private var mSidebarMenuIv: ImageView? = null

    /**
     * 关于实例
     */
    private var mSidebarAboutLayout: LinearLayout? = null

    /**
     * 扫一扫实例
     */
    private var mSidebarScanLayout: LinearLayout? = null

    /**
     * i8 听歌识曲实例
     */
    private var mSidebarMusicRecognitionLayout: LinearLayout? = null

    /**
     * 定时播放实例
     */
    private var mSidebarTimingStopPlayLayout: LinearLayout? = null
    private var mSidebarTimingStopPlayTv: TextView? = null

    /**
     * 音乐闹钟实例
     */
    private var mSidebarMusicAlarmLayout: LinearLayout? = null

    /**
     * 夜间模式实例
     */
    private val mSidebarNightModeLayout: LinearLayout? = null
    private val mSidebarNightModeIv: ImageView? = null
    private val mSidebarNightModeTv: TextView? = null

    /**
     * 设置实例
     */
    private var mSidebarSettingLayout: LinearLayout? = null

    /**
     * 退出实例
     */
    private var mSidebarQuitLayout: LinearLayout? = null

    /**
     * 底部音乐播放条相关组件实例
     */
    private var mPlaybarLayout: LinearLayout? = null
    private var mPlaybarDiskIv: ImageView? = null
    private var mPlaybarSongNameTv: TextView? = null
    private var mPlaybarSongWordTv: TextView? = null
    private var mPlaybarStatusIv: ImageView? = null
    private var mPlaybarMusicListIv: ImageView? = null

    /**
     * 保存my、found、mv的Fragment集合实例
     */
    private val mFragments: ArrayList<Fragment?>? = ArrayList<Fragment?>()

    /**
     * 保存my、found、friend的ImageView集合实例
     */
    private val mImageViews = ArrayList<ImageView?>()

    /**
     * 倒计时定时器对象实例
     */
    private var mCountDownTimer: CountDownTimer? = null

    /**
     * 定时停止播放本地广播接收对象实例
     */
    private var mLocalReceiver: LocalReceiver? = null

    /**
     * app每次正常启动判断标志
     */
    private var mIsFirstLaunch = false

    /**
     * 后台服务对象实例
     */
    private var mPlayMusicBinder: MusicBinder? = null
    var mPlayMusicServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayMusicBinder = service as MusicBinder?
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mPlayMusicBinder = null
        }
    }

    /**
     * 锁屏服务对象实例
     */
    private val mLockScreenService: LockScreenService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initLocalBroadcast()
        requestPermission()
        startLockScreenService() // 开启锁屏服务

        val playMusicIntent = Intent(this, MusicPlayerService::class.java) // Service
        bindService(playMusicIntent, mPlayMusicServiceConnection, BIND_AUTO_CREATE)
        EventBus.getDefault().register(this)
    }


    /**
     * 描述：侧边栏布局，主页面内容的初始化
     */
    private fun initView() {
        initMainView()
        initSidebarView()
        initPlaybarView()
        //        updateNightModeTitle();
    }


    /**
     * 描述：初始化主页面布局
     */
    private fun initMainView() {
        mIsFirstLaunch = true
        mDrawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout) // 获取主页面的DrawerLayout布局
        mMainViewPager = findViewById<ViewPager>(R.id.mainViewPager) // 获取主页面的ViewPager组件
        mSidebarMenuIv = findViewById<ImageView>(R.id.sidebar_iv_menu) // 获取侧边栏菜单组件
        mMy = findViewById<ImageView>(R.id.my) // 获取my、found、mv的ImageView组件
        mFound = findViewById<ImageView>(R.id.found)
        mMv = findViewById<ImageView>(R.id.mv)
        mSearch = findViewById<ImageView>(R.id.search)

        mImageViews.add(mMy) // 将my、found、friend的ImageView组件添加到mImageViews集合
        mImageViews.add(mFound)
        mImageViews.add(mMv)

        val myFragment = MyFragment() // 获取MyFragment、FoundFragment、MvFragment对象，添加到fragments集合中
        val foundFragment = FoundFragment(mMainViewPager)
        val mvFragment = MvFragment()
        mFragments!!.add(myFragment)
        mFragments.add(foundFragment)
        mFragments.add(mvFragment)

        val mainAdapter = MainAdapter(getSupportFragmentManager(), mFragments) // 创建主页面适配器
        mMainViewPager!!.setAdapter(mainAdapter)
        mMainViewPager!!.setCurrentItem(1)
        mFound!!.setSelected(true)
        mMy!!.setOnClickListener(this)
        mFound!!.setOnClickListener(this)
        mMv!!.setOnClickListener(this)
        mSearch!!.setOnClickListener(this) // 点击搜索图标，进入搜索界面
        mMainViewPager!!.setOffscreenPageLimit(2) // 缓存左右2个页面

        mMainViewPager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            // 为主页面的ViewPager添加页面切换监听事件
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                switchTabs(position) // 切换不同tab页,即不同fragment的页面
            }
        })

        mMainViewPager!!.setOnPageChangeListener(object : OnPageChangeListener {
            // 切换时隐藏mv播放条
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                releaseVideoPlayer()
            }

            override fun onPageSelected(position: Int) {
                releaseVideoPlayer()
            }

            override fun onPageScrollStateChanged(state: Int) {
                releaseVideoPlayer()
            }
        })
        mSidebarMenuIv!!.setOnClickListener(this) // 侧边栏菜单按钮监听事件
    }

    /**
     * 描述：释放mv播放器
     */
    private fun releaseVideoPlayer() {
        Log.i(TAG, "releaseVideoPlayer")
        if (mFragments != null && mFragments.size >= 3) {
            if (mFragments.get(2) is MvFragment) {
                ((mFragments.get(2)) as MvFragment).releaseVideoPlayer()
            }
        }
    }

    /**
     * 描述：初始化侧边栏布局组件
     */
    private fun initSidebarView() {
        mSidebarAboutLayout = findViewById<LinearLayout>(R.id.sidebar_ll_about)
        mSidebarScanLayout = findViewById<LinearLayout>(R.id.sidebar_ll_scan)
        mSidebarMusicRecognitionLayout =
            findViewById<LinearLayout>(R.id.sidebar_ll_music_recognition)
        mSidebarTimingStopPlayLayout = findViewById<LinearLayout>(R.id.sidebar_ll_timing_stop_play)
        mSidebarTimingStopPlayTv = findViewById<TextView>(R.id.sidebar_tv_timing_stop_play)
        mSidebarMusicAlarmLayout = findViewById<LinearLayout>(R.id.sidebar_ll_music_alarm)
        //        mSidebarNightModeLayout = findViewById(R.id.sidebar_ll_night_mode);
//        mSidebarNightModeIv = findViewById(R.id.sidebar_iv_night_mode);
//        mSidebarNightModeTv = findViewById(R.id.sidebar_tv_night_mode);
        mSidebarSettingLayout = findViewById<LinearLayout>(R.id.sidebar_ll_setting)
        mSidebarQuitLayout = findViewById<LinearLayout>(R.id.sidebar_ll_quit)

        mSidebarScanLayout!!.setOnClickListener(this) // 侧边栏-扫一扫监听方法
        mSidebarMusicRecognitionLayout!!.setOnClickListener(this) // 侧边栏-听歌识曲监听方法
        mSidebarTimingStopPlayLayout!!.setOnClickListener(this) // 侧边栏-定时停止播放监听方法
        mSidebarMusicAlarmLayout!!.setOnClickListener(this) // 侧边栏-音乐闹钟监听方法
        mSidebarAboutLayout!!.setOnClickListener(this) // 侧边栏-关于监听方法
        //        mSidebarNightModeLayout.setOnClickListener(this);  // 侧边栏-夜间模式监听方法
        mSidebarSettingLayout!!.setOnClickListener(this) // 侧边栏-设置按钮监听方法
        mSidebarQuitLayout!!.setOnClickListener(this) // 侧边栏-退出按钮监听方法（此处需要详细考虑各种activity，实现正确退出）
    }

    /**
     * 描述：初始化底部音乐播放条组件
     */
    private fun initPlaybarView() {
        mPlaybarLayout = findViewById<LinearLayout>(R.id.play_bar_layout)
        mPlaybarDiskIv = findViewById<ImageView>(R.id.play_bar_iv_disk)
        mPlaybarSongNameTv = findViewById<TextView>(R.id.play_bar_tv_song_name)
        mPlaybarSongNameTv!!.setSelected(true)
        mPlaybarSongWordTv = findViewById<TextView>(R.id.play_bar_tv_song_word)
        mPlaybarStatusIv = findViewById<ImageView>(R.id.play_bar_iv_status)
        mPlaybarMusicListIv = findViewById<ImageView>(R.id.play_bar_iv_music_list)

        mPlaybarLayout!!.setOnClickListener(this)
        mPlaybarStatusIv!!.setOnClickListener(this)
        mPlaybarMusicListIv!!.setOnClickListener(this)

        val name = SharedPreferencesUtils.Companion.getInstance()
            .getData(Constant.KEY_PLAY_BAR_SONG_NAME, "").toString()
        val singer = SharedPreferencesUtils.Companion.getInstance()
            .getData(Constant.KEY_PLAY_BAR_SONG_SINGER, "").toString()
        if (!TextUtils.isEmpty(name)) { // 启动时，恢复音乐播放条上的歌曲信息
            mPlaybarSongNameTv!!.setText(name) // 恢复音乐播放条上的歌曲名字
            mPlaybarSongWordTv!!.setText(singer) // 恢复音乐播放条上的歌曲作家
        } else { // 首次启动时为""，则是初次启动，设置默认值
            mPlaybarSongNameTv!!.setText(ResUtil.getString(R.string.str_play_bar_song_name))
            mPlaybarSongNameTv!!.setTextSize(14f)
            mPlaybarSongWordTv!!.setText(ResUtil.getString(R.string.str_play_bar_song_word))
            mPlaybarSongWordTv!!.setTextSize(12f)
        }
        val musicId =
            SharedPreferencesUtils.Companion.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_ID, 0)
                .toString()
        if (!TextUtils.isEmpty(musicId)) { // 根据歌曲Id恢复音乐播放条上的歌曲图片url，不为空则设置歌曲图片，为空则设置默认图片
            getPlaybarMusicPictureById(musicId.toInt())
        } else { // 设置默认图片
        }
    }

    /**
     * 描述：ViewPager页面不同fragment之间切换方法
     * 
     * @param position fragment页面的索引
     */
    private fun switchTabs(position: Int) {
        for (i in mImageViews.indices) {
            mImageViews.get(i)!!.setSelected(i == position)
        }
    }

    override fun onClick(view: View) {
        val id = view.getId()
        if (id == R.id.my) {
            mMainViewPager!!.setCurrentItem(0)
        } else if (id == R.id.found) {
            mMainViewPager!!.setCurrentItem(1)
        } else if (id == R.id.mv) {
            mMainViewPager!!.setCurrentItem(2)
        } else if (id == R.id.sidebar_iv_menu) {
            mDrawerLayout!!.openDrawer(Gravity.LEFT)
        } else if (id == R.id.search) {
            val searchIntent = Intent(this@MainActivity, SearchMainActivity::class.java)
            startActivityForResult(searchIntent, 0)
        } else if (id == R.id.sidebar_ll_scan) {
            val intentScan = Intent(this@MainActivity, ScanCaptureActivity::class.java)
            startActivity(intentScan)
        } else if (id == R.id.sidebar_ll_music_recognition) {
            val intentMusicRecognition =
                Intent(this@MainActivity, MusicRecognitionActivity::class.java)
            startActivity(intentMusicRecognition)
        } else if (id == R.id.sidebar_ll_timing_stop_play) {
            TimingStopPlayDialog.Companion.showTimingStopPlayDialog(this)
        } else if (id == R.id.sidebar_ll_music_alarm) {
            val intentMusicAlarm = Intent(this@MainActivity, MusicAlarmActivity::class.java)
            startActivity(intentMusicAlarm)
        } else if (id == R.id.sidebar_ll_about) {
            val intentAbout = Intent(this@MainActivity, AboutAppActivity::class.java)
            startActivity(intentAbout)
        } else if (id == R.id.sidebar_ll_setting) {
            val intentSetting = Intent(this@MainActivity, SettingActivity::class.java)
            startActivity(intentSetting)
        } else if (id == R.id.sidebar_ll_night_mode) {
            switchDayNightMode()
        } else if (id == R.id.sidebar_ll_quit) {
            ActivityCollector.finishAllActivity()
        } else if (id == R.id.play_bar_layout) {
            startMusicPlayActivity()
        } else if (id == R.id.play_bar_iv_status) {
            startPlaybarMusic()
        } else if (id == R.id.play_bar_iv_music_list) {
            showPlaybarMusicListDialog(this)
        }
    }

    /**
     * 描述：音乐播放条播放音乐
     */
    private fun startPlaybarMusic() {
        if (!MusicPlayerService.Companion.isPlaying) {   // 如果没在播放中，立刻开始播放
            if (!mIsFirstLaunch && MusicPlayerService.Companion.getMusicInfo() != null) { // 应用内其他任何位置点击音乐播放，同步到音乐播放条显示
                MusicPlayerService.Companion.playMusic(MusicPlayerService.Companion.getMusicInfo())
            } else { // 首次进入应用时，直接点击音乐播放条，根据SharedPreference保存的歌曲url播放当前音乐
                val musicInfo = MusicInfo()
                val name = SharedPreferencesUtils.Companion.getInstance()
                    .getData(Constant.KEY_PLAY_BAR_SONG_NAME, "").toString()
                val singer = SharedPreferencesUtils.Companion.getInstance()
                    .getData(Constant.KEY_PLAY_BAR_SONG_SINGER, "").toString()
                val url = SharedPreferencesUtils.Companion.getInstance()
                    .getData(Constant.KEY_PLAY_BAR_SONG_URL, "").toString()
                val id = SharedPreferencesUtils.Companion.getInstance()
                    .getData(Constant.KEY_PLAY_BAR_SONG_ID, 0).toString()
                val path = SharedPreferencesUtils.Companion.getInstance()
                    .getData(Constant.KEY_PLAY_BAR_SONG_PATH, "").toString()
                if (!TextUtils.isEmpty(name) && (!TextUtils.isEmpty(url) || !TextUtils.isEmpty(path))) { // SharedPreference中保存的值不为空才能进行设置
                    musicInfo.setMusicFilePath(path)
                    musicInfo.setId(id)
                    musicInfo.setSinger(singer)
                    musicInfo.setMusicSongName(name)
                    musicInfo.setUrl(url)
                    MusicPlayerService.Companion.playMusic(musicInfo)
                }
                mIsFirstLaunch = false
            }
            mPlaybarStatusIv!!.setBackgroundResource(R.mipmap.music_iv_play_bar_play) // 设置播放图标（广播接收存在延迟，此处多设置一次）
            SevenVideoPlayerManager.getInstance().releaseSevenVideoPlayer() // 暂停mv
        } else {
            MusicPlayerService.Companion.pauseMusic()
            mPlaybarStatusIv!!.setBackgroundResource(R.mipmap.music_iv_play_bar_pause)
        }
    }

    /**
     * 描述：音乐播放条->音乐播放界面
     */
    private fun startMusicPlayActivity() {
        if (MusicPlayerService.Companion.getMusicInfo() != null) { // 必须先点击一首歌曲播放，MusicInfo才不会为空
            //本地音乐不支持弹出播放界面
//            if(!TextUtils.isEmpty(MusicPlayerService.getMusicInfo().getMusicFilePath())){
//                Toast.makeText(this, R.string.local_music_tip, Toast.LENGTH_SHORT).show();
//            }
            val musicId = MusicPlayerService.Companion.getMusicInfo().getId().toInt() // 音乐id
            val musicSongName: String? =
                MusicPlayerService.Companion.getMusicInfo().getMusicSongName() // 音乐名字
            val pictureUrl: String? =
                MusicPlayerService.Companion.getMusicInfo().getAlbumPicUrl() // 专辑封面Url
            val artist = Artist(0, MusicPlayerService.Companion.getMusicInfo().getSinger()) // 音乐歌手
            val artistList: MutableList<Artist?> = ArrayList<Artist?>()
            artistList.add(artist)
            val mvMusicInfo = MvMusicInfo(musicId, musicSongName, pictureUrl, artistList)
            ActivityStart.startMusicPlayActivity(this, mvMusicInfo)
        }
    }

    /**
     * 描述：根据app当前是夜间模式or日间模式，设置对应的文字标题
     */
    private fun updateNightModeTitle() {
        val nightMode = SharedPreferencesUtils.Companion.getInstance().getData(
            Constant.KEY_NIGHT_MODE,
            false
        ) as Boolean // 初始化时，读取保存的夜间模式键值，根据键值为true还是false，设置相应的日间or夜间模式
        if (nightMode) {
            mSidebarNightModeTv!!.setText(ResUtil.getString(R.string.str_day_mode)) // 夜间模式时，将文字设置为日间模式
            mSidebarNightModeIv!!.setImageResource(R.mipmap.sidebar_day_mode)
        } else {
            mSidebarNightModeTv!!.setText(ResUtil.getString(R.string.str_night_mode)) // 日间模式时，将文字设置为夜间模式
            mSidebarNightModeIv!!.setImageResource(R.mipmap.sidebar_night_mode)
        }
    }

    /**
     * 描述：日间模式、夜间模式切换
     */
    private fun switchDayNightMode() {
        val currentMode =
            getResources().getConfiguration().uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentMode != Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // 保存夜间模式状态,Application中可以根据这个值判断是否设置夜间模式
            mSidebarNightModeIv!!.setImageResource(R.mipmap.sidebar_night_mode)
            SharedPreferencesUtils.Companion.getInstance()
                .saveData(Constant.KEY_NIGHT_MODE, true) // ThemeConfig主题配置，这里只是保存了是否是夜间模式的boolean值
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            mSidebarNightModeIv!!.setImageResource(R.mipmap.sidebar_day_mode)
            SharedPreferencesUtils.Companion.getInstance().saveData(Constant.KEY_NIGHT_MODE, false)
        }
        recreate()
    }

    /**
     * 描述：跳转到哪个fragment
     * 
     * @param intent
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    /**
     * 描述：动态申请权限
     */
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE
                )
            }
        }
    }

    /**
     * 开启锁屏服务
     */
    private fun startLockScreenService() {
        val lockIntent = Intent(this, LockScreenService::class.java)
        startService(lockIntent)
    }

    /**
     * 退出界面，但不退出应用,Service继续在后台执行
     */
    override fun onBackPressed() {
        // 在全屏或者小窗口时按返回键要先退出全屏或小窗口，
        if (SevenVideoPlayerManager.getInstance().onBackPressd()) {
            return
        }
        moveTaskToBack(true)
    }

    /**
     * 描述：初始化本地广播，用于监听来自定时停止播放的广播事件、实现同步显示设置的倒计时时间，音乐点击播放与暂停广播时间，实现底部音乐播放条的同步显示
     */
    private fun initLocalBroadcast() {
        mLocalReceiver = LocalReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(MusicBroadcastManager.MUSIC_GLOBAL_TIMING_STOP_PLAY)
        intentFilter.addAction(MusicBroadcastManager.MUSIC_GLOBAL_PLAY_BAR_UPDATE)
        intentFilter.addAction(MusicBroadcastManager.MUSIC_GLOBAL_PLAY)
        intentFilter.addAction(MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_ONE)
        intentFilter.addAction(MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO)
        intentFilter.addAction(MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE)
        MusicBroadcastManager.registerMusicBroadcastReceiver(mLocalReceiver, intentFilter)
    }

    /**
     * 描述：内部类-用于接收来自定时停止播放设置的时间消息
     */
    internal inner class LocalReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.getAction() != null) {
                when (intent.getAction()) {
                    MusicBroadcastManager.MUSIC_GLOBAL_TIMING_STOP_PLAY -> startCountDownTime(intent)
                    MusicBroadcastManager.MUSIC_GLOBAL_PLAY -> updatePlaybarInfo()
                    MusicBroadcastManager.MUSIC_GLOBAL_PAUSE -> mPlaybarStatusIv!!.setBackgroundResource(
                        R.mipmap.music_iv_play_bar_pause
                    ) // 监听到暂停播放广播，修改播放条暂停图标
                    MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_ONE -> mMainViewPager!!.setCurrentItem(
                        0
                    )

                    MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO -> mMainViewPager!!.setCurrentItem(
                        1
                    )

                    MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE -> mMainViewPager!!.setCurrentItem(
                        2
                    )

                    else -> {}
                }
            }
        }
    }

    /**
     * 描述：开启定时播放
     * 
     * @param intent
     */
    private fun startCountDownTime(intent: Intent) {
        val close = intent.getStringExtra("close")
        val time = intent.getLongExtra("time", 0)
        if (ResUtil.getString(R.string.str_timing_stop_play_close) == close && mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
            mSidebarTimingStopPlayTv!!.setText("") // 不开启时，显示为空
        } else {
            if (mCountDownTimer == null) { // 第一次设置定时播放，直接启动
                getCountDownTime(time)
            } else { // 重复设置定时播放，需要先cancel,再启动
                mCountDownTimer!!.cancel()
                getCountDownTime(time)
            }
        }
    }

    /**
     * 描述：显示定时停止播放设定的倒计时时间
     * 
     * @param timeStep 倒计时时间（单位：ms）
     */
    private fun getCountDownTime(timeStep: Long) {
        mCountDownTimer = object : CountDownTimer(timeStep, 1000) {
            override fun onTick(l: Long) {
                val day = l / (1000 * 24 * 60 * 60) // 单位天
                val hour = (l - day * (1000 * 24 * 60 * 60)) / (1000 * 60 * 60) // 单位时
                val minute =
                    (l - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60)) / (1000 * 60) // 单位分
                val second =
                    (l - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60) - minute * (1000 * 60)) / 1000 // 单位秒
                mSidebarTimingStopPlayTv!!.setText(hour.toString() + ":" + minute + ":" + second)
            }

            override fun onFinish() {
                Toast.makeText(
                    this@MainActivity,
                    ResUtil.getString(R.string.str_timing_stop_play_tips),
                    Toast.LENGTH_LONG
                ).show()
                MusicPlayerService.Companion.pauseMusic() // 暂停音乐播放
                mSidebarTimingStopPlayTv!!.setText("") // 倒计时结束，显示为空
            }
        }
        mCountDownTimer!!.start()
    }

    override fun onResume() {
        super.onResume()
        startLockScreenService()
        updatePlaybarInfo()
    }

    /**
     * 描述：实时更新音乐播放条的状态信息
     */
    private fun updatePlaybarInfo() {
        if (MusicPlayerService.Companion.getMusicInfo() != null) { // 实现在音乐播放条上更新显示最新播放歌曲的名字、作家、图片等信息
            mPlaybarSongNameTv.setText(
                MusicPlayerService.Companion.getMusicInfo().getMusicSongName()
            )
            mPlaybarSongWordTv.setText(MusicPlayerService.Companion.getMusicInfo().getSinger())
            getPlaybarMusicPictureById(MusicPlayerService.Companion.getMusicInfo().getId().toInt())
            if (MusicPlayerService.Companion.isPlaying) {
                mPlaybarStatusIv!!.setBackgroundResource(R.mipmap.music_iv_play_bar_play)
            } else {
                mPlaybarStatusIv!!.setBackgroundResource(R.mipmap.music_iv_play_bar_pause)
            }

            SharedPreferencesUtils.Companion.getInstance().saveData(
                Constant.KEY_PLAY_BAR_SONG_ID,
                MusicPlayerService.Companion.getMusicInfo().getId().toInt()
            ) // 保存音乐播放条上的歌曲id，以Integer型保存
            SharedPreferencesUtils.Companion.getInstance().saveData(
                Constant.KEY_PLAY_BAR_SONG_NAME,
                MusicPlayerService.Companion.getMusicInfo().getMusicSongName()
            ) // 保存音乐播放条上的歌曲名字
            SharedPreferencesUtils.Companion.getInstance().saveData(
                Constant.KEY_PLAY_BAR_SONG_SINGER,
                MusicPlayerService.Companion.getMusicInfo().getSinger()
            ) // 保存音乐播放条上的歌曲作家
            SharedPreferencesUtils.Companion.getInstance().saveData(
                Constant.KEY_PLAY_BAR_SONG_URL,
                MusicPlayerService.Companion.getMusicInfo().getUrl()
            ) // 保存音乐播放条上的歌曲url
            SharedPreferencesUtils.Companion.getInstance().saveData(
                Constant.KEY_PLAY_BAR_SONG_PATH,
                MusicPlayerService.Companion.getMusicInfo().getMusicFilePath()
            ) // 保存音乐播放条上的歌曲path
        }
    }

    /**
     * 描述：根据歌曲id，在线获取歌曲图片,在音乐播放条上填充显示
     * 
     * @param musicId
     */
    private fun getPlaybarMusicPictureById(musicId: Int) {
        val mvMusicInfo = MvMusicInfo(musicId, "", "", null) // 构造MvMusicInfo对象，用于填充音乐播放条上的歌曲图片
        MusicPresnter.getMusicPicture(mvMusicInfo, object : MusicCallBack {
            override fun onMusicInfoCompleted() {
                runOnUiThread(object : Runnable {
                    override fun run() {
                        if (!this@MainActivity.isDestroyed()) { // Glide的使用一定是在当前MainActivity不Destroyed情况下
                            Glide.with(this@MainActivity)
                                .load(mvMusicInfo.getPictureUrl())
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .into(mPlaybarDiskIv!!) // 对音乐播放条上的mMusicPlayBarDiskIv填充图片
                        }
                    }
                })
            }
        })
    }

    /**
     * 描述：显示音乐播放条播放列表对话框
     * 
     * @param context
     */
    private fun showPlaybarMusicListDialog(context: Context?) {
        val playBarMusicListDialog = PlaybarMusicListDialog(context)
        playBarMusicListDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        autoClearCache()
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mLocalReceiver)

        releaseCountDownTimer()
        releaseMusicService()

        ThreadDispatcher.Companion.getInstance().stop()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    /**
     * 描述:释放倒计时定时器资源
     */
    private fun releaseCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
            mCountDownTimer = null
        }
    }

    /**
     * 描述：释放音乐service资源
     */
    private fun releaseMusicService() {
        Log.i(TAG, "releaseMusicService")
        mPlayMusicBinder = null
        unbindService(mPlayMusicServiceConnection)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 5)
    fun onMessageEvent(event: String?) {
        if (MusicPlayerService.Companion.isPlaying && TextUtils.equals(event, "mv start")) {
            MusicPlayerService.Companion.pauseMusic()
            mPlaybarStatusIv!!.setBackgroundResource(R.mipmap.music_iv_play_bar_pause)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_PERMISSION_CODE -> {
                    val asyncIntent = Intent(this, AsyncService::class.java)
                    asyncIntent.putExtra(
                        AsyncService.Companion.KEY,
                        AsyncService.Companion.COMMAND_SYNC_SINGER
                    )
                    startService(asyncIntent)
                }

                else -> {}
            }
        }
    }

    /**
     * 描述：选中自动清除缓存选项时，应用退出，自动清空缓存。
     */
    private fun autoClearCache() {
        val isCheckAutoClearCache = SharedPreferencesUtils.Companion.getInstance()
            .getData(Constant.KEY_SETTING_CACHE_AUTO_CLEAR, false) as Boolean
        Log.d("MusicApplication", "isCheckAutoClearCache : " + isCheckAutoClearCache)
        if (isCheckAutoClearCache) {
            CacheUtil.deletePictureCache()
            CacheUtil.deleteMusicCache()
        }
    }

    companion object {
        /**
         * 日志标识符
         */
        private const val TAG = "MainActivity"

        /**
         * 权限申请数组
         */
        private val PERMISSIONS_STORAGE = arrayOf<String?>(permission.WRITE_EXTERNAL_STORAGE)

        /**
         * 权限请求状态码
         */
        private const val REQUEST_PERMISSION_CODE = 1
    }
}
