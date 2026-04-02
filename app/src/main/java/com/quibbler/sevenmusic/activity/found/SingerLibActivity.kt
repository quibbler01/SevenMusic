package com.quibbler.sevenmusic.activity.found

import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.gson.Gson
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.sidebar.SingerSiderBar
import com.quibbler.sevenmusic.activity.sidebar.SingerSiderBar.OnTouchingLetterChangedListener
import com.quibbler.sevenmusic.adapter.found.FoundSingerLibRecommendAdapter
import com.quibbler.sevenmusic.adapter.found.FoundSingerLibSelectedAdapter
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerLibBean
import com.quibbler.sevenmusic.comparator.PinyinComparator
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Collections

/**
 * Package:        com.quibbler.sevenmusic.activity.found
 * ClassName:      SingerLibActivity
 * Description:    歌手库页面
 * Author:         yanwuyang
 * CreateDate:     2019/9/19 18:49
 */
class SingerLibActivity : AppCompatActivity() {
    //歌手库上方tab指示器,歌手风格
    private var mStyleTabLayout: TabLayout? = null

    //歌手库上方tab指示器,歌手性别
    private var mGenderTabLayout: TabLayout? = null

    //当前style标签的index，默认为0
    private var mStylePosition = -1

    //当前gender标签的index，默认为0
    private var mGenderPosition = -1

    //异步请求筛选歌手信息的AsyncTask
    private var mRequestShowSingerAsyncTask: RequestShowSingerAsyncTask? = null

    //异步请求推荐歌手信息的AsyncTask
    private var mRequestTopSingerAsyncTask: RequestTopSingerAsyncTask? = null

    //显示推荐歌手的RecyclerView的adapter
    private var mSingerLibRecommendAdapter: FoundSingerLibRecommendAdapter? = null

    //显示歌手筛选结果的RecyclerView的adapter
    private var mSingerLibSelectedAdapter: FoundSingerLibSelectedAdapter? = null

    //显示歌手筛选结果的RecyclerView
    private var mSelectedRecyclerView: RecyclerView? = null

    //显示推荐歌手的RecyclerView
    private var mRecommendRecyclerView: RecyclerView? = null

    //从网络获取的筛选歌手数据源list
    private val mSelectedSingerInfoList: MutableList<FoundSingerInfo?> =
        ArrayList<FoundSingerInfo?>()

    //从网络获取的推荐歌手数据源list
    private val mRecommendSingerInfoList: MutableList<FoundSingerInfo?> =
        ArrayList<FoundSingerInfo?>()

    //右侧字母栏
    private var mSiderBar: SingerSiderBar? = null

