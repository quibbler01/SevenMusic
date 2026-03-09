package com.quibbler.sevenmusic.view.sidebar

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import com.quibbler.sevenmusic.R

/**
 * Package:        com.quibbler.alarm.view
 * ClassName:      SelectRemindCyclePopup
 * Description:    闹钟提醒频率类
 * Author:         11103876
 * CreateDate:     2019/9/24 21:41
 */
class SelectRemindCyclePopup(context: Context) : View.OnClickListener {
    /**
     * 周一
     */
    private var mMondayTv: TextView? = null

    /**
     * 周二
     */
    private var mTuesdayTv: TextView? = null

    /**
     * 周三
     */
    private var mWednesdayTv: TextView? = null

    /**
     * 周四
     */
    private var mThursdayTv: TextView? = null

    /**
     * 周五
     */
    private var mFridayTv: TextView? = null

    /**
     * 周六
     */
    private var mSaturdayTv: TextView? = null

    /**
     * 周日
     */
    private var mSundayTv: TextView? = null

    /**
     * 显示"确定"标题
     */
    private var mSureTv: TextView? = null

    /**
     * 每天
     */
    private var mEverdayTv: TextView? = null

    /**
     * 一次
     */
    private var mDrugCycleOnceTv: TextView? = null

    /**
     * 弹出窗口对象实例
     */
    private val mPopupWindow: PopupWindow?

    /**
     * 回调接口对象实例
     */
    var mSelectRemindCyclePopupListener: SelectRemindCyclePopupOnClickListener? = null

    /**
     * 上下文对象
     */
    private val mContext: Context

    fun getmPopupWindow(): PopupWindow? {
        return mPopupWindow
    }

