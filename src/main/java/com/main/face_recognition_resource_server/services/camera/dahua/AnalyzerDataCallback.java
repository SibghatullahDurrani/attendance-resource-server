package com.main.face_recognition_resource_server.services.camera.dahua;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCacheDTO;
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
  private final NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO msg = new NetSDKLib.DEV_EVENT_FACERECOGNITION_INFO();

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
      ToolKits.GetPointerData(pAlarmInfo, msg);
      try {
        String idString = new String(msg.stuCandidatesEx[0].stPersonInfo.szPersonName).trim();
        time = new GregorianCalendar(msg.UTC.dwYear, msg.UTC.dwMonth - 1, msg.UTC.dwDay, msg.UTC.dwHour, msg.UTC.dwMinute, msg.UTC.dwSecond).getTime();
        byte[] byteBuffer = pBuffer.getByteArray(0, dwBufSize);
        NetSDKLib.DH_RECT boundingBox = msg.stuObject.stuOriginalBoundingBox;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer);
        BufferedImage fullImage = ImageIO.read(byteArrayInputStream);
        int leftPadding = 20;
        int rightPadding = 20;
        int topPadding = 50;
        int bottomPadding = 50;
        int left = boundingBox.left.intValue() - leftPadding > 0 ? boundingBox.left.intValue() - leftPadding : boundingBox.left.intValue();
        int right = boundingBox.right.intValue() + rightPadding < fullImage.getWidth() ? boundingBox.right.intValue() + rightPadding : boundingBox.right.intValue();
        int top = boundingBox.top.intValue() - topPadding > 0 ? boundingBox.top.intValue() - topPadding : boundingBox.top.intValue();
        int bottom = boundingBox.bottom.intValue() + bottomPadding < fullImage.getHeight() ? boundingBox.bottom.intValue() + bottomPadding : boundingBox.bottom.intValue();
        BufferedImage faceImage = fullImage.getSubimage(left, top, right - left, bottom - top);
        if (!idString.isEmpty() && fullImage != null) {
          id = Long.parseLong(idString);
          attendanceCacheProducer.produceCache(id, time, cameraType, fullImage, faceImage);
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
