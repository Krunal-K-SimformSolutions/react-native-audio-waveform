//
//  AudioRecorderManager.swift
//  react-native-audio-waveform
//
//  Created by Umang Loriya on 03/02/22.
//

import Foundation
import UIKit
import AVFoundation
import Accelerate



@objc(AudioRecorderManager)
class AudioRecorderManager: RCTEventEmitter {
    
    var settings: [String:Any]  {
        return self.audioEngine.inputNode.outputFormat(forBus: 0).settings
    }
    
    var audioEngine = AVAudioEngine()
    private var averagePowerForChannel0: Float = 0
    private var averagePowerForChannel1: Float = 0
    let LEVEL_LOWPASS_TRIG:Float32 = 0.30
    var audioFile: AVAudioFile?
    public static let sharedInstance = AudioRecorderManager()
    
    var currentRecordPath: URL?
    var fileName = ""
    var isFullPathAvailable = false
    
    // we need to override this method and
    // return an array of event names that we can listen to
    override func supportedEvents() -> [String]! {
        return []
    }
    
    func saveAudioEngineSettings(sampleRate: Double, channel: Int) {
        //TODO: Make it dynamic later
        //        settings = [AVFormatIDKey: kAudioFormatLinearPCM, AVLinearPCMBitDepthKey: 16, AVLinearPCMIsFloatKey: true, AVSampleRateKey: sampleRate, AVNumberOfChannelsKey: channel]
    }
    
    func askPermission(with completion: @escaping (Bool?, Error?) -> Void) {
        let permission = AVAudioSession.sharedInstance().recordPermission
        switch permission {
        case .undetermined:
            AVAudioSession.sharedInstance().requestRecordPermission({ (result) in
                DispatchQueue.main.async {
                    if result {
                        completion(result, nil)
                    }
                }
            })
            break
        case .granted:
            completion(true, nil)
            break
        case .denied:
            completion(false, nil)
            break
        @unknown default:
            completion(false, nil)
            break
        }
    }
    
    func isHeadsetPluggedIn() -> Bool {
        let route = AVAudioSession.sharedInstance().currentRoute
        for desc in route.outputs {
            print(desc.portType)
            if desc.portType == AVAudioSession.Port.headphones || desc.portType == AVAudioSession.Port.bluetoothA2DP {
                return true
            }
        }
        return false
    }
    
    func isHeadSetBluetooth() -> Bool {
        let arrayInputs = AVAudioSession.sharedInstance().availableInputs
        for port in arrayInputs ?? [] {
            print(port.portType)
            
            if port.portType == AVAudioSession.Port.bluetoothHFP {
                return true
            }
        }
        return false
    }
    
    // MARK:- Recording
    func startRecording(isResume: Bool = false, with completion: @escaping (URL?, Error?) -> Void) {
        askPermission { isGranted, Error in
            if isGranted ?? false {
                if self.isRecording() {
                    completion(nil, nil)
                }
                
                self.audioEngine = AVAudioEngine()
                do {
                    let session = AVAudioSession.sharedInstance()
                    if self.isHeadsetPluggedIn() || self.isHeadSetBluetooth() {
                        try session.setCategory(.playAndRecord,
                                                mode: AVAudioSession.Mode.default,
                                                options: [.mixWithOthers, .allowBluetooth, .allowBluetoothA2DP])
                    } else {
                        try session.setCategory(.playAndRecord,
                                                mode: AVAudioSession.Mode.default,
                                                options: [.mixWithOthers, .allowBluetooth, .allowBluetoothA2DP, .defaultToSpeaker])
                    }
                    
                    try session.setActive(true, options: .notifyOthersOnDeactivation)
                } catch let error as NSError {
                    NotificationCenter.default.post(name: .errorNotification, object: self, userInfo: [errorKey: error.localizedDescription])
                    return
                }
                
                let inputNode = self.audioEngine.inputNode

                let recordingFormat: AVAudioFormat = inputNode.outputFormat(forBus: 0)
                inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) {[weak self] (buffer, time) in
                    guard let strongSelf = self else {
                        return
                    }
                    guard let channelData = buffer.floatChannelData else {
                        return
                    }
                    
                    let channelDataValue = channelData.pointee
                    // 4
                    let channelDataValueArray = stride(
                        from: 0,
                        to: Int(buffer.frameLength),
                        by: buffer.stride)
                        .map { channelDataValue[$0] }
                    
                    // 5
                    let rms = sqrt(channelDataValueArray.map {
                        return $0 * $0
                    }
                                    .reduce(0, +) / Float(buffer.frameLength))
                    
                    // 6
                    
                    let avgPower = 20 * log10(rms)
                    let percentage: Float = pow(10, (0.05 * avgPower))
                    NotificationCenter.default.post(name: .audioRecorderManagerMeteringLevelDidUpdateNotification2, object: self, userInfo: [audioPercentageUserInfoKey: percentage])
                    
                    strongSelf.audioMetering(buffer: buffer)
                }
                
                do {
                    self.audioEngine.prepare()
                    try self.audioEngine.start()
                    NotificationCenter.default.post(name: .recorderStateNotification, object: self, userInfo: [recordStateKey: isResume ? RecordState.resume : RecordState.start])
                } catch let error as NSError {
                    NotificationCenter.default.post(name: .errorNotification, object: self, userInfo: [errorKey: error.localizedDescription])
                    return
                }
                
                //TODO: Handle interruptions from session
                let notificationName = AVAudioSession.interruptionNotification
                NotificationCenter.default.addObserver(self, selector: #selector(self.handleRecording), name: notificationName, object: nil)
                completion(self.currentRecordPath, nil)
            }
        }
        
    }
    
