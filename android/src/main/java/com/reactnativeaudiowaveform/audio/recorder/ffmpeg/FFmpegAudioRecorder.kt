package com.reactnativeaudiowaveform.audio.recorder.ffmpeg

import android.content.Context
import com.arthenica.mobileffmpeg.FFmpeg
import com.reactnativeaudiowaveform.Utils
import com.reactnativeaudiowaveform.audio.recorder.extensions.runOnUiThread
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.config.FFmpegConvertConfig
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.extensions.weak
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.listener.OnConvertStateChangeListener
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegBitRate
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegConvertState
import com.reactnativeaudiowaveform.audio.recorder.ffmpeg.model.FFmpegSamplingRate
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.audio.recorder.recorder.WavAudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.writer.RecordWriter
import java.io.File


/**
 * [com.reactnativeaudiowaveform.audio.recorder.recorder.AudioRecorder] for record audio and save in wav and convert them by FFmpeg.
 */
open class FFmpegAudioRecorder(val extension: String, file: File, recordWriter: RecordWriter) : WavAudioRecorder(file, recordWriter) {
    private var _context: Context? by weak(null)
    private var convertStateChangeListener: OnConvertStateChangeListener? = null
    private var convertConfig: FFmpegConvertConfig = FFmpegConvertConfig.defaultConfig()

    override fun stopRecording() {
        super.stopRecording()
        convert()
    }

    /**
     * set [Context] to [FFmpegAudioRecorder]
     */
    fun setContext(context: Context) {
        this._context = context
    }

    /**
     * set [OnConvertStateChangeListener] to handle convert state of FFmpeg
     */
    fun setOnConvertStateChangeListener(listener: OnConvertStateChangeListener) {
        this.convertStateChangeListener = listener
    }

    /**
     * Kotlin-compatible version of [setOnConvertStateChangeListener]
     */
    fun setOnConvertStateChangeListener(callback: (FFmpegConvertState) -> Unit) {
        this.convertStateChangeListener = object : OnConvertStateChangeListener {
            override fun onState(state: FFmpegConvertState) {
                callback.invoke(state)
            }
        }
    }

    /**
     * set [FFmpegConvertConfig] to change encoding options of FFmpeg
     */
    fun setConvertConfig(convertConfig: FFmpegConvertConfig) {
        this.convertConfig = convertConfig
    }

    private fun convert() {
        val commandBuilder = mutableListOf<String>()

        //val destFile = File(file.absolutePath)
        val tempFile = File(file.parent, "${Utils.getNameWithoutExtension(file.name)}.${extension}")

        commandBuilder.addAll(listOf("-y", "-i", file.path))

        if (convertConfig.samplingRate != FFmpegSamplingRate.ORIGINAL) {
            commandBuilder.addAll(listOf("-ar", convertConfig.samplingRate.samplingRate.toString()))
        }

        if (convertConfig.bitRate !== FFmpegBitRate.DEF) {
            commandBuilder.addAll(listOf("-sample_fmt", convertConfig.bitRate.bitRate))
        }

        if (convertConfig.mono) {
            commandBuilder.addAll(listOf("-ac", "1"))
        }
        commandBuilder.add(tempFile.path)

        val cmd = commandBuilder.toTypedArray()
        DebugState.error(cmd.contentToString())

        try {
            // START
            runOnUiThread { convertStateChangeListener?.onState(FFmpegConvertState.START) }
          when(FFmpeg.execute(cmd)) {
                0 -> {
                    // SUCCESS
                    file.delete()
                    //tempFile.renameTo(destFile)
                    runOnUiThread { convertStateChangeListener?.onState(FFmpegConvertState.SUCCESS) }
                }
                255 -> {
                    // CANCEL
                    runOnUiThread { convertStateChangeListener?.onState(FFmpegConvertState.CANCEL) }
                }
                else -> {
                    // FAILURE
                    runOnUiThread { convertStateChangeListener?.onState(FFmpegConvertState.ERROR) }
                }
            }
        } catch (ex: Exception) {
            DebugState.error(ex.message ?: "", ex)
            runOnUiThread { convertStateChangeListener?.onState(FFmpegConvertState.ERROR) }
        } finally {
            // FINISH
            runOnUiThread { convertStateChangeListener?.onState(FFmpegConvertState.FINISH) }
        }
    }
}
