package com.reactnativeaudiowaveform

import android.graphics.Color
import android.media.AudioFormat
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerModule
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.EventDispatcher
import com.reactnativeaudiowaveform.audio.recorder.config.AudioRecordConfig
import com.reactnativeaudiowaveform.audio.recorder.constants.AudioConstants
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.config.FFmpegConvertConfig
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegBitRate
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegSamplingRate
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.event.recorder.*
import com.reactnativeaudiowaveform.visualizer.SeekBarOnProgressChanged
import com.reactnativeaudiowaveform.visualizer.WaveGravity
import com.reactnativeaudiowaveform.visualizer.WaveType
import com.reactnativeaudiowaveform.visualizer.WaveformSeekBar

class AudioRecorderWaveformViewManager(private val reactApplicationContext: ReactApplicationContext) : SimpleViewManager<WaveformSeekBar>() {
  private lateinit var recorder: Recorder
  private lateinit var localEventDispatcher: EventDispatcher

  companion object {
    const val COMMAND_RECORDER_CREATE = 1
    const val COMMAND_RECORDER_START = 2
    const val COMMAND_RECORDER_PAUSE = 3
    const val COMMAND_RECORDER_RESUME = 4
    const val COMMAND_RECORDER_STOP = 5
    const val TAG = "AudioRecorderWaveformView"
  }

  override fun getName() = TAG

  /**
   * Return a Waveform which will later hold the View
   */
  override fun createViewInstance(@NonNull reactContext: ThemedReactContext): WaveformSeekBar {
    val waveformSeekBar = WaveformSeekBar(reactContext)
    initView(reactContext, waveformSeekBar)
    return waveformSeekBar
  }

  /**
   * Map the "create, Start, Pause, Resume & Stop" command to an integer
   */
  @Nullable
  override fun getCommandsMap(): Map<String, Int>? {
    return MapBuilder.builder<String, Int>()
      .put("create", COMMAND_RECORDER_CREATE)
      .put("start", COMMAND_RECORDER_START)
      .put("pause", COMMAND_RECORDER_PAUSE)
      .put("resume", COMMAND_RECORDER_RESUME)
      .put("stop", COMMAND_RECORDER_STOP)
      .build()
  }

  /**
   * Handle "create" command (called from JS) and call createFragment method
   */
  override fun receiveCommand(@NonNull root: WaveformSeekBar, @NonNull commandId: Int, @Nullable args: ReadableArray?) {
    super.receiveCommand(root, commandId.toString(), args)
    when (commandId) {
      COMMAND_RECORDER_CREATE -> {
        val sourceMode = Utils.getArgsValue(args, 1, AudioConstants.SOURCE_MODE, String::class) ?: AudioConstants.SOURCE_MODE
        val isFFmpegMode = Utils.getArgsValue(args, 2, false, Boolean::class)?: false
        val isDebug = Utils.getArgsValue(args, 3, true, Boolean::class) ?: true
        val audioSource: Int = Utils.getAudioSource(Utils.getArgsValue(args, 4, null, Int::class))
        val audioEncoding: Int = Utils.getAudioEncoding(Utils.getArgsValue(args, 5, null, Int::class))
        val channel: Int = AudioFormat.CHANNEL_IN_MONO
        val frequency: Int  = Utils.getArgsValue(args, 6, AudioConstants.FREQUENCY_44100, Int::class) ?: AudioConstants.FREQUENCY_44100
        val bitRate: FFmpegBitRate  = Utils.getFFmpegBitRate(Utils.getArgsValue(args, 7, null, Int::class))
        val samplingRate: FFmpegSamplingRate = Utils.getFFmpegSamplingRate(Utils.getArgsValue(args, 8, null, Int::class))
        val mono: Boolean  = Utils.getArgsValue(args, 9, true, Boolean::class) ?: true

        val config = AudioRecordConfig(audioSource, audioEncoding, channel, frequency)
        val convertConfig = FFmpegConvertConfig(bitRate, samplingRate, mono)
        setUpRecorder(sourceMode, isFFmpegMode, isDebug, config, convertConfig, root)
      }
      COMMAND_RECORDER_START -> {
        val filepath = Utils.getArgsValue(args, 1, null, String::class)
        if (filepath != null) {
          startRecording(filepath, root)
        }
      }
      COMMAND_RECORDER_PAUSE -> pauseRecording(root)
      COMMAND_RECORDER_RESUME -> resumeRecording(root)
      COMMAND_RECORDER_STOP -> stopRecording(root)
      else -> {}
    }
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
    return MapBuilder.builder<String, Any>()
      .put(OnSeekChangeEvent.EVENT_NAME, MapBuilder.of("registrationName", OnSeekChangeEvent.EVENT_NAME))
      .put(OnErrorEvent.EVENT_NAME, MapBuilder.of("registrationName", OnErrorEvent.EVENT_NAME))
      .put(OnBufferEvent.EVENT_NAME, MapBuilder.of("registrationName", OnBufferEvent.EVENT_NAME))
      .put(OnFFmpegStateEvent.EVENT_NAME, MapBuilder.of("registrationName", OnFFmpegStateEvent.EVENT_NAME))
      .put(OnFinishedEvent.EVENT_NAME, MapBuilder.of("registrationName", OnFinishedEvent.EVENT_NAME))
      .put(OnProgressEvent.EVENT_NAME, MapBuilder.of("registrationName", OnProgressEvent.EVENT_NAME))
      .put(OnRecorderStateEvent.EVENT_NAME, MapBuilder.of("registrationName", OnRecorderStateEvent.EVENT_NAME))
      .put(OnSilentDetectedEvent.EVENT_NAME, MapBuilder.of("registrationName", OnSilentDetectedEvent.EVENT_NAME))
      .build()
  }

