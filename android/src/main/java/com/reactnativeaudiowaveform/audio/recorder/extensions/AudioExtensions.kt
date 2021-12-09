package com.reactnativeaudiowaveform.audio.recorder.extensions

import android.media.AudioRecord
import com.reactnativeaudiowaveform.audio.recorder.chunk.AudioChunk

/**
 * internal extension of check [AudioChunk] has available size
 */
internal fun AudioChunk.checkChunkAvailable() = this.getReadCount() != AudioRecord.ERROR_BAD_VALUE &&
        this.getReadCount() != AudioRecord.ERROR_INVALID_OPERATION
