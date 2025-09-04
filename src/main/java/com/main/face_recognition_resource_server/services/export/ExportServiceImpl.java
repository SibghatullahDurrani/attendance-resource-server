package com.main.face_recognition_resource_server.services.export;

import com.main.face_recognition_resource_server.DTOS.attendance.ExcelAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.export.*;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportServiceImpl implements ExportService {
    private final AttendanceRepository attendanceRepository;
    private final CheckInRepository checkInRepository;
    private final CheckOutRepository checkOutRepository;


    public ExportServiceImpl(AttendanceRepository attendanceRepository, CheckInRepository checkInRepository, CheckOutRepository checkOutRepository) {
        this.attendanceRepository = attendanceRepository;
        this.checkInRepository = checkInRepository;
        this.checkOutRepository = checkOutRepository;
    }

    @Override
    public List<AttendanceExcelDataDTO> getDepartmentAttendanceExcelData(List<Long> userIds, Long fromDate, Long toDate) {
        Date[] startAndEndDate = DateUtils.getStartAndEndDateOfRange(fromDate, toDate);
        List<AttendanceExcelDataDTO> attendanceData = attendanceRepository.getDepartmentsAttendanceExcelData(startAndEndDate[0], startAndEndDate[1], userIds);
        addExcelAttendances(attendanceData, startAndEndDate);
        return attendanceData;
    }

    @Override
    public List<AttendanceExcelDataDTO> getUserAttendanceExcelData(List<Long> userIds, Long fromDate, Long toDate) {
        Date[] startAndEndDate = DateUtils.getStartAndEndDateOfRange(fromDate, toDate);
        List<AttendanceExcelDataDTO> attendanceData = attendanceRepository.getUsersAttendanceExcelData(startAndEndDate[0], startAndEndDate[1], userIds);
        addExcelAttendances(attendanceData, startAndEndDate);
        return attendanceData;
    }

    @Override
    public List<DepartmentAttendanceLineChartDTO> getDepartmentsAttendanceLineChartData(List<Long> departmentIds, Long fromDate, Long toDate) {
        Date[] startAndEndDate = DateUtils.getStartAndEndDateOfRange(fromDate, toDate);
        List<FlatAttendanceExcelChartDTO> flatAttendanceExcelChartData = attendanceRepository.getExcelAttendanceChartData(departmentIds, startAndEndDate[0], startAndEndDate[1]);
        return flatAttendanceExcelChartData.stream()
                .collect(Collectors.groupingBy(
                        FlatAttendanceExcelChartDTO::getDepartmentId,
                        Collectors.collectingAndThen(Collectors.toList(), rows -> {
                            FlatAttendanceExcelChartDTO first = rows.getFirst();
                            List<ExcelDepartmentAttendanceCountDTO> attendanceCount = rows.stream()
                                    .map(row -> ExcelDepartmentAttendanceCountDTO
                                            .builder()
                                            .date(row.getDate())
                                            .onTime(row.getOnTime())
                                            .late(row.getLate())
                                            .absent(row.getAbsent())
                                            .onLeave(row.getOnLeave())
                                            .build()
                                    ).toList();
                            return DepartmentAttendanceLineChartDTO
                                    .builder()
                                    .departmentId(first.getDepartmentId())
                                    .departmentName(first.getDepartmentName())
                                    .excelDepartmentAttendances(attendanceCount)
                                    .build();
                        })
                ))
                .values()
                .stream()
                .toList();
    }

    @Override
    public List<UserAttendancePieChartDTO> getUserAttendancePieChartData(List<Long> userIds, Long fromDate, Long toDate) {
        Date[] startAndEndDate = DateUtils.getStartAndEndDateOfRange(fromDate, toDate);
        return attendanceRepository.getUsersAttendancePieChartData(startAndEndDate[0], startAndEndDate[1], userIds);
    }

    private void addExcelAttendances(List<AttendanceExcelDataDTO> attendanceData, Date[] startAndEndDate) {
        for (AttendanceExcelDataDTO attendance : attendanceData) {
            List<ExcelAttendanceDTO> excelAttendances = attendanceRepository.getUserExcelAttendance(startAndEndDate[0], startAndEndDate[1], attendance.getUserId());
            for (ExcelAttendanceDTO excelAttendance : excelAttendances) {
                excelAttendance.setCheckIn(checkInRepository.getFirstCheckInDateOfAttendanceId(excelAttendance.getAttendanceId()));
                excelAttendance.setCheckOut(checkOutRepository.getLastCheckOutDateOfAttendanceId(excelAttendance.getAttendanceId()));
            }
            attendance.setAttendances(excelAttendances);
        }
    }
}
