package com.reactnativeaudiowaveform.event.recorder

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter
import com.reactnativeaudiowaveform.audio.recorder.model.RecordState

class OnRecorderStateEvent(viewTag: Int, private val recordState: RecordState) : Event<OnRecorderStateEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putString("recordState", recordState.name)
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onRecorderState"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
