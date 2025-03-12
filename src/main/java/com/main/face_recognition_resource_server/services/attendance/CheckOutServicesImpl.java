package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.CheckOutDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckOut;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class CheckOutServicesImpl implements CheckOutServices {
  private final File picturePath;
  private final CheckOutRepository checkOutRepository;

  public CheckOutServicesImpl(CheckOutRepository checkOutRepository) {
    this.checkOutRepository = checkOutRepository;
    picturePath = new File("./FaceRecognition/");
    if (!picturePath.exists()) {
      picturePath.mkdirs();
    }
  }

  @Override
  @Transactional
  public void saveCheckOut(Date date, Attendance attendance, BufferedImage image) throws IOException {
    String uuid = UUID.randomUUID().toString();
    String snapName = uuid + "FaceRecognition.jpg";
    String snapPicPath = picturePath + "/" + snapName;
    CheckOut checkOut = CheckOut.builder()
            .date(date)
            .attendance(attendance)
            .imagePath(snapName)
            .build();
    checkOutRepository.saveAndFlush(checkOut);
    ImageIO.write(image, "jpg", new File(snapPicPath));
  }

  @Override
  public List<CheckOutDTO> getCheckOutsByAttendanceId(Long attendanceId) {
    return checkOutRepository.getCheckOutsByAttendanceId(attendanceId);
  }

  @Override
  public String getAverageCheckOutOfAttendances(List<Long> attendanceIds) throws NoStatsAvailableException {
    List<Date> checkOutDates = checkOutRepository.getCheckOutDatesOfAttendanceIds(attendanceIds);
    if (checkOutDates.size() < 1) throw new NoStatsAvailableException();
    Calendar calendar = GregorianCalendar.getInstance();
    int totalMinutes = 0;
    for (Date date : checkOutDates) {
      calendar.setTime(date);
      totalMinutes += calendar.get(Calendar.HOUR_OF_DAY) * 60;
      totalMinutes += calendar.get(Calendar.MINUTE);
    }
    int averageMinutes = totalMinutes / checkOutDates.size();
    int averageHours = 0;
    String ampm = "am";
    while (averageMinutes > 60) {
      averageMinutes -= 60;
      averageHours++;

      if (averageHours == 13) {
        averageHours = 1;
      }
      if (averageHours == 12) {
        ampm = "pm";
      }
    }
    String averageHoursString = averageHours < 10 ? "0" + averageHours : String.valueOf(averageHours);
    String averageMinutesString = averageMinutes < 10 ? "0" + averageMinutes : String.valueOf(averageMinutes);
    return averageHoursString + ":" + averageMinutesString + " " + ampm;
  }

  @Override
  public List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> getCheckOutSnapshotsOfAttendance(Long attendanceId) {
    List<GetAttendanceSnapPathDTO> checkOutSnapPaths = checkOutRepository.getCheckOutSnapPathsOfAttendance(attendanceId);
    List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> attendanceSnapshots = new ArrayList<>();
    checkOutSnapPaths.forEach(snapPath -> {
      attendanceSnapshots.add(
              AttendanceSnapshotDTO.AttendanceSnapShotDTOData.builder()
                      .snapName(snapPath.getSnapPath())
                      .attendanceType(AttendanceType.CHECK_OUT)
                      .attendanceTime(snapPath.getAttendanceTime())
                      .build()
      );
    });
    return attendanceSnapshots;
  }
}