package com.main.face_recognition_resource_server.services.checkout;

import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceSnapshotDTO;
import com.main.face_recognition_resource_server.DTOS.attendance.GetAttendanceSnapPathDTO;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import com.main.face_recognition_resource_server.domains.Attendance;
import com.main.face_recognition_resource_server.domains.CheckOut;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CheckOutServiceImpl implements CheckOutService {
    private final File picturePath;
    private final CheckOutRepository checkOutRepository;

    public CheckOutServiceImpl(CheckOutRepository checkOutRepository) {
        this.checkOutRepository = checkOutRepository;
        picturePath = new File("./FaceRecognition/");
        if (!picturePath.exists()) {
            picturePath.mkdirs();
        }
    }

    @Override
    @Transactional
    public void saveCheckOut(Instant date, Attendance attendance, BufferedImage fullImage, BufferedImage faceImage) throws IOException {
        CheckOut checkOut = CheckOut.builder()
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
            checkOut.setFullImageName(fullImageSnapName);
            checkOut.setFaceImageName(faceImageSnapName);
        }
        checkOutRepository.saveAndFlush(checkOut);
    }

    @Override
    public String getAverageCheckOutOfAttendances(List<Long> attendanceIds, String timezone) {
        List<Instant> checkOutDates = checkOutRepository.getCheckOutDatesOfAttendanceIds(attendanceIds);

        ZoneId zone = ZoneId.of(timezone);
        long totalSeconds = 0;
        for (Instant checkOutDate : checkOutDates) {
            totalSeconds += checkOutDate.atZone(zone).toLocalTime().toSecondOfDay();
        }

        long averageSeconds = totalSeconds / checkOutDates.size();
        LocalTime averageTime = LocalTime.ofSecondOfDay(averageSeconds);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        return averageTime.format(formatter).toLowerCase();
    }

    @Override
    public List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> getCheckOutSnapshotsOfAttendance(Long attendanceId) {
        List<GetAttendanceSnapPathDTO> checkOutSnapPaths = checkOutRepository.getCheckOutSnapPathsOfAttendance(attendanceId);
        List<AttendanceSnapshotDTO.AttendanceSnapShotDTOData> attendanceSnapshots = new ArrayList<>();
        checkOutSnapPaths.forEach(snapPath -> attendanceSnapshots.add(
                AttendanceSnapshotDTO.AttendanceSnapShotDTOData.builder()
                        .snapName(snapPath.getSnapPath())
                        .attendanceType(AttendanceType.CHECK_OUT)
                        .attendanceTime(snapPath.getAttendanceTime())
                        .build()
        ));
        return attendanceSnapshots;
    }

    @Override
    public List<Long> getCheckOutTimesByAttendanceId(Long attendanceId) {
        List<Instant> checkOutDates = checkOutRepository.getCheckOutDatesOfAttendanceId(attendanceId);
        return checkOutDates.stream().map(Instant::toEpochMilli).toList();
    }
}