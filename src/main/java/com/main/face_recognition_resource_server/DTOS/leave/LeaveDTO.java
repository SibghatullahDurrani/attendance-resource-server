package com.main.face_recognition_resource_server.DTOS.leave;

import com.main.face_recognition_resource_server.constants.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LeaveDTO {
  private long id;
  private long date;
  private LeaveStatus status;

  public LeaveDTO(long id, Date date, LeaveStatus status) {
    this.id = id;
    this.date = date.getTime();
    this.status = status;
  }
}
