package com.main.face_recognition_resource_server.services.camera.dahua;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.DTOS.camera.CameraCredentialsDTO;
import com.netsdk.lib.NetSDKLib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class FaceRecognitionSubscription implements Runnable {
  private final NetSDKLib sdkInstance;
  private final CameraCredentialsDTO cameraCredentials;
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;
  private final CountDownLatch latch;


  public FaceRecognitionSubscription(CameraCredentialsDTO cameraCredentials, BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
    this.cameraCredentials = cameraCredentials;
    this.attendanceCacheQueue = attendanceCacheQueue;
    this.sdkInstance = SDKInstance.getInstance();
    latch = new CountDownLatch(1);
  }

  @Override
  public void run() {
    NetSDKLib.LLong loginHandle = DahuaLogin.login(
            cameraCredentials.getIpAddress(),
            cameraCredentials.getPort(),
            cameraCredentials.getUsername(),
            cameraCredentials.getPassword(),
            sdkInstance
    );
    NetSDKLib.fAnalyzerDataCallBack analyzerDataCallBack = AnalyzerDataCallback.getInstance(cameraCredentials.getType(), attendanceCacheQueue);
    NetSDKLib.LLong eventHandle = new NetSDKLib.LLong(0);
    FaceRecognitionEventHandler faceRecognitionEventHandler = new FaceRecognitionEventHandler();
    faceRecognitionEventHandler.initFaceRecognitionSubscription(sdkInstance, loginHandle, eventHandle, analyzerDataCallBack, cameraCredentials.getChannel());

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      faceRecognitionEventHandler.detachEventLoadPic(sdkInstance, eventHandle);
      DahuaLogin.logout(sdkInstance, loginHandle);
      DahuaLogin.cleanup(sdkInstance, true);
      System.out.println("stopped");
    }));

    try {
      latch.await();
    } catch (InterruptedException e) {
      faceRecognitionEventHandler.detachEventLoadPic(sdkInstance, eventHandle);
      DahuaLogin.logout(sdkInstance, loginHandle);
//      DahuaLogin.cleanup(sdkInstance, true);
      System.out.println("stopped");
    }
  }
}
