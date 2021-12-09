package com.reactnativeaudiowaveform.audio.recorder.listener

/**
 * Listener for handling Silent event of recording while
 * using [com.reactnativeaudiowaveform.audio.recorder.source.NoiseAudioSource]
 */
interface OnSilentDetectedListener {

    /**
     * Invoke when silence measured.
     */
    fun onSilence(silenceTime: Long)
}
