export enum AudioSourceAndroidType {
  DEFAULT = 0,
  MIC = 1,
  VOICE_UPLINK = 2,
  VOICE_DOWNLINK = 3,
  VOICE_CALL = 4,
  CAMCORDER = 5,
  VOICE_RECOGNITION = 6,
  VOICE_COMMUNICATION = 7,
  REMOTE_SUBMIX = 8,
  UNPROCESSED = 9,
  VOICE_PERFORMANCE = 10,
}

export enum AudioEncoderAndroidType {
  INVALID = 0,
  DEFAULT = 1,
  PCM_16BIT = 2,
  PCM_8BIT = 3,
  PCM_FLOAT = 4,
  AC3 = 5,
  E_AC3 = 6,
  DTS = 7,
  DTS_HD = 8,
  MP3 = 9,
  AAC_LC = 10,
  AAC_HE_V1 = 11,
  AAC_HE_V2 = 12,
  IEC61937 = 13,
  DOLBY_TRUEHD = 14,
  AAC_ELD = 15,
  AAC_XHE = 16,
  AC4 = 17,
  E_AC3_JOC = 18,
  DOLBY_MAT = 19,
}

export enum FFmpegBitRate {
  def = 0,
  u8 = 1, //8 – unsigned 8 bits
  s16 = 2, //16 – signed 16 bits
  s32 = 3, // 32 – signed 32 bits (also used for 24-bit audio)
  flt = 4, //32 – float
  dbl = 5, //64 – double
  u8p = 6, //8 – unsigned 8 bits, planar
  s16p = 7, //16 – signed 16 bits, planar
  s32p = 8, //32 – signed 32 bits, planar
  fltp = 9, //32 – float, planar
  dblp = 10, //64 – double, planar
  s64 = 11, //64 – signed 64 bits
  s64p = 12, //64 – signed 64 bits, planar
}

export enum FFmpegSamplingRate {
  ENCODING_IN_8000 = 1,
  ENCODING_IN_11025 = 2,
  ENCODING_IN_16000 = 3,
  ENCODING_IN_22050 = 4,
  ENCODING_IN_32000 = 5,
  ENCODING_IN_44100 = 6,
  ENCODING_IN_48000 = 7,
  ENCODING_IN_88200 = 8,
  ENCODING_IN_96000 = 9,
  ENCODING_IN_76400 = 10,
  ENCODING_IN_192000 = 11,
  ENCODING_IN_352800 = 12,
  ENCODING_IN_384000 = 13,
  ORIGINAL = 0,
}

export type FFmpegConvert = {
  bitRate?: FFmpegBitRate;
  samplingRate?: FFmpegSamplingRate;
  mono?: boolean;
};

// '.pcm',  '.wav' -> false isFfmpegMode
// '.mp3', '.aac', '.m4a',  '.wma', '.flac', '.mp4'
export type AudioRecordConfig = {
  sourceMode?: 'normal' | 'noise' | 'auto';
  isFFmpegMode?: boolean;
  audioSourceAndroid?: AudioSourceAndroidType;
  audioEncoderAndroid?: AudioEncoderAndroidType;
  frequencyAndroid?: number;
  withDebug?: boolean;
  ffmpegConvertAndroid?: FFmpegConvert;
  subscriptionDurationInMilliseconds?: number;
};
