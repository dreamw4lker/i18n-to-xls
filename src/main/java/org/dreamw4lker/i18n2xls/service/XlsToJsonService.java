package org.dreamw4lker.i18n2xls.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Сервис переноса строк переводов из XLS-файла в отдельные JSON-файлы
 *
 * @author Alexander Shkirkov
 */
@Slf4j
public class XlsToJsonService {
    private final String workPath;
    private final String xlsPath;
    private final String langTo;

    /**
     * Ключ: "путь до файла".
     * Значение: "ID строки" - "строка перевода"
     */
    private final Map<String, Map<String, String>> filepathMap = new HashMap<>();

    public XlsToJsonService(Map<String, String> args) {
        this.workPath = args.get("path");
        this.xlsPath = args.get("xls");
        this.langTo = args.get("to");
    }

    /**
     * Основная функция преобразования XLS в JSON-файлы
     */
    public void createJsonFiles() {
        xlsToMap();
        mapToJson();
    }

    /**
     * Перевод XLS-файла в key-value map
     */
    private void xlsToMap() {
        File xlsFile = new File(xlsPath);
        try (FileInputStream fileInputStream = new FileInputStream(xlsFile)) {
            HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            //Первая строка с заголовками - она не нужна
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String key = row.getCell(0).getStringCellValue();
                String filepath = row.getCell(1).getStringCellValue();
                //Ячейку с индексом 2 намеренно пропускаем: там исходная строка, запрещённая к редактированию
                String translation = row.getCell(3).getStringCellValue();

                filepathMap.computeIfAbsent(filepath, k -> new TreeMap<>()).put(key, translation);
            }
        } catch (IOException e) {
            log.error("I/O exception at XLS file read. Filepath: {}", xlsPath, e);
        }
    }

    /**
     * Перевод key-value map в JSON
     */
    private void mapToJson() {
        for (Map.Entry<String, Map<String, String>> entry : filepathMap.entrySet()) {
            JsonObject jsonObject = createJsonObject(entry.getValue());
            saveJsonObject(entry.getKey(), jsonObject);
        }
    }

    /**
     * Создание JSON-объекта из key-value map
     *
     * @param values key-value map: "ID строки" - "строка перевода"
     * @return JSON-объект перевода
     */
    private JsonObject createJsonObject(Map<String, String> values) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String[] jsonPath = entry.getKey().split("\\.");
            JsonObject pointer = jsonObject;
            for (int i = 0; i < jsonPath.length; i++) {
                if (pointer.has(jsonPath[i])) {
                    //Возможно, частично вложенность была создана ранее при разборе другой строки. Идём по ней
                    pointer = pointer.getAsJsonObject(jsonPath[i]);
                } else {
                    if (i != jsonPath.length - 1) {
                        //Ещё не весь путь разобран - создаём объекты дальше
                        pointer.add(jsonPath[i], new JsonObject());
                        pointer = pointer.getAsJsonObject(jsonPath[i]);
                    } else {
                        //Весь путь разобран - записываем значение
                        pointer.addProperty(jsonPath[i], entry.getValue());
                    }
                }
            }
        }
        return jsonObject;
    }

    /**
     * Сохранение JSON-объекта в файл
     *
     * @param relativePath относительный путь до файла
     * @param jsonObject JSON-объект
     */
    private void saveJsonObject(String relativePath, JsonObject jsonObject) {
        String absolutePath = workPath + File.separator + langTo + File.separator + relativePath;
        try {
            File file = new File(absolutePath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileUtils.write(file, gson.toJson(jsonObject), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("I/O exception at JSON file save. Filepath: {}", absolutePath, e);
        }
    }
}
