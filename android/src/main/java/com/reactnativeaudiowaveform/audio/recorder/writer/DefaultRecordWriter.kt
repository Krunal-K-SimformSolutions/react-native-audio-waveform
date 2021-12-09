package com.reactnativeaudiowaveform.audio.recorder.writer

import android.media.AudioRecord
import com.reactnativeaudiowaveform.audio.recorder.chunk.AudioChunk
import com.reactnativeaudiowaveform.audio.recorder.chunk.ByteArrayAudioChunk
import com.reactnativeaudiowaveform.audio.recorder.extensions.checkChunkAvailable
import com.reactnativeaudiowaveform.audio.recorder.extensions.runOnUiThread
import com.reactnativeaudiowaveform.audio.recorder.listener.OnChunkAvailableListener
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.audio.recorder.source.AudioSource
import com.reactnativeaudiowaveform.audio.recorder.source.DefaultAudioSource
import java.io.OutputStream

/**
 * Default settings of [RecordWriter]
 *
 * @param audioSource
 */
open class DefaultRecordWriter(private val audioSource: AudioSource = DefaultAudioSource()) : RecordWriter {
    protected var chunkAvailableListener: OnChunkAvailableListener? = null
    private var confirmStart: Boolean = false

    /**
     * see [RecordWriter.startRecording]
     */
    override fun startRecording(outputStream: OutputStream) {
        DebugState.debug("Reqeust accepted, startRecording()")
        write(getAudioSource().preProcessAudioRecord(), getAudioSource().getBufferSize(), outputStream)
    }

    /**
     * see [RecordWriter.stopRecording]
     */
    override fun stopRecording() {
        if (!confirmStart) {
            DebugState.error("stop() called on an uninitialized AudioRecord.")
            return
        }

        getAudioSource().getAudioRecord().stop()
        getAudioSource().getAudioRecord().release()
        confirmStart = false
    }

    /**
     * see [RecordWriter.getAudioSource]
     */
    override fun getAudioSource(): AudioSource = audioSource

    /**
     * Read from [AudioRecord] and write to [OutputStream]
     */
    open fun write(audioRecord: AudioRecord, bufferSize: Int, outputStream: OutputStream) {
        val audioChunk = ByteArrayAudioChunk(ByteArray(bufferSize))
        DebugState.debug("read and write... available: ${audioSource.isRecordAvailable()}")
        while (audioSource.isRecordAvailable()) {
            if (!confirmStart) confirmStart = true

            audioChunk.setReadCount(audioRecord.read(audioChunk.bytes, 0, bufferSize))
            if (!audioChunk.checkChunkAvailable()) continue

            runOnUiThread { chunkAvailableListener?.onChunkAvailable(audioChunk) }
            outputStream.write(audioChunk.bytes)
        }
    }

    /**
     * set [OnChunkAvailableListener] to get [AudioChunk]
     */
    fun setOnChunkAvailableListener(listener: OnChunkAvailableListener?) =
        this.apply { this.chunkAvailableListener = listener }
}
