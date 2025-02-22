package com.main.face_recognition_resource_server.DTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserAttendanceDTO {
  private Long id;
  private Long userId;
  private String date;
  private List<CheckInDTO> checkIns;
  private List<CheckOutDTO> checkOuts;

  public UserAttendanceDTO(Long id, Long userId, Date date) {
    this.id = id;
    this.userId = userId;
    this.date = date.toString();
  }
}
