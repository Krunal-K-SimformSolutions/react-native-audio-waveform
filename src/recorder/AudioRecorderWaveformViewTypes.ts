import type { ViewStyle, StyleProp, NativeSyntheticEvent } from 'react-native';
import type { AudioRecordConfig } from './RecorderTypes';
import type {
  OnSeekChangeEvent,
  OnErrorEvent,
  OnProgressEvent,
} from '../GeneralTypes';

export type OnBufferEvent = (
  e: NativeSyntheticEvent<{
    maxAmplitude: number;
    bufferData: string;
    readCount: number;
  }>
) => void;

export type OnFFmpegStateEvent = (
  e: NativeSyntheticEvent<{ ffmpegState: string }>
) => void;

export type OnFinishedEvent = (
  e: NativeSyntheticEvent<{ file: string; duration: number }>
) => void;

export type OnRecorderStateEvent = (
  e: NativeSyntheticEvent<{ recordState: string }>
) => void;

export type OnSilentDetectedEvent = (
  e: NativeSyntheticEvent<{ time: number }>
) => void;

export type AudioRecorderWaveformViewProps = Partial<{
  visibleProgress: number;
  progress: number;
  maxProgress: number;
  waveWidth: number;
  gap: number;
  minHeight: number;
  radius: number;
  gravity: 'top' | 'center' | 'bottom';
  barBgColor: string | number;
  barPgColor: string | number;
  style: StyleProp<ViewStyle>;
  onSeekChange: OnSeekChangeEvent;
  onError: OnErrorEvent;
  onBuffer: OnBufferEvent;
  onFFmpegState: OnFFmpegStateEvent;
  onFinished: OnFinishedEvent;
  onProgress: OnProgressEvent;
  onRecorderState: OnRecorderStateEvent;
  onSilentDetected: OnSilentDetectedEvent;
}>;

export type AudioRecorderWaveformHandleType = Required<{
  createRecorder: (config: AudioRecordConfig) => void;
  startRecording: (filePath: string) => void;
  pauseRecording: () => void;
  resumeRecording: () => void;
  stopRecording: () => void;
}>;
