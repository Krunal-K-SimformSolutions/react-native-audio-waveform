package com.reactnativeaudiowaveform.event.recorder

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegConvertState

class OnFFmpegStateEvent(viewTag: Int, private val convertState: FFmpegConvertState) : Event<OnFFmpegStateEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putString("ffmpegState", convertState.name)
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onFFmpegState"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
