package com.reactnativeaudiowaveform.event.player

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter
import com.reactnativeaudiowaveform.Utils

class OnLoadAmpsEvent(viewTag: Int, private val amps: List<Int>) : Event<OnLoadAmpsEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putArray("loadAmps", Utils.toWritableArray(amps))
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onLoadAmps"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
