import type { ViewStyle, StyleProp, NativeSyntheticEvent } from 'react-native';
import type {
  OnSeekChangeEvent,
  OnErrorEvent,
  OnProgressEvent,
} from '../GeneralTypes';

export type OnPlayerStateEvent = (
  e: NativeSyntheticEvent<{ playState: string }>
) => void;

export type OnAmpsStateEvent = (
  e: NativeSyntheticEvent<{ ampsState: string }>
) => void;

export type OnLoadAmpsEvent = (
  e: NativeSyntheticEvent<{ loadAmps: Array<number> }>
) => void;

export type AudioPlayerWaveformViewProps = Partial<{
  visibleProgress: number;
  progress: number;
  maxProgress: number;
  waveWidth: number;
  gap: number;
  minHeight: number;
  radius: number;
  gravity: 'top' | 'center' | 'bottom';
  backgroundColor: string | number;
  progressColor: string | number;
  style: StyleProp<ViewStyle>;
  onSeekChange: OnSeekChangeEvent;
  onError: OnErrorEvent;
  onPlayerState: OnPlayerStateEvent;
  onProgress: OnProgressEvent;
  onLoadAmps: OnLoadAmpsEvent;
  onAmpsState: OnAmpsStateEvent;
}>;

export type AudioPlayerWaveformHandleType = Required<{
  createPlayer: (withDebug: boolean) => void;
  setSource: (filePath: string) => void;
  startPlaying: () => void;
  pausePlaying: () => void;
  resumePlaying: () => void;
  stopPlaying: () => void;
}>;
