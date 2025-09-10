package com.main.face_recognition_resource_server.services.export.strategies.sheet.strategies;

import com.main.face_recognition_resource_server.DTOS.attendance.ExcelAttendanceDTO;
import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.constants.export.ExcelSheetCreationStrategyType;
import com.main.face_recognition_resource_server.services.export.strategies.sheet.ExcelSheetStrategy;
import com.main.face_recognition_resource_server.services.export.strategies.sheet.ExcelSheetStrategyKey;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ExcelSheetStrategyKey(ExcelSheetCreationStrategyType.CHECK_IN_CHECK_OUT_SHEET)
public class CheckInCheckOutSheet implements ExcelSheetStrategy {
    @Override
    public void create(XSSFWorkbook workbook, List<AttendanceExcelDataDTO> attendanceExcelData) {
        XSSFSheet checkInCheckOutSheet = workbook.createSheet("Check Ins And Check Outs");

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
        for (AttendanceExcelDataDTO attendance : attendanceExcelData) {
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
