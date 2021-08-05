package com.qcloud.cordova.mlvb;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
  private final static String TAG = "TencentMLVB";
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

    ApplicationInfo ai = null;
    try {
      ai = cordova.getActivity().getPackageManager().getApplicationInfo(cordova.getActivity().getPackageName(), PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    Bundle bundle = ai.metaData;
    if (bundle.containsKey("mlvb.license.url")) {
      MLVBCommonDef.TCGlobalConfig.LICENCE_URL = bundle.getString("mlvb.license.url");
    }

    if (bundle.containsKey("mlvb.license.key")) {
      MLVBCommonDef.TCGlobalConfig.LICENCE_KEY = bundle.getString("mlvb.license.key");
    }

    MLVBRoomImplV1.getInstance(cordova, webView);

  }

  public void onRequestPermissionResult(int requestCode, String[] permissions,
                                        int[] grantResults) throws JSONException {
    //Log.d(TAG, "---> onRequestPermissionResult");

    synchronized (TAG) {
      TAG.notify();
    }
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
      synchronized (TAG) {
        this.requestPermissions(0);
        try {
          TAG.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      if (!hasPermisssion()) {
        callbackContext.error("请允许权限申请");
        return false;
      }
    }


    this.callbackContext = callbackContext;
    MLVBRoomImpl mlvbInstance = MLVBRoomImplV1.getInstance();
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
  private static final String DEFAULT_ROOM = "room1";
  private static MLVBRoomImpl instance;
  public V2TXLivePusher mLivePusher;                //直播推流
  private V2TXLivePlayer mLivePlayer;               //直播拉流的视频播放器
  private CordovaInterface cordova;
  private CordovaWebView cordovaWebView;
  private boolean mIsResume;
  private boolean mIsPushing;
  private TXLivePusher mTXLivePusher;
  private boolean mIsPlaying;

  protected MLVBRoomImpl() {
  }

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
//    //自动模式
//    mLivePlayer.setCacheParams(1.0f, 5.0f);
    //极速模式
    mLivePlayer.setCacheParams(1.0f, 1.0f);
    mLivePlayer.setProperty("setPlayURLType", TXLivePlayer.PLAY_TYPE_LIVE_FLV);
//    //流畅模式
//    mLivePlayer.setCacheParams(5.0f, 5.0f);
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
    final String playURL = userid;

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
        String newUrl = playURL;
        if (playURL == null || playURL.isEmpty() || playURL.equals("null")) {
          newUrl = createPullUrl(null);
          newUrl = "http://livetest2.homei-life.com/live/room1.flv?txSecret=d56ba4e50d522b9fe098be5c0e9a2f22&txTime=7F461120";
          //newUrl = "webrtc://livetest2.homei-life.com/live/room1?txSecret=d56ba4e50d522b9fe098be5c0e9a2f22&txTime=7F461120";
        }
        int code = mLivePlayer.startPlay(newUrl);
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
    videoView.setVisibility(View.INVISIBLE);
    videoView.setBackgroundColor(Color.WHITE);
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
    // String tRTMPURL = this.createPushUrl(userid);
    String tRTMPURL = userid;
    if (TextUtils.isEmpty(tRTMPURL) || (!tRTMPURL.trim().toLowerCase().startsWith("rtmp://"))) {
      tRTMPURL = this.createPushUrl(null);
      tRTMPURL = "rtmp://144681.livepush.myqcloud.com/live/room1?txSecret=abf2f6a4c5f858cf2c0fc51ebff71c63&txTime=82DFB501";
    }
    //在主线程开启推流
    Activity activity = this.cordova.getActivity();
    String finalTRTMPURL = tRTMPURL;
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
        mLivePusher.setVideoQuality(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360, V2TXLiveVideoResolutionModePortrait);

//          // 是否开启观众端镜像观看
//          mLivePusher.setEncoderMirror(true);
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
        int resultCode = mLivePusher.startPush(finalTRTMPURL);
        if (resultCode == -5) {
          TXLog.e(TAG,"License Error");
        }

        mIsPushing = true;
      }


    });
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
    this.videoView.setVisibility(View.INVISIBLE);
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
      @SuppressLint("SoonBlockedPrivateApi") Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
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

/**
 * tencent mlvb v1
 */
class MLVBRoomImplV1 extends MLVBRoomImpl {
  private static final String TAG = MLVBRoomImplV1.class.getName();
  private static final String DEFAULT_ROOM = "room1";
  private static MLVBRoomImplV1 instance;
  public TXLivePusher mLivePusher;                //直播推流
  private TXLivePlayer mLivePlayer;               //直播拉流的视频播放器
  private CordovaInterface cordova;
  private CordovaWebView cordovaWebView;
  private boolean mIsResume;
  private boolean mIsPushing;
  private TXLivePusher mTXLivePusher;
  private boolean mIsPlaying;
  private TXLivePushListenerImpl mTXLivePushListener;

  private MLVBRoomImplV1() {
    super();
  }

