package com.main.face_recognition_resource_server.services.camera.dahua;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;

public class FaceRecognitionEventHandler {

  public void initFaceRecognitionSubscription(NetSDKLib sdkInstance, NetSDKLib.LLong loginHandle, NetSDKLib.LLong eventHandle, NetSDKLib.fAnalyzerDataCallBack analyzerDataCallBack, int channel) {
    this.detachEventLoadPic(sdkInstance, eventHandle);

    int bNeedPicture = 1;
    eventHandle = sdkInstance.CLIENT_RealLoadPictureEx(
            loginHandle,
            channel,
            NetSDKLib.EVENT_IVS_FACERECOGNITION,
            bNeedPicture,
            analyzerDataCallBack,
            null,
            null
    );
    if (eventHandle.longValue() != 0) {
      System.out.printf("Chn[%d] CLIENT_RealLoadPictureEx Success\n", 0);
    } else {
      System.out.printf("Chn[%d] CLIENT_RealLoadPictureEx Failed! Last Error = %s\n", 0, ToolKits.getErrorCodePrint());
    }
  }


  public void detachEventLoadPic(NetSDKLib sdkInstance, NetSDKLib.LLong eventHandle) {
    if (eventHandle.longValue() != 0) {
      sdkInstance.CLIENT_StopLoadPic(eventHandle);
    }
  }

}
