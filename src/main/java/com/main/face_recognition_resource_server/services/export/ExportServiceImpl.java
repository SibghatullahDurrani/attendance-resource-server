package com.main.face_recognition_resource_server.services.export;

import com.main.face_recognition_resource_server.DTOS.export.AttendanceExcelDataDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExportAttendanceExcelDataPropsDTO;
import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.ExportMode;
import com.main.face_recognition_resource_server.repositories.CheckInRepository;
import com.main.face_recognition_resource_server.repositories.CheckOutRepository;
import com.main.face_recognition_resource_server.repositories.attendance.AttendanceRepository;
import com.main.face_recognition_resource_server.utilities.DateUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
        if (exportExcelProps.getExportMode() == ExportMode.USER) {
            attendanceData = attendanceRepository.getUsersAttendanceExcelData(startAndEndDate[0], startAndEndDate[1], exportExcelProps.getUserIds());
        } else {
            attendanceData = attendanceRepository.getDepartmentsAttendanceExcelData(startAndEndDate[0], startAndEndDate[1], exportExcelProps.getUserIds());
        }
        for (AttendanceExcelDataDTO attendance : attendanceData) {
            attendance.setCheckIn(checkInRepository.getFirstCheckInDateOfAttendanceId(attendance.getAttendanceId()));
            attendance.setCheckOut(checkOutRepository.getLastCheckOutDateOfAttendanceId(attendance.getAttendanceId()));
        }
        return attendanceData;
    }

    @Override
    public ByteArrayResource getAttendanceWorkBook(List<AttendanceExcelDataDTO> attendanceExcelDataDTO) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet checkInCheckOutSheet = workbook.createSheet("Check Ins And Check Outs");

        Row headerRow = checkInCheckOutSheet.createRow(0);
        headerRow.createCell(0).setCellValue("First Name");
        headerRow.createCell(1).setCellValue("Second Name");
        headerRow.createCell(2).setCellValue("Division");
        headerRow.createCell(3).setCellValue("Designation");
        headerRow.createCell(4).setCellValue("Date");
        headerRow.createCell(5).setCellValue("Check In Time");
        headerRow.createCell(6).setCellValue("Check Out Time");
        headerRow.createCell(7).setCellValue("Is Late?");

        CreationHelper creationHelper = workbook.getCreationHelper();

        CellStyle timeCellStyle = workbook.createCellStyle();
        timeCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("HH:mm"));
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        int rowNum = 1;
        for (AttendanceExcelDataDTO attendance : attendanceExcelDataDTO) {
            Row row = checkInCheckOutSheet.createRow(rowNum++);

            row.createCell(0).setCellValue(attendance.getFirstName());
            row.createCell(1).setCellValue(attendance.getSecondName());
            row.createCell(2).setCellValue(attendance.getDivision());
            row.createCell(3).setCellValue(attendance.getDesignation());
            Cell dateCell = row.createCell(4);
            dateCell.setCellValue(attendance.getDate());
            dateCell.setCellStyle(dateCellStyle);
            Cell checkInCell = row.createCell(5);
            checkInCell.setCellValue(attendance.getCheckIn());
            checkInCell.setCellStyle(timeCellStyle);
            Cell checkOutCell = row.createCell(6);
            checkOutCell.setCellValue(attendance.getCheckOut());
            checkOutCell.setCellStyle(timeCellStyle);
            if (attendance.getAttendanceStatus() == AttendanceStatus.LATE) {
                row.createCell(7).setCellValue("Yes");
            } else {
                row.createCell(8).setCellValue("No");
            }
        }

        for (int i = 0; i < 8; i++) {
            checkInCheckOutSheet.autoSizeColumn(i);
        }
        checkInCheckOutSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 7));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());

    }
}
