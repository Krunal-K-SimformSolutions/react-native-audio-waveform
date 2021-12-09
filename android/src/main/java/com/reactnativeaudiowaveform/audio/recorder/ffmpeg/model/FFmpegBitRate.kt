package com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model

import java.util.*

/**
 * FFmpegBitRate for convert by FFmpeg
 */
enum class FFmpegBitRate {
    DEF,
    U8, //8 – unsigned 8 bits
    S16, //16 – signed 16 bits
    S32, // 32 – signed 32 bits (also used for 24-bit audio)
    FLT, //32 – float
    DBL, //64 – double
    U8P, //8 – unsigned 8 bits, planar
    S16P, //16 – signed 16 bits, planar
    S32P, //32 – signed 32 bits, planar
    FLTP, //32 – float, planar
    DBLP, //64 – double, planar
    S64, //64 – signed 64 bits
    S64P; //64 – signed 64 bits, planar

    val bitRate: String
        get() = name.lowercase(Locale.getDefault())
}
