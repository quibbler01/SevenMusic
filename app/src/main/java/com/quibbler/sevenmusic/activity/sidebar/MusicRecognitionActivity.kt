package com.quibbler.sevenmusic.activity.sidebar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.google.android.material.tabs.TabLayout;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.sidebar.MusicRecognitionAdapter;
import com.quibbler.sevenmusic.utils.ResUtil;
import com.quibbler.sevenmusic.view.sidebar.RippleAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Package:        com.quibbler.music
 * ClassName:      ListenIdentifySongsActivity
 * Description:    听歌识曲
 * Author:         11103876
 * CreateDate:     2019/9/29 11:45
 */
public class MusicRecognitionActivity extends AppCompatActivity implements View.OnClickListener, IACRCloudListener {
    private static String TAG = "MusicRecognitionActivity";

    /**
     * tab标题实例
     */
    private TabLayout mSidebarRecognitionTab;
    /**
     * ViewPager实例
     */
    private ViewPager mSidebarRecognitionViewPager;

    private ImageView mSidebarAutoRecognitionIv;
    private RippleAnimationView mRippleAutoRecognitionAnimationView;
    private TextView mSidebarAutoRecognitionClickTv;
    private TextView mSidebarAutoRecognitionTipsTv;

    private ImageView mSidebarHummingRecognitionIv;
    private RippleAnimationView mRippleHummingRecognitionAnimationView;
    private TextView mSidebarHummingRecognitionClickTv;
    private TextView mSidebarHummingRecognitionTipsTv;

    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mBuilder;

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;

