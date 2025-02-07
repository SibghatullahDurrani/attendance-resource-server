package com.main.face_recognition_resource_server.services.camera.dahua;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.components.BlockingQueueAttendanceCacheProducer;
import com.main.face_recognition_resource_server.constants.CameraTypes;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.BlockingQueue;

public class AnalyzerDataCallback implements NetSDKLib.fAnalyzerDataCallBack {
  private final File picturePath;
  private final CameraTypes cameraType;
  private final BlockingQueueAttendanceCacheProducer attendanceCacheProducer;
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;

  private AnalyzerDataCallback(CameraTypes cameraType, BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
    this.attendanceCacheQueue = attendanceCacheQueue;
    attendanceCacheProducer = new BlockingQueueAttendanceCacheProducer(attendanceCacheQueue);
    this.cameraType = cameraType;

    picturePath = new File("./AnalyzerPicture/");
    if (!picturePath.exists()) {
      picturePath.mkdirs();
    }
  }

  public static AnalyzerDataCallback getInstance(CameraTypes cameraType, BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
    return AnalyzerDataCBHolder.instance(cameraType, attendanceCacheQueue);
  }

  @Override
  public int invoke(NetSDKLib.LLong lAnalyzerHandle, int dwAlarmType, Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize, Pointer dwUser, int nSequence, Pointer reserved) {
    if (lAnalyzerHandle.longValue() == 0 || pAlarmInfo == null) {
      return -1;
    }

    if (dwAlarmType == NetSDKLib.EVENT_IVS_FACERECOGNITION) {
      Long id;
      Date time;
      NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO msg = new NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO();
      ToolKits.GetPointerData(pAlarmInfo, msg);
      try {
        String idString = new String(msg.stuCandidatesEx[0].stPersonInfo.szPersonName).trim();
        time = new GregorianCalendar(msg.UTC.dwYear, msg.UTC.dwMonth - 1, msg.UTC.dwDay, msg.UTC.dwHour, msg.UTC.dwMinute, msg.UTC.dwSecond).getTime();
        if (!idString.isEmpty()) {
          id = Long.parseLong(idString);
          attendanceCacheProducer.produceCache(id, time, cameraType);
        }
//        if (nonResidentCache == null) {
//          handleDataWithJustResidentCache(id, time);
//        } else {
//          handleDataWithBothResidentAndNonResidentCache(id, time);
//        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      if (msg.nImageInfoNum == 0) {
        String snapPicPath = picturePath + "/" + System.currentTimeMillis() + "FaceRecognition.jpg";
        byte[] byteBuffer = pBuffer.getByteArray(0, dwBufSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer);
        try {
          BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
          if (bufferedImage != null) {
            ImageIO.write(bufferedImage, "jpg", new File(snapPicPath));
            System.out.println("Snapshot storage path: " + snapPicPath);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        String snapPicPath;
        for (int i = 0; i < msg.nImageInfoNum; i++) {
          snapPicPath = picturePath + "/" + System.currentTimeMillis() + "FaceRecognition_" + i + ".jpg";
          byte[] byteBuffer = pBuffer.getByteArray(msg.stuImageInfo[i].nOffset, msg.stuImageInfo[i].nLength);
          ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer);
          try {
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
            if (bufferedImage != null) {
              ImageIO.write(bufferedImage, "jpg", new File(snapPicPath));
              System.out.println("Snapshot storage path: " + snapPicPath);
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return 0;
  }

//  private void handleDataWithBothResidentAndNonResidentCache(Long userId, Date time) {
//    synchronized (synchronizationLock) {
//      if (!residentCache.isUserInCache(userId)) {
//        if (!nonResidentCache.isUserInCache(userId)) {
//          if (cameraType == CameraTypes.IN) {
//            //TODO: add an entry for attendance worker
//            residentCache.addUserToCache(userId);
//          }
//        } else {
//          //TODO: add an entry for attendance worker
//          nonResidentCache.removeUserFromCache(userId);
//          residentCache.addUserToCache(userId);
//        }
//      }
//    }
//  }
//
//  private void handleDataWithJustResidentCache(Long id, Date time) {
//    synchronized (synchronizationLock) {
//      if (!residentCache.isUserInCache(id)) {
//        //TODO: add an entry for attendance worker
//        residentCache.addUserToCache(id);
//        System.out.println("user with id: " + id + " added to cache at time: " + time);
//      } else {
//        System.out.println("user with id: " + id + " is already in the cache");
//      }
//    }
//  }


  private static final class AnalyzerDataCBHolder {
    static AnalyzerDataCallback instance(CameraTypes cameraType, BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
      return new AnalyzerDataCallback(cameraType, attendanceCacheQueue);
    }
  }
}
