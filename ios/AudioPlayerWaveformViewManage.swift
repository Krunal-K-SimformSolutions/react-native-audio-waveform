import Foundation
import AVKit

@objc(AudioPlayerWaveformViewManager)
class AudioPlayerWaveformViewManager: RCTViewManager {
    var fileUrl: URL?
    var component: AudioPlayerWaveformView? = nil
    
    override func view() -> (AudioPlayerWaveformView) {
        self.component = AudioPlayerWaveformView()
        NotificationCenter.default.addObserver(self, selector: #selector(didReceiveMeteringLevelUpdate),
                                               name: .audioRecorderManagerMeteringLevelDidUpdateNotification2, object: nil)
        return self.component!
    }
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc
    func create(_ reactTag: NSNumber, viewId: NSNumber, withDebug: NSNumber, subscriptionDurationInMilliseconds: NSNumber) {
        AudioPlayer.sharedInstance.inits(withDebug: withDebug)
    }
    @objc
    func source(_ reactTag: NSNumber, viewId: NSNumber, filePath: String, isAmplitudaMode: NSNumber) {
//        AudioPlayer.sharedInstance.setSource(filePath: filePath, isAmplitudaMode: isAmplitudaMode)
        print("url... \(filePath)")
        if filePath != "" {
          DispatchQueue.main.async {
              self.component?.audioVisualizationMode = .read
            let strif = filePath.replacingOccurrences(of: "file://", with: "")
            self.fileUrl = URL(fileURLWithPath: strif as String)
            self.component?.audioContextLoad(from: URL(fileURLWithPath: strif as String))
          }
    }
    }
    @objc
    func start(_ reactTag: NSNumber, viewId: NSNumber) {
        self.startPlayback()
    }
    
    func startPlayback() {
        guard let fileUrl = fileUrl else {
          return
        }
        DispatchQueue.main.async {
          if self.component?.playChronometer == nil || !(self.component?.playChronometer?.isPlaying ?? true) {
            let audioAsset = AVURLAsset.init(url: fileUrl, options: nil)
            let duration = audioAsset.duration
            let durationInSeconds = CMTimeGetSeconds(duration)
            self.component?.play(for: TimeInterval(durationInSeconds))
          }
    }
    }
    
    @objc
    func pause(_ reactTag: NSNumber, viewId: NSNumber) {
        DispatchQueue.main.async {
          self.component?.pause()
        }
    }
    @objc
    func resume(_ reactTag: NSNumber, viewId: NSNumber) {
        self.startPlayback()
    }
    @objc
    func stop(_ reactTag: NSNumber, viewId: NSNumber) {
        DispatchQueue.main.async {
          self.component?.stop()
            self.component?.reset()
        }
    }
    
    @objc private func didReceiveMeteringLevelUpdate(_ notification: Notification) {
        let percentage = notification.userInfo![audioPercentageUserInfoKey] as! Float
      DispatchQueue.main.async {
        print("didReceiveMeteringLevelUpdate",percentage)
        self.component?.add(meteringLevel: percentage)
      }
    }
    
}

class AudioPlayerWaveformView : AudioVisualizationView {    
    @objc var onSeekChange: RCTDirectEventBlock?
    @objc var onError: RCTDirectEventBlock?
    @objc var onPlayerState: RCTDirectEventBlock?
    @objc var onProgress: RCTDirectEventBlock?
    @objc var onLoadAmps: RCTDirectEventBlock?
    @objc var onAmpsState: RCTDirectEventBlock?
}
