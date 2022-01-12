package com.reactnativeaudiowaveform.visualizer

import android.content.Context
import android.util.TypedValue

object Utils {

    @JvmStatic
    fun dp(context: Context?, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context!!.resources.displayMetrics)
    }
}
