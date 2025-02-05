package com.main.face_recognition_resource_server.services.camera.dahua;

import com.netsdk.lib.NetSDKLib;
import com.sun.jna.Pointer;

public class DisconnectCallback implements NetSDKLib.fDisConnect {
  public static DisconnectCallback getInstance() {
    return DisconnectHolder.instance;
  }

  @Override
  public void invoke(NetSDKLib.LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
    System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
  }

  private static class DisconnectHolder {
    private static final DisconnectCallback instance = new DisconnectCallback();
  }
}
