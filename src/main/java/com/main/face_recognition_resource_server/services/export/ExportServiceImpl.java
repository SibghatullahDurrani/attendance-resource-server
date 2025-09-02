package com.main.face_recognition_resource_server.services.export;

import com.main.face_recognition_resource_server.DTOS.attendance.ExcelAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExportAttendanceExcelDataPropsDTO;
import com.main.face_recognition_resource_server.constants.ExportMode;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        log.info("List Count: {}", attendanceExcelDataDTO.size());
        Workbook workbook = new XSSFWorkbook();
        createCheckInCheckOutSheet(workbook, attendanceExcelDataDTO);
        createAttendanceAnalyticsSheet(workbook, attendanceExcelDataDTO);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());

    }

    private void createAttendanceAnalyticsSheet(Workbook workbook, List<AttendanceExcelDataDTO> attendanceExcelDataDTO) {
        Sheet attendanceAnalyticsSheet = workbook.createSheet("Attendance Analytics");

        List<Date> distinctDates = attendanceExcelDataDTO.stream()
                .flatMap(dto -> dto.getAttendances().stream())
                .map(ExcelAttendanceDTO::getDate)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        HashMap<Date, Integer> dateCellIndexMap = IntStream.range(0, distinctDates.size())
                .boxed()
                .collect(Collectors.toMap(
                        distinctDates::get,
                        i -> i + 3,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Row headerRow = attendanceAnalyticsSheet.createRow(0);
        headerRow.createCell(0).setCellValue("First Name");
        headerRow.createCell(1).setCellValue("Second Name");
        headerRow.createCell(2).setCellValue("Division");

        int headerIndex = 3;
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-MMM-yy"));
        dateCellStyle.setAlignment(HorizontalAlignment.LEFT);

        for (Date date : dateCellIndexMap.keySet()) {
            Cell dateColumnCell = headerRow.createCell(headerIndex++);
            dateColumnCell.setCellValue(date);
            dateColumnCell.setCellStyle(dateCellStyle);
        }

        int rowNum = 1;
        for (AttendanceExcelDataDTO attendance : attendanceExcelDataDTO) {
            Row row = attendanceAnalyticsSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attendance.getFirstName());
            row.createCell(1).setCellValue(attendance.getSecondName());
            row.createCell(2).setCellValue(attendance.getDivision());
            for (ExcelAttendanceDTO excelAttendance : attendance.getAttendances()) {
                if (dateCellIndexMap.containsKey(excelAttendance.getDate())) {
                    Integer index = dateCellIndexMap.get(excelAttendance.getDate());
                    switch (excelAttendance.getAttendanceStatus()) {
                        case ON_TIME -> row.createCell(index).setCellValue("Present");
                        case LATE -> row.createCell(index).setCellValue("Present (Late)");
                        case ABSENT -> row.createCell(index).setCellValue("Absent");
                        case ON_LEAVE -> row.createCell(index).setCellValue("On Leave");
                    }
                }
            }
        }

        attendanceAnalyticsSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headerIndex));

        for (int i = 0; i < headerIndex + 1; i++) {
            attendanceAnalyticsSheet.autoSizeColumn(i);
        }
    }

    private void createCheckInCheckOutSheet(Workbook workbook, List<AttendanceExcelDataDTO> attendanceExcelDataDTO) {
        Sheet checkInCheckOutSheet = workbook.createSheet("Check Ins And Check Outs");

        Row headerRow = checkInCheckOutSheet.createRow(0);
        headerRow.createCell(0).setCellValue("First Name");
        headerRow.createCell(1).setCellValue("Second Name");
        headerRow.createCell(2).setCellValue("Division");
        headerRow.createCell(3).setCellValue("Designation");
        headerRow.createCell(4).setCellValue("Date");
        headerRow.createCell(5).setCellValue("Check In");
        headerRow.createCell(6).setCellValue("Check Out");
        headerRow.createCell(7).setCellValue("Status");

        CreationHelper creationHelper = workbook.getCreationHelper();

        CellStyle timeCellStyle = workbook.createCellStyle();
        timeCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("HH:mm"));
        timeCellStyle.setAlignment(HorizontalAlignment.LEFT);
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-MMM-yy"));
        dateCellStyle.setAlignment(HorizontalAlignment.LEFT);

        int rowNum = 1;
        for (AttendanceExcelDataDTO attendance : attendanceExcelDataDTO) {
            for (ExcelAttendanceDTO excelAttendance : attendance.getAttendances()) {
                Row row = checkInCheckOutSheet.createRow(rowNum++);

                row.createCell(0).setCellValue(attendance.getFirstName());
                row.createCell(1).setCellValue(attendance.getSecondName());
                row.createCell(2).setCellValue(attendance.getDivision());
                row.createCell(3).setCellValue(attendance.getDesignation());
                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(excelAttendance.getDate());
                dateCell.setCellStyle(dateCellStyle);
                Cell checkInCell = row.createCell(5);
                checkInCell.setCellValue(excelAttendance.getCheckIn());
                checkInCell.setCellStyle(timeCellStyle);
                Cell checkOutCell = row.createCell(6);
                checkOutCell.setCellValue(excelAttendance.getCheckOut());
                checkOutCell.setCellStyle(timeCellStyle);
                switch (excelAttendance.getAttendanceStatus()) {
                    case ON_TIME -> row.createCell(7).setCellValue("Present");
                    case LATE -> row.createCell(7).setCellValue("Present (Late)");
                    case ABSENT -> row.createCell(7).setCellValue("Absent");
                    case ON_LEAVE -> row.createCell(7).setCellValue("On Leave");
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            checkInCheckOutSheet.autoSizeColumn(i);
        }
        checkInCheckOutSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 7));
    }
}
