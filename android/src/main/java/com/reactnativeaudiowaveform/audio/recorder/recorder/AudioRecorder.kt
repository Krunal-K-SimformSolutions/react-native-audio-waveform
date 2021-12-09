package com.reactnativeaudiowaveform.audio.recorder.recorder

/**
 * Recorder class for manage Recording time
 */
interface AudioRecorder {

    /**
     * start Recording
     */
    fun startRecording()

    /**
     * resume Recording
     */
    fun resumeRecording()

    /**
     * pause Recording
     */
    fun pauseRecording()

    /**
     * stop Recording
     */
    fun stopRecording()
}
