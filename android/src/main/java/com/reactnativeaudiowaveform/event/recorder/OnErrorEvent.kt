package com.reactnativeaudiowaveform.event.recorder

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter

class OnErrorEvent(viewTag: Int, private val error: Exception) : Event<OnErrorEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putString("error", error.toString())
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onError"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