    //点击SiderBar后显示点击的字母
    private var mTvDialog: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_found_singer_lib)

        initActionBar()
        initStyleTab()
        initGenderTab()
        initRecommendRecyclerView()
        initSelectedRecyclerView()
        initSiderBar()
        this.topSingerInfo
    }

    /**
     * 初始化页面的actionBar
     */
    private fun initActionBar() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setTitle("歌手库")
    }

    /**
     * 初始化歌手风格的tab指示器
     */
    private fun initStyleTab() {
        mStyleTabLayout = findViewById<TabLayout>(R.id.singer_lib_style_tablayout)

        mStyleTabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Log.d(TAG, tab.getText().toString() + "被选中")
                updateTabTextView(tab, true)
                mStylePosition = tab.getPosition()
                requestSingerData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                Log.d(TAG, tab.getText().toString() + "未被选中")
                updateTabTextView(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        //为tabLayout手动添加标签
        for (i in SINGER_STYLE.indices) {
            if (i == 0) {
                mStyleTabLayout!!.addTab(mStyleTabLayout!!.newTab().setText(SINGER_STYLE[i]), true)
            } else {
                mStyleTabLayout!!.addTab(mStyleTabLayout!!.newTab().setText(SINGER_STYLE[i]), false)
            }
        }
        //设置指示器可以滚动
        mStyleTabLayout!!.setTabMode(TabLayout.MODE_SCROLLABLE)
    }

    /**
     * 设置tab的textView
     * 
     * @param tab
     * @param isSelect 是否被选中
     */
    private fun updateTabTextView(tab: TabLayout.Tab, isSelect: Boolean) {
        //第一次要判空
        val view = tab.getCustomView()
        if (null == view) {
            tab.setCustomView(R.layout.tab_found_lib_custom)
        }

        //设置标签是否选中的样式
        if (isSelect) {
            //选中的样式
            val tabSelect = tab.getCustomView()!!.findViewById<TextView>(R.id.tab_found_lib_tv)
            tabSelect.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
            tabSelect.setTextColor(getResources().getColor(R.color.found_tab_selected))
            tabSelect.setText(tab.getText())
            tabSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        } else {
            //未选中的样式
            val tabUnSelect = tab.getCustomView()!!.findViewById<TextView>(R.id.tab_found_lib_tv)
            tabUnSelect.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
            tabUnSelect.setTextColor(getResources().getColor(R.color.found_tab_unselected))
            tabUnSelect.setText(tab.getText())
            tabUnSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
        }
    }

    /**
     * 初始化歌手性别的tab指示器
     */
    private fun initGenderTab() {
        mGenderTabLayout = findViewById<TabLayout>(R.id.singer_lib_gender_tablayout)

        mGenderTabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Log.d(TAG, tab.getText().toString() + "被选中")
                updateTabTextView(tab, true)
                mGenderPosition = tab.getPosition()
                requestSingerData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                Log.d(TAG, tab.getText().toString() + "未被选中")
                updateTabTextView(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        //为tabLayout手动添加标签
        for (i in SINGER_GENDER.indices) {
            if (i == 0) {
                mGenderTabLayout!!.addTab(
                    mGenderTabLayout!!.newTab().setText(SINGER_GENDER[i]),
                    true
                )
            } else {
                mGenderTabLayout!!.addTab(
                    mGenderTabLayout!!.newTab().setText(SINGER_GENDER[i]),
                    false
                )
            }
        }
        //设置指示器可以滚动
        mGenderTabLayout!!.setTabMode(TabLayout.MODE_SCROLLABLE)
    }

    /**
     * 初始化推荐歌手的RecyclerView
     */
    private fun initRecommendRecyclerView() {
        mRecommendRecyclerView = findViewById<RecyclerView>(R.id.singer_lib_top_recyclerview)
        mSingerLibRecommendAdapter = FoundSingerLibRecommendAdapter(mRecommendSingerInfoList)
        mRecommendRecyclerView!!.setAdapter(mSingerLibRecommendAdapter)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL)
        mRecommendRecyclerView!!.setLayoutManager(linearLayoutManager)
    }

    /**
     * 初始化歌手筛选结果的RecyclerView
     */
    private fun initSelectedRecyclerView() {
        mSelectedRecyclerView = findViewById<RecyclerView>(R.id.singer_lib_show_recyclerview)
        mSingerLibSelectedAdapter = FoundSingerLibSelectedAdapter(mSelectedSingerInfoList)
        mSelectedRecyclerView!!.setAdapter(mSingerLibSelectedAdapter)
        val linearLayoutManager = LinearLayoutManager(this)
        mSelectedRecyclerView!!.setLayoutManager(linearLayoutManager)
    }

    private fun initSiderBar() {
        mSiderBar = findViewById<SingerSiderBar>(R.id.singer_lib_sidebar)
        mTvDialog = findViewById<TextView?>(R.id.singer_lib_tv_dialog)
        mSiderBar!!.setTextViewDialog(mTvDialog)
        mSiderBar!!.setOnTouchingLetterChangedListener(object : OnTouchingLetterChangedListener {
            override fun onTouchingLetterChanged(s: String?): Boolean {
                // 该字母首次出现的位置
                val position = mSingerLibSelectedAdapter!!.getPositionForSelection(s?.get(0) ?: 'A')
                if (position != -1) {
                    mSelectedRecyclerView!!.getLayoutManager()!!.scrollToPosition(position)
                    return true
                }
                return false
            }
        })

        //        mSiderBar.refresh();
    }

    /**
     * 根据tab指示器选择的歌手信息，组合成requestCode，向网易api进行请求
     */
    private fun requestSingerData() {
        //先取消上一次的请求，包括后续图片的下载请求
        if (mRequestShowSingerAsyncTask != null) {
            mRequestShowSingerAsyncTask!!.cancel(true)
        }
        if (mSingerLibRecommendAdapter != null) {
            mSingerLibRecommendAdapter!!.stopUpdateData()
        }
        if (mStylePosition == -1 || mGenderPosition == -1) {
            return
        }

        val requestCode = SINGER_STYLE_CODE[mStylePosition] + SINGER_GENDER_CODE[mGenderPosition]
        val requestUrl: String =
            REQUEST_SINGER_AUTHORITY_HEAD + requestCode + REQUEST_SINGER_AUTHORITY_TAIL

        mRequestShowSingerAsyncTask = RequestShowSingerAsyncTask()
        mRequestShowSingerAsyncTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestUrl)
    }

    private val topSingerInfo: Unit
        get() {
            mRequestTopSingerAsyncTask = RequestTopSingerAsyncTask()
            mRequestTopSingerAsyncTask!!.executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                REQUEST_TOP_SINGER_URL
            )
        }

    /**
     * 根据url获取筛选歌手的数据，并更新显示
     */
    private inner class RequestShowSingerAsyncTask :
        AsyncTask<String?, Void?, MutableList<FoundSingerInfo?>?>() {
        override fun doInBackground(vararg strings: String?): MutableList<FoundSingerInfo?>? {
            val path: String = strings[0]

            //OkHttp获取网络资源
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(path)
                    .build()
                val response = client.newCall(request).execute()
                //                Log.d(TAG, "response is: " + response);
                val responseCode = response.code
                //                Log.d(TAG, "responseCode is: " + responseCode);
                if (responseCode == 200) {
                    val responseData = response.body!!.string()
                    if (TextUtils.isEmpty(responseData)) {
                        Log.d(TAG, "responseData is empty!")
                        return null
                    }
                    //                    Log.d(TAG, "responseData is: " + responseData);
                    val gson = Gson()
                    val responseBean = gson.fromJson<FoundSingerLibBean>(
                        responseData,
                        FoundSingerLibBean::class.java
                    )

                    return responseBean.artists
                }
            } catch (e: IOException) {
                Log.e(TAG, "网络异常！")
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(foundSingerInfoList: MutableList<FoundSingerInfo?>?) {
            super.onPostExecute(foundSingerInfoList)
            if (foundSingerInfoList == null) {
                return
            }
            val iterator = foundSingerInfoList.iterator()
            while (iterator.hasNext()) {
                val singer = iterator.next()
                if (singer == null || singer.firstPinyin == null) {
                    iterator.remove()
                }
            }
            //按拼音排序
            Collections.sort<FoundSingerInfo?>(foundSingerInfoList, PinyinComparator())
            //RecyclerView的数据源变化了，更新siderBar
            mSiderBar!!.refresh(foundSingerInfoList)

            mSingerLibSelectedAdapter!!.updateData(foundSingerInfoList)
            //切换标签之后，滚动到最上面
            mSelectedRecyclerView!!.scrollToPosition(0)
        }
    }

    /**
     * 根据url获取歌手库的数据，并更新显示
     */
    private inner class RequestTopSingerAsyncTask :
        AsyncTask<String?, Void?, MutableList<FoundSingerInfo?>?>() {
        override fun doInBackground(vararg strings: String?): MutableList<FoundSingerInfo?>? {
            val path: String = strings[0]

            //OkHttp获取网络资源
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(path)
                    .build()
                val response = client.newCall(request).execute()
                //                Log.d(TAG, "response is: " + response);
                val responseCode = response.code
                //                Log.d(TAG, "responseCode is: " + responseCode);
                if (responseCode == 200) {
                    val responseData = response.body!!.string()
                    if (TextUtils.isEmpty(responseData)) {
                        Log.d(TAG, "responseData is empty!")
                        return null
                    }
                    //                    Log.d(TAG, "responseData is: " + responseData);
                    val gson = Gson()
                    val responseBean = gson.fromJson<FoundSingerLibBean>(
                        responseData,
                        FoundSingerLibBean::class.java
                    )

                    return responseBean.artists
                }
            } catch (e: IOException) {
                Log.e(TAG, "网络异常！")
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(foundSingerInfoList: MutableList<FoundSingerInfo?>?) {
            super.onPostExecute(foundSingerInfoList)
            mSingerLibRecommendAdapter!!.updateData(foundSingerInfoList)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> super.onBackPressed()
            else -> {}
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mRequestShowSingerAsyncTask!!.cancel(true)
    }

    companion object {
        private const val TAG = "SingerLibActivity"

        //歌手风格名称
        private val SINGER_STYLE: Array<String?> =
            arrayOf("华语", "欧美", "日本", "韩国", "其他")

        //歌手风格名对应的请求码
        private val SINGER_STYLE_CODE: Array<String?> =
            arrayOf("10", "20", "60", "70", "40")

        //歌手性别名称
        private val SINGER_GENDER: Array<String?> = arrayOf("男歌手", "女歌手", "组合/乐队")

        //歌手性别名对应的请求码
        private val SINGER_GENDER_CODE = arrayOf("01", "02", "03")

        private const val REQUEST_SINGER_AUTHORITY_HEAD =
            "http://114.116.128.229:3000/artist/list?cat="
        private const val REQUEST_SINGER_AUTHORITY_TAIL = "&limit=30"

        private const val REQUEST_TOP_SINGER_URL =
            "http://114.116.128.229:3000/top/artists?offset=0&limit=8"
    }
}
