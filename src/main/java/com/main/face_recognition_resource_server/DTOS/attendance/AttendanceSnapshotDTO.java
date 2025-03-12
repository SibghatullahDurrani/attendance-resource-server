package com.main.face_recognition_resource_server.DTOS.attendance;

import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AttendanceSnapshotDTO {
  private AttendanceStatus attendanceStatus;
  private List<AttendanceSnapShotDTOData> data;
  private Long dayTime;

  public void addAttendanceSnapshotDTOData(List<AttendanceSnapShotDTOData> data) {
    this.data.addAll(data);
  }

  @AllArgsConstructor
  @Builder
  @Data
  @NoArgsConstructor
  public static class AttendanceSnapShotDTOData {
    private String snapName;
    private AttendanceType attendanceType;
    private Long attendanceTime;
  }
}
