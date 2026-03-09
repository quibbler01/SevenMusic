package com.quibbler.sevenmusic.activity.sidebar

import android.Manifest.permission
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.acrcloud.rec.sdk.ACRCloudClient
import com.acrcloud.rec.sdk.ACRCloudConfig
import com.acrcloud.rec.sdk.IACRCloudListener
import com.google.android.material.tabs.TabLayout
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.sidebar.MusicRecognitionAdapter
import com.quibbler.sevenmusic.utils.ResUtil
import com.quibbler.sevenmusic.view.sidebar.RippleAnimationView
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * Package:        com.quibbler.music
 * ClassName:      ListenIdentifySongsActivity
 * Description:    听歌识曲
 * Author:         11103876
 * CreateDate:     2019/9/29 11:45
 */
class MusicRecognitionActivity : AppCompatActivity(), View.OnClickListener, IACRCloudListener {
    /**
     * tab标题实例
     */
    private var mSidebarRecognitionTab: TabLayout? = null

    /**
     * ViewPager实例
     */
    private var mSidebarRecognitionViewPager: ViewPager? = null

    private var mSidebarAutoRecognitionIv: ImageView? = null
    private var mRippleAutoRecognitionAnimationView: RippleAnimationView? = null
    private var mSidebarAutoRecognitionClickTv: TextView? = null
    private var mSidebarAutoRecognitionTipsTv: TextView? = null

    private var mSidebarHummingRecognitionIv: ImageView? = null
    private var mRippleHummingRecognitionAnimationView: RippleAnimationView? = null
    private var mSidebarHummingRecognitionClickTv: TextView? = null
    private var mSidebarHummingRecognitionTipsTv: TextView? = null

    private var mAlertDialog: AlertDialog? = null
    private var mBuilder: AlertDialog.Builder? = null

    private var mClient: ACRCloudClient? = null
    private var mConfig: ACRCloudConfig? = null

    private var mProcessing = false
    private var initState = false

    /**
     * 听歌识曲状态-（true正在进行听歌识曲，false停止识曲）
     */
    private var isAutoRecognition = false

    /**
     * 哼唱识曲状态-（true正在进行哼唱识曲，false停止识曲）
     */
    private var isHummingRecognition = false

    /**
     * acrcloud模型存储路径
     */
    private var path = ""

    /**
     * 歌曲识别结果
     */
    private var mIdentifySongResult: String? = "\n"
    private var startTime: Long = 0
    private var stopTime: Long = 0

    /**
     * ViewPager适配器实例
     */
    private var mAdapter: MusicRecognitionAdapter? = null

    /**
     * 存储ViewPager下子视图集合实例
     */
    var mViews: ArrayList<View?> = ArrayList<View?>()

