package com.main.face_recognition_resource_server.services.camera.dahua;

import com.main.face_recognition_resource_server.DTOS.CameraDTO;
import com.main.face_recognition_resource_server.services.AttendanceCache;
import com.netsdk.lib.NetSDKLib;

import java.util.concurrent.CountDownLatch;

public class FaceRecognitionSubscription implements Runnable {
  private final NetSDKLib sdkInstance = SDKInstance.getInstance();
  private final CameraDTO cameraDTO;
  private final AttendanceCache residentCache;
  private final AttendanceCache nonResidentCache;
  private final Object synchronizationLock;

  public FaceRecognitionSubscription(AttendanceCache residentCache, AttendanceCache nonResidentCache, CameraDTO cameraDTO, Object synchronizationLock) {
    this.residentCache = residentCache;
    this.nonResidentCache = nonResidentCache;
    this.cameraDTO = cameraDTO;
    this.synchronizationLock = synchronizationLock;
  }

  @Override
  public void run() {
    NetSDKLib.LLong loginHandle = DahuaLogin.login(
            "192.168.100.37",
            80,
            "admin",
            "@dmin1234",
            sdkInstance
    );
    NetSDKLib.fAnalyzerDataCallBack analyzerDataCallBack = AnalyzerDataCallback.getInstance(residentCache, nonResidentCache, synchronizationLock);
    NetSDKLib.LLong eventHandle = new NetSDKLib.LLong(0);
    FaceRecognitionEventHandler faceRecognitionEventHandler = new FaceRecognitionEventHandler();
    faceRecognitionEventHandler.initFaceRecognitionSubscription(sdkInstance, loginHandle, eventHandle, analyzerDataCallBack);
    CountDownLatch latch = new CountDownLatch(1);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      faceRecognitionEventHandler.detachEventLoadPic(sdkInstance, eventHandle);
      DahuaLogin.logout(sdkInstance, loginHandle);
      DahuaLogin.cleanup(sdkInstance, true);
    }));

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
