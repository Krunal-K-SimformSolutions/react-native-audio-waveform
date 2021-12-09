package com.reactnativeaudiowaveform.event.recorder

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter
import com.reactnativeaudiowaveform.Utils
import com.reactnativeaudiowaveform.audio.recorder.chunk.AudioChunk

class OnBufferEvent(viewTag: Int, private val audioChunk: AudioChunk) : Event<OnBufferEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putInt("maxAmplitude", audioChunk.getMaxAmplitude())
    data.putString("bufferData", Utils.encodeToString(audioChunk.toByteArray()))
    data.putInt("readCount", audioChunk.getReadCount())
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onBuffer"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
