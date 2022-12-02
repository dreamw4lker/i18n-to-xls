package org.dreamw4lker.transylvania.service;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Сервис подготовки XLS-файла переводов
 *
 * @author Alexander Shkirkov
 */
public class XlsMakeService {

    private final String langFrom;
    private final String langTo;

    /**
     * Значения этой Map - массив из 3 элементов:
     * ["путь до файла", "перевод на исходном языке", "перевод на языке-результате"]
     */
    private final Map<String, String[]> jsonKVMap;

    public XlsMakeService(String langFrom, String langTo, Map<String, String[]> jsonKVMap) {
        this.langFrom = langFrom;
        this.langTo = langTo;
        this.jsonKVMap = jsonKVMap;
    }

    /**
     * Создание стиля для ячеек с жирным шрифтом (например, для заголовков колонок таблиц)
     */
    private HSSFCellStyle createBoldCellStyle(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);
        style.setFont(boldFont);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        return style;
    }

    /**
     * Создание стиля для "разблокированных" ячеек
     */
    private HSSFCellStyle createUnlockedCellStyle(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        style.setLocked(false);
        return style;
    }

    /**
     * Создание ячеек-заголовков
     */
    private HSSFCell createHeaderCell(String value, int columnIndex, HSSFRow row, HSSFCellStyle style) {
        HSSFCell headerCell = row.createCell(columnIndex);
        headerCell.setCellValue(value);
        headerCell.setCellStyle(style);
        return headerCell;
    }

    /**
     * Основной метод создания XLS-файла
     */
    public HSSFWorkbook createWorkbook() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(MessageFormat.format("Перевод {0} -> {1}", this.langFrom, this.langTo));
        sheet.protectSheet("12345678");

        HSSFRow header = sheet.createRow(0);
        HSSFCellStyle boldCellStyle = createBoldCellStyle(workbook);
        createHeaderCell("ID строки", 0, header, boldCellStyle);
        createHeaderCell("Путь до файла", 1, header, boldCellStyle);
        createHeaderCell(MessageFormat.format("{0} перевод", this.langFrom), 2, header, boldCellStyle);
        createHeaderCell(MessageFormat.format("{0} перевод", this.langTo), 3, header, boldCellStyle);

        CellStyle unlockedCellStyle = createUnlockedCellStyle(workbook);

        int i = 1;
        for (Map.Entry<String, String[]> entry : this.jsonKVMap.entrySet()) {
            HSSFRow row = sheet.createRow(i);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue()[0]);
            row.createCell(2).setCellValue(entry.getValue()[1]);

            Cell toLangCell = row.createCell(3);
            toLangCell.setCellValue(entry.getValue()[2]);
            toLangCell.setCellStyle(unlockedCellStyle);
            i++;
        }

        sheet.autoSizeColumn(0);
        sheet.setColumnWidth(1, 0); //Колонка "Путь до файла" будет скрыта (шириной в 0 точек)
        sheet.autoSizeColumn(2);
        //3й столбец часто будет пустой, поэтому autosize делать не следует, всегда ставим ширину в 100 символов.
        //Здесь ширина вычисляется как 1/256 от символа
        sheet.setColumnWidth(3, 100 * 256);

        return workbook;
    }
}
