import type { NativeSyntheticEvent } from 'react-native';

export type OnSeekChangeEvent = (
  e: NativeSyntheticEvent<{ progress: number; fromUser: boolean }>
) => void;

export type OnErrorEvent = (e: NativeSyntheticEvent<{ error: string }>) => void;

export type OnProgressEvent = (
  e: NativeSyntheticEvent<{ currentTime: number; maxTime?: number }>
) => void;
