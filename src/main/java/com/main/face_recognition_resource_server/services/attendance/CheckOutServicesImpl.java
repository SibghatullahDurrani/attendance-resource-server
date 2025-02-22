package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.CheckOutDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckOut;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
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
    String snapPicPath = picturePath + "/" + uuid + "FaceRecognition.jpg";
    CheckOut checkOut = CheckOut.builder()
            .date(date)
            .attendance(attendance)
            .imagePath(snapPicPath)
            .build();
    checkOutRepository.saveAndFlush(checkOut);
    ImageIO.write(image, "jpg", new File(snapPicPath));
  }

  @Override
  public List<CheckOutDTO> getCheckOutsByAttendanceId(Long attendanceId) {
    return checkOutRepository.getCheckOutsByAttendanceId(attendanceId);
  }
}
