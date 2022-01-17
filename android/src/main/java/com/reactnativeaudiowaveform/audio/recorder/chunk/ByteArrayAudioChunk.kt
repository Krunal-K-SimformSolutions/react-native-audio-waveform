package com.reactnativeaudiowaveform.audio.recorder.chunk

import com.reactnativeaudiowaveform.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * [AudioChunk] which handle [ByteArray]
 */
class ByteArrayAudioChunk(var bytes: ByteArray) : AudioChunk {
    private var _readCount: Int = 0

  /**
   * see [AudioChunk.getMaxAmplitude]
   */
  override fun getMaxAmplitude(): Int {
    val maxAmplitude = toShortArray().maxOrNull()?.toInt() ?: 0
    return Utils.amplitudeMagnify(maxAmplitude)
  }

    /**
     * see [AudioChunk.toByteArray]
     */
    override fun toByteArray(): ByteArray = bytes

    /**
     * see [AudioChunk.toShortArray]
     */
    override fun toShortArray(): ShortArray {
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    /**
     * see [AudioChunk.getReadCount]
     */
    override fun getReadCount(): Int = _readCount

    /**
     * see [AudioChunk.setReadCount]
     */
    override fun setReadCount(readCount: Int) = this.apply { _readCount = readCount }
}
