package com.example.sevenvideoview

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 切换清晰度对话框（仿腾讯视频切换清晰度的对话框）.
 */
class ChangeClarityDialog(context: Context) : Dialog(context, R.style.dialog_change_clarity) {
    private var mLinearLayout: LinearLayout? = null
    private var mCurrentCheckedIndex = 0

    private fun init(context: Context) {
        mLinearLayout = LinearLayout(context)
        mLinearLayout!!.setGravity(Gravity.CENTER)
        mLinearLayout!!.setOrientation(LinearLayout.VERTICAL)
        mLinearLayout!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mListener != null) {
                    mListener!!.onClarityNotChanged()
                }
                this@ChangeClarityDialog.dismiss()
            }
        })

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.MarginLayoutParams.MATCH_PARENT
        )
        setContentView(mLinearLayout!!, params)

        val windowParams = getWindow()!!.getAttributes()
        windowParams.width = SevenUtil.getScreenHeight(context)
        windowParams.height = SevenUtil.getScreenWidth(context)
        getWindow()!!.setAttributes(windowParams)
    }

    /**
     * 设置清晰度等级
     * 
     * @param items          清晰度等级items
     * @param defaultChecked 默认选中的清晰度索引
     */
    fun setClarityGrade(items: MutableList<String?>, defaultChecked: Int) {
        mCurrentCheckedIndex = defaultChecked
        for (i in items.indices) {
            val itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_change_clarity, mLinearLayout, false) as TextView
            itemView.setTag(i)
            itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (mListener != null) {
                        val checkIndex = v.getTag() as Int
                        if (checkIndex != mCurrentCheckedIndex) {
                            for (j in 0..<mLinearLayout!!.getChildCount()) {
                                mLinearLayout!!.getChildAt(j).setSelected(checkIndex == j)
                            }
                            mListener!!.onClarityChanged(checkIndex)
                            mCurrentCheckedIndex = checkIndex
                        } else {
                            mListener!!.onClarityNotChanged()
                        }
                    }
                    this@ChangeClarityDialog.dismiss()
                }
            })
            itemView.setText(items.get(i))
            itemView.setSelected(i == defaultChecked)
            val params = itemView.getLayoutParams() as ViewGroup.MarginLayoutParams
            params.topMargin = if (i == 0) 0 else SevenUtil.dp2px(getContext(), 16f)
            mLinearLayout!!.addView(itemView, params)
        }
    }

    interface OnClarityChangedListener {
        /**
         * 切换清晰度后回调
         * 
         * @param clarityIndex 切换到的清晰度的索引值
         */
        fun onClarityChanged(clarityIndex: Int)

        /**
         * 清晰度没有切换，比如点击了空白位置，或者点击的是之前的清晰度
         */
        fun onClarityNotChanged()
    }

    private var mListener: OnClarityChangedListener? = null

    init {
        init(context)
    }

    fun setOnClarityCheckedListener(listener: OnClarityChangedListener?) {
        mListener = listener
    }

    override fun onBackPressed() {
        // 按返回键时回调清晰度没有变化
        if (mListener != null) {
            mListener!!.onClarityNotChanged()
        }
        super.onBackPressed()
    }
}
