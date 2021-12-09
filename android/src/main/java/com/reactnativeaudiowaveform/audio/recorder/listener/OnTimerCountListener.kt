package com.reactnativeaudiowaveform.audio.recorder.listener

/**
 * Listener for handling timer
 */
interface OnTimerCountListener {

    /**
     * Invoke when every [com.reactnativeaudiowaveform.audio.recorder.config.AudioRecorderConfig.refreshTimerMillis] is passed
     */
    fun onTime(currentTime: Long = 0, maxTime: Long = 0)
}