    /**
     * ViewPager子视图的标题集合
     */
    private val mTitles = arrayOf<String?>("听歌识曲", "哼唱识曲")

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sidebar_music_recognition_activity)
        //checkPermission();
        initView()
        initData()
    }

    /**
     * 描述：初始化组件
     */
    private fun initView() {
        Log.d(TAG, "initView")
        val autoRecognitionView = LayoutInflater.from(this)
            .inflate(R.layout.sidebar_music_auto_recognition_tab, null, false)
        val hummingRecognitionView = LayoutInflater.from(this)
            .inflate(R.layout.sidebar_music_humming_recognition_tab, null, false)
        mViews.add(autoRecognitionView)
        mViews.add(hummingRecognitionView)

        mSidebarRecognitionViewPager = findViewById<ViewPager>(R.id.sidebar_vp_music_recognition)
        mSidebarRecognitionTab = findViewById<TabLayout>(R.id.sidebar_tab_music_recognition)
        // 听歌识曲组件
        mRippleAutoRecognitionAnimationView =
            autoRecognitionView.findViewById<RippleAnimationView>(R.id.music_ripple_auto_recognition_animation)
        mSidebarAutoRecognitionIv =
            autoRecognitionView.findViewById<ImageView>(R.id.music_iv_auto_recognition_icon)
        mSidebarAutoRecognitionClickTv =
            autoRecognitionView.findViewById<TextView>(R.id.music_tv_auto_recognition_click)
        mSidebarAutoRecognitionTipsTv =
            autoRecognitionView.findViewById<TextView>(R.id.music_tv_auto_recognition_tips)
        // 哼唱识曲组件
        mRippleHummingRecognitionAnimationView =
            hummingRecognitionView.findViewById<RippleAnimationView>(R.id.music_ripple_humming_recognition_animation)
        mSidebarHummingRecognitionIv =
            hummingRecognitionView.findViewById<ImageView>(R.id.music_iv_humming_recognition_icon)
        mSidebarHummingRecognitionClickTv =
            hummingRecognitionView.findViewById<TextView>(R.id.music_tv_humming_recognition_click)
        mSidebarHummingRecognitionTipsTv =
            hummingRecognitionView.findViewById<TextView>(R.id.music_tv_humming_recognition_tips)

        //设置监听事件
        mSidebarAutoRecognitionIv!!.setOnClickListener(this)
        mSidebarHummingRecognitionIv!!.setOnClickListener(this)

        mAdapter = MusicRecognitionAdapter(mTitles, mViews)

        mSidebarRecognitionViewPager!!.setAdapter(mAdapter)
        mSidebarRecognitionViewPager!!.setCurrentItem(0) // 默认显示第一个页卡
        mSidebarRecognitionViewPager!!.setOffscreenPageLimit(0)

        mSidebarRecognitionTab!!.setupWithViewPager(mSidebarRecognitionViewPager)
        mSidebarRecognitionTab!!.setTabTextColors(Color.WHITE, Color.RED)

        mAlertDialog = null
        mBuilder = AlertDialog.Builder(this)
    }

    /**
     * 描述：初始化数据
     */
    private fun initData() {
        Log.d(TAG, "initData")
        path = (Environment.getExternalStorageDirectory().toString()
                + "/acrcloud/model")
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }

        this.mConfig = ACRCloudConfig()
        this.mConfig!!.acrcloudListener = this
        this.mConfig!!.context = this
        this.mConfig!!.host = "identify-cn-north-1.acrcloud.com" // 远端host地址
        this.mConfig!!.dbPath = path
        this.mConfig!!.accessKey = "87a438e1665a041615894dea96e41d72" // 远端host访问key
        this.mConfig!!.accessSecret = "W5fRyv236UPauzOi8OqKSUWSDgpvY7uVmHl5mhYo" // 远端host访问secret
        this.mConfig!!.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTPS
        this.mConfig!!.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE

        this.mClient = ACRCloudClient()
        this.initState = this.mClient!!.initWithConfig(this.mConfig)
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.music_iv_auto_recognition_icon) {
            if (mRippleAutoRecognitionAnimationView!!.isRippleRunning()) {
                mRippleAutoRecognitionAnimationView!!.stopRippleAnimation()
                mSidebarAutoRecognitionClickTv!!.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition))
                mSidebarAutoRecognitionTipsTv!!.setText(ResUtil.getString(R.string.str_music_recognition_surround_song))
                isAutoRecognition = false
                cancelRecognition() // 停止识别歌曲
            } else {
                mRippleAutoRecognitionAnimationView!!.startRippleAnimation()
                mSidebarAutoRecognitionClickTv!!.setText(ResUtil.getString(R.string.str_music_recognition_identifying))
                mSidebarAutoRecognitionTipsTv!!.setText(ResUtil.getString(R.string.str_music_recognition_click_stop))
                isAutoRecognition = true
                startAutoRecognition() //识别歌曲
            }
        } else if (v.getId() == R.id.music_iv_humming_recognition_icon) {
            if (mRippleHummingRecognitionAnimationView!!.isRippleRunning()) {
                mRippleHummingRecognitionAnimationView!!.stopRippleAnimation()
                mSidebarHummingRecognitionClickTv!!.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition))
                mSidebarHummingRecognitionTipsTv!!.setText(ResUtil.getString(R.string.str_music_recognition_humming_song))
                isHummingRecognition = false
                cancelRecognition() // 停止识别歌曲
            } else {
                mRippleHummingRecognitionAnimationView!!.startRippleAnimation()
                mSidebarHummingRecognitionClickTv!!.setText(ResUtil.getString(R.string.str_music_recognition_identifying))
                mSidebarHummingRecognitionTipsTv!!.setText(ResUtil.getString(R.string.str_music_recognition_click_stop))
                isHummingRecognition = true
                startHummingRecognition() // 识别哼唱歌曲，不一定识别出结果
            }
        }
    }

    /**
     * 描述：动态权限申请
     * 此处注意一定注意RECORD_AUDIO权限的申请，否则报错
     */
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission.RECORD_AUDIO
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
     * 描述：自动进行歌曲识别
     * 
     * 
     * 搜索方法：
     * 1.startRecognize()：该方法将自动开始记录和识别过程，生成结果后，将调用ACRCloudConfig中的IACRCloudListener回调对象的回调函数。
     * 2.stopRecordToRecognize()：此功能仅适用于嗡嗡声识别，它将停止录制并立即开始识别。
     * 3.recognize(byte[] buffer):识别存储在buffer参数中的音频内容,音频格式：RIFF，PCM，16位，单声道8000 Hz,它不再开始录制过程。
     * 这是一个同步函数，它将等待直到结果返回;由于“ Android UI主线程”无法发送网络请求，因此必须在子线程中调用此函数。
     * 4.startPreRecord(int recordTimeMS):通过麦克风预先录制音频，使识别速度更快;参数recordTimeMS：最近x毫秒记录的音频。
     */
    private fun startAutoRecognition() {
        if (!this.initState) {
            Toast.makeText(
                this,
                ResUtil.getString(R.string.str_music_recognition_init_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        Log.d(TAG, "startAutoRecognition")
        if (!mProcessing) {
            mProcessing = true
            if (this.mClient == null || !this.mClient!!.startRecognize()) {
                mProcessing = false
                Toast.makeText(
                    this,
                    ResUtil.getString(R.string.str_music_recognition_network_request_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
            startTime = System.currentTimeMillis()
        }
    }


    /**
     * 描述：嗡嗡识别歌曲
     */
    private fun startHummingRecognition() {
        if (mProcessing && this.mClient != null) {
            Log.d(TAG, "startHummingRecognition")
            //            this.mClient.stopRecordToRecognize();
            this.mClient!!.startRecognize()
        }
        mProcessing = false
        stopTime = System.currentTimeMillis()
    }

    /**
     * 描述：取消识别歌曲
     */
    private fun cancelRecognition() {
        Log.d(TAG, "cancelRecognition")
        if (mProcessing && this.mClient != null) {
            mProcessing = false
            this.mClient!!.cancel()
        }
    }

    /**
     * 描述：如果在调用startRecognize函数期间和之后有任何结果，则将调用此函数。
     * 返回数据格式：
     * 
     * 
     * "status": {
     * "code":1001,
     * "msg":"NoResult",
     * "version":"1.0",
     * }
     * }
     * 
     * @param result 识别出的歌曲结果
     */
    override fun onResult(result: String) {
        if (this.mClient != null) {
            this.mClient!!.cancel()
            mProcessing = false
        }
        mIdentifySongResult = "\n"

        try {
            val totalResult = JSONObject(result)
            val status = totalResult.getJSONObject("status")
            val code = status.getInt("code")
            //            String msg = status.getString("msg");
//            String version = status.getString("version");
            if (code == 0) { // 识别到歌曲
                mSidebarHummingRecognitionClickTv!!.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition)) // 识别到歌曲后，首先将标题信息修改为“点击识别音乐”
                val metadata = totalResult.getJSONObject("metadata")
                if (metadata.has("humming")) {  // 人声识别
                    val hummings = metadata.getJSONArray("humming")
                    for (i in 0..<hummings.length()) {
                        val tt = hummings.get(i) as JSONObject
                        val title = tt.getString("title")
                        val artists = tt.getJSONArray("artists")
                        val art = artists.get(0) as JSONObject
                        val artist = art.getString("name")
                        mIdentifySongResult = mIdentifySongResult + (i + 1) + ". " + title + "\n"
                    }
                }

                if (metadata.has("music")) {  // 歌曲识别
                    val musics = metadata.getJSONArray("music")
                    for (i in 0..<musics.length()) {
                        val tt = musics.get(i) as JSONObject
                        val title = tt.getString("title") // 获得歌曲名称
                        val artists =
                            tt.getJSONArray("artists") // artists是一个数组对象，里面包含有识别出的歌曲作者相关信息，根据第一个name属性可获得作者
                        val art = artists.get(0) as JSONObject
                        val artist = art.getString("name") // 获取歌曲作者
                        mIdentifySongResult =
                            mIdentifySongResult + (i + 1) + ".  Title: " + title + "    Artist: " + artist + "\n"
                    }
                }

                if (metadata.has("streams")) {   // 声音streams
                    val musics = metadata.getJSONArray("streams")
                    for (i in 0..<musics.length()) {
                        val tt = musics.get(i) as JSONObject
                        val title = tt.getString("title")
                        val channelId = tt.getString("channel_id")
                        mIdentifySongResult =
                            mIdentifySongResult + (i + 1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n"
                    }
                }
                if (metadata.has("custom_files")) {
                    val musics = metadata.getJSONArray("custom_files")
                    for (i in 0..<musics.length()) {
                        val tt = musics.get(i) as JSONObject
                        val title = tt.getString("title")
                        mIdentifySongResult =
                            mIdentifySongResult + (i + 1) + ".  Title: " + title + "\n"
                    }
                }
                showIdentitySongDialog(mIdentifySongResult) // 显示识别出的歌曲信息
            } else { // 未识别出歌曲
                mIdentifySongResult = result
            }
        } catch (e: JSONException) {
            mIdentifySongResult = result
            e.printStackTrace()
        }
        Log.i(TAG, "onResult: " + mIdentifySongResult)
    }

    /**
     * 描述：此功能旨在告诉您录音时的音量。
     * 您可以使用实时音量信息来创建交互式UI。
     * 
     * @param volume 录音的音量大小
     */
    override fun onVolumeChanged(volume: Double) {
        val time = (System.currentTimeMillis() - startTime) / 1000
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this.mClient != null) {
            this.mClient!!.release()
            this.initState = false
            this.mClient = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        (ResUtil.getString(R.string.str_toast_permission)
                                + permissions[i] + ResUtil.getString(R.string.str_toast_permission_success)),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        (ResUtil.getString(R.string.str_toast_permission)
                                + permissions[i] + ResUtil.getString(R.string.str_toast_permission_fail)),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * 描述：使用对话框显示歌曲识别结果
     * 
     * @param msg 歌曲识别结果
     */
    private fun showIdentitySongDialog(msg: String?) {
        Log.d(TAG, "showIdentitySongDialog")
        if (isAutoRecognition) { // 听歌识曲-识别到结果时，立即停止动画，并设置相应文字显示
            mRippleAutoRecognitionAnimationView!!.stopRippleAnimation() //识别到歌曲结果，点击确定，停止波纹动画
            mSidebarAutoRecognitionClickTv!!.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition))
            mSidebarAutoRecognitionTipsTv!!.setText(ResUtil.getString(R.string.str_music_recognition_surround_song))
            isAutoRecognition = false
        }
        if (isHummingRecognition) { // 哼唱识曲-识别到结果时，立即停止动画，并设置相应文字显示
            mRippleHummingRecognitionAnimationView!!.stopRippleAnimation()
            mSidebarHummingRecognitionClickTv!!.setText(ResUtil.getString(R.string.str_music_recognition_click_recognition))
            mSidebarHummingRecognitionTipsTv!!.setText(ResUtil.getString(R.string.str_music_recognition_humming_song))
            isHummingRecognition = false
        }
        mAlertDialog =
            mBuilder!!.setTitle(ResUtil.getString(R.string.str_music_recognition_dialog_title))
                .setMessage(msg).setPositiveButton(
                    ResUtil.getString(R.string.str_dialog_btn_confirm),
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            cancelRecognition() // 停止识别歌曲
                        }
                    }).create()
        mAlertDialog!!.show()
    }


    companion object {
        private const val TAG = "MusicRecognitionActivity"

        /**
         * 权限申请数组
         */
        private val PERMISSIONS_STORAGE = arrayOf<String?>(permission.RECORD_AUDIO)

        /**
         * 权限请求状态码
         */
        private const val REQUEST_PERMISSION_CODE = 1
    }
}
