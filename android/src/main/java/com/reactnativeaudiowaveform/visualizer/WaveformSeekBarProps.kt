package com.reactnativeaudiowaveform.visualizer

import android.graphics.Color
import android.graphics.Paint
import com.reactnativeaudiowaveform.gradient.DefaultGradientName
import com.reactnativeaudiowaveform.gradient.GradientType
import com.reactnativeaudiowaveform.gradient.LinearGradientPojo

@Suppress("ArrayInDataClass")
data class WaveformSeekBarGradientProps(
  var name: DefaultGradientName? = null,
  var type: GradientType = GradientType.LINEAR,
  var radialRadius: Float? = null,
  var angle: Double? = null,
  var color: IntArray? = null,
  var positions: FloatArray? = null,
)

data class WaveformSeekBarProps(
  var progress: Float = 0F,
  var maxProgress: Float = 100F,
  var waveBackgroundColor: Int = Color.LTGRAY,
  var waveBackgroundGradient: WaveformSeekBarGradientProps? = null,
  var waveBackgroundGradientData: LinearGradientPojo? = null,
  var waveProgressColor: Int = Color.WHITE,
  var waveProgressGradient: WaveformSeekBarGradientProps? = null,
  var waveProgressGradientData: LinearGradientPojo? = null,
  var waveGap: Float = 2f,
  var waveWidth: Float = 2f,
  var waveMinHeight: Float = 2f,
  var waveMaxHeight: Int = 0,
  var waveTopLeftCornerRadius: Float = 2f,
  var waveTopRightCornerRadius: Float = 2f,
  var waveBottomLeftCornerRadius: Float = 2f,
  var waveBottomRightCornerRadius: Float = 2f,
  var waveGravity: WaveGravity = WaveGravity.CENTER,
  var waveType: WaveType = WaveType.PLAYER,
  var visibleProgress: Float = 0F,
  var strokeWidth: Float = 0F,
  var strokeCap: Paint.Cap = Paint.Cap.ROUND,
  var strokeJoin: Paint.Join = Paint.Join.ROUND,
  var strokeMiter: Float = 0F,
  var strokeDashWidth: Float = 0F,
  var strokeDashGap: Float = 0F,
  var waveStrokeBackgroundColor: Int = Color.TRANSPARENT,
  var waveStrokeBackgroundGradient: WaveformSeekBarGradientProps? = null,
  var waveStrokeBackgroundGradientData: LinearGradientPojo? = null,
  var waveStrokeProgressColor: Int = Color.TRANSPARENT,
  var waveStrokeProgressGradient: WaveformSeekBarGradientProps? = null,
  var waveStrokeProgressGradientData: LinearGradientPojo? = null
)

