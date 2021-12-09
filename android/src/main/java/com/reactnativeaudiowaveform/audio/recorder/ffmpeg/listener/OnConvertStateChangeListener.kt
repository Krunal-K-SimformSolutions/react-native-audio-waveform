package com.reactnativeaudiowaveform.audio.recorder.ffmpeg.listener

import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegConvertState

/**
 * Listener for handling state changes
 */
interface OnConvertStateChangeListener {

    /**
     * Call when [FFmpegConvertState] is changed
     */
    fun onState(state: FFmpegConvertState)
}
