package com.quibbler.sevenmusic.view.sidebar;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.quibbler.sevenmusic.R;

import static com.quibbler.sevenmusic.utils.ResUtil.getResources;

/**
 * Package:        com.quibbler.alarm.view
 * ClassName:      SelectRemindCyclePopup
 * Description:    闹钟提醒频率类
 * Author:         11103876
 * CreateDate:     2019/9/24 21:41
 */
public class SelectRemindCyclePopup implements View.OnClickListener {
    /**
     * 周一
     */
    private TextView mMondayTv;
    /**
     * 周二
     */
    private TextView mTuesdayTv;
    /**
     * 周三
     */
    private TextView mWednesdayTv;
    /**
     * 周四
     */
    private TextView mThursdayTv;
    /**
     * 周五
     */
    private TextView mFridayTv;
    /**
     * 周六
     */
    private TextView mSaturdayTv;
    /**
     * 周日
     */
    private TextView mSundayTv;
    /**
     * 显示"确定"标题
     */
    private TextView mSureTv;
    /**
     * 每天
     */
    private TextView mEverdayTv;
    /**
     * 一次
     */
    private TextView mDrugCycleOnceTv;
    /**
     * 弹出窗口对象实例
     */
    private PopupWindow mPopupWindow;
    /**
     * 回调接口对象实例
     */
    public SelectRemindCyclePopupOnClickListener mSelectRemindCyclePopupListener;
    /**
     * 上下文对象
     */
    private Context mContext;

    public PopupWindow getmPopupWindow() {
        return mPopupWindow;
    }

