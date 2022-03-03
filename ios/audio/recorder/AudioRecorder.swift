import Foundation
import UIKit
import AVFoundation
import Accelerate

class AudioRecorder {
    var audioEngine = AVAudioEngine()
    var audioFile: AVAudioFile?
    var audioVisualizationView: WaveformSeekBar!
    var audioRecorderManager: AudioRecorderManager = AudioRecorderManager.sharedInstance
    public static let sharedInstance = AudioRecorder()
    
    func inits(sourceMode: NSString, isFFmpegMode: NSNumber, withDebug: NSNumber, audioSourceAndroid: NSNumber, audioEncoderAndroid: NSNumber, frequencyAndroid: NSNumber, bitRate: NSNumber, samplingRate: NSNumber, mono: NSNumber, channel: NSNumber = 1) {
        let actualSampleRate = getSampleRateFromValue(index: samplingRate)
        audioRecorderManager.saveAudioEngineSettings(sampleRate: actualSampleRate, channel: Int(truncating: channel))
    }
    
    func setSource(filePath: NSString) {
        print(filePath);
        let fileManager = FileManager.default
        var isDir: ObjCBool = false
        if fileManager.fileExists(atPath: filePath as String, isDirectory: &isDir) {
            if isDir.boolValue {
                audioRecorderManager.isFullPathAvailable = false
                AudioRecorderManager.sharedInstance.fileName = filePath.lastPathComponent
            }else{
                audioRecorderManager.isFullPathAvailable = true
                audioRecorderManager.fileName = filePath as String
            }
        } else {
            audioRecorderManager.isFullPathAvailable = false
            audioRecorderManager.fileName = filePath as String
        }
    }
    
    func startRecording() {
        if !self.audioRecorderManager.isRecording() {
            self.audioRecorderManager.startRecording { (url, error) in
            }
        }
    }
    
    func stopRecording() {
        DispatchQueue.main.async {
            self.audioRecorderManager.stopRecording()
            
        }
    }
    
    func resumeRecording() {
        if !self.audioRecorderManager.isRecording() {
            audioRecorderManager.startRecording(isResume: true) { (url, error) in
            }
        }
    }
    
    func pauseRecording() {
        DispatchQueue.main.async {
            self.audioRecorderManager.pauseRecording()
        }
    }
    
    func finishRecording() {
        
    }
    
    func getSampleRateFromValue(index:NSNumber) -> Double {
        
        switch index {
        case 0,6:
            return 44100
        case 1:
            return 8000
        case 2:
            return 11025
        case 3:
            return 16000
        case 4:
            return 22050
        case 5:
            return 32000
        case 7:
            return 48000
        case 8:
            return 88200
        case 9:
            return 96000
        case 10:
            return 76400
        case 11:
            return 192000
        case 12:
            return 352800
        case 13:
            return 384000
        default:
            return 44100
        }
        
    }
    
}
