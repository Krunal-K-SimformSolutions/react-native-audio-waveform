package com.reactnativeaudiowaveform.audio.recorder.model

import java.io.File

/**
 * AudioRecorder
 * Class: RecordMetadata
 * Description:
 */

data class RecordMetadata(val file: File, val duration: Long = 0)
