/*
 * @Author: your name
 * @Date: 2021-07-10 07:57:46
 * @LastEditTime: 2021-07-23 13:39:14
 * @LastEditors: your name
 * @Description: In User Settings Edit
 * @FilePath: /box_dev/Users/hy/proj/dev/javascript/cordova-plugin-tencent-mlvb/src/ios/TencentMLVB.h
 */
#import <Cordova/CDV.h>
#import "TXLivePush.h"
#import "TXLivePlayer.h"

@interface TencentMLVB : CDVPlugin<TXLivePushListener, TXLivePlayListener>

@property UIView* videoView;
@property TXLivePush* livePusher;
@property TXLivePlayer* livePlayer;

- (void) getVersion:(CDVInvokedUrlCommand*)command;
- (void) prepareVideoView;
- (void) detachVideoView;
- (void) startPush:(CDVInvokedUrlCommand*)command;
- (void) stopPush:(CDVInvokedUrlCommand*)command;
- (void) startPlay:(CDVInvokedUrlCommand*)command;
- (void) stopPlay:(CDVInvokedUrlCommand*)command;
- (void) setVideoQuality:(CDVInvokedUrlCommand*)command;
- (void) setBeautyFilterDepth:(CDVInvokedUrlCommand*)command;
- (void) setFilter:(CDVInvokedUrlCommand*)command;
- (void) switchCamera:(CDVInvokedUrlCommand*)command;
- (void) toggleTorch:(CDVInvokedUrlCommand*)command;
- (void) setFocusPosition:(CDVInvokedUrlCommand*)command;
- (void) setWaterMark:(CDVInvokedUrlCommand*)command;
- (void) setPauseImage:(CDVInvokedUrlCommand*)command;
- (void) pause:(CDVInvokedUrlCommand*)command;
- (void) resume:(CDVInvokedUrlCommand*)command;
- (void) setRenderMode:(CDVInvokedUrlCommand*)command;
- (void) setRenderRotation:(CDVInvokedUrlCommand*)command;
- (void) seek:(CDVInvokedUrlCommand*)command;
- (void) enableHWAcceleration:(CDVInvokedUrlCommand*)command;
- (void) startRecord:(CDVInvokedUrlCommand*)command;
- (void) stopRecord:(CDVInvokedUrlCommand*)command;
- (void) alert:(NSString*)message title:(NSString*)title;
- (void) alert:(NSString*)message;

@end
