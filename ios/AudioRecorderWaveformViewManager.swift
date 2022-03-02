import Foundation
import AVKit

@objc(AudioRecorderWaveformViewManager)
class AudioRecorderWaveformViewManager: RCTViewManager {
    var component: AudioRecorderWaveformView? = nil
    var timer: Timer?
    var currentTime = 0.0
    var timeIntervals = 0.5
    var isTimerRunning = false
    var isTimerPaused = false
    
    override func view() -> (AudioRecorderWaveformView) {
        self.component = AudioRecorderWaveformView()
        NotificationCenter.default.addObserver(self, selector: #selector(didBuffersUpdate),
                                               name: .audioPlayerManagerBufferDidUpdateNotification, object: nil)
        
        NotificationCenter.default.addObserver(self, selector: #selector(handleRecorderStatusUpdate),
                                               name: .recorderStateNotification, object: nil)
        
        NotificationCenter.default.addObserver(self, selector: #selector(handleRecorderError),
                                               name: .errorNotification, object: nil)
        
        return self.component!
    }
    
    // MARK: - Notifications Handling
    @objc private func didBuffersUpdate(_ notification: Notification) {
        let percentage = notification.userInfo![audioPercentageUserInfoKey] as! Float
        DispatchQueue.main.async {
            let linear = 1 - pow(5, percentage / 20)
            if percentage < -99 {
                if let onSilentDetected = self.component?.onSilentDetected {
                    onSilentDetected([timeKey: -1])
                }
            }
            // Here we add the same sample 3 times to speed up the animation.
            // Usually you'd just add the sample once.
            self.component?.add(samples: [linear, linear, linear, linear, linear, linear, linear])
            let buffer = notification.userInfo![bufferUserInfoKey] as! String
            
            if let onBuffer = self.component?.onBuffer {
                onBuffer([bufferDataKey: buffer, maxAmplitudeKey: percentage])
            }
        }
    }
    
    
    @objc private func handleRecorderStatusUpdate(_ notification: Notification) {
        let currentStatus = notification.userInfo![recordStateKey] as! RecordState
        DispatchQueue.main.async {
            if let onRecorderState = self.component?.onRecorderState {
                onRecorderState([recordStateKey: currentStatus.rawValue])
                self.handleTimerBasedOnRecorderState(state: currentStatus)
            }
        }
    }
    
    @objc private func handleRecorderError(_ notification: Notification) {
        let currentStatus = notification.userInfo![errorKey] as! String
        DispatchQueue.main.async {
            if let onError = self.component?.onError {
                onError([errorKey: currentStatus])
            }
        }
    }
    
    func handleTimerBasedOnRecorderState(state: RecordState) {
        switch state {
        case .start:
            self.runTimer()
            break
        case .stop:
            DispatchQueue.main.async {
                if let onFinished = self.component?.onFinished {
                    onFinished([fileKey: AudioRecorderManager.sharedInstance.currentRecordPath?.absoluteString ?? "", durationKey: self.currentTime * 1000])
                }
                self.resetTimer()
            }
            break
        case .pause, .resume:
            self.pauseTimer()
            break
        }
    }
    
    deinit {
        self.resetTimer()
        NotificationCenter.default.removeObserver(self, name: .audioPlayerManagerBufferDidUpdateNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: .recorderStateNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: .errorNotification, object: nil)
        NotificationCenter.default.removeObserver(self)
        
    }
    
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    private func runTimer() {
        isTimerRunning = true
        isTimerPaused = false
        self.timer = Timer.scheduledTimer(timeInterval: self.timeIntervals, target: self, selector: #selector(updateTimer), userInfo: nil, repeats: true)
    }
    
    @objc func updateTimer() {
        currentTime += self.timeIntervals
        if let onProgress = self.component?.onProgress {
            onProgress([currentTimeKey: self.currentTime * 1000, maxTimeKey: -1])
        }
    }
    
    func pauseTimer() {
        if (isTimerRunning) {
            if self.isTimerPaused == false {
                timer?.invalidate()
                self.isTimerPaused = true
            } else {
                runTimer()
                self.isTimerPaused = false
            }
        }
    }
    
    func resetTimer() {
        timer?.invalidate()
        currentTime = 0.0
        isTimerRunning = false
        isTimerPaused = false
    }
    
    @objc
    func create(_ reactTag: NSNumber, viewId: NSNumber, sourceMode: NSString, isFFmpegMode: NSNumber, withDebug: NSNumber, audioSourceAndroid: NSNumber, audioEncoderAndroid: NSNumber, frequencyAndroid: NSNumber, bitRate: NSNumber, samplingRate: NSNumber, mono: NSNumber, subscriptionDurationInMilliseconds: NSNumber) {
        self.timeIntervals = Double(truncating: subscriptionDurationInMilliseconds)/1000
        AudioRecorder.sharedInstance.inits(sourceMode:sourceMode, isFFmpegMode:isFFmpegMode, withDebug:withDebug, audioSourceAndroid:audioSourceAndroid, audioEncoderAndroid:audioEncoderAndroid, frequencyAndroid:frequencyAndroid, bitRate:bitRate, samplingRate:samplingRate, mono:mono)
    }
    
    @objc
    func start(_ reactTag: NSNumber, viewId: NSNumber, filePath: NSString) {
        AudioRecorder.sharedInstance.setSource(filePath: filePath)
        AudioRecorder.sharedInstance.startRecording()
    }
    
    @objc
    func pause(_ reactTag: NSNumber, viewId: NSNumber) {
        AudioRecorder.sharedInstance.pauseRecording()
    }
    
    @objc
    func resume(_ reactTag: NSNumber, viewId: NSNumber) {
        AudioRecorder.sharedInstance.resumeRecording()
    }
    
    @objc
    func stop(_ reactTag: NSNumber, viewId: NSNumber) {
        AudioRecorder.sharedInstance.stopRecording()
        DispatchQueue.main.async {
            self.component?.reset()
        }
    }
}

class AudioRecorderWaveformView : WaveformSeekBar {
    @objc var onSeekChange: RCTDirectEventBlock?
    @objc var onError: RCTDirectEventBlock?
    @objc var onBuffer: RCTDirectEventBlock?
    @objc var onFFmpegState: RCTDirectEventBlock?
    @objc var onFinished: RCTDirectEventBlock?
    @objc var onProgress: RCTDirectEventBlock?
    @objc var onRecorderState: RCTDirectEventBlock?
    @objc var onSilentDetected: RCTDirectEventBlock?
}
