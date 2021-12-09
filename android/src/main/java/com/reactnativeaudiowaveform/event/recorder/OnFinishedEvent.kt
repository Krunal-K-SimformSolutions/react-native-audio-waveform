package com.reactnativeaudiowaveform.event.recorder

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter
import com.reactnativeaudiowaveform.audio.recorder.model.RecordMetadata
import java.io.File

class OnFinishedEvent(viewTag: Int, private val file: File?, private val metadata: RecordMetadata?) : Event<OnFinishedEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putString("file", metadata?.file?.path ?: "")
    data.putDouble("duration", (metadata?.duration ?: 0f).toDouble())
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onFinished"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
