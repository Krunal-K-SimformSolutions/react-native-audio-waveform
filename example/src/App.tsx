import * as React from 'react';
import {
  Platform,
  StyleSheet,
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

const isAndroid = Platform.OS === 'android';

export default function App() {
  const refRecorder = React.useRef<AudioRecorderWaveformHandleType>();
  const refPlayer = React.useRef<AudioPlayerWaveformHandleType>();

  React.useEffect(() => {
    refRecorder?.current?.createRecorder({
      sourceMode: 'normal',
      isFFmpegMode: false,
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
        <AudioRecorderWaveformView
          ref={refRecorder}
          style={styles.container}
          gap={3}
          waveWidth={6}
          radius={3}
          minHeight={1}
          gravity={'center'}
          progressColor={'#FF0000'}
          backgroundColor={'#0000FF'}
          onFinished={({ nativeEvent: { file } }) => {
            refPlayer?.current?.setSource(file, false);
          }}
        />
      )}
      {isAndroid && (
        <AudioPlayerWaveformView 
          ref={refPlayer} 
          style={styles.container}
          gap={3}
          waveWidth={6}
          radius={3}
          minHeight={1}
          gravity={'center'}
          progressColor={'#FF0000'}
          backgroundColor={'#00FF00'} />
      )}

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
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 90,
    height: 30,
    borderRadius: 15,
    backgroundColor: 'green',
    borderColor: 'blue',
    borderWidth: 2,
    justifyContent: 'center',
    alignItems: 'center',
  },
  boxText: {
    fontSize: 14,
    color: 'white',
  },
  boxContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
  },
  optionText: {
    fontSize: 28,
    color: 'red',
    paddingVertical: 10,
  },
});
