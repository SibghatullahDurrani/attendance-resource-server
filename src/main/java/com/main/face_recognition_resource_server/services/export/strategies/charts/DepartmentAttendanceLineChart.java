package com.main.face_recognition_resource_server.services.export.strategies.charts;

import com.main.face_recognition_resource_server.DTOS.export.DepartmentAttendanceLineChartDTO;
import com.main.face_recognition_resource_server.DTOS.export.ExcelDepartmentAttendanceCountDTO;
import com.main.face_recognition_resource_server.constants.export.ExcelChartStrategyType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@ExcelChartStrategyKey(ExcelChartStrategyType.DEPARTMENT_ATTENDANCE_LINE_CHART)
public class DepartmentAttendanceLineChart implements ExcelChartStrategy<DepartmentAttendanceLineChartDTO> {
    @Override
    public void create(XSSFWorkbook workbook, List<DepartmentAttendanceLineChartDTO> excelAttendanceChartData) {
        createAllDepartmentsChart(workbook, excelAttendanceChartData);
        for (DepartmentAttendanceLineChartDTO department : excelAttendanceChartData) {
            createSeparateDepartmentChart(workbook, department);
        }
    }

    @Override
    public Class<DepartmentAttendanceLineChartDTO> getExcelChartDTOClass() {
        return DepartmentAttendanceLineChartDTO.class;
    }

