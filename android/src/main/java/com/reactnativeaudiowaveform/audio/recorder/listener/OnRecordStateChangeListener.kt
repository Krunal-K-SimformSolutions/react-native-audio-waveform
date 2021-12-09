package com.reactnativeaudiowaveform.audio.recorder.listener

import com.reactnativeaudiowaveform.audio.recorder.model.RecordState

/**
 * Listener for handling state changes
 */
interface OnRecordStateChangeListener {

    /**
     * Call when [RecordState] is changed
     */
    fun onState(state: RecordState)
}
