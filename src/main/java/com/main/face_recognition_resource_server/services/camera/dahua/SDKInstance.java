package com.main.face_recognition_resource_server.services.camera.dahua;

import com.netsdk.lib.NetSDKLib;

public class SDKInstance {
  private static final NetSDKLib sdkInstance = NetSDKLib.NETSDK_INSTANCE;
  private static final NetSDKLib.fDisConnect disconnectCallback = DisconnectCallback.getInstance();
  private static final NetSDKLib.fHaveReConnect haveReconnectCallback = HaveReconnectCallback.getInstance();
  private static boolean isSDKInit;

  public static NetSDKLib getInstance() {
    if (isSDKInit) {
      return sdkInstance;
    }
    return init();
  }

  private static NetSDKLib init() throws RuntimeException {
    isSDKInit = sdkInstance.CLIENT_Init(SDKInstance.disconnectCallback, null);

    if (!isSDKInit) {
      System.out.println("Initialize SDK failed");
      throw new RuntimeException();
    }

    sdkInstance.CLIENT_SetAutoReconnect(SDKInstance.haveReconnectCallback, null);

    int waitTime = 5000;
    int tryTimes = 1;
    sdkInstance.CLIENT_SetConnectTime(waitTime, tryTimes);

    NetSDKLib.NET_PARAM netParam = new NetSDKLib.NET_PARAM();
    netParam.nConnectTime = 10000;
    netParam.nGetConnInfoTime = 3000;
    sdkInstance.CLIENT_SetNetworkParam(netParam);

    return sdkInstance;

  }
}
