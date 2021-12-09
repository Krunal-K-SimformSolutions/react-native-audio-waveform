package com.reactnativeaudiowaveform.audio.player.listener

import com.reactnativeaudiowaveform.audio.player.model.PlayState

/**
 * Listener for handling state changes
 */
interface OnPlayStateChangeListener {

    /**
     * Call when [PlayState] is changed
     */
    fun onState(state: PlayState)
}
