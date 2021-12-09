package com.reactnativeaudiowaveform.audio.recorder.writer

import android.media.AudioRecord
import com.reactnativeaudiowaveform.audio.recorder.chunk.AudioChunk
import com.reactnativeaudiowaveform.audio.recorder.chunk.ShortArrayAudioChunk
import com.reactnativeaudiowaveform.audio.recorder.constants.AudioConstants
import com.reactnativeaudiowaveform.audio.recorder.extensions.checkChunkAvailable
import com.reactnativeaudiowaveform.audio.recorder.extensions.runOnUiThread
import com.reactnativeaudiowaveform.audio.recorder.listener.OnSilentDetectedListener
import com.reactnativeaudiowaveform.audio.recorder.source.AudioSource
import com.reactnativeaudiowaveform.audio.recorder.source.DefaultAudioSource
import java.io.OutputStream

/**
 * Default settings + NoiseSuppressor of [RecordWriter]
 */
open class NoiseRecordWriter(audioSource: AudioSource = DefaultAudioSource()) : DefaultRecordWriter(audioSource) {
    private var firstSilenceMoment: Long = 0
    private var noiseRecordedAfterFirstSilenceThreshold = 0
    private var silentDetectedListener: OnSilentDetectedListener? = null

    /**
     * see [DefaultRecordWriter.write]
     */
    override fun write(audioRecord: AudioRecord, bufferSize: Int, outputStream: OutputStream) {
        val audioChunk = ShortArrayAudioChunk(ShortArray(bufferSize))
        while (getAudioSource().isRecordAvailable()) {
            val shorts = audioChunk.shorts
            audioChunk.setReadCount(audioRecord.read(shorts, 0, shorts.size))
            if (!audioChunk.checkChunkAvailable()) continue

            runOnUiThread { chunkAvailableListener?.onChunkAvailable(audioChunk) }

            if (audioChunk.findFirstIndex() > -1) {
                outputStream.write(audioChunk.toByteArray())
                firstSilenceMoment = 0
                noiseRecordedAfterFirstSilenceThreshold++
            } else {
                processSilentTime(audioChunk, outputStream)
            }
        }
    }

    /**
     * set [OnSilentDetectedListener] to get silent time
     */
    fun setOnSilentDetectedListener(listener: OnSilentDetectedListener?) =
            this.apply { silentDetectedListener = listener }

    private fun processSilentTime(audioChunk: AudioChunk, outputStream: OutputStream) {
        if (firstSilenceMoment == 0L) firstSilenceMoment = System.currentTimeMillis()
        val silentTime = System.currentTimeMillis() - firstSilenceMoment
        if (firstSilenceMoment != 0L && silentTime > AudioConstants.SILENCE_THRESHOLD) {
            if (silentTime > 1000L && noiseRecordedAfterFirstSilenceThreshold >= 3) {
                noiseRecordedAfterFirstSilenceThreshold = 0
                runOnUiThread { silentDetectedListener?.onSilence(silentTime) }
            }
        } else {
            outputStream.write(audioChunk.toByteArray())
        }
    }
}
