package com.main.face_recognition_resource_server.DTOS.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserDataDTO {
  private Long id;
  private String firstName;
  private String secondName;
  private byte[] sourceFaceImage;
  private String username;
  private String identificationNumber;
  private String departmentName;
  private String designation;

  public UserDataDTO(Long id, String firstName, String secondName, String username, String identificationNumber, String departmentName, String designation, String sourceFacePictureName) throws IOException {
    this.id = id;
    this.firstName = firstName;
    this.secondName = secondName;
    this.username = username;
    this.identificationNumber = identificationNumber;
    this.departmentName = departmentName;
    this.designation = designation;
    try {
      BufferedImage bufferedImage = ImageIO.read(new File("SourceFaces/" + sourceFacePictureName));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "jpg", baos);
      sourceFaceImage = baos.toByteArray();
      baos.reset();
      baos.close();
    } catch (IOException exception) {
      this.sourceFaceImage = null;
    }
  }
}