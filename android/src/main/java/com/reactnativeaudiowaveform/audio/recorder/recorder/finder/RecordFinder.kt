package com.reactnativeaudiowaveform.audio.recorder.recorder.finder

import com.reactnativeaudiowaveform.audio.recorder.recorder.AudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.writer.RecordWriter
import java.io.File

/**
 * find proper [AudioRecorder] class which condition
 */
interface RecordFinder {

    /**
     * find [AudioRecorder] with given [extension]
     */
    fun find(extension: String, file: File, writer: RecordWriter): AudioRecorder
}
