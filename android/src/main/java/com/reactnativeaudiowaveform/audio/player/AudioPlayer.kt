package com.reactnativeaudiowaveform.audio.player

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.annotation.FloatRange
import androidx.core.net.toUri
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.reactnativeaudiowaveform.Utils
import com.reactnativeaudiowaveform.audio.player.config.AudioPlayerConfig
import com.reactnativeaudiowaveform.audio.player.extensions.recordFile
import com.reactnativeaudiowaveform.audio.player.extensions.toMediaSource
import com.reactnativeaudiowaveform.audio.player.extensions.toMediaSourceFactory
import com.reactnativeaudiowaveform.audio.player.listener.OnPlayStateChangeListener
import com.reactnativeaudiowaveform.audio.player.model.PlayState
import com.reactnativeaudiowaveform.audio.player.player.SoundFile
import com.reactnativeaudiowaveform.audio.recorder.constants.LogConstants
import com.reactnativeaudiowaveform.audio.recorder.extensions.safeDispose
import com.reactnativeaudiowaveform.audio.recorder.extensions.subscribeMain
import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import linc.com.amplituda.Amplituda
import linc.com.amplituda.AmplitudaProgressListener
import linc.com.amplituda.Compress
import linc.com.amplituda.ProgressOperation
import linc.com.amplituda.exceptions.AmplitudaException
import java.io.*
import java.net.URL
import java.util.concurrent.TimeUnit

class AudioPlayer : Player.Listener {
    private lateinit var audioPlayer: ExoPlayer
    private lateinit var timerDisposable: Disposable
    private lateinit var playerConfig: AudioPlayerConfig

    private var playStateChangeListener: OnPlayStateChangeListener? = null

    /**
     * create and assign [AudioPlayer]
     */
    fun create(context: Context, config: AudioPlayerConfig.() -> Unit) {
        val audioPlayerConfig = AudioPlayerConfig()
        audioPlayerConfig.config()
        if (!audioPlayerConfig.check()) return

        this.playerConfig = audioPlayerConfig

        if (::audioPlayer.isInitialized) {
            audioPlayer.release()
        }

        audioPlayer = ExoPlayer.Builder(context)
          .setMediaSourceFactory(context.toMediaSourceFactory())
          .build()
        audioPlayer.setMediaSource(audioPlayerConfig.sourceFile!!.toMediaSource(context))
        audioPlayer.prepare()
        audioPlayer.addListener(this@AudioPlayer)

        DebugState.state = audioPlayerConfig.debugMode
      DebugState.debug("sourceFile -> ${audioPlayerConfig.sourceFile!!.toUri()}")
    }

    /**
     * get [AudioPlayer] object
     */
    fun getAudioPlayer() = audioPlayer

    /**
     * Start Recording
     */
    fun startPlaying() {
        if (!::audioPlayer.isInitialized) {
            return
        }

        audioPlayer.play()
        playStateChangeListener?.onState(PlayState.START)
        updateProgress()
    }

    /**
     * Stop playing
     */
    fun stopPlaying() {
        if (!::audioPlayer.isInitialized) {
            return
        }

        audioPlayer.stop()
        playStateChangeListener?.onState(PlayState.STOP)
        updateProgress()
    }

    /**
     * Pause playing
     */
    fun pausePlaying() {
        if (!::audioPlayer.isInitialized) {
            return
        }

        audioPlayer.pause()
        playStateChangeListener?.onState(PlayState.PAUSE)
        updateProgress()
    }

    /**
     * Resume playing
     */
    fun resumePlaying() {
        if (!::audioPlayer.isInitialized) {
            return
        }

        audioPlayer.play()
        playStateChangeListener?.onState(PlayState.RESUME)
        updateProgress()
    }

    /**
     * Reset playing
     */
    fun reset() {
        if (!::audioPlayer.isInitialized) {
            return
        }

        audioPlayer.prepare()
        audioPlayer.pause()
        audioPlayer.seekTo(0)
    }

    /**
     * Release player
     */
    fun release() {
        if (!::audioPlayer.isInitialized) {
            return
        }

        audioPlayer.release()
    }

