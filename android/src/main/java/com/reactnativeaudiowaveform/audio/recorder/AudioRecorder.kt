package com.reactnativeaudiowaveform.audio.recorder

import android.media.MediaMetadataRetriever
import android.media.audiofx.NoiseSuppressor
import com.reactnativeaudiowaveform.Utils
import com.reactnativeaudiowaveform.audio.recorder.config.AudioRecorderConfig
import com.reactnativeaudiowaveform.audio.recorder.constants.LogConstants
import com.reactnativeaudiowaveform.audio.recorder.extensions.safeDispose
import com.reactnativeaudiowaveform.audio.recorder.extensions.subscribeIO
import com.reactnativeaudiowaveform.audio.recorder.listener.OnRecordStateChangeListener
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import com.reactnativeaudiowaveform.audio.recorder.model.RecordMetadata
import com.reactnativeaudiowaveform.audio.recorder.model.RecordState
import com.reactnativeaudiowaveform.audio.recorder.recorder.AudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.recorder.WavAudioRecorder
import com.reactnativeaudiowaveform.audio.recorder.recorder.finder.DefaultRecordFinder
import com.reactnativeaudiowaveform.audio.recorder.recorder.finder.RecordFinder
import com.reactnativeaudiowaveform.audio.recorder.source.AutomaticGainAudioSource
import com.reactnativeaudiowaveform.audio.recorder.source.DefaultAudioSource
import com.reactnativeaudiowaveform.audio.recorder.source.NoiseAudioSource
import com.reactnativeaudiowaveform.audio.recorder.writer.DefaultRecordWriter
import com.reactnativeaudiowaveform.audio.recorder.writer.NoiseRecordWriter
import com.reactnativeaudiowaveform.audio.recorder.writer.RecordWriter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Main-stream class for manage Record feature
 */
class AudioRecorder {
    private lateinit var recorderConfig: AudioRecorderConfig
    private lateinit var recordWriter: RecordWriter
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var timerDisposable: Disposable
    private lateinit var recordMetadata: RecordMetadata

    private var currentTimer: Long = 0
    private var recordStateChangeListener: OnRecordStateChangeListener? = null

    /**
     * create and assign [AudioRecorder]
     */
    fun create(config: AudioRecorderConfig.() -> Unit) {
        create(DefaultRecordFinder::class.java, config)
    }

    /**
     * create and assign [AudioRecorder]
     */
    fun create(recordFinder: Class<*>, config: AudioRecorderConfig.() -> Unit) {
        val audioRecorderConfig = AudioRecorderConfig()
        audioRecorderConfig.config()
        if (!audioRecorderConfig.check()) return

        this.recorderConfig = audioRecorderConfig

        findRecordWriter()
        findAudioRecorder(recordFinder)

        DebugState.state = audioRecorderConfig.debugMode
    }

    /**
     * get [AudioRecorder] object
     */
    fun getAudioRecorder() = audioRecorder

    /**
     * Start Recording
     */
    fun startRecording() {
        audioRecorder.startRecording()
        recordStateChangeListener?.onState(RecordState.START)

        startTimer()
    }

    /**
     * Stop recording
     */
    fun stopRecording() {
        if (!::timerDisposable.isInitialized) {
            return
        }

        audioRecorder.stopRecording()
        recordStateChangeListener?.onState(RecordState.STOP)

        stopTimer()
        if (recorderConfig.destFile != null) {
            retrieveFileMetadata(recorderConfig.destFile ?: File(""))
        }
    }

    /**
     * Pause recording
     */
    fun pauseRecording() {
        if (!::timerDisposable.isInitialized) {
            return
        }

        audioRecorder.pauseRecording()
        recordStateChangeListener?.onState(RecordState.PAUSE)
    }

    /**
     * Resume recording
     */
    fun resumeRecording() {
        if (!::timerDisposable.isInitialized) {
            return
        }

        audioRecorder.resumeRecording()
        recordStateChangeListener?.onState(RecordState.RESUME)
    }

    /**
     * set [OnRecordStateChangeListener] to handle state changes of [AudioRecorder]
     */
    fun setOnRecordStateChangeListener(listener: OnRecordStateChangeListener) {
        this.recordStateChangeListener = listener
    }

