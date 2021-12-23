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
            "aac" -> FFmpegAudioRecorder(extension, file, writer)
            "mp3" -> FFmpegAudioRecorder(extension, file, writer)
            "m4a" -> FFmpegAudioRecorder(extension, file, writer)
            "wma" -> FFmpegAudioRecorder(extension, file, writer)
            "flac" -> FFmpegAudioRecorder(extension, file, writer)
            "mp4" -> FFmpegAudioRecorder(extension, file, writer)
            else -> PcmAudioRecorder(file, writer)
        }
    }

}
