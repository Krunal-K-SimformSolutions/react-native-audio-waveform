package com.reactnativeaudiowaveform.audio.player.extensions

import android.content.Context
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import java.io.File

fun Context.recordFile(filePath: String): File {
    var file = File(filePath)
    if(file.parentFile == null) {
      file = File(filesDir,filePath)
    }
    file.parentFile?.mkdir()
    return file
}

fun File.toMediaSource(): MediaSource =
    DataSpec(this.toUri())
        .let { FileDataSource().apply { open(it) } }
        .let { DataSource.Factory { it } }
        .let { ProgressiveMediaSource.Factory(it, DefaultExtractorsFactory()) }
        .createMediaSource(MediaItem.fromUri(this.toUri()))
