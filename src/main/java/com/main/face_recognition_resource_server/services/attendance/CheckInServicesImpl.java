package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.CheckInDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class CheckInServicesImpl implements CheckInServices {
  private final File picturePath;
  private final CheckInRepository checkInRepository;

  public CheckInServicesImpl(CheckInRepository checkInRepository) {
    this.checkInRepository = checkInRepository;
    picturePath = new File("./FaceRecognition/");
    if (!picturePath.exists()) {
      picturePath.mkdirs();
    }
  }

  @Override
  @Transactional
  public void saveCheckIn(Date date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException {
    String uuid = UUID.randomUUID().toString();
    String fullImageSnapName = uuid + "full-image" + "FaceRecognition.jpg";
    String faceImageSnapName = uuid + "face-image" + "FaceRecognition.jpg";
    String fullImageSnapPath = picturePath + "/" + fullImageSnapName;
    String faceImageSnapPath = picturePath + "/" + faceImageSnapName;
    CheckIn checkIn = CheckIn.builder()
            .date(date)
            .attendance(attendance)
            .fullImagePath(fullImageSnapName)
            .faceImagePath(faceImageSnapName)
            .build();
    checkInRepository.saveAndFlush(checkIn);
    ImageIO.write(fullImage, "jpg", new File(fullImageSnapPath));
    ImageIO.write(faceImage, "jpg", new File(faceImageSnapPath));
  }

  @Override
  public List<CheckInDTO> getCheckInsByAttendanceId(Long attendanceId) {
    return checkInRepository.getCheckInsOfAttendanceId(attendanceId);
  }

  @Override
  public List<Long> getCheckInTimesByAttendanceId(Long attendanceId) {
    List<Date> checkedInDates = checkInRepository.getCheckInDatesOfAttendanceId(attendanceId);
    return checkedInDates.stream().map(Date::getTime).toList();
  }

  @Override
  public String getAverageCheckInOfAttendances(List<Long> attendanceIds) throws NoStatsAvailableException {
    List<Date> checkInDates = checkInRepository.getCheckInDatesOfAttendanceIds(attendanceIds);
    if (checkInDates.size() < 1) throw new NoStatsAvailableException();
    Calendar calendar = GregorianCalendar.getInstance();
    int totalMinutes = 0;
    for (Date date : checkInDates) {
      calendar.setTime(date);
      totalMinutes += calendar.get(Calendar.HOUR_OF_DAY) * 60;
      totalMinutes += calendar.get(Calendar.MINUTE);
    }
    int averageMinutes = totalMinutes / checkInDates.size();
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
  public List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> getCheckInSnapshotsOfAttendance(Long attendanceId) {
    List<GetAttendanceSnapPathDTO> checkInSnapPaths = checkInRepository.getCheckInSnapPathsOfAttendance(attendanceId);
    List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> checkInSnapshots = new ArrayList<>();
    checkInSnapPaths.forEach(snapPath -> checkInSnapshots.add(
            AttendanceSnapshotDTO.AttendanceSnapShotDTOData.builder()
                    .snapName(snapPath.getSnapPath())
                    .attendanceType(AttendanceType.CHECK_IN)
                    .attendanceTime(snapPath.getAttendanceTime())
                    .build()
    ));
    return checkInSnapshots;
  }
}