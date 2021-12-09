package com.reactnativeaudiowaveform.event.player

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

import com.facebook.react.uimanager.events.RCTEventEmitter
import com.reactnativeaudiowaveform.audio.player.model.PlayState

class OnPlayerStateEvent(viewTag: Int, private val playState: PlayState) : Event<OnPlayerStateEvent>(viewTag) {
  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data: WritableMap = Arguments.createMap()
    data.putString("playState", playState.name)
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }

  companion object {
    const val EVENT_NAME = "onPlayerState"
  }

  override fun getEventName(): String {
    return EVENT_NAME
  }
}
