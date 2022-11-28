package org.dreamw4lker.transylvania.service;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.util.List;
import java.util.Map;

public class XlsMakeService {

    private final String langFrom;
    private final String langTo;
    private final Map<String, List<String>> jsonKVMap;

    public XlsMakeService(String langFrom, String langTo, Map<String, List<String>> jsonKVMap) {
        this.langFrom = langFrom;
        this.langTo = langTo;
        this.jsonKVMap = jsonKVMap;
    }

    private HSSFCellStyle createBoldCellStyle(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);
        style.setFont(boldFont);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        return style;
    }

    private HSSFCellStyle createUnlockedCellStyle(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        style.setLocked(false);
        return style;
    }

    private HSSFCell createHeaderCell(String value, int columnIndex, HSSFRow row, HSSFCellStyle style) {
        HSSFCell headerCell = row.createCell(columnIndex);
        headerCell.setCellValue(value);
        headerCell.setCellStyle(style);
        return headerCell;
    }

    public HSSFWorkbook createWorkbook() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Перевод " + this.langFrom + " -> " + this.langTo);
        sheet.protectSheet("1234"); //TODO: use strict pass. Use properties?

        HSSFRow header = sheet.createRow(0);
        HSSFCellStyle boldCellStyle = createBoldCellStyle(workbook);
        createHeaderCell("ID строки", 0, header, boldCellStyle);
        createHeaderCell("Путь до файла", 1, header, boldCellStyle);
        createHeaderCell(this.langFrom + " перевод", 2, header, boldCellStyle);
        createHeaderCell(this.langTo + " перевод", 3, header, boldCellStyle);

        CellStyle unlockedCellStyle = createUnlockedCellStyle(workbook);

        int i = 1;
        for (Map.Entry<String, List<String>> entry : this.jsonKVMap.entrySet()) {
            HSSFRow row = sheet.createRow(i);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().get(0));
            row.createCell(2).setCellValue(entry.getValue().get(1));

            Cell toLangCell = row.createCell(3);
            toLangCell.setCellValue(entry.getValue().size() > 2 ? entry.getValue().get(2) : ""); //TODO: beautify logic
            toLangCell.setCellStyle(unlockedCellStyle);
            i++;
        }

        sheet.autoSizeColumn(0);
        sheet.setColumnWidth(1, 0); //Hides filepath column
        sheet.autoSizeColumn(2);
        sheet.setColumnWidth(3, 100 * 256); //The unit is 1/256 of character -> the value is 100 chars

        return workbook;
    }
}
