package com.main.face_recognition_resource_server.services.checkout;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface CheckOutService {
    void saveCheckOut(Instant date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException;

    String getAverageCheckOutOfAttendances(List<Long> attendanceIds, String timezone) throws NoStatsAvailableException;

    List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> getCheckOutSnapshotsOfAttendance(Long attendanceId);

    List<Long> getCheckOutTimesByAttendanceId(Long attendanceId);

}
