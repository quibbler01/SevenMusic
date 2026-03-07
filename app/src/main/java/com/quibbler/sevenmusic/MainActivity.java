package com.quibbler.sevenmusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.sevenvideoview.SevenVideoPlayerManager;
import com.quibbler.sevenmusic.activity.ActivityCollector;
import com.quibbler.sevenmusic.activity.SearchMainActivity;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.activity.sidebar.AboutAppActivity;
import com.quibbler.sevenmusic.activity.sidebar.MusicAlarmActivity;
import com.quibbler.sevenmusic.activity.sidebar.MusicRecognitionActivity;
import com.quibbler.sevenmusic.activity.sidebar.ScanCaptureActivity;
import com.quibbler.sevenmusic.activity.sidebar.SettingActivity;
import com.quibbler.sevenmusic.adapter.MainAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.fragment.found.FoundFragment;
import com.quibbler.sevenmusic.fragment.mv.MvFragment;
import com.quibbler.sevenmusic.fragment.my.MyFragment;
import com.quibbler.sevenmusic.presenter.MusicPresnter;
import com.quibbler.sevenmusic.service.AsyncService;
import com.quibbler.sevenmusic.service.LockScreenService;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.ResUtil;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;
import com.quibbler.sevenmusic.utils.ThreadDispatcher;
import com.quibbler.sevenmusic.view.playbar.PlaybarMusicListDialog;
import com.quibbler.sevenmusic.view.sidebar.TimingStopPlayDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_ONE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_PAUSE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_PLAY;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_PLAY_BAR_UPDATE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_TIMING_STOP_PLAY;
import static com.quibbler.sevenmusic.utils.CacheUtil.deleteMusicCache;
import static com.quibbler.sevenmusic.utils.CacheUtil.deletePictureCache;


/**
 * Package:        com.quibbler.sevenmusic
 * ClassName:      MainActivity
 * Description:    主activity类
 * Author:         guojinliang & zhaopeng & yanwuyang
 * CreateDate:     2019/9/16 17:31
 */
public class MainActivity extends AppCompatActivity implements OnClickListener {
    /**
     * 日志标识符
     */
    private static final String TAG = "MainActivity";
    /**
     * DrawerLayout布局组件实例
     */
    private DrawerLayout mDrawerLayout;
    /**
     * ViewPager组件实例
     */
    private ViewPager mMainViewPager;
    /**
     * 主页面中“我的”tab 标识实例
     */
    private ImageView mMy;
    /**
     * 主页面中“发现”tab 标识实例
     */
    private ImageView mFound;
    /**
     * 主页面中“Mv”tab 标识实例
     */
    private ImageView mMv;
    /**
     * 搜索实例
     */
    private ImageView mSearch;
    /**
     * 侧边栏按钮实例
     */
    private ImageView mSidebarMenuIv;
    /**
     * 关于实例
     */
    private LinearLayout mSidebarAboutLayout;
    /**
     * 扫一扫实例
     */
    private LinearLayout mSidebarScanLayout;
    /**
     * i8 听歌识曲实例
     */
    private LinearLayout mSidebarMusicRecognitionLayout;
    /**
     * 定时播放实例
     */
    private LinearLayout mSidebarTimingStopPlayLayout;
    private TextView mSidebarTimingStopPlayTv;
    /**
     * 音乐闹钟实例
     */
    private LinearLayout mSidebarMusicAlarmLayout;
    /**
     * 夜间模式实例
     */
    private LinearLayout mSidebarNightModeLayout;
    private ImageView mSidebarNightModeIv;
    private TextView mSidebarNightModeTv;
    /**
     * 设置实例
     */
    private LinearLayout mSidebarSettingLayout;
    /**
     * 退出实例
     */
    private LinearLayout mSidebarQuitLayout;

    /**
     * 底部音乐播放条相关组件实例
     */
    private LinearLayout mPlaybarLayout;
    private ImageView mPlaybarDiskIv;
    private TextView mPlaybarSongNameTv;
    private TextView mPlaybarSongWordTv;
    private ImageView mPlaybarStatusIv;
    private ImageView mPlaybarMusicListIv;

