package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceLiveFeedDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.RecentAttendanceDTO;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckOut;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class CheckOutServicesImpl implements CheckOutServices {
  private final File picturePath;
  private final CheckOutRepository checkOutRepository;
  private final UserServices userServices;

  public CheckOutServicesImpl(CheckOutRepository checkOutRepository, UserServices userServices) {
    this.checkOutRepository = checkOutRepository;
    this.userServices = userServices;
    picturePath = new File("./FaceRecognition/");
    if (!picturePath.exists()) {
      picturePath.mkdirs();
    }
  }

  @Override
  @Transactional
  public void saveCheckOut(Date date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException {
    String uuid = UUID.randomUUID().toString();
    String fullImageSnapName = uuid + "full-image" + "FaceRecognition.jpg";
    String faceImageSnapName = uuid + "face-image" + "FaceRecognition.jpg";
    String fullImageSnapPath = picturePath + "/" + fullImageSnapName;
    String faceImageSnapPath = picturePath + "/" + faceImageSnapName;
    CheckOut checkOut = CheckOut.builder()
            .date(date)
            .attendance(attendance)
            .fullImageName(fullImageSnapName)
            .faceImageName(faceImageSnapName)
            .build();
    checkOutRepository.saveAndFlush(checkOut);
    ImageIO.write(fullImage, "jpg", new File(fullImageSnapPath));
    ImageIO.write(faceImage, "jpg", new File(faceImageSnapPath));
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
    checkOutSnapPaths.forEach(snapPath -> attendanceSnapshots.add(
            AttendanceSnapshotDTO.AttendanceSnapShotDTOData.builder()
                    .snapName(snapPath.getSnapPath())
                    .attendanceType(AttendanceType.CHECK_OUT)
                    .attendanceTime(snapPath.getAttendanceTime())
                    .build()
    ));
    return attendanceSnapshots;
  }

  @Override
  public List<Long> getCheckOutTimesByAttendanceId(Long attendanceId) {
    List<Date> checkOutDates = checkOutRepository.getCheckOutDatesOfAttendanceId(attendanceId);
    return checkOutDates.stream().map(Date::getTime).toList();
  }

  @Override
  public List<AttendanceLiveFeedDTO> getRecentCheckOutsOfAttendanceIdsForLiveAttendanceFeed(List<Long> attendanceIds) {
    List<RecentAttendanceDTO> recentAttendances = checkOutRepository.getRecentCheckOutsOfAttendanceIds(attendanceIds);
    List<AttendanceLiveFeedDTO> recentLiveFeedCheckOuts = new ArrayList<>();
    for (RecentAttendanceDTO recentAttendance : recentAttendances) {
      String fullName = userServices.getUserFullNameByUserId(recentAttendance.getUserId());
      String fullImageURI = "FaceRecognition/%s".formatted(recentAttendance.getFullImageName());
      String faceImageURI = "FaceRecognition/%s".formatted(recentAttendance.getFaceImageName());

      try {
        Path fullImagePath = Paths.get(fullImageURI);
        Path faceImagePath = Paths.get(faceImageURI);
        byte[] fullImage = Files.readAllBytes(fullImagePath);
        byte[] faceImage = Files.readAllBytes(faceImagePath);
        recentLiveFeedCheckOuts.add(
                AttendanceLiveFeedDTO.builder()
                        .userId(recentAttendance.getUserId())
                        .faceImage(faceImage)
                        .fullImage(fullImage)
                        .fullName(fullName)
                        .date(recentAttendance.getDate().getTime())
                        .attendanceType(AttendanceType.CHECK_OUT)
                        .build());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return recentLiveFeedCheckOuts;
  }
}