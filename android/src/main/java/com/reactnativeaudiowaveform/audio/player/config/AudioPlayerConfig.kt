package com.reactnativeaudiowaveform.audio.player.config

import com.reactnativeaudiowaveform.audio.player.listener.OnTimerCountListener
import com.reactnativeaudiowaveform.audio.recorder.constants.LogConstants
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import java.io.File

/**
 * Config file of [com.reactnativeaudiowaveform.audio.player.AudioPlayer]
 */
class AudioPlayerConfig {
    /**
     * Source file of AudioPlayer, This field can't be null-state.
     */
    var sourceFile: File? = null

    /**
     * [OnTimerCountListener] called when timer is changed
     */
    var timerCountListener: OnTimerCountListener? = null

    /**
     * Refresh time of timer.
     *
     * Every setting value (default = 50) is passed when recording is start, [timerCountListener] is called.
     * when pause recording using [com.reactnativeaudiowaveform.audio.recorder.recorder.AudioRecorder.pauseRecording],
     * it will not adding value.
     *
     * see [timerCountListener] for additional information.
     */
    var refreshTimerMillis: Long = 50

    /**
     * Kotlin-compatible version of [OnTimerCountListener]
     * if [OnTimerCountListener] is not provided and this value is provided, library will convert for you.
     */
    var timerCountCallback: ((Long, Boolean) -> Unit)? = null

    /**
     * if this value is turn on, Debug log in library is printed in Logcat.
     */
    var debugMode: Boolean = false

    /**
     * check all necessary parameter is provided.
     */
    fun check(): Boolean {
        if (sourceFile == null) {
            throw NullPointerException(LogConstants.EXCEPTION_DEST_FILE_NOT_ASSIGNED)
        }

        if (timerCountListener == null && timerCountCallback != null) {
            timerCountListener = object : OnTimerCountListener {
                override fun onTime(currentTime: Long, playing: Boolean) {
                    timerCountCallback?.invoke(currentTime, playing)
                }
            }
        }

        if (sourceFile?.exists() == false) {
            DebugState.debug("The file does not appear to exist at the destination. " +
                    "create parent directory. (file is generate automatically)")
            sourceFile?.parentFile?.mkdirs()
        }

        return true
    }

    fun checkAvailableTimer() = timerCountListener != null || refreshTimerMillis != 50L
}
