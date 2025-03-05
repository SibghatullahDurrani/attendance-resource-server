package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.CheckInDTO;
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
  public void saveCheckIn(Date date, Attendance attendance, BufferedImage image) throws IOException {
    String uuid = UUID.randomUUID().toString();
    String snapPicPath = picturePath + "/" + uuid + "FaceRecognition.jpg";
    CheckIn checkIn = CheckIn.builder()
            .date(date)
            .attendance(attendance)
            .imagePath(snapPicPath)
            .build();
    checkInRepository.saveAndFlush(checkIn);
    ImageIO.write(image, "jpg", new File(snapPicPath));
  }

  @Override
  public List<CheckInDTO> getCheckInsByAttendanceId(Long attendanceId) {
    return checkInRepository.getCheckInsOfAttendanceId(attendanceId);
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
    while (averageMinutes > 60) {
      averageMinutes -= 60;
      averageHours++;
    }
    String averageHoursString = averageHours < 10 ? "0" + averageHours : String.valueOf(averageHours);
    String averageMinutesString = averageMinutes < 10 ? "0" + averageMinutes : String.valueOf(averageMinutes);
    return averageHoursString + ":" + averageMinutesString;
  }
}