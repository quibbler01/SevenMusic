package com.google.zxing.view

import com.google.zxing.ResultPoint
import com.google.zxing.ResultPointCallback

class ViewfinderResultPointCallback(private val viewfinderView: ViewfinderView?) :
    ResultPointCallback {
    override fun foundPossibleResultPoint(point: ResultPoint?) {
        // viewfinderView.addPossibleResultPoint(point);
    }
}
