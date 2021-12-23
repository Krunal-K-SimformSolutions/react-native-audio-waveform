package com.reactnativeaudiowaveform

import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import androidx.annotation.Nullable
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableArray
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegBitRate
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegSamplingRate
import kotlin.reflect.KClass

object Utils {
  fun toWritableArray(@NonNull array: List<Int>): WritableArray {
    val writableArray = Arguments.createArray()
    for (i in array.indices) {
      val value = array[i]
        writableArray.pushInt(value)
    }
    return writableArray
  }

  fun encodeToString(@NonNull byteArray: ByteArray): String {
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
  }

  fun encodeToString(@NonNull byteArrayString: String): ByteArray {
    return Base64.decode(byteArrayString, Base64.NO_WRAP)
  }

  fun getNameWithoutExtension(fileName: String): String {
    val dotIndex = fileName.lastIndexOf(".")
    return if(dotIndex == -1) fileName else fileName.substring(0, dotIndex)
  }

  fun <T: Any> getArgsValue(@Nullable args: ReadableArray?, index: Int, defaultValue: T?, clazz: KClass<T>) : T? {
    return if(args == null || args.isNull(index)) {
      defaultValue
    } else {
      @Suppress("UNCHECKED_CAST")
      return when (clazz) {
        Boolean::class -> args.getBoolean(index) as? T
        Int::class -> args.getInt(index) as? T
        String::class -> args.getString(index) as? T

        java.lang.Boolean::class -> args.getBoolean(index) as? T
        java.lang.Integer::class -> args.getInt(index)  as? T
        java.lang.String::class -> args.getString(index) as? T

        else -> defaultValue
      }
    }
  }

  fun getAudioSource(@Nullable value: Int?): Int {
    return when(value) {
      0 -> MediaRecorder.AudioSource.DEFAULT
      1 -> MediaRecorder.AudioSource.MIC
      2 -> MediaRecorder.AudioSource.VOICE_UPLINK
      3 -> MediaRecorder.AudioSource.VOICE_DOWNLINK
      4 -> MediaRecorder.AudioSource.VOICE_CALL
      5 -> MediaRecorder.AudioSource.CAMCORDER
      6 -> MediaRecorder.AudioSource.VOICE_RECOGNITION
      7 -> MediaRecorder.AudioSource.VOICE_COMMUNICATION
      8 -> MediaRecorder.AudioSource.REMOTE_SUBMIX
      9 -> MediaRecorder.AudioSource.UNPROCESSED
      10 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaRecorder.AudioSource.VOICE_PERFORMANCE
      } else {
        throw Exception("Required grater than or Equal to Q Api")
      }
      else -> MediaRecorder.AudioSource.MIC
    }
  }

  fun getAudioEncoding(@Nullable value: Int?): Int {
    return when(value) {
      0 -> AudioFormat.ENCODING_INVALID
      1 -> AudioFormat.ENCODING_DEFAULT
      2 -> AudioFormat.ENCODING_PCM_16BIT
      3 -> AudioFormat.ENCODING_PCM_8BIT
      4 -> AudioFormat.ENCODING_PCM_FLOAT
      5 -> AudioFormat.ENCODING_AC3
      6 -> AudioFormat.ENCODING_E_AC3
      7 -> AudioFormat.ENCODING_DTS
      8 -> AudioFormat.ENCODING_DTS_HD
      in 9..12 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getAudioEncodingQAndAbove(value)
      } else {
        throw Exception("Required grater than or Equal to Q Api")
      }
      13 -> AudioFormat.ENCODING_IEC61937
      in 14..19 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getAudioEncodingQAndAbove(value)
      } else {
        throw Exception("Required grater than or Equal to Q Api")
      }
      else -> AudioFormat.ENCODING_PCM_16BIT
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  fun getAudioEncodingQAndAbove(@Nullable value: Int?): Int {
    return when(value) {
      9 -> AudioFormat.ENCODING_MP3
      10 -> AudioFormat.ENCODING_AAC_LC
      11 -> AudioFormat.ENCODING_AAC_HE_V1
      12 -> AudioFormat.ENCODING_AAC_HE_V2
      14 -> AudioFormat.ENCODING_DOLBY_TRUEHD
      15 -> AudioFormat.ENCODING_AAC_ELD
      16 -> AudioFormat.ENCODING_AAC_XHE
      17 -> AudioFormat.ENCODING_AC4
      18 -> AudioFormat.ENCODING_E_AC3_JOC
      19 -> AudioFormat.ENCODING_DOLBY_MAT
      else -> AudioFormat.ENCODING_PCM_16BIT
    }
  }

  fun getFFmpegBitRate(@Nullable value: Int?): FFmpegBitRate {
    return when(value) {
      0 -> FFmpegBitRate.DEF
      1 -> FFmpegBitRate.U8
      2 -> FFmpegBitRate.S16
      3 -> FFmpegBitRate.S32
      4 -> FFmpegBitRate.FLT
      5 -> FFmpegBitRate.DBL
      6 -> FFmpegBitRate.U8P
      7 -> FFmpegBitRate.S16P
      8 -> FFmpegBitRate.S32P
      9 -> FFmpegBitRate.FLTP
      10 -> FFmpegBitRate.DBLP
      11 -> FFmpegBitRate.S64
      12 -> FFmpegBitRate.S64P
      else -> FFmpegBitRate.DEF
    }
  }

  fun getFFmpegSamplingRate(@Nullable value: Int?): FFmpegSamplingRate {
    return when(value) {
      0 -> FFmpegSamplingRate.ORIGINAL
      1 -> FFmpegSamplingRate.ENCODING_IN_8000
      2 -> FFmpegSamplingRate.ENCODING_IN_11025
      3 -> FFmpegSamplingRate.ENCODING_IN_16000
      4 -> FFmpegSamplingRate.ENCODING_IN_22050
      5 -> FFmpegSamplingRate.ENCODING_IN_32000
      6 -> FFmpegSamplingRate.ENCODING_IN_44100
      7 -> FFmpegSamplingRate.ENCODING_IN_48000
      8 -> FFmpegSamplingRate.ENCODING_IN_88200
      9 -> FFmpegSamplingRate.ENCODING_IN_96000
      10 -> FFmpegSamplingRate.ENCODING_IN_76400
      11 -> FFmpegSamplingRate.ENCODING_IN_192000
      12 -> FFmpegSamplingRate.ENCODING_IN_352800
      13 -> FFmpegSamplingRate.ENCODING_IN_384000
      else -> FFmpegSamplingRate.ORIGINAL
    }
  }
}
