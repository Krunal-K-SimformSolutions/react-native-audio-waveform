package com.reactnativeaudiowaveform.audio.recorder.ffmpeg.config

import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegBitRate
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegSamplingRate

/**
 * Data class for convert config in FFmpeg
 *
 * @param bitRate Bitrate settings
 * @param samplingRate Sampling rate settings
 * @param mono true if channel is mono
 */
data class FFmpegConvertConfig(val bitRate: FFmpegBitRate, val samplingRate: FFmpegSamplingRate, val mono: Boolean) {

    companion object {
        @JvmStatic
        fun defaultConfig() = FFmpegConvertConfig(FFmpegBitRate.DEF, FFmpegSamplingRate.ORIGINAL, true)
    }
}