    /**
     * Seek playing
     */
    fun seekTo(time: Long) {
        if (!::audioPlayer.isInitialized) {
            return
        }

        audioPlayer.seekTo(time)
    }

    /**
     * Seek playing
     */
    fun playbackSpeed(@FloatRange(from = 0.0, fromInclusive = false) speed: Float) {
      if (!::audioPlayer.isInitialized) {
        return
      }

      val param = PlaybackParameters(speed)
      audioPlayer.playbackParameters = param
    }

    /**
     * check player is play or not
     */
    fun isPlaying(): Boolean {
        if (!::audioPlayer.isInitialized) {
            return false
        }

        return audioPlayer.isPlaying
    }

    /**
     * get total playing duration
     */
    fun duration(): Long {
        if (!::audioPlayer.isInitialized) {
            return 0L
        }

        return audioPlayer.duration
    }

    fun currentDuration(): Long {
        if (!::audioPlayer.isInitialized) {
            return 0L
        }

        return audioPlayer.currentPosition
    }

    /**
     * set [OnPlayStateChangeListener] to handle state changes of [AudioPlayer]
     */
    fun setOnPlayStateChangeListener(listener: OnPlayStateChangeListener) {
        this.playStateChangeListener = listener
    }

    /**
     * Kotlin-compatible version of [OnPlayStateChangeListener]
     */
    fun setOnPlayStateChangeListener(callback: (PlayState) -> Unit) {
        this.playStateChangeListener = object : OnPlayStateChangeListener {
            override fun onState(state: PlayState) {
                callback.invoke(state)
            }
        }
    }

    @Throws(FileNotFoundException::class, Exception::class)
    private fun downloadFile(filePath: String) {
      var count: Int
      val url = URL(playerConfig.sourceFile!!.path)
      val conection = url.openConnection()
      conection.connect()

      // this will be useful so that you can show a tipical 0-100%
      // progress bar
      //val lenghtOfFile = conection.contentLength

      // download the file
      val input: InputStream = BufferedInputStream(
        url.openStream(),
        8192
      )

      // Output stream
      val output: OutputStream = FileOutputStream(filePath)
      val data = ByteArray(1024)
      var total: Long = 0
      while (input.read(data).also { count = it } != -1) {
        total += count.toLong()
        // publishing the progress....
        // After this onProgressUpdate will be called
        //publishProgress("" + (total * 100 / lenghtOfFile).toInt())

        // writing data to file
        output.write(data, 0, count)
      }

      // flushing output
      output.flush()

      // closing streams
      output.close()
      input.close()
    }

    private fun convertFileFFmpeg(filePath: String): String {
      val extension = Utils.getExtension(playerConfig.sourceFile!!.path) ?: ""
      if(extension != "mp3") {
        val commandBuilder = mutableListOf<String>()
        commandBuilder.addAll(listOf("-y", "-i", playerConfig.sourceFile!!.path))
        commandBuilder.addAll(listOf("-c:v", "copy", "-c:a", "libmp3lame", "-q:a", "4", "-vn"))
        commandBuilder.add(filePath)
        val cmd = commandBuilder.toTypedArray()
        DebugState.error(cmd.contentToString())
        try {
          return when(FFmpeg.execute(cmd)) {
            0 -> {
              // SUCCESS
              filePath
            }
            255 -> {
              // CANCEL
              playerConfig.sourceFile!!.path
            }
            else -> {
              // FAILURE
              playerConfig.sourceFile!!.path
            }
          }
        } catch (ex: Exception) {
          DebugState.error(ex.message ?: "", ex)
          return playerConfig.sourceFile!!.path
        }
      } else {
        return playerConfig.sourceFile!!.path
      }
    }

    private fun withoutAmplituda(context: Context): Observable<List<Int>> {
      return Observable.create { subscriber: ObservableEmitter<List<Int>> ->

        var filePath =
          context.recordFile("${playerConfig.sourceFile!!.parentFile}/${playerConfig.sourceFile!!.nameWithoutExtension}.mp3").path
        var soundFile: SoundFile? = null
        try {
          downloadFile(filePath)
        } catch (e: Exception) {
          filePath = convertFileFFmpeg(filePath)
        }

        try {
          soundFile = SoundFile.create(filePath, null)
        } catch (e: IOException) {
          DebugState.error(TAG, e)
        } catch (e: SoundFile.InvalidInputException) {
          DebugState.error(TAG, e)
        } catch (e: Exception) {
          DebugState.error(TAG, e)
        }
        subscriber.onNext((soundFile?.frameGains?.toList() ?: mutableListOf<Int>().toList()))
        subscriber.onComplete()
      }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
    }

