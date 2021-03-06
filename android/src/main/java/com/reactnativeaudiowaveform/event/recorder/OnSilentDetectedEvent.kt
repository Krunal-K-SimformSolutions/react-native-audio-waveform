package com.reactnativeaudiowaveform.event.recorder

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter

class OnSilentDetectedEvent(viewTag: Int, private val time: Long) : Event<OnSilentDetectedEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putDouble("time", time.toDouble())
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onSilentDetected"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
