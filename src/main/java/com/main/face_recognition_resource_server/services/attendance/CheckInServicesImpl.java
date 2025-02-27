package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.CheckInDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    return checkInRepository.getCheckInsByAttendanceId(attendanceId);
  }
}
