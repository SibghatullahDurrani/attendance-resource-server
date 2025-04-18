package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendanceLiveFeedDTO {
  private long userId;
  private String fullName;
  private AttendanceType attendanceType;
  private long date;
  private byte[] fullImage;
  private byte[] faceImage;
}
