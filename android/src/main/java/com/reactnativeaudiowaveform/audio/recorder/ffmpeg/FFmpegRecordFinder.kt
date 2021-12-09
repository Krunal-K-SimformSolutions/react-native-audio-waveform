package com.reactnativeaudiowaveform.audio.recorder.ffmpeg

import com.reactnativeaudiowaveform.audio.recorder.recorder.AudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.recorder.PcmAudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.recorder.WavAudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.recorder.finder.RecordFinder
import com.reactnativeaudiowaveform.audio.recorder.writer.RecordWriter
import java.io.File

/**
 * Default + FFmpeg settings of [RecordFinder]
 */
class FFmpegRecordFinder : RecordFinder {

    /**
     * see [RecordFinder.find]
     */
    override fun find(extension: String, file: File, writer: RecordWriter): AudioRecorder {
        return when (extension) {
            "wav" -> WavAudioRecorder(file, writer)
            "pcm" -> PcmAudioRecorder(file, writer)
            "aac" -> FFmpegAudioRecorder(file, writer)
            "mp3" -> FFmpegAudioRecorder(file, writer)
            "m4a" -> FFmpegAudioRecorder(file, writer)
            "wma" -> FFmpegAudioRecorder(file, writer)
            "flac" -> FFmpegAudioRecorder(file, writer)
            "mp4" -> FFmpegAudioRecorder(file, writer)
            else -> PcmAudioRecorder(file, writer)
        }
    }

}