    /**
     * Kotlin-compatible version of [setOnRecordStateChangeListener]
     */
    fun setOnRecordStateChangeListener(callback: (RecordState) -> Unit) {
        this.recordStateChangeListener = object : OnRecordStateChangeListener {
            override fun onState(state: RecordState) {
                callback.invoke(state)
            }
        }
    }

    /**
     * get [RecordMetadata] which contains file and duration
     */
    fun getRecordMetadata(): RecordMetadata? {
        if (!::recordMetadata.isInitialized) {
            return null
        }

        return recordMetadata
    }

    /**
     * retrieve Metadata with given [file]
     */
    fun retrieveMetadata(file: File): RecordMetadata {
        return retrieveFileMetadata(file)
    }

    private fun findAudioRecorder(recordFinder: Class<*>) {
        try {
            val finder = recordFinder.getConstructor().newInstance() as? RecordFinder
                ?: throw IllegalArgumentException(LogConstants.EXCEPTION_FINDER_NOT_HAVE_EMPTY_CONSTRUCTOR)

            val destFile = recorderConfig.destFile ?: return // it can't be null
            var file = destFile
            if(recorderConfig.ffmpegMode) {
              file = File(destFile.parent, "${Utils.getNameWithoutExtension(destFile.name)}.wav")
            }
            audioRecorder = finder.find(destFile.extension, file, recordWriter)
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                LogConstants.EXCEPTION_FINDER_NOT_HAVE_EMPTY_CONSTRUCTOR,
                exception
            )
        }
    }

    private fun findRecordWriter() {
        val recordWriter = when (recorderConfig.audioSource) {
            is NoiseAudioSource -> NoiseRecordWriter(recorderConfig.audioSource)
            is AutomaticGainAudioSource -> if(NoiseSuppressor.isAvailable()) NoiseRecordWriter(recorderConfig.audioSource) else DefaultRecordWriter(recorderConfig.audioSource)
            is DefaultAudioSource -> DefaultRecordWriter(recorderConfig.audioSource)
            else -> null
        }

        if (recordWriter != null) this.recordWriter = recordWriter

        if (recordWriter is DefaultRecordWriter) {
            recordWriter.setOnChunkAvailableListener(recorderConfig.chunkAvailableListener)
        }

        if (recordWriter is NoiseRecordWriter) {
            recordWriter.setOnSilentDetectedListener(recorderConfig.silentDetectedListener)
        }
    }

    private fun startTimer() {
        if (!recorderConfig.checkAvailableTimer()) {
            DebugState.debug(LogConstants.TIMER_NOT_AVAILABLE)
            return
        }

        timerDisposable =
            Observable.interval(recorderConfig.refreshTimerMillis, TimeUnit.MILLISECONDS)
                .subscribeIO { data, _, _ ->

                    if (data == null) return@subscribeIO
                    if (recordWriter.getAudioSource().isRecordAvailable()) {
                        currentTimer += recorderConfig.refreshTimerMillis
                        recorderConfig.timerCountListener?.onTime(
                            currentTimer,
                            recorderConfig.maxAvailableMillis
                        )

                        if (recorderConfig.maxAvailableMillis != -1L && currentTimer >= recorderConfig.maxAvailableMillis) {
                            stopRecording()
                        }
                    }
                }
    }

    private fun stopTimer() {
        if (!recorderConfig.checkAvailableTimer()) {
            DebugState.debug(LogConstants.TIMER_NOT_AVAILABLE)
            return
        }

        timerDisposable.safeDispose()
    }

    private fun retrieveFileMetadata(file: File): RecordMetadata {
        if (audioRecorder !is WavAudioRecorder) {
            recordMetadata = RecordMetadata(file, 0)
            return recordMetadata
        }

        val metaRetriever = MediaMetadataRetriever()
        var duration: Long = 0
        try {
          metaRetriever.setDataSource(file.absolutePath)
          duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
          metaRetriever.release()
        } catch (e: RuntimeException) {
          DebugState.error(e.toString(), e)
        } catch (e: Exception) {
          DebugState.error(e.toString(), e)
        }
        recordMetadata = RecordMetadata(file, duration)
        return recordMetadata
    }
}
