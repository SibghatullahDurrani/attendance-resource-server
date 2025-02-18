package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
public class CheckInServicesImpl implements CheckInServices {
  private final File picturePath;
  private final CheckInRepository checkInRepository;

  public CheckInServicesImpl(CheckInRepository checkInRepository) {
    this.checkInRepository = checkInRepository;
    picturePath = new File("./AnalyzerPicture/");
    if (!picturePath.exists()) {
      picturePath.mkdirs();
    }
  }

  @Override
  public void saveCheckIn(Date date, Attendance attendance, BufferedImage image) throws IOException {
    String snapPicPath = picturePath + "/" + System.currentTimeMillis() + "FaceRecognition.jpg";
    ImageIO.write(image, "jpg", new File(snapPicPath));
    CheckIn checkIn = CheckIn.builder()
            .date(date)
            .attendance(attendance)
            .imagePath(snapPicPath)
            .build();
    checkInRepository.saveAndFlush(checkIn);
  }
}