    private fun withAmplituda(context: Context): Observable<List<Int>> {
      return Observable.create { subscriber: ObservableEmitter<List<Int>> ->
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var filePath =
          context.recordFile("${playerConfig.sourceFile!!.parentFile}/${playerConfig.sourceFile!!.nameWithoutExtension}.mp3").path

        if(!Utils.isAudioType(playerConfig.sourceFile!!.path)) {
          try {
            downloadFile(filePath)
          } catch (e: Exception) {
            filePath = convertFileFFmpeg(filePath)
          }
        } else {
          filePath = playerConfig.sourceFile!!.path
        }

        Log.e("loadFileAmps", filePath)
        val amplitudes = Amplituda(context)
        amplitudes.processAudio(
          filePath,
          Compress.withParams(Compress.AVERAGE, 20),
          object : AmplitudaProgressListener() {
            override fun onStartProgress() {
              super.onStartProgress()
              DebugState.debug("Start Progress of AMPS")
            }

            override fun onStopProgress() {
              super.onStopProgress()
              DebugState.debug("Stop Progress of AMPS")
            }

            override fun onProgress(operation: ProgressOperation, progress: Int) {
              val currentOperation = when (operation) {
                ProgressOperation.PROCESSING -> "Process audio"
                ProgressOperation.DECODING -> "Decode resource"
                ProgressOperation.DOWNLOADING -> "Download audio from url"
                else -> ""
              }
              DebugState.debug("$currentOperation: $progress%")
            }
          }
        )[{ result ->
          subscriber.onNext(result.amplitudesAsList())
          subscriber.onComplete()
        }, { exception: AmplitudaException ->
          DebugState.error(exception.message ?: "", exception)
          subscriber.onNext(mutableListOf<Int>().toList())
          subscriber.onComplete()
        }]
      }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadFileAmps(context: Context, isAmplitudaMode: Boolean): Observable<List<Int>> {
        return if(isAmplitudaMode) withAmplituda(context) else withoutAmplituda(context)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermissionGranted(context: Context, permission: String) =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun updateProgress(position: Long = audioPlayer.currentPosition) {
        playerConfig.timerCountListener?.onTime(
            position,
            audioPlayer.playWhenReady
        )
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_ENDED -> {
                updateProgress(duration())
                playStateChangeListener?.onState(PlayState.STOP)
                reset()
            }
            Player.STATE_READY -> {
              DebugState.debug("STATE_READY")
            }
            Player.STATE_BUFFERING -> {
              DebugState.debug("STATE_BUFFERING")
            }
            Player.STATE_IDLE -> {
              DebugState.debug("STATE_IDLE")
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            stopTimer()
            startTimer()
        } else {
            stopTimer()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        DebugState.error(TAG, error)
    }

    private fun startTimer() {
        if (!playerConfig.checkAvailableTimer()) {
            DebugState.debug(LogConstants.TIMER_NOT_AVAILABLE)
            return
        }

        timerDisposable =
            Observable.interval(playerConfig.refreshTimerMillis, TimeUnit.MILLISECONDS)
                .subscribeMain { data, _, _ ->
                    if (data == null) return@subscribeMain
                    if (isPlaying()) {
                        updateProgress()

                        if (duration() == currentDuration()) {
                            stopPlaying()
                        }
                    }
                }
    }

    private fun stopTimer() {
        if (!playerConfig.checkAvailableTimer()) {
            DebugState.debug(LogConstants.TIMER_NOT_AVAILABLE)
            return
        }

        if (!::timerDisposable.isInitialized) {
            return
        }
        timerDisposable.safeDispose()
    }

    companion object {
      val TAG = "AudioPlayer"
    }
}
