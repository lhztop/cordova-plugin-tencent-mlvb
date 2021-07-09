package com.qcloud.cordova.mlvb;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.tencent.av.sdk.*;
//import com.tencent.ilivesdk.*;
//import com.tencent.ilivesdk.core.*;
//import com.tencent.livesdk.*;

import com.google.gson.Gson;
import com.qcloud.cordova.mlvb.common.MLVBCommonDef;
import com.qcloud.cordova.mlvb.utils.TCUtils;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.live2.V2TXLiveCode;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.TXLog;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static com.tencent.live2.V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModeLandscape;
import static com.tencent.live2.V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;


public class TencentMLVB extends CordovaPlugin {
  private CallbackContext callbackContext;
  private Activity activity;
  private final String[] permissions = {
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.READ_PHONE_STATE,
//            Manifest.permission.CALL_PHONE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.READ_LOGS,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.MODIFY_AUDIO_SETTINGS,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.CAMERA
  };
  private CordovaWebView cordovaWebView;

  /**
   * Sets the context of the Command. This can then be used to do things like
   * get file paths associated with the Activity.
   *
   * @param cordova The context of the main Activity.
   * @param webView The CordovaWebView Cordova is running in.
   */
  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    this.activity = cordova.getActivity();
    this.cordovaWebView = webView;
    MLVBRoomImpl.getInstance(cordova, webView);
  }

  /**
   * Executes the request and returns PluginResult.
   *
   * @param action          The action to execute.
   * @param args            JSONArry of arguments for the plugin.
   * @param callbackContext The callback id used when calling back into JavaScript.
   * @return True if the action was valid, false if not.
   */
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    if (!hasPermisssion()) {
      requestPermissions(0);
    }

    this.callbackContext = callbackContext;
    MLVBRoomImpl mlvbInstance = MLVBRoomImpl.getInstance();
    switch (action) {
      case "getVersion":
        return mlvbInstance.getVersion(callbackContext);
      case "startPush": {
        final String url = args.getString(0);
        return mlvbInstance.startPush(url, callbackContext);
      }
      case "stopPush":
        return mlvbInstance.stopPush(callbackContext);
      case "onPushEvent":
        alert("尚未实现");
        break;
      case "startPlay": {
        final String url = args.getString(0);
        final int playType = args.getInt(1);
        return mlvbInstance.startPlay(url, callbackContext);
      }
      case "stopPlay":
        return mlvbInstance.stopPlay(callbackContext);
//      case "onPlayEvent":
//        alert("尚未实现");
//        break;
//      case "setVideoQuality":
//        if (mlvbInstance.mLivePusher == null) return false;
//        final int quality = args.getInt(0);
//        final int adjustBitrate = args.getInt(1);
//        final int adjustResolution = args.getInt(2);
////            mLivePusher.setVideoQuality(quality, adjustBitrate, adjustResolution);
//        mlvbInstance.mLivePusher.setVideoQuality(quality, true, true);
//        break;
//      case "setBeautyFilterDepth":
//        if (mlvbInstance.mLivePusher == null) return false;
//        final int beautyDepth = args.getInt(0);
//        final int whiteningDepth = args.getInt(1);
//        mlvbInstance.mLivePusher.setBeautyFilter(1, beautyDepth, whiteningDepth, 5);
//        break;
//      case "setExposureCompensation":
//        // TODO: 尚未测试
//        if (mlvbInstance.mLivePusher == null) return false;
//        final float depth = (float) args.getDouble(0);
//        mlvbInstance.mLivePusher.setExposureCompensation(depth);
//        break;
//      case "setFilter":
//        alert("尚未实现");
//        break;
//      case "switchCamera":
//        if (mlvbInstance.mLivePusher == null) return false;
//        mlvbInstance.mLivePusher.switchCamera();
//        break;
//      case "toggleTorch":
//        // TODO: 尚未测试
//        if (mlvbInstance.mLivePusher == null) return false;
//        final boolean enabled = args.getBoolean(0);
//        mlvbInstance.mLivePusher.turnOnFlashLight(enabled);
//        break;
//      case "setFocusPosition":
//        alert("尚未实现");
//        break;
//      case "setWaterMark":
//        alert("尚未实现");
//        break;
//      case "setPauseImage":
//        alert("尚未实现");
//        break;
//      case "resize":
//        alert("尚未实现");
//        break;
//      case "pause":
//        alert("尚未实现");
//        break;
//      case "resume":
//        alert("尚未实现");
//        break;
//      case "setRenderMode":
//        alert("尚未实现");
//        break;
//      case "setRenderRotation":
//        alert("尚未实现");
//        break;
//      case "enableHWAcceleration":
//        alert("尚未实现");
//        break;
//      case "startRecord":
////            return startRecord(callbackContext);
//        break;
//      case "stopRecord":
////            return stopRecord(callbackContext);
//        break;
      case "fixMinFontSize":
//            return stopRecord(callbackContext);
        return fixMinFontSize(callbackContext);
    }

    callbackContext.error("Undefined action: " + action);
    return false;

  }

  /**
   * 设置最小字号
   *
   * @param callbackContext
   * @return
   */
  private boolean fixMinFontSize(final CallbackContext callbackContext) {
    try {
      WebSettings settings = ((WebView) cordovaWebView.getEngine().getView()).getSettings();
      settings.setMinimumFontSize(1);
      settings.setMinimumLogicalFontSize(1);
    } catch (Exception error) {
      callbackContext.error("10003");
      return false;
    }
    return true;
  }

  public void alert(String msg) {
    alert(msg, "系统提示");
  }

  public void alert(String msg, String title) {
    new AlertDialog.Builder(this.activity)
      .setTitle(title)
      .setMessage(msg)//设置显示的内容
      .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
        @Override
        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
          // TODO Auto-generated method stub
//                        finish();
        }
      }).show();//在按键响应事件中显示此对话框
  }



  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    String statusCode;
    switch (requestCode) {
      case 990:  // demoPush
        if (resultCode == 1) {
          statusCode = "success";
          callbackContext.success(statusCode);
        }
        break;
      default:
        break;
    }
  }

  /**
   * check application's permissions
   */
  public boolean hasPermisssion() {
    for (String p : permissions) {
      if (!PermissionHelper.hasPermission(this, p)) {
        return false;
      }
    }
    return true;
  }

  /**
   * We override this so that we can access the permissions variable, which no longer exists in
   * the parent class, since we can't initialize it reliably in the constructor!
   *
   * @param requestCode The code to get request action
   */
  public void requestPermissions(int requestCode) {
    PermissionHelper.requestPermissions(this, requestCode, permissions);
  }
}


class MLVBRoomImpl {
  private static final String TAG = MLVBRoomImpl.class.getName();
  private static final String DEFAULT_ROOM = "camera_1";
  private static MLVBRoomImpl instance;
  public V2TXLivePusher mLivePusher;                //直播推流
  private V2TXLivePlayer mLivePlayer;               //直播拉流的视频播放器
  private CordovaInterface cordova;
  private CordovaWebView cordovaWebView;
  private boolean mIsResume;
  private boolean mIsPushing;
  private TXLivePusher mTXLivePusher;
  private boolean mIsPlaying;