    /**
     * 描述：构造函数-初始化PopupWindow对象
     *
     * @param context
     */
    public SelectRemindCyclePopup(Context context) {
        mContext = context;
        mPopupWindow = new PopupWindow(context);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
//        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
        mPopupWindow.setWidth(WindowManager.LayoutParams.FILL_PARENT);
        mPopupWindow.setHeight(WindowManager.LayoutParams.FILL_PARENT);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setAnimationStyle(R.style.AnimBottom);
        mPopupWindow.setContentView(initView());
        mPopupWindow.getContentView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPopupWindow.setFocusable(false);
                return true;
            }
        });
    }


    /**
     * 描述：初始化组件
     *
     * @return 返回view对象
     */
    public View initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sidebar_alarm_select_remind_cycle_pop_window, null);  // 加载闹钟提醒频率布局
        mDrugCycleOnceTv = view.findViewById(R.id.sidebar_tv_alarm__drug_cycle_once);
        mEverdayTv = view.findViewById(R.id.sidebar_tv_alarm_drug_cycle_everyday);
        mMondayTv = view.findViewById(R.id.sidebar_tv_alarmdrug_cycle_monday);
        mTuesdayTv = view.findViewById(R.id.sidebar_tv_alarmdrug_cycle_tuesday);
        mWednesdayTv = view.findViewById(R.id.sidebar_tv_alarm_drug_cycle_wednesday);
        mThursdayTv = view.findViewById(R.id.sidebar_tv_alarm_drug_cycle_thursday);
        mFridayTv = view.findViewById(R.id.sidebar_tv_alarm_drug_cycle_friday);
        mSaturdayTv = view.findViewById(R.id.sidebar_tv_alarm_drug_cycle_saturday);
        mSundayTv = view.findViewById(R.id.sidebar_tv_alarm_drug_cycle_sunday);
        mSureTv = view.findViewById(R.id.sidebar_tv_alarm_drug_cycle_sure);

        mDrugCycleOnceTv.setOnClickListener(this);
        mEverdayTv.setOnClickListener(this);
        mMondayTv.setOnClickListener(this);
        mTuesdayTv.setOnClickListener(this);
        mWednesdayTv.setOnClickListener(this);
        mThursdayTv.setOnClickListener(this);
        mFridayTv.setOnClickListener(this);
        mSaturdayTv.setOnClickListener(this);
        mSundayTv.setOnClickListener(this);
        mSureTv.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Drawable nav_right = mContext.getResources().getDrawable(R.drawable.sidebar_music_alarm_cycle_check);   // 获取选择的打勾图像
        nav_right.setBounds(0, 0, nav_right.getMinimumWidth(), nav_right.getMinimumHeight());
        // 判断选择闹钟的提醒频率
        if (v.getId() == R.id.sidebar_tv_alarm__drug_cycle_once) { // 一次
            mSelectRemindCyclePopupListener.obtainMessage(9, "");
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_everyday) {// 每天
            mSelectRemindCyclePopupListener.obtainMessage(8, "");
        } else if (v.getId() == R.id.sidebar_tv_alarmdrug_cycle_monday) { // 周一
            if (mMondayTv.getCompoundDrawables()[2] == null) {
                mMondayTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mMondayTv.setCompoundDrawables(null, null, null, null);
            }
            mSelectRemindCyclePopupListener.obtainMessage(0, "");
        } else if (v.getId() == R.id.sidebar_tv_alarmdrug_cycle_tuesday) {// 周二
            if (mTuesdayTv.getCompoundDrawables()[2] == null) {
                mTuesdayTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mTuesdayTv.setCompoundDrawables(null, null, null, null);
            }
            mSelectRemindCyclePopupListener.obtainMessage(1, "");
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_wednesday) { // 周三
            if (mWednesdayTv.getCompoundDrawables()[2] == null) {
                mWednesdayTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mWednesdayTv.setCompoundDrawables(null, null, null, null);
            }
            mSelectRemindCyclePopupListener.obtainMessage(2, "");
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_thursday) { // 周四
            if (mThursdayTv.getCompoundDrawables()[2] == null) {
                mThursdayTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mThursdayTv.setCompoundDrawables(null, null, null, null);
            }
            mSelectRemindCyclePopupListener.obtainMessage(3, "");
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_friday) { // 周五
            if (mFridayTv.getCompoundDrawables()[2] == null) {
                mFridayTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mFridayTv.setCompoundDrawables(null, null, null, null);
            }
            mSelectRemindCyclePopupListener.obtainMessage(4, "");
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_saturday) { // 周六
            if (mSaturdayTv.getCompoundDrawables()[2] == null) {
                mSaturdayTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mSaturdayTv.setCompoundDrawables(null, null, null, null);
            }
            mSelectRemindCyclePopupListener.obtainMessage(5, "");
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_sunday) { // 周日
            if (mSundayTv.getCompoundDrawables()[2] == null) {
                mSundayTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mSundayTv.setCompoundDrawables(null, null, null, null);
            }
            mSelectRemindCyclePopupListener.obtainMessage(6, "");
        } else if (v.getId() == R.id.sidebar_tv_alarm_drug_cycle_sure) { // 确定按钮
            int remind = ((mMondayTv.getCompoundDrawables()[2] == null) ? 0 : 1) * 1 // 周一
                    + ((mTuesdayTv.getCompoundDrawables()[2] == null) ? 0 : 1) * 2 // 周二
                    + ((mWednesdayTv.getCompoundDrawables()[2] == null) ? 0 : 1) * 4 // 周三
                    + ((mThursdayTv.getCompoundDrawables()[2] == null) ? 0 : 1) * 8 // 周四
                    + ((mFridayTv.getCompoundDrawables()[2] == null) ? 0 : 1) * 16 // 周五
                    + ((mSaturdayTv.getCompoundDrawables()[2] == null) ? 0 : 1) * 32 // 周六
                    + ((mSundayTv.getCompoundDrawables()[2] == null) ? 0 : 1) * 64; // 周日
            mSelectRemindCyclePopupListener.obtainMessage(7, String.valueOf(remind));
            dismiss();
        }
    }

    /**
     * 描述：定义闹钟功提醒频率接口
     */
    public interface SelectRemindCyclePopupOnClickListener {
        void obtainMessage(int flag, String ret);
    }

    public void setOnSelectRemindCyclePopupListener(SelectRemindCyclePopupOnClickListener remindCyclePopupListener) {
        this.mSelectRemindCyclePopupListener = remindCyclePopupListener;
    }

    /**
     * 描述：关闭弹出窗口
     */
    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    /**
     * 描述：显示弹出窗口
     *
     * @param rootView
     */
    public void showPopup(View rootView) {
        // 第一个参数是要将PopupWindow放到的View，第二个参数是位置，第三第四是偏移值
        mPopupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

}
