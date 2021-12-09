package com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model

/**
 * Indicate state of conversion of FFmpeg
 */
enum class FFmpegConvertState {
    START,
    SUCCESS,
    ERROR,
    CANCEL,
    FINISH
}
