package sonarqube.diff.report.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.util.HashMap;
import java.util.Map;

public class PoiUtil {

    private PoiUtil() {
        // util
    }

    private static final Map<CellStyleDefinition, CellStyle> CELL_STYLE_CACHE = new HashMap<>();

    static record CellStyleDefinition(PredefinedColor bgColor, PredefinedColor fgColor, boolean bold) {}

    public static void createCell(Workbook wb, Row row, int colIdx, PredefinedColor bgColor, String value) {
        createCell(wb, row, colIdx, bgColor, null, false, value);
    }

    public static void createCell(Workbook wb, Row row, int colIdx, PredefinedColor bgColor, Integer value) {
        createCell(wb, row, colIdx, bgColor, null, false, value);
    }

    public static void createCell(Workbook wb, Row row, int colIdx, PredefinedColor bgColor, PredefinedColor fgColor, boolean bold, Object value) {

        if (wb instanceof XSSFWorkbook xwb) {
            Cell cell = row.createCell(colIdx);

            if(value instanceof Integer ival) {
                cell.setCellValue(ival);
            } else {
                cell.setCellValue(value == null ? "-" : value.toString());
            }

            cell.setCellStyle(getCellStyle(xwb, bgColor, fgColor, bold));

            return;
        }

        throw new IllegalArgumentException("Only XSSFWorkbook class is supported.");
    }

    private static CellStyle getCellStyle(XSSFWorkbook wb, PredefinedColor bgColor, PredefinedColor fgColor, boolean bold) {
        return CELL_STYLE_CACHE.computeIfAbsent(new CellStyleDefinition(bgColor, fgColor, bold), p -> {

            XSSFCellStyle style = wb.createCellStyle();

            XSSFFont font = wb.createFont();
            if(fgColor != null) {
                font.setColor(new XSSFColor(new java.awt.Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()), new DefaultIndexedColorMap()));
            }
            font.setBold(bold);
            style.setFont(font);

            if(bgColor != null) {
                style.setFillForegroundColor(new XSSFColor(new java.awt.Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()), new DefaultIndexedColorMap()));
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }

            return style;
        });
    }
}