    private boolean mProcessing = false;
    private boolean initState = false;
    /**
     * 听歌识曲状态-（true正在进行听歌识曲，false停止识曲）
     */
    private boolean isAutoRecognition = false;
    /**
     * 哼唱识曲状态-（true正在进行哼唱识曲，false停止识曲）
     */
    private boolean isHummingRecognition = false;
    /**
     * acrcloud模型存储路径
     */
    private String path = "";
    /**
     * 歌曲识别结果
     */
    private String mIdentifySongResult = "\n";
    private long startTime = 0;
    private long stopTime = 0;
    /**
     * 权限申请数组
     */
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.RECORD_AUDIO};
    /**
     * 权限请求状态码
     */
    private static int REQUEST_PERMISSION_CODE = 1;
    /**
     * ViewPager适配器实例
     */
    private MusicRecognitionAdapter mAdapter;
    /**
     * 存储ViewPager下子视图集合实例
     */
    public ArrayList<View> mViews = new ArrayList<>();
    /**
     * ViewPager子视图的标题集合
     */
    private String[] mTitles = {"听歌识曲", "哼唱识曲"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_music_recognition_activity);
        //checkPermission();
        initView();
        initData();
    }

    /**
     * 描述：初始化组件
     */
    private void initView() {
        Log.d(TAG, "initView");
        View autoRecognitionView = LayoutInflater.from(this).inflate(R.layout.sidebar_music_auto_recognition_tab, null, false);
        View hummingRecognitionView = LayoutInflater.from(this).inflate(R.layout.sidebar_music_humming_recognition_tab, null, false);
        mViews.add(autoRecognitionView);
        mViews.add(hummingRecognitionView);

        mSidebarRecognitionViewPager = findViewById(R.id.sidebar_vp_music_recognition);
        mSidebarRecognitionTab = findViewById(R.id.sidebar_tab_music_recognition);
        // 听歌识曲组件
        mRippleAutoRecognitionAnimationView = autoRecognitionView.findViewById(R.id.music_ripple_auto_recognition_animation);
        mSidebarAutoRecognitionIv = autoRecognitionView.findViewById(R.id.music_iv_auto_recognition_icon);
        mSidebarAutoRecognitionClickTv = autoRecognitionView.findViewById(R.id.music_tv_auto_recognition_click);
        mSidebarAutoRecognitionTipsTv = autoRecognitionView.findViewById(R.id.music_tv_auto_recognition_tips);
        // 哼唱识曲组件
        mRippleHummingRecognitionAnimationView = hummingRecognitionView.findViewById(R.id.music_ripple_humming_recognition_animation);
        mSidebarHummingRecognitionIv = hummingRecognitionView.findViewById(R.id.music_iv_humming_recognition_icon);
        mSidebarHummingRecognitionClickTv = hummingRecognitionView.findViewById(R.id.music_tv_humming_recognition_click);
        mSidebarHummingRecognitionTipsTv = hummingRecognitionView.findViewById(R.id.music_tv_humming_recognition_tips);

        //设置监听事件
        mSidebarAutoRecognitionIv.setOnClickListener(this);
        mSidebarHummingRecognitionIv.setOnClickListener(this);

        mAdapter = new MusicRecognitionAdapter(mTitles, mViews);

        mSidebarRecognitionViewPager.setAdapter(mAdapter);
        mSidebarRecognitionViewPager.setCurrentItem(0); // 默认显示第一个页卡
        mSidebarRecognitionViewPager.setOffscreenPageLimit(0);

        mSidebarRecognitionTab.setupWithViewPager(mSidebarRecognitionViewPager);
        mSidebarRecognitionTab.setTabTextColors(Color.WHITE, Color.RED);

        mAlertDialog = null;
        mBuilder = new AlertDialog.Builder(this);
    }

    /**
     * 描述：初始化数据
     */
    private void initData() {
        Log.d(TAG, "initData");
        path = Environment.getExternalStorageDirectory().toString()
                + "/acrcloud/model";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        this.mConfig = new ACRCloudConfig();
        this.mConfig.acrcloudListener = this;
        this.mConfig.context = this;
        this.mConfig.host = "identify-cn-north-1.acrcloud.com"; // 远端host地址
        this.mConfig.dbPath = path;
        this.mConfig.accessKey = "87a438e1665a041615894dea96e41d72";  // 远端host访问key
        this.mConfig.accessSecret = "W5fRyv236UPauzOi8OqKSUWSDgpvY7uVmHl5mhYo";   // 远端host访问secret
        this.mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTPS;
        this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;

        this.mClient = new ACRCloudClient();
        this.initState = this.mClient.initWithConfig(this.mConfig);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.music_iv_auto_recognition_icon) {
            if (mRippleAutoRecognitionAnimationView.isRippleRunning()) {
                mRippleAutoRecognitionAnimationView.stopRippleAnimation();
                mSidebarAutoRecognitionClickTv.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition));
                mSidebarAutoRecognitionTipsTv.setText(ResUtil.getString(R.string.str_music_recognition_surround_song));
                isAutoRecognition = false;
                cancelRecognition();// 停止识别歌曲
            } else {
                mRippleAutoRecognitionAnimationView.startRippleAnimation();
                mSidebarAutoRecognitionClickTv.setText(ResUtil.getString(R.string.str_music_recognition_identifying));
                mSidebarAutoRecognitionTipsTv.setText(ResUtil.getString(R.string.str_music_recognition_click_stop));
                isAutoRecognition = true;
                startAutoRecognition();//识别歌曲
            }
        } else if (v.getId() == R.id.music_iv_humming_recognition_icon) {
            if (mRippleHummingRecognitionAnimationView.isRippleRunning()) {
                mRippleHummingRecognitionAnimationView.stopRippleAnimation();
                mSidebarHummingRecognitionClickTv.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition));
                mSidebarHummingRecognitionTipsTv.setText(ResUtil.getString(R.string.str_music_recognition_humming_song));
                isHummingRecognition = false;
                cancelRecognition();// 停止识别歌曲
            } else {
                mRippleHummingRecognitionAnimationView.startRippleAnimation();
                mSidebarHummingRecognitionClickTv.setText(ResUtil.getString(R.string.str_music_recognition_identifying));
                mSidebarHummingRecognitionTipsTv.setText(ResUtil.getString(R.string.str_music_recognition_click_stop));
                isHummingRecognition = true;
                startHummingRecognition();// 识别哼唱歌曲，不一定识别出结果
            }
        }
    }

    /**
     * 描述：动态权限申请
     * 此处注意一定注意RECORD_AUDIO权限的申请，否则报错
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

    }


    /**
     * 描述：自动进行歌曲识别
     * <p>
     * 搜索方法：
     * 1.startRecognize()：该方法将自动开始记录和识别过程，生成结果后，将调用ACRCloudConfig中的IACRCloudListener回调对象的回调函数。
     * 2.stopRecordToRecognize()：此功能仅适用于嗡嗡声识别，它将停止录制并立即开始识别。
     * 3.recognize(byte[] buffer):识别存储在buffer参数中的音频内容,音频格式：RIFF，PCM，16位，单声道8000 Hz,它不再开始录制过程。
     * 这是一个同步函数，它将等待直到结果返回;由于“ Android UI主线程”无法发送网络请求，因此必须在子线程中调用此函数。
     * 4.startPreRecord(int recordTimeMS):通过麦克风预先录制音频，使识别速度更快;参数recordTimeMS：最近x毫秒记录的音频。
     */
    private void startAutoRecognition() {
        if (!this.initState) {
            Toast.makeText(this, ResUtil.getString(R.string.str_music_recognition_init_error), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "startAutoRecognition");
        if (!mProcessing) {
            mProcessing = true;
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false;
                Toast.makeText(this, ResUtil.getString(R.string.str_music_recognition_network_request_error), Toast.LENGTH_SHORT).show();
            }
            startTime = System.currentTimeMillis();
        }

    }


    /**
     * 描述：嗡嗡识别歌曲
     */
    private void startHummingRecognition() {
        if (mProcessing && this.mClient != null) {
            Log.d(TAG, "startHummingRecognition");
//            this.mClient.stopRecordToRecognize();
            this.mClient.startRecognize();
        }
        mProcessing = false;
        stopTime = System.currentTimeMillis();
    }

    /**
     * 描述：取消识别歌曲
     */
    private void cancelRecognition() {
        Log.d(TAG, "cancelRecognition");
        if (mProcessing && this.mClient != null) {
            mProcessing = false;
            this.mClient.cancel();
        }
    }

    /**
     * 描述：如果在调用startRecognize函数期间和之后有任何结果，则将调用此函数。
     * 返回数据格式：
     * <p>
     * "status": {
     * "code":1001,
     * "msg":"NoResult",
     * "version":"1.0",
     * }
     * }
     *
     * @param result 识别出的歌曲结果
     */
    @Override
    public void onResult(String result) {
        if (this.mClient != null) {
            this.mClient.cancel();
            mProcessing = false;
        }
        mIdentifySongResult = "\n";

        try {
            JSONObject totalResult = new JSONObject(result);
            JSONObject status = totalResult.getJSONObject("status");
            int code = status.getInt("code");
//            String msg = status.getString("msg");
//            String version = status.getString("version");
            if (code == 0) { // 识别到歌曲
                mSidebarHummingRecognitionClickTv.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition));// 识别到歌曲后，首先将标题信息修改为“点击识别音乐”
                JSONObject metadata = totalResult.getJSONObject("metadata");
                if (metadata.has("humming")) {  // 人声识别
                    JSONArray hummings = metadata.getJSONArray("humming");
                    for (int i = 0; i < hummings.length(); i++) {
                        JSONObject tt = (JSONObject) hummings.get(i);
                        String title = tt.getString("title");
                        JSONArray artists = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artists.get(0);
                        String artist = art.getString("name");
                        mIdentifySongResult = mIdentifySongResult + (i + 1) + ". " + title + "\n";
                    }
                }

                if (metadata.has("music")) {  // 歌曲识别
                    JSONArray musics = metadata.getJSONArray("music");
                    for (int i = 0; i < musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title"); // 获得歌曲名称
                        JSONArray artists = tt.getJSONArray("artists");// artists是一个数组对象，里面包含有识别出的歌曲作者相关信息，根据第一个name属性可获得作者
                        JSONObject art = (JSONObject) artists.get(0);
                        String artist = art.getString("name"); // 获取歌曲作者
                        mIdentifySongResult = mIdentifySongResult + (i + 1) + ".  Title: " + title + "    Artist: " + artist + "\n";
                    }
                }

                if (metadata.has("streams")) {   // 声音streams
                    JSONArray musics = metadata.getJSONArray("streams");
                    for (int i = 0; i < musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        String channelId = tt.getString("channel_id");
                        mIdentifySongResult = mIdentifySongResult + (i + 1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n";
                    }
                }
                if (metadata.has("custom_files")) {
                    JSONArray musics = metadata.getJSONArray("custom_files");
                    for (int i = 0; i < musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        mIdentifySongResult = mIdentifySongResult + (i + 1) + ".  Title: " + title + "\n";
                    }
                }
                showIdentitySongDialog(mIdentifySongResult);// 显示识别出的歌曲信息
            } else { // 未识别出歌曲
                mIdentifySongResult = result;
            }
        } catch (JSONException e) {
            mIdentifySongResult = result;
            e.printStackTrace();
        }
        Log.i(TAG, "onResult: " + mIdentifySongResult);
    }

    /**
     * 描述：此功能旨在告诉您录音时的音量。
     * 您可以使用实时音量信息来创建交互式UI。
     *
     * @param volume 录音的音量大小
     */
    @Override
    public void onVolumeChanged(double volume) {
        long time = (System.currentTimeMillis() - startTime) / 1000;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    Toast.makeText(this, ResUtil.getString(R.string.str_toast_permission)
                            + permissions[i] + ResUtil.getString(R.string.str_toast_permission_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, ResUtil.getString(R.string.str_toast_permission)
                            + permissions[i] + ResUtil.getString(R.string.str_toast_permission_fail), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 描述：使用对话框显示歌曲识别结果
     *
     * @param msg 歌曲识别结果
     */
    private void showIdentitySongDialog(String msg) {
        Log.d(TAG, "showIdentitySongDialog");
        if (isAutoRecognition) { // 听歌识曲-识别到结果时，立即停止动画，并设置相应文字显示
            mRippleAutoRecognitionAnimationView.stopRippleAnimation();//识别到歌曲结果，点击确定，停止波纹动画
            mSidebarAutoRecognitionClickTv.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition));
            mSidebarAutoRecognitionTipsTv.setText(ResUtil.getString(R.string.str_music_recognition_surround_song));
            isAutoRecognition = false;
        }
        if (isHummingRecognition) { // 哼唱识曲-识别到结果时，立即停止动画，并设置相应文字显示
            mRippleHummingRecognitionAnimationView.stopRippleAnimation();
            mSidebarHummingRecognitionClickTv.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition));
            mSidebarHummingRecognitionTipsTv.setText(ResUtil.getString(R.string.str_music_recognition_humming_song));
            isHummingRecognition = false;
        }
        mAlertDialog = mBuilder.setTitle(ResUtil.getString(R.string.str_music_recognition_dialog_title)).setMessage(msg).setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelRecognition();// 停止识别歌曲
            }
        }).create();
        mAlertDialog.show();
    }


}
