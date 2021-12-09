package com.reactnativeaudiowaveform.audio.player.listener

/**
 * Listener for handling timer
 */
interface OnTimerCountListener {

    /**
     * Invoke when every [com.reactnativeaudiowaveform.audio.recorder.config.AudioRecorderConfig.refreshTimerMillis] is passed
     */
    fun onTime(currentTime: Long = 0, playing: Boolean = false)
}
