package com.reactnativeaudiowaveform.visualizer

interface SeekBarOnProgressChanged {
    fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean)
}
