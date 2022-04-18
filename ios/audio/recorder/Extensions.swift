//
//  Extensions.swift
//  react-native-audio-waveform
//
//  Created by Umang Loriya on 03/02/22.
//

import Foundation
import AVKit

enum RecordState : String {
    case start = "START"
    case stop = "STOP"
    case pause = "PAUSE"
    case resume = "RESUME"
    case canceled = "CANCELED"
}

//Notification Center
let bufferUserInfoKey = "buffer"
let audioPercentageUserInfoKey = "percentage"
let sampleRateKey = "sampleRate"
//Event Key
let timeKey = "time"
let fileKey = "file"
let durationKey = "duration"
let bufferDataKey = "bufferData"
let recordStateKey = "recordState"
let maxAmplitudeKey = "maxAmplitude"
let currentTimeKey = "currentTime"
let maxTimeKey = "maxTime"
let errorKey = "error"

// MARK: - UIColor
extension UIColor {
    
    convenience init(hexString: String) {
        let hex = hexString.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int = UInt64()
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(red: CGFloat(r) / 255, green: CGFloat(g) / 255, blue: CGFloat(b) / 255, alpha: CGFloat(a) / 255)
    }
    
}

extension StringProtocol {
    var data: Data { Data(utf8) }
    var base64Encoded: Data { data.base64EncodedData() }
    var base64Decoded: Data? { Data(base64Encoded: string) }
}

extension LosslessStringConvertible {
    var string: String { .init(self) }
}

extension Sequence where Element == UInt8 {
    var data: Data { .init(self) }
    var base64Decoded: Data? { Data(base64Encoded: data) }
    var string: String? { String(bytes: self, encoding: .utf8) }
}

extension AVAudioPCMBuffer {
    func data() -> Data {
        let channelCount = 1  // given PCMBuffer channel count is 1
        let channels = UnsafeBufferPointer(start: self.floatChannelData, count: channelCount)
        let ch0Data = NSData(bytes: channels[0], length:Int(self.frameCapacity * self.format.streamDescription.pointee.mBytesPerFrame))
        return ch0Data as Data
    }
}


extension Notification.Name {
    static let recorderStateNotification = Notification.Name("recorderStateNotification")
    static let audioPlayerManagerBufferDidUpdateNotification = Notification.Name("audioPlayerManagerBufferDidUpdateNotification")
    static let errorNotification = Notification.Name("errorNotification")
    static let sampleRate = Notification.Name("sampleRate")
    static let audioRecorderManagerMeteringLevelDidUpdateNotification2 = Notification.Name("AudioRecorderManagerMeteringLevelDidUpdateNotification2")
}

extension String {
    var fileURL: URL {
        return URL(fileURLWithPath: self)
    }
    var pathExtension: String {
        return fileURL.pathExtension
    }
    var lastPathComponent: String {
        return fileURL.lastPathComponent
    }
}
// End of Extension
