#import "TencentMLVB.h"
#import "TXLiveBase.h"
#import "TXLivePushConfig.h"
#import "TXLivePlayConfig.h"
#import "MainViewController.h"
#import "TXDeviceManager.h"

@implementation MainViewController(CDVViewController)
- (void) viewDidLoad {
    [super viewDidLoad];
    self.webView.backgroundColor = [UIColor clearColor];
    self.webView.opaque = NO;
}
@end


@implementation TencentMLVB

@synthesize videoView;
@synthesize livePusher;
@synthesize livePlayer;

//- (void) greet:(CDVInvokedUrlCommand*)command {
//    NSString* name = [[command arguments] objectAtIndex:0];
//    NSString* msg = [NSString stringWithFormat: @"Hello, %@", name];
//    CDVPluginResult* result = [CDVPluginResult
//                               resultWithStatus:CDVCommandStatus_OK
//                               messageAsString:msg];
//    [self alert:msg];
//    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
//}

+ (void)initialize
{
    NSDictionary* infoDict = [[NSBundle mainBundle] infoDictionary];
    [TXLiveBase setLicenceURL:[infoDict objectForKey:@"MlvbLicenseUrl"] key:[infoDict objectForKey:@"MlvbLicenseKey"]];
}

- (void) prepareVideoView {
    if (self.videoView) return;
    self.videoView = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    [self.webView.superview addSubview:self.videoView];
    [self.webView.superview bringSubviewToFront:self.webView];
}

- (void) destroyVideoView {
    if (!self.videoView) return;
    [self.videoView removeFromSuperview];
    self.videoView = nil;
    // 把 webView 变回白色
    // [self.webView setBackgroundColor:[UIColor whiteColor]];
}

- (void) getVersion:(CDVInvokedUrlCommand*)command {
    NSString* version = [TXLiveBase getSDKVersionStr];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:version];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) startPush:(CDVInvokedUrlCommand*)command {
    if (self.livePusher) return;
    NSString* url = [command.arguments objectAtIndex:0];
    if (url == nil || url == [NSNull null]) {
        url = @"rtmp://144681.livepush.myqcloud.com/live/room1?txSecret=abf2f6a4c5f858cf2c0fc51ebff71c63&txTime=82DFB501";
    }
    [self prepareVideoView];
    TXLivePushConfig* _config = [[TXLivePushConfig alloc] init];
    self.livePusher = [[V2TXLivePusher alloc] initWithLiveMode:V2TXLiveMode_RTMP];
    [self.livePusher setRenderView:videoView];
    [self.livePusher startCamera:YES];
    [self.livePusher startPush:url];
}

- (void) stopPush:(CDVInvokedUrlCommand*)command {
    if (!self.livePusher) return;
    [self.livePusher stopCamera];
    [self.livePusher stopPush];
    self.livePusher = nil;
    [self destroyVideoView];
}

- (void) startPlay:(CDVInvokedUrlCommand*)command {
    if (self.livePlayer) return;
    NSString* url = [command.arguments objectAtIndex:0];
//    TX_Enum_PlayType playUrlType = (TX_Enum_PlayType)[command.arguments objectAtIndex:1];
//    NSInteger playUrlType = (NSInteger)[command.arguments objectAtIndex:1];

    [self prepareVideoView];

    self.livePlayer = [[V2TXLivePlayer alloc] init];
    [self.livePlayer setRenderView:videoView];
    [self.livePlayer setRenderFillMode:V2TXLiveFillModeFit];
    if (url == nil || url == [NSNull null]) {
        url = @"rtmp://livetest2.homei-life.com/live/room1?txSecret=c627caf3b969c1dbd6929c95a71998b2&txTime=80FA8CCC";
    }
    [self.livePlayer startPlay:url];
}

- (void) stopPlay:(CDVInvokedUrlCommand*)command {
    if (!self.livePlayer) return;
    [self.livePlayer stopPlay];
    self.livePlayer = nil;
    [self destroyVideoView];
}

- (void) setVideoQuality:(CDVInvokedUrlCommand*)command {
}

- (void) setBeautyFilterDepth:(CDVInvokedUrlCommand*)command {
}

- (void) setWhiteningFilterDepth:(CDVInvokedUrlCommand*)command {
}

- (void) setFilter:(CDVInvokedUrlCommand*)command {
}

- (void) switchCamera:(CDVInvokedUrlCommand*)command {
    if (!self.livePusher) return;
    TXDeviceManager * dmgr = [self.livePusher getDeviceManager];
    BOOL isFront = [dmgr isFrontCamera];
    [[self.livePusher getDeviceManager] switchCamera: !isFront];
}

- (void) toggleTorch:(CDVInvokedUrlCommand*)command {
}

- (void) setFocusPosition:(CDVInvokedUrlCommand*)command {
}

- (void) setWaterMark:(CDVInvokedUrlCommand*)command {
}

- (void) setPauseImage:(CDVInvokedUrlCommand*)command {
}

- (void) resize:(CDVInvokedUrlCommand*)command {
}

- (void) pause:(CDVInvokedUrlCommand*)command {
}

- (void) resume:(CDVInvokedUrlCommand*)command {
}

- (void) setRenderMode:(CDVInvokedUrlCommand*)command {
}

- (void) setRenderRotation:(CDVInvokedUrlCommand*)command {
}

- (void) seek:(CDVInvokedUrlCommand*)command {
}

- (void) enableHWAcceleration:(CDVInvokedUrlCommand*)command {
}

- (void) startRecord:(CDVInvokedUrlCommand*)command {
}

- (void) stopRecord:(CDVInvokedUrlCommand*)command {
}

- (void) alert:(NSString*)message title:(NSString*)title {
    UIAlertView* alert = [
                          [UIAlertView alloc]
                          initWithTitle:title
                          message:message
                          delegate:nil
                          cancelButtonTitle:@"OK"
                          otherButtonTitles:nil
                          ];
    [alert show];
    //[alert release];
}

- (void)  alert:(NSString*)message {
    [self alert:message title:@"系统消息"];
}

@end