    private void createSeparateDepartmentChart(XSSFWorkbook workbook, DepartmentAttendanceLineChartDTO excelAttendanceChartData) {
        XSSFSheet sheet = workbook.createSheet(excelAttendanceChartData.getDepartmentName());
        createHeaderRow(sheet);

        int rowNum = 1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");

        for (ExcelDepartmentAttendanceCountDTO count : excelAttendanceChartData.getExcelDepartmentAttendances()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dateFormat.format(count.getDate()));
            row.createCell(1).setCellValue(count.getOnTime());
            row.createCell(2).setCellValue(count.getLate());
            row.createCell(3).setCellValue(count.getAbsent());
            row.createCell(4).setCellValue(count.getOnLeave());
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 6, 1, 20, 25);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Attendance Trend - " + excelAttendanceChartData.getDepartmentName());
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        int lastRow = rowNum - 1;
        XDDFDataSource<String> dates = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(1, lastRow, 0, 0));

        XDDFNumericalDataSource<Double> onTime = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(1, lastRow, 1, 1));
        XDDFNumericalDataSource<Double> late = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(1, lastRow, 2, 2));
        XDDFNumericalDataSource<Double> absent = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(1, lastRow, 3, 3));
        XDDFNumericalDataSource<Double> onLeave = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(1, lastRow, 4, 4));

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Date");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Count");

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        XDDFLineChartData.Series seriesOnTime = (XDDFLineChartData.Series) data.addSeries(dates, onTime);
        seriesOnTime.setTitle("On Time", null);
        setLineStyle(seriesOnTime, PresetColor.GREEN);

        XDDFLineChartData.Series seriesLate = (XDDFLineChartData.Series) data.addSeries(dates, late);
        seriesLate.setTitle("Late", null);
        setLineStyle(seriesLate, PresetColor.ORANGE);

        XDDFLineChartData.Series seriesAbsent = (XDDFLineChartData.Series) data.addSeries(dates, absent);
        seriesAbsent.setTitle("Absent", null);
        setLineStyle(seriesAbsent, PresetColor.RED);

        XDDFLineChartData.Series seriesOnLeave = (XDDFLineChartData.Series) data.addSeries(dates, onLeave);
        seriesOnLeave.setTitle("On Leave", null);
        setLineStyle(seriesOnLeave, PresetColor.LIME_GREEN);

        chart.plot(data);
    }

    private void createHeaderRow(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Date");
        headerRow.createCell(1).setCellValue("On Time");
        headerRow.createCell(2).setCellValue("Late");
        headerRow.createCell(3).setCellValue("Absent");
        headerRow.createCell(4).setCellValue("On Leave");
    }

    private void createAllDepartmentsChart(XSSFWorkbook workbook, List<DepartmentAttendanceLineChartDTO> excelAttendanceChartData) {
        XSSFSheet allDeptSheet = workbook.createSheet("All Departments Trend");

        createHeaderRow(allDeptSheet);

        Map<Date, long[]> totalsByDate = new TreeMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");

        for (DepartmentAttendanceLineChartDTO dept : excelAttendanceChartData) {
            for (ExcelDepartmentAttendanceCountDTO count : dept.getExcelDepartmentAttendances()) {
                Date dateKey = count.getDate();
                totalsByDate.putIfAbsent(dateKey, new long[4]);
                long[] totals = totalsByDate.get(dateKey);
                totals[0] += count.getOnTime();
                totals[1] += count.getLate();
                totals[2] += count.getAbsent();
                totals[3] += count.getOnLeave();
            }
        }

        int rowNum = 1;
        for (Map.Entry<Date, long[]> entry : totalsByDate.entrySet()) {
            Row row = allDeptSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dateFormat.format(entry.getKey()));
            row.createCell(1).setCellValue(entry.getValue()[0]);
            row.createCell(2).setCellValue(entry.getValue()[1]);
            row.createCell(3).setCellValue(entry.getValue()[2]);
            row.createCell(4).setCellValue(entry.getValue()[3]);
        }

        XSSFDrawing drawing = allDeptSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 6, 1, 20, 25);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Total Attendance Trend (All Departments)");
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        int lastRow = rowNum - 1;

        XDDFDataSource<String> dates = XDDFDataSourcesFactory.fromStringCellRange(allDeptSheet,
                new CellRangeAddress(1, lastRow, 0, 0));

        XDDFNumericalDataSource<Double> onTime = XDDFDataSourcesFactory.fromNumericCellRange(allDeptSheet,
                new CellRangeAddress(1, lastRow, 1, 1));
        XDDFNumericalDataSource<Double> late = XDDFDataSourcesFactory.fromNumericCellRange(allDeptSheet,
                new CellRangeAddress(1, lastRow, 2, 2));
        XDDFNumericalDataSource<Double> absent = XDDFDataSourcesFactory.fromNumericCellRange(allDeptSheet,
                new CellRangeAddress(1, lastRow, 3, 3));
        XDDFNumericalDataSource<Double> onLeave = XDDFDataSourcesFactory.fromNumericCellRange(allDeptSheet,
                new CellRangeAddress(1, lastRow, 4, 4));

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Date");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Total Count");

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        XDDFLineChartData.Series seriesOnTime = (XDDFLineChartData.Series) data.addSeries(dates, onTime);
        seriesOnTime.setTitle("On Time", null);
        setLineStyle(seriesOnTime, PresetColor.GREEN);

        XDDFLineChartData.Series seriesLate = (XDDFLineChartData.Series) data.addSeries(dates, late);
        seriesLate.setTitle("Late", null);
        setLineStyle(seriesLate, PresetColor.ORANGE);

        XDDFLineChartData.Series seriesAbsent = (XDDFLineChartData.Series) data.addSeries(dates, absent);
        seriesAbsent.setTitle("Absent", null);
        setLineStyle(seriesAbsent, PresetColor.RED);

        XDDFLineChartData.Series seriesOnLeave = (XDDFLineChartData.Series) data.addSeries(dates, onLeave);
        seriesOnLeave.setTitle("On Leave", null);
        setLineStyle(seriesOnLeave, PresetColor.LIME_GREEN);

        chart.plot(data);
    }

    private void setLineStyle(XDDFLineChartData.Series series, PresetColor color) {
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));

        XDDFLineProperties lineProperties = new XDDFLineProperties();
        lineProperties.setFillProperties(fill);

        XDDFShapeProperties shapeProperties = new XDDFShapeProperties();
        shapeProperties.setLineProperties(lineProperties);

        series.setShapeProperties(shapeProperties);

        series.setMarkerStyle(MarkerStyle.NONE);
    }

}
