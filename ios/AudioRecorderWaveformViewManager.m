#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(AudioRecorderWaveformViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(visibleProgress, float)
RCT_EXPORT_VIEW_PROPERTY(progress, float)
RCT_EXPORT_VIEW_PROPERTY(maxProgress, float)
RCT_EXPORT_VIEW_PROPERTY(waveWidth, float)
RCT_EXPORT_VIEW_PROPERTY(gap, float)
RCT_EXPORT_VIEW_PROPERTY(minHeight, float)
RCT_EXPORT_VIEW_PROPERTY(radius, float)
RCT_EXPORT_VIEW_PROPERTY(gravity, NSString)
RCT_EXPORT_VIEW_PROPERTY(barBgColor, NSString)
RCT_EXPORT_VIEW_PROPERTY(barPgColor, NSString)
RCT_EXPORT_VIEW_PROPERTY(isDrawSilencePadding, BOOL)

RCT_EXPORT_VIEW_PROPERTY(onSeekChange, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onError, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onBuffer, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onFFmpegState, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onFinished, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onProgress, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onRecorderState, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onSilentDetected, RCTDirectEventBlock)

RCT_EXTERN_METHOD(create:(nonnull NSNumber *)reactTag viewId:(nonnull NSNumber *)viewId sourceMode:(nonnull NSString *)sourceMode isFFmpegMode:(nonnull NSNumber *)isFFmpegMode withDebug:(nonnull NSNumber *)withDebug audioSourceAndroid:(nonnull NSNumber *)audioSourceAndroid audioEncoderAndroid:(nonnull NSNumber *)audioEncoderAndroid frequencyAndroid:(nonnull NSNumber *)frequencyAndroid bitRate:(nonnull NSNumber *)bitRate samplingRate:(nonnull NSNumber *)samplingRate mono:(nonnull NSNumber *)mono subscriptionDurationInMilliseconds:(nonnull NSNumber *)subscriptionDurationInMilliseconds)

RCT_EXTERN_METHOD(start:(nonnull NSNumber *)reactTag viewId:(nonnull NSNumber *)viewId filePath:(nonnull NSString *)filePath)
RCT_EXTERN_METHOD(pause:(nonnull NSNumber *)reactTag viewId:(nonnull NSNumber *)viewId)
RCT_EXTERN_METHOD(resume:(nonnull NSNumber *)reactTag viewId:(nonnull NSNumber *)viewId)
RCT_EXTERN_METHOD(stop:(nonnull NSNumber *)reactTag viewId:(nonnull NSNumber *)viewId)

@end
