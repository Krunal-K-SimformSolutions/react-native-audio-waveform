package com.reactnativeaudiowaveform.event.player

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter
import com.reactnativeaudiowaveform.audio.player.model.AmpsState

class OnAmpsStateEvent(viewTag: Int, private val ampsState: AmpsState) : Event<OnAmpsStateEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putString("ampsState", ampsState.name)
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onAmpsState"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
