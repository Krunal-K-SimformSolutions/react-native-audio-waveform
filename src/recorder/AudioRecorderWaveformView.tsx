import React, { forwardRef, useImperativeHandle } from 'react';
import styles from './AudioRecorderWaveformViewStyles';
import type {
  AudioRecorderWaveformViewProps,
  AudioRecorderWaveformHandleType,
} from './AudioRecorderWaveformViewTypes';
import type { AudioRecordConfig } from './RecorderTypes';
import {
  getViewId,
  pauseRecorder,
  resumeRecorder,
  setUpRecorder,
  startRecorder,
  stopRecorder,
  cancelRecorder,
  NativeAudioRecorderWaveformView,
} from './AudioRecorderWaveformViewUtils';
import { StyleSheet, View } from 'react-native';

const refView = React.createRef<View>();

function CustomAudioRecorderWaveformView(
  {
    style,
    progress,
    maxProgress,
    visibleProgress,
    waveWidth,
    gap,
    minHeight,
    radius,
    barBgColor,
    barPgColor,
    gravity = 'center',
    onSeekChange,
    onError,
    onBuffer,
    onFFmpegState,
    onFinished,
    onProgress,
    onRecorderState,
    onSilentDetected,
    onSampleRate
  }: AudioRecorderWaveformViewProps,
  ref: React.Ref<AudioRecorderWaveformHandleType>
): React.ReactElement {
  useImperativeHandle(ref, () => ({
    createRecorder: (config: AudioRecordConfig = {}) => {
      setUpRecorder(getViewId(refView), config);
    },
    startRecording: (filePath: string) => {
      startRecorder(getViewId(refView), filePath);
    },
    pauseRecording: () => {
      pauseRecorder(getViewId(refView));
    },
    resumeRecording: () => {
      resumeRecorder(getViewId(refView));
    },
    stopRecording: () => {
      stopRecorder(getViewId(refView));
    },
    cancelRecording: () => {
      cancelRecorder(getViewId(refView));
    },
  }));

  return (
    <NativeAudioRecorderWaveformView
      ref={refView}
      style={StyleSheet.flatten([styles.defaultStyle, style])}
      progress={progress}
      maxProgress={maxProgress}
      visibleProgress={visibleProgress}
      waveWidth={waveWidth}
      gap={gap}
      minHeight={minHeight}
      radius={radius}
      barBgColor={barBgColor}
      barPgColor={barPgColor}
      gravity={gravity}
      onSeekChange={onSeekChange}
      onError={onError}
      onBuffer={onBuffer}
      onFFmpegState={onFFmpegState}
      onFinished={onFinished}
      onProgress={onProgress}
      onRecorderState={onRecorderState}
      onSilentDetected={onSilentDetected}
      onSampleRate={onSampleRate}
    />
  );
}

export const AudioRecorderWaveformView = forwardRef(
  CustomAudioRecorderWaveformView
);
