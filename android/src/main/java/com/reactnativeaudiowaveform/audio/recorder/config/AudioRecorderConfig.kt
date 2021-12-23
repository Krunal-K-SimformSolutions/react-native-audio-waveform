package com.reactnativeaudiowaveform.audio.recorder.config

import com.reactnativeaudiowaveform.audio.recorder.chunk.AudioChunk
import com.reactnativeaudiowaveform.audio.recorder.constants.LogConstants
import com.reactnativeaudiowaveform.audio.recorder.listener.OnChunkAvailableListener
import com.reactnativeaudiowaveform.audio.recorder.listener.OnSilentDetectedListener
import com.reactnativeaudiowaveform.audio.recorder.listener.OnTimerCountListener
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.audio.recorder.source.AudioSource
import com.reactnativeaudiowaveform.audio.recorder.source.DefaultAudioSource
import java.io.File

/**
 * Config file of [com.reactnativeaudiowaveform.audio.recorder.AudioRecorder]
 */
class AudioRecorderConfig {
    /**
     * Destination file of AudioRecorder, This field can't be null-state.
     */
    var destFile: File? = null

    /**
     * Config of [android.media.AudioRecord]
     */
    var recordConfig: AudioRecordConfig = AudioRecordConfig.defaultConfig()

    /**
     * [AudioSource] to use for recording audio. default is [DefaultAudioSource]
     */
    var audioSource: AudioSource = DefaultAudioSource(recordConfig)

    /**
     * [OnChunkAvailableListener] called when [com.reactnativeaudiowaveform.audio.recorder.chunk.AudioChunk] is available.
     */
    var chunkAvailableListener: OnChunkAvailableListener? = null

    /**
     * [OnSilentDetectedListener] called when time of silent is detected.
     *
     * Note, this options need to use [com.reactnativeaudiowaveform.audio.recorder.source.NoiseAudioSource]
     */
    var silentDetectedListener: OnSilentDetectedListener? = null

    /**
     * [OnTimerCountListener] called when timer is changed
     *
     * if [maxAvailableMillis] is provided, maxTime of [OnTimerCountListener.onTime] is available.
     * otherwise, it always return -1
     */
    var timerCountListener: OnTimerCountListener? = null

    /**
     * Max time (in Millis) of recording time
     */
    var maxAvailableMillis: Long = -1L

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
     * Kotlin-compatible version of [OnChunkAvailableListener]
     * if [OnChunkAvailableListener] is not provided and this value is provided, library will convert for you.
     */
    var chunkAvailableCallback: ((AudioChunk) -> Unit)? = null

    /**
     * Kotlin-compatible version of [OnSilentDetectedListener]
     * if [OnSilentDetectedListener] is not provided and this value is provided, library will convert for you.
     */
    var silentDetectedCallback: ((Long) -> Unit)? = null

    /**
     * Kotlin-compatible version of [OnTimerCountListener]
     * if [OnTimerCountListener] is not provided and this value is provided, library will convert for you.
     */
    var timerCountCallback: ((Long, Long) -> Unit)? = null

    /**
     * if this value is turn on, Debug log in library is printed in Logcat.
     */
    var debugMode: Boolean = false

    /**
     * check all necessary parameter is provided.
     */
    fun check(): Boolean {
        if (destFile == null) {
            throw NullPointerException(LogConstants.EXCEPTION_DEST_FILE_NOT_ASSIGNED)
        }

        // if ~callback is provided and listener isn't provided, assign that
        if (chunkAvailableListener == null && chunkAvailableCallback != null) {
            chunkAvailableListener = object : OnChunkAvailableListener {
                override fun onChunkAvailable(audioChunk: AudioChunk) {
                    chunkAvailableCallback?.invoke(audioChunk)
                }
            }
        }

        if (silentDetectedListener == null && silentDetectedCallback != null) {
            silentDetectedListener = object : OnSilentDetectedListener {
                override fun onSilence(silenceTime: Long) {
                    silentDetectedCallback?.invoke(silenceTime)
                }
            }
        }

        if (timerCountListener == null && timerCountCallback != null) {
            timerCountListener = object : OnTimerCountListener {
                override fun onTime(currentTime: Long, maxTime: Long) {
                    timerCountCallback?.invoke(currentTime, maxTime)
                }
            }
        }

        if (destFile?.exists() == false) {
            DebugState.debug("The file does not appear to exist at the destination. " +
                    "create parent directory. (file is generate automatically)")
            destFile?.parentFile?.mkdirs()
        }

        return true
    }

    fun checkAvailableTimer() = timerCountListener != null || maxAvailableMillis != -1L || refreshTimerMillis != 50L
}