  private MLVBRoomImpl(CordovaInterface cordova, CordovaWebView webView) {
    this.cordova = cordova;
    this.cordovaWebView = webView;
    this.initilize();
  }

  public  static MLVBRoomImpl getInstance() {
    return MLVBRoomImpl.instance;
  }
  public static MLVBRoomImpl getInstance(CordovaInterface cordova, CordovaWebView webView) {
    if (MLVBRoomImpl.instance == null) {
      synchronized (MLVBRoomImpl.TAG) {
        if (MLVBRoomImpl.instance == null) {
          MLVBRoomImpl.instance = new MLVBRoomImpl(cordova, webView);
          MLVBRoomImpl.instance.initilize();
        }
      }
    }
    return MLVBRoomImpl.instance;
  }

  private void initilize() {
    TXLiveBase.setConsoleEnabled(true);

    TXLiveBase.getInstance().setLicence(this.cordova.getActivity().getApplicationContext(), MLVBCommonDef.TCGlobalConfig.LICENCE_URL, MLVBCommonDef.TCGlobalConfig.LICENCE_KEY);

    // 短视频licence设置
    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      builder.detectFileUriExposure();
    }
    this.closeAndroidPDialog();
  }

  private TXCloudVideoView videoView;

  public int _R(String defType, String name) {
    Activity activity = cordova.getActivity();
    return activity.getApplication().getResources().getIdentifier(
      name, defType, activity.getApplication().getPackageName());
  }

  /**
      * 在当前 Activity 底部 UI 层注册一个 TXCloudVideoView 以供直播渲染
      */
  private void prepareVideoView() {
    if (videoView != null) return;
    // 通过 layout 文件插入 videoView
    LayoutInflater layoutInflater = LayoutInflater.from(this.cordova.getActivity());
    videoView = (TXCloudVideoView) layoutInflater.inflate(_R("layout", "layout_video"), null);
    // 设置 webView 透明
    videoView.setLayoutParams(new FrameLayout.LayoutParams(
      FrameLayout.LayoutParams.MATCH_PARENT,
      FrameLayout.LayoutParams.MATCH_PARENT
    ));
    ViewGroup rootView = this.cordova.getActivity().findViewById(android.R.id.content);
    // 插入视图
    rootView.addView(videoView);
    videoView.setVisibility(View.VISIBLE);
    View webView = this.cordovaWebView.getView();
    // 设置 webView 透明
    webView.setBackgroundColor(Color.TRANSPARENT);
    // 关闭 webView 的硬件加速（否则不能透明）
    webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
    // 将 webView 提到顶层
    webView.bringToFront();
  }

  /**
   * 销毁 videoView
   */
  private void destroyVideoView() {
    if (videoView == null) return;
    videoView.onDestroy();
    ViewGroup rootView = this.cordova.getActivity().findViewById(android.R.id.content);
    rootView.removeView(videoView);
    videoView = null;

    // 把 webView 变回白色
    View webView = this.cordovaWebView.getView();
    webView.setBackgroundColor(Color.WHITE);
  }

  private void initPuherListener() {
    TXPhoneStateListener mPhoneListener = new TXPhoneStateListener();
    TelephonyManager tm = (TelephonyManager) this.cordova.getActivity().getSystemService(Service.TELEPHONY_SERVICE);
    tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  private void resume() {
    TXLog.i(TAG, "resume: mIsResume -> " + mIsResume);
    if (mIsResume) {
      return;
    }
    if (videoView != null) {
      videoView.onResume();
    }
    mIsResume = true;
  }

  private void pause() {
    TXLog.i(TAG, "pause: mIsResume -> " + mIsResume);
    if (videoView != null) {
      videoView.onPause();
    }
    mIsResume = false;
//    mAudioEffectPanel.pauseBGM();
  }
  /**
   * 电话监听
   */
  private class TXPhoneStateListener extends PhoneStateListener {

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
      super.onCallStateChanged(state, incomingNumber);
      TXLog.i(TAG, "onCallStateChanged: state -> " + state);
      switch (state) {
        case TelephonyManager.CALL_STATE_RINGING:   //电话等待接听
        case TelephonyManager.CALL_STATE_OFFHOOK:   //电话接听
          pause();
          break;
        case TelephonyManager.CALL_STATE_IDLE:      //电话挂机
          resume();
          break;
      }
    }
  }

  private void initTxLivePusher() {
    mLivePusher = new V2TXLivePusherImpl(this.cordova.getActivity(), V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP);
    // 设置默认美颜参数， 美颜样式为光滑，美颜等级 5，美白等级 3，红润等级 2
    mLivePusher.getBeautyManager().setBeautyStyle(TXLiveConstants.BEAUTY_STYLE_SMOOTH);
    mLivePusher.getBeautyManager().setBeautyLevel(5);
    mLivePusher.getBeautyManager().setWhitenessLevel(3);
    mLivePusher.getBeautyManager().setRuddyLevel(2);

    initPuherListener();
  }

  private void initTxLivePlayer() {
    mLivePlayer = new V2TXLivePlayerImpl(this.cordova.getActivity());
    TXLivePlayConfig config = new TXLivePlayConfig();
  }

  private void initTxLivePusherV1() {
    if (mTXLivePusher == null) {
      mTXLivePusher = new TXLivePusher(this.cordova.getActivity());
    }
    TXLivePushConfig config = new TXLivePushConfig();
    config.setFrontCamera(true);
    config.enableScreenCaptureAutoRotate(true);// 是否开启屏幕自适应
    config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
    mTXLivePusher.setConfig(config);
    mTXLivePusher.setBeautyFilter(TXLiveConstants.BEAUTY_STYLE_SMOOTH, 5, 3, 2);
//    mTXLivePushListener = new TXLivePushListenerImpl();
//    mTXLivePusher.setPushListener(mTXLivePushListener);
  }

  public boolean startPlay(String userid, final CallbackContext callbackContext) {
    String playURL = this.createPullUrl(userid);

    Activity activity = this.cordova.getActivity();
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (videoView == null) {
          prepareVideoView();
        }
        if (mLivePlayer == null) {
          initTxLivePlayer();
        }
        videoView.setVisibility(View.VISIBLE);
        mLivePlayer.setRenderView(videoView);
        mLivePlayer.setObserver(new MyPlayerObserver());

        mLivePlayer.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0);
        mLivePlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit);

        /**
         * result返回值：
         * 0 V2TXLIVE_OK; -2 V2TXLIVE_ERROR_INVALID_PARAMETER; -3 V2TXLIVE_ERROR_REFUSED;
         */
        int code = mLivePlayer.startPlay(playURL);
        mLivePlayer.setRenderView(videoView);
        TXLog.i(TAG, String.format("%d", code));
        mIsPlaying = true;
      }
    });

    return true;
  }

  public boolean stopPlay(final CallbackContext callbackContext) {
    if (!mIsPlaying) {
      return true;
    }
    if (mLivePlayer != null) {
      mLivePlayer.setObserver(null);
      mLivePlayer.stopPlay();
    }
    mIsPlaying = false;
    return true;
  }

  private class MyPlayerObserver extends V2TXLivePlayerObserver {

    @Override
    public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
      Log.w(TAG, "[Player] onWarning: player-" + player + " code-" + code + " msg-" + msg + " info-" + extraInfo);
    }

    @Override
    public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
      Log.e(TAG, "[Player] onError: player-" + player + " code-" + code + " msg-" + msg + " info-" + extraInfo);
    }

    @Override
    public void onSnapshotComplete(V2TXLivePlayer v2TXLivePlayer, Bitmap bitmap) {
    }

    @Override
    public void onVideoPlayStatusUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayStatus status, V2TXLiveDef.V2TXLiveStatusChangeReason reason, Bundle bundle) {
      Log.i(TAG, "[Player] onVideoPlayStatusUpdate: player-" + player + ", status-" + status + ", reason-" + reason);
      switch (status) {
        case V2TXLivePlayStatusLoading:
          break;
        case V2TXLivePlayStatusPlaying:
          break;
        default:
          break;
      }
    }

    @Override
    public void onAudioPlayStatusUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayStatus status, V2TXLiveDef.V2TXLiveStatusChangeReason reason, Bundle bundle) {
      Log.i(TAG, "[Player] onAudioPlayStatusUpdate: player-" + player + ", status-" + status + ", reason-" + reason);
      switch (status) {
        case V2TXLivePlayStatusLoading:
          break;
        case V2TXLivePlayStatusPlaying:
          break;
        default:
          break;
      }
    }

    @Override
    public void onPlayoutVolumeUpdate(V2TXLivePlayer player, int volume) {
//            Log.i(TAG, "onPlayoutVolumeUpdate: player-" + player +  ", volume-" + volume);
    }

    @Override
    public void onStatisticsUpdate(V2TXLivePlayer player, V2TXLiveDef.V2TXLivePlayerStatistics statistics) {
      Bundle netStatus = new Bundle();
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH, statistics.width);
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT, statistics.height);
      int appCpu = statistics.appCpu / 10;
      int totalCpu = statistics.systemCpu / 10;
      String strCpu = appCpu + "/" + totalCpu + "%";
      netStatus.putCharSequence(TXLiveConstants.NET_STATUS_CPU_USAGE, strCpu);
      netStatus.putInt(TXLiveConstants.NET_STATUS_NET_SPEED, statistics.videoBitrate + statistics.audioBitrate);
      netStatus.putInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE, statistics.audioBitrate);
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE, statistics.videoBitrate);
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_FPS, statistics.fps);
      netStatus.putInt(TXLiveConstants.NET_STATUS_AUDIO_CACHE, 0);
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_CACHE, 0);
      netStatus.putInt(TXLiveConstants.NET_STATUS_V_SUM_CACHE_SIZE, 0);
      netStatus.putInt(TXLiveConstants.NET_STATUS_V_DEC_CACHE_SIZE, 0);
      netStatus.putString(TXLiveConstants.NET_STATUS_AUDIO_INFO, "");
      Log.d(TAG, "Current status, CPU:" + netStatus.getString(TXLiveConstants.NET_STATUS_CPU_USAGE) +
        ", RES:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) +
        ", SPD:" + netStatus.getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" +
        ", FPS:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) +
        ", ARA:" + netStatus.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" +
        ", VRA:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
    }

  }

  private String createPullUrl(String userId) {
    String pullDomainKey = MLVBCommonDef.TCGlobalConfig.PULL_DOMAIN_KEY;
    String roomID;
    if (userId == null || userId.isEmpty()) {
      roomID = DEFAULT_ROOM;
    } else {
      roomID = userId;
    }
    String pullURL = "rtmp://" + MLVBCommonDef.TCGlobalConfig.PULL_DOMAIN_URL + "/" + MLVBCommonDef.TCGlobalConfig.APP_NAME + "/" + roomID + "?";

    long time = System.currentTimeMillis() / 1000 + MLVBCommonDef.TCGlobalConfig.EXPIRETIME;
    String timeHex = String.format("%x", time).toUpperCase();
    String sig = TCUtils.md5(String.format("%s%s%s", pullDomainKey, roomID, timeHex));
    pullURL += String.format("txSecret=%s&txTime=%s", sig, timeHex);
    return pullURL;
  }

  private String createPushUrl(String userId) {
    String pushDomainKey = MLVBCommonDef.TCGlobalConfig.PUSH_DOMAIN_KEY;
    String roomID;
    if (userId == null || userId.isEmpty()) {
      roomID = DEFAULT_ROOM;
    } else {
      roomID = userId;
    }
    String pushURL = "rtmp://" + MLVBCommonDef.TCGlobalConfig.PUSH_DOMAIN_URL + "/" + MLVBCommonDef.TCGlobalConfig.APP_NAME + "/" + roomID + "?";

    long time = System.currentTimeMillis() / 1000 + MLVBCommonDef.TCGlobalConfig.EXPIRETIME;
    time = 0x612DD29C;
    String timeHex = String.format("%x", time).toUpperCase();
    String sig = TCUtils.md5(String.format("%s%s%s", pushDomainKey, roomID, timeHex));
    pushURL += String.format("txSecret=%s&txTime=%s", sig, timeHex);
    return pushURL;
  }

  public boolean startPush(String userid, final CallbackContext callbackContext) {
    int resultCode = MLVBCommonDef.Constants.PLAY_STATUS_SUCCESS;
    String tRTMPURL = this.createPushUrl(userid);
    if (TextUtils.isEmpty(tRTMPURL) || (!tRTMPURL.trim().toLowerCase().startsWith("rtmp://"))) {
      resultCode = MLVBCommonDef.Constants.PLAY_STATUS_INVALID_URL;
    } else {
      //在主线程开启推流
      Activity activity = this.cordova.getActivity();
      activity.runOnUiThread(new Runnable() {

        @Override
        public void run() {
          if (videoView == null) {
            prepareVideoView();
          }
          View mPusherView = videoView;
          // 显示本地预览的View
          mPusherView.setVisibility(View.VISIBLE);
          initTxLivePusher();
          // 添加播放回调
          mLivePusher.setObserver(new MyPusherObserver());
          // 设置推流分辨率
          mLivePusher.setVideoQuality(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540, V2TXLiveVideoResolutionModePortrait);

          // 是否开启观众端镜像观看
          mLivePusher.setEncoderMirror(true);
          // 是否打开调试信息
//      ((TXCloudVideoView) mPusherView).showLog(true);


          // 是否打开曝光对焦
          mLivePusher.getDeviceManager().enableCameraAutoFocus(true);

          mLivePusher.getAudioEffectManager().enableVoiceEarMonitor(true);
//      // 设置场景
//      setPushScene(mQualityType, mIsEnableAdjustBitrate);
//
//      // 设置声道，设置音频采样率，必须在 TXLivePusher.setVideoQuality 之后，TXLivePusher.startPusher之前设置才能生效
//      setAudioQuality(mAudioQuality);

          // 设置本地预览View
          mLivePusher.setRenderView((TXCloudVideoView) mPusherView);
          mLivePusher.startCamera(true);
          mLivePusher.startMicrophone();
          // 发起推流
          int resultCode = mLivePusher.startPush(tRTMPURL.trim());
          if (resultCode == -5) {
            TXLog.e(TAG,"License Error");
          }

          mIsPushing = true;
        }


      });
    }
    TXLog.i(TAG, "start: mIsResume -> " + mIsResume);
    return true;
  }

  public boolean startPushV1(final CallbackContext callbackContext) {
    int resultCode = MLVBCommonDef.Constants.PLAY_STATUS_SUCCESS;
    String tRTMPURL = this.createPushUrl(null);
    if (TextUtils.isEmpty(tRTMPURL) || (!tRTMPURL.trim().toLowerCase().startsWith("rtmp://"))) {
      resultCode = MLVBCommonDef.Constants.PLAY_STATUS_INVALID_URL;
    } else {
      Activity activity = this.cordova.getActivity();
      activity.runOnUiThread(new Runnable() {

        @Override
        public void run() {
          if (videoView == null) {
            prepareVideoView();
          }
          initTxLivePusher();
          mTXLivePusher.startCameraPreview(videoView);
          if (mTXLivePusher != null) {
            mTXLivePusher.setVideoQuality(5, false, false);
            int ret = mTXLivePusher.startPusher(tRTMPURL.trim());
            if (ret == -5) {
              String msg = "[LiveRoom] 推流失败[license 校验失败]";
              TXCLog.e(TAG, msg);
              return;
            }
            mTXLivePusher.startCameraPreview(videoView);
            mIsPushing = true;
          } else {
            String msg = "[LiveRoom] 推流失败[TXLivePusher未初始化，请确保已经调用startLocalPreview]";
            TXCLog.e(TAG, msg);
          }
        }


      });
    }
    TXLog.i(TAG, "start: mIsResume -> " + mIsResume);
    return true;
  }

  public boolean stopPush(final CallbackContext callbackContext) {
    if (!mIsPushing) {
      return true;
    }
    // 停止本地预览
    mLivePusher.stopCamera();
    // 移除监听
    mLivePusher.setObserver(null);
    // 停止推流
    mLivePusher.stopPush();
    // 隐藏本地预览的View
    this.videoView.setVisibility(View.GONE);
    mIsPushing = false;
    return true;
  }

  private class MyPusherObserver extends V2TXLivePusherObserver {
    @Override
    public void onWarning(int code, String msg, Bundle extraInfo) {
      Log.w(TAG, "[Pusher] onWarning errorCode: " + code + ", msg " + msg);
      if (code == V2TXLiveCode.V2TXLIVE_WARNING_NETWORK_BUSY) {
//        showNetBusyTips();
      }
    }

    @Override
    public void onError(int code, String msg, Bundle extraInfo) {
      Log.e(TAG, "[Pusher] onError: " + msg + ", extraInfo " + extraInfo);
    }

    @Override
    public void onCaptureFirstAudioFrame() {
      Log.i(TAG, "[Pusher] onCaptureFirstAudioFrame");
    }

    @Override
    public void onCaptureFirstVideoFrame() {
      Log.i(TAG, "[Pusher] onCaptureFirstVideoFrame");
    }

    @Override
    public void onMicrophoneVolumeUpdate(int volume) {
    }

    @Override
    public void onPushStatusUpdate(V2TXLiveDef.V2TXLivePushStatus status, String msg, Bundle bundle) {
    }

    @Override
    public void onSnapshotComplete(Bitmap bitmap) {
//      if (mLivePusher.isPushing() == 1) {
//        if (bitmap != null) {
//          saveSnapshotBitmap(bitmap);
//        } else {
//          showToast(R.string.livepusher_screenshot_fail);
//        }
//      } else {
//        showToast(R.string.livepusher_screenshot_fail_push);
//      }
    }

    @Override
    public void onStatisticsUpdate(V2TXLiveDef.V2TXLivePusherStatistics statistics) {
      Bundle netStatus = new Bundle();
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH, statistics.width);
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT, statistics.height);
      int appCpu = statistics.appCpu / 10;
      int totalCpu = statistics.systemCpu / 10;
      String strCpu = appCpu + "/" + totalCpu + "%";
      netStatus.putCharSequence(TXLiveConstants.NET_STATUS_CPU_USAGE, strCpu);
      netStatus.putInt(TXLiveConstants.NET_STATUS_NET_SPEED, statistics.videoBitrate + statistics.audioBitrate);
      netStatus.putInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE, statistics.audioBitrate);
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE, statistics.videoBitrate);
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_FPS, statistics.fps);
      netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_GOP, 5);
      Log.d(TAG, "Current status, CPU:" + netStatus.getString(TXLiveConstants.NET_STATUS_CPU_USAGE) +
        ", RES:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) +
        ", SPD:" + netStatus.getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" +
        ", FPS:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) +
        ", ARA:" + netStatus.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" +
        ", VRA:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
    }
  }

  private void closeAndroidPDialog() {
    try {
      Class       aClass              = Class.forName("android.content.pm.PackageParser$Package");
      Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
      declaredConstructor.setAccessible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      Class  cls            = Class.forName("android.app.ActivityThread");
      Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
      declaredMethod.setAccessible(true);
      Object activityThread         = declaredMethod.invoke(null);
      Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
      mHiddenApiWarningShown.setAccessible(true);
      mHiddenApiWarningShown.setBoolean(activityThread, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean getVersion(CallbackContext callbackContext) {
    String sdkver = TXLiveBase.getSDKVersionStr();
    callbackContext.success(sdkver);
    return true;
  }
}


class MLVBaction {
  private static final String TAG = MLVBaction.class.getName();
  private static MLVBaction instance;
  protected MLVBCommonDef.IMLVBLiveRoomListener mListener = null;
  private CordovaInterface cordova;
  private CordovaWebView cordovaWebView;
  private ViewGroup rootView;
  private WebView webView;
  private WebSettings settings;
  private CallbackContext callbackContext;

  private TXCloudVideoView videoView = null;
  protected TXLivePusher mTXLivePusher;
  protected TXLivePushListenerImpl mTXLivePushListener;
  protected String mSelfPushUrl;
  protected String mSelfAccelerateURL;
  private TXLivePlayConfig mTXLivePlayConfig = null;
  protected TXLivePlayer mTXLivePlayer;
  protected Handler mListenerHandler = null;

  protected MLVBCommonDef.LoginInfo mSelfAccountInfo;
  protected StreamMixturer mStreamMixturer; //混流类
  protected String mCurrRoomID;
  protected int mRoomStatusCode = 0;

  public static MLVBaction getInstance() {
    if (MLVBaction.instance == null) {
      synchronized (MLVBaction.TAG) {
        if (MLVBaction.instance == null) {
          MLVBaction.instance = new MLVBaction();
        }
      }
    }
    return MLVBaction.instance;
  }

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    this.cordovaWebView = webView;
    this.cordova = cordova;
    Activity activity = cordova.getActivity();
    this.rootView = (ViewGroup) activity.findViewById(android.R.id.content);
    this.webView = (WebView) rootView.getChildAt(0);
    this.createMLVBPlatform(activity.getApplication());
  }

  private void createMLVBPlatform(Context context) {
    if (context == null) {
      throw new InvalidParameterException("MLVB初始化错误：context不能为空！");
    }

    // 必须：初始化 LiteAVSDK Licence。 用于直播推流鉴权。
    TXLiveBase.getInstance().setLicence(context, MLVBCommonDef.TCGlobalConfig.LICENCE_URL, MLVBCommonDef.TCGlobalConfig.LICENCE_KEY);

    Context mAppContext = context;
    mListenerHandler = new Handler(mAppContext.getMainLooper());
    mStreamMixturer = new StreamMixturer();

    mTXLivePlayConfig = new TXLivePlayConfig();
    mTXLivePlayer = new TXLivePlayer(context);
    mTXLivePlayConfig.setAutoAdjustCacheTime(true);
    mTXLivePlayConfig.setMaxAutoAdjustCacheTime(2.0f);
    mTXLivePlayConfig.setMinAutoAdjustCacheTime(2.0f);
    mTXLivePlayer.setConfig(mTXLivePlayConfig);
    mTXLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
    mTXLivePlayer.setPlayListener(new ITXLivePlayListener() {
      @Override
      public void onPlayEvent(final int event, final Bundle param) {
        if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
          String msg = "[LivePlayer] 拉流失败[" + param.getString(TXLiveConstants.EVT_DESCRIPTION) + "]";
          TXCLog.e(TAG, msg);
          callbackOnThread(mListener, "onDebugLog", msg);
          callbackOnThread(mListener, "onError", event, msg, param);
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
          int width = param.getInt(TXLiveConstants.EVT_PARAM1, 0);
          int height = param.getInt(TXLiveConstants.EVT_PARAM2, 0);
          if (width > 0 && height > 0) {
            float ratio = (float) height / width;
            //pc上混流后的宽高比为4:5，这种情况下填充模式会把左右的小主播窗口截掉一部分，用适应模式比较合适
            if (ratio > 1.3f) {
              mTXLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
            } else {
              mTXLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
            }
          }
        }
      }

      @Override
      public void onNetStatus(Bundle status) {

      }
    });
  }



  /**
   * 在当前 Activity 底部 UI 层注册一个 TXCloudVideoView 以供直播渲染
   */
  private void prepareVideoView() {
    if (videoView != null) return;
    // 通过 layout 文件插入 videoView
    LayoutInflater layoutInflater = LayoutInflater.from(this.cordova.getActivity());
    videoView = (TXCloudVideoView) layoutInflater.inflate(_R("layout", "layout_video"), null);
    // 设置 webView 透明
    videoView.setLayoutParams(new FrameLayout.LayoutParams(
      FrameLayout.LayoutParams.FILL_PARENT,
      FrameLayout.LayoutParams.FILL_PARENT
    ));
    // 插入视图
    rootView.addView(videoView);
    videoView.setVisibility(View.VISIBLE);
    // 设置 webView 透明
    webView.setBackgroundColor(Color.TRANSPARENT);
    // 关闭 webView 的硬件加速（否则不能透明）
    webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
    // 将 webView 提到顶层
    webView.bringToFront();
  }

  /**
   * 销毁 videoView
   */
  private void destroyVideoView() {
    if (videoView == null) return;
    videoView.onDestroy();
    rootView.removeView(videoView);
    videoView = null;
    // 把 webView 变回白色
    webView.setBackgroundColor(Color.WHITE);
  }

  /**
   * 返回 MLVB SDK 版本字符串
   *
   * @param callbackContext
   * @return
   */
  public boolean getVersion(final CallbackContext callbackContext) {
    String sdkver = TXLiveBase.getSDKVersionStr();
    callbackContext.success(sdkver);
    return true;
  }



  private String createPushUrl(String userId) {
    String pushDomainKey = MLVBCommonDef.TCGlobalConfig.PUSH_DOMAIN_KEY;
    String roomID;
    if (userId == null || userId.isEmpty()) {
      roomID = "RoomDefault";
    } else {
      roomID = userId;
    }
    String pushURL = "rtmp://" + MLVBCommonDef.TCGlobalConfig.PUSH_DOMAIN_URL + "/" + MLVBCommonDef.TCGlobalConfig.APP_NAME + "/" + roomID + "?";

    long time = System.currentTimeMillis() / 1000 + MLVBCommonDef.TCGlobalConfig.EXPIRETIME;
    time = 0x612DD29C;
    String timeHex = String.format("%x", time).toUpperCase();
    String sig = TCUtils.md5(String.format("%s%s%s", pushDomainKey, roomID, timeHex));
    pushURL += String.format("txSecret=%s&txTime=%s", sig, timeHex);
    return pushURL;
  }

  public boolean startPush(final CallbackContext callbackContext) {
    String url = this.createPushUrl(null);
    MLVBCommonDef.IMLVBLiveRoomListener.CreateRoomCallback callback = new MLVBCommonDef.IMLVBLiveRoomListener.CreateRoomCallback() {
      @Override
      public void onSuccess(String roomId) {
        Log.w(TAG, String.format("创建直播间%s成功", roomId));
      }

      @Override
      public void onError(int errCode, String e) {
        Log.w(TAG, String.format("创建直播间错误, code=%s,error=%s", errCode, e));
      }
    };

    return this.startPush(url, 5,  new StandardCallback() {
      @Override
      public void onError(int errCode, String errInfo) {
        callbackOnThread(callback, "onError", errCode, errInfo);
      }

      @Override
      public void onSuccess() {
        //推流过程中，可能会重复收到PUSH_EVT_PUSH_BEGIN事件，onSuccess可能会被回调多次，如果已经创建的房间，直接返回
        if (mCurrRoomID != null && mCurrRoomID.length() > 0) {
          return;
        }

        if (mTXLivePusher != null) {
          TXLivePushConfig config = mTXLivePusher.getConfig();
          config.setVideoEncodeGop(2);
          mTXLivePusher.setConfig(config);
        }
        callbackOnThread(callback, "onSuccess", mCurrRoomID);
      }
    });
  }

  protected void initLivePusher(boolean frontCamera) {
    if (mTXLivePusher == null) {
      mTXLivePusher = new TXLivePusher(this.cordova.getActivity().getApplicationContext());
    }
    TXLivePushConfig config = new TXLivePushConfig();
    config.setFrontCamera(frontCamera);
    config.enableScreenCaptureAutoRotate(true);// 是否开启屏幕自适应
    config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
    mTXLivePusher.setConfig(config);
    mTXLivePusher.setBeautyFilter(TXLiveConstants.BEAUTY_STYLE_SMOOTH, 5, 3, 2);
    mTXLivePushListener = new TXLivePushListenerImpl();
    mTXLivePusher.setPushListener(mTXLivePushListener);
  }

  protected void unInitLivePusher() {
    if (mTXLivePusher != null) {
      mSelfPushUrl = "";
      mTXLivePushListener = null;
      mTXLivePusher.setPushListener(null);
      mTXLivePusher.stopCameraPreview(true);
      mTXLivePusher.stopPusher();
      mTXLivePusher = null;
    }
  }

  protected boolean startPush(final String url, final int videoQuality, final StandardCallback callback){
    if (mTXLivePusher != null) {
      if (callbackContext != null) {
        callbackContext.error("10002");
      }
      return false;
    }
    Activity activity = this.cordova.getActivity();
    //在主线程开启推流
    Handler handler = new Handler(activity.getApplicationContext().getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        prepareVideoView();
        // 开始推流
        mTXLivePusher = new TXLivePusher(cordova.getActivity().getApplicationContext());
        TXLivePushConfig mLivePushConfig = new TXLivePushConfig();
        mLivePushConfig.setFrontCamera(true);
        mLivePushConfig.enableScreenCaptureAutoRotate(true);// 是否开启屏幕自适应
        mLivePushConfig.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
        mTXLivePusher.setConfig(mLivePushConfig);
        mTXLivePusher.setBeautyFilter(TXLiveConstants.BEAUTY_STYLE_SMOOTH, 5, 3, 2);
        mTXLivePushListener = new TXLivePushListenerImpl();
        mTXLivePusher.setPushListener(mTXLivePushListener);
        if (mTXLivePusher != null && mTXLivePushListener != null) {
          mTXLivePushListener.setCallback(callback);
          mTXLivePusher.setVideoQuality(videoQuality, false, false);
          int ret = mTXLivePusher.startPusher(url);
          if (ret == -5) {
            String msg = "[LiveRoom] 推流失败[license 校验失败]";
            TXCLog.e(TAG, msg);
            if (callback != null) callback.onError(MLVBCommonDef.LiveRoomErrorCode.ERROR_LICENSE_INVALID, msg);
            stopPush(callbackContext);
          }
          // 将视频绑定到 videoView
          mTXLivePusher.startCameraPreview(videoView);
        } else {
          String msg = "[LiveRoom] 推流失败[TXLivePusher未初始化，请确保已经调用startLocalPreview]";
          TXCLog.e(TAG, msg);
          if (callback != null) callback.onError(MLVBCommonDef.LiveRoomErrorCode.ERROR_PUSH, msg);
        }
      }
    });
    return true;
  }

  /**
   * 停止推流，并且注销 mLivePusher 对象
   *
   * @param callbackContext
   * @return
   */
  public boolean stopPush(final CallbackContext callbackContext) {
    if (mTXLivePusher == null) {
      if (callbackContext != null) {
        callbackContext.error("10003");
      }
      return false;
    }
    Activity activity = cordova.getActivity();
    activity.runOnUiThread(new Runnable() {
      public void run() {
        // 停止摄像头预览
        mTXLivePusher.stopCameraPreview(true);
        // 停止推流
        mTXLivePusher.stopPusher();
        // 解绑 Listener
        mTXLivePusher.setPushListener(null);
        // 移除 pusher 引用
        mTXLivePusher = null;
        // 销毁 videoView
        destroyVideoView();
      }
    });
    return true;
  }

  /**
   * 开始播放，在垫底的 videoView 显示视频
   * 会在当前对象上下文注册一个 TXLivePlayer
   *
   * @param url             播放URL
   * @param playType        播放类型，参见 mlvb.js 相关的枚举定义
   * @param callbackContext
   * @return
   */
  public boolean startPlay(final String url, final int playType, final CallbackContext callbackContext) {
    if (mTXLivePlayer != null) {
      callbackContext.error("10004");
      return false;
    }
    Activity activity = cordova.getActivity();
    // 准备 videoView，没有的话生成
    activity.runOnUiThread(new Runnable() {
      public void run() {
        prepareVideoView();
        // 开始推流
        mTXLivePlayer = new TXLivePlayer(activity);
        TXLivePushConfig mLivePushConfig = new TXLivePushConfig();
        // 将视频绑定到 videoView
        mTXLivePlayer.setPlayerView(videoView);
        mTXLivePlayer.startPlay(url, playType);
      }
    });
    return true;
  }

  /**
   * 停止推流，并且注销 mLivePlay 对象
   *
   * @param callbackContext
   * @return
   */
  public boolean stopPlay(final CallbackContext callbackContext) {
    if (mTXLivePlayer == null) {
      callbackContext.error("10005");
      return false;
    }
    Activity activity = cordova.getActivity();
    activity.runOnUiThread(new Runnable() {
      public void run() {
        // 停止播放
        mTXLivePlayer.stopPlay(true);
        // 销毁 videoView
        destroyVideoView();
        // 移除 pusher 引用
        mTXLivePlayer = null;
      }
    });
    return true;
  }

  public String jsonEncode(Object obj) {
    Gson gson = new Gson();
    return gson.toJson(obj);
  }

  public int _R(String defType, String name) {
    Activity activity = cordova.getActivity();
    return activity.getApplication().getResources().getIdentifier(
      name, defType, activity.getApplication().getPackageName());
  }

  public void setVideoQuality(int quality, boolean adjustBitrate, boolean adjustResolution) {
    this.mTXLivePusher.setVideoQuality(quality, adjustBitrate, adjustResolution);
  }

  //    public static void printViewHierarchy(ViewGroup vg, String prefix) {
