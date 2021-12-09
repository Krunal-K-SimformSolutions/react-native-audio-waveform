package com.reactnativeaudiowaveform.audio.recorder.recorder

import com.reactnativeaudiowaveform.audio.recorder.extensions.ignoreException
import com.reactnativeaudiowaveform.audio.recorder.recorder.wav.WavHeader
import com.reactnativeaudiowaveform.audio.recorder.writer.RecordWriter
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 * [AudioRecorder] for record audio and save in wav file
 */
open class WavAudioRecorder(file: File, recordWriter: RecordWriter) : DefaultAudioRecorder(file, recordWriter) {
    override fun stopRecording() {
        super.stopRecording()
        writeWavHeader()
    }

    @Throws(IOException::class)
    private fun writeWavHeader() {
        val wavFile = randomAccessFile(file)
        wavFile?.let {
            it.seek(0)
            it.write(WavHeader(recordWriter.getAudioSource(), file.length()).getWavFileHeaderByteArray())
            it.close()
        }
    }

    private fun randomAccessFile(file: File): RandomAccessFile? = ignoreException { RandomAccessFile(file, "rw") }
}
