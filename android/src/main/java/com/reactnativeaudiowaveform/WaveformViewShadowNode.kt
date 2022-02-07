package com.reactnativeaudiowaveform

import com.facebook.react.uimanager.LayoutShadowNode
import com.facebook.yoga.YogaMeasureFunction
import com.facebook.yoga.YogaMeasureMode
import com.facebook.yoga.YogaMeasureOutput
import com.facebook.yoga.YogaNode

class WaveformViewShadowNode : LayoutShadowNode(), YogaMeasureFunction {
  override fun measure(
    node: YogaNode,
    width: Float, widthMode: YogaMeasureMode,
    height: Float, heightMode: YogaMeasureMode
  ): Long {
    return YogaMeasureOutput.make(width, height)
  }

  init {
    setMeasureFunction(this)
  }
}
