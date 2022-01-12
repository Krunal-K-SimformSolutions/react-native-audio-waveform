package com.reactnativeaudiowaveform.audio.player.extensions

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.*
import java.io.File

fun Context.recordFile(filePath: String): File {
    var file = File(filePath)
    if(file.parentFile == null) {
      file = File(filesDir,filePath)
    }
    file.parentFile?.mkdir()
    return file
}

fun Context.toDataSourceFactory(): DataSource.Factory = DefaultDataSource.Factory(this, DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true))

fun Context.toMediaSourceFactory(): MediaSourceFactory = DefaultMediaSourceFactory(this.toDataSourceFactory())

//Normal
fun String.toMediaItem(): MediaItem = MediaItem.fromUri(this)

fun File.toMediaItem(): MediaItem = MediaItem.fromUri(this.toUri())

//Progressive
fun String.toMediaSource(context: Context): MediaSource =
  ProgressiveMediaSource.Factory(context.toDataSourceFactory()).createMediaSource(
    MediaItem.fromUri(this))

fun File.toMediaSource(context: Context): MediaSource =
  ProgressiveMediaSource.Factory(context.toDataSourceFactory()).createMediaSource(
  MediaItem.fromUri(this.toUri()))

//Other
fun String.toMediaSource(): MediaSource =
  DataSpec(Uri.parse(this))
    .let { FileDataSource().apply { open(it) } }
    .let { DataSource.Factory { it } }
    .let { ProgressiveMediaSource.Factory(it, DefaultExtractorsFactory()) }
    .createMediaSource(MediaItem.fromUri(Uri.parse(this)))

fun File.toMediaSource(): MediaSource =
  DataSpec(this.toUri())
    .let { FileDataSource().apply { open(it) } }
    .let { DataSource.Factory { it } }
    .let { ProgressiveMediaSource.Factory(it, DefaultExtractorsFactory()) }
    .createMediaSource(MediaItem.fromUri(this.toUri()))
