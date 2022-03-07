package com.reactnativeaudiowaveform

import android.Manifest
import android.media.AudioFormat
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerModule
import com.reactnativeaudiowaveform.audio.recorder.config.AudioRecordConfig
import com.reactnativeaudiowaveform.audio.recorder.constants.AudioConstants
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.config.FFmpegConvertConfig
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegBitRate
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegSamplingRate
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.event.recorder.*
import com.reactnativeaudiowaveform.permission.RuntimePermission
import com.reactnativeaudiowaveform.permission.callback.PermissionCallback
import com.reactnativeaudiowaveform.visualizer.SeekBarOnProgressChanged
import com.reactnativeaudiowaveform.visualizer.WaveType
import com.reactnativeaudiowaveform.visualizer.WaveformSeekBar

class AudioRecorderWaveformViewManager(reactApplicationContext: ReactApplicationContext) : WaveformViewManager(reactApplicationContext), PermissionListener {
  private lateinit var recorder: Recorder
  private lateinit var runtimePermission: RuntimePermission

  companion object {
    const val COMMAND_RECORDER_CREATE = 1
    const val COMMAND_RECORDER_START = 2
    const val COMMAND_RECORDER_PAUSE = 3
    const val COMMAND_RECORDER_RESUME = 4
    const val COMMAND_RECORDER_STOP = 5
    const val COMMAND_RECORDER_CANCEL = 6

    const val TAG = "AudioRecorderWaveformView"
  }

  override fun getName() = TAG

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
      .put("cancel", COMMAND_RECORDER_CANCEL)
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
        val subscriptionDurationInMilliseconds = Utils.getArgsValue(args, 10, null, Int::class)?.toLong() ?: AudioConstants.SUBSCRIPTION_DURATION_IN_MILLISECONDS
        val config = AudioRecordConfig(audioSource, audioEncoding, channel, frequency)
        val convertConfig = FFmpegConvertConfig(bitRate, samplingRate, mono)
        setUpRecorder(sourceMode, isFFmpegMode, isDebug, subscriptionDurationInMilliseconds, config, convertConfig, root)
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
      COMMAND_RECORDER_CANCEL -> cancelRecording(root)
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

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ): Boolean {
    DebugState.debug("onRequestPermissionsResult -> $requestCode,${permissions},${grantResults}")
    runtimePermission.onRequestPermissionsResult(requestCode, permissions, grantResults)
    return false
  }

  override fun onDropViewInstance(view: WaveformSeekBar) {
    super.onDropViewInstance(view)
    DebugState.debug("onDropViewInstance")
    try {
      if(this::recorder.isInitialized) {
        recorder.stopRecording()
      }
    }  catch (e: Exception) {
      DebugState.error("onDropViewInstance", e)
    }
  }

  override fun initView(@NonNull reactContext: ThemedReactContext, @NonNull waveformSeekBar: WaveformSeekBar) {
    localEventDispatcher = reactContext.getNativeModule(UIManagerModule::class.java)!!.eventDispatcher
    waveformSeekBar.setWaveType(WaveType.RECORDER)
    waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
      override fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean) {
        DebugState.debug("onProgressChanged -> progress: $progress, fromUser: $fromUser")
        if (progress in 0f..100f) {
          dispatchJSEvent(OnSeekChangeEvent(waveformSeekBar.id, progress, fromUser))
        }
      }
    }
  }

  private fun setUpRecorder(
    @NonNull sourceMode: String,
    @NonNull isFFmpegMode: Boolean,
    @NonNull isDebug: Boolean,
    @NonNull subscriptionDurationInMilliseconds: Long,
    @NonNull config: AudioRecordConfig,
    @NonNull convertConfig: FFmpegConvertConfig,
    @NonNull root: WaveformSeekBar
  ) {
    DebugState.state = isDebug
    DebugState.debug("setUpRecorder -> $sourceMode $isFFmpegMode $isDebug $subscriptionDurationInMilliseconds $config $convertConfig")
    try {
      recorder = Recorder.getInstance(reactApplicationContext.applicationContext)
        .init(sourceMode, isFFmpegMode, isDebug, subscriptionDurationInMilliseconds, config, convertConfig).apply {
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
            root.printAmplitudeList()
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

  private fun setPermissionResult(it: PermissionCallback, @NonNull filePath: String, @NonNull root: WaveformSeekBar) {
    DebugState.debug("setPermissionResult -> $filePath")
    with(it) {
      if (hasAccepted()) {
        try {
          if(checkRecorderInit(root)) {
            recorder.setSource(filePath)
            recorder.startRecording()
          }
        } catch (e: Exception) {
          DebugState.error("setPermissionResult", e)
          dispatchJSEvent(OnErrorEvent(root.id, e))
        }
      }

      if (hasDenied()) {
        AlertDialog.Builder(root.context)
          .setTitle(Constant.REQUIRED_PERMISSION)
          .setMessage(Constant.REQUIRED_PERMISSION_DENIED)
          .setPositiveButton(android.R.string.ok) { _, _ ->
            askAgain()
          }
          .setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
          }
          .show()
      }

      if (hasForeverDenied()) {
        AlertDialog.Builder(root.context)
          .setTitle(Constant.REQUIRED_PERMISSION)
          .setMessage(Constant.REQUIRED_PERMISSION_FOREVERDENIED)
          .setPositiveButton(android.R.string.ok) { _, _ ->
            goToSettings()
          }
          .setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
          }
          .show()
      }
    }
  }

  private fun startRecording(@NonNull filePath: String, @NonNull root: WaveformSeekBar) {
    DebugState.debug("startRecording -> $filePath")
    runtimePermission = RuntimePermission(reactApplicationContext.currentActivity, this)
      .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
      .onResponse { setPermissionResult(it, filePath, root) }
    runtimePermission.ask()
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

  private fun cancelRecording(@NonNull root: WaveformSeekBar) {
    DebugState.debug("cancelRecording")
    try {
      if(checkRecorderInit(root)) {
        recorder.cancelRecording()
      }
    }  catch (e: Exception) {
      DebugState.error("cancelRecording", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }
}