    /**
     * 保存my、found、mv的Fragment集合实例
     */
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    /**
     * 保存my、found、friend的ImageView集合实例
     */
    private ArrayList<ImageView> mImageViews = new ArrayList<>();

    /**
     * 倒计时定时器对象实例
     */
    private CountDownTimer mCountDownTimer;
    /**
     * 定时停止播放本地广播接收对象实例
     */
    private LocalReceiver mLocalReceiver;

    /**
     * 权限申请数组
     */
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    /**
     * 权限请求状态码
     */
    private final static int REQUEST_PERMISSION_CODE = 1;
    /**
     * app每次正常启动判断标志
     */
    private boolean mIsFirstLaunch = false;

    /**
     * 后台服务对象实例
     */
    private MusicPlayerService.MusicBinder mPlayMusicBinder = null;
    ServiceConnection mPlayMusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayMusicBinder = (MusicPlayerService.MusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayMusicBinder = null;
        }
    };

    /**
     * 锁屏服务对象实例
     */
    private LockScreenService mLockScreenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initLocalBroadcast();
        requestPermission();
        startLockScreenService();   // 开启锁屏服务

        Intent playMusicIntent = new Intent(this, MusicPlayerService.class);    // Service
        bindService(playMusicIntent, mPlayMusicServiceConnection, Context.BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
    }


    /**
     * 描述：侧边栏布局，主页面内容的初始化
     */
    private void initView() {
        initMainView();
        initSidebarView();
        initPlaybarView();
//        updateNightModeTitle();
    }


    /**
     * 描述：初始化主页面布局
     */
    private void initMainView() {
        mIsFirstLaunch = true;
        mDrawerLayout = findViewById(R.id.drawerLayout);  // 获取主页面的DrawerLayout布局
        mMainViewPager = findViewById(R.id.mainViewPager);   // 获取主页面的ViewPager组件
        mSidebarMenuIv = findViewById(R.id.sidebar_iv_menu); // 获取侧边栏菜单组件
        mMy = findViewById(R.id.my);   // 获取my、found、mv的ImageView组件
        mFound = findViewById(R.id.found);
        mMv = findViewById(R.id.mv);
        mSearch = findViewById(R.id.search);

        mImageViews.add(mMy);   // 将my、found、friend的ImageView组件添加到mImageViews集合
        mImageViews.add(mFound);
        mImageViews.add(mMv);

        MyFragment myFragment = new MyFragment();     // 获取MyFragment、FoundFragment、MvFragment对象，添加到fragments集合中
        FoundFragment foundFragment = new FoundFragment(mMainViewPager);
        MvFragment mvFragment = new MvFragment();
        mFragments.add(myFragment);
        mFragments.add(foundFragment);
        mFragments.add(mvFragment);

        MainAdapter mainAdapter = new MainAdapter(getSupportFragmentManager(), mFragments);   // 创建主页面适配器
        mMainViewPager.setAdapter(mainAdapter);
        mMainViewPager.setCurrentItem(1);
        mFound.setSelected(true);
        mMy.setOnClickListener(this);
        mFound.setOnClickListener(this);
        mMv.setOnClickListener(this);
        mSearch.setOnClickListener(this);  // 点击搜索图标，进入搜索界面
        mMainViewPager.setOffscreenPageLimit(2);  // 缓存左右2个页面

        mMainViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {   // 为主页面的ViewPager添加页面切换监听事件
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switchTabs(position);   // 切换不同tab页,即不同fragment的页面
            }
        });

        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {   // 切换时隐藏mv播放条
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                releaseVideoPlayer();
            }

            @Override
            public void onPageSelected(int position) {
                releaseVideoPlayer();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                releaseVideoPlayer();
            }
        });
        mSidebarMenuIv.setOnClickListener(this);   // 侧边栏菜单按钮监听事件
    }

    /**
     * 描述：释放mv播放器
     */
    private void releaseVideoPlayer() {
        Log.i(TAG, "releaseVideoPlayer");
        if (mFragments != null && mFragments.size() >= 3) {
            if (mFragments.get(2) instanceof MvFragment) {
                ((MvFragment) (mFragments.get(2))).releaseVideoPlayer();
            }
        }
    }

    /**
     * 描述：初始化侧边栏布局组件
     */
    private void initSidebarView() {
        mSidebarAboutLayout = findViewById(R.id.sidebar_ll_about);
        mSidebarScanLayout = findViewById(R.id.sidebar_ll_scan);
        mSidebarMusicRecognitionLayout = findViewById(R.id.sidebar_ll_music_recognition);
        mSidebarTimingStopPlayLayout = findViewById(R.id.sidebar_ll_timing_stop_play);
        mSidebarTimingStopPlayTv = findViewById(R.id.sidebar_tv_timing_stop_play);
        mSidebarMusicAlarmLayout = findViewById(R.id.sidebar_ll_music_alarm);
//        mSidebarNightModeLayout = findViewById(R.id.sidebar_ll_night_mode);
//        mSidebarNightModeIv = findViewById(R.id.sidebar_iv_night_mode);
//        mSidebarNightModeTv = findViewById(R.id.sidebar_tv_night_mode);
        mSidebarSettingLayout = findViewById(R.id.sidebar_ll_setting);
        mSidebarQuitLayout = findViewById(R.id.sidebar_ll_quit);

        mSidebarScanLayout.setOnClickListener(this); // 侧边栏-扫一扫监听方法
        mSidebarMusicRecognitionLayout.setOnClickListener(this);  // 侧边栏-听歌识曲监听方法
        mSidebarTimingStopPlayLayout.setOnClickListener(this);    // 侧边栏-定时停止播放监听方法
        mSidebarMusicAlarmLayout.setOnClickListener(this);   // 侧边栏-音乐闹钟监听方法
        mSidebarAboutLayout.setOnClickListener(this);  // 侧边栏-关于监听方法
//        mSidebarNightModeLayout.setOnClickListener(this);  // 侧边栏-夜间模式监听方法
        mSidebarSettingLayout.setOnClickListener(this);   // 侧边栏-设置按钮监听方法
        mSidebarQuitLayout.setOnClickListener(this);    // 侧边栏-退出按钮监听方法（此处需要详细考虑各种activity，实现正确退出）
    }

    /**
     * 描述：初始化底部音乐播放条组件
     */
    private void initPlaybarView() {
        mPlaybarLayout = findViewById(R.id.play_bar_layout);
        mPlaybarDiskIv = findViewById(R.id.play_bar_iv_disk);
        mPlaybarSongNameTv = findViewById(R.id.play_bar_tv_song_name);
        mPlaybarSongNameTv.setSelected(true);
        mPlaybarSongWordTv = findViewById(R.id.play_bar_tv_song_word);
        mPlaybarStatusIv = findViewById(R.id.play_bar_iv_status);
        mPlaybarMusicListIv = findViewById(R.id.play_bar_iv_music_list);

        mPlaybarLayout.setOnClickListener(this);
        mPlaybarStatusIv.setOnClickListener(this);
        mPlaybarMusicListIv.setOnClickListener(this);

        String name = String.valueOf(SharedPreferencesUtils.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_NAME, ""));
        String singer = String.valueOf(SharedPreferencesUtils.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_SINGER, ""));
        if (!TextUtils.isEmpty(name)) { // 启动时，恢复音乐播放条上的歌曲信息
            mPlaybarSongNameTv.setText(name);   // 恢复音乐播放条上的歌曲名字
            mPlaybarSongWordTv.setText(singer);   // 恢复音乐播放条上的歌曲作家
        } else { // 首次启动时为""，则是初次启动，设置默认值
            mPlaybarSongNameTv.setText(ResUtil.getString(R.string.str_play_bar_song_name));
            mPlaybarSongNameTv.setTextSize(14);
            mPlaybarSongWordTv.setText(ResUtil.getString(R.string.str_play_bar_song_word));
            mPlaybarSongWordTv.setTextSize(12);
        }
        String musicId = String.valueOf(SharedPreferencesUtils.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_ID, 0));
        if (!TextUtils.isEmpty(musicId)) { // 根据歌曲Id恢复音乐播放条上的歌曲图片url，不为空则设置歌曲图片，为空则设置默认图片
            getPlaybarMusicPictureById(Integer.parseInt(musicId));
        } else { // 设置默认图片

        }
    }

    /**
     * 描述：ViewPager页面不同fragment之间切换方法
     *
     * @param position fragment页面的索引
     */
    private void switchTabs(int position) {
        for (int i = 0; i < mImageViews.size(); i++) {
            mImageViews.get(i).setSelected(i == position);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.my) {
            mMainViewPager.setCurrentItem(0);
        } else if (id == R.id.found) {
            mMainViewPager.setCurrentItem(1);
        } else if (id == R.id.mv) {
            mMainViewPager.setCurrentItem(2);
        } else if (id == R.id.sidebar_iv_menu) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        } else if (id == R.id.search) {
            Intent searchIntent = new Intent(MainActivity.this, SearchMainActivity.class);
            startActivityForResult(searchIntent, 0);
        } else if (id == R.id.sidebar_ll_scan) {
            Intent intentScan = new Intent(MainActivity.this, ScanCaptureActivity.class);
            startActivity(intentScan);
        } else if (id == R.id.sidebar_ll_music_recognition) {
            Intent intentMusicRecognition = new Intent(MainActivity.this, MusicRecognitionActivity.class);
            startActivity(intentMusicRecognition);
        } else if (id == R.id.sidebar_ll_timing_stop_play) {
            TimingStopPlayDialog.showTimingStopPlayDialog(this);
        } else if (id == R.id.sidebar_ll_music_alarm) {
            Intent intentMusicAlarm = new Intent(MainActivity.this, MusicAlarmActivity.class);
            startActivity(intentMusicAlarm);
        } else if (id == R.id.sidebar_ll_about) {
            Intent intentAbout = new Intent(MainActivity.this, AboutAppActivity.class);
            startActivity(intentAbout);
        } else if (id == R.id.sidebar_ll_setting) {
            Intent intentSetting = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intentSetting);
        } else if (id == R.id.sidebar_ll_night_mode) {
            switchDayNightMode();
        } else if (id == R.id.sidebar_ll_quit) {
            ActivityCollector.finishAllActivity();
        } else if (id == R.id.play_bar_layout) {
            startMusicPlayActivity();
        } else if (id == R.id.play_bar_iv_status) {
            startPlaybarMusic();
        } else if (id == R.id.play_bar_iv_music_list) {
            showPlaybarMusicListDialog(this);
        }
    }

    /**
     * 描述：音乐播放条播放音乐
     */
    private void startPlaybarMusic() {
        if (!MusicPlayerService.isPlaying) {   // 如果没在播放中，立刻开始播放
            if (!mIsFirstLaunch && MusicPlayerService.getMusicInfo() != null) { // 应用内其他任何位置点击音乐播放，同步到音乐播放条显示
                MusicPlayerService.playMusic(MusicPlayerService.getMusicInfo());
            } else { // 首次进入应用时，直接点击音乐播放条，根据SharedPreference保存的歌曲url播放当前音乐
                MusicInfo musicInfo = new MusicInfo();
                String name = String.valueOf(SharedPreferencesUtils.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_NAME, ""));
                String singer = String.valueOf(SharedPreferencesUtils.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_SINGER, ""));
                String url = SharedPreferencesUtils.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_URL, "").toString();
                String id = String.valueOf(SharedPreferencesUtils.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_ID, 0));
                String path = String.valueOf(SharedPreferencesUtils.getInstance().getData(Constant.KEY_PLAY_BAR_SONG_PATH, ""));
                if (!TextUtils.isEmpty(name) && (!TextUtils.isEmpty(url) || !TextUtils.isEmpty(path))) { // SharedPreference中保存的值不为空才能进行设置
                    musicInfo.setMusicFilePath(path);
                    musicInfo.setId(id);
                    musicInfo.setSinger(singer);
                    musicInfo.setMusicSongName(name);
                    musicInfo.setUrl(url);
                    MusicPlayerService.playMusic(musicInfo);
                }
                mIsFirstLaunch = false;
            }
            mPlaybarStatusIv.setBackgroundResource(R.mipmap.music_iv_play_bar_play); // 设置播放图标（广播接收存在延迟，此处多设置一次）
            SevenVideoPlayerManager.getInstance().releaseSevenVideoPlayer();   // 暂停mv
        } else {
            MusicPlayerService.pauseMusic();
            mPlaybarStatusIv.setBackgroundResource(R.mipmap.music_iv_play_bar_pause);
        }
    }

    /**
     * 描述：音乐播放条->音乐播放界面
     */
    private void startMusicPlayActivity() {
        if (MusicPlayerService.getMusicInfo() != null) { // 必须先点击一首歌曲播放，MusicInfo才不会为空
            //本地音乐不支持弹出播放界面
//            if(!TextUtils.isEmpty(MusicPlayerService.getMusicInfo().getMusicFilePath())){
//                Toast.makeText(this, R.string.local_music_tip, Toast.LENGTH_SHORT).show();
//            }
            int musicId = Integer.parseInt(MusicPlayerService.getMusicInfo().getId());   // 音乐id
            String musicSongName = MusicPlayerService.getMusicInfo().getMusicSongName();  // 音乐名字
            String pictureUrl = MusicPlayerService.getMusicInfo().getAlbumPicUrl();  // 专辑封面Url
            Artist artist = new Artist(0, MusicPlayerService.getMusicInfo().getSinger());   // 音乐歌手
            List<Artist> artistList = new ArrayList<>();
            artistList.add(artist);
            MvMusicInfo mvMusicInfo = new MvMusicInfo(musicId, musicSongName, pictureUrl, artistList);
            ActivityStart.startMusicPlayActivity(this, mvMusicInfo);
        }
    }

    /**
     * 描述：根据app当前是夜间模式or日间模式，设置对应的文字标题
     */
    private void updateNightModeTitle() {
        boolean nightMode = (Boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_NIGHT_MODE, false);  // 初始化时，读取保存的夜间模式键值，根据键值为true还是false，设置相应的日间or夜间模式
        if (nightMode) {
            mSidebarNightModeTv.setText(ResUtil.getString(R.string.str_day_mode)); // 夜间模式时，将文字设置为日间模式
            mSidebarNightModeIv.setImageResource(R.mipmap.sidebar_day_mode);
        } else {
            mSidebarNightModeTv.setText(ResUtil.getString(R.string.str_night_mode)); // 日间模式时，将文字设置为夜间模式
            mSidebarNightModeIv.setImageResource(R.mipmap.sidebar_night_mode);
        }
    }

    /**
     * 描述：日间模式、夜间模式切换
     */
    private void switchDayNightMode() {
        int currentMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentMode != Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);   // 保存夜间模式状态,Application中可以根据这个值判断是否设置夜间模式
            mSidebarNightModeIv.setImageResource(R.mipmap.sidebar_night_mode);
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_NIGHT_MODE, true);    // ThemeConfig主题配置，这里只是保存了是否是夜间模式的boolean值
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            mSidebarNightModeIv.setImageResource(R.mipmap.sidebar_day_mode);
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_NIGHT_MODE, false);
        }
        recreate();
    }

    /**
     * 描述：跳转到哪个fragment
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * 描述：动态申请权限
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
    }

    /**
     * 开启锁屏服务
     */
    private void startLockScreenService() {
        Intent lockIntent = new Intent(this, LockScreenService.class);
        startService(lockIntent);
    }

    /**
     * 退出界面，但不退出应用,Service继续在后台执行
     */
    @Override
    public void onBackPressed() {
        // 在全屏或者小窗口时按返回键要先退出全屏或小窗口，
        if (SevenVideoPlayerManager.getInstance().onBackPressd()) {
            return;
        }
        moveTaskToBack(true);
    }

    /**
     * 描述：初始化本地广播，用于监听来自定时停止播放的广播事件、实现同步显示设置的倒计时时间，音乐点击播放与暂停广播时间，实现底部音乐播放条的同步显示
     */
    private void initLocalBroadcast() {
        mLocalReceiver = new LocalReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MUSIC_GLOBAL_TIMING_STOP_PLAY);
        intentFilter.addAction(MUSIC_GLOBAL_PLAY_BAR_UPDATE);
        intentFilter.addAction(MUSIC_GLOBAL_PLAY);
        intentFilter.addAction(MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_ONE);
        intentFilter.addAction(MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO);
        intentFilter.addAction(MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE);
        MusicBroadcastManager.registerMusicBroadcastReceiver(mLocalReceiver, intentFilter);
    }

    /**
     * 描述：内部类-用于接收来自定时停止播放设置的时间消息
     */
    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case MUSIC_GLOBAL_TIMING_STOP_PLAY: // 匹配来自定时停止播放的广播消息
                        startCountDownTime(intent);
                        break;
                    case MUSIC_GLOBAL_PLAY: // 匹配来自音乐播放条播放列表的广播消息
                        updatePlaybarInfo();
                        break;
                    case MUSIC_GLOBAL_PAUSE:
                        mPlaybarStatusIv.setBackgroundResource(R.mipmap.music_iv_play_bar_pause); // 监听到暂停播放广播，修改播放条暂停图标
                        break;
                    case MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_ONE:
                        mMainViewPager.setCurrentItem(0);
                        break;
                    case MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO:
                        mMainViewPager.setCurrentItem(1);
                        break;
                    case MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE:
                        mMainViewPager.setCurrentItem(2);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 描述：开启定时播放
     *
     * @param intent
     */
    private void startCountDownTime(Intent intent) {
        String close = intent.getStringExtra("close");
        long time = intent.getLongExtra("time", 0);
        if (ResUtil.getString(R.string.str_timing_stop_play_close).equals(close) && mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mSidebarTimingStopPlayTv.setText("");// 不开启时，显示为空
        } else {
            if (mCountDownTimer == null) { // 第一次设置定时播放，直接启动
                getCountDownTime(time);
            } else { // 重复设置定时播放，需要先cancel,再启动
                mCountDownTimer.cancel();
                getCountDownTime(time);
            }
        }
    }

    /**
     * 描述：显示定时停止播放设定的倒计时时间
     *
     * @param timeStep 倒计时时间（单位：ms）
     */
    private void getCountDownTime(long timeStep) {
        mCountDownTimer = new CountDownTimer(timeStep, 1000) {
            @Override
            public void onTick(long l) {
                long day = l / (1000 * 24 * 60 * 60); // 单位天
                long hour = (l - day * (1000 * 24 * 60 * 60)) / (1000 * 60 * 60); // 单位时
                long minute = (l - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60)) / (1000 * 60); // 单位分
                long second = (l - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60) - minute * (1000 * 60)) / 1000;// 单位秒
                mSidebarTimingStopPlayTv.setText(hour + ":" + minute + ":" + second);
            }

            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this, ResUtil.getString(R.string.str_timing_stop_play_tips), Toast.LENGTH_LONG).show();
                MusicPlayerService.pauseMusic(); // 暂停音乐播放
                mSidebarTimingStopPlayTv.setText("");// 倒计时结束，显示为空
            }
        };
        mCountDownTimer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLockScreenService();
        updatePlaybarInfo();
    }

    /**
     * 描述：实时更新音乐播放条的状态信息
     */
    private void updatePlaybarInfo() {
        if (MusicPlayerService.getMusicInfo() != null) { // 实现在音乐播放条上更新显示最新播放歌曲的名字、作家、图片等信息
            mPlaybarSongNameTv.setText(MusicPlayerService.getMusicInfo().getMusicSongName());
            mPlaybarSongWordTv.setText(MusicPlayerService.getMusicInfo().getSinger());
            getPlaybarMusicPictureById(Integer.valueOf(MusicPlayerService.getMusicInfo().getId()));
            if (MusicPlayerService.isPlaying) {
                mPlaybarStatusIv.setBackgroundResource(R.mipmap.music_iv_play_bar_play);
            } else {
                mPlaybarStatusIv.setBackgroundResource(R.mipmap.music_iv_play_bar_pause);
            }

            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_PLAY_BAR_SONG_ID, Integer.valueOf(MusicPlayerService.getMusicInfo().getId())); // 保存音乐播放条上的歌曲id，以Integer型保存
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_PLAY_BAR_SONG_NAME, MusicPlayerService.getMusicInfo().getMusicSongName()); // 保存音乐播放条上的歌曲名字
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_PLAY_BAR_SONG_SINGER, MusicPlayerService.getMusicInfo().getSinger());  // 保存音乐播放条上的歌曲作家
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_PLAY_BAR_SONG_URL, MusicPlayerService.getMusicInfo().getUrl()); // 保存音乐播放条上的歌曲url
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_PLAY_BAR_SONG_PATH, MusicPlayerService.getMusicInfo().getMusicFilePath()); // 保存音乐播放条上的歌曲path
        }
    }

    /**
     * 描述：根据歌曲id，在线获取歌曲图片,在音乐播放条上填充显示
     *
     * @param musicId
     */
    private void getPlaybarMusicPictureById(int musicId) {
        MvMusicInfo mvMusicInfo = new MvMusicInfo(musicId, "", "", null); // 构造MvMusicInfo对象，用于填充音乐播放条上的歌曲图片
        MusicPresnter.getMusicPicture(mvMusicInfo, new MusicCallBack() {
            @Override
            public void onMusicInfoCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!MainActivity.this.isDestroyed()) { // Glide的使用一定是在当前MainActivity不Destroyed情况下
                            Glide.with(MainActivity.this)
                                    .load(mvMusicInfo.getPictureUrl())
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(mPlaybarDiskIv);// 对音乐播放条上的mMusicPlayBarDiskIv填充图片
                        }
                    }
                });
            }
        });
    }

    /**
     * 描述：显示音乐播放条播放列表对话框
     *
     * @param context
     */
    private void showPlaybarMusicListDialog(Context context) {
        PlaybarMusicListDialog playBarMusicListDialog = new PlaybarMusicListDialog(context);
        playBarMusicListDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoClearCache();
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mLocalReceiver);

        releaseCountDownTimer();
        releaseMusicService();

        ThreadDispatcher.getInstance().stop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * 描述:释放倒计时定时器资源
     */
    private void releaseCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    /**
     * 描述：释放音乐service资源
     */
    private void releaseMusicService() {
        Log.i(TAG, "releaseMusicService");
        mPlayMusicBinder = null;
        unbindService(mPlayMusicServiceConnection);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 5)
    public void onMessageEvent(String event) {
        if (MusicPlayerService.isPlaying && TextUtils.equals(event, "mv start")) {
            MusicPlayerService.pauseMusic();
            mPlaybarStatusIv.setBackgroundResource(R.mipmap.music_iv_play_bar_pause);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_PERMISSION_CODE:
                    Intent asyncIntent = new Intent(this, AsyncService.class);
                    asyncIntent.putExtra(AsyncService.KEY, AsyncService.COMMAND_SYNC_SINGER);
                    startService(asyncIntent);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 描述：选中自动清除缓存选项时，应用退出，自动清空缓存。
     */
    private void autoClearCache() {
        boolean isCheckAutoClearCache = (boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_CACHE_AUTO_CLEAR, false);
        Log.d("MusicApplication", "isCheckAutoClearCache : " + isCheckAutoClearCache);
        if (isCheckAutoClearCache) {
            deletePictureCache();
            deleteMusicCache();
        }
    }
}