  @ReactProp(name = "visibleProgress", defaultFloat = 50f)
  fun setVisibleProgress(view: WaveformSeekBar, @NonNull visibleProgress: Float) {
    view.visibleProgress = visibleProgress
    DebugState.debug("setVisibleProgress -> visibleProgress: $visibleProgress")
  }

  @ReactProp(name = "progress", defaultFloat = 0f)
  fun setProgress(view: WaveformSeekBar, @NonNull progress: Float) {
    view.progress = progress
    DebugState.debug("setProgress -> progress: $progress")
  }

  @ReactProp(name = "maxProgress", defaultFloat = 100f)
  fun setMaxProgress(view: WaveformSeekBar, @NonNull maxProgress: Float) {
    view.maxProgress = maxProgress
    DebugState.debug("setMaxProgress -> maxProgress: $maxProgress")
  }

  @ReactProp(name = "waveWidth", defaultFloat = 10f)
  fun setWaveWidth(view: WaveformSeekBar, @NonNull waveWidth: Float) {
    view.waveWidth = waveWidth
    DebugState.debug("setWaveWidth -> waveWidth: $waveWidth")
  }

  @ReactProp(name = "gap", defaultFloat = 5f)
  fun setWaveGap(view: WaveformSeekBar, @NonNull gap: Float) {
    view.waveGap = gap
    DebugState.debug("setWaveGap -> gap: $gap")
  }

  @ReactProp(name = "minHeight", defaultFloat = 20f)
  fun setWaveMinHeight(view: WaveformSeekBar, @NonNull minHeight: Float) {
    view.waveMinHeight = minHeight
    DebugState.debug("setWaveMinHeight -> minHeight: $minHeight")
  }

  @ReactProp(name = "radius", defaultFloat = 5f)
  fun setWaveCornerRadius(view: WaveformSeekBar, @NonNull radius: Float) {
    view.waveCornerRadius = radius
    DebugState.debug("setWaveCornerRadius -> radius: $radius")
  }

  @ReactProp(name = "gravity")
  fun setWaveGravity(view: WaveformSeekBar, @Nullable gravity: String) {
    when(gravity) {
      "top" -> view.waveGravity = WaveGravity.TOP
      "center" -> view.waveGravity = WaveGravity.CENTER
      "bottom" -> view.waveGravity = WaveGravity.BOTTOM
      else -> view.waveGravity = WaveGravity.CENTER
    }
    DebugState.debug("setWaveGravity -> gravity: $gravity")
  }

  @ReactProp(name = "backgroundColor")
  fun setWaveBackgroundColor(view: WaveformSeekBar, @Nullable backgroundColor: String?) {
    if(backgroundColor != null) {
      view.waveBackgroundColor = Color.parseColor(backgroundColor)
    } else {
      view.waveBackgroundColor = Color.BLACK
    }
    DebugState.debug("setWaveBackgroundColor -> backgroundColor: $backgroundColor")
  }

  @ReactProp(name = "backgroundColor", defaultInt = 0)
  fun setWaveBackgroundColor(view: WaveformSeekBar, @NonNull backgroundColor: Int) {
    view.waveBackgroundColor = backgroundColor
    DebugState.debug("setWaveBackgroundColor -> backgroundColor: $backgroundColor")
  }

  @ReactProp(name = "progressColor")
  fun setWaveProgressColor(view: WaveformSeekBar, @Nullable progressColor: String?) {
    if(progressColor != null) {
      view.waveProgressColor = Color.parseColor(progressColor)
    } else {
      view.waveProgressColor = Color.RED
    }
    DebugState.debug("setWaveProgressColor -> progressColor: $progressColor")
  }

