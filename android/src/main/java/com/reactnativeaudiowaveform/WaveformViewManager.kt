package com.reactnativeaudiowaveform

import android.graphics.Color
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.LayoutShadowNode
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.EventDispatcher
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.visualizer.Utils
import com.reactnativeaudiowaveform.visualizer.WaveGravity
import com.reactnativeaudiowaveform.visualizer.WaveformSeekBar

abstract class WaveformViewManager (val reactApplicationContext: ReactApplicationContext) : SimpleViewManager<WaveformSeekBar>()  {
  lateinit var localEventDispatcher: EventDispatcher

  /**
   * Return a Waveform which will later hold the View
   */
  override fun createViewInstance(@NonNull reactContext: ThemedReactContext): WaveformSeekBar {
    val waveformSeekBar = WaveformSeekBar(reactContext)
    initView(reactContext, waveformSeekBar)
    return waveformSeekBar
  }

  override fun createShadowNodeInstance(): LayoutShadowNode {
    return WaveformViewShadowNode()
  }

  override fun createShadowNodeInstance(context: ReactApplicationContext): LayoutShadowNode {
    return WaveformViewShadowNode()
  }

  abstract fun initView(reactContext: ThemedReactContext, waveformSeekBar: WaveformSeekBar)

  fun dispatchJSEvent(@NonNull event: Event<*>) {
    if(!this::localEventDispatcher.isInitialized) {
      throw Exception(Constant.NOT_INIT_EVENT_DISPATCHER)
    }
    localEventDispatcher.dispatchEvent(event)
  }

  @ReactProp(name = "visibleProgress", defaultFloat = 0f)
  fun setVisibleProgress(view: WaveformSeekBar, @NonNull visibleProgress: Float) {
    view.setVisibleProgress(visibleProgress)
    DebugState.debug("setVisibleProgress -> visibleProgress: $visibleProgress")
  }

  @ReactProp(name = "progress", defaultFloat = 0f)
  fun setProgress(view: WaveformSeekBar, @NonNull progress: Float) {
    view.setProgress(progress)
    DebugState.debug("setProgress -> progress: $progress")
  }

  @ReactProp(name = "maxProgress", defaultFloat = 100f)
  fun setMaxProgress(view: WaveformSeekBar, @NonNull maxProgress: Float) {
    view.setMaxProgress(maxProgress)
    DebugState.debug("setMaxProgress -> maxProgress: $maxProgress")
  }

  @ReactProp(name = "waveWidth", defaultFloat = 10f)
  fun setWaveWidth(view: WaveformSeekBar, @NonNull waveWidth: Float) {
    view.setWaveWidth(Utils.dp(view.context, waveWidth))
    DebugState.debug("setWaveWidth -> waveWidth: $waveWidth")
  }

  @ReactProp(name = "gap", defaultFloat = 5f)
  fun setWaveGap(view: WaveformSeekBar, @NonNull gap: Float) {
    view.setWaveGap(Utils.dp(view.context, gap))
    DebugState.debug("setWaveGap -> gap: $gap")
  }

  @ReactProp(name = "minHeight", defaultFloat = 20f)
  fun setWaveMinHeight(view: WaveformSeekBar, @NonNull minHeight: Float) {
    view.setWaveMinHeight(Utils.dp(view.context, minHeight))
    DebugState.debug("setWaveMinHeight -> minHeight: $minHeight")
  }

  @ReactProp(name = "radius", defaultFloat = 5f)
  fun setWaveCornerRadius(view: WaveformSeekBar, @NonNull radius: Float) {
    view.setWaveCornerRadius(Utils.dp(view.context, radius))
    DebugState.debug("setWaveCornerRadius -> radius: $radius")
  }

  @ReactProp(name = "gravity")
  fun setWaveGravity(view: WaveformSeekBar, @Nullable gravity: String) {
    when(gravity) {
      "top" -> view.setWaveGravity(WaveGravity.TOP)
      "center" -> view.setWaveGravity(WaveGravity.CENTER)
      "bottom" -> view.setWaveGravity(WaveGravity.BOTTOM)
      else -> view.setWaveGravity(WaveGravity.CENTER)
    }
    DebugState.debug("setWaveGravity -> gravity: $gravity")
  }

  @ReactProp(name = "barBgColor")
  fun setWaveBackgroundColor(view: WaveformSeekBar, @Nullable backgroundColor: String?) {
    if(backgroundColor != null) {
      view.setWaveBackgroundColor(Color.parseColor(backgroundColor))
    } else {
      view.setWaveBackgroundColor(Color.BLACK)
    }
    DebugState.debug("setWaveBackgroundColor -> barBgColor: $backgroundColor")
  }

  @ReactProp(name = "barBgColor", defaultInt = Color.BLACK)
  fun setWaveBackgroundColor(view: WaveformSeekBar, @NonNull backgroundColor: Int) {
    view.setWaveBackgroundColor(backgroundColor)
    DebugState.debug("setWaveBackgroundColor -> barBgColor: $backgroundColor")
  }

  @ReactProp(name = "barPgColor")
  fun setWaveProgressColor(view: WaveformSeekBar, @Nullable progressColor: String?) {
    if(progressColor != null) {
      view.setWaveProgressColor(Color.parseColor(progressColor))
    } else {
      view.setWaveProgressColor(Color.RED)
    }
    DebugState.debug("setWaveProgressColor -> barPgColor: $progressColor")
  }

  @ReactProp(name = "barPgColor", defaultInt = Color.RED)
  fun setWaveProgressColor(view: WaveformSeekBar, @NonNull progressColor: Int) {
    view.setWaveProgressColor(progressColor)
    DebugState.debug("setWaveProgressColor -> barPgColor: $progressColor")
  }
}