    //MARK:- Actions
    @objc func handleRecording() {
        if !isRecording() {
            self.checkPermissionAndRecord()
        } else {
            self.stopRecording()
        }
    }
    
    private func audioMetering(buffer:AVAudioPCMBuffer) {
        let inNumberFrames:UInt = UInt(buffer.frameLength)
        if buffer.format.channelCount > 0 {
            let samples = (buffer.floatChannelData![0])
            var avgValue:Float32 = 0
            vDSP_meamgv(samples,1 , &avgValue, inNumberFrames)
            var v:Float = -100
            if avgValue != 0 {
                v = 20.0 * log10f(avgValue)
            }
            self.averagePowerForChannel0 = (self.LEVEL_LOWPASS_TRIG*v) + ((1-self.LEVEL_LOWPASS_TRIG)*self.averagePowerForChannel0)
            self.averagePowerForChannel1 = self.averagePowerForChannel0
        }
        
        if buffer.format.channelCount > 1 {
            let samples = buffer.floatChannelData![1]
            var avgValue:Float32 = 0
            vDSP_meamgv(samples, 1, &avgValue, inNumberFrames)
            var v:Float = -100
            if avgValue != 0 {
                v = 20.0 * log10f(avgValue)
            }
            self.averagePowerForChannel1 = (self.LEVEL_LOWPASS_TRIG*v) + ((1-self.LEVEL_LOWPASS_TRIG)*self.averagePowerForChannel1)
        }
        let data = buffer.data()
        let base64 = data.base64EncodedString()
        NotificationCenter.default.post(name: .audioPlayerManagerBufferDidUpdateNotification, object: self, userInfo: [bufferUserInfoKey: base64,audioPercentageUserInfoKey:self.averagePowerForChannel1])
        
        let write = true
        if write {
            if self.audioFile == nil {
                self.audioFile = self.createAudioRecordFile()
            }
            if let f = self.audioFile {
                do {
                    try f.write(from: buffer)
                } catch let error as NSError {
                    print(error.localizedDescription)
                }
            }
        }
    }
    
    
    // MARK:- Paths and files
    private func createAudioRecordPath(fileName: String) -> URL? {
        if isFullPathAvailable {
            return URL(string: fileName)
        }
        let currentFileName = "\(fileName)"
        let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let url = documentsDirectory.appendingPathComponent(currentFileName)
        return url
    }
    
    private func createAudioRecordFile() -> AVAudioFile? {
        guard let path = self.createAudioRecordPath(fileName: fileName) else {
            return nil
        }
        self.currentRecordPath = path
        do {
            let file = try AVAudioFile(forWriting: path, settings: self.settings, commonFormat: .pcmFormatFloat32, interleaved: true)
            return file
        } catch let error as NSError {
            NotificationCenter.default.post(name: .errorNotification, object: self, userInfo: [errorKey: error.localizedDescription])
            return nil
        }
    }
    
    func pauseRecording() {
        self.audioEngine.pause()
        NotificationCenter.default.post(name: .recorderStateNotification, object: self, userInfo: [recordStateKey: RecordState.pause])
        
        do {
            try AVAudioSession.sharedInstance().setActive(false)
        } catch  let error as NSError {
            print(error.localizedDescription)
            return
        }
    }
    
    func stopRecording(isCancel: Bool = false) {
        self.audioFile = nil
        self.audioEngine.inputNode.removeTap(onBus: 0)
        self.audioEngine.stop()
        do {
            try AVAudioSession.sharedInstance().setActive(false)
        } catch  let error as NSError {
            print(error.localizedDescription)
            return
        }
        if isCancel {
            NotificationCenter.default.post(name: .recorderStateNotification, object: self, userInfo: [recordStateKey: RecordState.canceled])
        } else {
            NotificationCenter.default.post(name: .recorderStateNotification, object: self, userInfo: [recordStateKey: RecordState.stop])
        }
        
    }
    
    private func checkPermissionAndRecord() {
        let permission = AVAudioSession.sharedInstance().recordPermission
        switch permission {
        case .undetermined:
            AVAudioSession.sharedInstance().requestRecordPermission({ (result) in
                DispatchQueue.main.async {
                    if result {
                        self.startRecording { (url, error) in
                        }
                    }
                }
            })
            break
        case .granted:
            self.startRecording { (url, error) in
            }
            break
        case .denied:
            break
        @unknown default:
            break
        }
    }
    
    func isRecording() -> Bool {
        if self.audioEngine.isRunning {
            return true
        }
        return false
    }
    
    private func format() -> AVAudioFormat? {
        let format = AVAudioFormat(settings: self.settings)
        return format
    }
}

