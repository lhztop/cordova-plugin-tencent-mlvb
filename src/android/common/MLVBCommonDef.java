package com.qcloud.cordova.mlvb.common;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.tencent.rtmp.ui.TXCloudVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MLVBCommonDef {
    public enum CustomFieldOp{
        SET/*设置*/, INC/*加计数*/, DEC/*减计数*/
    }

    public interface LiveRoomErrorCode {
        //推流和拉流错误码，请查看 TXLiteAVCode.h
        //IM 错误码，请查看 https://cloud.tencent.com/document/product/269/1671

        /******************************************
         *
         * LiveRoom错误码
         *
         *****************************************/
        int OK = 0;
// { 后台错误码
        /*msg处理错误*/
        int ERROR_CODE_INVALID_MSG = 200100;
        int ERROR_CODE_INVALID_JSON = 200101;
        /*参数校验错误*/
        int ERROR_CODE_INCOMPLETE_PARAM = 201000;
        int ERROR_CODE_INCOMPLETE_LOGIN_PARAM = 201001;
        int ERROR_CODE_NO_USERID = 201002;
        int ERROR_CODE_USERID_NOT_EQUAL = 201003;
        int ERROR_CODE_NO_ROOMID = 201004;
        int ERROR_CODE_NO_COUNT = 201005;
        int ERROR_CODE_NO_MERGE_STREAM_PARAM = 201006;
        int ERROR_CODE_OPERATION_EMPTY = 201007;
        int ERROR_CODE_UNSUPPORT_OPERATION = 201008;
        int ERROR_CODE_SET_FIELD_VALUE_EMPTY = 201009;
        /*鉴权错误*/
        int ERROR_CODE_VERIFY = 202000;
        int ERROR_CODE_VERIFY_FAILED = 202001;
        int ERROR_CODE_CONNECTED_TO_IM_SERVER = 202002;
        int ERROR_CODE_INVALID_RSP = 202003;
        int ERROR_CODE_LOGOUT = 202004;
        int ERROR_CODE_APPID_RELATION = 202005;
        /*房间操作错误*/
        int ERROR_CODE_ROOM_MGR = 203000;
        int ERROR_CODE_GET_ROOM_ID = 203001;
        int ERROR_CODE_CREATE_ROOM = 203002;
        int ERROR_CODE_DESTROY_ROOM = 203003;
        int ERROR_CODE_GET_ROOM_LIST = 203004;
        int ERROR_CODE_UPDATE_ROOM_MEMBER = 203005;
        int ERROR_CODE_ENTER_ROOM = 203006;
        int ERROR_CODE_ROOM_PUSHER_TOO_MUCH = 203007;
        int ERROR_CODE_INVALID_PUSH_URL = 203008;
        int ERROR_CODE_ROOM_NAME_TOO_LONG = 203009;
        int ERROR_CODE_USER_NOT_IN_ROOM = 203010;

        /*pusher操作错误*/
        int ERROR_CODE_PUSHER_MGR = 204000;
        int ERROR_CODE_GET_PUSH_URL = 204001;
        int ERROR_CODE_GET_PUSHERS = 204002;
        int ERROR_CODE_LEAVE_ROOM = 204003;
        int ERROR_CODE_GET_PUSH_AND_ACC_URL = 204004;

        /*观众操作错误*/
        int ERROR_CODE_AUDIENCE_MGR = 205000;
        int ERROR_CODE_AUDIENCE_NUM_FULL = 205001;
        int ERROR_CODE_ADD_AUDIENCE = 205002;
        int ERROR_CODE_DEL_AUDIENCE = 205003;
        int ERROR_CODE_GET_AUDIENCES = 205004;

        /*心跳处理错误*/
        int ERROR_CODE_HEARTBEAT = 206000;
        int ERROR_CODE_SET_HEARTBEAT = 206001;
        int ERROR_CODE_DEL_HEARTBEAT = 206002;
        /*其他错误*/
        int ERROR_CODE_OTHER = 207000;
        int ERROR_CODE_DB_FAILED = 207001;
        int ERROR_CODE_MIX_FAILED = 207002;
        int ERROR_CODE_SET_CUSTOM_FIELD = 207003;
        int ERROR_CODE_GET_CUSTOM_FIELD = 207004;
        int ERROR_CODE_UNSUPPORT_ACTION = 207005;
        int ERROR_CODE_UNSUPPORT_ROOM_TYPE = 207006;

// } 后台错误码

// { 客户端错误码
        int ERROR_NOT_LOGIN = -1; //未登录
        int ERROR_NOT_IN_ROOM = -2; //未进直播房间
        int ERROR_PUSH = -3; //推流错误
        int ERROR_PARAMETERS_INVALID = -4; //参数错误
        int ERROR_LICENSE_INVALID = -5; //license 校验失败
        int ERROR_PLAY = -6; //播放错误
        int ERROR_IM_FORCE_OFFLINE = -7; // IM 被强制下线（例如：多端登录）
// } 客户端错误码

        // @}
    }

  /**
   * Module:   TCGlobalConfig
   * <p>
   * Function: 小直播 的全局配置类
   * <p>
   * 1. LiteAVSDK Licence
   * 2. 计算腾讯云 UserSig 的 SDKAppId、加密密钥、签名过期时间
   * 3. 小直播后台服务器地址
   * 4. App 主色调
   * 5. 是否启用连麦
   */

  public static class TCGlobalConfig {

    /**
     * 1. LiteAVSDK Licence。 用于直播推流鉴权。
     * <p>
     * 获取License，请参考官网指引 https://cloud.tencent.com/document/product/454/34750
     */
    public static final String LICENCE_URL = "http://license.vod2.myqcloud.com/license/v1/3c6dec57c3d8540904f8bfebc39452bd/TXLiveSDK.licence";
    public static final String LICENCE_KEY = "728a4aaa1ac2ca1a3aad75a0a220ec60";


    /**
     * 2.1 腾讯云 SDKAppId，需要替换为您自己账号下的 SDKAppId。
     * <p>
     * 进入腾讯云直播[控制台-直播SDK-应用管理](https://console.cloud.tencent.com/live/license/appmanage) 创建应用，即可看到 SDKAppId，
     * 它是腾讯云用于区分客户的唯一标识。
     */
    public static final int SDKAPPID = 1400540398;

    /**
     * 2.2 计算签名用的加密密钥，获取步骤如下：
     * <p>
     * step1. 进入腾讯云直播[控制台-直播SDK-应用管理](https://console.cloud.tencent.com/live/license/appmanage)，如果还没有应用就创建一个，
     * step2. 单击您的应用，进入"应用管理"页面。
     * step3. 点击“查看密钥”按钮，就可以看到计算 UserSig 使用的加密的密钥了，请将其拷贝并复制到如下的变量中。
     * 如果提示"请先添加管理员才能生成公私钥"，点击"编辑"，输入管理员名称，如"admin"，点"确定"添加管理员。然后再查看密钥。
     * <p>
     * 注意：该方案仅适用于调试Demo，正式上线前请将 UserSig 计算代码和密钥迁移到您的后台服务器上，以避免加密密钥泄露导致的流量盗用。
     * 文档：https://cloud.tencent.com/document/product/647/17275#Server
     */
    public static final String SECRETKEY = "5a7c5c81896fd2071b1c4d0747c83040915a0613a8fedf74d29a37ad79a94929";

    /**
     * 2.3 签名过期时间，建议不要设置的过短
     * <p>
     * 时间单位：秒
     * 默认时间：7 x 24 x 60 x 60 = 604800 = 7 天
     */
    public static final int EXPIRETIME = 604800;


    /**
     * 3. 小直播后台服务器地址
     * <p>
     * 3.1 您可以不填写后台服务器地址：
     * 小直播 App 单靠客户端源码运行，方便快速跑通体验小直播。
     * 不过在这种模式下运行的“小直播”，没有注册登录、回放列表等功能，仅有基本的直播推拉流、聊天室、连麦等功能。
     * 另外在这种模式下，腾讯云安全签名 UserSig 是使用本地 GenerateTestUserSig 模块计算的，存在 SECRETKEY 被破解的导致腾讯云流量被盗用的风险。
     * <p>
     * 3.2 您可以填写后台服务器地址：
     * 服务器需要您参考文档 https://cloud.tencent.com/document/product/454/15187 自行搭建。
     * 服务器提供注册登录、回放列表、计算 UserSig 等服务。
     * 这种情况下 {@link #SDKAPPID} 和 {@link #SECRETKEY} 可以设置为任意值。
     * <p>
     * 注意：
     * 后台服务器地址（APP_SVR_URL）和 （SDKAPPID，SECRETKEY）一定要填一项。
     * 要么填写后台服务器地址（@link #APP_SVR_URL），要么填写 {@link #SDKAPPID} 和 {@link #SECRETKEY}。
     * <p>
     * 详情请参考：
     */
    public static final String APP_SVR_URL = "";


    /**
     * 4. App 主色调。
     */
    public static final int MAIN_COLOR = 0xff222B48;


    /**
     * 5. 是否启用连麦。
     * <p>
     * 由于连麦功能使用了比较昂贵的 BGP 专用线路，所以是按照通话时长进行收费的。最初级的体验包包含 3000 分钟的连麦时长，只需要 9.8 元。
     * 购买链接：https://buy.cloud.tencent.com/mobilelive?urlctr=yes&micconn=3000m##
     */
    public static final boolean ENABLE_LINKMIC = false;


    /**
     * 6. 直播的域名，用于push pull 流,直接用腾讯云直播地址
     */
    public static final String DOMAIN_URL = "https://144660.livepush.myqcloud.com";

    /**
     * 7. 直播的域名鉴权key，用于push pull 流,直接用腾讯云直播地址
     */
    public static final String DOMAIN_KEY = "c874b6166f43c9b53b57c5ffa64797e8";


    /**
     * 8. 直播应用名称，默认为live
     */
    public static final String APP_NAME = "live";

  }

  /**
   * Created by jac on 2017/10/30.
   */

  public static class RoomInfo implements Parcelable {

      /**
       * 房间ID
       */
      public String   roomID;

      /**
       * 房间信息（创建房间时传入）
       */
      public String   roomInfo;

      /**
       * 房间名称
       */
      public String   roomName;

      /**
       * 房间创建者ID
       */
      public String   roomCreator;

      /**
       * 房间创建者的拉流地址（实时模式下不使用该字段；直播模式下就是主播的拉流地址；连麦模式下就是混流地址）
       */
      public String   mixedPlayURL;

      /**
       * 房间成员列表
       */
      public List<AnchorInfo> pushers;

      /**
       * 房间观众数
       */
      public int audienceCount;

      /**
       * 房间观众列表
       */
      public List<Audience> audiences;

      /**
       * 房间自定义数据
       */
      public String custom;

      public static class Audience {
          public String userID;     //观众ID
          public String userInfo;   //观众信息
          public String userName;
          public String userAvatar;

          public void transferUserInfo() {
              JSONObject jsonRoomInfo = null;
              try {
                  jsonRoomInfo = new JSONObject(userInfo);
                  userName    = jsonRoomInfo.optString("userName");
                  userAvatar  = jsonRoomInfo.optString("userAvatar");
              } catch (JSONException e) {
                  e.printStackTrace();
              }
          }
      }

      public RoomInfo() {

      }

      public RoomInfo(String roomID, String roomInfo, String roomName, String roomCreator, String mixedPlayURL, List<AnchorInfo> anchors) {
          this.roomID = roomID;
          this.roomInfo = roomInfo;
          this.roomName = roomName;
          this.roomCreator = roomCreator;
          this.mixedPlayURL = mixedPlayURL;
          this.pushers = anchors;
      }

      protected RoomInfo(Parcel in) {
          this.roomID = in.readString();
          this.roomInfo = in.readString();
          this.roomName = in.readString();
          this.roomCreator = in.readString();
          this.mixedPlayURL = in.readString();
          this.pushers = new ArrayList<AnchorInfo>();
          in.readList(this.pushers, AnchorInfo.class.getClassLoader());
      }

      @Override
      public int describeContents() {
          return 0;
      }

      @Override
      public void writeToParcel(Parcel dest, int flags) {
          dest.writeString(this.roomID);
          dest.writeString(this.roomInfo);
          dest.writeString(this.roomName);
          dest.writeString(this.roomCreator);
          dest.writeString(this.mixedPlayURL);
          dest.writeList(this.pushers);
      }

      public static final Creator<RoomInfo> CREATOR = new Creator<RoomInfo>() {
          @Override
          public RoomInfo createFromParcel(Parcel source) {
              return new RoomInfo(source);
          }

          @Override
          public RoomInfo[] newArray(int size) {
              return new RoomInfo[size];
          }
      };
  }

  /**
   * Created by jac on 2017/11/14.
   * Copyright © 2013-2017 Tencent Cloud. All Rights Reserved.
   */

  public static class LoginInfo implements Parcelable {

      /**
       * 直播的appID
       */
      public long   sdkAppID;

      /**
       * 自己的用户ID
       */
      public String   userID;

      public String userSig;

      /**
       * 自己的用户名称
       */
      public String   userName;

      /**
       * 自己的头像地址
       */
      public String   userAvatar;


      public LoginInfo() {

      }

      public LoginInfo(int sdkAppID, String userID, String userName, String userAvatar, String userSig) {
          this.sdkAppID = sdkAppID;
          this.userID = userID;
          this.userName = userName;
          this.userAvatar = userAvatar;
          this.userSig = userSig;
      }

      protected LoginInfo(Parcel in) {
          this.userID = in.readString();
          this.userName = in.readString();
          this.userAvatar = in.readString();
          this.userSig = in.readString();
      }

      @Override
      public int describeContents() {
          return 0;
      }

      @Override
      public void writeToParcel(Parcel dest, int flags) {
          dest.writeString(this.userID);
          dest.writeString(this.userName);
          dest.writeString(this.userAvatar);
          dest.writeString(this.userSig);
      }

      public static final Creator<LoginInfo> CREATOR = new Creator<LoginInfo>() {
          @Override
          public LoginInfo createFromParcel(Parcel source) {
              return new LoginInfo(source);
          }

          @Override
          public LoginInfo[] newArray(int size) {
              return new LoginInfo[size];
          }
      };
  }

  public static class AnchorInfo implements Parcelable {

    /**
     * 用户ID
     */
    public String   userID;

    /**
     * 用户昵称
     */
    public String   userName;

    /**
     * 用户头像地址
     */
    public String   userAvatar;

    /**
     * 低时延拉流地址（带防盗链key）
     */
    public String   accelerateURL;


    public AnchorInfo() {

    }

    public AnchorInfo(String userID, String userName, String userAvatar, String accelerateURL) {
      this.userID = userID;
      this.userName = userName;
      this.userAvatar = userAvatar;
      this.accelerateURL = accelerateURL;
    }

    protected AnchorInfo(Parcel in) {
      this.userID = in.readString();
      this.userName = in.readString();
      this.accelerateURL = in.readString();
      this.userAvatar = in.readString();
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(this.userID);
      dest.writeString(this.userName);
      dest.writeString(this.accelerateURL);
      dest.writeString(this.userAvatar);
    }

    public static final Creator<AnchorInfo> CREATOR = new Creator<AnchorInfo>() {
      @Override
      public AnchorInfo createFromParcel(Parcel source) {
        return new AnchorInfo(source);
      }

      @Override
      public AnchorInfo[] newArray(int size) {
        return new AnchorInfo[size];
      }
    };

    @Override
    public int hashCode() {
      return userID.hashCode();
    }

    @Override
    public String toString() {
      return "AnchorInfo{" +
        "userID='" + userID + '\'' +
        ", userName='" + userName + '\'' +
        ", accelerateURL='" + accelerateURL + '\'' +
        ", userAvatar='" + userAvatar + '\'' +
        '}';
    }
  }

  public static class AudienceInfo {
      public String userID;     //观众ID
      public String userInfo;   //观众信息
      public String userName;
      public String userAvatar;

      public void transferUserInfo() {
          JSONObject jsonRoomInfo = null;
          try {
              jsonRoomInfo = new JSONObject(userInfo);
              userName    = jsonRoomInfo.optString("userName");
              userAvatar  = jsonRoomInfo.optString("userAvatar");
          } catch (JSONException e) {
              e.printStackTrace();
          }
      }
  }

  /**
   * MLVBLiveRoom 事件回调
   *
   * 包括房间关闭、Debug 事件信息、出错说明等。
   */
  public static interface IMLVBLiveRoomListener {

      /////////////////////////////////////////////////////////////////////////////////
      //
      //                       错误 & 警告
      //
      /////////////////////////////////////////////////////////////////////////////////

      /// @name 通用事件回调
      /// @{

      /**
       * 错误回调
       *
       * SDK 不可恢复的错误，一定要监听，并分情况给用户适当的界面提示
       *
       * @param errCode 	错误码
       * @param errMsg 	错误信息
       * @param extraInfo 额外信息，如错误发生的用户，一般不需要关注，默认是本地错误
       */
      public void onError(int errCode, String errMsg, Bundle extraInfo);

      /**
       * 警告回调
       *
       * @param warningCode 	错误码 TRTCWarningCode
       * @param warningMsg 	警告信息
       * @param extraInfo 	额外信息，如警告发生的用户，一般不需要关注，默认是本地错误
       */
      public void onWarning(int warningCode, String warningMsg, Bundle extraInfo);

      void onDebugLog(String log);

      /// @}

      /////////////////////////////////////////////////////////////////////////////////
      //
      //                      房间事件回调
      //
      /////////////////////////////////////////////////////////////////////////////////

      /// @name 房间事件回调
      /// @{

      /**
       * 房间被销毁的回调
       *
       * 主播退房时，房间内的所有用户都会收到此通知
       *
       * @param roomID 房间 ID
       */
      public void onRoomDestroy(String roomID) ;


      /////////////////////////////////////////////////////////////////////////////////
      //
      //                      主播 & 观众的进出事件回调
      //
      /////////////////////////////////////////////////////////////////////////////////

      /**
       * 收到新主播进房通知
       *
       * 房间内的主播（和连麦中的观众）会收到新主播的进房事件，您可以调用 {@link MLVBLiveRoom#startRemoteView(AnchorInfo, TXCloudVideoView, PlayCallback)} 显示该主播的视频画面。
       *
       * @param anchorInfo 新进房用户信息
       *
       * @note 直播间里的普通观众不会收到主播加入和推出的通知。
       */
      public void onAnchorEnter(AnchorInfo anchorInfo) ;

      /**
       * 收到主播退房通知
       *
       * 房间内的主播（和连麦中的观众）会收到新主播的退房事件，您可以调用 {@link MLVBLiveRoom#stopRemoteView(AnchorInfo)} 关闭该主播的视频画面。
       *
       * @param anchorInfo 退房用户信息
       *
       * @note 直播间里的普通观众不会收到主播加入和推出的通知。
       */
      public void onAnchorExit(AnchorInfo anchorInfo);

      /**
       * 收到观众进房通知
       *
       * @param audienceInfo 进房观众信息
       */
      public void onAudienceEnter(AudienceInfo audienceInfo) ;

      /**
       * 收到观众退房通知
       *
       * @param audienceInfo 退房观众信息
       */
      public void onAudienceExit(AudienceInfo audienceInfo) ;


      /////////////////////////////////////////////////////////////////////////////////
      //
      //                      主播和观众连麦事件回调
      //
      /////////////////////////////////////////////////////////////////////////////////
      /**
       * 主播收到观众连麦请求时的回调
       *
       * @param anchorInfo 观众信息
       * @param reason 连麦原因描述
       */
      public void onRequestJoinAnchor(AnchorInfo anchorInfo, String reason) ;

      /**
       * 连麦观众收到被踢出连麦的通知
       *
       * 连麦观众收到被主播踢除连麦的消息，您需要调用 {@link MLVBLiveRoom#kickoutJoinAnchor(String)} 来退出连麦
       */
      public void onKickoutJoinAnchor() ;


      /////////////////////////////////////////////////////////////////////////////////
      //
      //                      主播 PK 事件回调
      //
      /////////////////////////////////////////////////////////////////////////////////
      /**
       * 收到请求跨房 PK 通知
       *
       * 主播收到其他房间主播的 PK 请求
       * 如果同意 PK ，您需要调用 {@link MLVBLiveRoom#startRemoteView(AnchorInfo, TXCloudVideoView, PlayCallback)}  接口播放邀约主播的流
       *
       * @param anchorInfo 发起跨房连麦的主播信息
       */
      public void onRequestRoomPK(AnchorInfo anchorInfo) ;


      /**
       * 收到断开跨房 PK 通知
       */
      public void onQuitRoomPK(AnchorInfo anchorInfo) ;

      /// @}

      /////////////////////////////////////////////////////////////////////////////////
      //
      //                      消息事件回调
      //
      /////////////////////////////////////////////////////////////////////////////////

      /// @name 消息事件回调
      /// @{

      /**
       * 收到文本消息
       *
       * @param roomID        房间 ID
       * @param userID        发送者 ID
       * @param userName      发送者昵称
       * @param userAvatar    发送者头像
       * @param message       文本消息
       */
      public void onRecvRoomTextMsg(String roomID, String userID, String userName, String userAvatar, String message) ;

      /**
       * 收到自定义消息
       *
       * @param roomID        房间 ID
       * @param userID        发送者 ID
       * @param userName      发送者昵称
       * @param userAvatar    发送者头像
       * @param cmd           自定义 cmd
       * @param message       自定义消息内容
       */
      public void onRecvRoomCustomMsg(String roomID, String userID, String userName, String userAvatar, String cmd, String message) ;

      /// @}

      /**
       * 登录结果回调接口
       */
      public interface LoginCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 获取房间列表回调接口
       */
      public interface GetRoomListCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           *
           * @param roomInfoList 房间列表
           */
          void onSuccess(ArrayList<RoomInfo> roomInfoList);
      }

      /**
       * 获取观众列表回调接口
       *
       * 观众进房时，后台会将其信息加入观众列表中，观众列表最大保存30名观众信息。
       */
      public interface GetAudienceListCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           *
           * @param audienceInfoList 观众列表
           */
          void onSuccess(ArrayList<AudienceInfo> audienceInfoList);
      }

      /**
       * 创建房间的结果回调接口
       */
      public interface CreateRoomCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           *
           * @param RoomID 房间号标识
           */
          void onSuccess(String RoomID);
      }

      /**
       * 进入房间的结果回调接口
       */
      public interface EnterRoomCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 离开房间的结果回调接口
       */
      public interface ExitRoomCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 观众请求连麦的结果回调接口
       */
      public interface RequestJoinAnchorCallback {
          /**
           * 主播接受连麦
           */
          void onAccept();

          /**
           * 主播拒绝连麦
           * @param reason 拒绝原因
           */
          void onReject(String reason);

          /**
           * 请求超时
           */
          void onTimeOut();

          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);
      }

      /**
       * 进入连麦的结果回调接口
       */
      public interface JoinAnchorCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码 RequestRoomPKCallback
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 退出连麦的结果回调接口
       */
      public interface QuitAnchorCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 请求跨房 PK 的结果回调接口
       */
      public interface RequestRoomPKCallback {
          /**
           * 主播接受连麦
           *
           * @param anchorInfo 被邀请 PK 主播的信息
           */
          void onAccept(AnchorInfo anchorInfo);

          /**
           * 拒绝 PK
           *
           * @param reason 拒绝原因
           */
          void onReject(String reason);

          /**
           * 请求超时
           */
          void onTimeOut();

          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);
      }

      /**
       * 退出跨房 PK 的结果回调接口
       */
      public interface QuitRoomPKCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 播放器回调接口
       */
      public interface PlayCallback {
          /**
           * 开始回调
           */
          void onBegin();
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 其他事件回调
           *
           * @param event 事件 ID
           * @param param 事件附加信息
           */
          void onEvent(int event, Bundle param);
      }

      /**
       * 发送文本消息回调接口
       */
      public interface SendRoomTextMsgCallback{
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */

          void onError(int errCode, String errInfo);
          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 发送自定义消息回调接口
       */
      public interface SendRoomCustomMsgCallback{
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 设置自定义信息回调接口
       */
      public interface SetCustomInfoCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 成功回调
           */
          void onSuccess();
      }

      /**
       * 获取自定义信息回调接口
       */
      public interface GetCustomInfoCallback {
          /**
           * 错误回调
           *
           * @param errCode 错误码
           * @param errInfo 错误信息
           */
          void onError(int errCode, String errInfo);

          /**
           * 获取自定义信息的回调
           *
           * @param customInfo 自定义信息
           */
          void onGetCustomInfo(Map<String, Object> customInfo);
      }
  }
}
