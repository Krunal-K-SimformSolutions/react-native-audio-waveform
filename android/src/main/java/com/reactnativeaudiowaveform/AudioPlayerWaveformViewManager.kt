package com.reactnativeaudiowaveform

import android.graphics.Color
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
import com.reactnativeaudiowaveform.audio.player.model.AmpsState
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.event.player.*
import com.reactnativeaudiowaveform.visualizer.SeekBarOnProgressChanged
import com.reactnativeaudiowaveform.visualizer.WaveGravity
import com.reactnativeaudiowaveform.visualizer.WaveType
import com.reactnativeaudiowaveform.visualizer.WaveformSeekBar
import java.lang.Exception

class AudioPlayerWaveformViewManager(private val reactApplicationContext: ReactApplicationContext) : SimpleViewManager<WaveformSeekBar>() {
  private lateinit var player: Player
  private lateinit var localEventDispatcher: EventDispatcher

  companion object {
    const val COMMAND_PLAYER_CREATE = 1
    const val COMMAND_PLAYER_SOURCE = 2
    const val COMMAND_PLAYER_START = 3
    const val COMMAND_PLAYER_PAUSE = 4
    const val COMMAND_PLAYER_RESUME = 5
    const val COMMAND_PLAYER_STOP = 6
    const val TAG = "AudioPlayerWaveformView"
  }

  override fun getName() = TAG

  /**
   * Return a Waveform which will later hold the View
   */
  override fun createViewInstance(reactContext: ThemedReactContext): WaveformSeekBar {
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
        val rnDebug = Utils.getArgsValue(args, 1, true, Boolean::class) ?: true
        setUpPlayer(rnDebug, root)
      }
      COMMAND_PLAYER_SOURCE -> {
        val rnFilepath = Utils.getArgsValue(args, 1, null, String::class)
        if (rnFilepath != null) {
          setSource(rnFilepath, root)
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

  private fun dispatchJSEvent(@NonNull event: Event<*>) {
    if(!this::localEventDispatcher.isInitialized) {
      throw Exception(Constant.NOT_INIT_EVENT_DISPATCHER)
    }
    localEventDispatcher.dispatchEvent(event)
  }

  private fun setUpPlayer(
    @NonNull isDebug: Boolean,
    @NonNull root: WaveformSeekBar
  ) {
    DebugState.state = isDebug
    DebugState.debug("setUpPlayer -> $isDebug")
    try {
      player = Player.getInstance(reactApplicationContext.applicationContext).init(isDebug).apply {
        onProgress = { time, _ ->
          DebugState.debug("onProgress -> time: $time")
          dispatchJSEvent(OnProgressEvent(root.id, time))
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

  private fun setSource(@NonNull filePath: String, @NonNull root: WaveformSeekBar) {
    DebugState.debug("setSource -> $filePath")
    try {
      if(checkPlayerInit(root)) {
        player.setSource(filePath)
        dispatchJSEvent(OnAmpsStateEvent(root.id, AmpsState.START))
        player.loadFileAmps().subscribe({ amps ->
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
    try {
      if(checkPlayerInit(root)) {
        player.startPlaying()
      }
    } catch (e: Exception) {
      DebugState.error("startPlaying", e)
      dispatchJSEvent(OnErrorEvent(root.id, e))
    }
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
