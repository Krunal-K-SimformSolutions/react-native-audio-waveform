package com.reactnativeaudiowaveform.audio.recorder.source

import android.media.AudioRecord
import android.media.audiofx.NoiseSuppressor
import com.reactnativeaudiowaveform.audio.recorder.config.AudioRecordConfig
import com.reactnativeaudiowaveform.audio.recorder.constants.LogConstants
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState

/**
 * Default setting + Automatic Gain NoiseSuppressor for [AudioRecord]
 */
class AutomaticGainAudioSource(audioRecordConfig: AudioRecordConfig = AudioRecordConfig.defaultConfig())
    : DefaultAudioSource(audioRecordConfig) {

    override fun preProcessAudioRecord(): AudioRecord {
        if(NoiseSuppressor.isAvailable()) {
            val noiseSuppressor = NoiseSuppressor.create(getAudioRecord().audioSessionId)
            if (noiseSuppressor != null) {
                noiseSuppressor.enabled = true
            } else {
                DebugState.error(LogConstants.EXCEPTION_INITIAL_FAILED_NOISE_SUPPESSOR)
            }
        }

        return super.preProcessAudioRecord()
    }
}