  @ReactProp(name = "progressColor", defaultInt = 0)
  fun setWaveProgressColor(view: WaveformSeekBar, @NonNull progressColor: Int) {
    view.waveProgressColor = progressColor
    DebugState.debug("setWaveProgressColor -> progressColor: $progressColor")
  }

  private fun initView(@NonNull reactContext: ThemedReactContext, @NonNull waveformSeekBar: WaveformSeekBar) {
    localEventDispatcher = reactContext.getNativeModule(UIManagerModule::class.java).eventDispatcher

    waveformSeekBar.waveType = WaveType.RECORDER
    waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
      override fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean) {
        DebugState.debug("onProgressChanged -> progress: $progress, fromUser: $fromUser")
        if (progress in 0f..100f) {
          dispatchJSEvent(OnSeekChangeEvent(waveformSeekBar.id, progress, fromUser))
        }
      }
    }
  }

  private fun dispatchJSEvent(@NonNull event: Event<*>) {
    if(!this::localEventDispatcher.isInitialized) {
      throw Exception(Constant.NOT_INIT_EVENT_DISPATCHER)
    }
    localEventDispatcher.dispatchEvent(event)
  }

  private fun setUpRecorder(
    @NonNull sourceMode: String,
    @NonNull isFFmpegMode: Boolean,
    @NonNull isDebug: Boolean,
    @NonNull config: AudioRecordConfig,
    @NonNull convertConfig: FFmpegConvertConfig,
    @NonNull root: WaveformSeekBar
  ) {
    DebugState.state = isDebug
    DebugState.debug("setUpRecorder -> $sourceMode $isFFmpegMode $isDebug $config $convertConfig")
    try {
      recorder = Recorder.getInstance(reactApplicationContext.applicationContext)
        .init(sourceMode, isFFmpegMode, isDebug, config, convertConfig).apply {
          onRawBuffer = {
            DebugState.debug("onRawBuffer -> audioChunk: ${it.getMaxAmplitude()}")
            root.addAmp(it.getMaxAmplitude())
            dispatchJSEvent(OnBufferEvent(root.id, it))
          }
          onSilentDetected = {
            DebugState.debug("onSilentDetected -> time: $it")
            dispatchJSEvent(OnSilentDetectedEvent(root.id, it))
          }
          onProgress = { currentTime, maxTime ->
            DebugState.debug("onProgress -> currentTime: $currentTime, maxTime: $maxTime")
            dispatchJSEvent(OnProgressEvent(root.id, currentTime, maxTime))
          }
          onFFmpegState = {
            DebugState.debug("onFFmpegState -> fFmpegState: $it")
            dispatchJSEvent(OnFFmpegStateEvent(root.id, it))
          }
          onRecordState = {
            DebugState.debug("onRecordState -> recordState: $it")
            dispatchJSEvent(OnRecorderStateEvent(root.id, it))
          }
          onFinished = { file, metadata ->
            DebugState.debug("onFinished -> file: $file, metadata: $metadata")
            dispatchJSEvent(OnFinishedEvent(root.id, file, metadata))
          }
        }
    } catch (e: Exception) {
      DebugState.error("setUpRecorder", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }

  private fun checkRecorderInit(@NonNull root: WaveformSeekBar): Boolean {
    if(!this::recorder.isInitialized) {
      dispatchJSEvent(OnErrorEvent(root.id, Exception(Constant.NOT_INIT_RECORDER)))
      return false
    }
    return true
  }

  private fun startRecording(@NonNull filePath: String, @NonNull root: WaveformSeekBar) {
    DebugState.debug("startRecording -> $filePath")
    try {
      if(checkRecorderInit(root)) {
        recorder.setSource(filePath)
        recorder.startRecording()
      }
    } catch (e: Exception) {
      DebugState.error("startRecording", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }

  private fun pauseRecording(@NonNull root: WaveformSeekBar) {
    DebugState.debug("pauseRecording")
    try {
      if(checkRecorderInit(root)) {
        recorder.pauseRecording()
      }
    }  catch (e: Exception) {
      DebugState.error("pauseRecording", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }

  private fun resumeRecording(@NonNull root: WaveformSeekBar) {
    DebugState.debug("resumeRecording")
    try {
      if(checkRecorderInit(root)) {
        recorder.resumeRecording()
      }
    }  catch (e: Exception) {
      DebugState.error("resumeRecording", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }

  private fun stopRecording(@NonNull root: WaveformSeekBar) {
    DebugState.debug("stopRecording")
    try {
      if(checkRecorderInit(root)) {
        recorder.stopRecording()
      }
    }  catch (e: Exception) {
      DebugState.error("stopRecording", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }
}
