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
    if (!self.videoView) {
        self.videoView = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    }
    self.videoView.hidden = NO;
    [self.webView.superview addSubview:self.videoView];
    self.webView.backgroundColor = [UIColor clearColor];
    self.webView.opaque = NO;
    [self.webView.superview bringSubviewToFront:self.webView];
}

- (void) detachVideoView {
    if (!self.videoView) return;
    self.videoView.hidden = YES;
    [self.videoView removeFromSuperview];
    // 把 webView 变回白色
    [self.webView setBackgroundColor:[UIColor whiteColor]];
}

- (void) getVersion:(CDVInvokedUrlCommand*)command {
    NSString* version = [TXLiveBase getSDKVersionStr];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:version];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)initLivePusher {
    if (livePusher == nil) {
        TXLivePushConfig *config = [[TXLivePushConfig alloc] init];
        config.pauseImg = [UIImage imageNamed:@"pause_publish.jpg"];
        config.pauseFps = 15;
        config.pauseTime = 300;
        
        livePusher = [[TXLivePush alloc] initWithConfig:config];
        livePusher.delegate = self;
        [livePusher setVideoQuality:VIDEO_QUALITY_HIGH_DEFINITION adjustBitrate:NO adjustResolution:NO];
        [livePusher setLogViewMargin:UIEdgeInsetsMake(120, 10, 60, 10)];
        config.videoEncodeGop = 2;
        [livePusher setConfig:config];
    }
}

- (void) startPush:(CDVInvokedUrlCommand*)command {
    if (self.livePusher) return;
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString* url = [command.arguments objectAtIndex:0];
        if (url == nil || url == [NSNull null]) {
            url = @"rtmp://livepush.gwk001.com/live/room1?txSecret=19b2cdd333b00d0df3e863ae69c7a433&txTime=8308690A";
        }
        [self prepareVideoView];
        [self initLivePusher];
        [self.livePusher startPreview:self.videoView];
        int ret = [self.livePusher startPush:url];
        if (ret != 10000) {
            NSLog(@"push error with ret %d", ret);
        }
    });
}

- (void) initTxLivePlayer
{
    if (self.livePlayer == nil) {
        TXLivePlayConfig *playConfig = [[TXLivePlayConfig alloc] init];
        playConfig.bAutoAdjustCacheTime = YES;
        playConfig.minAutoAdjustCacheTime = 1.0f;
        playConfig.maxAutoAdjustCacheTime = 1.0f;
        self.livePlayer = [[TXLivePlayer alloc] init];
        [self.livePlayer setupVideoWidget:CGRectZero containView:self.videoView insertIndex:0];
        [self.livePlayer setConfig:playConfig];
        [self.livePlayer setRenderMode:RENDER_MODE_FILL_EDGE];
        
    }
}

- (void) stopPush:(CDVInvokedUrlCommand*)command {
    if (!self.livePusher) return;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.livePusher stopPreview];
        [self.livePusher stopPush];
        self.livePusher = nil;
        [self detachVideoView];
    });
}

-(int)getPlayType:(NSString*)playUrl {
    if ([playUrl hasPrefix:@"rtmp:"]) {
        return  PLAY_TYPE_LIVE_RTMP;
    }
    else if (([playUrl hasPrefix:@"https:"] || [playUrl hasPrefix:@"http:"]) && ([playUrl rangeOfString:@".flv"].length > 0)) {
        return PLAY_TYPE_LIVE_FLV;
    }
    else{
        return PLAY_TYPE_LIVE_FLV;
    }
}

- (void) startPlay:(CDVInvokedUrlCommand*)command {
    if (self.livePlayer) return;

    dispatch_async(dispatch_get_main_queue(), ^{
        [self prepareVideoView];
        [self initTxLivePlayer];
        NSString* url = [command.arguments objectAtIndex:0];
        if (url == nil || url == [NSNull null]) {
            url = @"https://live.gwk001.com/live/room1.flv?txSecret=5fceb0ac8b3f3bd43837ff36ca3de00c&txTime=830869F9";
        }
        [self.livePlayer startPlay:url type:[self getPlayType:url]];
    });
}

- (void) stopPlay:(CDVInvokedUrlCommand*)command {
    if (!self.livePlayer) return;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.livePlayer stopPlay];
        self.livePlayer = nil;
        [self detachVideoView];
    });
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
    [self.livePusher switchCamera];
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

- (void)onPushEvent:(int)EvtID withParam:(NSDictionary *)param
{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog (@"PUSH EVENT: %d: %@", EvtID, param);
        if (EvtID == PUSH_ERR_NET_DISCONNECT || EvtID == PUSH_ERR_INVALID_ADDRESS) {
          //...
        } else if (EvtID == PUSH_WARNING_NET_BUSY) {
          NSLog(@"您当前的网络环境不佳，请尽快更换网络保证正常直播");
        }
      //...
  });
}

- (void)onNetStatus:(NSDictionary *)param {
    
}

- (void)onPlayEvent:(int)EvtID withParam:(NSDictionary *)param {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog (@"PLAY EVENT: %d: %@", EvtID, param);
        if (EvtID == PLAY_WARNING_RECONNECT || EvtID == PLAY_EVT_PLAY_END) {
          //...
        } else if (EvtID == PLAY_WARNING_SERVER_DISCONNECT) {
          NSLog(@"您当前的网络环境不佳，请尽快更换网络保证正常直播");
        }
      //...
  });
}



- (void)onScreenCapturePaused:(int)reason {
    
}


- (void)onScreenCaptureResumed:(int)reason {
    
}


- (void)onScreenCaptureStarted {
    
}


- (void)onScreenCaptureStoped:(int)reason {
    
}


@end
