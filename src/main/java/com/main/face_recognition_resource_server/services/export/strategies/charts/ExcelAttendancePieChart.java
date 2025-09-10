package com.main.face_recognition_resource_server.services.export.strategies.charts;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.springframework.stereotype.Component;

@Component
public class ExcelAttendancePieChart {

    public void createPieChart(
            XSSFWorkbook workbook,
            String title,
            Long onTimeCount,
            Long lateCount,
            Long absentCount,
            Long onLeaveCount) {
        XSSFSheet sheet = workbook.createSheet(title);

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Status");
        headerRow.createCell(1).setCellValue("Count");

        int rowNum = 1;
        rowNum = createRow(sheet, rowNum, "On Time", onTimeCount);
        rowNum = createRow(sheet, rowNum, "Late", lateCount);
        rowNum = createRow(sheet, rowNum, "Absent", absentCount);
        rowNum = createRow(sheet, rowNum, "On Leave", onLeaveCount);

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, 1, 12, 20);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Attendance Distribution - " + title);
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        int lastRow = rowNum - 1;
        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(1, lastRow, 0, 0));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, lastRow, 1, 1));

        XDDFPieChartData data = (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);
        data.setVaryColors(true);

        XDDFPieChartData.Series series = (XDDFPieChartData.Series) data.addSeries(categories, values);
        series.setTitle("Attendance", null);

        chart.plot(data);
        applySliceColors(chart);
    }

    private int createRow(XSSFSheet sheet, int rowNum, String label, Long value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value != null ? value : 0);
        return rowNum + 1;
    }


    private void applySliceColors(XSSFChart chart) {
        // Define your fixed colors (R, G, B)
        byte[][] rgbColors = new byte[][]{new byte[]{(byte) 0, (byte) 128, (byte) 0},    // Green (On Time)
                new byte[]{(byte) 255, (byte) 165, (byte) 0},  // Orange (Late)
                new byte[]{(byte) 255, (byte) 0, (byte) 0},    // Red (Absent)
                new byte[]{(byte) 50, (byte) 205, (byte) 50}   // Lime Green (On Leave)
        };

        // Access underlying XML
        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();
        CTPieChart pieChart = plotArea.getPieChartArray(0);
        CTPieSer ser = pieChart.getSerArray(0);

        for (int i = 0; i < rgbColors.length; i++) {
            CTDPt dPt = ser.addNewDPt();
            dPt.addNewIdx().setVal(i);

            CTSolidColorFillProperties fill = CTSolidColorFillProperties.Factory.newInstance();
            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            rgb.setVal(rgbColors[i]); // directly set byte[3] RGB value
            fill.setSrgbClr(rgb);

            dPt.addNewSpPr().setSolidFill(fill);
        }
    }
}
