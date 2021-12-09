package com.reactnativeaudiowaveform.event.player

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter

class OnProgressEvent(viewTag: Int, private val currentTime: Long) : Event<OnProgressEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putDouble("currentTime", currentTime.toDouble())
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onProgress"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
