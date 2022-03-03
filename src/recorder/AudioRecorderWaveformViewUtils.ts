import type { MutableRefObject } from 'react';
import type { AudioRecordConfig } from './RecorderTypes';
import {
  requireNativeComponent,
  UIManager,
  findNodeHandle,
} from 'react-native';
import { LINKING_ERROR } from '../Constants';
import type { AudioRecorderWaveformViewProps } from './AudioRecorderWaveformViewTypes';

export const NativeAudioRecorderWaveformView =
  UIManager.getViewManagerConfig('AudioRecorderWaveformView') != null
    ? requireNativeComponent<AudioRecorderWaveformViewProps>(
        'AudioRecorderWaveformView'
      )
    : () => {
        throw new Error(LINKING_ERROR);
      };

export const setUpRecorder = (
  viewId: number | null,
  config?: AudioRecordConfig
) => {
  const {
    sourceMode,
    isFFmpegMode,
    withDebug,
    audioSourceAndroid,
    audioEncoderAndroid,
    frequencyAndroid,
    ffmpegConvertAndroid: { bitRate, samplingRate, mono } = {
      bitRate: undefined,
      samplingRate: undefined,
      mono: undefined,
    },
    subscriptionDurationInMilliseconds,
  } = config || {};
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command for init recorder
    UIManager.getViewManagerConfig('AudioRecorderWaveformView').Commands.create,
    [
      viewId,
      sourceMode,
      isFFmpegMode,
      withDebug,
      audioSourceAndroid,
      audioEncoderAndroid,
      frequencyAndroid,
      bitRate,
      samplingRate,
      mono,
      subscriptionDurationInMilliseconds,
    ]
  );
};

export const startRecorder = (viewId: number | null, filePath: string) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'start' command for start recording
    UIManager.getViewManagerConfig('AudioRecorderWaveformView').Commands.start,
    [viewId, filePath]
  );

export const pauseRecorder = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'pause' command for pause recording
    UIManager.getViewManagerConfig('AudioRecorderWaveformView').Commands.pause,
    [viewId]
  );

export const resumeRecorder = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'resume' command for resume recording
    UIManager.getViewManagerConfig('AudioRecorderWaveformView').Commands.resume,
    [viewId]
  );

export const stopRecorder = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'stop' command for stop recording
    UIManager.getViewManagerConfig('AudioRecorderWaveformView').Commands.stop,
    [viewId]
  );

export const cancelRecorder = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'stop' command for stop recording
    UIManager.getViewManagerConfig('AudioRecorderWaveformView').Commands.cancel,
    [viewId]
  );

export const getViewId = (ref?: MutableRefObject<any>): number | null => {
  const viewId = ref ? findNodeHandle(ref.current) : null;
  return viewId;
};
