package com.reactnativeaudiowaveform.audio.recorder.recorder.finder

import com.reactnativeaudiowaveform.audio.recorder.recorder.AudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.recorder.PcmAudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.recorder.WavAudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.writer.RecordWriter
import java.io.File

/**
 * Default settings of [RecordFinder]
 */
class DefaultRecordFinder : RecordFinder {

    /**
     * see [RecordFinder.find]
     */
    override fun find(extension: String, file: File, writer: RecordWriter): AudioRecorder {
        return when (extension) {
            "wav" -> WavAudioRecorder(file, writer)
            "pcm" -> PcmAudioRecorder(file, writer)
            else -> PcmAudioRecorder(file, writer)
        }
    }

}