//        for (int i = 0; i < vg.getChildCount(); i++) {
//            View v = vg.getChildAt(i);
//            String desc = prefix + " | " + "[" + i + "/" + (vg.getChildCount() - 1) + "] " + v.getClass().getSimpleName() + " " + v.getId();
//            Log.v("x", desc);
//
//            if (v instanceof ViewGroup) {
//                printViewHierarchy((ViewGroup) v, desc);
//            }
//        }
//    }

  class TXLivePushListenerImpl implements ITXLivePushListener {
    private StandardCallback mCallback = null;

    public void setCallback(StandardCallback callback) {
      mCallback = callback;
    }

    @Override
    public void onPushEvent(final int event, final Bundle param) {
      if (event == TXLiveConstants.PUSH_EVT_PUSH_BEGIN) {
        TXCLog.d(TAG, "推流成功");
        callbackOnThread(mCallback, "onSuccess");
      } else if (event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL) {
        String msg = "[LivePusher] 推流失败[打开摄像头失败]";
        TXCLog.e(TAG, msg);
        callbackOnThread(mCallback, "onError", event, msg);
      } else if (event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL) {
        String msg = "[LivePusher] 推流失败[打开麦克风失败]";
        TXCLog.e(TAG, msg);
        callbackOnThread(mCallback, "onError", event, msg);
      } else if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT || event == TXLiveConstants.PUSH_ERR_INVALID_ADDRESS) {
        String msg = "[LivePusher] 推流失败[网络断开]";
        TXCLog.e(TAG, msg);
        callbackOnThread(mCallback, "onError", event, msg);
      } else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
        String msg = "[LivePusher] 推流失败[录屏启动失败]";
        TXCLog.e(TAG, msg);
        callbackOnThread(mCallback, "onError", event, msg);
      }
    }

    @Override
    public void onNetStatus(Bundle status) {

    }
  }

  private class PlayerItem {
    public TXCloudVideoView view;
    public MLVBCommonDef.AnchorInfo anchorInfo;
    public TXLivePlayer player;

    public PlayerItem(TXCloudVideoView view, MLVBCommonDef.AnchorInfo anchorInfo, TXLivePlayer player) {
      this.view = view;
      this.anchorInfo = anchorInfo;
      this.player = player;
    }

    public void resume() {
      this.player.resume();
    }

    public void pause() {
      this.player.pause();
    }

    public void destroy() {
      this.player.stopPlay(true);
      this.view.onDestroy();
    }
  }

  protected class CommonJson<T> {
    public String cmd;
    public T data;

    public CommonJson() {
    }
  }

  private class JoinAnchorRequest {
    public String type;
    public String roomID;
    public String userID;
    public String userName;
    public String userAvatar;
    public String reason;
    public long timestamp;
  }

  private class JoinAnchorResponse {
    public String type;
    public String roomID;
    public String result;
    public String reason;
    public long timestamp;
  }

  private class KickoutResponse {
    public String type;
    public String roomID;
    public long timestamp;
  }

  private class PKRequest {
    public String type;
    public String action;
    public String roomID;
    public String userID;
    public String userName;
    public String userAvatar;
    public String accelerateURL;
    public long timestamp;
  }

  private class PKResponse {
    public String type;
    public String roomID;
    public String result;
    public String reason;
    public String accelerateURL;
    public long timestamp;
  }

  protected class CustomMessage {
    public String userName;
    public String userAvatar;
    public String cmd;
    public String msg;
  }

  public interface StandardCallback {
    /**
     * @param errCode 错误码
     * @param errInfo 错误信息
     */
    void onError(int errCode, String errInfo);

    void onSuccess();
  }

  protected interface UpdateAnchorsCallback {
    void onUpdateAnchors(int errcode, List<MLVBCommonDef.AnchorInfo> addAnchors, List<MLVBCommonDef.AnchorInfo> delAnchors, HashMap<String, MLVBCommonDef.AnchorInfo> mergedAnchors, MLVBCommonDef.AnchorInfo roomCreator);
  }

  private class StreamMixturer {
    private String mMainStreamId = "";
    private String mPKStreamId = "";
    private Vector<String> mSubStreamIds = new java.util.Vector<String>();
    private int mMainStreamWidth = 540;
    private int mMainStreamHeight = 960;

    public StreamMixturer() {

    }

    public void setMainVideoStream(String streamUrl) {
      mMainStreamId = getStreamIDByStreamUrl(streamUrl);

      Log.e(TAG, "MergeVideoStream: setMainVideoStream " + mMainStreamId);
    }

    public void setMainVideoStreamResolution(int width, int height) {
      if (width > 0 && height > 0) {
        mMainStreamWidth = width;
        mMainStreamHeight = height;
      }
    }

    public void addSubVideoStream(String streamUrl) {
      if (mSubStreamIds.size() > 3) {
        return;
      }

      String streamId = getStreamIDByStreamUrl(streamUrl);

      Log.e(TAG, "MergeVideoStream: addSubVideoStream " + streamId);

      if (streamId == null || streamId.length() == 0) {
        return;
      }

      for (String item : mSubStreamIds) {
        if (item.equalsIgnoreCase(streamId)) {
          return;
        }
      }

      mSubStreamIds.add(streamId);
      sendStreamMergeRequest(5);
    }

    public void delSubVideoStream(String streamUrl) {
      String streamId = getStreamIDByStreamUrl(streamUrl);

      Log.e(TAG, "MergeVideoStream: delSubVideoStream " + streamId);

      boolean bExist = false;
      for (String item : mSubStreamIds) {
        if (item.equalsIgnoreCase(streamId)) {
          bExist = true;
          break;
        }
      }

      if (bExist == true) {
        mSubStreamIds.remove(streamId);
        sendStreamMergeRequest(1);
      }
    }

    public void addPKVideoStream(String streamUrl) {
      mPKStreamId = getStreamIDByStreamUrl(streamUrl);
      if (mMainStreamId == null || mMainStreamId.length() == 0 || mPKStreamId == null || mPKStreamId.length() == 0) {
        return;
      }

      Log.e(TAG, "MergeVideoStream: addPKVideoStream " + mPKStreamId);

      final JSONObject requestParam = createPKRequestParam();
      if (requestParam == null) {
        return;
      }

      internalSendRequest(5, true, requestParam);
    }

    public void delPKVideoStream(String streamUrl) {
      mPKStreamId = null;
      if (mMainStreamId == null || mMainStreamId.length() == 0) {
        return;
      }

      String streamId = getStreamIDByStreamUrl(streamUrl);
      Log.e(TAG, "MergeVideoStream: delPKStream");

      final JSONObject requestParam = createPKRequestParam();
      if (requestParam == null) {
        return;
      }

      internalSendRequest(1, true, requestParam);
    }

    public void resetMergeState() {
      Log.e(TAG, "MergeVideoStream: resetMergeState");

      mSubStreamIds.clear();
      mMainStreamId = null;
      mPKStreamId = null;
      mMainStreamWidth = 540;
      mMainStreamHeight = 960;
    }

    private void sendStreamMergeRequest(final int retryCount) {
      if (mMainStreamId == null || mMainStreamId.length() == 0) {
        return;
      }

      final JSONObject requestParam = createRequestParam();
      if (requestParam == null) {
        return;
      }

      internalSendRequest(retryCount, true, requestParam);
    }

    private void internalSendRequest(final int retryIndex, final boolean runImmediately, final JSONObject requestParam) {
      new Thread() {
        @Override
        public void run() {
          if (runImmediately == false) {
            try {
              sleep(2000, 0);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          String streamsInfo = "mainStream: " + mMainStreamId;
          for (int i = 0; i < mSubStreamIds.size(); ++i) {
            streamsInfo = streamsInfo + " subStream" + i + ": " + mSubStreamIds.get(i);
          }

          Log.e(TAG, "MergeVideoStream: send request, " + streamsInfo + " retryIndex: " + retryIndex + "    " + requestParam.toString());
        }
      }.start();
    }

    private JSONObject createRequestParam() {

      JSONObject requestParam = null;

      try {
        // input_stream_list
        JSONArray inputStreamList = new JSONArray();

        // 大主播
        {
          JSONObject layoutParam = new JSONObject();
          layoutParam.put("image_layer", 1);

          JSONObject mainStream = new JSONObject();
          mainStream.put("input_stream_id", mMainStreamId);
          mainStream.put("layout_params", layoutParam);

          inputStreamList.put(mainStream);
        }

        int subWidth = 160;
        int subHeight = 240;
        int offsetHeight = 90;
        if (mMainStreamWidth < 540 || mMainStreamHeight < 960) {
          subWidth = 120;
          subHeight = 180;
          offsetHeight = 60;
        }
        int subLocationX = mMainStreamWidth - subWidth;
        int subLocationY = mMainStreamHeight - subHeight - offsetHeight;

        // 小主播
        int layerIndex = 0;
        for (String item : mSubStreamIds) {
          JSONObject layoutParam = new JSONObject();
          layoutParam.put("image_layer", layerIndex + 2);
          layoutParam.put("image_width", subWidth);
          layoutParam.put("image_height", subHeight);
          layoutParam.put("location_x", subLocationX);
          layoutParam.put("location_y", subLocationY - layerIndex * subHeight);

          JSONObject subStream = new JSONObject();
          subStream.put("input_stream_id", item);
          subStream.put("layout_params", layoutParam);

          inputStreamList.put(subStream);
          ++layerIndex;
        }

        // para
        JSONObject para = new JSONObject();
        para.put("app_id", "");
        para.put("interface", "mix_streamv2.start_mix_stream_advanced");
        para.put("mix_stream_session_id", mMainStreamId);
        para.put("output_stream_id", mMainStreamId);
        para.put("input_stream_list", inputStreamList);

        // interface
        JSONObject interfaceObj = new JSONObject();
        interfaceObj.put("interfaceName", "Mix_StreamV2");
        interfaceObj.put("para", para);

        // requestParam
        requestParam = new JSONObject();
        requestParam.put("timestamp", System.currentTimeMillis() / 1000);
        requestParam.put("eventId", System.currentTimeMillis() / 1000);
        requestParam.put("interface", interfaceObj);
      } catch (Exception e) {
        e.printStackTrace();
      }

      return requestParam;
    }

    private JSONObject createPKRequestParam() {

      if (mMainStreamId == null || mMainStreamId.length() == 0) {
        return null;
      }

      JSONObject requestParam = null;

      try {
        // input_stream_list
        JSONArray inputStreamList = new JSONArray();

        if (mPKStreamId != null && mPKStreamId.length() > 0) {
          // 画布
          {
            JSONObject layoutParam = new JSONObject();
            layoutParam.put("image_layer", 1);
            layoutParam.put("input_type", 3);
            layoutParam.put("image_width", 720);
            layoutParam.put("image_height", 640);

            JSONObject canvasStream = new JSONObject();
            canvasStream.put("input_stream_id", mMainStreamId);
            canvasStream.put("layout_params", layoutParam);

            inputStreamList.put(canvasStream);
          }

          // mainStream
          {
            JSONObject layoutParam = new JSONObject();
            layoutParam.put("image_layer", 2);
            layoutParam.put("image_width", 360);
            layoutParam.put("image_height", 640);
            layoutParam.put("location_x", 0);
            layoutParam.put("location_y", 0);

            JSONObject mainStream = new JSONObject();
            mainStream.put("input_stream_id", mMainStreamId);
            mainStream.put("layout_params", layoutParam);

            inputStreamList.put(mainStream);
          }

          // subStream
          {
            JSONObject layoutParam = new JSONObject();
            layoutParam.put("image_layer", 3);
            layoutParam.put("image_width", 360);
            layoutParam.put("image_height", 640);
            layoutParam.put("location_x", 360);
            layoutParam.put("location_y", 0);

            JSONObject mainStream = new JSONObject();
            mainStream.put("input_stream_id", mPKStreamId);
            mainStream.put("layout_params", layoutParam);

            inputStreamList.put(mainStream);
          }
        } else {
          JSONObject layoutParam = new JSONObject();
          layoutParam.put("image_layer", 1);

          JSONObject canvasStream = new JSONObject();
          canvasStream.put("input_stream_id", mMainStreamId);
          canvasStream.put("layout_params", layoutParam);

          inputStreamList.put(canvasStream);
        }

        // para
        JSONObject para = new JSONObject();
        para.put("app_id", "");
        para.put("interface", "mix_streamv2.start_mix_stream_advanced");
        para.put("mix_stream_session_id", mMainStreamId);
        para.put("output_stream_id", mMainStreamId);
        para.put("input_stream_list", inputStreamList);

        // interface
        JSONObject interfaceObj = new JSONObject();
        interfaceObj.put("interfaceName", "Mix_StreamV2");
        interfaceObj.put("para", para);

        // requestParam
        requestParam = new JSONObject();
        requestParam.put("timestamp", System.currentTimeMillis() / 1000);
        requestParam.put("eventId", System.currentTimeMillis() / 1000);
        requestParam.put("interface", interfaceObj);
      } catch (Exception e) {
        e.printStackTrace();
      }

      return requestParam;
    }

    private String getStreamIDByStreamUrl(String strStreamUrl) {
      if (strStreamUrl == null || strStreamUrl.length() == 0) {
        return null;
      }

      //推流地址格式：rtmp://8888.livepush.myqcloud.com/path/8888_test_12345_test?txSecret=aaaa&txTime=bbbb
      //拉流地址格式：rtmp://8888.liveplay.myqcloud.com/path/8888_test_12345_test
      //            http://8888.liveplay.myqcloud.com/path/8888_test_12345_test.flv
      //            http://8888.liveplay.myqcloud.com/path/8888_test_12345_test.m3u8


      String subString = strStreamUrl;

      {
        //1 截取第一个 ？之前的子串
        int index = subString.indexOf("?");
        if (index != -1) {
          subString = subString.substring(0, index);
        }
        if (subString == null || subString.length() == 0) {
          return null;
        }
      }

      {
        //2 截取最后一个 / 之后的子串
        int index = subString.lastIndexOf("/");
        if (index != -1) {
          subString = subString.substring(index + 1);
        }

        if (subString == null || subString.length() == 0) {
          return null;
        }
      }

      {
        //3 截取第一个 . 之前的子串
        int index = subString.indexOf(".");
        if (index != -1) {
          subString = subString.substring(0, index);
        }
        if (subString == null || subString.length() == 0) {
          return null;
        }
      }

      return subString;
    }
  }

  private void callbackOnThread(final Runnable runnable) {
    if (runnable == null) {
      return;
    }
    mListenerHandler.post(new Runnable() {
      @Override
      public void run() {
        runnable.run();
      }
    });
  }

  private void callbackOnThread(final Object object, final String methodName, final Object... args) {
    if (object == null || methodName == null || methodName.length() == 0) {
      return;
    }
    mListenerHandler.post(new Runnable() {
      @Override
      public void run() {
        Class objClass = object.getClass();
        while (objClass != null) {
          Method[] methods = objClass.getDeclaredMethods();
          for (Method method : methods) {
            if (method.getName() == methodName) {
              try {
                method.invoke(object, args);
              } catch (IllegalAccessException e) {
                e.printStackTrace();
              } catch (InvocationTargetException e) {
                e.printStackTrace();
              }
              return;
            }
          }
          objClass = objClass.getSuperclass();
        }
      }
    });
  }

}