    /**
     * 描述：构造函数-初始化PopupWindow对象
     * 
     * @param context
     */
    init {
        mContext = context
        mPopupWindow = PopupWindow(context)
        mPopupWindow.setBackgroundDrawable(BitmapDrawable())
        //        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
        mPopupWindow.setWidth(WindowManager.LayoutParams.FILL_PARENT)
        mPopupWindow.setHeight(WindowManager.LayoutParams.FILL_PARENT)
        mPopupWindow.setTouchable(true)
        mPopupWindow.setFocusable(true)
        mPopupWindow.setOutsideTouchable(true)
        mPopupWindow.setAnimationStyle(R.style.AnimBottom)
        mPopupWindow.setContentView(initView())
        mPopupWindow.getContentView().setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                mPopupWindow.setFocusable(false)
                return true
            }
        })
    }


    /**
     * 描述：初始化组件
     * 
     * @return 返回view对象
     */
    fun initView(): View {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.sidebar_alarm_select_remind_cycle_pop_window, null) // 加载闹钟提醒频率布局
        mDrugCycleOnceTv = view.findViewById<TextView>(R.id.sidebar_tv_alarm__drug_cycle_once)
        mEverdayTv = view.findViewById<TextView>(R.id.sidebar_tv_alarm_drug_cycle_everyday)
        mMondayTv = view.findViewById<TextView>(R.id.sidebar_tv_alarmdrug_cycle_monday)
        mTuesdayTv = view.findViewById<TextView>(R.id.sidebar_tv_alarmdrug_cycle_tuesday)
        mWednesdayTv = view.findViewById<TextView>(R.id.sidebar_tv_alarm_drug_cycle_wednesday)
        mThursdayTv = view.findViewById<TextView>(R.id.sidebar_tv_alarm_drug_cycle_thursday)
        mFridayTv = view.findViewById<TextView>(R.id.sidebar_tv_alarm_drug_cycle_friday)
        mSaturdayTv = view.findViewById<TextView>(R.id.sidebar_tv_alarm_drug_cycle_saturday)
        mSundayTv = view.findViewById<TextView>(R.id.sidebar_tv_alarm_drug_cycle_sunday)
        mSureTv = view.findViewById<TextView>(R.id.sidebar_tv_alarm_drug_cycle_sure)

        mDrugCycleOnceTv!!.setOnClickListener(this)
        mEverdayTv!!.setOnClickListener(this)
        mMondayTv!!.setOnClickListener(this)
        mTuesdayTv!!.setOnClickListener(this)
        mWednesdayTv!!.setOnClickListener(this)
        mThursdayTv!!.setOnClickListener(this)
        mFridayTv!!.setOnClickListener(this)
        mSaturdayTv!!.setOnClickListener(this)
        mSundayTv!!.setOnClickListener(this)
        mSureTv!!.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View) {
        val nav_right = mContext.getResources()
            .getDrawable(R.drawable.sidebar_music_alarm_cycle_check) // 获取选择的打勾图像
        nav_right.setBounds(0, 0, nav_right.getMinimumWidth(), nav_right.getMinimumHeight())
        // 判断选择闹钟的提醒频率
        if (v.getId() == R.id.sidebar_tv_alarm__drug_cycle_once) { // 一次
            mSelectRemindCyclePopupListener!!.obtainMessage(9, "")
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_everyday) { // 每天
            mSelectRemindCyclePopupListener!!.obtainMessage(8, "")
        } else if (v.getId() == R.id.sidebar_tv_alarmdrug_cycle_monday) { // 周一
            if (mMondayTv!!.getCompoundDrawables()[2] == null) {
                mMondayTv!!.setCompoundDrawables(null, null, nav_right, null)
            } else {
                mMondayTv!!.setCompoundDrawables(null, null, null, null)
            }
            mSelectRemindCyclePopupListener!!.obtainMessage(0, "")
        } else if (v.getId() == R.id.sidebar_tv_alarmdrug_cycle_tuesday) { // 周二
            if (mTuesdayTv!!.getCompoundDrawables()[2] == null) {
                mTuesdayTv!!.setCompoundDrawables(null, null, nav_right, null)
            } else {
                mTuesdayTv!!.setCompoundDrawables(null, null, null, null)
            }
            mSelectRemindCyclePopupListener!!.obtainMessage(1, "")
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_wednesday) { // 周三
            if (mWednesdayTv!!.getCompoundDrawables()[2] == null) {
                mWednesdayTv!!.setCompoundDrawables(null, null, nav_right, null)
            } else {
                mWednesdayTv!!.setCompoundDrawables(null, null, null, null)
            }
            mSelectRemindCyclePopupListener!!.obtainMessage(2, "")
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_thursday) { // 周四
            if (mThursdayTv!!.getCompoundDrawables()[2] == null) {
                mThursdayTv!!.setCompoundDrawables(null, null, nav_right, null)
            } else {
                mThursdayTv!!.setCompoundDrawables(null, null, null, null)
            }
            mSelectRemindCyclePopupListener!!.obtainMessage(3, "")
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_friday) { // 周五
            if (mFridayTv!!.getCompoundDrawables()[2] == null) {
                mFridayTv!!.setCompoundDrawables(null, null, nav_right, null)
            } else {
                mFridayTv!!.setCompoundDrawables(null, null, null, null)
            }
            mSelectRemindCyclePopupListener!!.obtainMessage(4, "")
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_saturday) { // 周六
            if (mSaturdayTv!!.getCompoundDrawables()[2] == null) {
                mSaturdayTv!!.setCompoundDrawables(null, null, nav_right, null)
            } else {
                mSaturdayTv!!.setCompoundDrawables(null, null, null, null)
            }
            mSelectRemindCyclePopupListener!!.obtainMessage(5, "")
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_sunday) { // 周日
            if (mSundayTv!!.getCompoundDrawables()[2] == null) {
                mSundayTv!!.setCompoundDrawables(null, null, nav_right, null)
            } else {
                mSundayTv!!.setCompoundDrawables(null, null, null, null)
            }
            mSelectRemindCyclePopupListener!!.obtainMessage(6, "")
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_sure) { // 确定按钮
            val remind = ((if (mMondayTv!!.getCompoundDrawables()[2] == null) 0 else 1) * 1 // 周一
                    + (if (mTuesdayTv!!.getCompoundDrawables()[2] == null) 0 else 1) * 2 // 周二
                    + (if (mWednesdayTv!!.getCompoundDrawables()[2] == null) 0 else 1) * 4 // 周三
                    + (if (mThursdayTv!!.getCompoundDrawables()[2] == null) 0 else 1) * 8 // 周四
                    + (if (mFridayTv!!.getCompoundDrawables()[2] == null) 0 else 1) * 16 // 周五
                    + (if (mSaturdayTv!!.getCompoundDrawables()[2] == null) 0 else 1) * 32 // 周六
                    + (if (mSundayTv!!.getCompoundDrawables()[2] == null) 0 else 1) * 64) // 周日
            mSelectRemindCyclePopupListener!!.obtainMessage(7, remind.toString())
            dismiss()
        }
    }

    /**
     * 描述：定义闹钟功提醒频率接口
     */
    interface SelectRemindCyclePopupOnClickListener {
        fun obtainMessage(flag: Int, ret: String?)
    }

    fun setOnSelectRemindCyclePopupListener(remindCyclePopupListener: SelectRemindCyclePopupOnClickListener) {
        this.mSelectRemindCyclePopupListener = remindCyclePopupListener
    }

    /**
     * 描述：关闭弹出窗口
     */
    fun dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss()
        }
    }

    /**
     * 描述：显示弹出窗口
     * 
     * @param rootView
     */
    fun showPopup(rootView: View?) {
        // 第一个参数是要将PopupWindow放到的View，第二个参数是位置，第三第四是偏移值
        mPopupWindow!!.showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
    }
}
