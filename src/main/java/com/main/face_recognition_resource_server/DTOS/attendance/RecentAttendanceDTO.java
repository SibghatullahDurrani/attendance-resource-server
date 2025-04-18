package com.main.face_recognition_resource_server.DTOS.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecentAttendanceDTO {
  private long userId;
  private Date date;
  private String fullImageName;
  private String faceImageName;

}
