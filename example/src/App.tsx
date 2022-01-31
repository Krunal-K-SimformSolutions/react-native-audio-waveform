import * as React from 'react';
import {
  Platform,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {
  AudioRecorderWaveformView,
  AudioPlayerWaveformView,
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

const isAndroid = Platform.OS === 'android';

export default function App() {
  const [playbackSpeed, setPlaybackSpeed] = React.useState(1)
  const refRecorder = React.useRef<AudioRecorderWaveformHandleType>();
  const refPlayer = React.useRef<AudioPlayerWaveformHandleType>();

  React.useEffect(() => {
    refRecorder?.current?.createRecorder({
      sourceMode: 'auto',
      isFFmpegMode: true,
      audioSourceAndroid: AudioSourceAndroidType.MIC,
      audioEncoderAndroid: AudioEncoderAndroidType.PCM_16BIT,
      frequencyAndroid: 44100,
      withDebug: true,
      ffmpegConvertAndroid: {
        bitRate: FFmpegBitRate.def,
        samplingRate: FFmpegSamplingRate.ORIGINAL,
        mono: true
      }
    });
  }, [refRecorder]);

  React.useEffect(() => {
    refPlayer?.current?.createPlayer(true);
  }, [refPlayer]);

  return (
    <View style={styles.container}>
      {isAndroid && (
        <View style={styles.waveContainer}>
            <AudioRecorderWaveformView
              ref={refRecorder}
              style={{ width: 400, height: 200 }}
              gap={3}
              waveWidth={6}
              radius={3}
              minHeight={1}
              gravity={'center'}
              progressColor={'#FF0000'}
              backgroundColor={'#0000FF'}
              onFinished={({ nativeEvent: { file } }) => {
                refPlayer?.current?.setSource(file, true);
              }}
            />
          </View>
      )}
      {isAndroid && (
        <View style={styles.waveContainer}>
          <AudioPlayerWaveformView 
            ref={refPlayer} 
            style={{ width: 400, height: 200 }}
            gap={3}
            waveWidth={6}
            radius={3}
            minHeight={1}
            gravity={'center'}
            playbackSpeed={playbackSpeed}
            progressColor={'#FF0000'}
            backgroundColor={'#00FF00'} />
          </View>
      )}

      <Text style={styles.optionText}>Recorder</Text>
      <View style={styles.boxContainer}>
        <TouchableOpacity
          activeOpacity={0.8}
          onPress={() => {
            refRecorder.current?.startRecording('sample.mp3');
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
