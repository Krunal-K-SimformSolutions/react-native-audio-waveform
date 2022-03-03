import React, { forwardRef, useImperativeHandle } from 'react';
import { StyleSheet, View } from 'react-native';
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

const refView = React.createRef<View>();

function CustomAudioPlayerWaveformView(
  {
    style,
    progress,
    maxProgress,
    visibleProgress,
    playbackSpeed,
    waveWidth,
    gap,
    minHeight,
    radius,
    barBgColor,
    barPgColor,
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
  useImperativeHandle(ref, () => ({
    createPlayer: (
      withDebug: boolean = false,
      subscriptionDurationInMilliseconds: number
    ) => {
      setUpPlayer(
        getViewId(refView),
        withDebug,
        subscriptionDurationInMilliseconds
      );
    },
    setSource: (filePath: string, isAmplitudaMode?: boolean) => {
      setSource(getViewId(refView), filePath, isAmplitudaMode);
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
      style={StyleSheet.flatten([styles.defaultStyle, style])}
      progress={progress}
      maxProgress={maxProgress}
      visibleProgress={visibleProgress}
      waveWidth={waveWidth}
      gap={gap}
      minHeight={minHeight}
      radius={radius}
      playbackSpeed={playbackSpeed}
      barBgColor={barBgColor}
      barPgColor={barPgColor}
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

export const AudioPlayerWaveformView = forwardRef(
  CustomAudioPlayerWaveformView
);
