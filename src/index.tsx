import { AudioRecorderWaveformView } from './recorder/AudioRecorderWaveformView';
import { AudioPlayerWaveformView } from './player/AudioPlayerWaveformView';
export type {
  FFmpegConvert,
  AudioRecordConfig,
} from './recorder/RecorderTypes';
export {
  AudioSourceAndroidType,
  AudioEncoderAndroidType,
  FFmpegBitRate,
  FFmpegSamplingRate
} from './recorder/RecorderTypes';
export type {
  OnSeekChangeEvent,
  OnErrorEvent,
  OnProgressEvent,
} from './GeneralTypes';
export type {
  AudioRecorderWaveformViewProps,
  AudioRecorderWaveformHandleType,
  OnBufferEvent,
  OnFFmpegStateEvent,
  OnFinishedEvent,
  OnRecorderStateEvent,
  OnSilentDetectedEvent,
} from './recorder/AudioRecorderWaveformViewTypes';
export type {
  AudioPlayerWaveformViewProps,
  AudioPlayerWaveformHandleType,
  OnPlayerStateEvent,
  OnLoadAmpsEvent,
  OnAmpsStateEvent,
} from './player/AudioPlayerWaveformViewTypes';

const AudioWaveformView = Object.assign(AudioRecorderWaveformView, {
  Recorder: AudioRecorderWaveformView,
  Player: AudioPlayerWaveformView,
});

export default AudioWaveformView;