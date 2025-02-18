package com.main.face_recognition_resource_server.services.camera.dahua;

import com.main.face_recognition_resource_server.DTOS.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.components.attendancecacheproducer.BlockingQueueAttendanceCacheProducer;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.BlockingQueue;

public class AnalyzerDataCallback implements NetSDKLib.fAnalyzerDataCallBack {
  private final CameraType cameraType;
  private final BlockingQueueAttendanceCacheProducer attendanceCacheProducer;
  private final BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue;

  private AnalyzerDataCallback(CameraType cameraType, BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
    this.attendanceCacheQueue = attendanceCacheQueue;
    attendanceCacheProducer = new BlockingQueueAttendanceCacheProducer(attendanceCacheQueue);
    this.cameraType = cameraType;
  }

  public static AnalyzerDataCallback getInstance(CameraType cameraType, BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
    return AnalyzerDataCBHolder.instance(cameraType, attendanceCacheQueue);
  }

  @Override
  public int invoke(NetSDKLib.LLong lAnalyzerHandle, int dwAlarmType, Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize, Pointer dwUser, int nSequence, Pointer reserved) {
    if (lAnalyzerHandle.longValue() == 0 || pAlarmInfo == null) {
      return -1;
    }

    if (dwAlarmType == NetSDKLib.EVENT_IVS_FACERECOGNITION) {
      long id;
      Date time;
      NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO msg = new NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO();
      ToolKits.GetPointerData(pAlarmInfo, msg);
      try {
        String idString = new String(msg.stuCandidatesEx[0].stPersonInfo.szPersonName).trim();
        time = new GregorianCalendar(msg.UTC.dwYear, msg.UTC.dwMonth - 1, msg.UTC.dwDay, msg.UTC.dwHour, msg.UTC.dwMinute, msg.UTC.dwSecond).getTime();
        byte[] byteBuffer = pBuffer.getByteArray(0, dwBufSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer);
        BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
        if (!idString.isEmpty() && bufferedImage != null) {
          id = Long.parseLong(idString);
          attendanceCacheProducer.produceCache(id, time, cameraType, bufferedImage);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return 0;
  }

  private static final class AnalyzerDataCBHolder {
    static AnalyzerDataCallback instance(CameraType cameraType, BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue) {
      return new AnalyzerDataCallback(cameraType, attendanceCacheQueue);
    }
  }
}
