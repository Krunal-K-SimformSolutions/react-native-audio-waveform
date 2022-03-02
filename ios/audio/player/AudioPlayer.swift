import Foundation
import UIKit
import AVFoundation
import Accelerate

class AudioPlayer {
    var audioEngine = AVAudioEngine()
    var audioFile: AVAudioFile?
    var fileUrl: URL?
    
    public static let sharedInstance = AudioPlayer()
        
    func inits(withDebug: NSNumber) {
    }
    func setSource(filePath: String, isAmplitudaMode: NSNumber) {
        print("url... \(filePath)")
        if filePath != "" {
          DispatchQueue.main.async {
//            self.audioVisualizationView.audioVisualizationMode = .read
            let strif = filePath.replacingOccurrences(of: "file://", with: "")
            self.fileUrl = URL(fileURLWithPath: strif as String)
//            self.audioVisualizationView.audioContextLoad(from: URL(fileURLWithPath: strif as String))
          }
    }
    }
    func startPlaying() {
    }
    func stopPlaying() {
    }
    func resumePlaying() {
    }
    func pausePlaying() {
    }
    func getTotalDuration() {
    }
    func seekTo() {
    }
    func playbackSpeed() {
    }
    func loadFileAmps() {
    }
}
