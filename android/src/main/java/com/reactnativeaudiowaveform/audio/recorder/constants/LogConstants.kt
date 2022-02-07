package com.reactnativeaudiowaveform.audio.recorder.constants

/**
 * Constants for define exception log
 */
object LogConstants {
    const val TAG = "WaveForms"

    const val EXCEPTION_NOT_SUPPORTED_NOISE_SUPPRESSOR =
            "This device doesn't support NoiseSuppressor. Try again with DefaultAudioSource"
    const val EXCEPTION_INITIAL_FAILED_NOISE_SUPPESSOR =
            "Initialization process of NoiseSuppressor is failed."

    const val EXCEPTION_DEST_FILE_NOT_ASSIGNED = "File isn't provided."
    const val EXCEPTION_FINDER_NOT_HAVE_EMPTY_CONSTRUCTOR =
            "All RecordFinder class need empty constructor to find AudioRecorder."

    const val TIMER_NOT_AVAILABLE = "Timer feature will ignored."
}
