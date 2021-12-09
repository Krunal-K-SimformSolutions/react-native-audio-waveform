import React, { forwardRef, useImperativeHandle, useRef } from 'react';
import styles from './AudioPlayerWaveformViewStyles';
import type {
  AudioPlayerWaveformViewProps,
  AudioPlayerWaveformHandleType,
} from './AudioPlayerWaveformViewTypes';
import {
  getViewId,
  pausePlayer,
  resumePlayer,
  setSource,
  setUpPlayer,
  startPlayer,
  stopPlayer,
  NativeAudioPlayerWaveformView,
} from './AudioPlayerWaveformViewUtils';

function CustomAudioPlayerWaveformView(
  {
    style,
    progress,
    maxProgress,
    visibleProgress,
    waveWidth,
    gap,
    minHeight,
    radius,
    backgroundColor,
    progressColor,
    gravity = 'center',
    onSeekChange,
    onError,
    onPlayerState,
    onProgress,
    onLoadAmps,
    onAmpsState,
  }: AudioPlayerWaveformViewProps,
  ref: React.Ref<AudioPlayerWaveformHandleType>
): React.ReactElement {
  const refView = useRef();

  useImperativeHandle(ref, () => ({
    createPlayer: (withDebug: boolean = false) => {
      setUpPlayer(getViewId(refView), withDebug);
    },
    setSource: (filePath: string) => {
      setSource(getViewId(refView), filePath);
    },
    startPlaying: () => {
      startPlayer(getViewId(refView));
    },
    pausePlaying: () => {
      pausePlayer(getViewId(refView));
    },
    resumePlaying: () => {
      resumePlayer(getViewId(refView));
    },
    stopPlaying: () => {
      stopPlayer(getViewId(refView));
    },
  }));

  return (
    <NativeAudioPlayerWaveformView
      ref={refView}
      style={[styles.defaultStyle, style]}
      progress={progress}
      maxProgress={maxProgress}
      visibleProgress={visibleProgress}
      waveWidth={waveWidth}
      gap={gap}
      minHeight={minHeight}
      radius={radius}
      backgroundColor={backgroundColor}
      progressColor={progressColor}
      gravity={gravity}
      onSeekChange={onSeekChange}
      onError={onError}
      onPlayerState={onPlayerState}
      onProgress={onProgress}
      onLoadAmps={onLoadAmps}
      onAmpsState={onAmpsState}
    />
  );
}

export const AudioPlayerWaveformView: React.ForwardRefExoticComponent<AudioPlayerWaveformViewProps> =
  forwardRef(CustomAudioPlayerWaveformView);
