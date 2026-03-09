package com.quibbler.sevenmusic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Package:        com.quibbler.sevenmusic.view.found
 * ClassName:      MyListView
 * Description:    自定义ListView 在我的页面显示歌单,原生ListView不能嵌套在ScrollView中全部显示数据.需要重写onMeasure()方法；三个构造函数都需要
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 16:26
 */
public class MyListView extends ListView {

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST));
    }
}
