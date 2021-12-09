package com.reactnativeaudiowaveform

import android.content.Context
import androidx.annotation.NonNull
import com.reactnativeaudiowaveform.audio.player.AudioPlayer
import com.reactnativeaudiowaveform.audio.player.extensions.SingletonHolder
import com.reactnativeaudiowaveform.audio.player.extensions.recordFile
import com.reactnativeaudiowaveform.audio.player.model.PlayState
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.lang.Exception

class Player private constructor(context: Context) {
    private val appContext = context
    private lateinit var player: AudioPlayer
    private var sourceFilePath: File? = null
    private var withDebug = false

    var onProgress: ((Long, Boolean) -> Unit)? = null
    var onPlayState: ((PlayState) -> Unit)? = null

    fun init(isDebug: Boolean = false): Player {
        this.withDebug = isDebug
        this.player = AudioPlayer()

        return this
    }

    fun setSource(@NonNull filePath: String) {
        if(!this::player.isInitialized)
            throw Exception("Player not initialized")

        sourceFilePath = appContext.recordFile(filePath)

        player.create(appContext) {
            this.sourceFile = sourceFilePath
            this.timerCountCallback = onProgress
            this.debugMode = withDebug
        }

        player.setOnPlayStateChangeListener { onPlayState?.invoke(it) }
    }

    fun startPlaying() {
        if(!this::player.isInitialized)
            throw Exception("Player not initialized")

        player.startPlaying(appContext)
    }

    fun stopPlaying() {
        if(!this::player.isInitialized)
            throw Exception("Player not initialized")

        player.stopPlaying()
    }

    fun resumePlaying() {
        if(!this::player.isInitialized)
            throw Exception("Player not initialized")

        player.resumePlaying()
    }

    fun pausePlaying() {
        if(!this::player.isInitialized)
            throw Exception("Player not initialized")

        player.pausePlaying()
    }

    fun getTotalDuration(): Long {
        if(!this::player.isInitialized)
            throw Exception("Player not initialized")

        return player.duration()
    }

    fun seekTo(@NonNull time: Long) {
        if(!this::player.isInitialized)
            throw Exception("Player not initialized")

        player.seekTo(time)
    }

    fun loadFileAmps(): Observable<List<Int>> {
        if(!this::player.isInitialized)
            throw Exception("Player not initialized")

        return player.loadFileAmps(appContext)
    }

    companion object : SingletonHolder<Player, Context>(::Player)
}
