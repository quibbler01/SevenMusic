package com.quibbler.sevenmusic.activity.sidebar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo;

import java.util.ArrayList;
import java.util.List;

public class SingerSiderBar extends View {
    private static final String TAG = "SingerSiderBar";
    // 触摸事件
    private OnTouchingLetterChangedListener mOnTouchingLetterChangedListener;
    //    // 26个字母
//    public static String[] sAZ1 = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
//            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
//            "W", "X", "Y", "Z", "#"};
    private List<String> mAZStrList = new ArrayList<>();
    private int mChoose = -1;// 选中
    private Paint mPaint = new Paint();
    private TextView mTextDialog;
    private List<FoundSingerInfo> mSingerList = new ArrayList<>();
    private int mTextColor;
    private int mChosenTextColor;


    /**
     * 为SideBar设置显示字母的TextView
     *
     * @param textDialog
     */
    public void setTextViewDialog(TextView textDialog) {
        mTextDialog = textDialog;
    }

    public SingerSiderBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTextColor = getResources().getColor(R.color.singer_lib_siderbar_unchosen_text);
        mChosenTextColor = getResources().getColor(R.color.singer_lib_siderbar_chosen_text);
    }

    public SingerSiderBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextColor = getResources().getColor(R.color.singer_lib_siderbar_unchosen_text);
        mChosenTextColor = getResources().getColor(R.color.singer_lib_siderbar_chosen_text);
    }

    public SingerSiderBar(Context context) {
        super(context);
        mTextColor = getResources().getColor(R.color.singer_lib_siderbar_unchosen_text);
        mChosenTextColor = getResources().getColor(R.color.singer_lib_siderbar_chosen_text);
    }

    public void refresh() {
        mSingerList.clear();
        mAZStrList.clear();
        invalidate();
    }

    public void refresh(List<FoundSingerInfo> list) {
        mSingerList.clear();
        mSingerList.addAll(list);
        mAZStrList.clear();
        initAZList();
        invalidate();
    }

    /**
     * 初始化siderBar中所含字母
     */
    private void initAZList() {
        if (mSingerList == null || mSingerList.size() == 0) {
            return;
        }
        for (FoundSingerInfo singerInfo : mSingerList) {
            String str = singerInfo.getFirstPinyin();
            char c = str.toUpperCase().charAt(0);
            if (c >= 'A' && c <= 'Z') {
//                Log.d(TAG, str.toUpperCase());
                if (!mAZStrList.contains(str.toUpperCase())) {
                    mAZStrList.add(str.toUpperCase());
                    continue;
                }
            } else {
                mAZStrList.add("#");
                //后面肯定都是“#”，所以可以直接return了
                return;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAZStrList.size() == 0) {
            return;
        }

        // 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        int singleHeight = height / mAZStrList.size() - 2;// 获取每一个字母的高度  (这里-2仅仅是为了好看而已)

        for (int i = 0; i < mAZStrList.size(); i++) {
            mPaint.setColor(mTextColor);  //设置字体颜色
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);  //设置字体
            mPaint.setAntiAlias(true);  //设置抗锯齿
            mPaint.setTextSize(30);  //设置字母字体大小
            // 选中的状态
            if (i == mChoose) {
                mPaint.setColor(mChosenTextColor);  //选中的字母改变颜色
                mPaint.setFakeBoldText(true);  //设置字体为粗体
            }
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - mPaint.measureText(mAZStrList.get(i)) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(mAZStrList.get(i), xPos, yPos, mPaint);  //绘制所有的字母
            mPaint.reset();// 重置画笔
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getHeight() == 0) {
            return false;
        }
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = mChoose;
        final OnTouchingLetterChangedListener listener = mOnTouchingLetterChangedListener;
        final int choose = (int) (y / getHeight() * mAZStrList.size());// 点击y坐标所占总高度的比例*数组的长度=点击字母的位置.
        switch (action) {
            case MotionEvent.ACTION_UP:
                setBackgroundDrawable(new ColorDrawable(0x00000000));
                mChoose = -1;//
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                setBackgroundResource(R.color.singer_lib_siderbar_gray_color);
                if (oldChoose != choose) {  //判断选中字母是否发生改变
                    if (choose >= 0 && choose < mAZStrList.size()) {
                        if (listener != null) {
                            if (!listener.onTouchingLetterChanged(mAZStrList.get(choose))) {
                                break;
                            }
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(mAZStrList.get(choose));
                            mTextDialog.setVisibility(View.VISIBLE);
                        }
                        mChoose = choose;
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 向外公开的方法
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        mOnTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }


    /**
     * 对外接口，当点击位置改变时，触发RecyclerView的相应变化
     */
    public interface OnTouchingLetterChangedListener {
        boolean onTouchingLetterChanged(String s);
    }

}
