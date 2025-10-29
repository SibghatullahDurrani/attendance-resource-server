package com.main.face_recognition_resource_server.services.checkin;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceLiveFeedDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.RecentAttendanceDTO;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckIn;
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
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public void saveCheckIn(Instant date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException {
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
        List<Instant> checkedInDates = checkInRepository.getCheckInDatesOfAttendanceId(attendanceId);
        return checkedInDates.stream().map(Instant::toEpochMilli).toList();
    }

    @Override
    public String getAverageCheckInOfAttendances(List<Long> attendanceIds, String timezone) {
        List<Instant> checkInDates = checkInRepository.getCheckInDatesOfAttendanceIds(attendanceIds);
        if (checkInDates.isEmpty()) return "-";

        ZoneId zone = ZoneId.of(timezone);
        long totalSeconds = 0;
        for (Instant checkOutDate : checkInDates) {
            totalSeconds += checkOutDate.atZone(zone).toLocalTime().toSecondOfDay();
        }

        long averageSeconds = totalSeconds / checkInDates.size();
        LocalTime averageTime = LocalTime.ofSecondOfDay(averageSeconds);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        return averageTime.format(formatter).toLowerCase();
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
                                .checkInTime(recentAttendance.getDate().toEpochMilli())
                                .attendanceType(AttendanceType.CHECK_IN)
                                .build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return recentLiveFeedCheckIns;
    }

    @Override
    public Instant getFirstCheckInOfAttendanceId(Long attendanceId) {
        return checkInRepository.getFirstCheckInDateOfAttendanceId(attendanceId);
    }
}