package com.quibbler.sevenmusic.activity.sidebar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo
import java.util.Locale

class SingerSiderBar : View {
    // 触摸事件
    private var mOnTouchingLetterChangedListener: OnTouchingLetterChangedListener? = null

    //    // 26个字母
    //    public static String[] sAZ1 = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
    //            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
    //            "W", "X", "Y", "Z", "#"};
    private val mAZStrList: MutableList<String?> = ArrayList<String?>()
    private var mChoose = -1 // 选中
    private val mPaint = Paint()
    private var mTextDialog: TextView? = null
    private val mSingerList: MutableList<FoundSingerInfo>? = ArrayList<FoundSingerInfo>()
    private val mTextColor: Int
    private val mChosenTextColor: Int


    /**
     * 为SideBar设置显示字母的TextView
     * 
     * @param textDialog
     */
    fun setTextViewDialog(textDialog: TextView?) {
        mTextDialog = textDialog
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        mTextColor = getResources().getColor(R.color.singer_lib_siderbar_unchosen_text)
        mChosenTextColor = getResources().getColor(R.color.singer_lib_siderbar_chosen_text)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mTextColor = getResources().getColor(R.color.singer_lib_siderbar_unchosen_text)
        mChosenTextColor = getResources().getColor(R.color.singer_lib_siderbar_chosen_text)
    }

    constructor(context: Context?) : super(context) {
        mTextColor = getResources().getColor(R.color.singer_lib_siderbar_unchosen_text)
        mChosenTextColor = getResources().getColor(R.color.singer_lib_siderbar_chosen_text)
    }

    fun refresh() {
        mSingerList!!.clear()
        mAZStrList.clear()
        invalidate()
    }

    fun refresh(list: MutableList<FoundSingerInfo?>) {
        mSingerList!!.clear()
        mSingerList.addAll(list)
        mAZStrList.clear()
        initAZList()
        invalidate()
    }

    /**
     * 初始化siderBar中所含字母
     */
    private fun initAZList() {
        if (mSingerList == null || mSingerList.size == 0) {
            return
        }
        for (singerInfo in mSingerList) {
            val str = singerInfo.getFirstPinyin()
            val c: Char = str.uppercase(Locale.getDefault()).get(0)
            if (c >= 'A' && c <= 'Z') {
//                Log.d(TAG, str.toUpperCase());
                if (!mAZStrList.contains(str.uppercase(Locale.getDefault()))) {
                    mAZStrList.add(str.uppercase(Locale.getDefault()))
                    continue
                }
            } else {
                mAZStrList.add("#")
                //后面肯定都是“#”，所以可以直接return了
                return
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mAZStrList.size == 0) {
            return
        }

        // 获取焦点改变背景颜色.
        val height = getHeight() // 获取对应高度
        val width = getWidth() // 获取对应宽度
        val singleHeight = height / mAZStrList.size - 2 // 获取每一个字母的高度  (这里-2仅仅是为了好看而已)

        for (i in mAZStrList.indices) {
            mPaint.setColor(mTextColor) //设置字体颜色
            mPaint.setTypeface(Typeface.DEFAULT_BOLD) //设置字体
            mPaint.setAntiAlias(true) //设置抗锯齿
            mPaint.setTextSize(30f) //设置字母字体大小
            // 选中的状态
            if (i == mChoose) {
                mPaint.setColor(mChosenTextColor) //选中的字母改变颜色
                mPaint.setFakeBoldText(true) //设置字体为粗体
            }
            // x坐标等于中间-字符串宽度的一半.
            val xPos = width / 2 - mPaint.measureText(mAZStrList.get(i)) / 2
            val yPos = (singleHeight * i + singleHeight).toFloat()
            canvas.drawText(mAZStrList.get(i)!!, xPos, yPos, mPaint) //绘制所有的字母
            mPaint.reset() // 重置画笔
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (getHeight() == 0) {
            return false
        }
        val action = event.getAction()
        val y = event.getY() // 点击y坐标
        val oldChoose = mChoose
        val listener = mOnTouchingLetterChangedListener
        val choose = (y / getHeight() * mAZStrList.size).toInt() // 点击y坐标所占总高度的比例*数组的长度=点击字母的位置.
        when (action) {
            MotionEvent.ACTION_UP -> {
                setBackgroundDrawable(ColorDrawable(0x00000000))
                mChoose = -1 //
                invalidate()
                if (mTextDialog != null) {
                    mTextDialog!!.setVisibility(INVISIBLE)
                }
            }

            else -> {
                setBackgroundResource(R.color.singer_lib_siderbar_gray_color)
                if (oldChoose != choose) {  //判断选中字母是否发生改变
                    if (choose >= 0 && choose < mAZStrList.size) {
                        if (listener != null) {
                            if (!listener.onTouchingLetterChanged(mAZStrList.get(choose))) {
                                break
                            }
                        }
                        if (mTextDialog != null) {
                            mTextDialog!!.setText(mAZStrList.get(choose))
                            mTextDialog!!.setVisibility(VISIBLE)
                        }
                        mChoose = choose
                        invalidate()
                    }
                }
            }
        }
        return true
    }

    /**
     * 向外公开的方法
     * 
     * @param onTouchingLetterChangedListener
     */
    fun setOnTouchingLetterChangedListener(
        onTouchingLetterChangedListener: OnTouchingLetterChangedListener?
    ) {
        mOnTouchingLetterChangedListener = onTouchingLetterChangedListener
    }


    /**
     * 对外接口，当点击位置改变时，触发RecyclerView的相应变化
     */
    interface OnTouchingLetterChangedListener {
        fun onTouchingLetterChanged(s: String?): Boolean
    }

    companion object {
        private const val TAG = "SingerSiderBar"
    }
}
