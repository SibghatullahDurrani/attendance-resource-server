package com.main.face_recognition_resource_server.services.export;

import com.main.face_recognition_resource_server.DTOS.attendance.ExcelAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.export.*;
import com.main.face_recognition_resource_server.constants.export.ExportMode;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public List<AttendanceExcelDataDTO> getAttendanceExcelData(ExportAttendanceExcelDataPropsDTO exportExcelProps) {
        Date[] startAndEndDate = DateUtils.getStartAndEndDateOfRange(exportExcelProps.getFromDate(), exportExcelProps.getToDate());
        List<AttendanceExcelDataDTO> attendanceData;
        if (exportExcelProps.getExportMode() == ExportMode.MEMBERS) {
            attendanceData = attendanceRepository.getUsersAttendanceExcelData(startAndEndDate[0], startAndEndDate[1], exportExcelProps.getUserIds());
        } else {
            attendanceData = attendanceRepository.getDepartmentsAttendanceExcelData(startAndEndDate[0], startAndEndDate[1], exportExcelProps.getDepartmentIds());
        }
        for (AttendanceExcelDataDTO attendance : attendanceData) {
            List<ExcelAttendanceDTO> excelAttendances = attendanceRepository.getUserExcelAttendance(startAndEndDate[0], startAndEndDate[1], attendance.getUserId());
            for (ExcelAttendanceDTO excelAttendance : excelAttendances) {
                excelAttendance.setCheckIn(checkInRepository.getFirstCheckInDateOfAttendanceId(excelAttendance.getAttendanceId()));
                excelAttendance.setCheckOut(checkOutRepository.getLastCheckOutDateOfAttendanceId(excelAttendance.getAttendanceId()));
            }
            attendance.setAttendances(excelAttendances);
        }
        return attendanceData;
    }

    @Override
    public ByteArrayResource getAttendanceWorkBook(List<AttendanceExcelDataDTO> attendanceExcelDataDTO) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }

    @Override
    public List<ExcelAttendanceChartDTO> getDivisionsExcelChartData(List<Long> departmentIds, Long fromDate, Long toDate) {
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
                            return ExcelAttendanceChartDTO
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
}
