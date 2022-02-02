import Foundation

class WaveformSeekBar : UIView {
    
    @objc
    var visibleProgress: Float = 0 {
        didSet {
        }
    }
    @objc
    var progress: Float = 0 {
        didSet {
        }
    }
    @objc
    var maxProgress: Float = 0 {
        didSet {
        }
    }
    @objc
    var waveWidth: Float = 0 {
        didSet {
            self.configuration = self.configuration.with(
                style: .striped(.init(color: UIColor(hexString: self.barPgColor), width: CGFloat(waveWidth), spacing: CGFloat(self.gap)))
            )
        }
    }
    @objc
    var gap: Float = 0 {
        didSet {
            self.configuration = self.configuration.with(
                style: .striped(.init(color: UIColor(hexString: self.barPgColor), width: CGFloat(self.waveWidth), spacing: CGFloat(gap)))
            )            
        }
    }
    @objc
    var minHeight: Float = 0 {
        didSet {
            self.configuration = self.configuration.with(
                verticalScalingFactor: CGFloat(minHeight)
            )
        }
    }
    @objc
    var radius: Float = 0 {
        didSet {
        }
    }
    @objc
    var gravity: String = "" {
        didSet {
            switch (gravity) {
            case "top":
                self.configuration = self.configuration.with(
                    position: WaveformSeekBarConfig.Position.top
                )
            case "center":
                self.configuration = self.configuration.with(
                    position: WaveformSeekBarConfig.Position.middle
                )
            case "bottom":
                self.configuration = self.configuration.with(
                    position: WaveformSeekBarConfig.Position.bottom
                )
            default:
                self.configuration = self.configuration.with(
                    position: WaveformSeekBarConfig.Position.middle
                )
                
            }
        }
    }
    @objc
    var barBgColor: String = "" {
        didSet {
            self.configuration = self.configuration.with(
                backgroundColor: UIColor(hexString: barBgColor)
            )
        }
    }
    @objc
    var barPgColor: String = "" {
        didSet {
            self.configuration = self.configuration.with(
                style: .striped(.init(color: UIColor(hexString: barPgColor), width: CGFloat(self.waveWidth), spacing: CGFloat(self.gap)))
            )
        }
    }
    @objc
    var isDrawSilencePadding: Bool = false {
        didSet {
            /// If set to `true`, a zero line, indicating silence, is being drawn while the received
            /// samples are not filling up the entire view's width yet.
            sampleLayer.shouldDrawSilencePadding = isDrawSilencePadding
        }
    }
    
    /// Default configuration with dampening enabled.
    public static let defaultConfiguration = WaveformSeekBarConfig.Configuration(dampening: .init(percentage: 0.025, sides: .both))
    
    public var configuration: WaveformSeekBarConfig.Configuration {
        didSet {
            sampleLayer.configuration = configuration
        }
    }
    
    private var sampleLayer: WaveformSeekBarLayer! {
        return layer as? WaveformSeekBarLayer
    }
    
    override public class var layerClass: AnyClass {
        return WaveformSeekBarLayer.self
    }
    
    public init(configuration: WaveformSeekBarConfig.Configuration = defaultConfiguration) {
        self.configuration = configuration
        super.init(frame: .zero)
        self.contentMode = .redraw
    }
    
    public override init(frame: CGRect) {
        self.configuration = Self.defaultConfiguration
        super.init(frame: frame)
        contentMode = .redraw
    }
    
    required init?(coder: NSCoder) {
        self.configuration = Self.defaultConfiguration
        super.init(coder: coder)
        contentMode = .redraw
    }
    
    /// The sample to be added. Re-draws the waveform with the pre-existing samples and the new one.
    /// Value must be within `(0...1)` to make sense (0 being loweset and 1 being maximum amplitude).
    public func add(sample: Float) {
        sampleLayer.add([sample])
    }
    
    /// The samples to be added. Re-draws the waveform with the pre-existing samples and the new ones.
    /// Values must be within `(0...1)` to make sense (0 being loweset and 1 being maximum amplitude).
    public func add(samples: [Float]) {
        sampleLayer.add(samples)
    }
    
    /// Clears the samples, emptying the waveform view.
    public func reset() {
        sampleLayer.reset()
    }
}

class WaveformSeekBarLayer: CALayer {
    @NSManaged var samples: [Float]
    
    private var lastNewSampleCount: Int = 0
    
    var configuration = WaveformSeekBar.defaultConfiguration {
        didSet { contentsScale = configuration.scale }
    }
    
    var shouldDrawSilencePadding: Bool = false {
        didSet {
            waveformDrawer.shouldDrawSilencePadding = shouldDrawSilencePadding
            setNeedsDisplay()
        }
    }
    
    private let waveformDrawer = WaveformSeekBarDraw()
    
    override class func needsDisplay(forKey key: String) -> Bool {
        if key == #keyPath(samples) {
            return true
        }
        return super.needsDisplay(forKey: key)
    }
    
    override func draw(in context: CGContext) {
        super.draw(in: context)
        
        UIGraphicsPushContext(context)
        waveformDrawer.draw(waveform: samples, newSampleCount: lastNewSampleCount, on: context, with: configuration.with(size: bounds.size))
        UIGraphicsPopContext()
    }
    
    func add(_ newSamples: [Float]) {
        lastNewSampleCount = newSamples.count
        samples += newSamples
    }
    
    func reset() {
        lastNewSampleCount = 0
        samples = []
    }
}