  private MLVBRoomImplV1(CordovaInterface cordova, CordovaWebView webView) {
    this.cordova = cordova;
    this.cordovaWebView = webView;
    mListenerHandler = new Handler(cordova.getActivity().getApplicationContext().getMainLooper());
    this.initilize();
  }

  public  static MLVBRoomImplV1 getInstance() {
    return MLVBRoomImplV1.instance;
  }
  public static MLVBRoomImplV1 getInstance(CordovaInterface cordova, CordovaWebView webView) {
    if (MLVBRoomImplV1.instance == null) {
      synchronized (TAG) {
        if (MLVBRoomImplV1.instance == null) {
          MLVBRoomImplV1.instance = new MLVBRoomImplV1(cordova, webView);
          MLVBRoomImplV1.instance.initilize();
        }
      }
    }
    return MLVBRoomImplV1.instance;
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


  private void detachVideoView() {
    Activity activity = this.cordova.getActivity();
    activity.runOnUiThread( new Runnable() {
      @Override
      public void run() {
        videoView.setVisibility(View.GONE);

        // 把 webView 变回白色
        View webView = cordovaWebView.getView();
        webView.setBackgroundColor(Color.WHITE);
      }
    });

  }
  /**
   * 在当前 Activity 底部 UI 层注册一个 TXCloudVideoView 以供直播渲染
   */
  private void prepareVideoView() {
    if (videoView == null) {
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
    }

    videoView.setVisibility(View.VISIBLE);
    View webView = this.cordovaWebView.getView();
    // 设置 webView 透明
    webView.setBackgroundColor(Color.TRANSPARENT);
    // 关闭 webView 的硬件加速（否则不能透明）
//    webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
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

  public int _R(String defType, String name) {
    Activity activity = cordova.getActivity();
    return activity.getApplication().getResources().getIdentifier(
      name, defType, activity.getApplication().getPackageName());
  }

//  private void initTxLivePusher() {
//    mLivePusher = new (this.cordova.getActivity());
//    // 设置默认美颜参数， 美颜样式为光滑，美颜等级 5，美白等级 3，红润等级 2
//    mLivePusher.getBeautyManager().setBeautyStyle(TXLiveConstants.BEAUTY_STYLE_SMOOTH);
//    mLivePusher.getBeautyManager().setBeautyLevel(5);
//    mLivePusher.getBeautyManager().setWhitenessLevel(3);
//    mLivePusher.getBeautyManager().setRuddyLevel(2);
//
//    initPuherListener();
//  }

  private void initTxLivePlayer() {
    TXLivePlayConfig mTXLivePlayConfig = new TXLivePlayConfig();
    mTXLivePlayConfig.setAutoAdjustCacheTime(true);
    mTXLivePlayConfig.setMaxAutoAdjustCacheTime(1.0f);
    mTXLivePlayConfig.setMinAutoAdjustCacheTime(1.0f);
    mLivePlayer = new TXLivePlayer(this.cordova.getActivity());
    mLivePlayer.setConfig(mTXLivePlayConfig);
    mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
    mLivePlayer.setPlayListener(new ITXLivePlayListener() {
      @Override
      public void onPlayEvent(final int event, final Bundle param) {
        if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
          String msg = "[LivePlayer] 拉流失败[" + param.getString(TXLiveConstants.EVT_DESCRIPTION) + "]";
          TXCLog.e(TAG, msg);
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
          int width = param.getInt(TXLiveConstants.EVT_PARAM1, 0);
          int height = param.getInt(TXLiveConstants.EVT_PARAM2, 0);
          if (width > 0 && height > 0) {
            float ratio = (float) height / width;
            //pc上混流后的宽高比为4:5，这种情况下填充模式会把左右的小主播窗口截掉一部分，用适应模式比较合适
            if (ratio > 1.3f) {
              mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
            } else {
              mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
            }
          }
        }
      }

      @Override
      public void onNetStatus(Bundle status) {

      }
    });
  }

  protected int getPlayType(String playUrl) {
    int playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
    if (playUrl.startsWith("rtmp://")) {
      playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
    } else if ((playUrl.startsWith("http://") || playUrl.startsWith("https://")) && playUrl.contains(".flv")) {
      playType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
    }
    return playType;
  }

  public interface StandardCallback {
    /**
     * @param errCode 错误码
     * @param errInfo 错误信息
     */
    void onError(int errCode, String errInfo);

    void onSuccess();
  }
  protected Handler                       mListenerHandler = null;
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
  private class TXLivePushListenerImpl implements ITXLivePushListener {
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
        TXCLog.e(TAG,msg);
        callbackOnThread(mCallback, "onError", event, msg);
      } else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
        String msg = "[LivePusher] 推流失败[录屏启动失败]";
        TXCLog.e(TAG,msg);
        callbackOnThread(mCallback, "onError", event, msg);
      }
    }

    @Override
    public void onNetStatus(Bundle status) {

    }
  }

  private void initTxLivePusher() {
    if (mLivePusher == null) {
      mLivePusher = new TXLivePusher(this.cordova.getActivity().getApplicationContext());
    }
    TXLivePushConfig config = new TXLivePushConfig();
    config.setFrontCamera(true);
    config.enableScreenCaptureAutoRotate(true);// 是否开启屏幕自适应
    config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
    mLivePusher.setConfig(config);
    mLivePusher.setBeautyFilter(TXLiveConstants.BEAUTY_STYLE_SMOOTH, 5, 3, 2);
    mTXLivePushListener = new TXLivePushListenerImpl();
    mLivePusher.setPushListener(mTXLivePushListener);
  }

  public boolean startPlay(String userid, final CallbackContext callbackContext) {
    final String playURL = userid;

    Activity activity = this.cordova.getActivity();
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        prepareVideoView();

        if (mLivePlayer == null) {
          initTxLivePlayer();
        }
        videoView.setVisibility(View.VISIBLE);
        mLivePlayer.setPlayerView(videoView);

        /**
         * result返回值：
         * 0 V2TXLIVE_OK; -2 V2TXLIVE_ERROR_INVALID_PARAMETER; -3 V2TXLIVE_ERROR_REFUSED;
         */
        String newUrl = playURL;
        if (playURL == null || playURL.isEmpty() || playURL.equals("null")) {
          newUrl = createPullUrl(null);
          newUrl = "http://livetest2.homei-life.com/live/room1.flv?txSecret=d56ba4e50d522b9fe098be5c0e9a2f22&txTime=7F461120";
          //newUrl = "webrtc://livetest2.homei-life.com/live/room1?txSecret=d56ba4e50d522b9fe098be5c0e9a2f22&txTime=7F461120";
//          newUrl = "http://3891.liveplay.myqcloud.com/live/1400025029_topone.flv";
        }
        int code = mLivePlayer.startPlay(newUrl, getPlayType(newUrl));
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
      this.cordova.getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mLivePlayer.stopPlay(true);
        }
      });
    }
    mIsPlaying = false;
    this.detachVideoView();
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

  boolean pushStreamSuccessOn = false;
  public boolean startPush(String userid, final CallbackContext callbackContext) {
    int resultCode = MLVBCommonDef.Constants.PLAY_STATUS_SUCCESS;
    // String tRTMPURL = this.createPushUrl(userid);
    String tRTMPURL = userid;
    if (TextUtils.isEmpty(tRTMPURL) || (!tRTMPURL.trim().toLowerCase().startsWith("rtmp://"))) {
      tRTMPURL = this.createPushUrl(null);
      tRTMPURL = "rtmp://144681.livepush.myqcloud.com/live/room1?txSecret=abf2f6a4c5f858cf2c0fc51ebff71c63&txTime=82DFB501";
//      tRTMPURL = "rtmp://3891.live.ipcamera.myqcloud.com/live/1400025029_topone?txSecret=9bd79482dacf5715e0f6fa10f60ac02b&txTime=610B4855";
    }
    //在主线程开启推流
    Activity activity = this.cordova.getActivity();
    String finalTRTMPURL = tRTMPURL;
    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        prepareVideoView();

        View mPusherView = videoView;
        // 显示本地预览的View
        mPusherView.setVisibility(View.VISIBLE);
        initTxLivePusher();
        // 设置推流分辨率
        mLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION, false, false);

