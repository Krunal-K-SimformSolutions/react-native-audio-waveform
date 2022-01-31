import type { MutableRefObject } from 'react';
import {
  requireNativeComponent,
  UIManager,
  findNodeHandle,
} from 'react-native';
import { LINKING_ERROR } from '../Constants';

export const NativeAudioPlayerWaveformView =
  UIManager.getViewManagerConfig('AudioPlayerWaveformView') != null
    ? requireNativeComponent('AudioPlayerWaveformView')
    : () => {
        throw new Error(LINKING_ERROR);
      };

export const setUpPlayer = (viewId: number | null, withDebug: boolean) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command for init player
    UIManager.getViewManagerConfig('AudioPlayerWaveformView').Commands.create,
    [viewId, withDebug]
  );

export const setSource = (viewId: number | null, filePath: string, isAmplitudaMode?: boolean) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'source' command for set source
    UIManager.getViewManagerConfig('AudioPlayerWaveformView').Commands.source,
    [viewId, filePath, isAmplitudaMode]
  );

export const startPlayer = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'start' command for start playing
    UIManager.getViewManagerConfig('AudioPlayerWaveformView').Commands.start,
    [viewId]
  );

export const pausePlayer = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'pause' command for pause playing
    UIManager.getViewManagerConfig('AudioPlayerWaveformView').Commands.pause,
    [viewId]
  );

export const resumePlayer = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'resume' command for resume playing
    UIManager.getViewManagerConfig('AudioPlayerWaveformView').Commands.resume,
    [viewId]
  );

export const stopPlayer = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'stop' command for stop playing
    UIManager.getViewManagerConfig('AudioPlayerWaveformView').Commands.stop,
    [viewId]
  );

export const getViewId = (ref?: MutableRefObject<any>): number | null => {
  const viewId = ref ? findNodeHandle(ref.current) : null;
  return viewId;
};
