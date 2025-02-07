package com.main.face_recognition_resource_server.services.camera.dahua;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.DTOS.CameraDTO;
import com.netsdk.lib.NetSDKLib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class FaceRecognitionSubscription implements Runnable {
  private final NetSDKLib sdkInstance = SDKInstance.getInstance();
  private final CameraDTO cameraDTO;
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;

  public FaceRecognitionSubscription(CameraDTO cameraDTO, BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
    this.cameraDTO = cameraDTO;
    this.attendanceCacheQueue = attendanceCacheQueue;
  }

  @Override
  public void run() {
    NetSDKLib.LLong loginHandle = DahuaLogin.login(
            cameraDTO.getIpAddress(),
            cameraDTO.getPort(),
            cameraDTO.getUsername(),
            cameraDTO.getPassword(),
            sdkInstance
    );
    NetSDKLib.fAnalyzerDataCallBack analyzerDataCallBack = AnalyzerDataCallback.getInstance(cameraDTO.getType(), attendanceCacheQueue);
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
