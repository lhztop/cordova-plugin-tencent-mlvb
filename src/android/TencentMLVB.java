package com.qcloud.cordova.mlvb;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.google.gson.Gson;

//import com.tencent.av.sdk.*;
//import com.tencent.ilivesdk.*;
//import com.tencent.ilivesdk.core.*;
//import com.tencent.livesdk.*;

import com.qcloud.cordova.mlvb.common.MLVBCommonDef;
import com.qcloud.cordova.mlvb.utils.TCUtils;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import com.tencent.liteav.basic.log.TXCLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


public class TencentMLVB extends CordovaPlugin {
  private static final String TAG = TencentMLVB.class.getName();
  private Context context;
  protected MLVBCommonDef.IMLVBLiveRoomListener mListener = null;
  private Activity activity;
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

  private String[] permissions = {
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
    this.cordovaWebView = webView;
    this.cordova = cordova;
    this.activity = cordova.getActivity();
    this.context = this.activity;
    this.rootView = (ViewGroup) activity.findViewById(android.R.id.content);
    this.webView = (WebView) rootView.getChildAt(0);
    this.createMLVBPlatform(this.context);
  }

  private void createMLVBPlatform(Context context) {
    if (context == null) {
      throw new InvalidParameterException("MLVB初始化错误：context不能为空！");
    }
    // 必须：初始化 LiteAVSDK Licence。 用于直播推流鉴权。
    TXLiveBase.getInstance().setLicence(context, MLVBCommonDef.TCGlobalConfig.LICENCE_URL, MLVBCommonDef.TCGlobalConfig.LICENCE_KEY);

    Context mAppContext = context.getApplicationContext();
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

    if (action.equals("getVersion")) {
      return getVersion(callbackContext);
    } else if (action.equals("startPush")) {
      final String url = args.getString(0);
      return startPush(url, callbackContext);
    } else if (action.equals("stopPush")) {
      return stopPush(callbackContext);
    } else if (action.equals("onPushEvent")) {
      alert("尚未实现");
    } else if (action.equals("startPlay")) {
      final String url = args.getString(0);
      final int playType = args.getInt(1);
      return startPlay(url, playType, callbackContext);
    } else if (action.equals("stopPlay")) {
      return stopPlay(callbackContext);
    } else if (action.equals("onPlayEvent")) {
      alert("尚未实现");
    } else if (action.equals("setVideoQuality")) {
      if (mTXLivePusher == null) return false;
      final int quality = args.getInt(0);
      final int adjustBitrate = args.getInt(1);
      final int adjustResolution = args.getInt(2);
//            mLivePusher.setVideoQuality(quality, adjustBitrate, adjustResolution);
      mTXLivePusher.setVideoQuality(quality, true, true);
    } else if (action.equals("setBeautyFilterDepth")) {
      if (mTXLivePusher == null) return false;
      final int beautyDepth = args.getInt(0);
      final int whiteningDepth = args.getInt(1);
      mTXLivePusher.setBeautyFilter(1, beautyDepth, whiteningDepth, 5);
    } else if (action.equals("setExposureCompensation")) {
      // TODO: 尚未测试
      if (mTXLivePusher == null) return false;
      final float depth = (float) args.getDouble(0);
      mTXLivePusher.setExposureCompensation(depth);
    } else if (action.equals("setFilter")) {
      alert("尚未实现");
    } else if (action.equals("switchCamera")) {
      if (mTXLivePusher == null) return false;
      mTXLivePusher.switchCamera();
    } else if (action.equals("toggleTorch")) {
      // TODO: 尚未测试
      if (mTXLivePusher == null) return false;
      final boolean enabled = args.getBoolean(0);
      mTXLivePusher.turnOnFlashLight(enabled);
    } else if (action.equals("setFocusPosition")) {
      alert("尚未实现");
    } else if (action.equals("setWaterMark")) {
      alert("尚未实现");
    } else if (action.equals("setPauseImage")) {
      alert("尚未实现");
    } else if (action.equals("resize")) {
      alert("尚未实现");
    } else if (action.equals("pause")) {
      alert("尚未实现");
    } else if (action.equals("resume")) {
      alert("尚未实现");
    } else if (action.equals("setRenderMode")) {
      alert("尚未实现");
    } else if (action.equals("setRenderRotation")) {
      alert("尚未实现");
    } else if (action.equals("enableHWAcceleration")) {
      alert("尚未实现");
    } else if (action.equals("startRecord")) {
//            return startRecord(callbackContext);
    } else if (action.equals("stopRecord")) {
//            return stopRecord(callbackContext);
    } else if (action.equals("fixMinFontSize")) {
//            return stopRecord(callbackContext);
      return fixMinFontSize(callbackContext);
    }

    callbackContext.error("Undefined action: " + action);
    return false;

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
   * 在当前 Activity 底部 UI 层注册一个 TXCloudVideoView 以供直播渲染
   */
  private void prepareVideoView() {
    if (videoView != null) return;
    // 通过 layout 文件插入 videoView
    LayoutInflater layoutInflater = LayoutInflater.from(activity);
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
  private boolean getVersion(final CallbackContext callbackContext) {
    int[] sdkver = new int[]{6, 7};
    if (sdkver != null && sdkver.length > 0) {
      String ver = "" + sdkver[0];
      for (int i = 1; i < sdkver.length; ++i) {
        ver += "." + sdkver[i];
      }
      callbackContext.success(ver);
      return true;
    }
    callbackContext.error("Cannot get rtmp sdk version.");
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
      settings = ((WebView) cordovaWebView.getEngine().getView()).getSettings();
      settings.setMinimumFontSize(1);
      settings.setMinimumLogicalFontSize(1);
    } catch (Exception error) {
      callbackContext.error("10003");
      return false;
    }
    return true;
  }

  private String createPushUrl(String userId) {
    String pushDomainKey = MLVBCommonDef.TCGlobalConfig.DOMAIN_KEY;
    String roomID;
    if (userId != null && userId.isEmpty()) {
      roomID = "RoomDefault";
    } else {
      roomID = userId;
    }
    String pushURL = "https://144660.livepush.myqcloud.com/live/" + roomID + "?";

    long time = System.currentTimeMillis() / 1000;
    String timeHex = String.format("%x", time);
    String sig = TCUtils.md5(String.format("%s%s%s", pushDomainKey, roomID, timeHex));
    pushURL += String.format("txSecret=%s&txTime=%s", sig, timeHex);
    return pushURL;
  }

  private boolean startPush(final CallbackContext callbackContext) {
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

  protected boolean startPush(final String url, final int videoQuality, final StandardCallback callback){
    if (mTXLivePusher != null) {
      callbackContext.error("10002");
      return false;
    }
    //在主线程开启推流
    Handler handler = new Handler(context.getApplicationContext().getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        prepareVideoView();
        // 开始推流
        mTXLivePusher = new TXLivePusher(activity);
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
   * 开始推流，并且在垫底的 videoView 显示视频
   * 会在当前对象上下文注册一个 TXLivePusher
   *
   * @param url             推流URL
   * @param callbackContext
   * @return
   */
  private boolean startPush(final String url, final CallbackContext callbackContext) {

    // 准备 videoView，没有的话生成
    activity.runOnUiThread(new Runnable() {
      public void run() {
        prepareVideoView();
        // 开始推流
        mTXLivePusher = new TXLivePusher(activity);
        TXLivePushConfig mLivePushConfig = new TXLivePushConfig();
        mTXLivePusher.setConfig(mLivePushConfig);
        mTXLivePusher.startPusher(url);
        // 将视频绑定到 videoView
        mTXLivePusher.startCameraPreview(videoView);
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
  private boolean stopPush(final CallbackContext callbackContext) {
    if (mTXLivePusher == null) {
      callbackContext.error("10003");
      return false;
    }
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
  private boolean startPlay(final String url, final int playType, final CallbackContext callbackContext) {
    if (mTXLivePlayer != null) {
      callbackContext.error("10004");
      return false;
    }
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
  private boolean stopPlay(final CallbackContext callbackContext) {
    if (mTXLivePlayer == null) {
      callbackContext.error("10005");
      return false;
    }
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

  public void alert(String msg) {
    alert(msg, "系统提示");
  }

  public String jsonEncode(Object obj) {
    Gson gson = new Gson();
    return gson.toJson(obj);
  }

  public int _R(String defType, String name) {
    return activity.getApplication().getResources().getIdentifier(
      name, defType, activity.getApplication().getPackageName());
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
