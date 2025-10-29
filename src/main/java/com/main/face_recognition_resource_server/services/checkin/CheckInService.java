package com.main.face_recognition_resource_server.services.checkin;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceLiveFeedDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface CheckInService {
    void saveCheckIn(Instant date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException;

    List<Long> getCheckInTimesByAttendanceId(Long attendanceId);

    String getAverageCheckInOfAttendances(List<Long> attendanceIds, String timezone) throws NoStatsAvailableException;

    List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> getCheckInSnapshotsOfAttendance(Long attendanceId);

    List<AttendanceLiveFeedDTO> getFirstCheckInsOfAttendanceIdsForLiveAttendanceFeed(List<Long> attendanceIds);

    Instant getFirstCheckInOfAttendanceId(Long attendanceId);
}