//      // 设置场景
//      setPushScene(mQualityType, mIsEnableAdjustBitrate);
//
//      // 设置声道，设置音频采样率，必须在 TXLivePusher.setVideoQuality 之后，TXLivePusher.startPusher之前设置才能生效
//      setAudioQuality(mAudioQuality);

        // 设置本地预览View
        mLivePusher.startCameraPreview(videoView);
        pushStreamSuccessOn = false;

        mTXLivePushListener.setCallback(new StandardCallback() {
          @Override
          public void onError(int errCode, String errInfo) {
            LOG.e(String.valueOf(errCode), errInfo);
          }

          @Override
          public void onSuccess() {
            //推流过程中，可能会重复收到PUSH_EVT_PUSH_BEGIN事件，onSuccess可能会被回调多次，如果已经创建的房间，直接返回
            if (pushStreamSuccessOn) {
              return;
            }

            if (mLivePusher != null) {
              TXLivePushConfig config = mLivePusher.getConfig();
              config.setVideoEncodeGop(2);
              mLivePusher.setConfig(config);
            }
            pushStreamSuccessOn = true;
          }
        });

        // 发起推流
        int resultCode = mLivePusher.startPusher(finalTRTMPURL);
        if (resultCode == -5) {
          TXLog.e(TAG,"License Error");
        }

        mIsPushing = true;
      }


    });
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
    if (this.mLivePusher != null) {
      this.cordova.getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          // 停止本地预览
          mLivePusher.stopCameraPreview(true);
          // 停止推流
          mLivePusher.stopPusher();
        }
      });
    }

    // 隐藏本地预览的View
    this.detachVideoView();
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
      @SuppressLint("SoonBlockedPrivateApi") Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
      mHiddenApiWarningShown.setAccessible(true);
      mHiddenApiWarningShown.setBoolean(activityThread, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

