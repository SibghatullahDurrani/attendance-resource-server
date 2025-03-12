package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class CheckInDTO {
  private String date;
  private String imagePath;

  public CheckInDTO(Date date, String imagePath) {
    this.date = date.toString();
    this.imagePath = imagePath;
  }
}
