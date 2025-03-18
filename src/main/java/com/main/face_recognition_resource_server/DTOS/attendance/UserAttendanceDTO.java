package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class UserAttendanceDTO {
  private Long id;
  private Long date;
  private AttendanceStatus status;
  private List<Long> checkIns;
  private List<Long> checkOuts;

  public UserAttendanceDTO(Long id, Date date, AttendanceStatus status) {
    this.id = id;
    this.date = date.getTime();
    this.status = status;
  }
}
