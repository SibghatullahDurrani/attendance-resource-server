package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceLiveFeedDTO;
import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

public interface AttendanceService {
    void markCheckIn(Long userId, Long endDate, BufferedImage fullImage, BufferedImage faceImage) throws UserDoesntExistException, IOException;

    void markCheckOut(Long userId, Long endDate, BufferedImage fullImage, BufferedImage faceImage) throws IOException, UserDoesntExistException;

    void markAbsentOfUsersOfOrganizationForDate(Long organizationId, Instant markAbsentTime, DayOfWeek dayOfWeek);

    void sendLiveAttendanceFeed(Long organizationId, AttendanceLiveFeedDTO attendanceLiveFeedDTO);

    List<AttendanceLiveFeedDTO> getRecentAttendancesOfOrganization(long organizationId);

    void markLeaveOfUserOnDate(Long userId, Instant date);
}
