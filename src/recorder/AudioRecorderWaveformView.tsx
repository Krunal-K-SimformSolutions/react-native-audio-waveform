import React, { forwardRef, useImperativeHandle, useRef } from 'react';
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
  NativeAudioRecorderWaveformView,
} from './AudioRecorderWaveformViewUtils';

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
  }: AudioRecorderWaveformViewProps,
  ref: React.Ref<AudioRecorderWaveformHandleType>
): React.ReactElement {
  const refView = useRef();

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
  }));

  return (
    <NativeAudioRecorderWaveformView
      ref={refView}
      style={[styles.defaultStyle, style]}
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
    />
  );
}

export const AudioRecorderWaveformView: React.ForwardRefExoticComponent<AudioRecorderWaveformViewProps> =
  forwardRef(CustomAudioRecorderWaveformView);
