import * as React from 'react';
import {
  Platform,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { Buffer } from 'buffer';
import AudioWaveformView, {
  AudioSourceAndroidType,
  AudioEncoderAndroidType,
  FFmpegBitRate,
  FFmpegSamplingRate,
} from 'react-native-audio-waveform';
import type {
  AudioRecorderWaveformHandleType,
  AudioPlayerWaveformHandleType
} from 'react-native-audio-waveform';
import styles from './AppStyles';

export default function App() {
  const [isHide, setHide] = React.useState(false)
  const [playbackSpeed, setPlaybackSpeed] = React.useState(1)
  const refRecorder = React.useRef<AudioRecorderWaveformHandleType>();
  const refPlayer = React.useRef<AudioPlayerWaveformHandleType>();

  React.useEffect(() => {
    refRecorder?.current?.createRecorder({
      sourceMode: 'auto',
      isFFmpegMode: false,
      audioSourceAndroid: AudioSourceAndroidType.MIC,
      audioEncoderAndroid: AudioEncoderAndroidType.PCM_16BIT,
      frequencyAndroid: 44100,
      withDebug: true,
      ffmpegConvertAndroid: {
        bitRate: FFmpegBitRate.def,
        samplingRate: FFmpegSamplingRate.ORIGINAL,
        mono: true
      },
      subscriptionDurationInMilliseconds: 500
    });
  }, [refRecorder]);

  React.useEffect(() => {
    refPlayer?.current?.createPlayer(true, 50);
  }, [refPlayer]);

  return (
    <View style={styles.container}>
    <View style={styles.waveContainer}>
      {!isHide && <AudioWaveformView.Recorder
        ref={refRecorder}
        style={{ width: 400, height: 200 }}
        gap={5}
        waveWidth={10}
        radius={5}
        minHeight={60}
        gravity={'center'}
        barPgColor={'#FF0000'}
        barBgColor={'#0000FF'}
        onError={({ nativeEvent: { error } }) => {
            console.log({ error })
          }}
        onBuffer={({ nativeEvent: { maxAmplitude, bufferData, readCount } }) => {
           const chunk = Buffer.from(bufferData, 'base64');
            console.log({ bufferData: chunk, maxAmplitude,  })
          }}
        onFFmpegState={({ nativeEvent: { ffmpegState } }) => {
            console.log({ ffmpegState })
          }}
        onFinished={({ nativeEvent: { file, duration } }) => {
            console.log({ file, duration })
            refPlayer?.current?.setSource(file, false);
          }}
        onProgress={({ nativeEvent: { currentTime, maxTime, timeString } }) => {
            console.log({ currentTime, maxTime, timeString })
          }}
        onRecorderState={({ nativeEvent: { recordState } }) => {
            console.log({ recordState })
          }}
        onSilentDetected={({ nativeEvent: { time } }) => {
            console.log({ time })
          }}
      />
        }
    </View>
    <View style={styles.waveContainer}>
      {!isHide && <AudioWaveformView.Player 
        ref={refPlayer} 
        style={{ width: 400, height: 200 }}
        gap={5}
        waveWidth={10}
        radius={5}
        minHeight={60}
        gravity={'center'}
        playbackSpeed={playbackSpeed}
        barPgColor={'#FF0000'}
        barBgColor={'#00FF00'}
        onSeekChange={({ nativeEvent: { progress,  fromUser } }) => {
          console.log({ progress, fromUser })
        }}
        onError={({ nativeEvent: { error } }) => {
            console.log({ error })
          }}
        onPlayerState={({ nativeEvent: { playState } }) => {
            console.log({ playState })
          }}
        onProgress={({ nativeEvent: { currentTime,  maxTime } }) => {
            console.log({ currentTime, maxTime })
          }}
        onLoadAmps={({ nativeEvent: { loadAmps } }) => {
            console.log({ loadAmps })
          }}
        onAmpsState={({ nativeEvent: { ampsState } }) => {
            console.log({ ampsState })
          }} />
        }
      </View>
      <Text style={styles.optionText}>Recorder</Text>
      <View style={styles.boxContainer}>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refRecorder.current?.startRecording('sample.wav');
          }}
          style={styles.box}
        >
          <Text style={styles.boxText}>Start</Text>
        </TouchableOpacity>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refRecorder.current?.pauseRecording();
          }}
          style={styles.box}
        >
          <Text style={styles.boxText}>Pause</Text>
        </TouchableOpacity>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refRecorder.current?.resumeRecording();
          }}
          style={styles.box}
        >
          <Text style={styles.boxText}>Resume</Text>
        </TouchableOpacity>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refRecorder.current?.stopRecording();
          }}
          style={styles.box}
        >
          <Text style={styles.boxText}>Stop</Text>
        </TouchableOpacity>
        <TouchableOpacity
            activeOpacity={0.8}
            onPress={() => {
              setHide((prev) => !prev)
            }}
            style={styles.box}
          >
            <Text style={styles.boxText}>{isHide ? 'Visible' : 'InVisible'}</Text>
        </TouchableOpacity>
      </View>
      <Text style={styles.optionText}>Player</Text>
      <View style={styles.boxContainer}>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refPlayer.current?.startPlaying();
          }}
          style={styles.box}
        >
          <Text style={styles.boxText}>Start</Text>
        </TouchableOpacity>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refPlayer.current?.pausePlaying();
          }}
          style={styles.box}
        >
          <Text style={styles.boxText}>Pause</Text>
        </TouchableOpacity>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refPlayer.current?.resumePlaying();
          }}
          style={styles.box}
        >
          <Text style={styles.boxText}>Resume</Text>
        </TouchableOpacity>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refPlayer.current?.stopPlaying();
          }}
          style={styles.box}
        >
          <Text style={styles.boxText}>Stop</Text>
        </TouchableOpacity>
         <TouchableOpacity
            activeOpacity={0.8}
            onPress={() => {
              if(playbackSpeed === 1.0) {
                setPlaybackSpeed(1.25);
              } else if(playbackSpeed === 1.25) {
                setPlaybackSpeed(2);
              } else {
                setPlaybackSpeed(1);
              }
            }}
            style={styles.box}
          >
            <Text style={styles.boxText}>{playbackSpeed === 1.25 ? 1.5 : playbackSpeed}x</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
