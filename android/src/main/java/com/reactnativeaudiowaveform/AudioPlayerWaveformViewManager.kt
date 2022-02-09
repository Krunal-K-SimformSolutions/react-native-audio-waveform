package com.reactnativeaudiowaveform

import android.Manifest
import androidx.annotation.FloatRange
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerModule
import com.facebook.react.uimanager.annotations.ReactProp
import com.reactnativeaudiowaveform.audio.player.model.AmpsState
import com.reactnativeaudiowaveform.audio.recorder.constants.AudioConstants
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.event.player.*
import com.reactnativeaudiowaveform.permission.RuntimePermission
import com.reactnativeaudiowaveform.permission.callback.PermissionCallback
import com.reactnativeaudiowaveform.visualizer.SeekBarOnProgressChanged
import com.reactnativeaudiowaveform.visualizer.WaveType
import com.reactnativeaudiowaveform.visualizer.WaveformSeekBar

class AudioPlayerWaveformViewManager(reactApplicationContext: ReactApplicationContext) : WaveformViewManager(reactApplicationContext),
  PermissionListener {
  private lateinit var player: Player
  private lateinit var runtimePermission: RuntimePermission

  companion object {
    const val COMMAND_PLAYER_CREATE = 1
    const val COMMAND_PLAYER_SOURCE = 2
    const val COMMAND_PLAYER_START = 3
    const val COMMAND_PLAYER_PAUSE = 4
    const val COMMAND_PLAYER_RESUME = 5
    const val COMMAND_PLAYER_STOP = 6
    const val TAG = "AudioPlayerWaveformView"
  }

  override fun getName(): String = TAG

  /**
   * Map the "create, Start, Pause, Resume & Stop" command to an integer
   */
  @Nullable
  override fun getCommandsMap(): Map<String, Int>? {
    return MapBuilder.builder<String, Int>()
      .put("create", COMMAND_PLAYER_CREATE)
      .put("source", COMMAND_PLAYER_SOURCE)
      .put("start", COMMAND_PLAYER_START)
      .put("pause", COMMAND_PLAYER_PAUSE)
      .put("resume", COMMAND_PLAYER_RESUME)
      .put("stop", COMMAND_PLAYER_STOP)
      .build()
  }

  /**
   * Handle "create" command (called from JS) and call createFragment method
   */
  override fun receiveCommand(@NonNull root: WaveformSeekBar, @NonNull commandId: Int, @Nullable args: ReadableArray?) {
    super.receiveCommand(root, commandId.toString(), args)
    when (commandId) {
      COMMAND_PLAYER_CREATE -> {
        val isDebug = Utils.getArgsValue(args, 1, true, Boolean::class) ?: true
        val subscriptionDurationInMilliseconds = Utils.getArgsValue(args, 2, null, Int::class)?.toLong() ?: AudioConstants.SUBSCRIPTION_DURATION_IN_MILLISECONDS

        setUpPlayer(isDebug, subscriptionDurationInMilliseconds, root)
      }
      COMMAND_PLAYER_SOURCE -> {
        val filepath = Utils.getArgsValue(args, 1, null, String::class)
        val isAmplitudaMode = Utils.getArgsValue(args, 2, false, Boolean::class)?: false
        if (filepath != null) {
          setSource(filepath, isAmplitudaMode, root)
        }
      }
      COMMAND_PLAYER_START -> startPlaying(root)
      COMMAND_PLAYER_PAUSE -> pausePlaying(root)
      COMMAND_PLAYER_RESUME -> resumePlaying(root)
      COMMAND_PLAYER_STOP -> stopPlaying(root)
      else -> {}
    }
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
    return MapBuilder.builder<String, Any>()
      .put(OnSeekChangeEvent.EVENT_NAME, MapBuilder.of("registrationName", OnSeekChangeEvent.EVENT_NAME))
      .put(OnErrorEvent.EVENT_NAME, MapBuilder.of("registrationName", OnErrorEvent.EVENT_NAME))
      .put(OnPlayerStateEvent.EVENT_NAME, MapBuilder.of("registrationName", OnPlayerStateEvent.EVENT_NAME))
      .put(OnProgressEvent.EVENT_NAME, MapBuilder.of("registrationName", OnProgressEvent.EVENT_NAME))
      .put(OnLoadAmpsEvent.EVENT_NAME, MapBuilder.of("registrationName", OnLoadAmpsEvent.EVENT_NAME))
      .put(OnAmpsStateEvent.EVENT_NAME, MapBuilder.of("registrationName", OnAmpsStateEvent.EVENT_NAME))
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
      if(this::player.isInitialized) {
        player.stopPlaying()
      }
    } catch (e: Exception) {
      DebugState.error("onHostDestroy", e)
    }
  }

  @ReactProp(name = "playbackSpeed", defaultFloat = 1f)
  fun setPlaybackSpeed(view: WaveformSeekBar, @FloatRange(from = 0.0, fromInclusive = false) speed: Float) {
    DebugState.debug("setPlaybackSpeed -> playbackSpeed: $speed")
    try {
      if(checkPlayerInit(view)) {
        player.playbackSpeed(speed)
      }
    } catch (e: Exception) {
      DebugState.error("setPlaybackSpeed", e)
      dispatchJSEvent(OnErrorEvent(view.id, e))
    }
  }

  override fun initView(@NonNull reactContext: ThemedReactContext, @NonNull waveformSeekBar: WaveformSeekBar) {
    localEventDispatcher = reactContext.getNativeModule(UIManagerModule::class.java).eventDispatcher
    waveformSeekBar.waveType = WaveType.PLAYER
    waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
      override fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean) {
        DebugState.debug("onProgressChanged -> progress: $progress, fromUser: $fromUser")
        if (progress in 0f..100f) {
          dispatchJSEvent(OnSeekChangeEvent(waveformSeekBar.id, progress, fromUser))
        }
      }
    }
  }

  private fun setPermissionResult(it: PermissionCallback, @NonNull root: WaveformSeekBar) {
    DebugState.debug("setPermissionResult ->")
    with(it) {
      if (hasAccepted()) {
        try {
          if(checkPlayerInit(root)) {
            player.startPlaying()
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

  private fun setUpPlayer(
    @NonNull isDebug: Boolean,
    @NonNull subscriptionDurationInMilliseconds: Long,
    @NonNull root: WaveformSeekBar
  ) {
    DebugState.state = isDebug
    DebugState.debug("setUpPlayer -> $isDebug $subscriptionDurationInMilliseconds")
    try {
      player = Player.getInstance(reactApplicationContext.applicationContext).init(isDebug, subscriptionDurationInMilliseconds).apply {
        onProgress = { time, _ ->
          DebugState.debug("onProgress -> time: $time")
          dispatchJSEvent(OnProgressEvent(root.id, time, player.getTotalDuration()))
          root.progress = (time.toFloat() / player.getTotalDuration()) * 100
        }
        onPlayState = {
          DebugState.debug("onPlayState -> playState: $it")
          dispatchJSEvent(OnPlayerStateEvent(root.id, it))
        }
      }
    } catch (e: Exception) {
      DebugState.error("setUpPlayer", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }

  private fun checkPlayerInit(@NonNull root: WaveformSeekBar): Boolean {
    if(!this::player.isInitialized) {
      dispatchJSEvent(OnErrorEvent(root.id, Exception(Constant.NOT_INIT_PLAYER)))
      return false
    }
    return true
  }

  private fun setSource(@NonNull filePath: String, @NonNull isAmplitudaMode: Boolean, @NonNull root: WaveformSeekBar) {
    DebugState.debug("setSource -> $filePath")
    try {
      if(checkPlayerInit(root)) {
        player.setSource(filePath)
        dispatchJSEvent(OnAmpsStateEvent(root.id, AmpsState.START))
        player.loadFileAmps(isAmplitudaMode).subscribe({ amps ->
          DebugState.debug("loadFileAmps -> amps: $amps")
          root.setWaveForm(amps)
          dispatchJSEvent(OnLoadAmpsEvent(root.id, amps))
          dispatchJSEvent(OnAmpsStateEvent(root.id, AmpsState.SUCCESS))
        }, {
          DebugState.error("loadFileAmps", it)
          dispatchJSEvent(OnErrorEvent(root.id, Exception(it)))
          dispatchJSEvent(OnAmpsStateEvent(root.id, AmpsState.ERROR))
        }, {
          dispatchJSEvent(OnAmpsStateEvent(root.id, AmpsState.COMPLETED))
        })
      }
    } catch (e: Exception) {
      DebugState.error("setSource", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }

  private fun startPlaying(@NonNull root: WaveformSeekBar) {
    DebugState.debug("startPlaying")
    runtimePermission = RuntimePermission(reactApplicationContext.currentActivity, this)
      .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
      .onResponse { setPermissionResult(it, root) }
    runtimePermission.ask()
  }

  private fun pausePlaying(@NonNull root: WaveformSeekBar) {
    DebugState.debug("pausePlaying")
    try {
      if(checkPlayerInit(root)) {
        player.pausePlaying()
      }
    } catch (e: Exception) {
      DebugState.error("pausePlaying", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }

  private fun resumePlaying(@NonNull root: WaveformSeekBar) {
    DebugState.debug("resumePlaying")
    try {
      if(checkPlayerInit(root)) {
        player.resumePlaying()
      }
    } catch (e: Exception) {
      DebugState.error("resumePlaying", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }

  private fun stopPlaying(@NonNull root: WaveformSeekBar) {
    DebugState.debug("stopPlaying")
    try {
      if(checkPlayerInit(root)) {
        player.stopPlaying()
      }
    } catch (e: Exception) {
      DebugState.error("stopPlaying", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
  }
}
