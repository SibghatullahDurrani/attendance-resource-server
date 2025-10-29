package com.main.face_recognition_resource_server.services.export.strategies.sheet.strategies;

import com.main.face_recognition_resource_server.DTOS.attendance.ExcelAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.constants.export.ExcelSheetCreationStrategyType;
import com.main.face_recognition_resource_server.services.export.strategies.sheet.ExcelSheetStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.sheet.ExcelSheetStrategyKey;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@ExcelSheetStrategyKey(ExcelSheetCreationStrategyType.ATTENDANCE_ANALYTICS_SHEET)
public class AttendanceAnalyticsSheet implements ExcelSheetStrategy {
    @Override
    public void create(XSSFWorkbook workbook, List<AttendanceExcelDataDTO> attendanceExcelData, String timeZone) {
        XSSFSheet attendanceAnalyticsSheet = workbook.createSheet("Attendance Analytics");

        //TODO DATE CONVERSION CHECK
        List<Instant> distinctDates = attendanceExcelData.stream()
                .flatMap(dto -> dto.getAttendances().stream())
                .map(ExcelAttendanceDTO::getDate)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        //TODO DATE CONVERSION CHECK
        HashMap<Instant, Integer> dateCellIndexMap = IntStream.range(0, distinctDates.size())
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

        //TODO DATE CONVERSION CHECK
        for (Instant date : dateCellIndexMap.keySet()) {
            ZoneId zone = ZoneId.of(timeZone);
            ZonedDateTime zonedDate = ZonedDateTime.ofInstant(date, zone);
            Cell dateColumnCell = headerRow.createCell(headerIndex++);
            //TODO DATE CONVERSION CHECK
            String dateCellText = zonedDate.format(DateTimeFormatter.ofPattern("dd-MMM-yy"));
            dateColumnCell.setCellValue(dateCellText);
        }

        int rowNum = 1;
        for (AttendanceExcelDataDTO attendance : attendanceExcelData) {
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
}
