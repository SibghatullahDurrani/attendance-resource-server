package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceLiveFeedDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.RecentAttendanceDTO;
import com.main.face_recognition_resource_server.constants.AttendanceType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import com.main.face_recognition_resource_server.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class CheckInServiceImpl implements CheckInService {
    private final File picturePath;
    private final CheckInRepository checkInRepository;
    private final UserService userService;

    public CheckInServiceImpl(CheckInRepository checkInRepository, UserService userService) {
        this.checkInRepository = checkInRepository;
        picturePath = new File("./FaceRecognition/");
        if (!picturePath.exists()) {
            picturePath.mkdirs();
        }
        this.userService = userService;
    }

    @Override
    @Transactional
    public void saveCheckIn(Date date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException {
        CheckIn checkIn = CheckIn.builder()
                .date(date)
                .attendance(attendance)
                .build();
        if (fullImage != null && faceImage != null) {
            String uuid = UUID.randomUUID().toString();
            String fullImageSnapName = uuid + "full-image" + "FaceRecognition.jpg";
            String faceImageSnapName = uuid + "face-image" + "FaceRecognition.jpg";
            String fullImageSnapPath = picturePath + "/" + fullImageSnapName;
            String faceImageSnapPath = picturePath + "/" + faceImageSnapName;
            ImageIO.write(fullImage, "jpg", new File(fullImageSnapPath));
            ImageIO.write(faceImage, "jpg", new File(faceImageSnapPath));
            checkIn.setFullImageName(fullImageSnapName);
            checkIn.setFaceImageName(faceImageSnapName);
        }
        checkInRepository.saveAndFlush(checkIn);
    }

    @Override
    public List<Long> getCheckInTimesByAttendanceId(Long attendanceId) {
        List<Date> checkedInDates = checkInRepository.getCheckInDatesOfAttendanceId(attendanceId);
        return checkedInDates.stream().map(Date::getTime).toList();
    }

    @Override
    public String getAverageCheckInOfAttendances(List<Long> attendanceIds) throws NoStatsAvailableException {
        List<Date> checkInDates = checkInRepository.getCheckInDatesOfAttendanceIds(attendanceIds);
        if (checkInDates.size() < 1) throw new NoStatsAvailableException();
        Calendar calendar = GregorianCalendar.getInstance();
        int totalMinutes = 0;
        for (Date date : checkInDates) {
            calendar.setTime(date);
            totalMinutes += calendar.get(Calendar.HOUR_OF_DAY) * 60;
            totalMinutes += calendar.get(Calendar.MINUTE);
        }
        int averageMinutes = totalMinutes / checkInDates.size();
        int averageHours = 0;
        String ampm = "am";
        while (averageMinutes > 60) {
            averageMinutes -= 60;
            averageHours++;

            if (averageHours == 13) {
                averageHours = 1;
            }
            if (averageHours == 12) {
                ampm = "pm";
            }
        }
        String averageHoursString = averageHours < 10 ? "0" + averageHours : String.valueOf(averageHours);
        String averageMinutesString = averageMinutes < 10 ? "0" + averageMinutes : String.valueOf(averageMinutes);
        return averageHoursString + ":" + averageMinutesString + " " + ampm;
    }

    @Override
    public List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> getCheckInSnapshotsOfAttendance(Long attendanceId) {
        List<GetAttendanceSnapPathDTO> checkInSnapPaths = checkInRepository.getCheckInSnapPathsOfAttendance(attendanceId);
        List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> checkInSnapshots = new ArrayList<>();
        checkInSnapPaths.forEach(snapPath -> checkInSnapshots.add(
                AttendanceSnapshotDTO.AttendanceSnapShotDTOData.builder()
                        .snapName(snapPath.getSnapPath())
                        .attendanceType(AttendanceType.CHECK_IN)
                        .attendanceTime(snapPath.getAttendanceTime())
                        .build()
        ));
        return checkInSnapshots;
    }

    @Override
    public List<AttendanceLiveFeedDTO> getFirstCheckInsOfAttendanceIdsForLiveAttendanceFeed(List<Long> attendanceIds) {
        List<RecentAttendanceDTO> recentAttendances = checkInRepository.getFirstCheckInsOfAttendanceIds(attendanceIds);
        List<AttendanceLiveFeedDTO> recentLiveFeedCheckIns = new ArrayList<>();
        for (RecentAttendanceDTO recentAttendance : recentAttendances) {
            String fullName = userService.getUserFullNameByUserId(recentAttendance.getUserId());
            String sourceImageURI = "SourceFaces/%s%s".formatted(recentAttendance.getUserId(), ".jpg");

            try {
                Path sourceImagePath = Paths.get(sourceImageURI);
                byte[] sourceImage = Files.readAllBytes(sourceImagePath);
                recentLiveFeedCheckIns.add(
                        AttendanceLiveFeedDTO.builder()
                                .userId(recentAttendance.getUserId())
                                .fullName(fullName)
                                .sourceImage(sourceImage)
                                .checkInTime(recentAttendance.getDate().getTime())
                                .attendanceType(AttendanceType.CHECK_IN)
                                .build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return recentLiveFeedCheckIns;
    }

    @Override
    public Date getFirstCheckInOfAttendanceId(Long attendanceId) {
        return checkInRepository.getFirstCheckInDateOfAttendanceId(attendanceId);
    }
}