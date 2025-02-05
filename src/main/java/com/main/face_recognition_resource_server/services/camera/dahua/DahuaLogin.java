package com.main.face_recognition_resource_server.services.camera.dahua;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.ptr.IntByReference;

public class DahuaLogin {

  public static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();


  public static void cleanup(NetSDKLib netsdk, boolean isSdkInit) {
    if (isSdkInit) {
      netsdk.CLIENT_Cleanup();
    }
  }

  public static NetSDKLib.LLong login(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, NetSDKLib netsdk) {
    NetSDKLib.LLong loginHandle;
    IntByReference nError = new IntByReference(0);
    loginHandle = netsdk.CLIENT_LoginEx2(m_strIp, m_nPort, m_strUser, m_strPassword, 0, null, m_stDeviceInfo, nError);
    if (loginHandle.longValue() == 0) {
      System.out.printf("Login Device[%s]Port[%d]Failed. %s\n", m_strIp, m_nPort, ToolKits.getErrorCodePrint());
    } else {
      System.out.println("Login Success[" + m_strIp + "]");
    }

    return loginHandle;
  }

  public static boolean logout(NetSDKLib netsdk, NetSDKLib.LLong loginHandle) {
    if (loginHandle.longValue() == 0) {
      return false;
    }

    boolean bRet = netsdk.CLIENT_Logout(loginHandle);
    if (bRet) {
      loginHandle.setValue(0);
    }
    return bRet;
  }

}
